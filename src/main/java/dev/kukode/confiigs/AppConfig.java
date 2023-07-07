package dev.kukode.confiigs;

import org.apache.logging.log4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
//Scan these packages for components
@ComponentScan("dev.kukode.services")
@ComponentScan("dev.kukode.beans")
public class AppConfig {
}
