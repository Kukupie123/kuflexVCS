    /*
 * Copyright (C) 18/07/23, 10:46 am KUKODE - Kuchuk Boram Debbarma . - All Rights Reserved
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
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.stereotype.Service;

    import java.io.File;
    import java.nio.file.DirectoryNotEmptyException;
    import java.nio.file.Files;
    import java.util.ArrayList;
    import java.util.Date;
    import java.util.List;

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
            //Check if KuFlex repo already exist
            if (doesRepoAlreadyExist(projectDir)) {
                throw new DirectoryNotEmptyException("KuFlex Repository already exists");
            }

            //Create kuflexrepo model
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
            kuflexRepoModel.initialBranch = defaultCommit.getUID();
            dirService.updateKuFlexRepo(projectDir, kuflexRepoModel);
        }

        @Override
        public boolean createNewCommit(String projectDir, String commitName, String comment) throws Exception {
            var kuflexRepo = dirService.getKuFlexRepoModel(projectDir);
            //Get Current commit
            CommitModel currentCommitModel = dirService.getCommitByID(projectDir, kuflexRepo.activeCommit, kuflexRepo.activeBranch);
            //Create New commit
            CommitModel newCommitModel = new CommitModel(commitName, comment, new Date(), currentCommitModel.getBranchID(), currentCommitModel.getInheritedCommit());
            //Create New commit directory
            dirService.createCommitDir(projectDir, newCommitModel.getBranchID(), newCommitModel.getUID());
            //Add new commit to commitDB of DB
            CommitDB commitDB = dirService.getCommitDbModelForBranch(projectDir, newCommitModel.getBranchID());
            commitDB.commits.add(newCommitModel);
            dirService.updateCommitDbForBranch(projectDir, newCommitModel.getBranchID(), commitDB);
            //Create project snapshot for new commit
            createSnapshot(projectDir, newCommitModel);
            /*
            We can't directly go for file diff yet because
            What if the new snapshot has files that are no longer there?
            What if the new snapshot has files that are new and doesn't exist in the previous snapshot?
             */
            //Determine which file has been removed from current branch for moving to vault. To do this we compare the snapshots
            //TODO: The comment above
            List<String> removedFiles = new ArrayList<>(); //These files do not exist in new commit
            List<String> addedFiles = new ArrayList<>(); //These files do not exist in new commit
            //Now comes the hard part. File content Diff and then saving them in diff folder
            return false;
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
            CommitModel commitModel = new CommitModel(commitName, commitComment, new Date(), branchID, "");
            //Create commit directory
            dirService.createCommitDir(projectDir, branchID, commitModel.getUID());
            String commitPath = projectDir + "\\.kuflex\\branches\\" + branchID + "\\" + commitModel.getUID(); //remove in future
            //Create CommitDB file
            dirService.createCommitDBFileForBranch(projectDir, branchID);
            //Write the new commit to commitDBFile file
            CommitDB commitDB = new CommitDB();
            commitDB.commits = new ArrayList<>();
            commitDB.commits.add(commitModel);
            dirService.updateCommitDbForBranch(projectDir, branchID, commitDB);

            //Create snapshot file for the commit
            SnapshotModel snapshotModel = createSnapshot(projectDir, commitModel);
            //Create Initial File copy
            createInitialFileCopy(projectDir, snapshotModel, commitModel);

            return commitModel;
        }

        private SnapshotModel createSnapshot(String projectDir, CommitModel commitModel) throws Exception {
            //Create snapshot file in commit path
            dirService.createCommitSnapshot(projectDir, commitModel.getUID(), commitModel.getBranchID());

            //Get file paths
            List<String> filePaths = dirService.getProjectFilesPath(projectDir);
            //Remove file paths that are part of kuFlex
            List<String> pathToRemove = new ArrayList<>();
            for (String path : filePaths) {
                if (path.contains(".kuflex")) {
                    pathToRemove.add(path);
                }
            }
            filePaths.removeAll(pathToRemove);

            //Create SnapShotModel and set its values
            SnapshotModel snapshotModel = new SnapshotModel();
            snapshotModel.files = filePaths;

            //Write file path to snapshot
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
