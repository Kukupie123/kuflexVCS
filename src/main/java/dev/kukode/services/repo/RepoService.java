package dev.kukode.services.repo;

import com.google.gson.Gson;
import dev.kukode.beans.KuflexRepo;
import dev.kukode.beans.commits.CommitDB;
import dev.kukode.beans.commits.CommitModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class RepoService implements IRepoService {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    private List<String> getProjectFilesPath(String projectDir) {
          /*
        RECURSIVELY access subdirectories
        1. Access the rootDir
        2. Get list of files in rootDir
        3. Iterate the list of files (call it f)
            3.1 If "f" is a file then add it to the list
            3.2 If "f" is a directory then add the return value of getFiles(f) to the list
         */
        List<String> filePaths = new ArrayList<>();

        //Access RootDir
        File file = new File(projectDir);

        //Iterate rootDir's subdirectories
        if (file.isDirectory()) {
            File[] dirFiles = file.listFiles();
            if (dirFiles == null) return filePaths;
            for (File dirFile : dirFiles) {
                if (dirFile.isFile()) {
                    filePaths.add(dirFile.getAbsolutePath());
                } else {
                    List<String> subDirFiles = getProjectFilesPath(dirFile.getAbsolutePath());
                    filePaths.addAll(subDirFiles);
                }
            }
        }
        return filePaths;
    }

    private boolean createRepoDir(String dir, KuflexRepo kuflexRepo) throws Exception {
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

    private void createDefaultBranchNCommitDir(String projectDir) {
        File branchDir = new File(projectDir + "\\.kuflex\\branches\\default");
        //create default branch folder
        if (branchDir.mkdirs()) {
            File commitDir = new File(branchDir, "\\commits\\default");
            //create default commit folder
            if (!commitDir.mkdirs()) {
                logger.error("Failed to create default commit : " + commitDir.getPath());
                return;
            }
            //create commits db for default branch
            File commitDBFile = new File(branchDir, "commitDB.json");
            try {
                commitDBFile.createNewFile();
                CommitDB commitDB = new CommitDB();
                commitDB.commits = new ArrayList<>();
                commitDB.commits.add(new CommitModel("initial commit", "", "", ""));
                Gson gson = new Gson();
                String jsonData = gson.toJson(commitDB);
                try (FileWriter commitDBWriter = new FileWriter(commitDBFile)) {
                    commitDBWriter.write(jsonData);
                }
            } catch (IOException e) {
                logger.error(e.getMessage() + " : \n" + Arrays.toString(e.getStackTrace()));
            }

            return;
        }
        logger.error("Failed to create branch default directory " + projectDir);
    }

    @Override
    public boolean initializeRepo(String projectDir, String projectName, Date creationDate, String creator) throws Exception {


        //Check if KuFlex repo already exist
        if (doesRepoAlreadyExist(projectDir)) {
            throw new DirectoryNotEmptyException("KuFlex Repository already exists");
        }

        //Create kuflexrepo
        logger.info("Creating Repository folder with projectName : " + projectName);
        KuflexRepo kuflexRepo = new KuflexRepo(projectName, creator, creationDate);

        //Create repo directories
        if (!createRepoDir(projectDir, kuflexRepo)) {
            throw new Exception("Failed to create KuFlex repo projectDir");
        }

        //Initial Commit with all files excluding the ones in .KuFlexIgnore
        createDefaultBranchNCommitDir(projectDir);

        return false;
    }

    @Override
    public boolean doesRepoAlreadyExist(String projectDir) {
        File repoDir = new File(projectDir + "\\.kuflex");
        return repoDir.isDirectory();
    }
}
