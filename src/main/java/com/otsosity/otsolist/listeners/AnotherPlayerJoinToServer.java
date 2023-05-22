package com.otsosity.otsolist.listeners;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;

public class AnotherPlayerJoinToServer {

    public AnotherPlayerJoinToServer(Entity entity, ClientWorld clientWorld) {
        if (entity instanceof OtherClientPlayerEntity player){
            //System.out.println(player.getEntityName());
        }
    }
}
