package com.chatutils.vanish;

import com.chatutils.ChatUtils;
import com.chatutils.utils.ColorUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class VanishManager {

    private final ChatUtils plugin;
    private final Set<UUID> vanishedPlayers = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Boolean> previousFlightState = new ConcurrentHashMap<>();
    private BukkitTask actionBarTask;

    public VanishManager(ChatUtils plugin) {
        this.plugin = plugin;
        startActionBarTask();
    }

    public void setVanished(Player player, boolean vanish, boolean silent) {
        if (player == null || !player.isOnline()) return;

        UUID uuid = player.getUniqueId();
        if (vanish) {
            vanishedPlayers.add(uuid);
            previousFlightState.put(uuid, player.getAllowFlight());

            // Tüm oyunculardan (görme yetkisi olmayanlardan) gizle
            for (Player other : Bukkit.getOnlinePlayers()) {
                if (!other.equals(player) && !other.hasPermission("chatutils.vanish.see")) {
                    other.hidePlayer(plugin, player);
                }
            }

            // Uçuş izni ver
            player.setAllowFlight(true);

            // Fake Çıkış Duyurusu
            if (!silent && plugin.getConfigManager().getConfig().getBoolean("vanish.fake-announcements", true)) {
                String fakeQuitMsg = plugin.getConfigManager().getMessage("fake-quit", Map.of("player", player.getName()));
                if (fakeQuitMsg != null && !fakeQuitMsg.isEmpty()) {
                    for (Player other : Bukkit.getOnlinePlayers()) {
                        if (!other.hasPermission("chatutils.vanish.see")) {
                            other.sendMessage(fakeQuitMsg);
                        }
                    }
                }
            }

            player.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("vanish-enabled"));
        } else {
            vanishedPlayers.remove(uuid);

            // Tüm oyunculara tekrar görünür yap
            for (Player other : Bukkit.getOnlinePlayers()) {
                other.showPlayer(plugin, player);
            }

            // Uçuşu sıfırla (eğer vanish öncesinde uçuş izni yoksa kapat)
            boolean wasAllowedFlightBefore = Boolean.TRUE.equals(previousFlightState.remove(uuid));
            if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
                if (!wasAllowedFlightBefore) {
                    player.setFlying(false);
                    player.setAllowFlight(false);
                }
            }

            // Fake Giriş Duyurusu
            if (!silent && plugin.getConfigManager().getConfig().getBoolean("vanish.fake-announcements", true)) {
                String fakeJoinMsg = plugin.getConfigManager().getMessage("fake-join", Map.of("player", player.getName()));
                if (fakeJoinMsg != null && !fakeJoinMsg.isEmpty()) {
                    for (Player other : Bukkit.getOnlinePlayers()) {
                        if (!other.hasPermission("chatutils.vanish.see")) {
                            other.sendMessage(fakeJoinMsg);
                        }
                    }
                }
            }

            player.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("vanish-disabled"));
        }
    }

    public void toggleVanish(Player player) {
        setVanished(player, !isVanished(player), false);
    }

    public boolean isVanished(Player player) {
        return player != null && isVanished(player.getUniqueId());
    }

    public boolean isVanished(UUID uuid) {
        return uuid != null && vanishedPlayers.contains(uuid);
    }

    public Set<UUID> getVanishedPlayers() {
        return Collections.unmodifiableSet(vanishedPlayers);
    }

    /**
     * Yeni giriş yapan oyuncuya mevcut görünmez yetkilileri gizler.
     */
    public void onPlayerJoin(Player joiner) {
        boolean canSee = joiner.hasPermission("chatutils.vanish.see");
        for (UUID vUuid : vanishedPlayers) {
            Player vPlayer = Bukkit.getPlayer(vUuid);
            if (vPlayer != null && vPlayer.isOnline() && !vPlayer.equals(joiner)) {
                if (!canSee) {
                    joiner.hidePlayer(plugin, vPlayer);
                } else {
                    joiner.showPlayer(plugin, vPlayer);
                }
            }
        }
    }

    private void startActionBarTask() {
        if (actionBarTask != null) {
            actionBarTask.cancel();
        }

        actionBarTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!plugin.getConfigManager().getConfig().getBoolean("vanish.actionbar-hud", true)) {
                return;
            }

            String hudText = plugin.getConfigManager().getMessage("vanish-actionbar");
            if (hudText == null || hudText.isEmpty()) return;

            Component actionBarComponent = Component.text(ColorUtil.colorize(hudText));

            for (UUID uuid : vanishedPlayers) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null && player.isOnline()) {
                    player.sendActionBar(actionBarComponent);
                }
            }
        }, 20L, 40L); // Her 2 saniyede bir
    }

    public void shutdown() {
        if (actionBarTask != null) {
            actionBarTask.cancel();
            actionBarTask = null;
        }

        for (UUID uuid : vanishedPlayers) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                for (Player other : Bukkit.getOnlinePlayers()) {
                    other.showPlayer(plugin, player);
                }
                boolean wasAllowedFlightBefore = Boolean.TRUE.equals(previousFlightState.remove(uuid));
                if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
                    if (!wasAllowedFlightBefore) {
                        player.setFlying(false);
                        player.setAllowFlight(false);
                    }
                }
            }
        }
        vanishedPlayers.clear();
        previousFlightState.clear();
    }
}
