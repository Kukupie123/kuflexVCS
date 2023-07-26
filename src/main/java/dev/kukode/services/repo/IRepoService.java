/*
 * Copyright (C) 26/07/23, 7:14 am KUKODE - Kuchuk Boram Debbarma . - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package dev.kukode.services.repo;

import java.util.Date;


public interface IRepoService {

    /**
     * Initialize the repository with the respective folders and files.
     * Creates an initial Branch and Commit as well
     *
     * @param projectDir   Root Directory of the project
     * @param projectName  Name of the project
     * @param creationDate Date of creation
     * @param creator      Name of Creator
     * @throws Exception File-Write exception
     */
    void initializeRepo(String projectDir, String projectName, Date creationDate, String creator) throws Exception;

    /**
     * Creates a new commit with the currently active branch and commit as it's parent
     *
     * @param projectDir Root Directory of the project
     * @param commitName Name of the commit
     * @param comment    Comment for the commit
     * @throws Exception File related exception
     */
    void createNewCommit(String projectDir, String commitName, String comment) throws Exception;

    /**
     * Creates a new Branch from another Branch & Commit
     *
     * @param projectDir      Root directory of the project
     * @param branchName      Name of the branch
     * @param branchComment   Comment for the branch
     * @param inheritedBranch The BranchID the new branch is inheriting from
     * @param inheritedCommit The CommitID the new branch is inheriting from
     * @return True if successful
     */
    boolean createNewBranch(String projectDir, String branchName, String branchComment, String inheritedBranch, String inheritedCommit) throws Exception;

    /**
     * Loads a commit.
     *
     * @param projectDir Root directory of the project
     * @param commitID   ID of the commit
     * @param branchID   BranchID of the commit
     */
    void loadCommit(String projectDir, String commitID, String branchID) throws Exception;
}
