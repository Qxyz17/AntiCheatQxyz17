package com.qxyz17.acq.detection;

import com.qxyz17.acq.manager.DetectionManager;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * 所有检测模块的父类，自带开关、VL、权重、ML 打分
 */
public abstract class DetectionModule {

    protected final DetectionManager manager;
    protected final String name;
    protected boolean enabled;
    protected int maxVL;
    protected double weight;

    public DetectionModule(DetectionManager manager, String name) {
        this.manager = manager;
        this.name = name.toLowerCase();
        reload();
    }

    public final void reload() {
        FileConfiguration c = manager.getPlugin().getConfigManager().getConfig();
        this.enabled = c.getBoolean("checks." + name + ".enable", true);
        this.maxVL = c.getInt("checks." + name + ".max-vl", 50);
        this.weight = c.getDouble("checks." + name + ".weight", 1.0);
    }

    public boolean isEnabled() { return enabled; }

    public String getName() { return name; }

    /** 子类只需实现具体逻辑，调用 punish 即可 */
    protected void punish(String player, String debug) {
        int vl = manager.increaseVL(player, name, weight);
        if (vl >= maxVL) {
            manager.flag(player, name, debug);
        }
    }
}
