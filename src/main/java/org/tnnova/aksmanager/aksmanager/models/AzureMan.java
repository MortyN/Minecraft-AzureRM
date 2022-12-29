package org.tnnova.aksmanager.aksmanager.models;

import com.azure.identity.DeviceCodeCredential;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.resources.models.Subscription;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class AzureMan {
    public static AzureResourceManager.Authenticated azureResourceManager;
    private Subscription sub;

    public static World world;

    public AzureMan() {}

    public void setAzureResourceManager(AzureResourceManager.Authenticated azureResourceManager) {
        AzureMan.azureResourceManager = azureResourceManager;
    }

    public void setWorld(World world) {
        AzureMan.world = world;
    }


}
