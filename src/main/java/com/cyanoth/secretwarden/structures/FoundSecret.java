package com.cyanoth.secretwarden.structures;

import javax.annotation.Nullable;
import java.io.Serializable;

/**
 *  Details about a single found secret. Intentionally becomes immutable (no setters) after initialization.
 */
public class FoundSecret implements Serializable {
    private String matchedRuleName;
    private String destinationFilePath = null;
    private String sourceContext = null;
    private int occurrenceLine = -1;

    FoundSecret(String matchedRuleName) {
        this.matchedRuleName = matchedRuleName;
    }

    FoundSecret(String matchedRuleName, String destinationFilePath, int occurrenceLine) {
        this.matchedRuleName = matchedRuleName;
        this.destinationFilePath = destinationFilePath;
        this.occurrenceLine = occurrenceLine;
    }

    public FoundSecret(String matchedRuleName, String destinationFilePath, String sourceContext, int occurrenceLine) {
        this.matchedRuleName = matchedRuleName;
        this.destinationFilePath = destinationFilePath;
        this.sourceContext = sourceContext;
        this.occurrenceLine = occurrenceLine;
    }

    public String getmatchedRuleName() {
        return matchedRuleName;
    }

    public int getOccurrenceLine() {
        return occurrenceLine;
    }

    @Nullable
    public String getDestinationFilePath() {
        return destinationFilePath;
    }

    @Nullable
    public String getSourceContext() {
        return sourceContext;
    }

    /**
     * @return Friendly string containing information that exists about this found secret
     */
    public String toString() {
        String builder = String.format("Matched Rule Name: %s", matchedRuleName);

        if (destinationFilePath != null)
            builder += String.format(" File Path: %s", destinationFilePath);

        if (sourceContext != null)
            builder += String.format(" Context: %s", sourceContext);

        if (occurrenceLine != -1) {
            builder += String.format(" Occurred on line: %d", occurrenceLine);
        }

        return builder;
    }

}
