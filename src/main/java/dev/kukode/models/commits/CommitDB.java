/*
 * Copyright (C) 25/07/23, 10:28 pm KUKODE - Kuchuk Boram Debbarma . - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package dev.kukode.models.commits;


import java.util.ArrayList;
import java.util.List;

public class CommitDB {
    public List<CommitModel> commits;

    public CommitDB() {
        commits = new ArrayList<>();
    }
}
