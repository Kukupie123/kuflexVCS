package dev.kukode;

import dev.kukode.confiigs.AppConfig;
import dev.kukode.services.menu.MenuService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
    public static void main(String[] args) {
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);
        MenuService menuService = applicationContext.getBean(MenuService.class);

        try {
            menuService.ShowMainOptions(args[0]);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}