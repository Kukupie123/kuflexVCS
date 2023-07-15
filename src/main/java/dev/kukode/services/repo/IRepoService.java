/*
 * Copyright (C) 15/07/23, 9:31 pm KUKODE - Kuchuk Boram Debbarma . - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package dev.kukode.services.repo;

import dev.kukode.models.KuflexRepoModel;

import java.util.Date;


public interface IRepoService {

    /**
     * Read kuflexrepo.json file and returns a KuFlexRepoModel
     *
     * @param projectDir Directory of the project directory
     * @return KuFlexRepoModel based on kuflexrepo.json
     * @throws Exception File related exception
     */
    KuflexRepoModel getKuFlexRepo(String projectDir) throws Exception;

    /**
     * Update KuFlexRepo file of the repository
     *
     * @param projectDir      Path to project
     * @param kuflexRepoModel KuFlexRepoModel that is going to be used to update KuFlexRepo
     * @return true if successful
     * @throws Exception File related Exception
     */
    boolean updateKuFlexRepo(String projectDir, KuflexRepoModel kuflexRepoModel) throws Exception;

    /**
     * Validates if a repository directory and repository file exist.
     *
     * @param projectDir Path of project directory
     * @return true if successful
     */
    boolean doesRepoAlreadyExist(String projectDir);

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

    /**
     * Loads the active commit and branch from the KuFlexRepo repository.
     *
     * @param projectDir The directory of the project
     * @throws Exception File related exception
     */
    void loadRepository(String projectDir) throws Exception;

}
