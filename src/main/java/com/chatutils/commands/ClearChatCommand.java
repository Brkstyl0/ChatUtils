package com.chatutils.commands;

import com.chatutils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ClearChatCommand implements CommandExecutor, TabCompleter {

    private final ChatUtils plugin;

    public ClearChatCommand(ChatUtils plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("chatutils.chat.clear")) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        executeClear(plugin, sender);
        return true;
    }

    public static void executeClear(ChatUtils plugin, CommandSender sender) {
        int lines = plugin.getConfigManager().getConfig().getInt("clearchat.blank-lines", 120);
        String soundName = plugin.getConfigManager().getConfig().getString("clearchat.sound", "ENTITY_EXPERIENCE_ORB_PICKUP");

        // Modern Minecraft (Paper 26.2) istemcilerinde sohbeti yukarı kaydırmak için boşluklu satırlar
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (int i = 0; i < lines; i++) {
                player.sendMessage(" ");
            }
            if (soundName != null && !soundName.equalsIgnoreCase("NONE")) {
                try {
                    player.playSound(player.getLocation(), Sound.valueOf(soundName.toUpperCase()), 1.0f, 1.0f);
                } catch (Exception ignored) {}
            }
        }

        Map<String, String> placeholders = Map.of("staff", sender.getName());
        String msg = plugin.getConfigManager().getMessage("chat-cleared-broadcast", placeholders);
        Bukkit.broadcastMessage(msg);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}
