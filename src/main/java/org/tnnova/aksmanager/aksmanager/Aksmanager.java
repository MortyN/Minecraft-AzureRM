package org.tnnova.aksmanager.aksmanager;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DeviceCodeCredential;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.resources.models.Subscription;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.channel.ChannelHandler;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
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
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import org.tnnova.aksmanager.aksmanager.client.AksmanagerClient;
import org.tnnova.aksmanager.aksmanager.entities.AzureSheep;
import org.tnnova.aksmanager.aksmanager.entities.AzureVillager;
import org.tnnova.aksmanager.aksmanager.models.AzureMan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class Aksmanager implements ModInitializer {

    public static final String SUBSCRIPTIONID = "subId";
    public static final String OWNEDBYAKSMANAGER = "ownedbyaksmanager";
    public static MinecraftClient MC;
    public static MinecraftClient CLIENTMC;
    private DeviceCodeCredential deviceCodeCredential;

    public static ArrayList<AzureVillager> azureVillagers;

    void createPlatformAtPos(MinecraftClient mc, BlockPos blockPos, Integer xAxisLength, Integer zAxisLength) {
        for (int i = 0; i < xAxisLength; i++) {
            for (int j = 0; j < zAxisLength; j++) {
                mc.getServer().getOverworld().setBlockState(blockPos, Blocks.COBBLESTONE.getDefaultState(), Block.NOTIFY_LISTENERS);
            }
        }
        System.out.printf("X: %d, Y: %d, Z: %d\n", blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    public static final EntityType<AzureSheep> AZURE_SHEEP_ENTITY_TYPE = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier("azureentity", "sheep"),
            FabricEntityTypeBuilder.create(SpawnGroup.AMBIENT, AzureSheep::new).dimensions(EntityDimensions.fixed(0.9f, 1.3f)).build()
    );

    public static final EntityType<AzureVillager> AZURE_VILLAGER_ENTITY_TYPE = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier("azureentity", "villager"),
            FabricEntityTypeBuilder.create(SpawnGroup.AMBIENT, AzureVillager::new).dimensions(EntityDimensions.fixed(0.6f, 1.95f)).build()
    );


    public static HashMap<String, AzureSheep> azureSheepHashMap;

    public static ArrayList<String> markedAzureSheep;

    public static Identifier TARGETED_AZURE_VILLAGER = new Identifier("azureentity", "targetedvillager");

    @Override
    public void onInitialize() {
        azureSheepHashMap = new HashMap<>();
        azureVillagers = new ArrayList<>();

        MC = MinecraftClient.getInstance();

        ServerPlayNetworking.registerGlobalReceiver(TARGETED_AZURE_VILLAGER, (server, player, handler, buf, responseSender) -> {
            String targetedVillagerName = buf.readString();
            markedAzureSheep = new ArrayList<>();
            azureSheepHashMap.forEach((uuid, entity) -> {
                AzureSheep azureSheep = (AzureSheep) entity;
                String sheepOwnerName = azureSheep.getOwnerName();
                if (Objects.equals(sheepOwnerName, targetedVillagerName)){
                    markedAzureSheep.add(azureSheep.getEntityName());
                }
            });
        });



        FabricDefaultAttributeRegistry.register(AZURE_SHEEP_ENTITY_TYPE, AzureSheep.createMobAttributes());
        FabricDefaultAttributeRegistry.register(AZURE_VILLAGER_ENTITY_TYPE, AzureSheep.createMobAttributes());

        AzureMan azureMan = new AzureMan();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("azlogin")
                .executes(context -> {

                    deviceCodeCredential = Utils.getDeviceCodeCredentialWithMsg(context.getSource().getPlayer());

                    AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
                    //<T>.Authenticated is to get non subscription scoped resources, such as subscriptions themselves without providing withSubscriptionId
                    AzureResourceManager.Authenticated azure = AzureResourceManager
                            .authenticate(deviceCodeCredential, profile);

                    azureMan.setAzureResourceManager(azure);

                    PagedIterable<Subscription> subList = azure.subscriptions().list();

                    World world = MC.getServer().getOverworld();

                    azureMan.setWorld(world);

                    AtomicInteger i = new AtomicInteger();

                    BlockPos blockPos = context.getSource().getPlayer().getBlockPos().add(1, 0, 0);
                    MC.getServer().getOverworld().setBlockState(blockPos, Blocks.CHEST.getDefaultState(), Block.NOTIFY_LISTENERS);

                    subList.forEach((e) -> {
                        NbtCompound compound = new NbtCompound();
                        compound.putString(Aksmanager.SUBSCRIPTIONID, e.subscriptionId());
                        Inventory inventory = ((ChestBlockEntity) MC.getServer().getOverworld().getBlockEntity(blockPos));

                        //Test with: /data get entity @s SelectedItem
                        ItemStack tripWireKey = new ItemStack(Items.TRIPWIRE_HOOK, 1);
                        tripWireKey.setNbt(compound);
                        tripWireKey.setCustomName(Text.literal(e.displayName()));
                        inventory.setStack(i.intValue(), tripWireKey);

                        i.getAndIncrement();
                    });
                    Utils.spawnAzureMan(MC.getServer().getOverworld(), blockPos.add(0, 1, 0));

                    return 1;
                })));

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("createplatform")
                .executes(context -> {
                    // For versions below 1.19, replace "Text.literal" with "new LiteralText".

                    PlayerEntity player = Objects.requireNonNull(context.getSource().getPlayer(), "Player not found");

                    Vec3d playerPos = player.getPos();

                    Integer platformLengthInBlocksX = 40;
                    Integer platformHeightInBlocksY = 200;
                    Integer platformLengthInBlocksZ = 200;

                    Vec3d centerOfPlatform = new Vec3d(playerPos.x + (platformLengthInBlocksX.floatValue() / 2), platformHeightInBlocksY + 1, playerPos.z + (platformLengthInBlocksZ.floatValue() / 2));

                    BlockPos platformPosVec3Dd = new BlockPos(player.getX(), platformHeightInBlocksY, player.getZ());
                    createPlatformAtPos(MC, platformPosVec3Dd, platformLengthInBlocksX, platformLengthInBlocksZ);
                    player.teleport(centerOfPlatform.x, centerOfPlatform.y, centerOfPlatform.z);

                    return 1;
                })));

    }
}
