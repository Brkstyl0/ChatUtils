package com.chatutils.data;

import com.chatutils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PunishmentManager {

    private final ChatUtils plugin;
    private final Map<String, Punishment> activeMutes = new ConcurrentHashMap<>();
    private final Map<String, Punishment> activeBans = new ConcurrentHashMap<>();
    
    private boolean chatLocked = false;
    private File dataFile;
    private FileConfiguration dataConfig;

    public PunishmentManager(ChatUtils plugin) {
        this.plugin = plugin;
        loadData();
    }

    public void loadData() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        dataFile = new File(plugin.getDataFolder(), "punishments.yml");
        if (!dataFile.exists()) {
            try {
                if (dataFile.getParentFile() != null) {
                    dataFile.getParentFile().mkdirs();
                }
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("punishments.yml dosyasi olusturulamadi: " + e.getMessage());
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        activeMutes.clear();
        activeBans.clear();

        // Mutes yükle
        ConfigurationSection muteSec = dataConfig.getConfigurationSection("mutes");
        if (muteSec != null) {
            for (String key : muteSec.getKeys(false)) {
                Map<String, Object> values = muteSec.getConfigurationSection(key).getValues(false);
                Punishment p = Punishment.fromMap(values);
                if (!p.isExpired()) {
                    activeMutes.put(p.getTargetName().toLowerCase(Locale.ROOT), p);
                }
            }
        }

        // Bans yükle
        ConfigurationSection banSec = dataConfig.getConfigurationSection("bans");
        if (banSec != null) {
            for (String key : banSec.getKeys(false)) {
                Map<String, Object> values = banSec.getConfigurationSection(key).getValues(false);
                Punishment p = Punishment.fromMap(values);
                if (!p.isExpired()) {
                    activeBans.put(p.getTargetName().toLowerCase(Locale.ROOT), p);
                }
            }
        }

        this.chatLocked = dataConfig.getBoolean("chat-locked", false);
    }

    public synchronized void saveData() {
        if (dataConfig == null || dataFile == null) return;

        dataConfig.set("mutes", null);
        dataConfig.set("bans", null);
        dataConfig.set("chat-locked", chatLocked);

        for (Map.Entry<String, Punishment> entry : activeMutes.entrySet()) {
            if (!entry.getValue().isExpired()) {
                dataConfig.set("mutes." + entry.getKey(), entry.getValue().toMap());
            }
        }

        for (Map.Entry<String, Punishment> entry : activeBans.entrySet()) {
            if (!entry.getValue().isExpired()) {
                dataConfig.set("bans." + entry.getKey(), entry.getValue().toMap());
            }
        }

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("punishments.yml kaydedilirken hata olustu: " + e.getMessage());
        }
    }

    // --- MUTE METODLARI ---

    public void addMute(Punishment punishment) {
        String key = punishment.getTargetName().toLowerCase(Locale.ROOT);
        activeMutes.put(key, punishment);
        saveDataAsync();
    }

    public boolean removeMute(String targetName) {
        String key = targetName.toLowerCase(Locale.ROOT);
        if (activeMutes.remove(key) != null) {
            saveDataAsync();
            return true;
        }
        return false;
    }

    public Punishment getMute(String targetName) {
        String key = targetName.toLowerCase(Locale.ROOT);
        Punishment p = activeMutes.get(key);
        if (p != null) {
            if (p.isExpired()) {
                activeMutes.remove(key);
                saveDataAsync();
                return null;
            }
            return p;
        }
        return null;
    }

    public boolean isMuted(String targetName) {
        return getMute(targetName) != null;
    }

    public Set<String> getMutedPlayerNames() {
        cleanExpiredMutes();
        return new HashSet<>(activeMutes.keySet());
    }

    private void cleanExpiredMutes() {
        boolean changed = false;
        Iterator<Map.Entry<String, Punishment>> it = activeMutes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Punishment> entry = it.next();
            if (entry.getValue().isExpired()) {
                it.remove();
                changed = true;
            }
        }
        if (changed) saveDataAsync();
    }

    // --- BAN METODLARI ---

    public void addBan(Punishment punishment) {
        String key = punishment.getTargetName().toLowerCase(Locale.ROOT);
        activeBans.put(key, punishment);
        saveDataAsync();
    }

    public boolean removeBan(String targetName) {
        String key = targetName.toLowerCase(Locale.ROOT);
        if (activeBans.remove(key) != null) {
            saveDataAsync();
            return true;
        }
        return false;
    }

    public Punishment getBan(String targetName) {
        String key = targetName.toLowerCase(Locale.ROOT);
        Punishment p = activeBans.get(key);
        if (p != null) {
            if (p.isExpired()) {
                activeBans.remove(key);
                saveDataAsync();
                return null;
            }
            return p;
        }
        return null;
    }

    public boolean isBanned(String targetName) {
        return getBan(targetName) != null;
    }

    public Set<String> getBannedPlayerNames() {
        cleanExpiredBans();
        return new HashSet<>(activeBans.keySet());
    }

    private void cleanExpiredBans() {
        boolean changed = false;
        Iterator<Map.Entry<String, Punishment>> it = activeBans.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Punishment> entry = it.next();
            if (entry.getValue().isExpired()) {
                it.remove();
                changed = true;
            }
        }
        if (changed) saveDataAsync();
    }

    // --- SOHBET KİLİDİ METODLARI ---

    public boolean isChatLocked() {
        return chatLocked;
    }

    public void setChatLocked(boolean chatLocked) {
        this.chatLocked = chatLocked;
        saveDataAsync();
    }

    public void toggleChatLock() {
        this.chatLocked = !this.chatLocked;
        saveDataAsync();
    }

    private void saveDataAsync() {
        if (plugin.isEnabled()) {
            try {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, this::saveData);
                return;
            } catch (Throwable ignored) {}
        }
        saveData();
    }
}
