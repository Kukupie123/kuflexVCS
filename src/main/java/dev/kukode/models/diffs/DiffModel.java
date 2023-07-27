/*
 * Copyright (C) 27/07/23, 7:00 am KUKODE - Kuchuk Boram Debbarma . - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package dev.kukode.models.diffs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class DiffModel {
    String ID; //it will be a combination of branchID+commitID
    String diff;
    String commitID;
    String branchID;
    boolean isInitialDiff;

}
