/*
 * Copyright (C) 24/07/23, 10:38 am KUKODE - Kuchuk Boram Debbarma . - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package dev.kukode.models.commits;

import lombok.Getter;

import java.util.Date;
import java.util.UUID;

@Getter

public class CommitModel {
    String UID; //UID of the commit
    String name; //Name of the commit
    String comment; //Comment of the commit
    Date creationDate; //Creation date
    String branchID; //The branch this commit is for
    String inheritedCommit; // The commit which this commit comes from
    String inheritedBranch; // The branch this commit was inherited from.
    // Will be null unless it's the first commit of the branch,
    // in which case the value will be the branchID of the branch the new branch was created from.

    public CommitModel(String name, String comment, Date creationDate, String branchID, String inheritedCommit, String inheritedBranch) {
        this.inheritedBranch = inheritedBranch;
        this.creationDate = creationDate;
        this.name = name;
        this.comment = comment;
        this.branchID = branchID;
        this.inheritedCommit = inheritedCommit;
        UUID uid = UUID.randomUUID();
        this.UID = uid.toString();
    }
}
