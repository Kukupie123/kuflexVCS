/*
 * Copyright (C) 02/08/23, 10:09 am KUKODE - Kuchuk Boram Debbarma . - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package dev.kukode.services.menu;

import dev.kukode.services.dirNFile.DirNFileService;
import dev.kukode.services.repo.RepoService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Date;
import java.util.Scanner;

@Service
public class MenuService {
    final RepoService repoService;
    final DirNFileService dirService;

    public MenuService(RepoService repoService, DirNFileService dirService) {
        this.repoService = repoService;
        this.dirService = dirService;
    }

    public void ShowMainOptions(String path) throws Exception {
        if (!new File(path).isDirectory()) {
            System.out.println("Invalid Path argument : " + path);
            return;
        }

        Scanner scanner = new Scanner(System.in);
        //noinspection InfiniteLoopStatement
        while (true) {
            System.out.println("1. Initialize\n2. Commit\n3. Load initial Commit \n4. Reload current commit\n5. Load commit by ID.");
            int choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    System.out.println("Type the name of the project");
                    String projectName = scanner.next();
                    Date createdDate = new Date();
                    repoService.initializeRepo(projectName, createdDate, "Random Creator");
                    break;
                case 2:
                    System.out.println("Name of commit");
                    String commitName = scanner.next();
                    System.out.println("Comment");
                    String comment = scanner.next();
                    repoService.createNewCommit(commitName, comment);
                    break;
                case 3:
                    var repo = dirService.getKuFlexRepoModel();
                    repoService.loadCommit(repo.getInitialCommit(), repo.getInitialBranch());
                    break;
                case 4:
                    var repo1 = dirService.getKuFlexRepoModel();
                    repoService.loadCommit(repo1.getActiveCommit(), repo1.getActiveBranch());
                    break;
                case 5:
                    var repo2 = dirService.getKuFlexRepoModel();
                    System.out.println("Type commit ID to load");
                    String commitID = scanner.next();
                    repoService.loadCommit(commitID, repo2.getActiveBranch());
                    break;
                default:
                    break;
            }

        }
    }
}
