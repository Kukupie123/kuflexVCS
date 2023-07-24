/*
 * Copyright (C) 25/07/23, 12:18 am KUKODE - Kuchuk Boram Debbarma . - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package dev.kukode.models.snapshots;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class SnapshotModel {
    String id; //Combination of branchID+commitID
    public List<String> files;

}
