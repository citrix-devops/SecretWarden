package com.cyanoth.secretwarden.structures;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

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
