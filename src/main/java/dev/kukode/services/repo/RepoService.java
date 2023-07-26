    /*
 * Copyright (C) 26/07/23, 7:23 am KUKODE - Kuchuk Boram Debbarma . - All Rights Reserved
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
            //Create initial branch obj
            BranchModel initialBranchModel = new BranchModel("Initial Branch", new Date(), null, null);
            //Create initial branch directory
            dirService.createBranchDirectory(projectDir, initialBranchModel.getUID());
            //Create branchDB obj and add initial branch to it
            var branchDB = new BranchDB();
            if (branchDB.branches == null) {
                branchDB.branches = new ArrayList<>();
            }
            branchDB.branches.add(initialBranchModel);
            //Create branchDB file
            dirService.createBranchDBFile(projectDir, branchDB);

            //Create initial commitModel obj
            CommitModel initialCommitModel = new CommitModel("Initial Commit", "", new Date(), initialBranchModel.getUID(), null, null, new ArrayList<>());
            //Create commitDB and add initial commit
            var commitDb = new CommitDB();
            commitDb.commits.add(initialCommitModel);
            //Create commitDB for the branch
            dirService.createCommitDBFileForBranch(projectDir, initialCommitModel.getBranchID(), commitDb);

            //Create snapshot obj for initial commit
            String snapShotID = initialBranchModel.getUID() + initialCommitModel.getUID();
            List<String> filePaths = getProjectFileSnapshot(projectDir);
            SnapshotModel snapshotModel = new SnapshotModel(snapShotID, filePaths);
            //Create snapshotDB and add snapshot
            SnapshotDB snapshotDB = new SnapshotDB();
            snapshotDB.getSnapshotModels().add(snapshotModel);
            //Create snapshotDB file
            dirService.createSnapshotDBFile(projectDir, snapshotDB);

            //Iterate filePaths from snapshot of initial commit
            for (String filePath : filePaths) {
                //Read the content from the project for each file
                String diff = Files.readString(Path.of(projectDir + filePath));
                //Create an initial Diff model for filePath
                DiffModel initialDiff = new DiffModel(initialBranchModel.getUID() + initialCommitModel.getUID(), diff, initialCommitModel.getUID(), initialBranchModel.getUID());
                //Create DiffDB for the filePath and adds initialDiff to it
                dirService.addFileDiff(projectDir, filePath, initialDiff);
            }

            //Update Initial and Active commits and Branch in Repo
            var repo = dirService.getKuFlexRepoModel(projectDir);
            repo.setInitialCommit(initialCommitModel.getUID());
            repo.setActiveCommit(initialCommitModel.getUID());
            repo.setInitialBranch(initialBranchModel.getUID());
            repo.setActiveBranch(initialBranchModel.getUID());
            dirService.updateKuFlexRepo(projectDir, repo);
        }

        @Override
        public void createNewCommit(String projectDir, String commitName, String comment) throws Exception {
            //Get repo
            var repo = dirService.getKuFlexRepoModel(projectDir);
            //Get current commit
            var currentCommit = dirService.getCommitByID(projectDir, repo.getActiveCommit(), repo.getActiveBranch());
            //Create new commit
            var newCommit = new CommitModel(commitName, comment, new Date(), currentCommit.getBranchID(), currentCommit.getUID(), currentCommit.getBranchID(), new ArrayList<>());
            //Add new commit
            dirService.AddOrUpdateCommit(projectDir, newCommit);

            //Create snapshot
            List<String> filePaths = getProjectFileSnapshot(projectDir);
            var newSnap = new SnapshotModel(newCommit.getBranchID() + newCommit.getUID(), filePaths);
            //Add snapshot
            dirService.addNewSnapshot(projectDir, newSnap);

            //Is this the first commit after initial commit?
            if (currentCommit.getBranchID().equals(repo.getInitialBranch()) && currentCommit.getUID().equals(repo.getInitialCommit())) {
                //Iterate filePath of snapshots
                for (String filePath : filePaths) {
                    //Read project file content for filePath
                    String currentContent = Files.readString(Path.of(projectDir + filePath));
                    //Does filePath have diffDB?
                    if (dirService.doesDiffDBExist(projectDir, filePath)) {
                        var currentDiffModel = dirService.getDiffModel(projectDir, filePath, currentCommit.getBranchID() + currentCommit.getUID());
                        if (currentDiffModel == null) {
                            throw new Exception("Diff model for current commit is null");
                        }
                        String originalFileContent = currentDiffModel.getDiff();
                        String diffString = diffService.generateFileDiff(originalFileContent, currentContent);
                        var newDiffModel = new DiffModel(newCommit.getBranchID() + newCommit.getUID(), diffString, newCommit.getUID(), newCommit.getBranchID());
                        dirService.addFileDiff(projectDir, filePath, newDiffModel);
                    } else {
                        //No previous diff exists so create a new one
                        var newDiffModel = new DiffModel(newCommit.getBranchID() + newCommit.getUID(), currentContent, newCommit.getUID(), newCommit.getBranchID());
                        dirService.addFileDiff(projectDir, filePath, newDiffModel);
                    }


                }

            } else {
                System.out.println("WIP");
                //WIP
            }

            //Update the children list of current commit
            if (currentCommit.getChildrenBranchCommit() == null) {
                currentCommit.setChildrenBranchCommit(new ArrayList<>());
            }
            currentCommit.getChildrenBranchCommit().add(newCommit.getBranchID() + ".." + newCommit.getUID());
            dirService.AddOrUpdateCommit(projectDir, currentCommit);

            //Update active branch and commit
            repo.setActiveBranch(newCommit.getBranchID());
            repo.setActiveCommit(newCommit.getUID());
            dirService.updateKuFlexRepo(projectDir, repo);
        }

        @Override
        public boolean createNewBranch(String projectDir, String branchName, String branchComment, String inheritedBranch, String inheritedCommit) throws Exception {
            return false;
        }

        @Override
        public void loadCommit(String projectDir, String commitID, String branchID) throws Exception {
            /*
            Firstly, we need to load the commit model that we are trying to load.
            Secondly, we need snapshots of the commit we are trying to load
            Once we have the snapshots we know the files we are going to have to get

            Iterate the files (f):
            Find the linked list of the diff by starting from initial commit up to commit we want to load.
            Load the file content and diff one by one until we end up with final content.

            We simply delete the other files in project directory that we do not need.

            The hard part will be figuring out how to effectively traverse the commits.
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
