/*
 * Copyright (C) 15/07/23, 4:33 am KUKODE - Kuchuk Boram Debbarma . - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package dev.kukode.services.repo;

import com.google.gson.Gson;
import dev.kukode.beans.KuflexRepoModel;
import dev.kukode.beans.SnapshotModel;
import dev.kukode.beans.branches.BranchDB;
import dev.kukode.beans.branches.BranchModel;
import dev.kukode.beans.commits.CommitModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.DirectoryNotEmptyException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SuppressWarnings("SameParameterValue")
@Service
public class RepoService implements IRepoService {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    private List<String> getProjectFilesPath(String projectDir, String basePath) {
    /*
    RECURSIVELY access subdirectories
    1. Access the rootDir
    2. Get list of files in rootDir
    3. Iterate the list of files (call it f)
        3.1 If "f" is a file then add its relative path to the list
        3.2 If "f" is a directory then add the return value of getFiles(f) to the list
     */
        List<String> filePaths = new ArrayList<>();

        // Access RootDir
        File file = new File(projectDir);

        // Iterate rootDir's subdirectories
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


    private boolean createRepoDir(String dir, KuflexRepoModel kuflexRepo) throws Exception {
        logger.info("Create Repo dir for path : " + dir + " and projectName " + kuflexRepo.projectName);
        //Create the necessary directories
        File rootDir = new File(dir);
        if (!rootDir.isDirectory()) {
            throw new Exception("Not a directory");
        }
        File kuFlexDir = new File(rootDir, ".kuflex");
        if (!kuFlexDir.mkdir()) {
            throw new Exception("Failed to create .kuFlex repository folder");
        }
        Gson gson = new Gson();
        //create the kuflex repository file
        String kuFlexRepoJSON = gson.toJson(kuflexRepo);

        //Save kuFlexRepo to ".kuflex" folder
        File kuFlexRepoFile = new File(kuFlexDir, "kuFlexRepo.json");
        try (FileWriter fileWriter = new FileWriter(kuFlexRepoFile)) {
            fileWriter.write(kuFlexRepoJSON);
        } catch (Exception e) {
            throw new Exception(e);
        }

        return true;
    }

    @Override
    public boolean initializeRepo(String projectDir, String projectName, Date creationDate, String creator) throws Exception {


        //Check if KuFlex repo already exist
        if (doesRepoAlreadyExist(projectDir)) {
            throw new DirectoryNotEmptyException("KuFlex Repository already exists");
        }

        //Create kuflexrepo
        logger.info("Creating Repository folder with projectName : " + projectName);
        KuflexRepoModel kuflexRepo = new KuflexRepoModel(projectName, creator, creationDate);

        //Create repo directories
        if (!createRepoDir(projectDir, kuflexRepo)) {
            throw new Exception("Failed to create KuFlex repo projectDir");
        }

        //Create initial branch
        logger.info("Initial Branch Creation");
        BranchModel defaultBranch = createInitialBranch(projectDir, "default");
        //Create initial Commit
        createInitialCommit(projectDir, "Initial Commit", "", defaultBranch.getUID());
        //Update initial Commit value and initial Branch value in kuflexRepo.json

        return false;
    }

    private BranchModel createInitialBranch(String projectDir, String branchName) throws Exception {
        //Create BranchModel
        BranchModel branchModel = new BranchModel(branchName, new Date(), "", "");
        //Create branch directory
        File branchDir = new File(projectDir + "\\.kuflex\\branches\\" + branchModel.getUID());
        if (!branchDir.mkdirs()) {
            throw new Exception("Failed to create " + branchName + " initial branch directories");
        }
        //Create branch DB
        File branchDBFile = new File(projectDir + "\\.kuflex\\branchesDB.json");
        if (!branchDBFile.createNewFile()) {
            throw new Exception("Failed to create branch DB");
        }
        //Write DB content
        try (FileWriter branchDBWriter = new FileWriter(branchDBFile)) {
            BranchDB branchDB = new BranchDB();
            branchDB.branches = new ArrayList<>();
            branchDB.branches.add(branchModel);
            Gson gson = new Gson();
            String jsonData = gson.toJson(branchDB);
            branchDBWriter.write(jsonData);
            return branchModel;
        }
    }

    private CommitModel createInitialCommit(String projectDir, String commitName, String commitComment, String branchID) throws Exception {
        //Create commit model
        CommitModel commitModel = new CommitModel(commitName, commitComment, new Date(), branchID, "");
        //Create commit directory
        String commitPath = projectDir + "\\.kuflex\\branches\\" + branchID + "\\" + commitModel.getUID();
        File commitDir = new File(commitPath);
        if (!commitDir.mkdirs()) {
            throw new Exception("Failed to create commit directory");
        }
        //Create CommitDB file
        File commitDB = new File(projectDir + "\\.kuflex\\branches\\" + branchID + "\\commitsDB.json");
        if (!commitDB.createNewFile()) {
            throw new Exception("Failed to create commit DB File");
        }
        //Write the new commit to commitDB file
        try (FileWriter fileWriter = new FileWriter(commitDB)) {
            Gson gson = new Gson();
            String jsonDate = gson.toJson(commitModel);
            fileWriter.write(jsonDate);
        }

        //Create snapshot file for the commit
        createSnapshot(projectDir, commitPath);
        //TODO: Create File Content Diff
        return commitModel;
    }

    private void createSnapshot(String projectDir, String commitPath) throws Exception {
        //Create snapshot file in commit path
        File snapshotFile = new File(commitPath, "kuFlexSnap.json");
        if (!snapshotFile.createNewFile()) {
            throw new Exception("Failed to create snapshot");
        }

        //Get file paths
        List<String> filePaths = getProjectFilesPath(projectDir, "");
        List<String> pathToRemove = new ArrayList<>();
        for (String path : filePaths) {
            if (path.contains(".kuflex")) {
                pathToRemove.add(path);
            }
        }
        filePaths.removeAll(pathToRemove);

        //Write file path to snapshot
        try (FileWriter snapshotWriter = new FileWriter(snapshotFile)) {
            SnapshotModel snapshotModel = new SnapshotModel();
            snapshotModel.files = filePaths;
            Gson gson = new Gson();
            String jsonData = gson.toJson(snapshotModel);
            snapshotWriter.write(jsonData);
        }
    }

    @Override
    public boolean doesRepoAlreadyExist(String projectDir) {
        File repoDir = new File(projectDir + "\\.kuflex");
        return repoDir.isDirectory();
    }
}
