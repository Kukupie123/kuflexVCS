/*
 * Copyright (C) 03/08/23, 10:11 am KUKODE - Kuchuk Boram Debbarma . - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package DiffAlgorithmTest;

import com.github.difflib.patch.DiffException;
import dev.kukode.services.diff.DiffService;
import org.junit.Assert;
import org.junit.Test;

public class FileDiffTestClass {
    DiffService diffService = new DiffService();

    @Test
    public void integrationTestFileDiffONE() throws DiffException {
        String originalContent = "This is the initial Content";
        String firstChange = "This is the first change we made to the content";
        String secondChange = "This is the second change\nand now we are truly testing the chain";

        // Generate the diff data
        String diff1 = diffService.generateDiffData(originalContent, firstChange);
        String diff2 = diffService.generateDiffData(firstChange, secondChange);


        // Load diffs
        String firstChangeContent = diffService.getContentFromOriginalNDiff(originalContent, diff1);
        Assert.assertEquals(firstChange, firstChangeContent.trim());

        String secondChangeContent = diffService.getContentFromOriginalNDiff(firstChangeContent, diff2);
        Assert.assertEquals(secondChange, secondChangeContent.trim());
    }

}
