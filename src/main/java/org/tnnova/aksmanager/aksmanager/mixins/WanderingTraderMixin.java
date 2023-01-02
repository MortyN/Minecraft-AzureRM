package org.tnnova.aksmanager.aksmanager.mixins;

import com.azure.resourcemanager.compute.models.VirtualMachineScaleSets;
import com.azure.resourcemanager.containerservice.models.KubernetesClusters;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.tnnova.aksmanager.aksmanager.Aksmanager;
import org.tnnova.aksmanager.aksmanager.Utils;
import org.tnnova.aksmanager.aksmanager.entities.AzureSheep;
import org.tnnova.aksmanager.aksmanager.models.AzureMan;

import java.util.ArrayList;

@Mixin(WanderingTraderEntity.class)
public abstract class WanderingTraderMixin {
    @Shadow
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        return ActionResult.PASS;
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("HEAD"))
    private void readCustomDataFromTag(NbtCompound tag, CallbackInfo ci) {
        if (tag.contains("hello_world")) {
            String helloWorld = tag.getString("hello_world");
            // do something with the hello world string
        }
    }

    @Inject(method = "interactMob", at = @At("HEAD"), cancellable = true)
    public void interactMob(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> ci) {
        if (!player.world.isClient()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        World world = mc.getServer().getOverworld();

        ItemStack itemStack = player.getStackInHand(hand);

        if (itemStack.getItem() == Items.TRIPWIRE_HOOK) {
            NbtCompound nbtCompound = itemStack.getNbt();
            String subId = nbtCompound.getString("subId");
            player.sendMessage(Text.literal(subId));

            Thread thread = new Thread(() -> {
                VirtualMachineScaleSets vmss = AzureMan.azureResourceManager.withSubscription(subId).virtualMachineScaleSets();

                KubernetesClusters clusters = AzureMan.azureResourceManager.withSubscription(subId).kubernetesClusters();
                ArrayList<VillagerEntity> villagerEntities = new ArrayList<>();
                vmss.list().forEach((vmsselement) -> {
                    clusters.list().forEach(cluster -> {
                        String vmsselementRGName = vmsselement.resourceGroupName().toUpperCase();
                        String clusterRGName = cluster.nodeResourceGroup().toUpperCase();

                        if (vmsselementRGName.equals(clusterRGName)){
                            VillagerEntity villagerEntity = EntityType.VILLAGER.create(world);
                            villagerEntity.setPosition(player.getX(), player.getY(), player.getZ());
                            villagerEntity.setCustomName(Text.literal(cluster.name()));
                            world.spawnEntity(villagerEntity);
                            villagerEntities.add(villagerEntity);

                            vmsselement.virtualMachines().list().forEach((instance) -> {
                                DyeColor sheepPowerState = Utils.updateVmProvisioningStateSheepColor(instance.powerState());

                                AzureSheep sheep = new AzureSheep(Aksmanager.AZURE_SHEEP_ENTITY_TYPE, world);
                                sheep.updatePosition(player.getX(), player.getY(), player.getZ());
                                sheep.setColor(sheepPowerState);
                                sheep.setCustomName(Text.literal(instance.computerName()));
                                sheep.attachLeash(villagerEntity, true);
                                sheep.setVirtualMachineScaleSetVM(instance);
                                world.spawnEntity(sheep);
                            });
                        }
                    });
                });

            });

            if (AzureMan.azureResourceManager == null) {
                AzureMan.azureResourceManager = Utils.getAzureAuth(player);
            }

            thread.start();

            ci.cancel();
        }
        ci.setReturnValue(ActionResult.SUCCESS);
    }
}

