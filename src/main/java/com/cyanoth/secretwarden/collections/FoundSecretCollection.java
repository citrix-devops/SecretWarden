package com.cyanoth.secretwarden.collections;

import com.cyanoth.secretwarden.structures.FoundSecret;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

/**
 * Collection of FoundSecret objects - that is, the location & what secrets have been found in the source code.
 * This must implement Serializable to be cluster-safe & have the ability to replicate the scan results cache across the cluster
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
     * @param secrets Another collection of secrets to merge into >THIS< object
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
