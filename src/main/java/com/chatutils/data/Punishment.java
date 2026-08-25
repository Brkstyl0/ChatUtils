package com.chatutils.data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Punishment {

    private final UUID targetUuid;
    private final String targetName;
    private final String staffName;
    private final String reason;
    private final long startTimestamp;
    private final long endTimestamp; // -1 for permanent
    private final PunishmentType type;

    public Punishment(UUID targetUuid, String targetName, String staffName, String reason, long startTimestamp, long endTimestamp, PunishmentType type) {
        this.targetUuid = targetUuid;
        this.targetName = targetName;
        this.staffName = staffName != null ? staffName : "Konsol";
        this.reason = (reason != null && !reason.trim().isEmpty()) ? reason.trim() : "Sebep belirtilmedi";
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
        this.type = type;
    }

    public UUID getTargetUuid() {
        return targetUuid;
    }

    public String getTargetName() {
        return targetName;
    }

    public String getStaffName() {
        return staffName;
    }

    public String getReason() {
        return reason;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public long getEndTimestamp() {
        return endTimestamp;
    }

    public PunishmentType getType() {
        return type;
    }

    public boolean isPermanent() {
        return endTimestamp <= -1L;
    }

    public boolean isExpired() {
        if (isPermanent()) {
            return false;
        }
        return System.currentTimeMillis() >= endTimestamp;
    }

    public long getRemainingMillis() {
        if (isPermanent()) {
            return -1L;
        }
        long diff = endTimestamp - System.currentTimeMillis();
        return Math.max(0, diff);
    }

    public long getOriginalDurationMillis() {
        if (isPermanent()) {
            return -1L;
        }
        return Math.max(0, endTimestamp - startTimestamp);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        if (targetUuid != null) {
            map.put("targetUuid", targetUuid.toString());
        }
        map.put("targetName", targetName);
        map.put("staffName", staffName);
        map.put("reason", reason);
        map.put("startTimestamp", startTimestamp);
        map.put("endTimestamp", endTimestamp);
        map.put("type", type.name());
        return map;
    }

    public static Punishment fromMap(Map<?, ?> map) {
        UUID uuid = null;
        if (map.get("targetUuid") != null) {
            try {
                uuid = UUID.fromString(map.get("targetUuid").toString());
            } catch (Exception ignored) {}
        }
        String targetName = map.get("targetName") != null ? map.get("targetName").toString() : "Bilinmeyen";
        String staffName = map.get("staffName") != null ? map.get("staffName").toString() : "Konsol";
        String reason = map.get("reason") != null ? map.get("reason").toString() : "Sebep belirtilmedi";
        long start = map.get("startTimestamp") instanceof Number ? ((Number) map.get("startTimestamp")).longValue() : System.currentTimeMillis();
        long end = map.get("endTimestamp") instanceof Number ? ((Number) map.get("endTimestamp")).longValue() : -1L;
        PunishmentType type = PunishmentType.valueOf(map.get("type") != null ? map.get("type").toString() : "MUTE");

        return new Punishment(uuid, targetName, staffName, reason, start, end, type);
    }
}
