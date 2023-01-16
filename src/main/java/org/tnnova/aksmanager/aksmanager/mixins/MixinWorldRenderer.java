package org.tnnova.aksmanager.aksmanager.mixins;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.AbstractTeam;
import org.tnnova.aksmanager.aksmanager.client.AksmanagerClient;
import org.tnnova.aksmanager.aksmanager.entities.AzureVillager;
import org.tnnova.aksmanager.aksmanager.enums.Color;

@Mixin(WorldRenderer.class)
public abstract class MixinWorldRenderer {

    @Shadow protected abstract void renderEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers);

    @Inject(method = "renderEntity", at = @At("HEAD"))
    private void renderEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        if (entity instanceof AzureVillager && vertexConsumers instanceof OutlineVertexConsumerProvider) {

            OutlineVertexConsumerProvider outlineVertexConsumers = (OutlineVertexConsumerProvider) vertexConsumers;
            outlineVertexConsumers.setColor(13, 106, 255, 255);

            System.out.println(AksmanagerClient.IAMSHARED);
            if (entity.getType() == EntityType.PLAYER) {
                PlayerEntity player = (PlayerEntity) entity;
                AbstractTeam team = player.getScoreboardTeam();
                if (team != null && team.getColor().getColorValue() != null) {
                    int hexColor = team.getColor().getColorValue();
                    int blue = hexColor % 256;
                    int green = (hexColor / 256) % 256;
                    int red = (hexColor / 65536) % 256;
                    outlineVertexConsumers.setColor(red, green, blue, 255);
                }
            }
        }
    }
}