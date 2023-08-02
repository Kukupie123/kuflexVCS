/*
 * Copyright (C) 02/08/23, 6:34 am KUKODE - Kuchuk Boram Debbarma . - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */
package dev.kukode.services.diff;

import com.github.difflib.patch.PatchFailedException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DiffService {
    private List<String> findLongestCommonSubsequence(List<String> originalLines, List<String> modifiedLines) {
        int m = originalLines.size();
        int n = modifiedLines.size();

        int[][] dp = new int[m + 1][n + 1];

        for (int i = 0; i <= m; i++) {
            for (int j = 0; j <= n; j++) {
                if (i == 0 || j == 0) {
                    dp[i][j] = 0;
                } else if (originalLines.get(i - 1).equals(modifiedLines.get(j - 1))) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }

        List<String> commonLines = new ArrayList<>();
        int i = m, j = n;
        while (i > 0 && j > 0) {
            if (originalLines.get(i - 1).equals(modifiedLines.get(j - 1))) {
                commonLines.add(0, originalLines.get(i - 1));
                i--;
                j--;
            } else if (dp[i - 1][j] > dp[i][j - 1]) {
                i--;
            } else {
                j--;
            }
        }

        return commonLines;
    }

    private List<String> applyPatch(List<String> originalLines, List<String> diffLines) throws PatchFailedException {
        List<String> modifiedLines = new ArrayList<>();
        int originalIndex = 0;

        for (String line : diffLines) {
            if (line.startsWith("-")) {
                // Original content deletion, skip the line in the original content
                originalIndex++;
            } else if (line.startsWith("+")) {
                // Revised content addition, add the line to the modified content
                modifiedLines.add(line.substring(1));
            } else if (line.startsWith(" ")) {
                // Unchanged line, add the line from original content to the modified content
                modifiedLines.add(originalLines.get(originalIndex));
                originalIndex++;
            }
        }

        return modifiedLines;
    }


    public String getContentFromOriginalNDiff(String originalContent, String diffData) throws PatchFailedException {
        // Convert the original content to a list of lines
        List<String> originalLines = originalContent.lines().toList();

        // Parse the diff data and apply the patch to the original content to get the modified content
        List<String> diffLines = diffData.lines().toList();
        List<String> modifiedContentLines = applyPatch(originalLines, diffLines);

        // Join the lines back into a single string
        return String.join("\n", modifiedContentLines).trim();
    }


    public String generateDiffData(String originalContent, String modifiedContent) {
        List<String> originalLines = originalContent.lines().toList();
        List<String> modifiedLines = modifiedContent.lines().toList();

        List<String> diffLines = new ArrayList<>();
        List<String> commonLines = findLongestCommonSubsequence(originalLines, modifiedLines);

        int originalIndex = 0;
        int modifiedIndex = 0;

        for (String commonLine : commonLines) {
            while (!originalLines.get(originalIndex).equals(commonLine)) {
                diffLines.add("- " + originalLines.get(originalIndex));
                originalIndex++;
            }

            while (!modifiedLines.get(modifiedIndex).equals(commonLine)) {
                diffLines.add("+ " + modifiedLines.get(modifiedIndex));
                modifiedIndex++;
            }

            diffLines.add("  " + commonLine);
            originalIndex++;
            modifiedIndex++;
        }

        while (originalIndex < originalLines.size()) {
            diffLines.add("- " + originalLines.get(originalIndex));
            originalIndex++;
        }

        while (modifiedIndex < modifiedLines.size()) {
            diffLines.add("+ " + modifiedLines.get(modifiedIndex));
            modifiedIndex++;
        }

        return String.join("\n", diffLines);
    }


}