package com.cyanoth.secretwarden;
import com.atlassian.bitbucket.content.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;


 // Stream the differences changes. For each added line (includes modified) check whether it matches a possible secret.
        // As strongly recommended by the documentation [1][2] intentionally implement the ABSTRACT version to prevent breakage from future API changes.
        // [1] https://docs.atlassian.com/bitbucket-server/javadoc/5.16.4/api/reference/com/atlassian/bitbucket/content/DiffContentCallback.html
    // [2] https://docs.atlassian.com/bitbucket-server/javadoc/5.16.4/api/reference/com/atlassian/bitbucket/content/ChangeCallback.html
    // not named count as further logic may be introduced

public class MatchSecretCollector extends AbstractDiffContentCallback {
    private static final Logger log = LoggerFactory.getLogger(MatchSecretCollector.class);

    private final Collection<String> matchedSecrets = new HashSet<String>();

    @Override
    public void onSegmentLine(@Nonnull String s, @Nullable ConflictMarker conflictMarker, boolean b) throws IOException {
        // Compare the pattern

    }

    Collection<String> getFoundSecrets() {
        return matchedSecrets;
    }
}
