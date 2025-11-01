package com.tomkeuper.bedwars.proxy.rejoin;

import com.astroid.bedwars.proxy.api.CachedArena;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RemoteReJoin implements com.astroid.bedwars.proxy.api.RemoteReJoin {

    private final CachedArena arena;
    private final UUID uuid;

    private static final ConcurrentHashMap<UUID, com.astroid.bedwars.proxy.api.RemoteReJoin> rejoinByUUID = new ConcurrentHashMap<>();

    public RemoteReJoin(UUID player, CachedArena arena){
        this.uuid = player;
        this.arena = arena;
        rejoinByUUID.put(uuid, this);
    }

    public static com.astroid.bedwars.proxy.api.RemoteReJoin getReJoin(UUID player){
        return rejoinByUUID.getOrDefault(player, null);
    }

    public CachedArena getArena() {
        return arena;
    }

    public UUID getUUID() {
        return uuid;
    }

    public void destroy(){
        rejoinByUUID.remove(uuid);
    }

    public static ConcurrentHashMap<UUID, com.astroid.bedwars.proxy.api.RemoteReJoin> getRejoinByUUID() {
        return rejoinByUUID;
    }
}