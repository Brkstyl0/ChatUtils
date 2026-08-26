package com.chatutils.disguise;

import java.util.UUID;

public class DisguiseData {

    private final UUID playerUuid;
    private final String originalName;
    private final String disguisedName;
    private final String disguisedRank;
    private final String disguisedPrefix;
    private final long disguisedTimestamp;

    public DisguiseData(UUID playerUuid, String originalName, String disguisedName, String disguisedRank, String disguisedPrefix) {
        this.playerUuid = playerUuid;
        this.originalName = originalName;
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
