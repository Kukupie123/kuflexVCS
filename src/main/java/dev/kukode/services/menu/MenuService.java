/*
 * Copyright (C) 15/07/23, 7:18 pm KUKODE - Kuchuk Boram Debbarma . - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package dev.kukode.services.menu;

import dev.kukode.services.repo.RepoService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Date;
import java.util.Scanner;

@Service
public class MenuService {
    final
    RepoService repoService;

    public MenuService(RepoService repoService) {
        this.repoService = repoService;
    }

    public void ShowMainOptions(String path) throws Exception {
        if (!new File(path).isDirectory()) {
            System.out.println("Invalid Path argument : " + path);
            return;
        }

        Scanner scanner = new Scanner(System.in);
        //noinspection InfiniteLoopStatement
        while (true) {
            System.out.println("1. Initialize");
            int choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    System.out.println("Type the name of the project");
                    String projectName = scanner.next();
                    System.out.println("Type the name of the initial Branch");
                    Date createdDate = new Date();
                    repoService.initializeRepo(path, projectName, createdDate, "Random Creator");
                default:
                    break;
            }

        }
    }
}
