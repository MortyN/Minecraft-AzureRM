package org.tnnova.aksmanager.aksmanager;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DeviceCodeCredential;
import com.azure.identity.DeviceCodeCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.resources.models.Subscription;
import net.fabricmc.api.ModInitializer;

import net.minecraft.nbt.NbtCompound;

import static net.minecraft.server.command.CommandManager.*;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import org.tnnova.aksmanager.aksmanager.models.AzureButtonPos;
import org.tnnova.aksmanager.aksmanager.models.AzureButtonPosList;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class Aksmanager implements ModInitializer {
    public static final Block EXAMPLE_BLOCK = new Block(FabricBlockSettings.of(Material.METAL).strength(4.0f));
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
        AzureButtonPos azureButtonPos = null;
        AzureButtonPosList azureButtonPosArrayList = new AzureButtonPosList();

        Registry.register(Registries.BLOCK, new Identifier("tutorial", "example_block"), EXAMPLE_BLOCK);
        Registry.register(Registries.ITEM, new Identifier("tutorial", "example_block"), new BlockItem(EXAMPLE_BLOCK, new FabricItemSettings()));

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("azlogin")
                .executes(context -> {

                    deviceCodeCredential = getDeviceCodeCredentialWithMsg(context.getSource());

                    AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
                    //<T>.Authenticated is to get non subscription scoped resources, such as subscriptions themselves without providing withSubscriptionId
                    AzureResourceManager.Authenticated azure = AzureResourceManager
                            .authenticate(deviceCodeCredential, profile);

                    PagedIterable<Subscription> subList = azure.subscriptions().list();

                    World world = mc.getServer().getOverworld();

                    AtomicInteger i = new AtomicInteger();

                    BlockPos blockPos = context.getSource().getPlayer().getBlockPos().add(1,0,0);
                    mc.getServer().getOverworld().setBlockState(blockPos, Blocks.CHEST.getDefaultState(), Block.NOTIFY_LISTENERS);

                    subList.forEach((e)-> {
                        NbtCompound compound = new NbtCompound();
                        compound.putString(e.displayName(),e.subscriptionId());
                        Inventory inventory = ((ChestBlockEntity) mc.getServer().getOverworld().getBlockEntity(blockPos));

                        //Test with: /data get entity @s SelectedItem
                        ItemStack tripWireKey = new ItemStack(Items.TRIPWIRE_HOOK, 1);
                        tripWireKey.setNbt(compound);
                        tripWireKey.setCustomName(Text.literal(e.displayName()));
                        inventory.setStack(i.intValue(), tripWireKey);

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




        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("createchest")
                .executes(context -> {
                    // For versions below 1.19, replace "Text.literal" with "new LiteralText".
                    BlockPos blockPos = context.getSource().getPlayer().getBlockPos().add(1,0,0);
                    mc.getServer().getOverworld().setBlockState(blockPos, Blocks.CHEST.getDefaultState(), Block.NOTIFY_LISTENERS);
                    NbtCompound compound = new NbtCompound();
                    compound.putString("d-nexp-sub","e9b12c4f-b4b6-4e5b-94ab-032824a151ba");
                    Inventory inventory = ((ChestBlockEntity) mc.getServer().getOverworld().getBlockEntity(blockPos));

                    // Add 3 oak wood to the chest
                    ItemStack oakWood = new ItemStack(Items.OAK_PLANKS, 3);
                    oakWood.setNbt(compound);
                    inventory.setStack(1, oakWood);

                    return 1;
                })));
    }
}
