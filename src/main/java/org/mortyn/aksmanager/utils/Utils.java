package org.mortyn.aksmanager.utils;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.util.ExpandableStringEnum;
import com.azure.identity.DeviceCodeCredential;
import com.azure.identity.DeviceCodeCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.compute.models.PowerState;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.visitor.NbtTextFormatter;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Matrix4f;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Utils {

    public static final String AZUREMAN = "Azure Man";
    public static WanderingTraderEntity spawnAzureMan(World world, BlockPos pos) {

        WanderingTraderEntity wanderingTraderEntity = EntityType.WANDERING_TRADER.create(world);
        wanderingTraderEntity.setPosition(pos.getX(), pos.getY(), pos.getZ());
        wanderingTraderEntity.setCustomName(Text.literal(AZUREMAN));
        wanderingTraderEntity.setPersistent();

        Scoreboard scoreboard = wanderingTraderEntity.world.getScoreboard();
        Team team = scoreboard.getPlayerTeam(AZUREMAN);
        if (team == null) {
            Team blueTeam = scoreboard.addTeam("BlueTeam");
            blueTeam.setColor(Formatting.BLUE);
            scoreboard.addPlayerToTeam(AZUREMAN, blueTeam);
            team = blueTeam;
        }
        team.setNameTagVisibilityRule(AbstractTeam.VisibilityRule.ALWAYS);

        world.spawnEntity(wanderingTraderEntity);
        return wanderingTraderEntity;
    }

    public static Vec3d getClientLookVec(MinecraftClient mc)
    {
        ClientPlayerEntity player = mc.player;
        float f = 0.017453292F;
        float pi = (float)Math.PI;

        float f1 = MathHelper.cos(-player.getYaw() * f - pi);
        float f2 = MathHelper.sin(-player.getYaw() * f - pi);
        float f3 = -MathHelper.cos(-player.getPitch() * f);
        float f4 = MathHelper.sin(-player.getPitch() * f);

        return new Vec3d(f2 * f3, f4, f1 * f3);
    }

    public static Vec3d getCameraPos(MinecraftClient mc)
    {
        Camera camera = mc.getBlockEntityRenderDispatcher().camera;
        if(camera == null)
            return Vec3d.ZERO;

        return camera.getPos();
    }

    public static void renderTracers(MatrixStack matrixStack, double partialTicks,
                                      int regionX, int regionZ, MinecraftClient mc, ArrayList<VillagerEntity> villagerEntities)
    {
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.setShaderColor(1, 1, 1, 1);

        Matrix4f matrix = matrixStack.peek().getPositionMatrix();

        Tessellator tessellator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES,
                VertexFormats.POSITION_COLOR);

        Vec3d start = getClientLookVec(mc)
                .add(getCameraPos(mc)).subtract(regionX, 0, regionZ);

        for(MobEntity e : villagerEntities)
        {
            Vec3d interpolationOffset = new Vec3d(e.getX(), e.getY(), e.getZ())
                    .subtract(e.prevX, e.prevY, e.prevZ).multiply(1 - partialTicks);

            Vec3d end = e.getBoundingBox().getCenter()
                    .subtract(interpolationOffset).subtract(regionX, 0, regionZ);

            float f = mc.player.distanceTo(e) / 20F;
            float r = MathHelper.clamp(2 - f, 0, 1);
            float g = MathHelper.clamp(f, 0, 1);

            bufferBuilder
                    .vertex(matrix, (float)start.x, (float)start.y, (float)start.z)
                    .color(r, g, 0, 0.5F).next();

            bufferBuilder
                    .vertex(matrix, (float)end.x, (float)end.y, (float)end.z)
                    .color(r, g, 0, 0.5F).next();
        }

        tessellator.draw();

    }

    public static DeviceCodeCredential getDeviceCodeCredentialWithMsg(PlayerEntity player) {
        player.sendMessage(Text.literal("Starting Azure login process..."));
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

    public static AzureResourceManager.Authenticated getAzureAuth (PlayerEntity player){
        DeviceCodeCredential deviceCodeCredential = getDeviceCodeCredentialWithMsg(player);

        AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
        //<T>.Authenticated is to get non subscription scoped resources, such as subscriptions themselves without providing withSubscriptionId

        return AzureResourceManager
                .authenticate(deviceCodeCredential, profile);
    }


}
