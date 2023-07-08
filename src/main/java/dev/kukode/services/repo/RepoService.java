package dev.kukode.services.repo;

import dev.kukode.beans.KuflexRepo;
import dev.kukode.services.dir.DirService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.NotDirectoryException;
import java.util.Date;

@Service
public class RepoService implements IRepoService {

    public RepoService(DirService dirService) {
        this.dirService = dirService;
    }

    final DirService dirService;


    @Override
    public boolean initializeRepo(String directory, String projectName, Date creationDate, String creator) throws Exception {
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

        //Create a KuFlex folder
        File file = new File(directory);
        if (!file.isDirectory()) throw new NotDirectoryException("Not a directory");
        file = new File(directory + "/.kuflex");
        if (!file.mkdir())
            throw new Exception("Failed to create .kuflex directory");

        //Create kuflexrepo
        KuflexRepo repo = new KuflexRepo(projectName, creator, creationDate);

        //Initial Commit with all files excluding the ones in .KuFlexIgnore
        //TODO: Figure out how committing will work
        return false;
    }

    @Override
    public boolean doesRepoAlreadyExist(String directory) {
        return true;
    }
}
