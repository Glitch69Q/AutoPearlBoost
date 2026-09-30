package com.example.autopearlboost;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public final class AutoPearlBoostClient implements ClientModInitializer {
    private static UUID trackedPearl;
    private static int cooldown;
    private static int ticksSinceThrow;

    private static final double LOOK_DOT = Math.cos(Math.toRadians(8.0));
    private static final double PEARL_GRAVITY = 0.03;
    private static final double PEARL_DRAG = 0.99;
    private static final double WIND_CHARGE_SPEED = 1.5;

    @Override
    public void onInitializeClient() {
        Config.load();
        ClientTickEvents.END_CLIENT_TICK.register(AutoPearlBoostClient::tick);
    }

    private static void tick(Minecraft client) {
        if (cooldown > 0) cooldown--;

        if (!Config.enabled
                || client.level == null
                || client.player == null
                || client.gameMode == null) {
            trackedPearl = null;
            ticksSinceThrow = 0;
            return;
        }

        LocalPlayer player = client.player;
        ThrownEnderpearl pearl = findOwnPearl(client, player);

        if (pearl == null) {
            trackedPearl = null;
            ticksSinceThrow = 0;
            return;
        }

        if (!pearl.getUUID().equals(trackedPearl)) {
            trackedPearl = pearl.getUUID();
            ticksSinceThrow = 0;
        } else {
            ticksSinceThrow++;
        }

        if (!Config.autoUse
                || cooldown > 0
                || ticksSinceThrow < Config.minimumTicksAfterThrow) {
            return;
        }

        // Only activate when the player is actually looking at the pearl.
        if (!isLookingAtPearl(player, pearl)) {
            return;
        }

        int windSlot = findWindChargeHotbarSlot(player);
        if (windSlot < 0) return;

        Vec3 target = findInterceptPoint(player, pearl);
        if (target == null) return;

        if (target.distanceTo(player.getEyePosition()) > Config.maxDistance) {
            return;
        }

        int oldSlot = player.getInventory().getSelectedSlot();
        float oldYaw = player.getYRot();
        float oldPitch = player.getXRot();

        try {
            player.getInventory().setSelectedSlot(windSlot);

            Vec3 aim = target.subtract(player.getEyePosition());

            double horizontal = Math.sqrt(
                    aim.x * aim.x + aim.z * aim.z
            );

            if (horizontal < 0.001) return;

            float yaw = (float) Math.toDegrees(
                    Math.atan2(-aim.x, aim.z)
            );

            float pitch = (float) Math.toDegrees(
                    Math.atan2(-aim.y, horizontal)
            );

            player.setYRot(yaw);
            player.setXRot(pitch);

            client.gameMode.useItem(
                    player,
                    InteractionHand.MAIN_HAND
            );

            cooldown = Math.max(1, Config.cooldownTicks);

        } finally {
            player.getInventory().setSelectedSlot(oldSlot);
            player.setYRot(oldYaw);
            player.setXRot(oldPitch);
        }
    }

    private static boolean isLookingAtPearl(
            LocalPlayer player,
            ThrownEnderpearl pearl
    ) {
        Vec3 eye = player.getEyePosition();
        Vec3 toPearl = pearl.position().subtract(eye);

        double distance = toPearl.length();

        if (distance < 0.001
                || distance > Config.maxDistance) {
            return false;
        }

        Vec3 lookDirection =
                player.getViewVector(1.0F).normalize();

        Vec3 pearlDirection =
                toPearl.normalize();

        double dot =
                lookDirection.dot(pearlDirection);

        return dot >= LOOK_DOT;
    }

    private static ThrownEnderpearl findOwnPearl(
            Minecraft client,
            LocalPlayer player
    ) {
        double radius = Config.maxDistance + 8.0;

        for (ThrownEnderpearl pearl : client.level.getEntitiesOfClass(
                ThrownEnderpearl.class,
                player.getBoundingBox().inflate(radius),
                p -> !p.isRemoved()
        )) {
            if (pearl.getOwner() == player) {
                return pearl;
            }

            if (pearl.getOwner() != null
                    && pearl.getOwner()
                    .getUUID()
                    .equals(player.getUUID())) {
                return pearl;
            }
        }

        return null;
    }

    private static int findWindChargeHotbarSlot(
            LocalPlayer player
    ) {
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack =
                    player.getInventory().getItem(slot);

            if (stack.is(Items.WIND_CHARGE)) {
                return slot;
            }
        }

        return -1;
    }

    private static Vec3 findInterceptPoint(
            LocalPlayer player,
            ThrownEnderpearl pearl
    ) {
        Vec3 origin = player.getEyePosition();
        Vec3 start = pearl.position();
        Vec3 velocity = pearl.getDeltaMovement();

        double bestScore = Double.MAX_VALUE;
        Vec3 best = null;

        for (
                int ticks = 1;
                ticks <= Config.predictionTicks;
                ticks++
        ) {
            Vec3 future =
                    predictPearl(
                            start,
                            velocity,
                            ticks
                    );

            double distance =
                    future.distanceTo(origin);

            if (distance < 1.0
                    || distance > Config.maxDistance) {
                continue;
            }

            double flightTicks =
                    distance / WIND_CHARGE_SPEED;

            double predictedTime =
                    ticks
                    + flightTicks
                    * Config.predictionStrength;

            Vec3 intercept =
                    predictPearl(
                            start,
                            velocity,
                            predictedTime
                    );

            double interceptDistance =
                    intercept.distanceTo(origin);

            if (interceptDistance < 1.0
                    || interceptDistance > Config.maxDistance) {
                continue;
            }

            double score =
                    Math.abs(
                            distance
                            - Config.preferredDistance
                    )
                    + Math.abs(
                            ticks
                            - Config.preferredPredictionTick
                    ) * 0.25
                    + Math.abs(
                            interceptDistance
                            - Config.preferredDistance
                    ) * 0.15;

            if (score < bestScore) {
                bestScore = score;
                best = intercept;
            }
        }

        return best;
    }

    private static Vec3 predictPearl(
            Vec3 position,
            Vec3 velocity,
            double ticks
    ) {
        Vec3 p = position;
        Vec3 v = velocity;

        int wholeTicks =
                (int) Math.floor(ticks);

        double fraction =
                ticks - wholeTicks;

        for (int i = 0; i < wholeTicks; i++) {
            p = p.add(v);

            v = v.scale(PEARL_DRAG)
                    .add(
                            0.0,
                            -PEARL_GRAVITY,
                            0.0
                    );
        }

        if (fraction > 0.0) {
            p = p.add(v.scale(fraction));
        }

        return p;
    }
}
