    /*
 * Copyright (C) 24/07/23, 11:49 am KUKODE - Kuchuk Boram Debbarma . - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

    package dev.kukode.services.repo;

    import com.google.gson.Gson;
    import dev.kukode.models.DiffModel;
    import dev.kukode.models.KuflexRepoModel;
    import dev.kukode.models.SnapshotModel;
    import dev.kukode.models.branches.BranchDB;
    import dev.kukode.models.branches.BranchModel;
    import dev.kukode.models.commits.CommitDB;
    import dev.kukode.models.commits.CommitModel;
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
    import java.util.*;
    /*
    TODO: Redesign the whole File diff storing algorithm
    Here is my new design idea
    Lets say our project has a file "file.txt"

    File diff saves storage a lot BUT will take a lot of computation power as we will have to chain commits to reach a certain commit
    Project snapshot/ State saving is nice, we are essentially saving a copy of the project for each commit and we can then load each commit super fast without any calculation
    BUT it takes a lot of storage

    So we will be combining them, once certain amount of file diff chain has been reached we can take a project snapshot

    For now we will be using file-diff approach and perfect it.
    Once perfected we will work on project snapshot
    Then combine them both ultimately
     */

    @Service
    public class RepoService implements IRepoService {
        final Gson gson;
        final DirNFileService dirService;
        Logger logger = LoggerFactory.getLogger(this.getClass());

        public RepoService(Gson gson, DirNFileService dirService) {
            this.gson = gson;
            this.dirService = dirService;
        }


        @Override
        public void initializeRepo(String projectDir, String projectName, Date creationDate, String creator) throws Exception {
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

        @Override
        public boolean createNewCommit(String projectDir, String commitName, String comment) throws Exception {
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
            SnapshotModel newSnap = setupSnapshot(projectDir, newCommitModel);
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
                if (currentDiffModel == null) throw new Exception("Failed to find diff model for file " + f.getPath());
                //Load content from currentDiffModel
                String diffContent = currentDiffModel.diff;
                System.out.println(diffContent);
                //Tokenize the content
                List<String> diffContentTokens = Arrays.asList(diffContent.split("\\n"));
                //Load content from file f
                String projectFileContent = Files.readString(f.toPath());
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
                currentDiffModel.diff = diffFileContent.toString();
                dirService.createCommitDiffFile(projectDir, newCommitModel.getUID(), newCommitModel.getBranchID(), currentDiffModel);
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

        @Override
        public boolean createNewBranch(String projectDir, String branchName, String branchComment, String inheritedBranch, String inheritedCommit) throws Exception {
            /*
            The first commit of the branch will NEED to have it's branchID as the interhitedBranch ID.
            This is needed to load commits which go through more than one branch.
            When we notice that commits branchID and the branchID of the branch Don't match we know
            that it is the FIRST commit of the branch
            and we can now refer to branch's inherited Commit and inherited BranchID to track.
             */
            return false;
        }

        @Override
        public void loadCommit(String projectDir, String commitID, String branchID) throws Exception {
            /*
            1. Make a Linked-List that is going to link initialBranch to the commit we want to load
            2. We can do this by traversing down from the commit we need to load to the initial commit
            3. Load file diffs sequentially from initial to final commit
            4. On the way we will come across files that are present in old commit but not in new commit, we move those in vault
            5. On the way we will come across files that are not present in old commit but present in new commit, we just read their diffs content and load the file
             */

            // Traverse from currentCommit to initialCommit and store them as a linked list
            CommitModel currentCommit = dirService.getCommitByID(projectDir, commitID, branchID);
            LinkedList<CommitModel> commitChain = getCommitChain(projectDir, currentCommit, null);

            //TODO
            // Iterate the chain from initial all the way to currentCommit,
            //Load file diffs & check file deletion and addition and use vault directory accordingly

        }

        private LinkedList<CommitModel> getCommitChain(String projectDir, CommitModel currentCommit, LinkedList<CommitModel> currentList) throws Exception {
            //Null during first function call, initialize a list and add currentCommit to the list
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
            //Create commit model
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
            SnapshotModel snapshotModel = setupSnapshot(projectDir, commitModel);
            //Create Initial File copy
            createInitialFileCopy(projectDir, snapshotModel, commitModel);

            return commitModel;
        }

        private SnapshotModel setupSnapshot(String projectDir, CommitModel commitModel) throws Exception {
            //Create snapshot file in a commit path
            dirService.createCommitSnapshotFile(projectDir, commitModel.getUID(), commitModel.getBranchID());

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

            //Write a file path to snapshot
            dirService.updateCommitSnapshot(projectDir, commitModel.getUID(), commitModel.getBranchID(), snapshotModel);
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

    }
