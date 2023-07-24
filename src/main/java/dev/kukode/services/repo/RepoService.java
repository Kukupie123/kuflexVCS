    /*
 * Copyright (C) 25/07/23, 12:18 am KUKODE - Kuchuk Boram Debbarma . - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

    package dev.kukode.services.repo;

    import com.google.gson.Gson;
    import dev.kukode.models.KuflexRepoModel;
    import dev.kukode.models.branches.BranchDB;
    import dev.kukode.models.branches.BranchModel;
    import dev.kukode.models.commits.CommitDB;
    import dev.kukode.models.commits.CommitModel;
    import dev.kukode.models.diffs.DiffDB;
    import dev.kukode.models.diffs.DiffModel;
    import dev.kukode.models.snapshots.SnapshotModel;
    import dev.kukode.services.dirNFile.DirNFileService;
    import dev.kukode.util.ConstantNames;
    import difflib.Delta;
    import difflib.DiffUtils;
    import difflib.Patch;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.stereotype.Service;

    import java.io.File;
    import java.nio.file.DirectoryNotEmptyException;
    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.util.*;

    @Service
    public class RepoService implements IRepoService {
        final Gson gson;
        final DirNFileService dirService;
        Logger logger = LoggerFactory.getLogger(this.getClass());

        public RepoService(Gson gson, DirNFileService dirService) {
            this.gson = gson;
            this.dirService = dirService;
        }


        public static String encode(String input) {
            byte[] encodedBytes = Base64.getEncoder().encode(input.getBytes());
            return new String(encodedBytes);
        }

        public static String decode(String encodedInput) {
            byte[] decodedBytes = Base64.getDecoder().decode(encodedInput);
            return new String(decodedBytes);
        }

        public void initializeRepoz(String projectDir, String projectName, Date creationDate, String creator) throws Exception {
            //Check if KuFlex repo already exists
            if (doesRepoAlreadyExist(projectDir)) {
                throw new DirectoryNotEmptyException("KuFlex Repository already exists");
            }

            //Create a kuflexrepo model
            logger.info("Creating Repository folder with projectName : " + projectName);
            KuflexRepoModel kuflexRepo = new KuflexRepoModel(projectName, creator, creationDate);

            //Create .KuFlex repo directory
            dirService.createKuFlexRepoDir(projectDir);

            //create the kuflex repository file
            dirService.createKuFlexRepoFile(projectDir, kuflexRepo);

            //Create initial branch
            logger.info("Initial Branch Creation");
            BranchModel defaultBranch = createInitialBranch(projectDir, "default");

            //Create initial Commit
            CommitModel defaultCommit = createInitialCommit(projectDir, "Initial Commit", "", defaultBranch.getUID());

            //Update initial Commit value and initial Branch value in kuflexRepo.json
            KuflexRepoModel kuflexRepoModel = dirService.getKuFlexRepoModel(projectDir);
            kuflexRepoModel.activeBranch = defaultBranch.getUID();
            kuflexRepoModel.activeCommit = defaultCommit.getUID();
            kuflexRepoModel.initialBranch = defaultBranch.getUID();
            kuflexRepoModel.initialCommit = defaultCommit.getUID();
            dirService.updateKuFlexRepo(projectDir, kuflexRepoModel);
        }

        public boolean createNewCommitz(String projectDir, String commitName, String comment) throws Exception {
            //TODO: Change create commit function to make diff by loading from initial to final
            var kuflexRepo = dirService.getKuFlexRepoModel(projectDir);
            //Get Current commit
            CommitModel currentCommitModel = dirService.getCommitByID(projectDir, kuflexRepo.activeCommit, kuflexRepo.activeBranch);
            //Create New commit
            CommitModel newCommitModel = new CommitModel(commitName, comment, new Date(), currentCommitModel.getBranchID(), currentCommitModel.getInheritedCommit(), null);
            //Create New commit directory
            dirService.createCommitDir(projectDir, newCommitModel.getBranchID(), newCommitModel.getUID());
            //Add new commit to commitDB of DB
            CommitDB commitDB = dirService.getCommitDbModelForBranch(projectDir, newCommitModel.getBranchID());
            commitDB.commits.add(newCommitModel);
            dirService.updateCommitDbForBranch(projectDir, newCommitModel.getBranchID(), commitDB);
            //Create project snapshot for new commit
            dirService.createCommitSnapshotFile(projectDir, newCommitModel.getUID(), newCommitModel.getBranchID());
            SnapshotModel newSnap = createSnapshotModel(projectDir, newCommitModel);
            dirService.updateCommitSnapshot(projectDir, newCommitModel.getUID(), newCommitModel.getBranchID(), newSnap);

            SnapshotModel currentSnap = dirService.getCommitSnapshotModel(projectDir, currentCommitModel.getUID(), currentCommitModel.getBranchID());
            //Determine which files are new to the commit, these files can simply have their content copy and pasted
            List<String> addedFiles = new ArrayList<>(); //These files do not exist in new commit
            for (String s : newSnap.files) {
                if (!currentSnap.files.contains(s)) {
                    System.out.println(s + " is new. Not found in previous commit");
                    addedFiles.add(s);
                }
            }
            //Remove these paths from newSnap as the file's diffing process is going to be different. We only need to copy it's content
            newSnap.files.removeAll(addedFiles);
            //Copy the added files to the diff directory as diff models, since they are new they do not have any previous file to compare with
            for (String s : addedFiles) {
                DiffModel diffModel = new DiffModel();
                diffModel.path = s;
                diffModel.diff = Files.readString(new File(projectDir + s).toPath());
                dirService.createCommitDiffFile(projectDir, newCommitModel.getUID(), newCommitModel.getBranchID(), diffModel);
            }

            //Load diff files from currentCommit
            List<DiffModel> diffModels = dirService.getAllDiffsOfCommit(projectDir, currentCommitModel.getUID(), currentCommitModel.getBranchID());
            //Load the current file from a project based on new snapshot
            List<File> projectFiles = dirService.getProjectFileBasedOnSnapshot(projectDir, newSnap);
            //Iterate project files to find the DiffModel from the list of DiffModels
            for (File f : projectFiles) {
                //Find currentDiffModel file that represents the file f from currentCommit
                DiffModel currentDiffModel = null;
                String relativePath = f.getPath().replace(projectDir, "");
                System.out.println(relativePath + "============");
                for (DiffModel dm : diffModels) {
                    if (dm.path.equals(relativePath)) {
                        currentDiffModel = dm;
                        break;
                    }
                }
                //Create diff file
                createDiffFile(projectDir, currentDiffModel, newCommitModel, f.getPath());
            }

            //Update active branch and active commit
            var repo = dirService.getKuFlexRepoModel(projectDir);
            repo.activeBranch = newCommitModel.getBranchID();
            repo.activeCommit = newCommitModel.getUID();
            dirService.updateKuFlexRepo(projectDir, repo);

            /*
            Example code of reading from patch
            private static Patch<String> readPatchFromFile(String diffFilePath) throws IOException, PatchFormatException {
        try (BufferedReader reader = new BufferedReader(new FileReader(diffFilePath))) {
            List<String> diffLines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                diffLines.add(line);
            }
            // Parse the patch from the diff lines
            return DiffUtils.parseUnifiedDiff(diffLines);
        }
    }
            // Read the patch file
            Patch<String> patch = readPatchFromFile(diffFilePath);

            // Apply the patch to the original text
            List<String> modifiedLines = (List<String>) DiffUtils.patch(originalLines, patch);

            // Convert the modified lines back to a single text
            String modifiedText = String.join("\n", modifiedLines);

            System.out.println("Modified text:\n" + modifiedText);
             */
            return true;
        }

        public boolean createNewBranchz(String projectDir, String branchName, String branchComment, String inheritedBranch, String inheritedCommit) throws Exception {
            /*
            The first commit of the branch will NEED to have it's branchID as the interhitedBranch ID.
            This is needed to load commits which go through more than one branch.
            When we notice that commits branchID and the branchID of the branch Don't match we know
            that it is the FIRST commit of the branch
            and we can now refer to branch's inherited Commit and inherited BranchID to track.
             */
            return false;
        }

        public void loadCommitz(String projectDir, String commitID, String branchID) throws Exception {
            /*
            1. Make a Linked-List that is going to link initialBranch to the commit we want to load
            2. We can do this by traversing down from the commit we need to load to the initial commit
            3. Load file diffs sequentially from initial to final commit
            4. On the way we will come across files that are present in old commit but not in new commit, we move those in vault
            5. On the way we will come across files that are not present in old commit but present in new commit, we just read their diffs content and load the file
             */

            // Traverse from lastCommit to initialCommit and store them as a linked list
            CommitModel lastCommit = dirService.getCommitByID(projectDir, commitID, branchID);
            LinkedList<CommitModel> commitChain = getCommitChain(projectDir, lastCommit, null);

            int currentCommitIndex = commitChain.size() - 1;

            //Restore the files upto initial branch and commit
            while (true) {
                CommitModel currentCommit = commitChain.get(currentCommitIndex);
            }

            //TODO
            // Iterate the chain from initial all the way to lastCommit,
            // Load file diffs & check file deletion and addition and use vault directory accordingly


        }

        private boolean doesRepoAlreadyExist(String projectDir) {
            File repoDir = new File(projectDir + "\\.kuflex");
            return repoDir.isDirectory();
        }

        private BranchModel createInitialBranch(String projectDir, String branchName) throws Exception {
            //Create BranchModel
            BranchModel branchModel = new BranchModel(branchName, new Date(), "", "");
            //Create branch directory
            dirService.createBranchDirectory(projectDir, branchModel.getUID());
            //Create branch DB
            dirService.createBranchDBFile(projectDir);
            //Write branch to branchDBFile
            BranchDB branchDB = new BranchDB();
            branchDB.branches = new ArrayList<>();
            branchDB.branches.add(branchModel);
            dirService.updateBranchDbFile(projectDir, branchDB);
            return branchModel;
        }

        private CommitModel createInitialCommit(String projectDir, String commitName, String commitComment, String branchID) throws Exception {
            //Create a commit model
            CommitModel commitModel = new CommitModel(commitName, commitComment, new Date(), branchID, null, null);
            //Create commit directory
            dirService.createCommitDir(projectDir, branchID, commitModel.getUID());
            //Create CommitDB file
            dirService.createCommitDBFileForBranch(projectDir, branchID);
            //Write the new commit to commitDBFile file
            CommitDB commitDB = new CommitDB();
            commitDB.commits = new ArrayList<>();
            commitDB.commits.add(commitModel);
            dirService.updateCommitDbForBranch(projectDir, branchID, commitDB);

            //Create snapshot file for the commit
            dirService.createCommitSnapshotFile(projectDir, commitModel.getUID(), commitModel.getBranchID());
            SnapshotModel snapshotModel = createSnapshotModel(projectDir, commitModel);
            //Write a file path to snapshot
            dirService.updateCommitSnapshot(projectDir, commitModel.getUID(), commitModel.getBranchID(), snapshotModel);
            //Create Initial File copy
            createInitialFileCopy(projectDir, snapshotModel, commitModel);

            return commitModel;
        }

        private SnapshotModel createSnapshotModel(String projectDir, CommitModel commitModel) throws Exception {

            //Get file paths
            List<String> filePaths = dirService.getProjectFilesPath(projectDir);
            //Remove file paths that are part of kuFlex
            List<String> pathToRemove = new ArrayList<>();
            for (String path : filePaths) {
                if (path.contains(ConstantNames.KUFLEX)) {
                    pathToRemove.add(path);
                }
            }
            filePaths.removeAll(pathToRemove);

            //Create SnapShotModel and set its values
            SnapshotModel snapshotModel = new SnapshotModel();
            snapshotModel.files = filePaths;
            return snapshotModel;
        }

        private void createInitialFileCopy(String projectDir, SnapshotModel snapshotModel, CommitModel commitModel) throws Exception {
            //Create diffs directory
            dirService.createCommitDiffDirectory(projectDir, commitModel.getUID(), commitModel.getBranchID());
            for (String s : snapshotModel.files) {
                //Get original file
                File file = new File(projectDir + s);

                //Save diff file
                DiffModel diffModel = new DiffModel();
                diffModel.path = s;
                diffModel.diff = Files.readString(file.toPath());
                dirService.createCommitDiffFile(projectDir, commitModel.getUID(), commitModel.getBranchID(), diffModel);

            }


        }

        private void createDiffFile(String projectDir, DiffModel diffModel, CommitModel commitModel, String projectFilePath) throws Exception {
            //TODO:
            // Create our own file diff function
            // and we also need
            // to load from initial branch all the way upto latest branch
            // before we commit so that we get the correct diff content

            if (diffModel == null) {
                throw new Exception("Failed to find diff model for file " + projectFilePath);
            }
            //Load content from currentDiffModel
            String diffContent = diffModel.diff;
            System.out.println(diffContent);
            //Tokenize the content
            List<String> diffContentTokens = Arrays.asList(diffContent.split("\\n"));
            //Load content from file f
            String projectFileContent = Files.readString(Path.of(projectFilePath));
            System.out.println(projectFileContent);
            //Tokenize the content
            List<String> projectFileContentTokens = Arrays.asList(projectFileContent.split("\\n"));
            //Create patch
            Patch<String> patch = DiffUtils.diff(diffContentTokens, projectFileContentTokens);
            //Save patch as diff
            StringBuilder diffFileContent = new StringBuilder();
            for (Delta<String> delta : patch.getDeltas()) {
                System.out.println(delta);
                diffFileContent.append(delta.toString()).append("\n\n");
            }
            //We will reuse currentDiffModel as newDiffModel
            diffModel.diff = diffFileContent.toString();
            dirService.createCommitDiffFile(projectDir, commitModel.getUID(), commitModel.getBranchID(), diffModel);
        }

        private LinkedList<CommitModel> getCommitChain(String projectDir, CommitModel currentCommit, LinkedList<CommitModel> currentList) throws Exception {
            //Null during the first function call, initialize a list and add currentCommit to the list
            if (currentList == null) {
                currentList = new LinkedList<>();
                currentList.add(currentCommit);
            }

            //Check if this commit has inheritedBranchID, If true, then this is an initial commit to a branch
            //Else load inherited commit
            if (currentCommit.getInheritedBranch() != null && !currentCommit.getInheritedBranch().isEmpty() && !currentCommit.getInheritedBranch().equals(currentCommit.getBranchID())) {

                /*
                Since this in inherited commit
                Load Previous commit
                from which this commit was inherited from
                by using inheritedBranchID and inheritedCommitID
                */
                CommitModel prevCommit = dirService.getCommitByID(projectDir, currentCommit.getInheritedCommit(), currentCommit.getInheritedBranch());
                currentList.add(prevCommit);
                return getCommitChain(projectDir, prevCommit, currentList);
            } else {

                //Check if this is the initial branch
                KuflexRepoModel kuflexRepoModel = dirService.getKuFlexRepoModel(projectDir);
                if (currentCommit.getBranchID().equals(kuflexRepoModel.initialBranch)) {
                    if (currentCommit.getUID().equals(kuflexRepoModel.initialCommit)) {
                        currentList.add(currentCommit);
                        return currentList;
                    }
                }

                //Load the previous commit of the same branch by using inherited commit variables value
                CommitModel prevCommit = dirService.getCommitByID(projectDir, currentCommit.getInheritedCommit(), currentCommit.getBranchID());
                currentList.add(prevCommit);
                return getCommitChain(projectDir, prevCommit, currentList);
            }
        }

        @Override
        public void initializeRepo(String projectDir, String projectName, Date creationDate, String creator) throws Exception {
            /*
            1. Create .KuFlex directory
            2. Create KuFlexRepo.json
            3. Create BranchDB, SnapshotDB
            4. Create Branch Model
            5. Create Branch Directory based on branch model ID
            6. Add branch to record to BranchDB
            7. Create CommitDB inside Branch directory
            8. Create new Commit Model
            9. Create a new snapshot with CommitID and branchID as its name
            10. Add the commit model to commitDB of branch
            11. Add snapshot to snapshotDB
            12. Create new folder called diffs
            13. Create a db file for each file path in the snapshot with it's path name in encoded form
            as the dbs file name
            14. Add a new record diffModel for each file with it's index as 0 as its diff as the content of the file from the project
            15. Update Initial and Active branch and commit
             */

            //1, 2
            dirService.createKuFlexRepoDir(projectDir);
            dirService.createKuFlexRepoFile(projectDir, new KuflexRepoModel(projectName, creator, new Date()));

            //3
            dirService.createBranchDBFile(projectDir);
            dirService.createSnapshotDBFile(projectDir);

            //4
            BranchModel initialBranchModel = new BranchModel("Initial Branch", new Date(), null, null);

            //5
            dirService.createBranchDirectory(projectDir, initialBranchModel.getUID());

            //6
            dirService.addBranchToBranchDB(projectDir, initialBranchModel);

            //7
            dirService.createCommitDBFileForBranch(projectDir, initialBranchModel.getUID());

            //8
            CommitModel initialCommitModel = new CommitModel("Initial Commit", "", new Date(), initialBranchModel.getUID(), null, null);

            //9
            String snapShotID = initialBranchModel.getUID() + initialCommitModel.getUID();
            List<String> filePaths = dirService.getProjectFilesPath(projectDir);
            SnapshotModel snapshotModel = new SnapshotModel(snapShotID, filePaths);

            //10
            dirService.addCommitToCommitDB(projectDir, initialBranchModel.getUID(), initialCommitModel);

            //11
            dirService.addSnapshotToSnapshotDB(projectDir, snapshotModel);

            //12
            dirService.createDiffDirectory(projectDir);

            //13
            for (String filePath : filePaths) {
                //Create encoded name from path
                String filePathEncoded = encode(filePath);
                //Read the content from the project for each file
                String diff = "";
                //Create a Diff model
                DiffModel initialDiff = new DiffModel(0, diff, initialCommitModel.getUID(), initialBranchModel.getUID());
                //Create Diff DB and add initial Diff
                DiffDB diffDB = new DiffDB(filePathEncoded);
                diffDB.getDiffModels().add(initialDiff);

                //Add Diff model in the Diff DB
                //14
                dirService.createInitialDiffDB(projectDir, diffDB);
            }

            //15
            var repo = dirService.getKuFlexRepoModel(projectDir);
            repo.initialCommit = initialCommitModel.getUID();
            repo.activeCommit = initialCommitModel.getUID();
            repo.initialCommit = initialBranchModel.getUID();
            repo.activeBranch = initialBranchModel.getUID();
            dirService.updateKuFlexRepo(projectDir, repo);
        }

        @Override
        public boolean createNewCommit(String projectDir, String commitName, String comment) throws Exception {
            /*
            1. Create new Commit Model
            2. Create new snapshot model based on new commit model and branchID
            3. Loop the snapshot and make two lists.
            One will contain files who have diffs, Another will contain files that have no diffs
            4. Create new diff file for the files that have no diff and add 0th record
            5. For files that have diffs, load the diff dbs 0th index upto current commit.
                1. Now create new diff model record
            6. Add snapshot to DB
            7. Add the new commit model to branch -> commitDB record
             */
            return false;
        }

        @Override
        public boolean createNewBranch(String projectDir, String branchName, String branchComment, String inheritedBranch, String inheritedCommit) throws Exception {
            return false;
        }

        @Override
        public void loadCommit(String projectDir, String commitID, String branchID) throws Exception {
            /*
            1. get snapshot based on commitID and branchID
            2. Iterate the files in snapshot list
            3. For each file load its diffs starting from 0th index up to the commit we want to load
            4. The rest of the file can be deleted.
            It's safe to delete because every file has a copy saved on it's original file diff.
            i.e., 0th index
             */
        }
    }
