package org.tnnova.aksmanager.aksmanager.entities;

import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetVM;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class AzureSheep extends SheepEntity {

    private VirtualMachineScaleSetVM virtualMachineScaleSetVM = null;

    private String somenbt = "FIVEMILLION";

    private String writtennbt = null;

    public AzureSheep(EntityType<? extends SheepEntity> entityType, World worldIn) {
        super(entityType, worldIn);

    }

    private static final TrackedData<String> AZURE = DataTracker.registerData(
            AzureSheep.class, TrackedDataHandlerRegistry.STRING);


    public void setVirtualMachineScaleSetVM(VirtualMachineScaleSetVM virtualMachineScaleSetVM) {
        String data = virtualMachineScaleSetVM.id();
        NbtCompound nbtCompound = new NbtCompound();
        nbtCompound.putString("EPIC", data);
        writeNbt(nbtCompound);
        this.virtualMachineScaleSetVM = virtualMachineScaleSetVM;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        tag.putString("EPICGAMER", somenbt);

        super.writeNbt(tag);
        return tag;
    }
    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);

        this.writtennbt = tag.getString("EPICGAMER");
    }

    @Override
    public void detachLeash(boolean sendPacket, boolean dropItem) {
        super.detachLeash(false, false);
    }

    @Override
    public ActionResult interactAt(PlayerEntity player, Vec3d hitPos, Hand hand) {
        if (player.world.isClient()) return super.interactAt(player, hitPos, hand);

        if (virtualMachineScaleSetVM != null){
            player.sendMessage(Text.literal(writtennbt));
        }

        return super.interactAt(player, hitPos, hand);
    }

    @Override
    public void setMovementSpeed(float movementSpeed) {
        super.setMovementSpeed(0);
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (player.world.isClient()) return super.interactMob(player, hand);

        if (virtualMachineScaleSetVM != null){
            player.sendMessage(Text.literal(writtennbt));
        }

        return super.interactMob(player, hand);
    }

}

