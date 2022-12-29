package org.tnnova.aksmanager.aksmanager.mixins;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DeviceCodeCredential;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.compute.fluent.VirtualMachineScaleSetsClient;
import com.azure.resourcemanager.compute.fluent.VirtualMachinesClient;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSet;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSets;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.tnnova.aksmanager.aksmanager.models.AzureMan;

import java.util.HashMap;

@Mixin(VillagerEntity.class)
public class VillagerMixin {
    @Shadow
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        return ActionResult.PASS;
    }

    @Inject(method = "interactMob", at = @At("HEAD"), cancellable = true)
    public void interactMob(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> ci) {

        if (!player.world.isClient()) return;

        ItemStack itemStack = player.getStackInHand(hand);
        if (itemStack.getItem() == Items.TRIPWIRE_HOOK) {

            NbtCompound nbtCompound = itemStack.getNbt();
            String subId = nbtCompound.getString("subId");
            player.sendMessage(Text.literal(subId));

            AzureResourceManager.Authenticated azureResourceManager = AzureMan.azureResourceManager;

            if (azureResourceManager != null){

                VirtualMachineScaleSets vmss = azureResourceManager.withSubscription(subId).virtualMachineScaleSets();
                vmss.list().forEach((e) -> {
                    e.virtualMachines().list().forEach((instance) -> {
                        player.sendMessage(Text.literal(instance.computerName()));

                        SheepEntity sheep = new SheepEntity(EntityType.SHEEP , AzureMan.world);
                        sheep.updatePosition(player.getX(), player.getY(), player.getZ());
                        sheep.setColor(DyeColor.GREEN);
                        sheep.setCustomName(Text.literal(instance.computerName()));
                        AzureMan.world.spawnEntity(sheep);
                    });
                });

            }

            ci.setReturnValue(ActionResult.SUCCESS);
        }

    }
}

