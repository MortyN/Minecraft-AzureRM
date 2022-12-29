package org.tnnova.aksmanager.aksmanager.mixins;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ButtonBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.tnnova.aksmanager.aksmanager.models.AzureButtonPos;
import org.tnnova.aksmanager.aksmanager.models.AzureButtonPosList;

import java.util.ArrayList;
import java.util.Objects;

@Mixin(ButtonBlock.class)
public abstract class AzureButtonBlock {
    @Inject(method = "onUse", at = @At("HEAD"))
    private void onButtonUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> ci) {
        if (state.getBlock() == Blocks.OAK_BUTTON) {
           ArrayList<AzureButtonPos> list =  AzureButtonPosList.getAzureButtonPosArrayList();
            BlockEntity blockEntity;
           list.forEach((e) -> {
               if (Objects.equals(e.getBlockPos(),pos)){
                   player.sendMessage(Text.literal(e.getSub().displayName()));
               }
           });

        }

    }


}
