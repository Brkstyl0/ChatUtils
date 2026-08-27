package com.chatutils;

import com.chatutils.commands.*;
import com.chatutils.config.ConfigManager;
import com.chatutils.data.PunishmentManager;
import com.chatutils.disguise.DisguiseManager;
import com.chatutils.listeners.ChatListener;
import com.chatutils.listeners.LoginListener;
import com.chatutils.listeners.VanishListener;
import com.chatutils.vanish.VanishManager;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class ChatUtils extends JavaPlugin {

    private static ChatUtils instance;
    private ConfigManager configManager;
    private PunishmentManager punishmentManager;
    private VanishManager vanishManager;
    private DisguiseManager disguiseManager;
    private com.chatutils.hook.VoiceChatManager voiceChatManager;

    @Override
    public void onEnable() {
        instance = this;

        try {
            // 1. Yapılandırma ve veri yöneticilerini başlat
            this.configManager = new ConfigManager(this);
            this.punishmentManager = new PunishmentManager(this);
            this.vanishManager = new VanishManager(this);
            this.disguiseManager = new DisguiseManager(this);

            // 2. Komutları kaydet
            registerCommands();

            // 3. Olay dinleyicilerini (Listeners) kaydet
            registerListeners();

            // 4. Simple Voice Chat entegrasyonunu başlat
            this.voiceChatManager = new com.chatutils.hook.VoiceChatManager(this);
            this.voiceChatManager.init();

            // 5. Konsola bilgilendirme mesajı gönder
            Bukkit.getConsoleSender().sendMessage("§a[ChatUtils] Eklenti aktif edildi.");
        } catch (Throwable t) {
            getLogger().severe("ChatUtils baslatilirken bir hata olustu: " + t.getMessage());
            t.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        // 1. Bekleyen asenkron görevleri iptal et
        Bukkit.getScheduler().cancelTasks(this);

        // 2. Dinleyicileri (Listeners) kaldır
        org.bukkit.event.HandlerList.unregisterAll(this);

        // 3. Vanish, Disguise ve VoiceChat temizlikleri
        if (vanishManager != null) {
            vanishManager.shutdown();
        }
        if (disguiseManager != null) {
            disguiseManager.shutdown();
        }
        if (voiceChatManager != null) {
            voiceChatManager.shutdown();
        }

        // 4. Verileri diske kaydet
        if (punishmentManager != null) {
            punishmentManager.saveData();
        }

        Bukkit.getConsoleSender().sendMessage("§c[ChatUtils] Eklenti devre disi birakildi.");
        instance = null;
    }

    private void registerCommands() {
        registerCommand("mute", new MuteCommand(this));
        registerCommand("unmute", new UnmuteCommand(this));
        registerCommand("voicemute", new VoiceMuteCommand(this));
        registerCommand("unvoicemute", new UnvoicemuteCommand(this));
        registerCommand("ban", new BanCommand(this));
        registerCommand("unban", new UnbanCommand(this));
        registerCommand("kick", new KickCommand(this));
        registerCommand("vanish", new VanishCommand(this));
        registerCommand("disguise", new DisguiseCommand(this));
        registerCommand("undisguise", new UndisguiseCommand(this));
        registerCommand("chat", new ChatCommand(this));
        registerCommand("clearchat", new ClearChatCommand(this));
        registerCommand("duyuru", new BroadcastCommand(this));
        registerCommand("chatutils", new ChatUtilsCommand(this));

        // Simple Voice Chat Yetkili & Şifresiz Grup Yönetim Komutları
        registerCommand("vcjoin", new VoiceChatJoinCommand(this));
        registerCommand("vcleave", new VoiceChatLeaveCommand(this));
        registerCommand("vcspy", new VoiceChatSpyCommand(this));
        registerCommand("vclist", new VoiceChatListCommand(this));
        registerCommand("vcmove", new VoiceChatMoveCommand(this));
        registerCommand("vcinfo", new VoiceChatInfoCommand(this));
    }

    private void registerCommand(String name, Object executor) {
        PluginCommand cmd = getCommand(name);
        if (cmd != null) {
            if (executor instanceof org.bukkit.command.CommandExecutor) {
                cmd.setExecutor((org.bukkit.command.CommandExecutor) executor);
            }
            if (executor instanceof org.bukkit.command.TabCompleter) {
                cmd.setTabCompleter((org.bukkit.command.TabCompleter) executor);
            }
        } else {
            // Dinamik CommandMap kaydı (Reload veya PlugMan yüklemeleri için)
            try {
                java.lang.reflect.Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
                commandMapField.setAccessible(true);
                org.bukkit.command.CommandMap commandMap = (org.bukkit.command.CommandMap) commandMapField
                        .get(Bukkit.getServer());

                java.lang.reflect.Constructor<PluginCommand> constructor = PluginCommand.class
                        .getDeclaredConstructor(String.class, org.bukkit.plugin.Plugin.class);
                constructor.setAccessible(true);
                PluginCommand dynamicCmd = constructor.newInstance(name, this);
                if (executor instanceof org.bukkit.command.CommandExecutor) {
                    dynamicCmd.setExecutor((org.bukkit.command.CommandExecutor) executor);
                }
                if (executor instanceof org.bukkit.command.TabCompleter) {
                    dynamicCmd.setTabCompleter((org.bukkit.command.TabCompleter) executor);
                }
                commandMap.register(getDescription().getName().toLowerCase(), dynamicCmd);
            } catch (Throwable ignored) {
            }
        }
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new ChatListener(this), this);
        Bukkit.getPluginManager().registerEvents(new LoginListener(this), this);
        Bukkit.getPluginManager().registerEvents(new VanishListener(this), this);
        Bukkit.getPluginManager().registerEvents(new com.chatutils.listeners.DisguiseListener(this), this);
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

    public VanishManager getVanishManager() {
        return vanishManager;
    }

    public DisguiseManager getDisguiseManager() {
        return disguiseManager;
    }

    public com.chatutils.hook.VoiceChatManager getVoiceChatManager() {
        return voiceChatManager;
    }
}
