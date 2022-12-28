package org.tnnova.aksmanager.aksmanager;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DeviceCodeCredential;
import com.azure.identity.DeviceCodeCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.resources.models.Subscription;
import net.fabricmc.api.ModInitializer;

import static net.minecraft.server.command.CommandManager.*;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Clearable;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class Aksmanager implements ModInitializer {

    private DeviceCodeCredential deviceCodeCredential;

    public DeviceCodeCredential getDeviceCodeCredentialWithMsg(ServerCommandSource player) {
        return new DeviceCodeCredentialBuilder()
                .challengeConsumer(challenge -> {
                    // Lets the user know about the challenge.
                    String deviceCodeCredentialMessage = challenge.getMessage();
                    String deviceCodeCredentialUrl = challenge.getVerificationUrl();
                    String deviceCodeCredentialUserCode = challenge.getUserCode();

                    player.sendMessage(Text.literal(deviceCodeCredentialMessage));
                    player.sendMessage(Text.literal("Click Here to Copy User Code!").styled(style -> style
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(deviceCodeCredentialMessage)))
                            .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, deviceCodeCredentialUserCode))
                            .withColor(Formatting.BLUE)
                            .withBold(true)));
                    player.sendMessage(Text.literal("Click Here for Azure Login!").styled(style -> style
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(deviceCodeCredentialMessage)))
                            .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, deviceCodeCredentialUrl))
                            .withColor(Formatting.AQUA)
                            .withBold(true)));
                }).build();
    }

    PlayerEntity getPlayer(MinecraftClient mc) {
        return Objects.requireNonNull(mc.player, "Player is null");
    }

    void setClientServerBlockState(MinecraftClient mc, BlockPos blockPos, BlockState blockState) {
        World overworldServer = mc.getServer().getOverworld();
        World overworldClient = mc.getServer().getOverworld();

        if (overworldServer != null) {
            BlockEntity blockEntity = overworldServer.getBlockEntity(blockPos);
            Clearable.clear(blockEntity);
            //2 = update clients
            overworldServer.setBlockState(blockPos, blockState, 2);
            overworldClient.updateNeighbors(blockPos, blockState.getBlock());
        }
    }

    void createPlatformAtPos(MinecraftClient mc, BlockPos blockPos, Integer xAxisLength, Integer zAxisLength) {
        for (int i = 0; i < xAxisLength; i++) {
            for (int j = 0; j < zAxisLength; j++) {
                setClientServerBlockState(mc, blockPos.add(i, 0, j), Blocks.COBBLESTONE.getDefaultState());
            }
        }
        System.out.printf("X: %d, Y: %d, Z: %d\n", blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }


    @Override
    public void onInitialize() {
        MinecraftClient mc = MinecraftClient.getInstance();


        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("azlogin")
                .executes(context -> {

                    deviceCodeCredential = getDeviceCodeCredentialWithMsg(context.getSource());

                    AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
                    //<T>.Authenticated is to get non subscription scoped resources, such as subscriptions themselves without providing withSubscriptionId
                    AzureResourceManager.Authenticated azure = AzureResourceManager
                            .authenticate(deviceCodeCredential, profile);

                    PagedIterable<Subscription> subList = azure.subscriptions().list();

                    AtomicInteger i = new AtomicInteger();

                    subList.forEach((e)-> {
                        context.getSource().getPlayer().sendMessage(Text.literal(e.displayName()));
                        BlockPos bpos = context.getSource().getPlayer().getBlockPos().add(i.get(),0,0);
                        mc.getServer().getOverworld().setBlockState(bpos, Blocks.OAK_SIGN.getDefaultState());
                        BlockEntity be = mc.getServer().getOverworld().getBlockEntity(bpos);
                        if (be instanceof SignBlockEntity) {
                            SignBlockEntity sign = (SignBlockEntity) be;
                            // Set the NBT data for the sign
                            sign.setTextOnRow(1, Text.literal(e.displayName()));
                            // Mark the sign as dirty so that it is saved and updated in the world
                            sign.markDirty();
                        }
                        i.getAndIncrement();
                    });
                    return 1;
                })));

       /* CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("listsubs")
                .executes(context -> {
                    AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
                    AzureResourceManager azure = AzureResourceManager
                            .authenticate(deviceCodeCredential, profile).withSubscription(profile.getSubscriptionId());
                    PagedIterable<Subscription> subList = azure.subscriptions().list();

*//*
                    VirtualMachineScaleSetsClient vmss = azure.virtualMachines().manager().serviceClient().getVirtualMachineScaleSets();
*//*

*//*
                    PagedIterable<VirtualMachineScaleSetInner> list = vmss.list();
*//*


                    subList.forEach((e) -> {
                        mc.player.sendMessage(Text.literal(e.displayName()));
                    });
                    return 1;
                })));*/

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("azlogins")
                .executes(context -> {
                    // For versions below 1.19, replace "Text.literal" with "new LiteralText".

                    PlayerEntity player = Objects.requireNonNull(context.getSource().getPlayer(), "Player not found");

                    Vec3d playerPos = player.getPos();

                    Integer platformLengthInBlocksX = 40;
                    Integer platformHeightInBlocksY = 200;
                    Integer platformLengthInBlocksZ = 200;





                    Vec3d centerOfPlatform = new Vec3d(playerPos.x + (platformLengthInBlocksX.floatValue() / 2), platformHeightInBlocksY + 1, playerPos.z + (platformLengthInBlocksZ.floatValue() / 2));

                    BlockPos platformPosVec3Dd = new BlockPos(player.getX(), platformHeightInBlocksY, player.getZ());
                    createPlatformAtPos(mc, platformPosVec3Dd, platformLengthInBlocksX, platformLengthInBlocksZ);
                    player.teleport(centerOfPlatform.x, centerOfPlatform.y, centerOfPlatform.z);

                    return 1;
                })));




        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("signplace")
                .executes(context -> {
                    // For versions below 1.19, replace "Text.literal" with "new LiteralText".
                    mc.getServer().getOverworld().setBlockState(context.getSource().getPlayer().getBlockPos(), Blocks.OAK_SIGN.getDefaultState(), 2);
                    BlockEntity be = mc.getServer().getOverworld().getBlockEntity(context.getSource().getPlayer().getBlockPos());
                    if (be instanceof SignBlockEntity) {
                        SignBlockEntity sign = (SignBlockEntity) be;
                        // Set the NBT data for the sign
                        sign.setTextOnRow(1, Text.literal("HELLO WORLD"));
                        // Mark the sign as dirty so that it is saved and updated in the world
                        sign.markDirty();
                    }
                    return 1;
                })));
    }
}