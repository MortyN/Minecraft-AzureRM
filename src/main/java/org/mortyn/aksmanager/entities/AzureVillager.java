package org.mortyn.aksmanager.entities;

import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetVM;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetVMs;
import com.azure.resourcemanager.containerservice.models.AgentPool;
import com.azure.resourcemanager.containerservice.models.Code;
import com.azure.resourcemanager.containerservice.models.KubernetesCluster;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.mortyn.aksmanager.Aksmanager;
import org.mortyn.aksmanager.screens.AzureModal;
import org.mortyn.aksmanager.utils.Utils;

import java.util.ArrayList;

public class AzureVillager extends VillagerEntity {

    private KubernetesCluster kubernetesCluster;
    private VirtualMachineScaleSetVMs virtualMachineScaleSetVMs;
    private ArrayList<String> k8sMetadataStringList;

    private ArrayList<AzureSheep> entityChildren;

    public AzureVillager(EntityType<? extends VillagerEntity> entityType, World world) {
        super(entityType, world);
        entityChildren = new ArrayList<>();
    }


    public void setKubernetesCluster(KubernetesCluster kubernetesCluster) {
        k8sMetadataStringList = new ArrayList<>();
        k8sMetadataStringList.add("Cluster: "+kubernetesCluster.name());
        k8sMetadataStringList.add("ResourceGroup: "+kubernetesCluster.resourceGroupName());
        k8sMetadataStringList.add("PowerState: "+kubernetesCluster.powerState());
        k8sMetadataStringList.add("NodepoolCount: "+kubernetesCluster.agentPools().size());
        k8sMetadataStringList.add("Region: "+kubernetesCluster.regionName());
        this.kubernetesCluster = kubernetesCluster;
    }

    private void provisionAzureSheep(VirtualMachineScaleSetVM instance, AgentPool agentPool){
        AzureSheep sheep = new AzureSheep(Aksmanager.AZURE_SHEEP_ENTITY_TYPE, world);
        sheep.updatePosition(this.getX(), this.getY(), this.getZ());
        sheep.setCustomName(Text.literal(instance.computerName()));
        sheep.attachLeash(this, true);
        sheep.setVirtualMachineScaleSetVM(instance);
        sheep.setAgentPool(agentPool);
        sheep.setOwnerEntity(this);
        sheep.setOwnerEntityName(this.getEntityName());
        entityChildren.add(sheep);
        world.spawnEntity(sheep);
    }

    private void provisionAzureSheep(AzureSheep azureSheep,VirtualMachineScaleSetVM instance, AgentPool agentPool){
        azureSheep.updatePosition(this.getX(), this.getY(), this.getZ());
        azureSheep.setCustomName(Text.literal(instance.computerName()));
        azureSheep.attachLeash(this, true);
        azureSheep.setVirtualMachineScaleSetVM(instance);
        azureSheep.setAgentPool(agentPool);
        azureSheep.setOwnerEntity(this);
        azureSheep.setOwnerEntityName(this.getEntityName());
        entityChildren.add(azureSheep);
        world.spawnEntity(azureSheep);
    }

    public ArrayList<AzureSheep> getEntityChildren() {
        return entityChildren;
    }

    public void setVirtualMachineScaleSetVMs(VirtualMachineScaleSetVMs virtualMachineScaleSetVMs, AgentPool agentPool) {
        virtualMachineScaleSetVMs.list().forEach((instance) -> provisionAzureSheep(instance, agentPool));
        this.virtualMachineScaleSetVMs = virtualMachineScaleSetVMs;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
    }

    @Override
    protected void mobTick() {
        if (!this.world.isClient()){
            if(kubernetesCluster == null){
                this.kill();
            }
        }
        super.mobTick();
    }

    private void startUpdateLoop(AzureSheep azureSheep, Code instancePowerCode, String provisioningState) {
        //boolean = !row.agentPool.some(a => a.powerState.code === "Stopped" || a.provisioningState === undefined || a.provisioningState === "Starting" || a.provisioningState === "Stopping"
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    Utils.updateVmPowerStateSheepColor(azureSheep ,instancePowerCode, provisioningState);
                    /*this.world.sendEntityStatus(this, (byte) 10);*/ //this means sheared.
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        thread.start();
    }

    public void respawnKilledSheep(VirtualMachineScaleSetVM instance, AgentPool agentPool, Entity killedBy){
        if (killedBy == null) return;

        killedBy.sendMessage(Text.literal("Rebooting " + instance.computerName() + "..."));
        Thread.UncaughtExceptionHandler h = (th, ex) -> {
            killedBy.sendMessage(Text.literal("Exception"+ ex));
        };
        AzureSheep sheep = new AzureSheep(Aksmanager.AZURE_SHEEP_ENTITY_TYPE, world);
        provisionAzureSheep(sheep, instance, agentPool);

        Thread t = new Thread(() -> {
/*
            instance.redeploy();
*/
            try {
                //Juks og fanteri
                Utils.updateVmPowerStateSheepColor(sheep, Code.STOPPED, "Succeeded");
                Thread.sleep(10000);
                Utils.updateVmPowerStateSheepColor(sheep, Code.STOPPED, "Updating");
                Thread.sleep(10000);
                Utils.updateVmPowerStateSheepColor(sheep, Code.RUNNING, "Succeeded");

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }


        });
        t.setUncaughtExceptionHandler(h);
        t.start();

    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {

        if (player.world.isClient()){
            Aksmanager.MC.setScreen(new AzureModal(Aksmanager.MC.getInstance().currentScreen));
            return super.interactMob(player, hand);
        }else{
            AzureModal.metadataStringList = k8sMetadataStringList;
            AzureModal.selectedAzureVillager = kubernetesCluster;
        }

        return super.interactMob(player, hand);
    }
}
