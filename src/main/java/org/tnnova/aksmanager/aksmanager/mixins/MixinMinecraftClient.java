package org.tnnova.aksmanager.aksmanager.mixins;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.tnnova.aksmanager.aksmanager.client.AksmanagerClient;
import org.tnnova.aksmanager.aksmanager.entities.AzureSheep;
import org.tnnova.aksmanager.aksmanager.entities.AzureVillager;

import java.util.ArrayList;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient {
    @Shadow @Nullable public HitResult crosshairTarget;

    @Shadow @Nullable public ClientPlayerEntity player;

    @Shadow public abstract boolean hasOutline(Entity entity);

    @Shadow @Nullable public Entity targetedEntity;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = false)
    private void onTick(CallbackInfo ci){
        if(this.crosshairTarget != null){
            if(this.crosshairTarget.getType() == HitResult.Type.ENTITY){
                Entity target = ((EntityHitResult) this.crosshairTarget).getEntity();
                if(target != null){
                    if(target instanceof AzureVillager){
                        String str = "OI FAEN, DER KOM DET EN PAKKE GITT";
                        PacketByteBuf byteBuf = PacketByteBufs.create();
                        byteBuf.writeString(str);
                        ClientPlayNetworking.send(AksmanagerClient.TARGET_ENTITY_CHILDREN_ID, byteBuf);
                    }
                }
            }
        }
    }

    @Inject(method = "hasOutline", at = @At("HEAD"), cancellable = true)
    private void outlineEntities(Entity entity, CallbackInfoReturnable<Boolean> ci) {
//        if (EntityOutliner.outliningEntities && EntitySelector.outlinedEntityTypes != null) {
//            if (EntitySelector.outlinedEntityTypes.containsKey(entity.getType())) {
//            }
//        }

        /*    ci.setReturnValue(ci.getReturnValue());*/
    }
}