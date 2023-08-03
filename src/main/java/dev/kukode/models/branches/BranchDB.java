/*
 * Copyright (C) 03/08/23, 10:11 am KUKODE - Kuchuk Boram Debbarma . - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package dev.kukode.models.branches;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class BranchDB {
    List<BranchModel> branches;

    public BranchDB() {
        branches = new ArrayList<>();
    }
}
