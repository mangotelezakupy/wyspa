package com.client.WYSPAAC.api;

import java.util.List;

import org.bukkit.entity.Player;

import com.client.WYSPAAC.WYSPAAC;
import com.client.WYSPAAC.check.CheckType;
import com.client.WYSPAAC.manage.AntiCheatManager;
import com.client.WYSPAAC.manage.CheckManager;
import com.client.WYSPAAC.manage.UserManager;
import com.client.WYSPAAC.util.Group;

@Deprecated
public class AntiCheatAPI {
    private static CheckManager chk = WYSPAAC.getManager().getCheckManager();
    private static UserManager umr = WYSPAAC.getManager().getUserManager();

    public static void activateCheck(CheckType type, Class<?> caller) {
        chk.activateCheck(type, caller.getName());
    }

    public static void deactivateCheck(CheckType type, Class<?> caller) {
        chk.deactivateCheck(type, caller.getName());
    }

    public static boolean isActive(CheckType type) {
        return chk.isActive(type);
    }

    public static void exemptPlayer(Player player, CheckType type, Class<?> caller) {
        chk.exemptPlayer(player, type, caller.getName());
    }

    public static void unexemptPlayer(Player player, CheckType type, Class<?> caller) {
        chk.unexemptPlayer(player, type, caller.getName());
    }

    public static boolean isExempt(Player player, CheckType type) {
        return chk.isExempt(player, type);
    }

    public boolean willCheck(Player player, CheckType type) {
        return chk.willCheck(player, type);
    }

    public static void resetPlayer(Player player) {
        umr.getUser(player.getUniqueId()).resetLevel();
    }

    public static Group getGroup(Player player) {
        return umr.getUser(player.getUniqueId()).getGroup();
    }

    public static List<Group> getGroups() {
        return getManager().getConfiguration().getGroups().getGroups();
    }

    public static AntiCheatManager getManager() {
        return WYSPAAC.getManager();
    }

}
