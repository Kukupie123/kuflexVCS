package dev.kukode.services.repo;

import dev.kukode.beans.KuflexRepo;
import dev.kukode.services.dir.DirService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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
    public boolean initializeRepo(String directory, String projectName, Date creationDate, String creator, String initialBranch) throws Exception {
        /*
        1. Validate if there is already a valid repo setup
        2. If not we can continue
        3. create a ".kuflex" folder
        4. Create a kuflexrepo object
        5. Initial Commit with all files excluding the ones in .KuFlexignore
        6. Saving the kuflexrepo as a file inside .kuflex
         */

        //Check if KuFlex repo already exist
        if (doesRepoAlreadyExist(directory)) {
            throw new DirectoryNotEmptyException("KuFlex Repository already exists");
        }

        //Create kuflexrepo
        logger.info("Creating Repository folder with projectName : " + projectName);
        KuflexRepo kuflexRepo = new KuflexRepo(projectName, creator, creationDate);

        //Create repo directories
        if (!dirService.createRepoDir(directory, kuflexRepo)) {
            throw new Exception("Failed to create KuFlex repo directory");
        }


        //Initial Commit with all files excluding the ones in .KuFlexIgnore
        return false;
    }

    @Override
    public boolean doesRepoAlreadyExist(String directory) {
        //TODO: Complete this
        return false;
    }
}
