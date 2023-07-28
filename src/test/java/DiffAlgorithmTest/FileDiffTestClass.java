/*
 * Copyright (C) 29/07/23, 1:15 am KUKODE - Kuchuk Boram Debbarma . - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package DiffAlgorithmTest;

import com.github.difflib.patch.PatchFailedException;
import dev.kukode.services.diff.DiffService;
import org.junit.Assert;
import org.junit.Test;

public class FileDiffTestClass {
    DiffService diffService = new DiffService();

    @Test
    public void integrationTestFileDiffONE() throws PatchFailedException {
        String originalContent = "This is the initial Content";
        String firstChange = "This is the first change we made to the content";
        String secondChange = "Well to the first change we say, get out! we are new here";
        String thirdChange = "Ok I am tired now lol";

        //Save diffs
        String diff1 = diffService.generateDiffData(originalContent, firstChange);

        String diff2 = diffService.generateDiffData(firstChange, secondChange);
        String diff3 = diffService.generateDiffData(secondChange, thirdChange);

        //Load diffs
        //We get the original diff
        String firstChangeContent = diffService.getContentFromOriginalNDiff(originalContent, diff1); //Will return us firstChange
        String secondChangeContent = diffService.getContentFromOriginalNDiff(firstChangeContent, diff2);
        String thirdChangeContent = diffService.getContentFromOriginalNDiff(secondChangeContent, diff3);

        System.out.println("DIFFS==========================");
        System.out.println(diff1);
        System.out.println(diff2);
        System.out.println(diff3);

        System.out.println("RESULTS+++++++++++++++++++");
        System.out.println(firstChangeContent);
        System.out.println(secondChangeContent);
        System.out.println(thirdChangeContent);

        Assert.assertEquals(firstChange, firstChangeContent);
        Assert.assertEquals(secondChange, secondChangeContent);
        Assert.assertEquals(thirdChange, thirdChangeContent);


    }
}
