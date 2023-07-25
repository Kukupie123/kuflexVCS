/*
 * Copyright (C) 26/07/23, 12:51 am KUKODE - Kuchuk Boram Debbarma . - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package dev.kukode.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class KuflexRepoModel {

    public KuflexRepoModel(String projectName, String creatorName, Date creationDate) {
        this.projectName = projectName;
        this.creatorName = creatorName;
        this.creationDate = creationDate;
    }

    String projectName;
    String creatorName;
    Date creationDate;

    String initialBranch; //The branch that was initially created
    String initialCommit; //The commit that was initially created for the initialBranch
    String activeBranch; //The currently active branch
    String activeCommit; //The currently active commit we are working with
}
