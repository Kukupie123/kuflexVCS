/*
 * Copyright (C) 15/07/23, 4:33 am KUKODE - Kuchuk Boram Debbarma . - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package dev.kukode.beans;

import java.util.Date;
import java.util.List;

public class KuflexRepoModel {

    public KuflexRepoModel(String projectName, String creatorName, Date creationDate) {
        this.projectName = projectName;
        this.creatorName = creatorName;
        this.creationDate = creationDate;
    }

    public String projectName;
    public String creatorName;
    public Date creationDate;
    public List<String> files; //Files that are part of the repository
    public String activeBranch; //The currently active branch
    public String activeCommit; //The currently active commit we are working with
}
