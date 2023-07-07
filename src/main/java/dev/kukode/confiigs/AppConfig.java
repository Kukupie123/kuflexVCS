package dev.kukode.confiigs;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
//Scan these packages for components
@ComponentScan("dev.kukode.services.menu")
public class AppConfig {
}
