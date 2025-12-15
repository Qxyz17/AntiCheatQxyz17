package com.qxyz17.acq.command;

import com.qxyz17.acq.ACQ;
import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class ACQCommand implements TabExecutor {

    private final ACQ plugin;

    public ACQCommand(ACQ plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§b[ACQ] §7用法: /acq <info|ban|unban|reload|ml|web>");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "info":
                sender.sendMessage("§b[ACQ] §7检测模块: " + plugin.getDetectionManager().getLoadedModules().size());
                sender.sendMessage("§b[ACQ] §7Web 面板地址: http://" + plugin.getWebHost() + ":" + plugin.getWebPort());
                break;
            case "ban":
                if (args.length < 2) {
                    sender.sendMessage("§c[ACQ] /acq ban <player>");
                    return true;
                }
                plugin.getDetectionManager().manualBan(args[1], "Admin");
                break;
            case "unban":
                if (args.length < 2) return true;
                plugin.getDetectionManager().unban(args[1]);
                break;
            case "reload":
                plugin.getConfigManager().reload();
                plugin.getLanguageManager().reload();
                sender.sendMessage("§a[ACQ] 配置重载完成");
                break;
            case "ml":
                plugin.getDetectionManager().toggleML();
                sender.sendMessage("§a[ACQ] ML 检测已 " + (plugin.getDetectionManager().isMLEnabled() ? "开启" : "关闭"));
                break;
            case "web":
                sender.sendMessage("§b[ACQ] Web 面板: http://" + plugin.getWebHost() + ":" + plugin.getWebPort());
                break;
            default:
                sender.sendMessage("§c[ACQ] 未知子命令");
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd,
                                      @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1)
            return Arrays.asList("info", "ban", "unban", "reload", "ml", "web");
        return null;
    }
}
