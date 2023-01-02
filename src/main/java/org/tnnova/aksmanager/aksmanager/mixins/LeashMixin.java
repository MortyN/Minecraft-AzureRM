package org.tnnova.aksmanager.aksmanager.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.village.VillagerProfession;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
public class LeashMixin {

    @Shadow @Nullable private Entity holdingEntity;

    @Inject(method = "detachLeash", at = @At("HEAD"), cancellable = true)
    public void detachLeash(boolean sendPacket, boolean dropItem, CallbackInfo ci) {
        if (this.holdingEntity != null && this.holdingEntity.getType() == EntityType.VILLAGER){
            return;
        }

    }
}
