package com.qxyz17.acq.manager;

import com.qxyz17.acq.ACQ;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class LanguageManager {
    
    private final ACQ plugin;
    private Map<String, String> messages = new HashMap<>();
    private String currentLanguage;
    
    public LanguageManager(ACQ plugin) {
        this.plugin = plugin;
    }
    
    public void loadLanguages() {
        messages.clear();
        currentLanguage = plugin.getConfigManager().getLanguage();
        
        // 加载默认语言文件
        File langFile = new File(plugin.getDataFolder(), "messages_" + currentLanguage + ".yml");
        if (!langFile.exists()) {
            plugin.saveResource("messages_" + currentLanguage + ".yml", false);
        }
        
        FileConfiguration langConfig = YamlConfiguration.loadConfiguration(langFile);
        
        // 加载所有消息
        for (String key : langConfig.getKeys(true)) {
            if (langConfig.isString(key)) {
                messages.put(key, langConfig.getString(key));
            }
        }
        
        plugin.getLogger().info("已加载语言: " + currentLanguage + " (" + messages.size() + " 条消息)");
    }
    
    public String getMessage(String key, CommandSender sender) {
        return getMessage(key).replace("%prefix%", plugin.getPrefix());
    }
    
    public String getMessage(String key, Player player) {
        String message = getMessage(key);
        message = message.replace("%player%", player.getName());
        message = message.replace("%world%", player.getWorld().getName());
        message = message.replace("%ping%", String.valueOf(getPing(player)));
        return message;
    }
    
    public String getMessage(String key) {
        return messages.getOrDefault(key, "§cMissing message: " + key);
    }
    
    private int getPing(Player player) {
        try {
            Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);
            return (int) entityPlayer.getClass().getField("ping").get(entityPlayer);
        } catch (Exception e) {
            return 0;
        }
    }
    
    public String getCurrentLanguage() {
        return currentLanguage;
    }
}
