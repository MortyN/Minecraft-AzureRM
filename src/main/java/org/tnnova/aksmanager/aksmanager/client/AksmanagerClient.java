package org.tnnova.aksmanager.aksmanager.client;

import com.google.gson.Gson;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.entity.SheepEntityRenderer;
import net.minecraft.client.render.entity.VillagerEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.SheepEntityModel;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;
import org.tnnova.aksmanager.aksmanager.Aksmanager;
import org.tnnova.aksmanager.aksmanager.entities.AzureSheep;

import java.util.ArrayList;

@Environment(EnvType.CLIENT)
public class AksmanagerClient implements ClientModInitializer {

    private static final Gson GSON = new Gson();
    public static boolean outliningEntities;

    public static ArrayList<AzureSheep> markedVillagerChildren;

    public static String IAMSHARED = "OHYES";

    private static final KeyBinding CONFIG_BIND = new KeyBinding(
            "key.entity-outliner.selector",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_SEMICOLON,
            "title.entity-outliner.title"
    );

    private static final KeyBinding OUTLINE_BIND = new KeyBinding(
            "key.entity-outliner.outline",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            "title.entity-outliner.title"
    );

    public static Identifier TARGET_ENTITY_CHILDREN_ID = new Identifier("azureentity", "villagerchildren");


    public static final EntityModelLayer MODEL_AZURE_SHEEP_LAYER = new EntityModelLayer(new Identifier("azureentity", "sheep"), "main");

    @Override
    public void onInitializeClient() {

        ClientPlayNetworking.registerGlobalReceiver(TARGET_ENTITY_CHILDREN_ID, (client, handler, buf, responseSender) -> {
            client.execute(() -> {
                // Everything in this lambda is run on the render thread
                String str = buf.readString();

                System.out.println(str);
            });
        });

        EntityRendererRegistry.register(Aksmanager.AZURE_SHEEP_ENTITY_TYPE, SheepEntityRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(MODEL_AZURE_SHEEP_LAYER, SheepEntityModel::getTexturedModelData);
        EntityRendererRegistry.register(Aksmanager.AZURE_VILLAGER_ENTITY_TYPE, VillagerEntityRenderer::new);

        KeyBindingHelper.registerKeyBinding(CONFIG_BIND);
        KeyBindingHelper.registerKeyBinding(OUTLINE_BIND);


        ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);


    }

    private void onEndTick(MinecraftClient client) {
        while (OUTLINE_BIND.wasPressed()) {
            outliningEntities = !outliningEntities;
        }
    }


}
