/*
 * Copyright (C) 25/07/23, 11:41 pm KUKODE - Kuchuk Boram Debbarma . - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package dev.kukode.models.diffs;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
//Each file will have its own DiffDB, and the name will be encoded version of the path to the file
public class DiffDB {
    List<DiffModel> diffModels;

    public DiffDB() {
        diffModels = new ArrayList<>();
    }
}
