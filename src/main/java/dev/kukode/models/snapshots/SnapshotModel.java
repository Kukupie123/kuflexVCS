/*
 * Copyright (C) 27/07/23, 7:42 am KUKODE - Kuchuk Boram Debbarma . - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package dev.kukode.models.snapshots;

import dev.kukode.util.ConstantNames;
import lombok.Getter;

import java.util.List;

@Getter
public class SnapshotModel {
    String id; //Combination of branchID+commitID
    List<String> files;

    public SnapshotModel(String branchID, String commitID, List<String> files) {
        this.id = ConstantNames.GET_UID_OF_SNAPSHOT(branchID, commitID);
        this.files = files;
    }
}
