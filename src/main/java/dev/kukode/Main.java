package dev.kukode;

import dev.kukode.confiigs.AppConfig;
import dev.kukode.services.menu.MenuService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);
        MenuService menuService = applicationContext.getBean(MenuService.class);

        System.out.println("Kukode's Flex VSC");
        Scanner input = new Scanner(System.in);


        menuService.ShowMainOptions();
        input.nextInt();
    }
}