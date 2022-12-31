package org.tnnova.aksmanager.aksmanager;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DeviceCodeCredential;
import com.azure.identity.DeviceCodeCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.resources.models.Subscription;
import net.fabricmc.api.ModInitializer;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;

import static net.minecraft.server.command.CommandManager.*;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.block.*;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import org.tnnova.aksmanager.aksmanager.models.AzureMan;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class Aksmanager implements ModInitializer {

    public static final String SUBSCRIPTIONID = "subId";
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

/*
    void setClientServerBlockState(MinecraftClient mc, BlockPos blockPos, BlockState blockState) {
        World overworldServer = mc.getServer().getOverworld();

        if (overworldServer != null) {
            BlockEntity blockEntity = overworldServer.getBlockEntity(blockPos);
            Clearable.clear(blockEntity);
            //2 = update clients
            overworldServer.setBlockState(blockPos, blockState, 2);
        }
    }
*/

    void createPlatformAtPos(MinecraftClient mc, BlockPos blockPos, Integer xAxisLength, Integer zAxisLength) {
        for (int i = 0; i < xAxisLength; i++) {
            for (int j = 0; j < zAxisLength; j++) {
                mc.getServer().getOverworld().setBlockState(blockPos, Blocks.COBBLESTONE.getDefaultState(), Block.NOTIFY_LISTENERS);
            }
        }
        System.out.printf("X: %d, Y: %d, Z: %d\n", blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }


    @Override
    public void onInitialize() {

        MinecraftClient mc = MinecraftClient.getInstance();
        AzureMan azureMan = new AzureMan();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("azlogin")
                .executes(context -> {

                    deviceCodeCredential = getDeviceCodeCredentialWithMsg(context.getSource());

                    AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
                    //<T>.Authenticated is to get non subscription scoped resources, such as subscriptions themselves without providing withSubscriptionId
                    AzureResourceManager.Authenticated azure = AzureResourceManager
                            .authenticate(deviceCodeCredential, profile);

                    azureMan.setAzureResourceManager(azure);

                    PagedIterable<Subscription> subList = azure.subscriptions().list();

                    World world = mc.getServer().getOverworld();

                    azureMan.setWorld(world);

                    AtomicInteger i = new AtomicInteger();

                    BlockPos blockPos = context.getSource().getPlayer().getBlockPos().add(1,0,0);
                    mc.getServer().getOverworld().setBlockState(blockPos, Blocks.CHEST.getDefaultState(), Block.NOTIFY_LISTENERS);

                    subList.forEach((e)-> {
                        NbtCompound compound = new NbtCompound();
                        compound.putString(Aksmanager.SUBSCRIPTIONID,e.subscriptionId());
                        Inventory inventory = ((ChestBlockEntity) mc.getServer().getOverworld().getBlockEntity(blockPos));

                        //Test with: /data get entity @s SelectedItem
                        ItemStack tripWireKey = new ItemStack(Items.TRIPWIRE_HOOK, 1);
                        tripWireKey.setNbt(compound);
                        tripWireKey.setCustomName(Text.literal(e.displayName()));
                        inventory.setStack(i.intValue(), tripWireKey);


                        i.getAndIncrement();
                    });
                    WanderingTraderEntity wanderingTraderEntity = Utils.spawnAzureMan(mc.getServer().getOverworld(), blockPos.add(0,1,0));
                    AzureMan.setWanderingTraderEntity(wanderingTraderEntity);
                    return 1;
                })));

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

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("sheepspawn")
                .executes(context -> {
                    SheepEntity sheep = new SheepEntity(EntityType.SHEEP ,mc.getServer().getOverworld());
                    Vec3d playerPos = context.getSource().getPlayer().getPos();
                    sheep.updatePosition(context.getSource().getPlayer().getX(), context.getSource().getPlayer().getY(), context.getSource().getPlayer().getZ());
                    sheep.setColor(DyeColor.GREEN);
                    sheep.setCustomName(Text.literal("moo, im a sheep"));

                    if (context.getSource().getWorld().isClient()){
                        context.getSource().getPlayer().sendMessage(Text.literal("spawned by player"));
                    }

                    mc.getServer().getOverworld().spawnEntity(sheep);
                    return 1;
                })));




    }
}
