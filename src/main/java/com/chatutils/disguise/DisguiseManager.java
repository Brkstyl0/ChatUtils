package com.chatutils.disguise;

import com.chatutils.ChatUtils;
import com.chatutils.hook.LuckPermsHook;
import com.destroystokyo.paper.profile.PlayerProfile;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class DisguiseManager {

    private final ChatUtils plugin;
    private final Map<UUID, DisguiseData> activeDisguises = new ConcurrentHashMap<>();

    public DisguiseManager(ChatUtils plugin) {
        this.plugin = plugin;
    }

    public void disguise(Player player, String targetName, String targetRank, Consumer<Boolean> callback) {
        if (player == null || !player.isOnline() || targetName == null || targetName.trim().isEmpty()) {
            if (callback != null) callback.accept(false);
            return;
        }

        final String cleanTargetName = targetName.trim();
        final UUID uuid = player.getUniqueId();

        // Orijinal profil kaydedilmemişse kopyala
        PlayerProfile originalProfile = isDisguised(player) ?
                activeDisguises.get(uuid).getOriginalProfile() :
                player.getPlayerProfile().clone();

        // Rank prefix'ini belirle (LuckPerms üzerinden veya fallback)
        String prefix = null;
        if (targetRank != null && !targetRank.trim().isEmpty()) {
            prefix = LuckPermsHook.getGroupPrefix(targetRank);
            if (prefix == null) {
                prefix = plugin.getConfigManager().getConfig().getString("disguise.custom-ranks." + targetRank.toLowerCase());
            }
        }

        final String finalPrefix = prefix;

        // Paper Profile API ile asenkron skin ve profil çek
        PlayerProfile newProfile = Bukkit.createProfile(cleanTargetName);

        newProfile.update().thenAcceptAsync(updatedProfile -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) {
                    if (callback != null) callback.accept(false);
                    return;
                }

                try {
                    updatedProfile.setName(cleanTargetName);
                    player.setPlayerProfile(updatedProfile);
                } catch (Throwable t) {
                    player.setDisplayName(cleanTargetName);
                }

                player.setDisplayName(cleanTargetName);
                player.setPlayerListName(cleanTargetName);
                player.customName(Component.text(cleanTargetName));

                DisguiseData data = new DisguiseData(
                        uuid,
                        player.getName(),
                        originalProfile,
                        cleanTargetName,
                        targetRank,
                        finalPrefix
                );
                activeDisguises.put(uuid, data);

                refreshPlayer(player);

                if (callback != null) callback.accept(true);
            });
        }).exceptionally(ex -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (player.isOnline()) {
                    try {
                        newProfile.setName(cleanTargetName);
                        player.setPlayerProfile(newProfile);
                    } catch (Throwable t) {
                        player.setDisplayName(cleanTargetName);
                    }

                    player.setDisplayName(cleanTargetName);
                    player.setPlayerListName(cleanTargetName);
                    player.customName(Component.text(cleanTargetName));

                    DisguiseData data = new DisguiseData(
                            uuid,
                            player.getName(),
                            originalProfile,
                            cleanTargetName,
                            targetRank,
                            finalPrefix
                    );
                    activeDisguises.put(uuid, data);

                    refreshPlayer(player);
                }
                if (callback != null) callback.accept(true);
            });
            return null;
        });
    }

    public boolean undisguise(Player player) {
        if (player == null || !player.isOnline()) return false;

        DisguiseData data = activeDisguises.remove(player.getUniqueId());
        if (data != null) {
            try {
                player.setPlayerProfile(data.getOriginalProfile());
            } catch (Throwable t) {
                // Fallback
            }

            player.setDisplayName(player.getName());
            player.setPlayerListName(player.getName());
            player.customName(Component.text(player.getName()));

            refreshPlayer(player);
            return true;
        }
        return false;
    }

    public void refreshPlayer(Player player) {
        if (player == null || !player.isOnline()) return;

        for (Player other : Bukkit.getOnlinePlayers()) {
            if (!other.equals(player)) {
                // Eğer oyuncu vanished ise görme yetkisi olmayanlara tekrar gösterme
                if (plugin.getVanishManager().isVanished(player) && !other.hasPermission("chatutils.vanish.see")) {
                    continue;
                }
                other.hidePlayer(plugin, player);
                other.showPlayer(plugin, player);
            }
        }
    }

    public boolean isDisguised(Player player) {
        return player != null && isDisguised(player.getUniqueId());
    }

    public boolean isDisguised(UUID uuid) {
        return uuid != null && activeDisguises.containsKey(uuid);
    }

    public DisguiseData getDisguise(Player player) {
        return player != null ? getDisguise(player.getUniqueId()) : null;
    }

    public DisguiseData getDisguise(UUID uuid) {
        return uuid != null ? activeDisguises.get(uuid) : null;
    }

    public String getDisguisedName(Player player) {
        DisguiseData data = getDisguise(player);
        return data != null ? data.getDisguisedName() : player.getName();
    }

    public String getDisguisedPrefix(Player player) {
        DisguiseData data = getDisguise(player);
        return data != null ? data.getDisguisedPrefix() : null;
    }

    public void shutdown() {
        for (UUID uuid : activeDisguises.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                undisguise(player);
            }
        }
        activeDisguises.clear();
    }
}
