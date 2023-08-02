/*
 * Copyright (C) 02/08/23, 10:09 am KUKODE - Kuchuk Boram Debbarma . - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package dev.kukode.models.branches;

import java.util.Date;
import java.util.UUID;

public class BranchModel {
    String UID; //UID of the commit

    String name; //Name of the branch

    Date creationDate; //Creation Date

    String interhitedBranch; //The branchID the branch the inherited commit belongs to

    String inheritedCommit; //The commitID of the commit this branch inherited from

    public BranchModel(String name, Date creationDate, String interhitedBranch, String inheritedCommit) {
        this.name = name;
        this.creationDate = creationDate;
        this.interhitedBranch = interhitedBranch;
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

    public Date getCreationDate() {
        return creationDate;
    }

    public String getInterhitedBranch() {
        return interhitedBranch;
    }

    public String getInheritedCommit() {
        return inheritedCommit;
    }
}
