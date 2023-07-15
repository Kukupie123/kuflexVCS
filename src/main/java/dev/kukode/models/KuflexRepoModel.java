/*
 * Copyright (C) 15/07/23, 8:51 pm KUKODE - Kuchuk Boram Debbarma . - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package dev.kukode.models;

import lombok.Getter;

import java.util.Date;

@Getter
public class KuflexRepoModel {

    public KuflexRepoModel(String projectName, String creatorName, Date creationDate) {
        this.projectName = projectName;
        this.creatorName = creatorName;
        this.creationDate = creationDate;
    }

    public String projectName;
    public String creatorName;
    public Date creationDate;

    public String initialBranch; //The branch that was initially created
    public String initialCommit; //The commit that was initially created for the initialBranch
    public String activeBranch; //The currently active branch
    public String activeCommit; //The currently active commit we are working with
}
