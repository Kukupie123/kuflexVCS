/*
 * Copyright (C) 15/07/23, 7:15 pm KUKODE - Kuchuk Boram Debbarma . - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package dev.kukode.models.commits;

import java.util.Date;
import java.util.UUID;

public class CommitModel {
    String UID; //UID of the commit
    String name; //Name of the commit
    String comment; //Comment of the commit

    Date creationDate; //Creation date
    String inheritedBranch; //The branch this commit was made from
    String inheritedCommit; // The commit which this commit comes from

    public CommitModel(String name, String comment, Date creationDate, String inheritedBranch, String inheritedCommit) {
        this.creationDate = creationDate;
        this.name = name;
        this.comment = comment;
        this.inheritedBranch = inheritedBranch;
        this.inheritedCommit = inheritedCommit;
        UUID uid = UUID.randomUUID();
        this.UID = uid.toString();
    }

    public String getUID() {
        return UID;
    }

    public String getName() {
        return name;
    }

    public String getComment() {
        return comment;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public String getInheritedBranch() {
        return inheritedBranch;
    }

    public String getInheritedCommit() {
        return inheritedCommit;
    }
}
