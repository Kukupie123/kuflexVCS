package dev.kukode.services.repo;

import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.NotDirectoryException;
import java.util.Date;

public interface IRepoService {
    boolean initializeRepo(String directory, String projectName, Date creationDate, String creator, String intialBranch) throws Exception;

    boolean doesRepoAlreadyExist(String directory);
}
