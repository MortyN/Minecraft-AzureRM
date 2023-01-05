package org.tnnova.aksmanager.aksmanager.entities;

import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetVM;
import com.azure.resourcemanager.containerservice.models.AgentPool;
import com.azure.resourcemanager.containerservice.models.Code;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class AzureSheep extends SheepEntity {

    private VirtualMachineScaleSetVM virtualMachineScaleSetVM = null;

    private AgentPool agentPool = null;

    private boolean hasInit = false;

    public AzureSheep(EntityType<? extends SheepEntity> entityType, World worldIn) {
        super(entityType, worldIn);
        setColor(DyeColor.GRAY);
    }

    private void updateVmPowerStateSheepColor(AgentPool agentPool) {
        DyeColor dyeColor = DyeColor.GRAY;
        NbtCompound nbtCompound = new NbtCompound();
        if (agentPool == null) {
            setColor(dyeColor);
            writeCustomDataToNbt(nbtCompound);
            return;
        }
        Code code = agentPool.powerState().code();
        if (Code.RUNNING.equals(code)) {
            dyeColor = DyeColor.GREEN;
        } else if (Code.STOPPED.equals(code)) {
            dyeColor = DyeColor.RED;
        }

/*        PowerState powerState = virtualMachineScaleSetVM.powerState();
if (PowerState.RUNNING.equals(powerState)) {
            dyeColor = DyeColor.GREEN;
        } else if (PowerState.STARTING.equals(powerState)) {
            dyeColor = DyeColor.ORANGE;
        } else if (PowerState.STOPPING.equals(powerState)) {
            dyeColor = DyeColor.PURPLE;
        } else if (PowerState.STOPPED.equals(powerState)) {
            dyeColor = DyeColor.RED;
        }*/

        writeCustomDataToNbt(nbtCompound);
        setColor(dyeColor);

    }

    @Override
    public boolean isSheared() {
        if (this.world.isClient()) return super.isSheared();
        if (!hasInit){
            startUpdateLoop();
            hasInit = true;
        }
        return super.isSheared();
    }

    private void startUpdateLoop() {
        //boolean = !row.agentPool.some(a => a.powerState.code === "Stopped" || a.provisioningState === undefined || a.provisioningState === "Starting" || a.provisioningState === "Stopping"
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    updateVmPowerStateSheepColor(agentPool);
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        thread.start();
    }

    public void setAgentPool(AgentPool agentPool) {
        this.agentPool = agentPool;
    }

    public void setVirtualMachineScaleSetVM(VirtualMachineScaleSetVM virtualMachineScaleSetVM) {
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
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (player.world.isClient()) return super.interactMob(player, hand);

        Thread thread = new Thread(() -> {

            while (true) {
                this.setColor(DyeColor.RED);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                this.setColor(DyeColor.YELLOW);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                this.setColor(DyeColor.GREEN);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }

        });

        thread.start();

        return super.interactMob(player, hand);
    }

}

