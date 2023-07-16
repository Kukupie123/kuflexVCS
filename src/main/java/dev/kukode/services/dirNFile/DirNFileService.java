/*
 * Copyright (C) 16/07/23, 10:18 pm KUKODE - Kuchuk Boram Debbarma . - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package dev.kukode.services.dirNFile;

import com.google.gson.Gson;
import dev.kukode.models.KuflexRepoModel;
import dev.kukode.models.branches.BranchDB;
import dev.kukode.models.commits.CommitDB;
import dev.kukode.util.ConstantNames;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;

@Service
public class DirNFileService {
    final Gson gson;

    public DirNFileService(Gson gson) {
        this.gson = gson;
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

    public File getCommitDbFileForBranch(String projectDir, String branchID) throws Exception {
        File file = new File(projectDir + "\\" + ConstantNames.KUFLEX + "\\branches\\" + branchID + "\\" + ConstantNames.CommitsDBFile);
        if (!file.isFile()) {
            throw new Exception("Failed to load " + ConstantNames.KUFLEXREPOFILE);
        }
        return file;
    }

    public CommitDB getCommitDbModel(String projectDir, String branchID) throws Exception {
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

}
