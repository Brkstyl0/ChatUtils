package com.chatutils.listeners;

import com.chatutils.ChatUtils;
import org.bukkit.Material;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class VanishListener implements Listener {

    private final ChatUtils plugin;

    public VanishListener(ChatUtils plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Orijinal profil verilerini kaydet
        plugin.getDisguiseManager().onPlayerJoin(player);

        // Eğer oyuncu vanish durumundaysa giriş mesajını tamamen engelle
        if (plugin.getVanishManager().isVanished(player)) {
            event.joinMessage(null);
        }

        // Yeni giren oyuncuya mevcut görünmezleri gizle
        plugin.getVanishManager().onPlayerJoin(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Eğer oyuncu vanish durumundaysa çıkış mesajını tamamen engelle
        if (plugin.getVanishManager().isVanished(player)) {
            event.quitMessage(null);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPickupItem(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (plugin.getVanishManager().isVanished(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAttemptPickup(PlayerAttemptPickupItemEvent event) {
        if (plugin.getVanishManager().isVanished(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMobTarget(EntityTargetLivingEntityEvent event) {
        if (event.getTarget() instanceof Player) {
            Player target = (Player) event.getTarget();
            if (plugin.getVanishManager().isVanished(target)) {
                event.setCancelled(true);
                event.setTarget(null);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGeneralTarget(EntityTargetEvent event) {
        if (event.getTarget() instanceof Player) {
            Player target = (Player) event.getTarget();
            if (plugin.getVanishManager().isVanished(target)) {
                event.setCancelled(true);
                event.setTarget(null);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getVanishManager().isVanished(player)) {
            return;
        }

        // Basınç plakaları, tuzak ipleri ve tarım arazilerinin bozulmasını engelle
        if (event.getAction() == Action.PHYSICAL) {
            event.setCancelled(true);
            return;
        }

        // Sessiz sandık & konteyner açma (Animasyon ve ses olmadan açar)
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            if (!player.isSneaking() && plugin.getConfigManager().getConfig().getBoolean("vanish.silent-containers", true)) {
                if (event.getClickedBlock().getState() instanceof Container) {
                    Container container = (Container) event.getClickedBlock().getState();
                    event.setCancelled(true);
                    player.openInventory(container.getInventory());
                } else if (event.getClickedBlock().getType() == Material.ENDER_CHEST) {
                    event.setCancelled(true);
                    player.openInventory(player.getEnderChest());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (plugin.getVanishManager().isVanished(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAttackWhileVanished(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player damager = (Player) event.getDamager();
            if (plugin.getVanishManager().isVanished(damager)) {
                event.setCancelled(true);
                damager.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("vanish-cannot-damage"));
            }
        }
    }
}
