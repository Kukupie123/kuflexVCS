    /*
 * Copyright (C) 25/07/23, 10:28 pm KUKODE - Kuchuk Boram Debbarma . - All Rights Reserved
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
    import dev.kukode.models.snapshots.SnapshotDB;
    import dev.kukode.models.snapshots.SnapshotModel;
    import dev.kukode.services.dirNFile.DirNFileService;
    import dev.kukode.util.ConstantNames;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.stereotype.Service;

    import java.io.File;
    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.util.ArrayList;
    import java.util.Base64;
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


        public static String encode(String input) {
            byte[] encodedBytes = Base64.getEncoder().encode(input.getBytes());
            return new String(encodedBytes);
        }

        public static String decode(String encodedInput) {
            byte[] decodedBytes = Base64.getDecoder().decode(encodedInput);
            return new String(decodedBytes);
        }


        private boolean doesRepoAlreadyExist(String projectDir) {
            File repoDir = new File(projectDir + "\\.kuflex");
            return repoDir.isDirectory();
        }


        @Override
        public void initializeRepo(String projectDir, String projectName, Date creationDate, String creator) throws Exception {

            //Create .KuFlex Directory
            dirService.createKuFlexRepoDir(projectDir);
            //Create Diff directory
            dirService.createDiffDirectory(projectDir);

            //Create KuFlexRepo.json
            dirService.createKuFlexRepoFile(projectDir, new KuflexRepoModel(projectName, creator, new Date()));
            //Create BranchesDB file, Branch Directory and add initial Branch to branchesDB
            BranchModel initialBranchModel = new BranchModel("Initial Branch", new Date(), null, null);
            dirService.createBranchDirectory(projectDir, initialBranchModel.getUID());
            var branchDB = new BranchDB();
            if (branchDB.branches == null) {
                branchDB.branches = new ArrayList<>();
            }
            branchDB.branches.add(initialBranchModel);
            dirService.createBranchDBFile(projectDir, branchDB);

            //Create CommitDB file for initial branch, add initial commit to the CommitDB
            var commitDb = new CommitDB();
            CommitModel initialCommitModel = new CommitModel("Initial Commit", "", new Date(), initialBranchModel.getUID(), null, null);
            commitDb.commits.add(initialCommitModel);
            dirService.createCommitDBFileForBranch(projectDir, initialCommitModel.getBranchID(), commitDb);

            //Create snapshotDB file and add snapshot for the initial commit
            String snapShotID = initialBranchModel.getUID() + initialCommitModel.getUID();
            List<String> filePaths = dirService.getProjectFilesPath(projectDir);
            List<String> unwantedPaths = new ArrayList<>();
            filePaths.forEach(s -> {
                if (s.contains("\\" + ConstantNames.KUFLEX)) {
                    unwantedPaths.add(s);
                }
            });
            filePaths.removeAll(unwantedPaths);
            SnapshotModel snapshotModel = new SnapshotModel(snapShotID, filePaths);
            SnapshotDB snapshotDB = new SnapshotDB();
            snapshotDB.getSnapshotModels().add(snapshotModel);
            dirService.createSnapshotDBFile(projectDir, snapshotDB);

            //14. Create DiffDB for each file
            for (String filePath : filePaths) {
                //Create encoded name from path
                String filePathEncoded = encode(filePath);
                //Read the content from the project for each file
                String diff = Files.readString(Path.of(projectDir + filePath));
                //Create a Diff model
                DiffModel initialDiff = new DiffModel("0", diff, initialCommitModel.getUID(), initialBranchModel.getUID(), null, null, new ArrayList<>());
                //Create Diff DB and add initial Diff
                DiffDB diffDB = new DiffDB(filePathEncoded);
                diffDB.getDiffModels().add(initialDiff);
                //Create Diff DB file
                dirService.createInitialDiffDB(projectDir, diffDB);
            }

            //15. Update Initial and Active commits and Branch in Repo
            var repo = dirService.getKuFlexRepoModel(projectDir);
            repo.initialCommit = initialCommitModel.getUID();
            repo.activeCommit = initialCommitModel.getUID();
            repo.initialCommit = initialBranchModel.getUID();
            repo.initialBranch = initialBranchModel.getUID();
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
            It's safe to delete because every file has a copy saved on it's original file diffs.
            i.e., 0th index
             */
        }
    }
