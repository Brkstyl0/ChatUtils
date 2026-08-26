package com.chatutils.hook;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LuckPermsHook {

    private static Boolean available = null;

    public static boolean isAvailable() {
        if (available == null) {
            try {
                Class.forName("net.luckperms.api.LuckPermsProvider");
                available = Bukkit.getPluginManager().getPlugin("LuckPerms") != null;
            } catch (Throwable t) {
                available = false;
            }
        }
        return available;
    }

    public static List<String> getGroupNames() {
        if (!isAvailable()) {
            return Collections.emptyList();
        }
        try {
            LuckPerms lp = LuckPermsProvider.get();
            List<String> list = new ArrayList<>();
            for (Group group : lp.getGroupManager().getLoadedGroups()) {
                list.add(group.getName());
            }
            return list;
        } catch (Throwable t) {
            return Collections.emptyList();
        }
    }

    public static String getGroupPrefix(String groupName) {
        if (!isAvailable() || groupName == null) {
            return null;
        }
        try {
            LuckPerms lp = LuckPermsProvider.get();
            Group group = lp.getGroupManager().getGroup(groupName.toLowerCase());
            if (group != null) {
                String prefix = group.getCachedData().getMetaData().getPrefix();
                if (prefix != null && !prefix.trim().isEmpty()) {
                    return prefix;
                }
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    public static String getGroupDisplayName(String groupName) {
        if (!isAvailable() || groupName == null) {
            return groupName;
        }
        try {
            LuckPerms lp = LuckPermsProvider.get();
            Group group = lp.getGroupManager().getGroup(groupName.toLowerCase());
            if (group != null) {
                String displayName = group.getDisplayName();
                if (displayName != null && !displayName.trim().isEmpty()) {
                    return displayName;
                }
            }
        } catch (Throwable ignored) {
        }
        return groupName;
    }
}
