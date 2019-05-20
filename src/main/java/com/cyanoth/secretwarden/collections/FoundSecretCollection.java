package com.cyanoth.secretwarden.collections;

import com.cyanoth.secretwarden.structures.FoundSecret;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

/**
 * Collection of FoundSecret - that is, where in the source code a match was found & what was matched.
 * Must implement Serializable to be cluster-safe & ability to replicate within scan results cache across the cluster
 */
public class FoundSecretCollection implements Serializable {

    private final Collection<FoundSecret> foundSecrets;

    public FoundSecretCollection() {
        foundSecrets = new HashSet<FoundSecret>();
    }

    public FoundSecretCollection(@NotNull HashSet<FoundSecret> secrets) {
        this.foundSecrets = secrets;
    }

    public void add(@NotNull FoundSecret secret) {
        foundSecrets.add(secret);
    }

    public void addAll(@NotNull Collection<FoundSecret> secrets){
        foundSecrets.addAll(secrets);
    }

    /**
     * Can be used to merge a smaller set of found secrets to a larger set
     * @param secrets Another collection of secrets to merge to THIS object
     */
    public void merge(@NotNull FoundSecretCollection secrets) {
        foundSecrets.addAll(secrets.getSecrets());
    }

    @NotNull
    public int count() {
        return foundSecrets.size();
    }

    @NotNull
    public Collection<FoundSecret> getSecrets() {
        return foundSecrets;
    }

}
