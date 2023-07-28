/*
 * Copyright (C) 28/07/23, 11:47 pm KUKODE - Kuchuk Boram Debbarma . - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */
package dev.kukode.services.diff;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;
import difflib.PatchFailedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiffService {

    public String getContentFromOriginalNDiff(String originalContent, String diffContent) throws PatchFailedException {
        System.out.println("-----------\n" + originalContent + "\n" + diffContent + "\n---------------");

        // Convert the original and diff content to a list of lines
        List<String> originalLines = originalContent.lines().toList();
        List<String> diffLines = diffContent.lines().toList();

        // Apply the patch to the original content to get the modified content
        Patch<String> patch = DiffUtils.parseUnifiedDiff(diffLines);
        List<String> modifiedContentLines = DiffUtils.patch(originalLines, patch);

        // Join the lines back into a single string
        var a = String.join("\n", modifiedContentLines);
        System.out.println(a);
        return a;
    }

    public String generateDiffFile(String originalContent, String modifiedContent) {
        // Convert the original and modified content to lists of lines
        List<String> originalLines = originalContent.lines().toList();
        List<String> modifiedLines = modifiedContent.lines().toList();

        // Generate the diff patch
        Patch<String> patch = DiffUtils.diff(originalLines, modifiedLines);

        // Save the unified diff to the file
        StringBuilder diffString = new StringBuilder();
        for (Delta<String> delta : patch.getDeltas()) {
            diffString.append(delta.toString());
            diffString.append("\n");
        }
        return diffString.toString();
    }
}