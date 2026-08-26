package com.chatutils.disguise;

import com.chatutils.ChatUtils;
import com.chatutils.hook.LuckPermsHook;
import com.chatutils.utils.ColorUtil;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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

        // 1. Orijinal skin dokularını kaydet (Daha önce disguise olunmamışsa)
        Set<ProfileProperty> originalProperties = new HashSet<>();
        if (isDisguised(player)) {
            originalProperties.addAll(activeDisguises.get(uuid).getOriginalProperties());
        } else {
            PlayerProfile current = player.getPlayerProfile();
            if (current != null && current.getProperties() != null) {
                for (ProfileProperty prop : current.getProperties()) {
                    originalProperties.add(new ProfileProperty(prop.getName(), prop.getValue(), prop.getSignature()));
                }
            }
        }

        // 2. Rank Prefix & Renk belirle (Yetkiler DEĞİŞMEZ, sadece görsel TAB ve Chat rengi/prefix'i alınır)
        String prefix = null;
        if (targetRank != null && !targetRank.trim().isEmpty()) {
            prefix = LuckPermsHook.getGroupPrefix(targetRank);
            if (prefix == null) {
                prefix = plugin.getConfigManager().getConfig().getString("disguise.custom-ranks." + targetRank.toLowerCase());
            }
        }

        final String finalPrefix = prefix;

        // 3. Mojang'dan asenkron skin çekme
        PlayerProfile fetchProfile = Bukkit.createProfile(cleanTargetName);

        fetchProfile.update().thenAcceptAsync(fetched -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) {
                    if (callback != null) callback.accept(false);
                    return;
                }

                try {
                    PlayerProfile playerProfile = player.getPlayerProfile();
                    playerProfile.clearProperties();
                    if (fetched != null && fetched.getProperties() != null) {
                        for (ProfileProperty prop : fetched.getProperties()) {
                            playerProfile.setProperty(new ProfileProperty(prop.getName(), prop.getValue(), prop.getSignature()));
                        }
                    }
                    playerProfile.setName(cleanTargetName);
                    player.setPlayerProfile(playerProfile);
                } catch (Throwable t) {
                    // Fallback
                }

                String tabFormatted = (finalPrefix != null && !finalPrefix.isEmpty() ? finalPrefix : "") + cleanTargetName;
                String coloredTab = ColorUtil.colorize(tabFormatted);

                player.setDisplayName(cleanTargetName);
                player.setPlayerListName(coloredTab);
                player.customName(Component.text(cleanTargetName));

                DisguiseData data = new DisguiseData(
                        uuid,
                        player.getName(),
                        originalProperties,
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
                        PlayerProfile playerProfile = player.getPlayerProfile();
                        playerProfile.setName(cleanTargetName);
                        player.setPlayerProfile(playerProfile);
                    } catch (Throwable t) {
                    }

                    String tabFormatted = (finalPrefix != null && !finalPrefix.isEmpty() ? finalPrefix : "") + cleanTargetName;
                    String coloredTab = ColorUtil.colorize(tabFormatted);

                    player.setDisplayName(cleanTargetName);
                    player.setPlayerListName(coloredTab);
                    player.customName(Component.text(cleanTargetName));

                    DisguiseData data = new DisguiseData(
                            uuid,
                            player.getName(),
                            originalProperties,
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
                PlayerProfile playerProfile = player.getPlayerProfile();
                playerProfile.clearProperties();
                for (ProfileProperty prop : data.getOriginalProperties()) {
                    playerProfile.setProperty(new ProfileProperty(prop.getName(), prop.getValue(), prop.getSignature()));
                }
                playerProfile.setName(player.getName());
                player.setPlayerProfile(playerProfile);
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
                if (plugin.getVanishManager().isVanished(player) && !other.hasPermission("chatutils.vanish.see")) {
                    continue;
                }
                other.hidePlayer(plugin, player);
                other.showPlayer(plugin, player);
            }
        }

        // Kendi istemcisinde de modeli yenile
        if (!plugin.getVanishManager().isVanished(player)) {
            player.hidePlayer(plugin, player);
            player.showPlayer(plugin, player);
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
