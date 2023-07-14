package dev.kukode.services.repo;

import dev.kukode.beans.KuflexRepo;
import dev.kukode.services.dir.DirService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.DirectoryNotEmptyException;
import java.util.Date;

@Service
public class RepoService implements IRepoService {

    public RepoService(DirService dirService) {
        this.dirService = dirService;
    }

    final DirService dirService;
    Logger logger = LoggerFactory.getLogger(this.getClass());


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
        if (!dirService.createRepoDir(projectDir, kuflexRepo)) {
            throw new Exception("Failed to create KuFlex repo projectDir");
        }

        //Initial Commit with all files excluding the ones in .KuFlexIgnore
        dirService.createDefaultBranchNCommitDir(projectDir);
        return false;
    }

    @Override
    public boolean doesRepoAlreadyExist(String projectDir) {
        File repoDir = new File(projectDir + "\\.kuflex");
        return repoDir.isDirectory();
    }
}
