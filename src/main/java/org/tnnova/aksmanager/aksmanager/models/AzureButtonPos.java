package org.tnnova.aksmanager.aksmanager.models;

import com.azure.resourcemanager.resources.models.Subscription;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;

public class AzureButtonPos {
    private BlockPos blockPos;
    private Subscription sub;

    public AzureButtonPos(BlockPos blockPos, Subscription sub) {
        this.blockPos = blockPos;
        this.sub = sub;
    }

    public void setBlockPos(BlockPos blockPos) {
        this.blockPos = blockPos;
    }

    public void setSub(Subscription sub) {
        this.sub = sub;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public Subscription getSub() {
        return sub;
    }
}
