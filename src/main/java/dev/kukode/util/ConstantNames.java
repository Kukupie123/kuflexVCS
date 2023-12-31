/*
 * Copyright (C) 27/07/23, 7:42 am KUKODE - Kuchuk Boram Debbarma . - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package dev.kukode.util;

public class ConstantNames {
    public static final String KUFLEXREPOFILE = "KuFlexRepo.json";
    public static final String KUFLEX = ".kuflex";
    public static final String SNAPSHOTDBFile = "kuFlexSnapDB.json";
    public static String BranchesDBFILE = "branchesDB.json";
    public static String CommitsDBFile = "commitsDb.json";
    public static String DiffDir = "diffs";
    public static String ProjectPath = null;

    public static String GET_UID_OF_SNAPSHOT(String branchID, String commitID) {
        return branchID + ".." + commitID;
    }

    public static String GET_UID_OF_DIFFMODEL(String branchID, String commitID) {
        return GET_UID_OF_SNAPSHOT(branchID, commitID);
    }

    private ConstantNames() {
    }
}
