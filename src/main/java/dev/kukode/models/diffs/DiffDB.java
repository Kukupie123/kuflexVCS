/*
 * Copyright (C) 25/07/23, 12:18 am KUKODE - Kuchuk Boram Debbarma . - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package dev.kukode.models.diffs;

import lombok.Getter;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;

import java.util.ArrayList;
import java.util.List;

@Getter
//Each file will have its own DiffDB, and the name will be encoded version of the path to the file
public class DiffDB {
    @Required
    String name; //Name of the file. This will be the encoded version of the path to the file
    List<DiffModel> diffModels;

    public DiffDB(String name) {
        this.name = name;
        diffModels = new ArrayList<>();
    }
}
