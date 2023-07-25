/*
 * Copyright (C) 25/07/23, 10:28 pm KUKODE - Kuchuk Boram Debbarma . - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package dev.kukode.services.dirNFile;

import com.google.gson.Gson;
import dev.kukode.models.KuflexRepoModel;
import dev.kukode.models.branches.BranchDB;
import dev.kukode.models.commits.CommitDB;
import dev.kukode.models.commits.CommitModel;
import dev.kukode.models.diffs.DiffDB;
import dev.kukode.models.diffs.DiffModel;
import dev.kukode.models.snapshots.SnapshotDB;
import dev.kukode.models.snapshots.SnapshotModel;
import dev.kukode.util.ConstantNames;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class DirNFileService {
    final Gson gson;

    public DirNFileService(Gson gson) {
        this.gson = gson;
    }

    //GENERAL*************

    /**
     * Returns a list of file path relative to the directory of the project
     */
    public List<String> getProjectFilesPath(String projectDir) {
        String basePath = "";
        return getProjectFilesPath(projectDir, basePath);
    }

    private List<String> getProjectFilesPath(String projectDir, String basePath) {
        List<String> filePaths = new ArrayList<>();
        File file = new File(projectDir);

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

    /**
     * Returns a list of file that is in snapshotModel.Files
     *
     * @param projectDir    Root directory of the project
     * @param snapshotModel The snapshot to load the files from
     */
    public List<File> getProjectFileBasedOnSnapshot(String projectDir, SnapshotModel snapshotModel) {
        List<File> files = new ArrayList<>();
        for (String s : snapshotModel.files) {
            File f = new File(projectDir + s);
            if (f.isFile()) {
                files.add(f);
            }
        }
        return files;
    }

    //REPOSITORY***********************

    /**
     * Create a new repository directory at the root of the project
     *
     * @param projectDir Root directory of the project
     */
    public void createKuFlexRepoDir(String projectDir) throws Exception {
        File rootDir = new File(projectDir);
        if (!rootDir.isDirectory()) {
            throw new Exception(projectDir + " is not a directory. Failed to createKuFlexRepoDir");
        }
        File kuFlexDir = new File(rootDir, ConstantNames.KUFLEX);
        if (!kuFlexDir.mkdir()) {
            throw new Exception("Failed to create " + ConstantNames.KUFLEX + " repository folder");
        }
    }

    /**
     * Create the repository file inside the repository directory
     *
     * @param kuflexRepoModel KuFlexRepo model to write to the file
     */
    public void createKuFlexRepoFile(String projectDir, KuflexRepoModel kuflexRepoModel) throws Exception {
        File kuFlexRepoFile = new File(projectDir + "\\" + ConstantNames.KUFLEX, ConstantNames.KUFLEXREPOFILE);
        if (!kuFlexRepoFile.createNewFile()) {
            throw new Exception("Failed to create " + ConstantNames.KUFLEXREPOFILE);
        }
        try (FileWriter kuFlexRepoWriter = new FileWriter(kuFlexRepoFile)) {
            String data = gson.toJson(kuflexRepoModel);
            kuFlexRepoWriter.write(data);
        }
    }

    /**
     * Get KuFlexRepo file as a File object
     */
    public File getKuFlexRepoFile(String projectDir) throws Exception {
        File file = new File(projectDir + "\\" + ConstantNames.KUFLEX, ConstantNames.KUFLEXREPOFILE);
        if (!file.isFile()) {
            throw new Exception("Failed to load " + ConstantNames.KUFLEXREPOFILE);
        }
        return file;
    }

    /**
     * Get KuFlexRepo file as KuFlexRepoModel
     */
    public KuflexRepoModel getKuFlexRepoModel(String projectDir) throws Exception {
        File file = getKuFlexRepoFile(projectDir);
        return gson.fromJson(Files.readString(file.toPath()), KuflexRepoModel.class);
    }

    /**
     * Update KuFlexRepo with new KuFlexRepo object
     *
     * @param kuflexRepoModel Updated KuFlexRepo object
     */
    public boolean updateKuFlexRepo(String projectDir, KuflexRepoModel kuflexRepoModel) throws Exception {
        File repoFile = new File(projectDir + "\\" + ConstantNames.KUFLEX, ConstantNames.KUFLEXREPOFILE);
        if (!repoFile.exists() || !repoFile.isFile()) {
            throw new Exception("Repo doesn't exist or is not a file");
        }
        try (FileWriter fileWriter = new FileWriter(repoFile)) {
            String data = gson.toJson(kuflexRepoModel);
            fileWriter.write(data);
            return true;
        }
    }

    //BRANCH*****************

    /**
     * Create new Branch directory inside repository directory
     */
    public void createBranchDirectory(String projectDir, String branchID) throws Exception {
        File branchDir = new File(projectDir + "\\" + ConstantNames.KUFLEX + "\\branches\\" + branchID);
        if (!branchDir.mkdirs()) {
            throw new Exception("Failed to create branch directory for branchID " + branchID);
        }
    }

    /**
     * Create BranchDB file inside Repository Directory that is going to hold all branches.
     */
    public void createBranchDBFile(String projectDir, BranchDB initialBranch) throws Exception {
        File branchDBFile = new File(projectDir + "\\" + ConstantNames.KUFLEX + "\\" + ConstantNames.BranchesDBFILE);
        if (branchDBFile.exists()) {
            throw new Exception("BranchDBFile already exists");
        }
        if (!branchDBFile.createNewFile()) {
            throw new Exception("Failed to create branchDBFile");
        }

        try (FileWriter fileWriter = new FileWriter(branchDBFile)) {
            fileWriter.write(gson.toJson(initialBranch));
        }
    }

    /**
     * Get branchDB database as a File object
     */
    public File getBranchDbFile(String projectDir) throws Exception {
        File file = new File(projectDir + "\\" + ConstantNames.KUFLEX + "\\" + ConstantNames.BranchesDBFILE);
        if (!file.isFile()) {
            throw new Exception("Failed to load " + ConstantNames.BranchesDBFILE);
        }
        return file;
    }

    /**
     * Get branchDB database as a BranchDBModel
     */
    public BranchDB getBranchDbModel(String projectDir) throws Exception {
        File file = getBranchDbFile(projectDir);
        return gson.fromJson(Files.readString(file.toPath()), BranchDB.class);
    }

    /**
     * Update BranchDbFile
     *
     * @param branchDB Updated branchDB
     */
    public void updateBranchDbFile(String projectDir, BranchDB branchDB) throws Exception {
        File branchDbFile = getBranchDbFile(projectDir);
        String data = gson.toJson(branchDB);
        try (FileWriter fileWriter = new FileWriter(branchDbFile)) {
            fileWriter.write(data);
        }
    }

    //COMMIT**************

    /**
     * Create Commit Directory
     *
     * @param branchID BranchID where we are going to create the CommitDir
     * @param commitID The CommitID of the commit. The commitDir will be named after the commitID
     */
    public void createCommitDir(String projectDir, String branchID, String commitID) throws Exception {
        File commitDir = new File(projectDir + "\\" + ConstantNames.KUFLEX + "\\branches\\" + branchID + "\\" + commitID);
        if (!commitDir.mkdirs()) {
            throw new Exception("Failed to create Commit Directory");
        }
    }

    /**
     * Create CommitDB file for the given bran
     *
     * @param branchID BranchID where we are going to create commitDB file
     */
    public void createCommitDBFileForBranch(String projectDir, String branchID, CommitDB initialCOmmitDB) throws Exception {
        String filePath = projectDir + "\\" + ConstantNames.KUFLEX + "\\branches\\" + branchID + "\\" + ConstantNames.CommitsDBFile;
        File commitDbFile = new File(filePath);
        if (!commitDbFile.createNewFile()) {
            throw new Exception("Failed to create commit DB File");
        }

        try (FileWriter fileWriter = new FileWriter(filePath)) {
            fileWriter.write(gson.toJson(initialCOmmitDB));
        }
    }

    /**
     * Get CommitModel based on IDs
     *
     * @param commitID ID of the commit
     * @param branchID ID of the branch to which the commit belongs to
     */
    public CommitModel getCommitByID(String projectDir, String commitID, String branchID) throws Exception {
        CommitDB commitDB = getCommitDbModelForBranch(projectDir, branchID);
        for (CommitModel commit : commitDB.commits) {
            if (commit.getUID().equals(commitID)) {
                return commit;
            }
        }
        return null;
    }

    /**
     * Get CommitDB file for a branch
     *
     * @param branchID ID of the branch
     */
    public File getCommitDbFileForBranch(String projectDir, String branchID) throws Exception {
        File file = new File(projectDir + "\\" + ConstantNames.KUFLEX + "\\branches\\" + branchID + "\\" + ConstantNames.CommitsDBFile);
        if (!file.isFile()) {
            throw new Exception("Failed to load " + ConstantNames.KUFLEXREPOFILE);
        }
        return file;
    }

    /**
     * Get CommitDBModel for a branch
     *
     * @param branchID ID of the branch
     */
    public CommitDB getCommitDbModelForBranch(String projectDir, String branchID) throws Exception {
        File file = getCommitDbFileForBranch(projectDir, branchID);
        return gson.fromJson(Files.readString(file.toPath()), CommitDB.class);
    }

    /**
     * Update CommitDB for a branch
     *
     * @param branchID ID of the branch
     * @param commitDB Updated CommitDB
     */
    public void updateCommitDbForBranch(String projectDir, String branchID, CommitDB commitDB) throws Exception {
        File commitDbFile = getCommitDbFileForBranch(projectDir, branchID);
        String data = gson.toJson(commitDB);
        try (FileWriter fileWriter = new FileWriter(commitDbFile)) {
            fileWriter.write(data);
        }
    }

    //SNAPSHOT*************

    /**
     * Create a new snapshot file for a commit
     *
     * @param commitID ID of the commit
     * @param branchID ID of the branch to which the commit belongs to
     */
    public void createCommitSnapshotFile(String projectDir, String commitID, String branchID) throws Exception {
        var snapFile = getCommitSnapshotFile(projectDir, commitID, branchID);
        if (!snapFile.createNewFile()) {
            throw new Exception("Failed to create snapshot for commitID : " + commitID + ", branchID : " + branchID);
        }
    }

    /**
     * Get CommitSnapshot as a file of a Commit
     *
     * @param commitID ID of the commit
     * @param branchID ID of the branch that the commit belongs to
     */
    public File getCommitSnapshotFile(String projectDir, String commitID, String branchID) {
        return new File(projectDir + "\\" + ConstantNames.KUFLEX + "\\branches\\" + branchID + "\\" + commitID, ConstantNames.SNAPSHOTDBFile);
    }

    /**
     * Get CommitSnapshot object of a commit
     *
     * @param commitID ID of the commit
     * @param branchID ID of the branch that the commit belongs to
     */
    public SnapshotModel getCommitSnapshotModel(String projectDir, String commitID, String branchID) throws Exception {
        File file = getCommitSnapshotFile(projectDir, commitID, branchID);
        return gson.fromJson(Files.readString(file.toPath()), SnapshotModel.class);
    }

    /**
     * Update CommitSnapshot. Be careful with this. As a snapshot file should only be updated once, not following the rule may result in weird behaviors
     *
     * @param commitID      ID of the Commit
     * @param branchID      ID of the branch that the commit belongs to
     * @param snapshotModel Updated Snapshot model
     */
    public void updateCommitSnapshot(String projectDir, String commitID, String branchID, SnapshotModel snapshotModel) throws Exception {
        var file = getCommitSnapshotFile(projectDir, commitID, branchID);
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(gson.toJson(snapshotModel));
        }
    }

    //DIFF******

    /**
     * Create a Diff directory inside a commit directory
     *
     * @param commitID ID of the commit
     * @param branchID ID of the branch the commit belongs to
     */
    public void createCommitDiffDirectory(String projectDir, String commitID, String branchID) throws Exception {
        File diffDir = new File(projectDir + "\\" + ConstantNames.KUFLEX + "\\branches\\" + branchID + "\\" + commitID + "\\" + ConstantNames.DiffDir);
        if (!diffDir.mkdirs()) {
            throw new Exception("Failed to create diff folder");
        }
    }

    /**
     * Create Diff file inside diff directory
     *
     * @param commitID  ID of the Commit
     * @param branchID  ID of the branch the commit belongs to
     * @param diffModel DiffModel that is going to be written in the created Diff file
     */
    public void createCommitDiffFile(String projectDir, String commitID, String branchID, DiffModel diffModel) throws Exception {
        //Check if the diffs folder exists, if not create one
        String diffDirPath = projectDir + "\\" + ConstantNames.KUFLEX + "\\branches\\" + branchID + "\\" + commitID + "\\" + ConstantNames.DiffDir;
        File diffDir = new File(diffDirPath);
        if (!diffDir.exists()) {
            diffDir.mkdirs();
        }
        //Create file
        String fileID = UUID.randomUUID().toString();
        File file = new File(diffDirPath, fileID + ".json");
        if (!file.createNewFile()) {
            throw new Exception("Failed to create new diff file");
        }

        //write to file
        try (FileWriter fileWriter = new FileWriter(file)) {
            String data = gson.toJson(diffModel);
            System.out.println("Diff model data :\n " + data + "\n-------------\n");
            fileWriter.write(data);
        }
    }

    /**
     * get CommitDiff file based on diffID as a File object
     *
     * @param commitID    ID of the commit
     * @param branchID    ID of the branch the Commit belongs to
     * @param diffModelID ID of the diff
     * @throws Exception
     */
    public File getCommitDiffFile(String projectDir, String commitID, String branchID, String diffModelID) throws Exception {
        File diffDir = new File(projectDir + "\\" + ConstantNames.KUFLEX + "\\branches\\" + branchID + "\\" + commitID + "\\" + ConstantNames.DiffDir + "\\" + diffModelID);
        if (!diffDir.exists()) {
            throw new Exception("Couldn't find diffID " + diffModelID + " for commit " + commitID + " for branch " + branchID);
        }
        return diffDir;
    }

    /**
     * get CommitDiff file based on diffID as a DiffModel object
     *
     * @param commitID    ID of the commit
     * @param branchID    ID of the branch the Commit belongs to
     * @param diffModelID ID of the diff
     * @throws Exception
     */
    public DiffModel getCommitDiffModel(String projectDir, String commitID, String branchID, String diffModelID) throws Exception {
        File file = getCommitDiffFile(projectDir, commitID, branchID, diffModelID);
        return gson.fromJson(Files.readString(file.toPath()), DiffModel.class);
    }

    /**
     * Get List of Diff models of a Commit by using commitID
     *
     * @param commitID ID of the commit
     * @param branchID ID of the branch the commit belongs to
     */
    public List<DiffModel> getAllDiffsOfCommit(String projectDir, String commitID, String branchID) throws Exception {
        List<DiffModel> diffModels = new ArrayList<>();
        File diffDir = new File(projectDir + "\\" + ConstantNames.KUFLEX + "\\branches\\" + branchID + "\\" + commitID + "\\" + ConstantNames.DiffDir);
        for (File f : Objects.requireNonNull(diffDir.listFiles())) {
            diffModels.add(getCommitDiffModel(projectDir, commitID, branchID, f.getName()));
        }
        return diffModels;
    }


    public void createSnapshotDBFile(String projectDir, SnapshotDB initialSnapshotDB) throws IOException {
        File path = new File(projectDir + "\\" + ConstantNames.KUFLEX, ConstantNames.SNAPSHOTDBFile);
        path.createNewFile();

        try (FileWriter fileWriter = new FileWriter(path)) {
            fileWriter.write(gson.toJson(initialSnapshotDB));
        }
    }

    public void addSnapshotToSnapshotDB(String projectDir, SnapshotModel snapshotModel) throws IOException {
        File file = new File(projectDir + "\\" + ConstantNames.KUFLEX + "\\" + ConstantNames.SNAPSHOTDBFile);

        String data = Files.readString(file.toPath());
        var snap = gson.fromJson(data, SnapshotDB.class);
        snap.getSnapshotModels().add(snapshotModel);
    }

    public void createDiffDirectory(String projectDir) {
        File file = new File(projectDir + "\\" + ConstantNames.KUFLEX + "\\" + ConstantNames.DiffDir);
        file.mkdirs();
    }

    public void createInitialDiffDB(String projectDir, DiffDB diffDB) throws IOException {
        File file = new File(projectDir + "\\" + ConstantNames.KUFLEX + "\\" + ConstantNames.DiffDir, diffDB.getName());
        file.createNewFile();
        try (FileWriter fileWriter = new FileWriter(file)) {
            String data = gson.toJson(diffDB);
            fileWriter.write(data);
        }
    }
}
