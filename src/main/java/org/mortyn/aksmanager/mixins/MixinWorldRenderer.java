package org.mortyn.aksmanager.mixins;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.text.Text;
import org.mortyn.aksmanager.utils.Utils;
import org.mortyn.aksmanager.entities.AzureVillager;
import org.spongepowered.asm.mixin.Final;
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

import java.util.Objects;

@Mixin(WorldRenderer.class)
public abstract class MixinWorldRenderer {

    @Shadow protected abstract void renderEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers);

    @Shadow @Final private MinecraftClient client;

    @Inject(method = "renderEntity", at = @At("HEAD"))
    private void renderEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        if (entity instanceof AzureVillager && vertexConsumers instanceof OutlineVertexConsumerProvider) {

            OutlineVertexConsumerProvider outlineVertexConsumers = (OutlineVertexConsumerProvider) vertexConsumers;
            outlineVertexConsumers.setColor(119, 3, 252, 255);

        }
        if(entity instanceof WanderingTraderEntity && vertexConsumers instanceof OutlineVertexConsumerProvider){
            if (Objects.equals(entity.getName(), Text.literal(Utils.AZUREMAN))) {
                OutlineVertexConsumerProvider outlineVertexConsumers = (OutlineVertexConsumerProvider) vertexConsumers;
                outlineVertexConsumers.setColor(13, 106, 255, 255);
            }
        }
    }
}