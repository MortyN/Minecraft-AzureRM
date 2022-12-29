package org.tnnova.aksmanager.aksmanager.models;

import com.azure.resourcemanager.resources.models.Subscription;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;

public class AzureButtonPosList {

    private static ArrayList<AzureButtonPos> azureButtonPosArrayList;

    public AzureButtonPosList() {
        azureButtonPosArrayList = new ArrayList<>();
    }

    public static ArrayList<AzureButtonPos> getAzureButtonPosArrayList() {
        return azureButtonPosArrayList;
    }

    public void addAzureButtonPos(AzureButtonPos azureButtonPos){
        azureButtonPosArrayList.add(azureButtonPos);
    }

}
