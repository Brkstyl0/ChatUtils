package com.chatutils;

import com.chatutils.commands.*;
import com.chatutils.config.ConfigManager;
import com.chatutils.data.PunishmentManager;
import com.chatutils.listeners.ChatListener;
import com.chatutils.listeners.LoginListener;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class ChatUtils extends JavaPlugin {

    private static ChatUtils instance;
    private ConfigManager configManager;
    private PunishmentManager punishmentManager;

    @Override
    public void onEnable() {
        instance = this;

        // 1. Yapılandırma ve veri yöneticilerini başlat
        this.configManager = new ConfigManager(this);
        this.punishmentManager = new PunishmentManager(this);

        // 2. Komutları kaydet
        registerCommands();

        // 3. Olay dinleyicilerini (Listeners) kaydet
        registerListeners();

        // 4. Konsola sade bilgilendirme mesajı gönder
        Bukkit.getConsoleSender().sendMessage("§a[ChatUtils] Eklenti aktif edildi.");
    }

    @Override
    public void onDisable() {
        // Verileri diske kaydet
        if (punishmentManager != null) {
            punishmentManager.saveData();
        }

        Bukkit.getConsoleSender().sendMessage("§c[ChatUtils] Eklenti devre disi birakildi.");
        instance = null;
    }

    private void registerCommands() {
        // /mute
        PluginCommand muteCmd = getCommand("mute");
        if (muteCmd != null) {
            MuteCommand muteExecutor = new MuteCommand(this);
            muteCmd.setExecutor(muteExecutor);
            muteCmd.setTabCompleter(muteExecutor);
        }

        // /unmute
        PluginCommand unmuteCmd = getCommand("unmute");
        if (unmuteCmd != null) {
            UnmuteCommand unmuteExecutor = new UnmuteCommand(this);
            unmuteCmd.setExecutor(unmuteExecutor);
            unmuteCmd.setTabCompleter(unmuteExecutor);
        }

        // /ban
        PluginCommand banCmd = getCommand("ban");
        if (banCmd != null) {
            BanCommand banExecutor = new BanCommand(this);
            banCmd.setExecutor(banExecutor);
            banCmd.setTabCompleter(banExecutor);
        }

        // /tempban
        PluginCommand tempbanCmd = getCommand("tempban");
        if (tempbanCmd != null) {
            TempbanCommand tempbanExecutor = new TempbanCommand(this);
            tempbanCmd.setExecutor(tempbanExecutor);
            tempbanCmd.setTabCompleter(tempbanExecutor);
        }

        // /unban
        PluginCommand unbanCmd = getCommand("unban");
        if (unbanCmd != null) {
            UnbanCommand unbanExecutor = new UnbanCommand(this);
            unbanCmd.setExecutor(unbanExecutor);
            unbanCmd.setTabCompleter(unbanExecutor);
        }

        // /chat
        PluginCommand chatCmd = getCommand("chat");
        if (chatCmd != null) {
            ChatCommand chatExecutor = new ChatCommand(this);
            chatCmd.setExecutor(chatExecutor);
            chatCmd.setTabCompleter(chatExecutor);
        }

        // /clearchat
        PluginCommand clearCmd = getCommand("clearchat");
        if (clearCmd != null) {
            ClearChatCommand clearExecutor = new ClearChatCommand(this);
            clearCmd.setExecutor(clearExecutor);
            clearCmd.setTabCompleter(clearExecutor);
        }

        // /duyuru
        PluginCommand broadcastCmd = getCommand("duyuru");
        if (broadcastCmd != null) {
            BroadcastCommand broadcastExecutor = new BroadcastCommand(this);
            broadcastCmd.setExecutor(broadcastExecutor);
            broadcastCmd.setTabCompleter(broadcastExecutor);
        }

        // /chatutils
        PluginCommand mainCmd = getCommand("chatutils");
        if (mainCmd != null) {
            ChatUtilsCommand mainExecutor = new ChatUtilsCommand(this);
            mainCmd.setExecutor(mainExecutor);
            mainCmd.setTabCompleter(mainExecutor);
        }
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new ChatListener(this), this);
        Bukkit.getPluginManager().registerEvents(new LoginListener(this), this);
    }

    public static ChatUtils getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public PunishmentManager getPunishmentManager() {
        return punishmentManager;
    }
}
