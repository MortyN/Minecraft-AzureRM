package org.tnnova.aksmanager.aksmanager.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.SheepEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.SheepEntityModel;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.tnnova.aksmanager.aksmanager.Aksmanager;
import org.tnnova.aksmanager.aksmanager.entities.AzureSheep;

@Environment(EnvType.CLIENT)
public class AksmanagerClient implements ClientModInitializer {
    public static final EntityModelLayer MODEL_AZURE_SHEEP_LAYER = new EntityModelLayer(new Identifier("azureentity", "sheep"), "main");
    @Override
    public void onInitializeClient() {

        EntityRendererRegistry.register(Aksmanager.AZURE_SHEEP_ENTITY_TYPE, SheepEntityRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(MODEL_AZURE_SHEEP_LAYER, SheepEntityModel::getTexturedModelData);
    }

}
