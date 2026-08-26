package com.chatutils.disguise;

import com.chatutils.ChatUtils;
import com.chatutils.hook.LuckPermsHook;
import com.chatutils.utils.ColorUtil;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class DisguiseManager {

    private final ChatUtils plugin;
    private final Map<UUID, DisguiseData> activeDisguises = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerProfile> originalProfiles = new ConcurrentHashMap<>();
    private final Map<UUID, String> originalDisplayNames = new ConcurrentHashMap<>();
    private final Map<UUID, String> originalListNames = new ConcurrentHashMap<>();

    public DisguiseManager(ChatUtils plugin) {
        this.plugin = plugin;
    }

    public void onPlayerJoin(Player player) {
        saveOriginalState(player);
    }

    private void saveOriginalState(Player player) {
        if (player == null) return;
        UUID uuid = player.getUniqueId();
        if (!originalProfiles.containsKey(uuid)) {
            PlayerProfile cleanOriginal = Bukkit.createProfile(uuid, player.getName());
            PlayerProfile current = player.getPlayerProfile();
            if (current != null && current.getProperties() != null) {
                for (ProfileProperty prop : current.getProperties()) {
                    cleanOriginal.setProperty(new ProfileProperty(prop.getName(), prop.getValue(), prop.getSignature()));
                }
            }
            originalProfiles.put(uuid, cleanOriginal);
            originalDisplayNames.put(uuid, player.getDisplayName() != null ? player.getDisplayName() : player.getName());
            originalListNames.put(uuid, player.getPlayerListName() != null ? player.getPlayerListName() : player.getName());
        }
    }

    public void disguise(Player player, String targetName, String targetRank, Consumer<Boolean> callback) {
        if (player == null || !player.isOnline() || targetName == null || targetName.trim().isEmpty()) {
            if (callback != null) callback.accept(false);
            return;
        }

        final String cleanTargetName = targetName.trim();
        final UUID uuid = player.getUniqueId();

        // 1. Orijinal profili ve isimleri hafızaya al
        saveOriginalState(player);

        // 2. Rank Prefix & Görünüm belirle (Yetkiler DEĞİŞMEZ, yalnızca TAB ve Chat formatı)
        String prefix = null;
        if (targetRank != null && !targetRank.trim().isEmpty()) {
            prefix = LuckPermsHook.getGroupPrefix(targetRank);
            if (prefix == null) {
                prefix = plugin.getConfigManager().getConfig().getString("disguise.custom-ranks." + targetRank.toLowerCase());
            }
        }

        final String finalPrefix = prefix;

        // 3. Hedef oyuncunun Mojang skinini asenkron çek
        PlayerProfile fetchProfile = Bukkit.createProfile(cleanTargetName);

        fetchProfile.update().thenAcceptAsync(fetched -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) {
                    if (callback != null) callback.accept(false);
                    return;
                }

                try {
                    // Oyuncunun UUID'si ile yeni hedef isimli profil oluştur
                    PlayerProfile newProfile = Bukkit.createProfile(uuid, cleanTargetName);
                    if (fetched != null && fetched.getProperties() != null) {
                        for (ProfileProperty prop : fetched.getProperties()) {
                            newProfile.setProperty(new ProfileProperty(prop.getName(), prop.getValue(), prop.getSignature()));
                        }
                    }
                    player.setPlayerProfile(newProfile);
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
                        PlayerProfile newProfile = Bukkit.createProfile(uuid, cleanTargetName);
                        player.setPlayerProfile(newProfile);
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

        UUID uuid = player.getUniqueId();
        DisguiseData data = activeDisguises.remove(uuid);
        if (data != null) {
            // 1. Orijinal profili ve skin dokularını geri yükle
            PlayerProfile originalProfile = originalProfiles.remove(uuid);
            if (originalProfile == null) {
                originalProfile = Bukkit.createProfile(uuid, player.getName());
                originalProfile.update();
            }

            try {
                player.setPlayerProfile(originalProfile);
            } catch (Throwable t) {
                // Fallback
            }

            // 2. Orijinal isim ve tab listesini geri yükle
            String origDisplay = originalDisplayNames.remove(uuid);
            if (origDisplay == null) origDisplay = player.getName();

            String origList = originalListNames.remove(uuid);
            if (origList == null) origList = player.getName();

            player.setDisplayName(origDisplay);
            player.setPlayerListName(origList);
            player.customName(null);

            // 3. Görsel güncelleme (1 tick gecikmeli hide/show)
            refreshPlayer(player);
            return true;
        }
        return false;
    }

    public void refreshPlayer(Player player) {
        if (player == null || !player.isOnline()) return;

        // 1. Diğer tüm oyunculardan gizle
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (!other.equals(player)) {
                other.hidePlayer(plugin, player);
            }
        }

        // 2. Kendi istemcisini güncelle
        Location loc = player.getLocation();
        player.teleport(loc);

        // 3. Paket ayrımını sağlamak için 1 tick sonra diğer oyunculara tekrar göster
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;

            for (Player other : Bukkit.getOnlinePlayers()) {
                if (!other.equals(player)) {
                    if (plugin.getVanishManager().isVanished(player) && !other.hasPermission("chatutils.vanish.see")) {
                        continue;
                    }
                    other.showPlayer(plugin, player);
                }
            }
        }, 1L);
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
        originalProfiles.clear();
        originalDisplayNames.clear();
        originalListNames.clear();
    }
}
