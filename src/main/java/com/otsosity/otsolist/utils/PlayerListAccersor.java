package com.otsosity.otsolist.mixin.Accessors;

import net.minecraft.client.network.PlayerListEntry;

import java.util.List;

public interface PlayerListAccersor {
    default List<PlayerListEntry> collectPlayerEntries() {
        return null;
    }

}
