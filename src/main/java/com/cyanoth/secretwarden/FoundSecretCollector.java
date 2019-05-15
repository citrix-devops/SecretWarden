package com.cyanoth.secretwarden;

import com.atlassian.bitbucket.content.AbstractDiffContentCallback;
import com.atlassian.bitbucket.content.ConflictMarker;
import com.atlassian.bitbucket.content.DiffSegmentType;
import com.atlassian.bitbucket.content.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;

/**
 * Finds & collect secrets from a given hunk of changes.
 * As strongly recommended by the documentation [1] intentionally extend the ABSTRACT version to prevent breakage from future API changes.
 *
 *    Segment - A collection of one or more content lines sharing the same DiffSegmentType
 *    Hunk - A collection of segments, pinned to starting lines within the source and destination, representing contiguous lines within the files being compared
 *    Diff - A collection of hunks comprising the changes between a given source and destination
 *
 * [1] https://docs.atlassian.com/bitbucket-server/javadoc/5.16.4/api/reference/com/atlassian/bitbucket/content/DiffContentCallback.html
 *
 * Note, we only care about added (or changed) lines going into the PR. Where possible, skip checking deleted lines.
 *
 */
public class FoundSecretCollector extends AbstractDiffContentCallback {
    private static final Logger log = LoggerFactory.getLogger(FoundSecretCollector.class);
    private final Collection<FoundSecret> foundSecrets = new HashSet<>();

    private boolean flag_ScanSegment = false; // Consider the regex matching as intensive. So don't scan unless all pre-conditions pass

    // These properties are set during various stages of the callback, we must keep this information until onSegmentLine(...)
    // where if a secret is found, then these details are called-upon to create a FoundSecret details object
    // IMPROVEME: With this, the secret will be "somewhere in file /src/foo.java between source line 12 to 35". Better if this was an exact line number.
    private String destinationFilePath = null;
    private String sourceContext = null;
    private int sourceLine = -1;
    private int destinationLine = -1;


    @Override
    public void onDiffStart(@Nullable Path src, @Nullable Path dst) {
        // dst will be null if the file is being deleted. We're not interested in the deleted files, so precondition for scan false
        destinationFilePath = dst != null ? dst.toString() : null;
        flag_ScanSegment = dst != null;
    }

    @Override
    public void onHunkStart(int srcLine, int srcSpan, int dstLine, int dstSpan, @Nullable String context) {
        sourceLine = srcLine;
        destinationLine  = dstLine;
        sourceContext = context;
    }

    @Override
    public void onSegmentLine(@Nonnull String s, @Nullable ConflictMarker conflictMarker, boolean b) {
        if (!flag_ScanSegment || conflictMarker != null) // Ignore & don't scan this segment if preconditions for scan is false or in conflict.
            return;

        // TODO: Replace this test code with matching from loaded MatchSecretRuleSet
        // TODO: Make matching more efficent than just a contains.
        String matchExpression = "hello";

        if (s.contains(matchExpression)) {
            FoundSecret foundSecret = new FoundSecret(matchExpression, destinationFilePath, sourceContext, sourceLine, destinationLine);
            foundSecrets.add(foundSecret);
            log.warn(String.format("Possible secret identified: %s", foundSecret.toString())); // TODO: Change me to INFO level
        }
    }

    @Override
    public void onSegmentStart(@Nonnull DiffSegmentType type) {
        // Only care about added (changed) lines. Precondition for scan will be false on context/deleted lines.
        flag_ScanSegment = type == DiffSegmentType.ADDED;
    }

    Collection<FoundSecret> getFoundSecrets() {
        return foundSecrets;
    }
}
