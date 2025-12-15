package com.qxyz17.acq.manager;

import com.qxyz17.acq.ACQ;
import com.qxyz17.acq.ui.MainGUI;
import com.qxyz17.acq.ui.PlayerInfoGUI;
import com.qxyz17.acq.ui.SettingsGUI;
import org.bukkit.entity.Player;

public class UIManager {
    
    private final ACQ plugin;
    
    public UIManager(ACQ plugin) {
        this.plugin = plugin;
    }
    
    public void openMainGUI(Player player) {
        new MainGUI(player).open();
    }
    
    public void openSettingsGUI(Player player) {
        new SettingsGUI(player).open();
    }
    
    public void openPlayerInfoGUI(Player player, Player target) {
        new PlayerInfoGUI(player, target).open();
    }
    
    public String formatViolationLevel(int vl) {
        if (vl < 10) return "§a" + vl;
        if (vl < 30) return "§e" + vl;
        if (vl < 50) return "§6" + vl;
        if (vl < 70) return "§c" + vl;
        return "§4" + vl;
    }
    
    public String createGradientText(String text) {
        // 使用颜色工具类创建渐变文字
        return plugin.getGradientText(text);
    }
    
    public String getProgressBar(int current, int max, int length) {
        float percentage = (float) current / max;
        int progress = (int) (length * percentage);
        
        StringBuilder bar = new StringBuilder("§a");
        for (int i = 0; i < length; i++) {
            if (i < progress) {
                bar.append("|");
            } else {
                bar.append("§7|");
            }
        }
        return bar.toString();
    }
}
