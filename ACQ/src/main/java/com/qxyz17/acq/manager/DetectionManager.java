package com.qxyz17.acq.manager;

import com.qxyz17.acq.ACQ;
import com.qxyz17.acq.detection.modules.*;
import org.bukkit.entity.Player;

import java.util.*;

public class DetectionManager {
    
    private final ACQ plugin;
    private Map<String, DetectionModule> modules = new HashMap<>();
    private Map<UUID, Map<String, Integer>> violations = new HashMap<>();
    private Map<UUID, Map<String, Object>> playerData = new HashMap<>();
    private boolean debug = false;
    
    public DetectionManager(ACQ plugin) {
        this.plugin = plugin;
        initializeModules();
    }
    
    private void initializeModules() {
        // 注册所有检测模块
        registerModule(new KillAuraCheck(plugin));
        registerModule(new ReachCheck(plugin));
        registerModule(new SpeedCheck(plugin));
        registerModule(new FlyCheck(plugin));
        registerModule(new AutoClickCheck(plugin));
        registerModule(new ScaffoldCheck(plugin));
        registerModule(new TimerCheck(plugin));
        registerModule(new PhaseCheck(plugin));
        registerModule(new JesusCheck(plugin));
        registerModule(new MovementCheck(plugin));
        registerModule(new CombatCheck(plugin));
        registerModule(new InventoryCheck(plugin));
        
        plugin.getLogger().info("已加载 " + modules.size() + " 个检测模块");
    }
    
    private void registerModule(DetectionModule module) {
        modules.put(module.getName(), module);
    }
    
    public void runFastChecks(Player player) {
        if (player.hasPermission("acq.bypass")) return;
        
        for (DetectionModule module : modules.values()) {
            if (module.isEnabled() && module.getCheckType() == CheckType.FAST) {
                module.check(player);
            }
        }
    }
    
    public void runSlowChecks(Player player) {
        if (player.hasPermission("acq.bypass")) return;
        
        for (DetectionModule module : modules.values()) {
            if (module.isEnabled() && module.getCheckType() == CheckType.SLOW) {
                module.check(player);
            }
        }
    }
    
    public void addViolation(Player player, String module, int amount, String details) {
        UUID uuid = player.getUniqueId();
        Map<String, Integer> playerViolations = violations.getOrDefault(uuid, new HashMap<>());
        
        int currentVL = playerViolations.getOrDefault(module, 0);
        int newVL = currentVL + amount;
        playerViolations.put(module, newVL);
        violations.put(uuid, playerViolations);
        
        // 发送警报
        if (plugin.getConfigManager().isAlertsEnabled()) {
            plugin.alertStaff(player, module, newVL, details);
        }
        
        if (debug) {
            plugin.getLogger().info("[DEBUG] " + player.getName() + " - " + module + 
                    " +" + amount + " (Now: " + newVL + ") - " + details);
        }
    }
    
    public int getViolationLevel(Player player, String module) {
        Map<String, Integer> playerViolations = violations.get(player.getUniqueId());
        return playerViolations != null ? playerViolations.getOrDefault(module, 0) : 0;
    }
    
    public void resetViolations(Player player) {
        violations.remove(player.getUniqueId());
    }
    
    public void saveViolations() {
        // TODO: 保存违规数据到文件
    }
    
    public void reloadModules() {
        for (DetectionModule module : modules.values()) {
            module.reload();
        }
    }
    
    public void toggleDebug() {
        debug = !debug;
    }
    
    public boolean isDebug() {
        return debug;
    }
    
    public Map<String, Object> getPlayerData(Player player) {
        return playerData.getOrDefault(player.getUniqueId(), new HashMap<>());
    }
    
    public void setPlayerData(Player player, String key, Object value) {
        Map<String, Object> data = playerData.getOrDefault(player.getUniqueId(), new HashMap<>());
        data.put(key, value);
        playerData.put(player.getUniqueId(), data);
    }
}
