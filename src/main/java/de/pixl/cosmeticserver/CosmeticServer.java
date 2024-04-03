package de.pixl.cosmeticserver;

import de.craftsblock.craftsnet.addon.Addon;

public class CosmeticServer extends Addon {

    @Override
    public void onEnable() {
        CosmeticSocket socket = new CosmeticSocket();
        routeRegistry().register(socket);
        listenerRegistry().register(socket);
        new HeartbeatThread();
    }

    @Override
    public void onDisable() {

    }
}
