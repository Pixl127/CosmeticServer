package de.pixl.cosmeticserver;

import de.craftsblock.craftsnet.api.websocket.WebSocketClient;

public class HeartbeatThread implements Runnable{

    public HeartbeatThread() {
        Thread thread = new Thread(this);
        thread.setName("Connection Heartbeat Thread");
        thread.start();
        Runtime.getRuntime().addShutdownHook(new Thread(thread::interrupt));
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted())
            try {
                for (WebSocketClient client : CosmeticSocket.clients.keySet())
                    client.sendMessage("ping");
                Thread.sleep(15000);
            } catch (Exception e) {
                e.printStackTrace();
            }
    }
}
