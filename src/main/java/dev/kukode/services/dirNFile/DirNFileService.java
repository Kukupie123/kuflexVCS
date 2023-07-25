/*
 * Copyright (C) 26/07/23, 12:51 am KUKODE - Kuchuk Boram Debbarma . - All Rights Reserved
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
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

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
        String path = projectDir + "\\" + ConstantNames.KUFLEX + "\\branches\\" + branchID + "\\" + ConstantNames.CommitsDBFile;
        File file = new File(path);
        if (!file.isFile()) {
            throw new Exception("Failed to load " + ConstantNames.CommitsDBFile + path);
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


    public void createSnapshotDBFile(String projectDir, SnapshotDB initialSnapshotDB) throws IOException {
        File path = new File(projectDir + "\\" + ConstantNames.KUFLEX, ConstantNames.SNAPSHOTDBFile);
        path.createNewFile();

        try (FileWriter fileWriter = new FileWriter(path)) {
            fileWriter.write(gson.toJson(initialSnapshotDB));
        }
    }


    public void createDiffDirectory(String projectDir) {
        File file = new File(projectDir + "\\" + ConstantNames.KUFLEX + "\\" + ConstantNames.DiffDir);
        file.mkdirs();
    }

    public static String encode(String input) {
        byte[] encodedBytes = Base64.getEncoder().encode(input.getBytes());
        return new String(encodedBytes);
    }

    public static String decode(String encodedInput) {
        byte[] decodedBytes = Base64.getDecoder().decode(encodedInput);
        return new String(decodedBytes);
    }

    public void createOrUpdateFileDiff(String projectDir, String fileName, DiffModel diffModel) throws IOException {
        String encodedName = encode(fileName);
        File file = new File(projectDir + "\\" + ConstantNames.KUFLEX + "\\" + ConstantNames.DiffDir, encodedName);
        if (file.exists() && file.isFile()) {
            var diffDB = gson.fromJson(Files.readString(file.toPath()), DiffDB.class);
            if (diffDB.getDiffModels() == null) {
                diffDB.setDiffModels(new ArrayList<>());
            }
            //Remove diffModel if same index exist
            DiffModel toRemove = null;
            for (DiffModel d : diffDB.getDiffModels()) {
                if (d.getIndex().equals(diffModel.getIndex())) {
                    toRemove = d;
                    break;
                }
            }

            if (toRemove != null) {
                diffDB.getDiffModels().remove(toRemove);
            }

            //Add new one
            diffDB.getDiffModels().add(diffModel);

            try (FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.write(gson.toJson(diffDB));
            }
        } else {
            var diffDB = new DiffDB();
            diffModel.setIndex("0"); //Since this is the first time it is being created
            diffDB.getDiffModels().add(diffModel);
            file.createNewFile();
            try (FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.write(gson.toJson(diffDB));
            }
        }
    }

    public DiffDB getDiffDBForFile(String projectDir, String s) throws IOException {
        String encodedName = encode(s);
        File file = new File(projectDir + "\\" + ConstantNames.KUFLEX + "\\" + ConstantNames.DiffDir, encodedName);
        if (!file.exists() && !file.isFile()) throw new NoSuchFileException("Diff file not found " + s);

        return gson.fromJson(Files.readString(file.toPath()), DiffDB.class);
    }

    public boolean doesDiffDBExist(String projectDir, String fileName) {
        String encodedName = encode(fileName);
        File file = new File(projectDir + "\\" + ConstantNames.KUFLEX + "\\" + ConstantNames.DiffDir, encodedName);
        return (file.exists() && file.isFile());
    }

    public void addNewSnapshot(String projectDir, SnapshotModel newSnap) throws IOException {
        File path = new File(projectDir + "\\" + ConstantNames.KUFLEX, ConstantNames.SNAPSHOTDBFile);
        var db = gson.fromJson(Files.readString(path.toPath()), SnapshotDB.class);
        db.getSnapshotModels().add(newSnap);
        try (FileWriter fileWriter = new FileWriter(path)) {
            fileWriter.write(gson.toJson(db));
        }
    }

    public void addNewCommit(String projectDir, CommitModel newCommit) throws Exception {
        var commitDB = getCommitDbModelForBranch(projectDir, newCommit.getBranchID());
        commitDB.commits.add(newCommit);
        var file = getCommitDbFileForBranch(projectDir, newCommit.getBranchID());

        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(gson.toJson(commitDB));
        }
    }
}
