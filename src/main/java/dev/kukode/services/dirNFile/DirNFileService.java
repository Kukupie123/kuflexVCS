/*
 * Copyright (C) 18/07/23, 8:41 pm KUKODE - Kuchuk Boram Debbarma . - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package dev.kukode.services.dirNFile;

import com.google.gson.Gson;
import dev.kukode.models.DiffModel;
import dev.kukode.models.KuflexRepoModel;
import dev.kukode.models.SnapshotModel;
import dev.kukode.models.branches.BranchDB;
import dev.kukode.models.commits.CommitDB;
import dev.kukode.models.commits.CommitModel;
import dev.kukode.util.ConstantNames;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
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

    public File getKuFlexRepoFile(String projectDir) throws Exception {
        File file = new File(projectDir + "\\" + ConstantNames.KUFLEX, ConstantNames.KUFLEXREPOFILE);
        if (!file.isFile()) {
            throw new Exception("Failed to load " + ConstantNames.KUFLEXREPOFILE);
        }
        return file;
    }

    public KuflexRepoModel getKuFlexRepoModel(String projectDir) throws Exception {
        File file = getKuFlexRepoFile(projectDir);
        return gson.fromJson(Files.readString(file.toPath()), KuflexRepoModel.class);
    }

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
    public void createBranchDirectory(String projectDir, String branchID) throws Exception {
        File branchDir = new File(projectDir + "\\" + ConstantNames.KUFLEX + "\\branches\\" + branchID);
        if (!branchDir.mkdirs()) {
            throw new Exception("Failed to create branch directory for branchID " + branchID);
        }
    }

    public void createBranchDBFile(String projectDir) throws Exception {
        File branchDBFile = new File(projectDir + "\\" + ConstantNames.KUFLEX + "\\" + ConstantNames.BranchesDBFILE);
        if (branchDBFile.exists()) {
            throw new Exception("BranchDBFile already exists");
        }
        if (!branchDBFile.createNewFile()) {
            throw new Exception("Failed to create branchDBFile");
        }
    }

    public File getBranchDbFile(String projectDir) throws Exception {
        File file = new File(projectDir + "\\" + ConstantNames.KUFLEX + "\\" + ConstantNames.BranchesDBFILE);
        if (!file.isFile()) {
            throw new Exception("Failed to load " + ConstantNames.KUFLEXREPOFILE);
        }
        return file;
    }

    public BranchDB getBranchDbModel(String projectDir) throws Exception {
        File file = getBranchDbFile(projectDir);
        return gson.fromJson(Files.readString(file.toPath()), BranchDB.class);
    }

    public void updateBranchDbFile(String projectDir, BranchDB branchDB) throws Exception {
        File branchDbFile = getBranchDbFile(projectDir);
        String data = gson.toJson(branchDB);
        try (FileWriter fileWriter = new FileWriter(branchDbFile)) {
            fileWriter.write(data);
        }
    }

    //COMMIT**************
    public void createCommitDir(String projectDir, String branchID, String commitID) throws Exception {
        File commitDir = new File(projectDir + "\\" + ConstantNames.KUFLEX + "\\branches\\" + branchID + "\\" + commitID);
        if (!commitDir.mkdirs()) {
            throw new Exception("Failed to create Commit Directory");
        }
    }

    public void createCommitDBFileForBranch(String projectDir, String branchID) throws Exception {
        String filePath = projectDir + "\\" + ConstantNames.KUFLEX + "\\branches\\" + branchID + "\\" + ConstantNames.CommitsDBFile;
        File commitDbFile = new File(filePath);
        if (!commitDbFile.createNewFile()) {
            throw new Exception("Failed to create commit DB File");
        }
    }


    public CommitModel getCommitByID(String projectDir, String commitID, String branchID) throws Exception {
        CommitDB commitDB = getCommitDbModelForBranch(projectDir, branchID);
        for (CommitModel commit : commitDB.commits) {
            if (commit.getUID().equals(commitID)) {
                return commit;
            }
        }
        return null;
    }


    public File getCommitDbFileForBranch(String projectDir, String branchID) throws Exception {
        File file = new File(projectDir + "\\" + ConstantNames.KUFLEX + "\\branches\\" + branchID + "\\" + ConstantNames.CommitsDBFile);
        if (!file.isFile()) {
            throw new Exception("Failed to load " + ConstantNames.KUFLEXREPOFILE);
        }
        return file;
    }

    public CommitDB getCommitDbModelForBranch(String projectDir, String branchID) throws Exception {
        File file = getCommitDbFileForBranch(projectDir, branchID);
        return gson.fromJson(Files.readString(file.toPath()), CommitDB.class);
    }

    public void updateCommitDbForBranch(String projectDir, String branchID, CommitDB commitDB) throws Exception {
        File commitDbFile = getCommitDbFileForBranch(projectDir, branchID);
        String data = gson.toJson(commitDB);
        try (FileWriter fileWriter = new FileWriter(commitDbFile)) {
            fileWriter.write(data);
        }
    }

    //SNAPSHOT*************
    public void createCommitSnapshot(String projectDir, String commitID, String branchID) throws Exception {
        var snapFile = getCommitSnapshotFile(projectDir, commitID, branchID);
        if (!snapFile.createNewFile()) {
            throw new Exception("Failed to create snapshot for commitID : " + commitID + ", branchID : " + branchID);
        }
    }

    public File getCommitSnapshotFile(String projectDir, String commitID, String branchID) {
        return new File(projectDir + "\\" + ConstantNames.KUFLEX + "\\branches\\" + branchID + "\\" + commitID, ConstantNames.SNAPSHOTFILE);
    }

    public SnapshotModel getCommitSnapshotModel(String projectDir, String commitID, String branchID) throws Exception {
        File file = getCommitSnapshotFile(projectDir, commitID, branchID);
        return gson.fromJson(Files.readString(file.toPath()), SnapshotModel.class);
    }

    public void updateCommitSnapshot(String projectDir, String commitID, String branchID, SnapshotModel snapshotModel) throws Exception {
        var file = getCommitSnapshotFile(projectDir, commitID, branchID);
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(gson.toJson(snapshotModel));
        }
    }

    //DIFF******
    public void createCommitDiffDirectory(String projectDir, String commitID, String branchID) throws Exception {
        File diffDir = new File(projectDir + "\\" + ConstantNames.KUFLEX + "\\branches\\" + branchID + "\\" + commitID + "\\" + ConstantNames.DiffDir);
        if (!diffDir.mkdirs()) {
            throw new Exception("Failed to create diff folder");
        }
    }

    public void createCommitDiffFile(String projectDir, String commitID, String branchID, DiffModel diffModel) throws Exception {
        //Create file
        String fileID = UUID.randomUUID().toString();
        File file = new File(projectDir + "\\" + ConstantNames.KUFLEX + "\\branches\\" + branchID + "\\" + commitID + "\\" + ConstantNames.DiffDir, fileID + ".json");
        if (!file.createNewFile()) {
            throw new Exception("Failed to create new diff file");
        }

        //write to file
        try (FileWriter fileWriter = new FileWriter(file)) {
            String data = gson.toJson(diffModel);
            fileWriter.write(data);
        }
    }

    public File getCommitDiffFile(String projectDir, String commitID, String branchID, String diffModelID) throws Exception {
        File diffDir = new File(projectDir + "\\" + ConstantNames.KUFLEX + "\\branches" + branchID + "\\" + commitID + "\\" + ConstantNames.DiffDir + "\\" + diffModelID);
        if (!diffDir.exists()) {
            throw new Exception("Couldn't find diffID " + diffModelID + " for commit " + commitID + " for branch " + branchID);
        }
        return diffDir;
    }

    public DiffModel getCommitDiffModel(String projectDir, String commitID, String branchID, String diffModelID) throws Exception {
        File file = getCommitDiffFile(projectDir, commitID, branchID, diffModelID);
        return gson.fromJson(Files.readString(file.toPath()), DiffModel.class);
    }

    public List<DiffModel> getAllDiffsOfCommit(String projectDir, String commitID, String branchID) throws Exception {
        List<DiffModel> diffModels = new ArrayList<>();
        File diffDir = new File(projectDir + "\\" + ConstantNames.KUFLEX + "\\branches\\" + branchID + "\\" + commitID + "\\" + ConstantNames.DiffDir);
        for (File f : Objects.requireNonNull(diffDir.listFiles())) {
            diffModels.add(getCommitDiffModel(projectDir, commitID, branchID, f.getName()));
        }
        return diffModels;
    }


}
