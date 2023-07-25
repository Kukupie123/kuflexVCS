    /*
 * Copyright (C) 26/07/23, 12:51 am KUKODE - Kuchuk Boram Debbarma . - All Rights Reserved
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
    import dev.kukode.models.diffs.DiffModel;
    import dev.kukode.models.snapshots.SnapshotDB;
    import dev.kukode.models.snapshots.SnapshotModel;
    import dev.kukode.services.diff.DiffService;
    import dev.kukode.services.dirNFile.DirNFileService;
    import dev.kukode.util.ConstantNames;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.stereotype.Service;

    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.util.ArrayList;
    import java.util.Date;
    import java.util.List;

    @Service
    public class RepoService implements IRepoService {
        final Gson gson;
        final DirNFileService dirService;

        final DiffService diffService;
        Logger logger = LoggerFactory.getLogger(this.getClass());

        public RepoService(Gson gson, DirNFileService dirService, DiffService diffService) {
            this.gson = gson;
            this.dirService = dirService;
            this.diffService = diffService;
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
            List<String> filePaths = getProjectFileSnapshot(projectDir);
            SnapshotModel snapshotModel = new SnapshotModel(snapShotID, filePaths);
            SnapshotDB snapshotDB = new SnapshotDB();
            snapshotDB.getSnapshotModels().add(snapshotModel);
            dirService.createSnapshotDBFile(projectDir, snapshotDB);

            //14. Create DiffDB for each file
            for (String filePath : filePaths) {
                //Read the content from the project for each file
                String diff = Files.readString(Path.of(projectDir + filePath));
                //Create a Diff model
                DiffModel initialDiff = new DiffModel("0", diff, initialCommitModel.getUID(), initialBranchModel.getUID(), null, null, new ArrayList<>());
                dirService.createOrUpdateFileDiff(projectDir, filePath, initialDiff);
            }

            //15. Update Initial and Active commits and Branch in Repo
            var repo = dirService.getKuFlexRepoModel(projectDir);
            repo.setInitialCommit(initialCommitModel.getUID());
            repo.setActiveCommit(initialCommitModel.getUID());
            repo.setInitialBranch(initialBranchModel.getUID());
            repo.setActiveBranch(initialBranchModel.getUID());
            dirService.updateKuFlexRepo(projectDir, repo);
        }

        @Override
        public boolean createNewCommit(String projectDir, String commitName, String comment) throws Exception {
            var repo = dirService.getKuFlexRepoModel(projectDir);
            var currentCommit = dirService.getCommitByID(projectDir, repo.getActiveCommit(), repo.getActiveBranch());
            var newCommit = new CommitModel(commitName, comment, new Date(), currentCommit.getBranchID(), currentCommit.getUID(), currentCommit.getBranchID());

            dirService.addNewCommit(projectDir, newCommit);

            //Is this the first commit after initial commit?
            if (newCommit.getInheritedBranch().equals(repo.getInitialBranch()) && newCommit.getInheritedCommit().equals(repo.getInitialCommit())) {
                System.out.println("second commit");
                //Create snapshot
                List<String> filePaths = getProjectFileSnapshot(projectDir);
                var newSnap = new SnapshotModel(newCommit.getBranchID() + newCommit.getUID(), filePaths);
                dirService.addNewSnapshot(projectDir, newSnap);

                //Create file diff for each snapshot file or update
                for (String s : filePaths) {
                    String currentContent = Files.readString(Path.of(projectDir + s));
                    if (dirService.doesDiffDBExist(projectDir, s)) {
                        var currentDiffDBModel = dirService.getDiffDBForFile(projectDir, s);
                        DiffModel currentDiffModel = null;
                        for (DiffModel dm : currentDiffDBModel.getDiffModels()) {
                            if (dm.getCommitID().equals(currentCommit.getUID()) && dm.getBranchID().equals(currentCommit.getBranchID())) {
                                currentDiffModel = dm;
                                break;
                            }
                        }
                        assert currentDiffModel != null;
                        String originalFileContent = currentDiffModel.getDiff();
                        String diffString = diffService.generateFileDiff(originalFileContent, currentContent);

                        var newDiffModel = new DiffModel(newCommit.getBranchID() + newCommit.getUID(), diffString, newCommit.getUID(), newCommit.getBranchID(), currentCommit.getUID(), currentCommit.getBranchID(), new ArrayList<>());

                        dirService.createOrUpdateFileDiff(projectDir, s, newDiffModel);

                        //Update the Children list of current diff and add new commit
                        currentDiffModel.getChildrenBranchCommit().add(newCommit.getBranchID() + newCommit.getUID());

                        dirService.createOrUpdateFileDiff(projectDir, s, currentDiffModel);

                    }
                }

                //Update active branch and commit
                repo.setActiveBranch(newCommit.getBranchID());
                repo.setActiveCommit(newCommit.getUID());
                dirService.updateKuFlexRepo(projectDir, repo);
            } else {
                System.out.println("WIP");
                //WIP
            }

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

        private List<String> getProjectFileSnapshot(String projectDir) {
            List<String> filePaths = dirService.getProjectFilesPath(projectDir);
            List<String> unwantedPaths = new ArrayList<>();
            filePaths.forEach(s -> {
                if (s.contains("\\" + ConstantNames.KUFLEX)) {
                    unwantedPaths.add(s);
                }
            });
            filePaths.removeAll(unwantedPaths);
            return filePaths;
        }
    }
