package org.tnnova.aksmanager.aksmanager.models;

import java.util.ArrayList;

public class AzureButtonPosList {

    private static ArrayList<AzureMan> azureButtonPosArrayList;

    public AzureButtonPosList() {
        azureButtonPosArrayList = new ArrayList<>();
    }

    public static ArrayList<AzureMan> getAzureButtonPosArrayList() {
        return azureButtonPosArrayList;
    }

    public void addAzureButtonPos(AzureMan azureButtonPos){
        azureButtonPosArrayList.add(azureButtonPos);
    }

}
