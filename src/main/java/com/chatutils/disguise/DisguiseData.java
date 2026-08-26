package com.chatutils.disguise;

import com.destroystokyo.paper.profile.ProfileProperty;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DisguiseData {

    private final UUID playerUuid;
    private final String originalName;
    private final Set<ProfileProperty> originalProperties;
    private final String disguisedName;
    private final String disguisedRank;
    private final String disguisedPrefix;
    private final long disguisedTimestamp;

    public DisguiseData(UUID playerUuid, String originalName, Set<ProfileProperty> originalProperties, String disguisedName, String disguisedRank, String disguisedPrefix) {
        this.playerUuid = playerUuid;
        this.originalName = originalName;
        this.originalProperties = originalProperties != null ? Collections.unmodifiableSet(new HashSet<>(originalProperties)) : Collections.emptySet();
        this.disguisedName = disguisedName;
        this.disguisedRank = disguisedRank;
        this.disguisedPrefix = disguisedPrefix;
        this.disguisedTimestamp = System.currentTimeMillis();
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public String getOriginalName() {
        return originalName;
    }

    public Set<ProfileProperty> getOriginalProperties() {
        return originalProperties;
    }

    public String getDisguisedName() {
        return disguisedName;
    }

    public String getDisguisedRank() {
        return disguisedRank;
    }

    public String getDisguisedPrefix() {
        return disguisedPrefix;
    }

    public long getDisguisedTimestamp() {
        return disguisedTimestamp;
    }
}
