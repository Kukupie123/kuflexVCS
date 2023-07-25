/*
 * Copyright (C) 26/07/23, 12:51 am KUKODE - Kuchuk Boram Debbarma . - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package dev.kukode.services.diff;


import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class DiffService {
    private static List<String> toUnifiedDiffFormat(List<Delta<String>> deltas, List<String> originalLines) {
        List<String> unifiedDiffLines = new ArrayList<>();

        for (Delta<String> delta : deltas) {
            List<String> originalChunk = delta.getOriginal().getLines();
            int orgStart = delta.getOriginal().getPosition();
            int orgEnd = orgStart + originalChunk.size() - 1;

            List<String> revisedChunk = delta.getRevised().getLines();
            int revStart = delta.getRevised().getPosition();
            int revEnd = revStart + revisedChunk.size() - 1;

            unifiedDiffLines.add("@@ -" + (orgStart + 1) + "," + originalChunk.size() +
                    " +" + (revStart + 1) + "," + revisedChunk.size() + " @@");

            for (String line : originalChunk) {
                unifiedDiffLines.add("-" + line);
            }

            for (String line : revisedChunk) {
                unifiedDiffLines.add("+" + line);
            }
        }
        return unifiedDiffLines;
    }

    private static void saveDiffToFile(List<String> diffLines, String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (String line : diffLines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String generateFileDiff(String original, String current) {
        List<String> currentContentToken = current.lines().toList();
        List<String> originalFileToken = original.lines().toList();
        Patch<String> patch = DiffUtils.diff(originalFileToken, currentContentToken);
        List<String> diffLines = toUnifiedDiffFormat(patch.getDeltas(), originalFileToken);
        StringBuilder diffString = new StringBuilder();
        for (String line : diffLines) {
            diffString.append(line);
            diffString.append("\n");
        }
        return String.valueOf(diffString);
    }
}
