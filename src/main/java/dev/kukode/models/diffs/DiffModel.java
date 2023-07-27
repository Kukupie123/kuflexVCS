/*
 * Copyright (C) 27/07/23, 7:28 am KUKODE - Kuchuk Boram Debbarma . - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package dev.kukode.models.diffs;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
public class DiffModel {
    String ID; //it will be a combination of branchID+commitID
    String diff;
    String commitID;
    String branchID;
    @Setter
    boolean isInitialDiff;

    public DiffModel(String diff, String commitID, String branchID) {
        this.diff = diff;
        this.commitID = commitID;
        this.branchID = branchID;
        this.ID = branchID + ".." + commitID;
        this.isInitialDiff = true;
    }
}
