package com.qxyz17.acq.manager;

import com.qxyz17.acq.ACQ;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    
    private final ACQ plugin;
    private FileConfiguration config;
    private FileConfiguration guiConfig;
    private Map<String, Object> settings = new HashMap<>();
    
    public ConfigManager(ACQ plugin) {
        this.plugin = plugin;
    }
    
    public void loadConfig() {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
        
        // 加载GUI配置
        File guiFile = new File(plugin.getDataFolder(), "gui.yml");
        if (!guiFile.exists()) {
            plugin.saveResource("gui.yml", false);
        }
        guiConfig = YamlConfiguration.loadConfiguration(guiFile);
        
        // 加载默认设置
        loadSettings();
    }
    
    private void loadSettings() {
        settings.clear();
        
        // 封禁设置
        settings.put("ban.enabled", config.getBoolean("ban.enabled", true));
        settings.put("ban.command", config.getString("ban.command", "ban %player% [ACQ] %reason%"));
        settings.put("ban.threshold", config.getInt("ban.threshold", 50));
        settings.put("ban.broadcast", config.getBoolean("ban.broadcast", true));
        
        // 检测设置
        settings.put("detection.sensitivity", config.getDouble("detection.sensitivity", 1.0));
        settings.put("detection.max_vl", config.getInt("detection.max_vl", 100));
        settings.put("detection.reset_time", config.getInt("detection.reset_time", 300));
        
        // 警报设置
        settings.put("alerts.enabled", config.getBoolean("alerts.enabled", true));
        settings.put("alerts.sound", config.getBoolean("alerts.sound", true));
        settings.put("alerts.title", config.getBoolean("alerts.title", true));
        
        // 语言设置
        settings.put("language", config.getString("language", "zh"));
        
        // UI设置
        settings.put("ui.animations", config.getBoolean("ui.animations", true));
        settings.put("ui.gradient_effects", config.getBoolean("ui.gradient_effects", true));
        settings.put("ui.particle_effects", config.getBoolean("ui.particle_effects", false));
    }
    
    public void setLanguage(String lang) {
        config.set("language", lang);
        plugin.saveConfig();
        settings.put("language", lang);
        plugin.getLanguageManager().loadLanguages();
    }
    
    public String getLanguage() {
        return (String) settings.get("language");
    }
    
    public int getAutoBanThreshold() {
        return (int) settings.get("ban.threshold");
    }
    
    public String getBanCommand() {
        return (String) settings.get("ban.command");
    }
    
    public double getSensitivity() {
        return (double) settings.get("detection.sensitivity");
    }
    
    public boolean isAlertsEnabled() {
        return (boolean) settings.get("alerts.enabled");
    }
    
    public FileConfiguration getGuiConfig() {
        return guiConfig;
    }
    
    public void saveSetting(String path, Object value) {
        config.set(path, value);
        plugin.saveConfig();
        settings.put(path, value);
    }
    
    public Object getSetting(String path) {
        return settings.get(path);
    }
}
