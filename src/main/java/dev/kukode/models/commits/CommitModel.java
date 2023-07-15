/*
 * Copyright (C) 16/07/23, 1:18 am KUKODE - Kuchuk Boram Debbarma . - All Rights Reserved
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

    public CommitModel(String name, String comment, Date creationDate, String branchID, String inheritedCommit) {
        this.creationDate = creationDate;
        this.name = name;
        this.comment = comment;
        this.branchID = branchID;
        this.inheritedCommit = inheritedCommit;
        UUID uid = UUID.randomUUID();
        this.UID = uid.toString();
    }
}
