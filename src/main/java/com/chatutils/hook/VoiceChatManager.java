package com.chatutils.hook;

import com.chatutils.ChatUtils;
import com.chatutils.utils.ColorUtil;
import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class VoiceChatManager {

    private final ChatUtils plugin;
    private boolean active = false;

    // Admin UUID -> Gizlice dinlediği Group UUID
    private final Map<UUID, UUID> spyingAdmins = new ConcurrentHashMap<>();

    // Grup konuşma modu açık olan Admin UUID'leri (Konuştuğunda sesi gruptakilere gitsin)
    private final Set<UUID> spyTalkEnabled = ConcurrentHashMap.newKeySet();

    private BukkitTask actionBarTask;

    public VoiceChatManager(ChatUtils plugin) {
        this.plugin = plugin;
    }

    public void init() {
        try {
            Class.forName("de.maxhenkel.voicechat.api.BukkitVoicechatService");
            Class.forName("de.maxhenkel.voicechat.api.VoicechatPlugin");

            Class<?> hookClass = Class.forName("com.chatutils.hook.VoiceChatHook");
            hookClass.getMethod("register", ChatUtils.class).invoke(null, plugin);
            this.active = true;

            startActionBarTask();
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            plugin.getLogger().info("Simple Voice Chat eklentisi bulunamadi, sesli sohbet entegrasyonu pasif durumda.");
        } catch (Throwable t) {
            plugin.getLogger().warning("Simple Voice Chat baglantisi kurulurken bir sorun olustu: " + t.getMessage());
        }
    }

    public boolean isActive() {
        return active;
    }

    public boolean isAvailable() {
        return active && VoiceChatHook.getServerApi() != null;
    }

    public VoicechatServerApi getServerApi() {
        return VoiceChatHook.getServerApi();
    }

    public Collection<Group> getGroups() {
        VoicechatServerApi api = getServerApi();
        if (api == null) {
            return Collections.emptyList();
        }
        Collection<Group> groups = api.getGroups();
        return groups != null ? groups : Collections.emptyList();
    }

    public Group getGroup(UUID groupId) {
        VoicechatServerApi api = getServerApi();
        if (api == null || groupId == null) {
            return null;
        }
        return api.getGroup(groupId);
    }

    /**
     * Grup türünü okunabilir metne dönüştürür.
     */
    public static String formatGroupType(Group.Type type) {
        if (type == null) return "NORMAL";
        if (type == Group.Type.OPEN) return "AÇIK";
        if (type == Group.Type.ISOLATED) return "İZOLE";
        return "NORMAL";
    }

    /**
     * Grup adı, UUID veya bir oyuncunun ismi girilerek akıllı grup araması yapar.
     */
    public Group findGroup(String query) {
        if (!isAvailable() || query == null || query.trim().isEmpty()) {
            return null;
        }
        query = query.trim();
        VoicechatServerApi api = getServerApi();

        // 1. UUID ile arama
        try {
            UUID id = UUID.fromString(query);
            Group g = api.getGroup(id);
            if (g != null) {
                return g;
            }
        } catch (IllegalArgumentException ignored) {
        }

        // 2. Birebir Grup Adı (Büyük/Küçük harf duyarsız)
        for (Group g : getGroups()) {
            if (g.getName() != null && g.getName().equalsIgnoreCase(query)) {
                return g;
            }
        }

        // 3. Oyuncu adı ile arama (Oyuncunun bulunduğu grubu bul)
        Player targetPlayer = Bukkit.getPlayerExact(query);
        if (targetPlayer != null) {
            Group playerGroup = getPlayerGroup(targetPlayer);
            if (playerGroup != null) {
                return playerGroup;
            }
        }

        // 4. Kısmi Grup Adı eşleşmesi
        String lower = query.toLowerCase(Locale.ROOT);
        for (Group g : getGroups()) {
            if (g.getName() != null && g.getName().toLowerCase(Locale.ROOT).contains(lower)) {
                return g;
            }
        }

        return null;
    }

    public Group getPlayerGroup(Player player) {
        if (player == null) return null;
        return getPlayerGroup(player.getUniqueId());
    }

    public Group getPlayerGroup(UUID playerUuid) {
        VoicechatServerApi api = getServerApi();
        if (api == null || playerUuid == null) {
            return null;
        }
        VoicechatConnection connection = api.getConnectionOf(playerUuid);
        return (connection != null) ? connection.getGroup() : null;
    }

    public List<Player> getPlayersInGroup(UUID groupId) {
        if (!isAvailable() || groupId == null) {
            return Collections.emptyList();
        }
        List<Player> members = new ArrayList<>();
        VoicechatServerApi api = getServerApi();

        for (Player p : Bukkit.getOnlinePlayers()) {
            VoicechatConnection conn = api.getConnectionOf(p.getUniqueId());
            if (conn != null && conn.isInGroup()) {
                Group g = conn.getGroup();
                if (g != null && groupId.equals(g.getId())) {
                    members.add(p);
                }
            }
        }
        return members;
    }

    public List<Player> getPlayersInGroup(Group group) {
        if (group == null) return Collections.emptyList();
        return getPlayersInGroup(group.getId());
    }

    // ==========================================
    //           GİZLİ DİNLEME (STEALTH SPY)
    // ==========================================

    /**
     * Yetkiliyi gizli dinleme (Stealth Ghost) moduna alır.
     * Bu modda yetkili grubun seslerini duyar, fakat resmi grupta listelenmediği için SOL ÜSTTE GÖZÜKMEZ.
     */
    public boolean startSpying(Player admin, Group group, boolean talk) {
        if (!isAvailable() || admin == null || group == null) {
            return false;
        }

        VoicechatConnection conn = getServerApi().getConnectionOf(admin.getUniqueId());
        if (conn == null || !conn.isConnected()) {
            return false;
        }

        // Eğer admin normal bir gruptaysa, oradan çıkar
        if (conn.isInGroup()) {
            conn.setGroup(null);
        }

        UUID adminUuid = admin.getUniqueId();
        spyingAdmins.put(adminUuid, group.getId());

        if (talk) {
            spyTalkEnabled.add(adminUuid);
        } else {
            spyTalkEnabled.remove(adminUuid);
        }

        return true;
    }

    public void stopSpying(Player admin) {
        if (admin != null) {
            stopSpying(admin.getUniqueId());
        }
    }

    public void stopSpying(UUID adminUuid) {
        if (adminUuid == null) return;
        spyingAdmins.remove(adminUuid);
        spyTalkEnabled.remove(adminUuid);
    }

    public boolean isSpying(Player admin) {
        return admin != null && isSpying(admin.getUniqueId());
    }

    public boolean isSpying(UUID adminUuid) {
        return adminUuid != null && spyingAdmins.containsKey(adminUuid);
    }

    public UUID getSpiedGroupId(UUID adminUuid) {
        return adminUuid != null ? spyingAdmins.get(adminUuid) : null;
    }

    public boolean isSpyTalkEnabled(UUID adminUuid) {
        return adminUuid != null && spyTalkEnabled.contains(adminUuid);
    }

    public void setSpyTalkEnabled(UUID adminUuid, boolean talk) {
        if (adminUuid == null) return;
        if (talk) {
            spyTalkEnabled.add(adminUuid);
        } else {
            spyTalkEnabled.remove(adminUuid);
        }
    }

    public Set<UUID> getSpyingAdminsForGroup(UUID groupId) {
        if (groupId == null || spyingAdmins.isEmpty()) {
            return Collections.emptySet();
        }
        return spyingAdmins.entrySet().stream()
                .filter(entry -> groupId.equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    // ==========================================
    //           STANDART GRUP YÖNETİMİ
    // ==========================================

    /**
     * Oyuncuyu veya yetkiliyi standart olarak gruba sokar (Şifreyi baypas eder).
     * Sol üstte gözükür.
     */
    public boolean joinGroupVisible(Player player, Group group) {
        if (!isAvailable() || player == null || group == null) {
            return false;
        }
        stopSpying(player.getUniqueId());
        VoicechatConnection conn = getServerApi().getConnectionOf(player.getUniqueId());
        if (conn == null || !conn.isConnected()) {
            return false;
        }
        conn.setGroup(group);
        return true;
    }

    public boolean leaveGroup(Player player) {
        if (!isAvailable() || player == null) {
            return false;
        }
        stopSpying(player.getUniqueId());
        VoicechatConnection conn = getServerApi().getConnectionOf(player.getUniqueId());
        if (conn != null && conn.isInGroup()) {
            conn.setGroup(null);
            return true;
        }
        return true;
    }

    public void onGroupRemoved(UUID groupId, String groupName) {
        if (groupId == null) return;
        Set<UUID> admins = getSpyingAdminsForGroup(groupId);
        for (UUID adminUuid : admins) {
            stopSpying(adminUuid);
            Player admin = Bukkit.getPlayer(adminUuid);
            if (admin != null && admin.isOnline()) {
                String gName = groupName != null ? groupName : "Grup";
                admin.sendMessage(plugin.getConfigManager().getPrefix() + "§c[VoiceChat] Dinlediğiniz §e" + gName + " §cadlı ses grubu kapatıldı/dağıtıldı.");
            }
        }
    }

    private void startActionBarTask() {
        if (actionBarTask != null) {
            actionBarTask.cancel();
        }

        actionBarTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (spyingAdmins.isEmpty()) return;

            for (Map.Entry<UUID, UUID> entry : spyingAdmins.entrySet()) {
                UUID adminUuid = entry.getKey();
                UUID groupId = entry.getValue();

                Player admin = Bukkit.getPlayer(adminUuid);
                if (admin != null && admin.isOnline()) {
                    Group group = getGroup(groupId);
                    String groupName = (group != null && group.getName() != null) ? group.getName() : "Bilinmeyen Grup";
                    boolean talk = spyTalkEnabled.contains(adminUuid);

                    String talkStatus = talk ? "§aKonuşma Açık" : "§cSalt Dinleme";
                    String hudText = "§a§l[SESLİ SPY: §e" + groupName + "§a§l] §7(Gizli Dinleme | " + talkStatus + " §8| §cSol üstte yoksunuz§7)";

                    admin.sendActionBar(Component.text(ColorUtil.colorize(hudText)));
                }
            }
        }, 20L, 40L);
    }

    public void shutdown() {
        if (actionBarTask != null) {
            actionBarTask.cancel();
            actionBarTask = null;
        }
        spyingAdmins.clear();
        spyTalkEnabled.clear();
    }
}
