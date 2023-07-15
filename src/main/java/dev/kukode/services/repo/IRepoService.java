/*
 * Copyright (C) 15/07/23, 9:52 pm KUKODE - Kuchuk Boram Debbarma . - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

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


    boolean createNewCommit(WIP parameter); //TODO: Complete this

}
