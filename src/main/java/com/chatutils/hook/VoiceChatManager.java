package com.chatutils.hook;

import com.chatutils.ChatUtils;
import org.bukkit.Bukkit;

public class VoiceChatManager {

    private final ChatUtils plugin;
    private boolean active = false;

    public VoiceChatManager(ChatUtils plugin) {
        this.plugin = plugin;
    }

    public void init() {
        try {
            // Simple Voice Chat API siniflarinin sunucuda varligini guvenle kontrol et
            Class.forName("de.maxhenkel.voicechat.api.BukkitVoicechatService");
            Class.forName("de.maxhenkel.voicechat.api.VoicechatPlugin");

            // Siniflar varsa dinamik olarak Hook'u baslat (Sinif dogrulama hatasini onler)
            Class<?> hookClass = Class.forName("com.chatutils.hook.VoiceChatHook");
            hookClass.getMethod("register", ChatUtils.class).invoke(null, plugin);
            this.active = true;
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            plugin.getLogger().info("Simple Voice Chat eklentisi bulunamadi, sesli sohbet susturma entegrasyonu pasif durumda.");
        } catch (Throwable t) {
            plugin.getLogger().warning("Simple Voice Chat baglantisi kurulurken bir sorun olustu: " + t.getMessage());
        }
    }

    public boolean isActive() {
        return active;
    }
}
