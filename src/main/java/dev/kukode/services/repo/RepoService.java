    /*
 * Copyright (C) 29/07/23, 12:56 am KUKODE - Kuchuk Boram Debbarma . - All Rights Reserved
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
    import java.util.*;

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
        public void initializeRepo(String projectName, Date creationDate, String creator) throws Exception {
            // Create .KuFlex Directory
            dirService.createKuFlexRepoDir();
            // Create Diff directory
            dirService.createDiffDirectory();
            // Create KuFlexRepo.json
            dirService.createKuFlexRepoFile(new KuflexRepoModel(projectName, creator, new Date()));
            // Create initial branch obj
            BranchModel initialBranchModel = new BranchModel("Initial Branch", new Date(), null, null);
            // Create initial branch directory
            dirService.createBranchDirectory(initialBranchModel.getUID());
            // Create branchDB obj and add initial branch to it
            var branchDB = new BranchDB();
            if (branchDB.branches == null) {
                branchDB.branches = new ArrayList<>();
            }
            branchDB.branches.add(initialBranchModel);
            // Create branchDB file
            dirService.createBranchDBFile(branchDB);
            // Create initial commitModel obj
            CommitModel initialCommitModel = new CommitModel("Initial Commit", "", new Date(), initialBranchModel.getUID(), null, null, new ArrayList<>());
            // Create commitDB and add initial commit
            var commitDb = new CommitDB();
            commitDb.commits.add(initialCommitModel);
            // Create commitDB for the branch
            dirService.createCommitDBFileForBranch(initialCommitModel.getBranchID(), commitDb);
            // Create snapshot obj for initial commit
            List<String> filePaths = getProjectFileSnapshot();
            SnapshotModel snapshotModel = new SnapshotModel(initialBranchModel.getUID(), initialCommitModel.getUID(), filePaths);
            // Create snapshotDB and add snapshot
            SnapshotDB snapshotDB = new SnapshotDB();
            snapshotDB.getSnapshotModels().add(snapshotModel);
            // Create snapshotDB file
            dirService.createSnapshotDBFile(snapshotDB);
            // Iterate filePaths from snapshot of initial commit
            for (String filePath : filePaths) {
                // Read the content from the project for each file
                String diff = Files.readString(Path.of(ConstantNames.ProjectPath + filePath));
                // Create an initial Diff model for filePath
                DiffModel initialDiff = new DiffModel(diff, initialCommitModel.getUID(), initialBranchModel.getUID());
                // Create DiffDB for the filePath and adds initialDiff to it
                dirService.addFileDiff(filePath, initialDiff);
            }
            // Update Initial and Active commits and Branch in Repo
            var repo = dirService.getKuFlexRepoModel();
            repo.setInitialCommit(initialCommitModel.getUID());
            repo.setActiveCommit(initialCommitModel.getUID());
            repo.setInitialBranch(initialBranchModel.getUID());
            repo.setActiveBranch(initialBranchModel.getUID());
            dirService.updateKuFlexRepo(repo);
        }

        @Override
        public void createNewCommit(String commitName, String comment) throws Exception {
            // Get the repository
            var repo = dirService.getKuFlexRepoModel();
            // Get the current commit
            var currentCommit = dirService.getCommitByID(repo.getActiveCommit(), repo.getActiveBranch());
            // Create a new commit
            var newCommit = new CommitModel(commitName, comment, new Date(), currentCommit.getBranchID(), currentCommit.getUID(), currentCommit.getBranchID(), new ArrayList<>());
            // Add the new commit
            dirService.AddOrUpdateCommit(newCommit);
            // Create a snapshot
            List<String> filePaths = getProjectFileSnapshot();
            var newSnap = new SnapshotModel(newCommit.getBranchID(), newCommit.getUID(), filePaths);
            // Add the snapshot
            dirService.addNewSnapshot(newSnap);
            // Check if this is the first commit after the initial commit
            if (currentCommit.getBranchID().equals(repo.getInitialBranch()) && currentCommit.getUID().equals(repo.getInitialCommit())) {
                // Iterate over the file paths of the snapshots
                for (String filePath : filePaths) {
                    // Read the content of the project file for the current file path
                    String currentContent = Files.readString(Path.of(ConstantNames.ProjectPath + filePath));
                    // Check if the file path has a diffDB
                    if (dirService.doesDiffDBExist(filePath)) {
                        var currentDiffModel = dirService.getDiffModel(filePath, currentCommit.getBranchID(), currentCommit.getUID());
                        if (currentDiffModel == null) {
                            throw new Exception("Diff model for current commit is null");
                        }
                        String originalFileContent = currentDiffModel.getDiff();
                        String diffString = diffService.generateDiffData(originalFileContent, currentContent);
                        var newDiffModel = new DiffModel(diffString, newCommit.getUID(), newCommit.getBranchID());
                        dirService.addFileDiff(filePath, newDiffModel);
                    } else {
                        // No previous diff exists, so create a new one
                        var newDiffModel = new DiffModel(currentContent, newCommit.getUID(), newCommit.getBranchID());
                        dirService.addFileDiff(filePath, newDiffModel);
                    }
                }
            } else {
                /*
                Load the file content upto current diff.
                Generate diff based on current content of project for the file
                 */

                for (String filePath : newSnap.getFiles()) {
                    String fileContentForCurrentCommit = getFileContentForCommit(filePath, currentCommit);
                    String currentContent = Files.readString(Path.of(ConstantNames.ProjectPath + filePath));
                    String diffString = diffService.generateDiffData(fileContentForCurrentCommit, currentContent);
                    var newDiffModel = new DiffModel(diffString, newCommit.getUID(), newCommit.getBranchID());
                    dirService.addFileDiff(filePath, newDiffModel);
                }
            }
            // Update the children list of the current commit
            if (currentCommit.getChildrenBranchCommit() == null) {
                currentCommit.setChildrenBranchCommit(new ArrayList<>());
            }
            currentCommit.getChildrenBranchCommit().add(newCommit.getBranchID() + ".." + newCommit.getUID());
            dirService.AddOrUpdateCommit(currentCommit);
            // Update the active branch and commit
            repo.setActiveBranch(newCommit.getBranchID());
            repo.setActiveCommit(newCommit.getUID());
            dirService.updateKuFlexRepo(repo);

        }

        @Override
        public boolean createNewBranch(String branchName, String branchComment, String inheritedBranch, String inheritedCommit) throws Exception {
            return false;
        }

        @Override
        public void loadCommit(String commitIDtoLoad, String branchIDtoLoad) throws Exception {
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

            KuflexRepoModel repoModel = dirService.getKuFlexRepoModel();
            SnapshotModel snap = dirService.getSnapshot(branchIDtoLoad, commitIDtoLoad);

            //Load linked list from current commit to initial commit
            CommitModel commitToLoad = dirService.getCommitByID(commitIDtoLoad, branchIDtoLoad);
            var commitChain = getCommitChainToInitial(commitToLoad, null);
            Collections.reverse(commitChain); //So that we start from initial commit to the commit, we want to load.

            for (String filePath : snap.getFiles()) {
                var content = getFileContentForCommit(filePath, commitToLoad);
                dirService.writeContentToProjectFile(content, filePath);

            }


            //Iterate over project files and remove those who are not part of snapshot
            List<String> projectFilePaths = getProjectFileSnapshot();
            for (String path : projectFilePaths) {
                if (!snap.getFiles().contains(path)) {
                    dirService.removeProjectFile(path);
                }
            }
            //Update active branch and commit
            repoModel.setActiveCommit(commitIDtoLoad);
            repoModel.setActiveBranch(branchIDtoLoad);
            dirService.updateKuFlexRepo(repoModel);
        }

        private List<String> getProjectFileSnapshot() {
            List<String> filePaths = dirService.getProjectFilesPath(ConstantNames.ProjectPath);
            List<String> unwantedPaths = new ArrayList<>();
            filePaths.forEach(s -> {
                if (s.contains("\\" + ConstantNames.KUFLEX)) {
                    unwantedPaths.add(s);
                }
            });
            filePaths.removeAll(unwantedPaths);
            return filePaths;
        }

        private LinkedList<CommitModel> getCommitChainToInitial(CommitModel currentCommitModel, LinkedList<CommitModel> linkedList) throws Exception {
            if (linkedList == null) {
                linkedList = new LinkedList<>();
            }
            if (!linkedList.contains(currentCommitModel)) {
                linkedList.add(currentCommitModel);
            }
            var repo = dirService.getKuFlexRepoModel();
            if (repo.getInitialCommit().equals(currentCommitModel.getUID()) && repo.getInitialBranch().equals(currentCommitModel.getBranchID())) {
                return linkedList;
            }
            CommitModel prevCommit = dirService.getCommitByID(currentCommitModel.getInheritedCommit(), currentCommitModel.getInheritedBranch());
            if (!linkedList.contains(prevCommit)) {
                linkedList.add(prevCommit);
            }
            linkedList.addAll(getCommitChainToInitial(prevCommit, linkedList));
            return linkedList;
        }

        private String getFileContentForCommit(String filePath, CommitModel commitToLoad) throws Exception {
            var commitChain = getCommitChainToInitial(commitToLoad, null);
            Collections.reverse(commitChain);

            String currentContent = null;

            for (int i = 0; i < commitChain.size(); i++) {
                CommitModel currentCommit = commitChain.get(i);
                DiffModel diffModel = dirService.getDiffModel(filePath, currentCommit.getBranchID(), currentCommit.getUID());

                if (diffModel.isInitialDiff()) {
                    // Use the initial content stored during repository initialization
                    currentContent = diffModel.getDiff();
                } else {
                    // Apply the diff to the current content
                    String diffText = diffModel.getDiff();
                    assert currentContent != null;
                    currentContent = diffService.getContentFromOriginalNDiff(currentContent, diffText);
                }
            }

            return currentContent;
        }

    }
