package com.qxyz17.acq;

import com.qxyz17.acq.manager.*;
import com.qxyz17.acq.ui.MainGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ACQ extends JavaPlugin {
    
    private static ACQ instance;
    private DetectionManager detectionManager;
    private ConfigManager configManager;
    private LanguageManager languageManager;
    private UIManager uiManager;
    
    private Map<UUID, Long> lastAlert = new HashMap<>();
    private Map<UUID, Integer> violationLevels = new HashMap<>();
    private boolean enabled = true;
    private String prefix = "§8[§d§lACQ§8] §7";
    
    @Override
    public void onEnable() {
        instance = this;
        
        // 初始化管理器
        configManager = new ConfigManager(this);
        languageManager = new LanguageManager(this);
        detectionManager = new DetectionManager(this);
        uiManager = new UIManager(this);
        
        // 加载配置
        saveDefaultConfig();
        configManager.loadConfig();
        languageManager.loadLanguages();
        
        // 注册监听器
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        
        // 启动检测任务
        startDetectionTasks();
        
        // 注册命令
        this.getCommand("acq").setExecutor(this);
        this.getCommand("acqadmin").setExecutor(this);
        
        // 发送启动消息
        sendStartupMessage();
        
        // 启用bStats统计
        try {
            new Metrics(this, 17897);
        } catch (Exception e) {
            getLogger().info("统计服务启动失败，但插件正常运行");
        // 注册指令
        getCommand("acq").setExecutor(new ACQCommand(this));
        // 注册跨版本协
        PacketEvents.getAPI().getEventManager().registerListener(new VersionBridge(detectionManager));
        PacketEvents.getAPI().init();
        //启动 Web 面板
        webHost = getConfig().getString("web.host", "0.0.0.0");
        webPort = getConfig().getInt("web.port", 8080);
        web = new WebPanel(configManager, detectionManager, webPort);
        getLogger().info("WebPanel running at http://" + webHost + ":" + webPort);
    }

    @Override
    public void onDisable() {
        if (web != null) web.stop();
        PacketEvents.getAPI().terminate();
    }

    public String getWebHost() { return webHost; }
    public int    getWebPort() { return webPort; }
        }
    }
    
    @Override
    public void onDisable() {
        detectionManager.saveViolations();
        sendShutdownMessage();
    }
    
    private void sendStartupMessage() {
        String[] art = {
            "§8╔════════════════════════════════════╗",
            "§8║  §d╔═╗╔═╗╔═╗  §5╔═╗╦ ╦╔═╗╦═╗╔╦╗  §8║",
            "§8║  §d║ ║╠╣ ╠═╝  §5║  ╠═╣║╣ ╠╦╝ ║║  §8║",
            "§8║  §d╚═╝╚  ╩    §5╚═╝╩ ╩╚═╝╩╚══╩╝  §8║",
            "§8║                                    §8║",
            "§8║  §7版本: §f" + getDescription().getVersion() + "  §7|  §f作者: Qxyz17  §8║",
            "§8║  §7支持版本: §f1.7.10 - 1.21.8    §8║",
            "§8╚════════════════════════════════════╝"
        };
        
        for (String line : art) {
            getLogger().info(ChatColor.stripColor(line));
        }
        
        Bukkit.getConsoleSender().sendMessage(getGradientText("§lACQ 反作弊已成功启动！"));
    }
    
    private void sendShutdownMessage() {
        Bukkit.getConsoleSender().sendMessage(getGradientText("§lACQ 反作弊已关闭"));
    }
    
    private void startDetectionTasks() {
        // 高频检测 (每tick)
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!enabled) return;
                
                for (Player player : Bukkit.getOnlinePlayers()) {
                    detectionManager.runFastChecks(player);
                }
            }
        }.runTaskTimer(this, 0L, 1L);
        
        // 低频检测 (每10tick)
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!enabled) return;
                
                for (Player player : Bukkit.getOnlinePlayers()) {
                    detectionManager.runSlowChecks(player);
                }
            }
        }.runTaskTimer(this, 0L, 10L);
    }
    
    public void alertStaff(Player player, String check, int vl, String details) {
        UUID playerId = player.getUniqueId();
        long now = System.currentTimeMillis();
        
        // 防刷屏：每2秒最多一次警报
        if (lastAlert.containsKey(playerId) && now - lastAlert.get(playerId) < 2000) {
            return;
        }
        lastAlert.put(playerId, now);
        
        // 增加违规等级
        int totalVL = violationLevels.getOrDefault(playerId, 0) + vl;
        violationLevels.put(playerId, totalVL);
        
        String message = languageManager.getMessage("alerts.format", player)
                .replace("%player%", player.getName())
                .replace("%check%", check)
                .replace("%vl%", String.valueOf(vl))
                .replace("%total_vl%", String.valueOf(totalVL))
                .replace("%details%", details);
        
        // 发送给有权限的管理员
        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (staff.hasPermission("acq.alerts") || staff.isOp()) {
                staff.sendMessage(getGradientText(message));
            }
        }
        
        // 控制台输出
        String consoleMsg = String.format("[ACQ] %s 触发了 %s (VL: %d, Total: %d, Details: %s)",
                player.getName(), check, vl, totalVL, details);
        getLogger().warning(consoleMsg);
        
        // 自动封禁检查
        if (totalVL >= configManager.getAutoBanThreshold()) {
            executeBan(player, "累计违规等级过高 (VL: " + totalVL + ")");
        }
    }
    
    private void executeBan(Player player, String reason) {
        String banCommand = configManager.getBanCommand()
                .replace("%player%", player.getName())
                .replace("%reason%", reason);
        
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), banCommand);
        
        // 广播封禁消息
        String banMessage = languageManager.getMessage("punishments.ban_broadcast", player)
                .replace("%player%", player.getName())
                .replace("%reason%", reason);
        
        Bukkit.broadcastMessage(getGradientText(banMessage));
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("acq")) {
            if (args.length == 0) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (player.hasPermission("acq.admin")) {
                        new MainGUI(player).open();
                    } else {
                        player.sendMessage(prefix + languageManager.getMessage("no_permission", player));
                    }
                } else {
                    sender.sendMessage("控制台请使用: /acqadmin");
                }
                return true;
            }
            
            if (args[0].equalsIgnoreCase("reload") && sender.hasPermission("acq.admin")) {
                reload();
                sender.sendMessage(prefix + languageManager.getMessage("config_reloaded", sender));
                return true;
            }
        }
        
        if (cmd.getName().equalsIgnoreCase("acqadmin") && (sender.isOp() || sender.hasPermission("acq.*"))) {
            handleAdminCommand(sender, args);
            return true;
        }
        
        return false;
    }
    
    private void handleAdminCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(getGradientText("§l§m     §8[ §d§lACQ Admin §8]§l§m     "));
            sender.sendMessage("§d/acqadmin toggle §7- 开关反作弊");
            sender.sendMessage("§d/acqadmin lang <zh/en> §7- 切换语言");
            sender.sendMessage("§d/acqadmin debug §7- 调试模式");
            sender.sendMessage("§d/acqadmin bypass <player> §7- 给玩家绕过权限");
            sender.sendMessage("§d/acqadmin alerts §7- 开关警报");
            return;
        }
        
        switch (args[0].toLowerCase()) {
            case "toggle":
                enabled = !enabled;
                sender.sendMessage(prefix + "反作弊已" + (enabled ? "§a开启" : "§c关闭"));
                break;
                
            case "lang":
                if (args.length > 1) {
                    String lang = args[1].toLowerCase();
                    if (lang.equals("zh") || lang.equals("en")) {
                        configManager.setLanguage(lang);
                        sender.sendMessage(prefix + "语言已切换为: " + lang);
                    }
                }
                break;
                
            case "debug":
                detectionManager.toggleDebug();
                sender.sendMessage(prefix + "调试模式: " + (detectionManager.isDebug() ? "§a开启" : "§c关闭"));
                break;
        }
    }
    
    private void reload() {
        reloadConfig();
        configManager.loadConfig();
        languageManager.loadLanguages();
        detectionManager.reloadModules();
    }
    
    public String getGradientText(String text) {
        return ColorUtil.createGradient(text, ColorUtil.GRADIENT_PURPLE_PINK);
    }
    
    public static ACQ getInstance() {
        return instance;
    }
    
    public DetectionManager getDetectionManager() {
        return detectionManager;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public LanguageManager getLanguageManager() {
        return languageManager;
    }
    
    public UIManager getUIManager() {
        return uiManager;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public String getPrefix() {
        return prefix;
    }
}
