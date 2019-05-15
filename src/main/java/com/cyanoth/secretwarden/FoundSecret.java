package com.cyanoth.secretwarden;

import javax.annotation.Nullable;

/**
 *  Details about a single found secret. Assume that not all properties will be available.
 *  Intentionally immutable after initialization.
 */
public class FoundSecret {

    private String matchedExpression = null;
    private String destinationFilePath = null;
    private String sourceContext = null;
    private int sourceLine = -1;
    private int destLine = -1;

    FoundSecret(String matchedExpression) {
        this.matchedExpression = matchedExpression;
    }

    FoundSecret(String matchedExpression, String destinationFilePath, int sourceLine, int destLine) {
        this.matchedExpression = matchedExpression;
        this.destinationFilePath = destinationFilePath;
        this.sourceLine = sourceLine;
        this.destLine = destLine;
    }

    FoundSecret(String matchedExpression, String destinationFilePath, String sourceContext, int sourceLine, int destLine) {
        this.matchedExpression = matchedExpression;
        this.destinationFilePath = destinationFilePath;
        this.sourceContext = sourceContext;
        this.sourceLine = sourceLine;
        this.destLine = destLine;
    }


    public String getMatchedExpression() {
        return matchedExpression;
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
        String builder = String.format("Matched Expression: %s", matchedExpression);

        if (destinationFilePath != null)
            builder += String.format(" File Path: %s", destinationFilePath);

        if (sourceContext != null)
            builder += String.format(" Context: %s", sourceContext);

        if (sourceLine != 1 && destLine != 1)
            builder += String.format(" Between Lines: %d - %d", sourceLine, destLine);

        return builder;
    }

}
