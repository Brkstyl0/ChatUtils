package com.chatutils.hook;

import com.chatutils.ChatUtils;
import com.chatutils.data.Punishment;
import com.chatutils.utils.TimeUtil;
import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import de.maxhenkel.voicechat.api.ServerPlayer;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServiceRegisterEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VoiceChatHook implements VoicechatPlugin, Listener {

    private final ChatUtils plugin;
    private final Map<UUID, Long> lastNotificationMap = new ConcurrentHashMap<>();
    private static boolean registered = false;

    public VoiceChatHook(ChatUtils plugin) {
        this.plugin = plugin;
    }

    public static void register(ChatUtils plugin) {
        if (registered) return;
        try {
            BukkitVoicechatService service = Bukkit.getServer().getServicesManager().load(BukkitVoicechatService.class);
            VoiceChatHook hook = new VoiceChatHook(plugin);
            if (service != null) {
                service.registerPlugin(hook);
                registered = true;
                Bukkit.getConsoleSender().sendMessage("§a[ChatUtils] Simple Voice Chat entegrasyonu basariyla aktif edildi.");
            } else {
                Bukkit.getPluginManager().registerEvents(hook, plugin);
            }
        } catch (Throwable ignored) {
        }
    }

    @EventHandler
    public void onServiceRegister(ServiceRegisterEvent event) {
        if (registered) return;
        try {
            if (event.getProvider().getService().equals(BukkitVoicechatService.class)) {
                BukkitVoicechatService service = (BukkitVoicechatService) event.getProvider().getProvider();
                if (service != null) {
                    service.registerPlugin(this);
                    registered = true;
                    Bukkit.getConsoleSender().sendMessage("§a[ChatUtils] Simple Voice Chat servisi tespit edildi ve entegre edildi.");
                }
            }
        } catch (Throwable ignored) {
        }
    }

    @Override
    public String getPluginId() {
        return "chatutils";
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophonePacket);
    }

    private void onMicrophonePacket(MicrophonePacketEvent event) {
        VoicechatConnection connection = event.getSenderConnection();
        if (connection == null) {
            return;
        }

        ServerPlayer serverPlayer = connection.getPlayer();
        if (serverPlayer == null) {
            return;
        }

        UUID uuid = serverPlayer.getUuid();
        Punishment voiceMute = null;

        if (uuid != null) {
            voiceMute = plugin.getPunishmentManager().getVoiceMute(uuid);
        }

        if (voiceMute == null && serverPlayer.getPlayer() instanceof Player) {
            Player bukkitPlayer = (Player) serverPlayer.getPlayer();
            voiceMute = plugin.getPunishmentManager().getVoiceMute(bukkitPlayer.getName());
        }

        if (voiceMute != null) {
            event.cancel();

            // Oyuncuya cok sik uyari gitmesini engellemek icin 6 saniyelik bekleme suresi
            if (uuid != null) {
                long now = System.currentTimeMillis();
                Long lastNotify = lastNotificationMap.get(uuid);
                if (lastNotify == null || (now - lastNotify) > 6000L) {
                    lastNotificationMap.put(uuid, now);

                    if (serverPlayer.getPlayer() instanceof Player) {
                        Player bukkitPlayer = (Player) serverPlayer.getPlayer();
                        String remaining = TimeUtil.formatRemaining(voiceMute.getEndTimestamp());
                        Map<String, String> pl = Map.of(
                                "remaining", remaining,
                                "reason", voiceMute.getReason(),
                                "staff", voiceMute.getStaffName()
                        );
                        List<String> notifyLines = plugin.getConfigManager().getMessageList("voicemuted-attempt", pl);
                        for (String line : notifyLines) {
                            bukkitPlayer.sendMessage(line);
                        }
                    }
                }
            }
        }
    }
}
