package dev.kukode.services.dir;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
public class DirService implements IDirService {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public List<String> getFiles(String dir) {
        /*
        RECURSIVELY access subdirectories
        1. Access the rootDir
        2. Get list of files in rootDir
        3. Iterate the list of files (call it f)
            3.1 If "f" is a file then add it to the list
            3.2 If "f" is a directory then add the return value of getFiles(f) to the list
         */
        List<String> filePaths = new ArrayList<>();

        //Access RootDir
        File file = new File(dir);

        //Iterate rootDir's subdirectories
        if (file.isDirectory()) {
            File[] dirFiles = file.listFiles();
            if (dirFiles == null) return filePaths;
            for (File dirFile : dirFiles) {
                if (dirFile.isFile()) {
                    filePaths.add(dirFile.getAbsolutePath());
                } else {
                    List<String> subDirFiles = getFiles(dirFile.getAbsolutePath());
                    filePaths.addAll(subDirFiles);
                }
            }
        }
        return filePaths;
    }

    @Override
    public boolean createRepoDir(String dir) {
        return false;
    }
}
