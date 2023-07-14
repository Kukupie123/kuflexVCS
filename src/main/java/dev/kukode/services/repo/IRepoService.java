package dev.kukode.services.repo;

import java.util.Date;


public interface IRepoService {
    /**
     * Initialize the repository with the respective folders and files
     *
     * @param projectDir   Directory of the root project
     * @param projectName  Name of the project
     * @param creationDate Date of creation
     * @param creator      Name of Creator
     * @return true if successful
     * @throws Exception File-Write exception
     */
    boolean initializeRepo(String projectDir, String projectName, Date creationDate, String creator) throws Exception;

    boolean doesRepoAlreadyExist(String projectDir);

}
