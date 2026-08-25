package com.chatutils.commands;

import com.chatutils.ChatUtils;
import com.chatutils.utils.ColorUtil;
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

public class BroadcastCommand implements CommandExecutor, TabCompleter {

    private final ChatUtils plugin;

    public BroadcastCommand(ChatUtils plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("chatutils.broadcast")) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMessage("broadcast-usage"));
            return true;
        }

        StringBuilder sb = new StringBuilder();
        for (String arg : args) {
            sb.append(arg).append(" ");
        }
        String rawMessage = sb.toString().trim();
        String coloredMessage = ColorUtil.colorize(rawMessage);

        Map<String, String> placeholders = Map.of(
                "staff", sender.getName(),
                "message", coloredMessage
        );

        List<String> broadcastLines = plugin.getConfigManager().getMessageList("broadcast-format", placeholders);
        for (String line : broadcastLines) {
            Bukkit.broadcastMessage(line);
        }

        // Ses ve Title efektleri
        String soundName = plugin.getConfigManager().getConfig().getString("broadcast.sound", "BLOCK_NOTE_BLOCK_PLING");
        float soundVolume = (float) plugin.getConfigManager().getConfig().getDouble("broadcast.sound-volume", 1.0);
        float soundPitch = (float) plugin.getConfigManager().getConfig().getDouble("broadcast.sound-pitch", 1.2);

        boolean showTitle = plugin.getConfigManager().getConfig().getBoolean("broadcast.show-title", true);
        String titleTop = ColorUtil.colorize(plugin.getConfigManager().getConfig().getString("broadcast.title-top", "&6&l[DUYURU]"));
        String titleBottom = ColorUtil.colorize(plugin.getConfigManager().getConfig().getString("broadcast.title-bottom", "&eSohbete bakınız!"));
        int fadeIn = plugin.getConfigManager().getConfig().getInt("broadcast.title-fade-in", 10);
        int stay = plugin.getConfigManager().getConfig().getInt("broadcast.title-stay", 60);
        int fadeOut = plugin.getConfigManager().getConfig().getInt("broadcast.title-fade-out", 10);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (soundName != null && !soundName.equalsIgnoreCase("NONE")) {
                try {
                    player.playSound(player.getLocation(), Sound.valueOf(soundName), soundVolume, soundPitch);
                } catch (Exception ignored) {}
            }

            if (showTitle) {
                try {
                    player.sendTitle(titleTop, titleBottom, fadeIn, stay, fadeOut);
                } catch (Exception ignored) {}
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}
