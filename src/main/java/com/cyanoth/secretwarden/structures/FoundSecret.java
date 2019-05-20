package com.cyanoth.secretwarden.structures;

import javax.annotation.Nullable;
import java.io.Serializable;

/**
 *  Details about a single found secret. Intentionally immutable after initialization.
 */
public class FoundSecret implements Serializable {
    private String matchedRuleName;
    private String destinationFilePath = null;
    private String sourceContext = null;
    private int startSourceLine = -1;
    private int endSourceLine = -1;

    FoundSecret(String matchedRuleName) {
        this.matchedRuleName = matchedRuleName;
    }

    FoundSecret(String matchedRuleName, String destinationFilePath, int startSourceLine, int endSourceLine) {
        this.matchedRuleName = matchedRuleName;
        this.destinationFilePath = destinationFilePath;
        this.startSourceLine = startSourceLine;
        this.endSourceLine = endSourceLine;
    }

    public FoundSecret(String matchedRuleName, String destinationFilePath, String sourceContext, int startSourceLine, int endSourceLine) {
        this.matchedRuleName = matchedRuleName;
        this.destinationFilePath = destinationFilePath;
        this.sourceContext = sourceContext;
        this.startSourceLine = startSourceLine;
        this.endSourceLine = endSourceLine;
    }


    public String getmatchedRuleName() {
        return matchedRuleName;
    }

    public int getStartSourceLine() {
        return startSourceLine;
    }

    public int getEndSourceLine() {
        return endSourceLine;
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

        if (startSourceLine != 1 && endSourceLine != 1)
            builder += String.format(" Between Lines: %d - %d", startSourceLine, endSourceLine);

        return builder;
    }

}
