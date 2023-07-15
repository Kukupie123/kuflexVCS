    /*
 * Copyright (C) 16/07/23, 1:39 am KUKODE - Kuchuk Boram Debbarma . - All Rights Reserved
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
    import java.io.FileWriter;
    import java.nio.file.DirectoryNotEmptyException;
    import java.nio.file.Files;
    import java.util.ArrayList;
    import java.util.Date;
    import java.util.List;
    import java.util.UUID;

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
        public boolean initializeRepo(String projectDir, String projectName, Date creationDate, String creator) throws Exception {
            //TODO: Clean up and move dir functions to dir service
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
            KuflexRepoModel kuflexRepoModel = getKuFlexRepo(projectDir);
            kuflexRepoModel.activeBranch = defaultBranch.getUID();
            kuflexRepoModel.activeCommit = defaultCommit.getUID();
            kuflexRepoModel.initialBranch = defaultBranch.getUID();
            kuflexRepoModel.initialBranch = defaultCommit.getUID();
            updateKuFlexRepo(projectDir, kuflexRepoModel);
            return false;
        }

        @Override
        public boolean createNewCommit(String projectDir, String commitName, String comment) throws Exception {
            var kuflexRepo = getKuFlexRepo(projectDir);
            //Current commit
            CommitModel currentCommitModel = getCommitByID(projectDir, kuflexRepo.activeCommit, kuflexRepo.activeBranch);

            //Create New commit
            CommitModel newCommitModel = new CommitModel(commitName, comment, new Date(), currentCommitModel.getBranchID(), currentCommitModel.getInheritedCommit());
            //Create New commit directory
            createCommitDirectory(projectDir, newCommitModel.getBranchID(), newCommitModel.getUID());
            //Add new commit to commitDB of DB
            addCommitToDB(projectDir, kuflexRepo.activeBranch, newCommitModel);
            String newCommitPath = projectDir + "\\.kuflex\\branches\\" + kuflexRepo.activeCommit + "\\" + newCommitModel.getUID();
            //Create project snapshot for new commit
            //TODO: Change create snapshot function
            createSnapshot(projectDir, newCommitPath);
            //Determine which file has been removed from current branch for moving to vault. To do this we compare the snapshots
            List<String> removedFiles = new ArrayList<>(); //These files do not exist in new commit
            //Now comes the hard part. File content Diff and then saving them in diff folder

            return false;
        }

        private void createCommitDirectory(String projectDir, String branchID, String commitID) throws Exception {
            File file = new File(projectDir + "\\.kuflex\\branches\\" + branchID, commitID);
            if (!file.mkdir()) {
                throw new Exception("Failed to create Commit directory");
            }
        }

        private void addCommitToDB(String projectDir, String branchID, CommitModel commitModel) throws Exception {
            var commitDB = getCommitDB(projectDir, branchID);
            commitDB.commits.add(commitModel);
            updateCommitDB(projectDir, branchID, commitDB);
        }

        private void updateCommitDB(String projectDir, String branchID, CommitDB commitDB) throws Exception {
            File file = new File(projectDir + "\\.kuflex\\branches\\" + branchID + "\\commitsDB.json");
            if (!file.exists() || !file.isFile()) {
                throw new Exception("CommitDB doesn't exist or is not a file");
            }
            try (FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.write(gson.toJson(commitDB));
            }
        }

        private CommitDB getCommitDB(String projectDir, String branchID) throws Exception {
            File file = new File(projectDir + "\\.kuflex\\branches\\" + branchID + "\\commitsDB.json");
            if (!file.exists() || !file.isFile()) {
                throw new Exception("CommitDB doesn't exist or is not a file");
            }
            String data = Files.readString(file.toPath());
            return gson.fromJson(data, CommitDB.class);
        }

        private CommitModel getCommitByID(String projectDir, String commitID, String branchID) throws Exception {
            CommitDB commitDB = getCommitDB(projectDir, branchID);
            for (CommitModel commit : commitDB.commits) {
                if (commit.getUID().equals(commitID)) {
                    return commit;
                }
            }
            return null;
        }


        private KuflexRepoModel getKuFlexRepo(String projectDir) throws Exception {
            File file = new File(projectDir + "\\.kuflex", "kuFlexRepo.json");
            if (!file.isFile()) {
                throw new Exception("Failed to load kuFlexRepo.json");
            }
            return gson.fromJson(Files.readString(file.toPath()), KuflexRepoModel.class);
        }

        private boolean updateKuFlexRepo(String projectDir, KuflexRepoModel kuflexRepoModel) throws Exception {
            File repoFile = new File(projectDir + "\\.kuflex", "kuFlexRepo.json");
            if (!repoFile.exists() || !repoFile.isFile()) {
                throw new Exception("Repo doesn't exist or is not a file");
            }
            try (FileWriter fileWriter = new FileWriter(repoFile)) {
                String data = gson.toJson(kuflexRepoModel);
                fileWriter.write(data);
                return true;
            }
        }


        private boolean doesRepoAlreadyExist(String projectDir) {
            File repoDir = new File(projectDir + "\\.kuflex");
            return repoDir.isDirectory();
        }


        private List<String> getProjectFilesPath(String projectDir, String basePath) {
        /*
        RECURSIVELY access subdirectories
        1. Access the rootDir
        2. Get list of files in rootDir
        3. Iterate the list of files (call it f)
            3.1 If "f" is a file then add its relative path to the list
            3.2 If "f" is a directory then add the return value of getFiles(f) to the list
         */
            List<String> filePaths = new ArrayList<>();

            // Access RootDir
            File file = new File(projectDir);

            // Iterate rootDir's subdirectories
            if (file.isDirectory()) {
                File[] dirFiles = file.listFiles();
                if (dirFiles == null) return filePaths;
                for (File dirFile : dirFiles) {
                    if (dirFile.isFile()) {
                        String relativePath = basePath + File.separator + dirFile.getName();
                        filePaths.add(relativePath);
                    } else {
                        List<String> subDirFiles = getProjectFilesPath(dirFile.getAbsolutePath(), basePath + File.separator + dirFile.getName());
                        filePaths.addAll(subDirFiles);
                    }
                }
            }
            return filePaths;
        }

        private boolean createRepoDir(String dir, KuflexRepoModel kuflexRepo) throws Exception {
            logger.info("Create Repo dir for path : " + dir + " and projectName " + kuflexRepo.projectName);
            //Create .KuFlex repo directory
            dirService.createKuFlexRepoDir(dir);
            //create the kuflex repository file
            dirService.createKuFlexRepoFile(dir, kuflexRepo);
            return true;
        }

        private BranchModel createInitialBranch(String projectDir, String branchName) throws Exception {
            //Create BranchModel
            BranchModel branchModel = new BranchModel(branchName, new Date(), "", "");
            //Create branch directory
            File branchDir = new File(projectDir + "\\.kuflex\\branches\\" + branchModel.getUID());
            if (!branchDir.mkdirs()) {
                throw new Exception("Failed to create " + branchName + " initial branch directories");
            }
            //Create branch DB
            File branchDBFile = new File(projectDir + "\\.kuflex\\branchesDB.json");
            if (!branchDBFile.createNewFile()) {
                throw new Exception("Failed to create branch DB");
            }
            //Write DB content
            try (FileWriter branchDBWriter = new FileWriter(branchDBFile)) {
                BranchDB branchDB = new BranchDB();
                branchDB.branches = new ArrayList<>();
                branchDB.branches.add(branchModel);
                String jsonData = gson.toJson(branchDB);
                branchDBWriter.write(jsonData);
                return branchModel;
            }
        }

        private CommitModel createInitialCommit(String projectDir, String commitName, String commitComment, String branchID) throws Exception {
            //Create commit model
            CommitModel commitModel = new CommitModel(commitName, commitComment, new Date(), branchID, "");
            //Create commit directory
            String commitPath = projectDir + "\\.kuflex\\branches\\" + branchID + "\\" + commitModel.getUID();
            File commitDir = new File(commitPath);
            if (!commitDir.mkdirs()) {
                throw new Exception("Failed to create commit directory");
            }
            //Create CommitDB file
            File commitDBFile = new File(projectDir + "\\.kuflex\\branches\\" + branchID + "\\commitsDB.json");
            if (!commitDBFile.createNewFile()) {
                throw new Exception("Failed to create commit DB File");
            }
            //Write the new commit to commitDBFile file
            try (FileWriter fileWriter = new FileWriter(commitDBFile)) {
                CommitDB commitDB = new CommitDB();
                commitDB.commits = new ArrayList<>();
                commitDB.commits.add(commitModel);
                String jsonDate = gson.toJson(commitDB);
                fileWriter.write(jsonDate);
            }

            //Create snapshot file for the commit
            SnapshotModel snapshotModel = createSnapshot(projectDir, commitPath);
            //Create Initial File copy
            createInitialFileCopy(projectDir, snapshotModel, commitPath);

            return commitModel;
        }

        private SnapshotModel createSnapshot(String projectDir, String commitPath) throws Exception {
            //Create snapshot file in commit path
            File snapshotFile = new File(commitPath, "kuFlexSnap.json");
            if (!snapshotFile.createNewFile()) {
                throw new Exception("Failed to create snapshot");
            }

            //Get file paths
            List<String> filePaths = getProjectFilesPath(projectDir, "");
            List<String> pathToRemove = new ArrayList<>();
            for (String path : filePaths) {
                if (path.contains(".kuflex")) {
                    pathToRemove.add(path);
                }
            }
            filePaths.removeAll(pathToRemove);

            //Write file path to snapshot
            try (FileWriter snapshotWriter = new FileWriter(snapshotFile)) {
                SnapshotModel snapshotModel = new SnapshotModel();
                snapshotModel.files = filePaths;
                String jsonData = gson.toJson(snapshotModel);
                snapshotWriter.write(jsonData);
                return snapshotModel;
            }
        }

        private void createInitialFileCopy(String projectDir, SnapshotModel snapshotModel, String commitPath) throws Exception {
            for (String s : snapshotModel.files) {
                //Get original files
                File file = new File(projectDir + s);

                //Create diffs directory
                File diffDir = new File(commitPath, "diffs");
                if (!(diffDir.exists() && diffDir.isDirectory())) {
                    if (!diffDir.mkdir()) {
                        throw new Exception("Failed to create diff folder although it didn't exist in commit : " + commitPath);
                    }
                }

                //Save diff file
                String fileID = UUID.randomUUID().toString();
                DiffModel diffModel = new DiffModel();
                diffModel.path = s;
                diffModel.diff = Files.readString(file.toPath());

                File diffFile = new File(diffDir, fileID + ".kuflexDiff");
                if (!diffFile.createNewFile()) {
                    throw new Exception("Failed to create new commit file : " + s + ".kuflex");
                }

                try (FileWriter fileWriter = new FileWriter(diffFile)) {
                    String data = gson.toJson(diffModel);
                    fileWriter.write(data);
                }
            }


        }

    }
