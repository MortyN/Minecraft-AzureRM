package org.tnnova.aksmanager.aksmanager.mixins;

import com.azure.resourcemanager.compute.models.VirtualMachineScaleSets;
import com.azure.resourcemanager.containerservice.models.Code;
import com.azure.resourcemanager.containerservice.models.KubernetesClusters;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
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
import org.tnnova.aksmanager.aksmanager.entities.AzureVillager;
import org.tnnova.aksmanager.aksmanager.models.AzureMan;

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
        if (!player.world.isClient()){
            if (player.getStackInHand(hand).getItem() == Items.TRIPWIRE_HOOK) {
                ci.setReturnValue(ActionResult.PASS);
            }
            return;
        }

        World world = Aksmanager.MC.getServer().getOverworld();

        ItemStack itemStack = player.getStackInHand(hand);

        if (itemStack.getItem() == Items.TRIPWIRE_HOOK) {
            NbtCompound nbtCompound = itemStack.getNbt();
            if (nbtCompound == null) return;

            String subId = nbtCompound.getString("subId");
            player.sendMessage(Text.literal("Retrieving all clusters for subscription: " + itemStack.getName().getString()));

            Thread thread = new Thread(() -> {
                VirtualMachineScaleSets vmss = AzureMan.azureResourceManager.withSubscription(subId).virtualMachineScaleSets();
                KubernetesClusters clusters = AzureMan.azureResourceManager.withSubscription(subId).kubernetesClusters();
                AzureMan.setKubernetesClusters(clusters);
                AzureMan.setVirtualMachineScaleSets(vmss);

                vmss.list().forEach((vmsselement) -> {
                    clusters.list().forEach(cluster -> {
                        String vmsselementRGName = vmsselement.resourceGroupName().toUpperCase();
                        String clusterRGName = cluster.nodeResourceGroup().toUpperCase();

                        if (vmsselementRGName.equals(clusterRGName)) {
                            cluster.agentPools().forEach((s, agentpool) -> {
                                if (!vmsselement.name().contains(agentpool.name())) return;

                                String powerState = "(" + agentpool.count() + "/" + agentpool.nodeSize() + ")";
                                if (agentpool.powerState().code().equals(Code.STOPPED)) {
                                    powerState = "(0/" + agentpool.count() + ") STOPPED";
                                }

                                AzureVillager azureVillager = new AzureVillager(Aksmanager.AZURE_VILLAGER_ENTITY_TYPE, world);
                                azureVillager.setPosition(player.getX(), player.getY(), player.getZ());
                                azureVillager.setCustomName(Text.literal(cluster.name() + " ")
                                        .append(agentpool.name() + " " + powerState));
                                azureVillager.setKubernetesCluster(cluster);
                                azureVillager.setVirtualMachineScaleSetVMs(vmsselement.virtualMachines(), agentpool);
                                Aksmanager.azureVillagers.add(azureVillager);
                                world.spawnEntity(azureVillager);
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

