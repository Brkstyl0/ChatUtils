package com.chatutils.config;

import com.chatutils.ChatUtils;
import com.chatutils.utils.ColorUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConfigManager {

    private final ChatUtils plugin;
    private FileConfiguration config;
    private FileConfiguration messages;
    private File messagesFile;

    public ConfigManager(ChatUtils plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        // config.yml
        try {
            plugin.saveDefaultConfig();
            plugin.reloadConfig();
            this.config = plugin.getConfig();
        } catch (Exception e) {
            plugin.getLogger().warning("config.yml yuklenirken hata: " + e.getMessage());
        }

        // messages.yml
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            try {
                plugin.saveResource("messages.yml", false);
            } catch (Exception ignored) {}
        }
        this.messages = YamlConfiguration.loadConfiguration(messagesFile);

        // Eksik anahtarları default messages.yml'den tamamla
        try {
            InputStream defStream = plugin.getResource("messages.yml");
            if (defStream != null) {
                YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defStream, StandardCharsets.UTF_8));
                this.messages.setDefaults(defConfig);
            }
        } catch (Exception ignored) {}
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public FileConfiguration getMessages() {
        return messages;
    }

    public String getPrefix() {
        return ColorUtil.colorize(messages.getString("prefix", "&8[&6ChatUtils&8] &r"));
    }

    public String getMessage(String path) {
        return getMessage(path, null);
    }

    public String getMessage(String path, Map<String, String> placeholders) {
        String msg = messages.getString(path);
        if (msg == null) {
            return ColorUtil.colorize("&c[Mesaj Bulunamadı: " + path + "]");
        }

        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                msg = msg.replace("{" + entry.getKey() + "}", entry.getValue() != null ? entry.getValue() : "");
            }
        }
        return ColorUtil.colorize(msg);
    }

    public List<String> getMessageList(String path, Map<String, String> placeholders) {
        List<String> list = messages.getStringList(path);
        if (list.isEmpty()) {
            String single = messages.getString(path);
            if (single != null) {
                list = List.of(single);
            }
        }

        List<String> formatted = new ArrayList<>();
        for (String line : list) {
            if (placeholders != null) {
                for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                    line = line.replace("{" + entry.getKey() + "}", entry.getValue() != null ? entry.getValue() : "");
                }
            }
            formatted.add(ColorUtil.colorize(line));
        }
        return formatted;
    }

    public List<String> getSuggestedReasons(String type) {
        return config.getStringList("suggested-reasons." + type);
    }
}
