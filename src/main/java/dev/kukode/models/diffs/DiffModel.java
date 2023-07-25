/*
 * Copyright (C) 25/07/23, 10:28 pm KUKODE - Kuchuk Boram Debbarma . - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package dev.kukode.models.diffs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
@AllArgsConstructor
public class DiffModel {
    String index; //Will be 0 for initial Commit and branch after that, it will be a combination of branchID+commitID
    String diff;
    String commitID;
    String branchID;

    String prevCommitID; //Parent of the commit from whom this commit diff extended
    String prevBranchID;

    List<String> parentCommitAndBranchID; //Children Commits will be stored here
}