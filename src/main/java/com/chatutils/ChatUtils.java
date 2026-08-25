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

        try {
            // 1. Yapılandırma ve veri yöneticilerini başlat
            this.configManager = new ConfigManager(this);
            this.punishmentManager = new PunishmentManager(this);

            // 2. Komutları kaydet
            registerCommands();

            // 3. Olay dinleyicilerini (Listeners) kaydet
            registerListeners();

            // 4. Konsola bilgilendirme mesajı gönder
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

        // 3. Verileri diske kaydet
        if (punishmentManager != null) {
            punishmentManager.saveData();
        }

        Bukkit.getConsoleSender().sendMessage("§c[ChatUtils] Eklenti devre disi birakildi.");
        instance = null;
    }

    private void registerCommands() {
        registerCommand("mute", new MuteCommand(this));
        registerCommand("unmute", new UnmuteCommand(this));
        registerCommand("ban", new BanCommand(this));
        registerCommand("tempban", new TempbanCommand(this));
        registerCommand("unban", new UnbanCommand(this));
        registerCommand("chat", new ChatCommand(this));
        registerCommand("clearchat", new ClearChatCommand(this));
        registerCommand("duyuru", new BroadcastCommand(this));
        registerCommand("chatutils", new ChatUtilsCommand(this));
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
