/*
 * Copyright (C) 03/08/23, 11:12 am KUKODE - Kuchuk Boram Debbarma . - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package dev.kukode.services.dirNFile;

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
import dev.kukode.util.ConstantNames;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class DirNFileService {
    final Gson gson;

    public DirNFileService(Gson gson) {
        this.gson = gson;
    }

    //PROJECT*************

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

    public void writeContentToProjectFile(String content, String filePath) throws IOException {
        File file = new File(ConstantNames.ProjectPath + filePath);
        if (!file.exists()) {
            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            file.createNewFile();
        }
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(content);
        }
    }

    public void removeProjectFile(String relativePath) {
        File file = new File(ConstantNames.ProjectPath + relativePath);
        if (!file.exists() || !file.isFile()) return;
        file.delete();
    }

    public static String encode(String input) {
        byte[] encodedBytes = Base64.getEncoder().encode(input.getBytes());
        return new String(encodedBytes);
    }

    //REPOSITORY***********************

    /**
     * Create a new repository directory at the root of the project
     */
    public void createKuFlexRepoDir() throws Exception {
        File rootDir = new File(ConstantNames.ProjectPath);
        if (!rootDir.isDirectory()) {
            throw new Exception(ConstantNames.ProjectPath + " is not a directory. Failed to createKuFlexRepoDir");
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
    public void createKuFlexRepoFile(KuflexRepoModel kuflexRepoModel) throws Exception {
        File kuFlexRepoFile = new File(ConstantNames.ProjectPath + "\\" + ConstantNames.KUFLEX, ConstantNames.KUFLEXREPOFILE);
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
    public File getKuFlexRepoFile() throws Exception {
        File file = new File(ConstantNames.ProjectPath + "\\" + ConstantNames.KUFLEX, ConstantNames.KUFLEXREPOFILE);
        if (!file.isFile()) {
            throw new Exception("Failed to load " + ConstantNames.KUFLEXREPOFILE);
        }
        return file;
    }

    /**
     * Get KuFlexRepo file as KuFlexRepoModel
     */
    public KuflexRepoModel getKuFlexRepoModel() throws Exception {
        File file = getKuFlexRepoFile();
        return gson.fromJson(Files.readString(file.toPath()), KuflexRepoModel.class);
    }

    /**
     * Update KuFlexRepo with a new KuFlexRepo object
     *
     * @param kuflexRepoModel Updated KuFlexRepo object
     */
    public boolean updateKuFlexRepo(KuflexRepoModel kuflexRepoModel) throws Exception {
        File repoFile = new File(ConstantNames.ProjectPath + "\\" + ConstantNames.KUFLEX, ConstantNames.KUFLEXREPOFILE);
        if (!repoFile.exists() || !repoFile.isFile()) {
            throw new Exception("Repo doesn't exist or is not a file");
        }
        try (FileWriter fileWriter = new FileWriter(repoFile)) {
            String data = gson.toJson(kuflexRepoModel);
            fileWriter.write(data);
            return true;
        }
    }


    //COMMITS


    public CommitModel getCommitByID(String commitID, String branchID) throws Exception {
        CommitDB commitDB = getCommitDbModelForBranch(branchID);
        for (CommitModel commit : commitDB.commits) {
            if (commit.getUID().equals(commitID)) {
                return commit;
            }
        }
        return null;
    }


    public File getCommitDbFileForBranch(String branchID) throws Exception {
        String path = ConstantNames.ProjectPath + "\\" + ConstantNames.KUFLEX + "\\branches\\" + branchID + "\\" + ConstantNames.CommitsDBFile;
        File file = new File(path);
        if (!file.isFile()) {
            throw new Exception("Failed to load " + ConstantNames.CommitsDBFile + path);
        }
        return file;
    }


    public CommitDB getCommitDbModelForBranch(String branchID) throws Exception {
        File file = getCommitDbFileForBranch(branchID);
        return gson.fromJson(Files.readString(file.toPath()), CommitDB.class);
    }

    public void AddOrUpdateCommit(CommitModel newCommit) throws Exception {
        var commitDB = getCommitDbModelForBranch(newCommit.getBranchID());
        CommitModel commitModelToRemove = null;
        for (CommitModel cm : commitDB.commits) {
            if (cm.getUID().equals(newCommit.getUID())) {
                commitModelToRemove = cm;
                break;
            }
        }
        if (commitModelToRemove != null) commitDB.commits.remove(commitModelToRemove);

        commitDB.commits.add(newCommit);
        var file = getCommitDbFileForBranch(newCommit.getBranchID());

        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(gson.toJson(commitDB));
        }
    }


    //SNAPSHOTS

    public SnapshotModel getSnapshot(String branchID, String commitID) throws IOException {
        File path = new File(ConstantNames.ProjectPath + "\\" + ConstantNames.KUFLEX + "\\" + ConstantNames.SNAPSHOTDBFile);
        var db = gson.fromJson(Files.readString(path.toPath()), SnapshotDB.class);
        for (var s : db.getSnapshotModels()) {
            if (s.getId().equals(ConstantNames.GET_UID_OF_SNAPSHOT(branchID, commitID))) return s;
        }
        return null;
    }


    //DIFF
    public void createDiffDirectory() {
        File file = new File(ConstantNames.ProjectPath + "\\" + ConstantNames.KUFLEX + "\\" + ConstantNames.DiffDir);
        file.mkdirs();
    }

    public void addFileDiff(String fileName, DiffModel diffModel) throws IOException {
        String encodedName = encode(fileName);
        File file = new File(ConstantNames.ProjectPath + "\\" + ConstantNames.KUFLEX + "\\" + ConstantNames.DiffDir, encodedName);
        if (file.exists()) {
            var diffDB = gson.fromJson(Files.readString(file.toPath()), DiffDB.class);
            if (diffDB.getDiffModels() == null) {
                diffDB.setDiffModels(new ArrayList<>());
            }
            //Remove diffModel if same index exist
            DiffModel toRemove = null;
            for (DiffModel d : diffDB.getDiffModels()) {
                if (d.getID().equals(diffModel.getID())) {
                    toRemove = d;
                    break;
                }
            }

            if (toRemove != null) {
                diffDB.getDiffModels().remove(toRemove);
            }

            //Add new one
            diffModel.setInitialDiff(false);
            diffDB.getDiffModels().add(diffModel);

            try (FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.write(gson.toJson(diffDB));
            }
        } else {
            var diffDB = new DiffDB();
            diffModel.setInitialDiff(true);
            diffDB.getDiffModels().add(diffModel);
            file.createNewFile();
            try (FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.write(gson.toJson(diffDB));
            }
        }
    }

    public DiffModel getDiffModel(String fileName, String branchID, String commitID) throws IOException {
        var db = getDiffDBForFile(fileName);
        for (DiffModel dm : db.getDiffModels()) {
            if (dm.getCommitID().equals(commitID) && dm.getBranchID().equals(branchID)) {
                return dm;
            }
        }
        return null;
    }

    public DiffDB getDiffDBForFile(String fileName) throws IOException {
        String encodedName = encode(fileName);
        File file = new File(ConstantNames.ProjectPath + "\\" + ConstantNames.KUFLEX + "\\" + ConstantNames.DiffDir, encodedName);
        if (!file.exists() && !file.isFile()) return null;

        return gson.fromJson(Files.readString(file.toPath()), DiffDB.class);
    }

    public boolean doesDiffDBExist(String fileName) {
        String encodedName = encode(fileName);
        File file = new File(ConstantNames.ProjectPath + "\\" + ConstantNames.KUFLEX + "\\" + ConstantNames.DiffDir, encodedName);
        return (file.exists() && file.isFile());
    }


    //Branch*********************
    public void addBranch(BranchModel branchModel) throws IOException {
        //Create branch directory if it doesn't exist
        File branchDir = new File(ConstantNames.ProjectPath + "\\" + ConstantNames.KUFLEX + "\\branches");
        if (!branchDir.exists()) {
            branchDir.mkdirs();
        }
        //Create branchModel directory if it doesn't exist
        File branchModelDir = new File(branchDir, branchModel.getUID());
        if (!branchModelDir.exists()) {
            branchModelDir.mkdirs();
        }

        //Add new branch Model to branchDB

        BranchDB branchDB;

        File branchDBFile = new File(ConstantNames.ProjectPath + "\\" + ConstantNames.KUFLEX + "\\" + ConstantNames.BranchesDBFILE);
        if (!branchDBFile.exists()) {
            branchDBFile.createNewFile();
            branchDB = new BranchDB();
        } else {
            branchDB = gson.fromJson(Files.readString(branchDBFile.toPath()), BranchDB.class);
        }

        branchDB.getBranches().add(branchModel);
        try (FileWriter fileWriter = new FileWriter(branchDBFile)) {
            fileWriter.write(gson.toJson(branchDB));
        }
    }

    //Commit*****************
    public void addCommit(CommitModel commitModel) throws IOException {
        //Get/Create CommitDB File

        File commitDBFile = new File(ConstantNames.ProjectPath + "\\" + ConstantNames.KUFLEX + "\\branches\\" + commitModel.getBranchID() + "\\" + ConstantNames.CommitsDBFile);
        CommitDB commitDB;
        if (!commitDBFile.exists()) {
            commitDBFile.createNewFile();
            commitDB = new CommitDB();
        } else {
            commitDB = gson.fromJson(Files.readString(commitDBFile.toPath()), CommitDB.class);
        }
        commitDB.commits.add(commitModel);

        try (FileWriter fileWriter = new FileWriter(commitDBFile)) {
            fileWriter.write(gson.toJson(commitDB));
        }
    }

    //SNAPSHOT*******************
    public void addSnapshot(SnapshotModel snapshotModel) throws IOException {
        File snapshotDBFile = new File(ConstantNames.ProjectPath + "\\" + ConstantNames.KUFLEX, ConstantNames.SNAPSHOTDBFile);
        SnapshotDB snapshotDB;
        if (!snapshotDBFile.exists()) {
            snapshotDBFile.createNewFile();
            snapshotDB = new SnapshotDB();
        } else {
            snapshotDB = gson.fromJson(Files.readString(snapshotDBFile.toPath()), SnapshotDB.class);
        }
        snapshotDB.getSnapshotModels().add(snapshotModel);
        try (FileWriter fileWriter = new FileWriter(snapshotDBFile)) {
            fileWriter.write(gson.toJson(snapshotDB));
        }
    }

}
