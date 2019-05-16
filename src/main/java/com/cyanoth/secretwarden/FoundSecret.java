package com.cyanoth.secretwarden;

import javax.annotation.Nullable;

/**
 *  Details about a single found secret. Assume that not all properties will be available.
 *  Intentionally immutable after initialization.
 */
public class FoundSecret {

    private String matchedRuleName;
    private String destinationFilePath = null;
    private String sourceContext = null;
    private int sourceLine = -1;
    private int destLine = -1;

    FoundSecret(String matchedRuleName) {
        this.matchedRuleName = matchedRuleName;
    }

    FoundSecret(String matchedRuleName, String destinationFilePath, int sourceLine, int destLine) {
        this.matchedRuleName = matchedRuleName;
        this.destinationFilePath = destinationFilePath;
        this.sourceLine = sourceLine;
        this.destLine = destLine;
    }

    FoundSecret(String matchedRuleName, String destinationFilePath, String sourceContext, int sourceLine, int destLine) {
        this.matchedRuleName = matchedRuleName;
        this.destinationFilePath = destinationFilePath;
        this.sourceContext = sourceContext;
        this.sourceLine = sourceLine;
        this.destLine = destLine;
    }


    public String getmatchedRuleName() {
        return matchedRuleName;
    }

    public int getSourceLine() {
        return sourceLine;
    }

    public int getDestLine() {
        return destLine;
    }

    @Nullable
    public String getDestinationFilePath() {
        return destinationFilePath;
    }

    @Nullable
    public String getSourceContext() {
        return sourceContext;
    }

    public String toString() {
        String builder = String.format("Matched Rule Name: %s", matchedRuleName);

        if (destinationFilePath != null)
            builder += String.format(" File Path: %s", destinationFilePath);

        if (sourceContext != null)
            builder += String.format(" Context: %s", sourceContext);

        if (sourceLine != 1 && destLine != 1)
            builder += String.format(" Between Lines: %d - %d", sourceLine, destLine);

        return builder;
    }

}
