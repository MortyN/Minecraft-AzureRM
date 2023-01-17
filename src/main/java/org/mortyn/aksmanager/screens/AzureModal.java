package org.mortyn.aksmanager.screens;

import com.azure.resourcemanager.compute.models.VirtualMachineScaleSet;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetVM;
import com.azure.resourcemanager.containerservice.models.KubernetesCluster;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class AzureModal extends Screen {

    private final Screen prevScreen;
    private ButtonWidget closeButton;
    public static VirtualMachineScaleSetVM selectedAzureSheep;
    public static KubernetesCluster selectedAzureVillager;
    public static ArrayList<String> metadataStringList;

    private final int LISTOFFSETINPX = 10;

    public AzureModal(Screen prevScreen) {
        super(Text.literal(""));
        this.prevScreen = prevScreen;
    }

    @Override
    protected void init() {
        super.init();
        addDrawableChild(closeButton = ButtonWidget
                .builder(Text.literal("Close"),
                        b -> this.close())
                .dimensions(width / 2 - 102, height - 52, 100, 20).build());

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
    {
        if(mouseX > closeButton.getX() && mouseX < closeButton.getX() + closeButton.getWidth() && mouseY > closeButton.getY()&& mouseY < closeButton.getY() + closeButton.getHeight() ){
            closeButton.onPress();
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double double_1, double double_2, int int_1,
                                double double_3, double double_4)
    {
        return super.mouseDragged(double_1, double_2, int_1, double_3,
                double_4);
    }

    private void renderList(MatrixStack matrixStack){
        int startY = 8;
        for (String s : metadataStringList) {
            drawTextWithShadow(matrixStack, textRenderer, Text.literal(s), 20, startY, 0xffffff);
            startY += LISTOFFSETINPX;
        }

    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(matrixStack);

        renderList(matrixStack);

        super.render(matrixStack, mouseX, mouseY, partialTicks);

    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}