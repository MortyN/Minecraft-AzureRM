package org.tnnova.aksmanager.aksmanager;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Utils {
    public static WanderingTraderEntity spawnAzureMan(World world, BlockPos pos) {
        WanderingTraderEntity wanderingTraderEntity = EntityType.WANDERING_TRADER.create(world);
        wanderingTraderEntity.setPosition(pos.getX(), pos.getY(), pos.getZ());
        wanderingTraderEntity.setCustomName(Text.literal("Azure Man"));
        wanderingTraderEntity.setPersistent();
        world.spawnEntity(wanderingTraderEntity);
        return wanderingTraderEntity;
    }
}
