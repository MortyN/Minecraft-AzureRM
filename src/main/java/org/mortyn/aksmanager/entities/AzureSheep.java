package org.mortyn.aksmanager.entities;

import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetVM;
import com.azure.resourcemanager.containerservice.models.AgentPool;
import com.azure.resourcemanager.containerservice.models.Code;
import com.azure.resourcemanager.network.models.LoadBalancer;
import com.azure.resourcemanager.network.models.Network;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.mortyn.aksmanager.Aksmanager;
import org.mortyn.aksmanager.client.AksmanagerClient;
import org.mortyn.aksmanager.screens.AzureModal;
import org.mortyn.aksmanager.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;

public class AzureSheep extends SheepEntity {

    private VirtualMachineScaleSetVM virtualMachineScaleSetVM = null;

    private AgentPool agentPool = null;

    private boolean hasInit = false;

    private AzureVillager owner;

    ArrayList<String> vmMetadataStringList;

    private String ownerEntityName;

    public AzureSheep(EntityType<? extends SheepEntity> entityType, World worldIn) {
        super(entityType, worldIn);
        if (!this.world.isClient){
            Aksmanager.azureSheepHashMap.put(this.getEntityName(), this);
        }
        setColor(DyeColor.GREEN);
    }



    @Override
    protected boolean shouldDropLoot() {
        return false;
    }

    @Override
    public boolean isSheared() {
        if (this.world.isClient()) return super.isSheared();
        return super.isSheared();
    }

    public VirtualMachineScaleSetVM getVirtualMachineScaleSetVM() {
        return virtualMachineScaleSetVM;
    }

    public void setAgentPool(AgentPool agentPool) {
        this.agentPool = agentPool;
    }

    public void setOwnerEntity(AzureVillager owner) {
        this.owner = owner;
    }

    public void setOwnerEntityName(String ownerEntityName){
        this.ownerEntityName = ownerEntityName;
    }

    public String getOwnerName(){
        return ownerEntityName;
    }

    public void setVirtualMachineScaleSetVM(VirtualMachineScaleSetVM virtualMachineScaleSetVM) {
        vmMetadataStringList = new ArrayList<>();
        vmMetadataStringList.add("VMSS: "+virtualMachineScaleSetVM.parent().name());
        vmMetadataStringList.add("Node: "+virtualMachineScaleSetVM.computerName());
        vmMetadataStringList.add("VMSize: "+virtualMachineScaleSetVM.size());
        vmMetadataStringList.add("PowerState: "+virtualMachineScaleSetVM.powerState());
        vmMetadataStringList.add("ProvisioningState: "+virtualMachineScaleSetVM.innerModel().provisioningState());
        vmMetadataStringList.add("ResourceGroup: "+virtualMachineScaleSetVM.parent().resourceGroupName());
        vmMetadataStringList.add("Region: "+virtualMachineScaleSetVM.parent().regionName());
        try {
            Network network = virtualMachineScaleSetVM.parent().getPrimaryNetwork();
            LoadBalancer loadBalancer = virtualMachineScaleSetVM.parent().getPrimaryInternalLoadBalancer();
            vmMetadataStringList.add("VNET: "+network.name());
            if (loadBalancer != null){
                loadBalancer.frontends().forEach((str, lb) ->{
                    String id = lb.innerModel().subnet().id();
                    vmMetadataStringList.add("   Subnet: "+id.split("/")[10]);
                    vmMetadataStringList.add("      ILB: "+lb.innerModel().privateIpAddress());
                });
            }
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
        vmMetadataStringList.add("InstanceId: "+virtualMachineScaleSetVM.instanceId());
        vmMetadataStringList.add("OSDiskSize: "+virtualMachineScaleSetVM.osDiskSizeInGB()+"gb");
        this.virtualMachineScaleSetVM = virtualMachineScaleSetVM;
    }

    @Override
    public void detachLeash(boolean sendPacket, boolean dropItem) {
        super.detachLeash(false, false);
    }

    @Override
    public ActionResult interactAt(PlayerEntity player, Vec3d hitPos, Hand hand) {

        return super.interactAt(player, hitPos, hand);
    }

    @Override
    public void setMovementSpeed(float movementSpeed) {
        super.setMovementSpeed(0);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        return super.writeNbt(nbt);
    }

    @Override
    protected void mobTick() {
        if (!this.world.isClient()){
            if(virtualMachineScaleSetVM == null){
                this.kill();
            }
        }
        super.mobTick();
    }

    @Override
    protected void onKilledBy(@Nullable LivingEntity adversary) {
        if (virtualMachineScaleSetVM!=null) owner.respawnKilledSheep(virtualMachineScaleSetVM, agentPool, adversary);
        super.onKilledBy(adversary);
    }



    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (player.world.isClient()){
            Aksmanager.MC.setScreen(new AzureModal(Aksmanager.MC.getInstance().currentScreen));
            return super.interactMob(player, hand);
        }else{
            AzureModal.metadataStringList = vmMetadataStringList;
            AzureModal.selectedAzureSheep = virtualMachineScaleSetVM;
        }

        return super.interactMob(player, hand);
    }

}

