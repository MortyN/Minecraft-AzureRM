package org.tnnova.aksmanager.aksmanager.mixins;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VillagerEntity.class)
public abstract class VillagerMixin {
    @Shadow
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        return ActionResult.PASS;
    }

    @Shadow public abstract VillagerData getVillagerData();

    public VillagerMixin(EntityType<? extends VillagerEntity> type, World world, boolean spawnedByMod) {
        this.spawnedByMod = spawnedByMod;
    }


    private boolean spawnedByMod = false;
    @Inject(method = "interactMob", at = @At("HEAD"), cancellable = true)
    public void interactMob(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> ci) {
        if (!player.world.isClient()) return;
        VillagerProfession villagerProfession = getVillagerData().getProfession();
        ItemStack itemStack = player.getStackInHand(hand);
        if (itemStack.getItem() == Items.TRIPWIRE_HOOK && villagerProfession == VillagerProfession.NITWIT) {

            player.sendMessage(Text.literal("vmss: hi, im the virtual machine scale set master"));

            ci.setReturnValue(ActionResult.SUCCESS);
        }

    }
}

