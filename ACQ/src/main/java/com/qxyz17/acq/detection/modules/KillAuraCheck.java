package com.qxyz17.acq.detection.modules;

import com.qxyz17.acq.ACQ;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KillAuraCheck extends DetectionModule {
    
    private Map<UUID, Map<Long, Integer>> hitData = new HashMap<>();
    private Map<UUID, Location> lastLocations = new HashMap<>();
    private Map<UUID, Long> lastHitTime = new HashMap<>();
    
    public KillAuraCheck(ACQ plugin) {
        super(plugin, "KillAura", CheckType.FAST, 10);
    }
    
    @Override
    public void check(Player player) {
        // 获取玩家数据
        UUID uuid = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        
        // 检查攻击速度
        if (lastHitTime.containsKey(uuid)) {
            long timeDiff = currentTime - lastHitTime.get(uuid);
            
            if (timeDiff < 50) { // 20次/秒以上
                addViolation(player, 5, "攻击速度异常 (" + timeDiff + "ms)");
                return;
            }
            
            // 记录攻击数据
            long secondKey = currentTime / 1000;
            Map<Long, Integer> data = hitData.getOrDefault(uuid, new HashMap<>());
            data.put(secondKey, data.getOrDefault(secondKey, 0) + 1);
            hitData.put(uuid, data);
            
            // 检查秒级攻击频率
            int hitsPerSecond = data.getOrDefault(secondKey, 0);
            if (hitsPerSecond > 15) { // 每秒超过15次攻击
                addViolation(player, 8, "高频攻击 (" + hitsPerSecond + "次/秒)");
            }
        }
        lastHitTime.put(uuid, currentTime);
        
        // 检查攻击角度
        checkAttackAngle(player);
        
        // 检查攻击距离
        checkAttackDistance(player);
    }
    
    private void checkAttackAngle(Player player) {
        UUID uuid = player.getUniqueId();
        Location currentLoc = player.getLocation();
        
        if (lastLocations.containsKey(uuid)) {
            Location lastLoc = lastLocations.get(uuid);
            
            // 计算视角变化
            float yawDiff = Math.abs(currentLoc.getYaw() - lastLoc.getYaw());
            float pitchDiff = Math.abs(currentLoc.getPitch() - lastLoc.getPitch());
            
            // 检查不自然的视角变化
            if (yawDiff > 90 && pitchDiff > 45) {
                addViolation(player, 3, "视角变化异常 (Yaw: " + yawDiff + ", Pitch: " + pitchDiff + ")");
            }
            
            // 检查平滑度
            if (yawDiff > 0 && yawDiff < 1) {
                // 过于平滑可能为aimbot
                addViolation(player, 2, "攻击角度过于平滑");
            }
        }
        
        lastLocations.put(uuid, currentLoc);
    }
    
    private void checkAttackDistance(Player player) {
        // 获取玩家准星内的实体
        Player target = getTargetPlayer(player);
        if (target != null) {
            double distance = player.getLocation().distance(target.getLocation());
            
            // 检查攻击距离
            if (distance > 4.5) { // 超过正常攻击距离
                addViolation(player, 10, "攻击距离过远 (" + String.format("%.1f", distance) + " blocks)");
            }
            
            // 检查攻击角度
            Vector direction = target.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
            Vector lookDirection = player.getLocation().getDirection();
            
            double angle = Math.toDegrees(direction.angle(lookDirection));
            if (angle > 45) { // 角度偏差过大
                addViolation(player, 6, "攻击角度偏差 (" + String.format("%.1f", angle) + "°)");
            }
        }
    }
    
    private Player getTargetPlayer(Player player) {
        // 简化的目标获取方法
        for (Player target : player.getWorld().getPlayers()) {
            if (target != player && player.hasLineOfSight(target)) {
                Location loc = player.getLocation();
                Location targetLoc = target.getLocation();
                
                if (loc.distance(targetLoc) < 6) {
                    return target;
                }
            }
        }
        return null;
    }
    
    @Override
    public void reload() {
        // 重新加载配置
        enabled = plugin.getConfig().getBoolean("modules." + name + ".enabled", true);
        maxViolations = plugin.getConfig().getInt("modules." + name + ".max_violations", 10);
    }
}
