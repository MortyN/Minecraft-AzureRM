package org.mortyn.aksmanager.models;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSets;
import com.azure.resourcemanager.containerservice.models.KubernetesClusters;
import com.azure.resourcemanager.resources.models.Subscription;
import net.minecraft.world.World;

public class AzureMan {
    public static AzureResourceManager.Authenticated azureResourceManager;
    private Subscription sub;

    public static World world;

    public static KubernetesClusters kubernetesClusters;

    public static VirtualMachineScaleSets virtualMachineScaleSets;

    public AzureMan() {}

    public void setAzureResourceManager(AzureResourceManager.Authenticated azureResourceManager) {
        AzureMan.azureResourceManager = azureResourceManager;
    }

    public static void setVirtualMachineScaleSets(VirtualMachineScaleSets virtualMachineScaleSets) {
        AzureMan.virtualMachineScaleSets = virtualMachineScaleSets;
    }

    public static void setKubernetesClusters(KubernetesClusters kubernetesClusters) {
        AzureMan.kubernetesClusters = kubernetesClusters;
    }

    public void setWorld(World world) {
        AzureMan.world = world;
    }


}