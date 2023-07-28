/*
 * Copyright (C) 28/07/23, 9:55 pm KUKODE - Kuchuk Boram Debbarma . - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */
package dev.kukode.services.diff;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DiffService {
    // Convert the list of deltas to unified diff format
    private static List<String> toUnifiedDiffFormat(List<Delta<String>> deltas) {
        List<String> unifiedDiffLines = new ArrayList<>();
        for (Delta<String> delta : deltas) {
            // Get the original chunk information
            List<String> originalChunk = delta.getOriginal().getLines();
            int orgStart = delta.getOriginal().getPosition();
            int orgEnd = orgStart + originalChunk.size() - 1;
            // Get the revised chunk information
            List<String> revisedChunk = delta.getRevised().getLines();
            int revStart = delta.getRevised().getPosition();
            int revEnd = revStart + revisedChunk.size() - 1;
            // Add the chunk header to the unified diff lines
            unifiedDiffLines.add("@@ -" + (orgStart + 1) + "," + originalChunk.size() +
                    " +" + (revStart + 1) + "," + revisedChunk.size() + " @@");
            // Add the original lines with a "-" prefix
            for (String line : originalChunk) {
                unifiedDiffLines.add("-" + line);
            }
            // Add the revised lines with a "+" prefix
            for (String line : revisedChunk) {
                unifiedDiffLines.add("+" + line);
            }
        }
        return unifiedDiffLines;
    }

    // Generate the diff between the original and current content
    public String generateFileDiff(String original, String current) {
        // Convert the current and original content to lists of lines
        List<String> currentContentToken = current.lines().toList();
        List<String> originalFileToken = original.lines().toList();
        // Generate the diff patch
        Patch<String> patch = DiffUtils.diff(originalFileToken, currentContentToken);
        // Convert the patch deltas to unified diff format
        List<String> diffLines = toUnifiedDiffFormat(patch.getDeltas());
        // Build the diff string
        StringBuilder diffString = new StringBuilder();
        for (String line : diffLines) {
            diffString.append(line);
            diffString.append("\n");
        }
        return String.valueOf(diffString);
    }

    public String getOriginalContentFromDiff(String diffString) {
        List<String> originalLines = new ArrayList<>();
        String[] lines = diffString.split("\n");
        for (String line : lines) {
            if (line.startsWith("-")) {
                originalLines.add(line.substring(1));
            }
        }
        return String.join("\n", originalLines);
    }
}