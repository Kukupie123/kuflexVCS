    /*
 * Copyright (C) 25/07/23, 9:57 pm KUKODE - Kuchuk Boram Debbarma . - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

    package dev.kukode.services.repo;

    import com.google.gson.Gson;
    import dev.kukode.models.KuflexRepoModel;
    import dev.kukode.models.branches.BranchModel;
    import dev.kukode.models.commits.CommitModel;
    import dev.kukode.models.diffs.DiffDB;
    import dev.kukode.models.diffs.DiffModel;
    import dev.kukode.models.snapshots.SnapshotModel;
    import dev.kukode.services.dirNFile.DirNFileService;
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

            //1. Create .KuFlex Directory
            dirService.createKuFlexRepoDir(projectDir);

            //2. Create KuFlexRepo.json
            dirService.createKuFlexRepoFile(projectDir, new KuflexRepoModel(projectName, creator, new Date()));

            //3. Create BranchDB
            dirService.createBranchDBFile(projectDir);

            //4. Create SnapshotDB
            dirService.createSnapshotDBFile(projectDir);

            //5. Create an initial Branch Model object
            BranchModel initialBranchModel = new BranchModel("Initial Branch", new Date(), null, null);

            //6. Create Initial Branch Directory
            dirService.createBranchDirectory(projectDir, initialBranchModel.getUID());

            //7. Add the created branch to BranchDB
            var branchDB = dirService.getBranchDbModel(projectDir);
            branchDB.branches.add(initialBranchModel);
            dirService.updateBranchDbFile(projectDir, branchDB);

            //8. Create CommitDB file for the branch
            dirService.createCommitDBFileForBranch(projectDir, initialBranchModel.getUID());

            //9. Create an Initial Commit Model object
            CommitModel initialCommitModel = new CommitModel("Initial Commit", "", new Date(), initialBranchModel.getUID(), null, null);

            //10. Create a snapshot Model object
            String snapShotID = initialBranchModel.getUID() + initialCommitModel.getUID();
            List<String> filePaths = dirService.getProjectFilesPath(projectDir);
            SnapshotModel snapshotModel = new SnapshotModel(snapShotID, filePaths);

            //11. Add initial Commit Model to CommitDB of the initial Branch
            var commitDb = dirService.getCommitDbModelForBranch(projectDir, initialBranchModel.getUID());
            commitDb.commits.add(initialCommitModel);
            dirService.updateCommitDbForBranch(projectDir, initialCommitModel.getBranchID(), commitDb);

            //12. Add the snapshot to snapshotDB
            dirService.addSnapshotToSnapshotDB(projectDir, snapshotModel);

            //13. Create Diff directory
            dirService.createDiffDirectory(projectDir);

            //14. Create DiffDB for each file
            for (String filePath : filePaths) {
                //Create encoded name from path
                String filePathEncoded = encode(filePath);
                //Read the content from the project for each file
                String diff = Files.readString(Path.of(projectDir + filePath));
                //Create a Diff model
                DiffModel initialDiff = new DiffModel(0, diff, initialCommitModel.getUID(), initialBranchModel.getUID(), null, null, new ArrayList<>());
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
            It's safe to delete because every file has a copy saved on it's original file diffs.
            i.e., 0th index
             */
        }
    }
