package org.tnnova.aksmanager.aksmanager.entities;

import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetVM;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetVMs;
import com.azure.resourcemanager.containerservice.models.AgentPool;
import com.azure.resourcemanager.containerservice.models.KubernetesCluster;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.tnnova.aksmanager.aksmanager.Aksmanager;
import org.tnnova.aksmanager.aksmanager.AzureModal;

import java.util.ArrayList;

public class AzureVillager extends VillagerEntity {

    private KubernetesCluster kubernetesCluster;
    private VirtualMachineScaleSetVMs virtualMachineScaleSetVMs;
    private ArrayList<String> k8sMetadataStringList;


    public AzureVillager(EntityType<? extends VillagerEntity> entityType, World world) {
        super(entityType, world);
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
        sheep.setOwner(this);
        world.spawnEntity(sheep);
    }

    public void setVirtualMachineScaleSetVMs(VirtualMachineScaleSetVMs virtualMachineScaleSetVMs, AgentPool agentPool) {
        virtualMachineScaleSetVMs.list().forEach((instance) -> provisionAzureSheep(instance, agentPool));
        this.virtualMachineScaleSetVMs = virtualMachineScaleSetVMs;
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

    public void respawnKilledSheep(VirtualMachineScaleSetVM instance, AgentPool agentPool){
        provisionAzureSheep(instance, agentPool);
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
