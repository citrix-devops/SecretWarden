package com.cyanoth.secretwarden.pullrequest.internal;

import com.atlassian.bitbucket.content.AbstractDiffContentCallback;
import com.atlassian.bitbucket.content.ConflictMarker;
import com.atlassian.bitbucket.content.DiffSegmentType;
import com.atlassian.bitbucket.content.Path;
import com.cyanoth.secretwarden.structures.FoundSecret;
import com.cyanoth.secretwarden.structures.FoundSecretCollection;
import com.cyanoth.secretwarden.structures.MatchRule;
import com.cyanoth.secretwarden.config.MatchRuleSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/**
 * INTERNAL: Finds & collect secrets that match rules in the ruleset in a given hunk of changes.
 *
 *    Segment - A collection of one or more content lines sharing the same DiffSegmentType
 *    Hunk - A collection of segments, pinned to starting lines within the source and destination, representing contiguous lines within the files being compared
 *    Diff - A collection of hunks comprising the changes between a given source and destination
 **
 * Only care about the lines added (changed) going into the destination repository. This means we can avoid wasting resources by
 * skipping the removed lines. It also allows for users to remove secrets from repositories without flagging it as a found secret.
 *
 * Callback Flow (only implementing ones we're interested in): onDiffStart(...) -> onHunkStart(...) -> onSegmentStart(...) -> onSegmentLine(...)
 *
 * [1] https://docs.atlassian.com/bitbucket-server/javadoc/5.16.4/api/reference/com/atlassian/bitbucket/content/DiffContentCallback.html
 */
class DiffMatcher extends AbstractDiffContentCallback {
    private final MatchRuleSet ruleSet;
    private final FoundSecretCollection foundSecrets = new FoundSecretCollection();

    private boolean flagScanSegment= false;

    // These properties are set during various stages of the callback flow and used for found secret information
    // We must keep this information incase of a matched secret at the end of the callback flow.
    private String destinationFilePath = null;
    private String sourceContext = null;
    private int sourceLine = -1;
    private int destinationLine = -1;

    DiffMatcher(MatchRuleSet ruleSet) {
        this.ruleSet = ruleSet;
    }

    @Override
    public void onDiffStart(@Nullable Path src, @Nullable Path dst) {
        // dst will be null if the file is being deleted. We're not interested in the deleted files at all, so flag false to not scan this change
        destinationFilePath = dst != null ? dst.toString() : null;
        flagScanSegment = dst != null;
    }

    @Override
    public void onHunkStart(int srcLine, int srcSpan, int dstLine, int dstSpan, @Nullable String context) {
        sourceLine = srcLine;
        destinationLine  = dstLine;
        sourceContext = context;
    }

    @Override
    public void onSegmentLine(@Nonnull String s, @Nullable ConflictMarker conflictMarker, boolean b) {
        if (!flagScanSegment || conflictMarker != null) // Don't scan this code segment if preconditions for a scan is false or in conflict.
            return;

        for (MatchRule rule : ruleSet.getRuleSet()) {
            if (rule.getRegexPattern().matcher(s).matches()) {
                foundSecrets.add(new FoundSecret(rule.getFriendlyName(), destinationFilePath,
                        sourceContext, sourceLine, destinationLine));
            }
        }
    }

    @Override
    public void onSegmentStart(@Nonnull DiffSegmentType type) {
        // Only care about added (changed) lines. Precondition for scan will be false on context/deleted lines.
        flagScanSegment = type == DiffSegmentType.ADDED;
    }

    FoundSecretCollection getFoundSecrets() {
        return foundSecrets;
    }
}
