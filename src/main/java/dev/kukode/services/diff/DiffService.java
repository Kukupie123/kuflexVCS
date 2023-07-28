/*
 * Copyright (C) 29/07/23, 1:15 am KUKODE - Kuchuk Boram Debbarma . - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */
package dev.kukode.services.diff;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DiffService {
    private static Patch<String> parseUnifiedDiff(List<String> diffLines) {
        //TODO
        //So turns out the problem is the diff algorithms, we need to make our own file diff i guess
        List<AbstractDelta<String>> deltas = new ArrayList<>();
        List<String> originalChunk = new ArrayList<>();
        List<String> revisedChunk = new ArrayList<>();
        int orgStart = -1;
        int revStart = -1;
        boolean inOriginalChunk = false;
        boolean inRevisedChunk = false;

        for (String line : diffLines) {
            if (line.startsWith("@@")) {
                // Start of a new delta chunk
                if (inRevisedChunk) {
                    deltas.add(new ChangeDelta<>(new Chunk<>(orgStart, originalChunk), new Chunk<>(revStart, revisedChunk)));
                    originalChunk.clear();
                    revisedChunk.clear();
                }
                String[] chunks = line.split("[, ]");
                orgStart = Integer.parseInt(chunks[1].substring(1));
                int orgSize = Integer.parseInt(chunks[2]);
                revStart = Integer.parseInt(chunks[4].substring(1));
                int revSize = Integer.parseInt(chunks[5]);
                inOriginalChunk = inRevisedChunk = true;
            } else if (line.startsWith("-")) {
                // Original content deletion
                originalChunk.add(line.substring(1));
            } else if (line.startsWith("+")) {
                // Revised content addition
                revisedChunk.add(line.substring(1));
            } else if (line.startsWith(" ")) {
                // Unchanged line
                if (inOriginalChunk) {
                    originalChunk.add(line.substring(1));
                } else if (inRevisedChunk) {
                    revisedChunk.add(line.substring(1));
                }
            }
        }

        // Add the last delta chunk, if any
        if (inRevisedChunk) {
            deltas.add(new ChangeDelta<>(new Chunk<>(orgStart, originalChunk), new Chunk<>(revStart, revisedChunk)));
        }

        return new Patch<>();
    }

    public String getContentFromOriginalNDiff(String originalContent, String diffData) throws PatchFailedException {
        // Convert the original content to a list of lines
        List<String> originalLines = originalContent.lines().toList();

        // Parse the diff data and generate the patch
        List<String> diffLines = diffData.lines().toList();
        Patch<String> patch = parseUnifiedDiff(diffLines);

        // Apply the patch to the original content to get the modified content
        List<String> modifiedContentLines = DiffUtils.patch(originalLines, patch);

        // Join the lines back into a single string
        return String.join("\n", modifiedContentLines);
    }

    public String generateDiffData(String originalContent, String modifiedContent) {
        var originalToken = originalContent.lines().toList();
        var modifiedToken = modifiedContent.lines().toList();
        var patch = DiffUtils.diff(originalToken, modifiedToken);
        StringBuilder diffData = new StringBuilder();
        for (AbstractDelta<String> delta : patch.getDeltas()) {
            diffData.append(delta);
            diffData.append("\n");
        }
        System.out.println("Diff data : " + diffData);
        return diffData.toString();
    }
}