/*
 * Copyright (C) 02/08/23, 10:09 am KUKODE - Kuchuk Boram Debbarma . - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package dev.kukode.models.commits;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class CommitModel {
    String UID; //UID of the commit
    String name; //Name of the commit
    String comment; //Comment of the commit
    Date creationDate; //Creation date
    String branchID; //The branch this commit is for
    String inheritedCommit; // The commit which this commit comes from
    String inheritedBranch; // The branchID the inherited commit belongs too
    // Will be null unless it's the first commit of the branch,
    // in which case the value will be the branchID of the branch the new branch was created from.
    List<String> childrenBranchCommit; //Children Commits will be stored here

    public CommitModel(String name, String comment, Date creationDate, String branchID, String inheritedCommit, String inheritedBranch, List<String> childrenBranchCommit) {
        this.childrenBranchCommit = childrenBranchCommit;
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
