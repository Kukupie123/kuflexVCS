/*
 * Copyright (C) 27/07/23, 7:21 am KUKODE - Kuchuk Boram Debbarma . - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package dev.kukode;

import dev.kukode.confiigs.AppConfig;
import dev.kukode.services.menu.MenuService;
import dev.kukode.util.ConstantNames;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {

    public static void main(String[] args) {
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);
        ConstantNames.ProjectPath = args[0];
        System.out.println(ConstantNames.ProjectPath);
        MenuService menuService = applicationContext.getBean(MenuService.class);

        try {
            menuService.ShowMainOptions(args[0]);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}