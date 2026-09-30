package com.example.autopearlboost;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public final class AutoPearlBoostClient implements ClientModInitializer {
    private static UUID trackedPearl;
    private static int cooldown;
    private static int ticksSinceThrow;
    private static int lastPearlId = -1;

    @Override
    public void onInitializeClient() {
        Config.load();
        ClientTickEvents.END_CLIENT_TICK.register(AutoPearlBoostClient::tick);
    }

    private static void tick(Minecraft client) {
        if (cooldown > 0) cooldown--;
        if (!Config.enabled || client.level == null || client.player == null || client.gameMode == null) {
            trackedPearl = null;
            ticksSinceThrow = 0;
            return;
        }

        LocalPlayer player = client.player;

        ThrownEnderpearl pearl = findOwnPearl(client, player);
        if (pearl == null) {
            if (trackedPearl != null && ticksSinceThrow > 0) {
                trackedPearl = null;
                ticksSinceThrow = 0;
            }
            return;
        }

        if (!pearl.getUUID().equals(trackedPearl)) {
            trackedPearl = pearl.getUUID();
            ticksSinceThrow = 0;
        } else {
            ticksSinceThrow++;
        }

        if (!Config.autoUse || cooldown > 0 || ticksSinceThrow < Config.minimumTicksAfterThrow) return;

        int windSlot = findWindChargeHotbarSlot(player);
        if (windSlot < 0) return;

        Vec3 target = findInterceptPoint(player, pearl);
        if (target == null) return;

        if (target.distanceTo(player.getEyePosition()) > Config.maxDistance) return;

        int oldSlot = player.getInventory().getSelectedSlot();
        float oldYaw = player.getYRot();
        float oldPitch = player.getXRot();

        try {
            player.getInventory().setSelectedSlot(windSlot);

            Vec3 aim = target.subtract(player.getEyePosition());
            double horizontal = Math.sqrt(aim.x * aim.x + aim.z * aim.z);
            float yaw = (float)(Math.toDegrees(Math.atan2(-aim.x, aim.z)));
            float pitch = (float)(Math.toDegrees(Math.atan2(-aim.y, horizontal)));

            player.setYRot(yaw);
            player.setXRot(pitch);

            client.gameMode.useItem(player, client.level, InteractionHand.MAIN_HAND);
            cooldown = Config.cooldownTicks;
        } finally {
            player.getInventory().setSelectedSlot(oldSlot);
            player.setYRot(oldYaw);
            player.setXRot(oldPitch);
        }
    }

    private static ThrownEnderpearl findOwnPearl(Minecraft client, LocalPlayer player) {
        double radius = Config.maxDistance + 8.0;
        for (ThrownEnderpearl pearl : client.level.getEntitiesOfClass(
                ThrownEnderpearl.class,
                player.getBoundingBox().inflate(radius),
                p -> !p.isRemoved())) {
            if (pearl.getOwner() == player) return pearl;
            if (pearl.getOwner() != null && pearl.getOwner().getUUID().equals(player.getUUID())) return pearl;
        }
        return null;
    }

    private static int findWindChargeHotbarSlot(LocalPlayer player) {
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(Items.WIND_CHARGE)) return slot;
        }
        return -1;
    }

    private static Vec3 findInterceptPoint(LocalPlayer player, ThrownEnderpearl pearl) {
        Vec3 p = pearl.position();
        Vec3 v = pearl.getDeltaMovement();
        Vec3 origin = player.getEyePosition();

        double bestScore = Double.MAX_VALUE;
        Vec3 best = null;

        // Search future pearl positions. The wind charge is a projectile, so we
        // choose a lead point rather than simply aiming at the pearl's current position.
        for (int ticks = 1; ticks <= Config.predictionTicks; ticks++) {
            double t = ticks;
            double gravity = 0.03 * t * t * 0.5;
            Vec3 future = p.add(v.scale(t)).add(0, -gravity, 0);

            double distance = future.distanceTo(origin);
            if (distance < 1.0 || distance > Config.maxDistance) continue;

            // Estimate wind-charge flight time using its approximate vanilla launch speed.
            double flight = distance / 1.5;
            Vec3 lead = p.add(v.scale(t + flight * Config.predictionStrength))
                    .add(0, -0.03 * (t + flight * Config.predictionStrength) * (t + flight * Config.predictionStrength) * 0.5, 0);

            double score = Math.abs(distance - Config.preferredDistance)
                    + Math.abs(t - Config.preferredPredictionTick) * 0.25;

            if (score < bestScore) {
                bestScore = score;
                best = lead;
            }
        }
        return best;
    }
}
