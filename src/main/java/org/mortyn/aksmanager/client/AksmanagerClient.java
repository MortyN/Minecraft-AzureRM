package org.mortyn.aksmanager.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.SheepEntityRenderer;
import net.minecraft.client.render.entity.VillagerEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.SheepEntityModel;
import net.minecraft.util.Identifier;
import org.mortyn.aksmanager.Aksmanager;

@Environment(EnvType.CLIENT)
public class AksmanagerClient implements ClientModInitializer {

    public static boolean outliningEntities;

    public static final EntityModelLayer MODEL_AZURE_SHEEP_LAYER = new EntityModelLayer(new Identifier("azureentity", "sheep"), "main");

    @Override
    public void onInitializeClient() {

        EntityRendererRegistry.register(Aksmanager.AZURE_SHEEP_ENTITY_TYPE, SheepEntityRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(MODEL_AZURE_SHEEP_LAYER, SheepEntityModel::getTexturedModelData);
        EntityRendererRegistry.register(Aksmanager.AZURE_VILLAGER_ENTITY_TYPE, VillagerEntityRenderer::new);

        ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);

    }

    private void onEndTick(MinecraftClient client) {
/*        while (OUTLINE_BIND.wasPressed()) {
            outliningEntities = !outliningEntities;
        }*/
    }


}
