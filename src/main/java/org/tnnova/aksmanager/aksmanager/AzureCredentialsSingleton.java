package org.tnnova.aksmanager.aksmanager;

import com.azure.identity.DeviceCodeCredential;
import com.azure.identity.DeviceCodeCredentialBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import static net.minecraft.server.command.CommandManager.*;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
/*
class AzureCredentialsSingleton {
    // Static variable reference of single_instance
    // of type Singleton
    private static AzureCredentialsSingleton single_instance = null;

    public DeviceCodeCredential deviceCodeCredential;
    public static String deviceCodeCredentialMessage;

    // Constructor
    // Here we will be creating private constructor
    // restricted to this class itself
    private AzureCredentialsSingleton()
    {
        deviceCodeCredential = new DeviceCodeCredentialBuilder()
                .challengeConsumer(challenge -> {
                    // Lets the user know about the challenge.
                    deviceCodeCredentialMessage = challenge.getMessage();
                }).build();
    }

    // Static method to create instance of Singleton class
    public static AzureCredentialsSingleton getInstance(CommandContext<ServerCommandSource> context)
    {
        if (single_instance == null)
            single_instance = new AzureCredentialsSingleton();
        context.getSource().sendMessage(Text.literal(deviceCodeCredentialMessage));
        return single_instance;
    }
}

*/
