package dev.kukode.services.dir;

import dev.kukode.beans.KuflexRepo;

import java.util.List;

public interface IDirService {
    /**
     * Get files path from the dir and it's sub-dirs
     *
     * @param dir The root directory
     * @return List of directories to each file in the dir and sub-dirs
     */
    List<String> getFiles(String dir);

    boolean createRepoDir(String dir, KuflexRepo kuflexRepo) throws Exception;
}
