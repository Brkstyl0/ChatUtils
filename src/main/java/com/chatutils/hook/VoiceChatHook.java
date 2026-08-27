package com.chatutils.hook;

import com.chatutils.ChatUtils;
import com.chatutils.data.Punishment;
import com.chatutils.utils.TimeUtil;
import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.ServerPlayer;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.PlayerDisconnectedEvent;
import de.maxhenkel.voicechat.api.events.RemoveGroupEvent;
import de.maxhenkel.voicechat.api.packets.StaticSoundPacket;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServiceRegisterEvent;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VoiceChatHook implements VoicechatPlugin, Listener {

    private final ChatUtils plugin;
    private final Map<UUID, Long> lastNotificationMap = new ConcurrentHashMap<>();
    private static VoicechatServerApi serverApi;
    private static VoiceChatHook instance;
    private static boolean registered = false;

    public VoiceChatHook(ChatUtils plugin) {
        this.plugin = plugin;
        instance = this;
    }

    public static void register(ChatUtils plugin) {
        if (registered) return;
        try {
            BukkitVoicechatService service = Bukkit.getServer().getServicesManager().load(BukkitVoicechatService.class);
            VoiceChatHook hook = new VoiceChatHook(plugin);
            Bukkit.getPluginManager().registerEvents(hook, plugin);
            if (service != null) {
                service.registerPlugin(hook);
                registered = true;
                Bukkit.getConsoleSender().sendMessage("§a[ChatUtils] Simple Voice Chat entegrasyonu basariyla aktif edildi.");
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

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (plugin.getVoiceChatManager() != null) {
            plugin.getVoiceChatManager().stopSpying(event.getPlayer().getUniqueId());
        }
    }

    @Override
    public String getPluginId() {
        return "chatutils";
    }

    @Override
    public void initialize(VoicechatApi api) {
        if (api instanceof VoicechatServerApi) {
            serverApi = (VoicechatServerApi) api;
            Bukkit.getConsoleSender().sendMessage("§a[ChatUtils] Simple Voice Chat Server API basariyla tanimlandi.");
        }
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophonePacket);
        registration.registerEvent(RemoveGroupEvent.class, this::onRemoveGroup);
        registration.registerEvent(PlayerDisconnectedEvent.class, this::onPlayerDisconnected);
    }

    private void onMicrophonePacket(MicrophonePacketEvent event) {
        VoicechatConnection senderConnection = event.getSenderConnection();
        if (senderConnection == null) {
            return;
        }

        ServerPlayer serverPlayer = senderConnection.getPlayer();
        if (serverPlayer == null) {
            return;
        }

        UUID senderUuid = serverPlayer.getUuid();
        Punishment voiceMute = null;

        if (senderUuid != null) {
            voiceMute = plugin.getPunishmentManager().getVoiceMute(senderUuid);
        }

        if (voiceMute == null && serverPlayer.getPlayer() instanceof Player) {
            Player bukkitPlayer = (Player) serverPlayer.getPlayer();
            voiceMute = plugin.getPunishmentManager().getVoiceMute(bukkitPlayer.getName());
        }

        // 1. Susturma kontrolü (Voice Mute)
        if (voiceMute != null) {
            event.cancel();

            if (senderUuid != null) {
                long now = System.currentTimeMillis();
                Long lastNotify = lastNotificationMap.get(senderUuid);
                if (lastNotify == null || (now - lastNotify) > 6000L) {
                    lastNotificationMap.put(senderUuid, now);

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
            return;
        }

        if (serverApi == null) {
            return;
        }

        VoiceChatManager vcManager = plugin.getVoiceChatManager();
        if (vcManager == null) {
            return;
        }

        Group senderGroup = senderConnection.getGroup();

        // 2. Gizli Dinleme (Stealth Spy) - Grupta konuşan oyuncunun sesini gizlice dinleyen adminlere ilet
        if (senderGroup != null) {
            Set<UUID> spyingAdmins = vcManager.getSpyingAdminsForGroup(senderGroup.getId());
            if (!spyingAdmins.isEmpty()) {
                StaticSoundPacket staticPacket = null;
                for (UUID adminUuid : spyingAdmins) {
                    if (adminUuid.equals(senderUuid)) {
                        continue;
                    }
                    VoicechatConnection adminConn = serverApi.getConnectionOf(adminUuid);
                    if (adminConn != null && adminConn.isConnected()) {
                        if (staticPacket == null) {
                            staticPacket = event.getPacket().toStaticSoundPacket();
                        }
                        serverApi.sendStaticSoundPacketTo(adminConn, staticPacket);
                    }
                }
            }
        }

        // 3. Gizli Dinleme (Stealth Spy) - Adminin konuşması (Eğer talk modu açıksa)
        if (senderUuid != null && vcManager.isSpying(senderUuid)) {
            if (vcManager.isSpyTalkEnabled(senderUuid)) {
                UUID spiedGroupId = vcManager.getSpiedGroupId(senderUuid);
                if (spiedGroupId != null) {
                    List<Player> groupMembers = vcManager.getPlayersInGroup(spiedGroupId);
                    if (!groupMembers.isEmpty()) {
                        StaticSoundPacket adminPacket = event.getPacket().toStaticSoundPacket();
                        for (Player member : groupMembers) {
                            if (member.getUniqueId().equals(senderUuid)) {
                                continue;
                            }
                            VoicechatConnection memberConn = serverApi.getConnectionOf(member.getUniqueId());
                            if (memberConn != null && memberConn.isConnected()) {
                                serverApi.sendStaticSoundPacketTo(memberConn, adminPacket);
                            }
                        }
                    }
                    // Adminin etraftaki fiziksel proximity oyuncularına sesi sızmasın
                    event.cancel();
                }
            }
        }
    }

    private void onRemoveGroup(RemoveGroupEvent event) {
        Group group = event.getGroup();
        if (group != null && plugin.getVoiceChatManager() != null) {
            plugin.getVoiceChatManager().onGroupRemoved(group.getId(), group.getName());
        }
    }

    private void onPlayerDisconnected(PlayerDisconnectedEvent event) {
        UUID uuid = event.getPlayerUuid();
        if (uuid != null && plugin.getVoiceChatManager() != null) {
            plugin.getVoiceChatManager().stopSpying(uuid);
        }
    }

    public static VoicechatServerApi getServerApi() {
        return serverApi;
    }

    public static VoiceChatHook getInstance() {
        return instance;
    }
}
