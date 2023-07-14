package dev.kukode.services.dir;

import dev.kukode.beans.KuflexRepo;

import java.util.List;

/**
 * Any operation that deals with directories and files are to be handled by this interface
 */
public interface IDirService {
    /**
     * Get files path from the dir and it's sub-dirs
     * @param dir The root directory
     * @return List of directories to each file in the dir and sub-dirs
     */
    List<String> getFiles(String dir);

    /**
     * Create an empty repository with no branch and commit
     * @param dir Directory of project
     * @param kuflexRepo KuFlexRepo model for creating repository file
     * @return true if successful
     * @throws Exception Read/Write exception
     */
    boolean createRepoDir(String dir, KuflexRepo kuflexRepo) throws Exception;

    /**
     * Create default branch and commit directory and commitDB.json file, doesn't create other files such as projectSnapshots etc.
     * @param projectDir directory of the project
     * @return true if successful
     */
    boolean createDefaultBranchNCommitDir(String projectDir);
}
