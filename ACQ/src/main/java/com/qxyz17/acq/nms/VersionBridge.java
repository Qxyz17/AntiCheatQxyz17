package com.qxyz17.acq.nms;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

/**
 * 1.7.10 → 1.21.8 统一入口，屏蔽 NMS 差异
 */
public class VersionBridge extends PacketListenerAbstract {

    private final com.qxyz17.acq.manager.DetectionManager detectionManager;

    public VersionBridge(com.qxyz17.acq.manager.DetectionManager dm) {
        this.detectionManager = dm;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent e) {
        if (e.getPacketType() == PacketType.Play.Client.PLAYER_FLYING
            || e.getPacketType() == PacketType.Play.Client.PLAYER_POSITION
            || e.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION
            || e.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {

            WrapperPlayClientPlayerFlying fly = new WrapperPlayClientPlayerFlying(e);
            detectionManager.onMovement(e.getUser().getName(), fly.getLocation(), fly.isOnGround());
        }

        if (e.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            detectionManager.onAttack(e.getUser().getName(), e.getPlayer());
        }

        if (e.getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT) {
            WrapperPlayClientPlayerBlockPlacement place = new WrapperPlayClientPlayerBlockPlacement(e);
            detectionManager.onPlace(e.getUser().getName(), place.getBlockPosition());
        }
    }

    /* 获取 Ping（兼容 1.7） */
    public static int getPing(Player p) {
        try {
            Object entityPlayer = SpigotReflectionUtil.getEntityPlayer(p);
            Method getPing = entityPlayer.getClass().getDeclaredMethod("getPing");
            return (int) getPing.invoke(entityPlayer);
        } catch (Exception ex) {
            return 0;
        }
    }
}
