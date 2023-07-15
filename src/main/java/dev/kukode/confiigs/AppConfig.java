/*
 * Copyright (C) 15/07/23, 7:15 pm KUKODE - Kuchuk Boram Debbarma . - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package dev.kukode.confiigs;

import com.google.gson.Gson;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
//Scan these packages for components
@ComponentScan("dev.kukode.services")
@ComponentScan("dev.kukode.models")
public class AppConfig {

    @Bean
    public Gson gson() {
        return new Gson();
    }
}
