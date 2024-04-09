package de.pixl.cosmeticserver;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import de.craftsblock.craftscore.event.EventHandler;
import de.craftsblock.craftscore.event.ListenerAdapter;
import de.craftsblock.craftscore.json.Json;
import de.craftsblock.craftscore.json.JsonParser;
import de.craftsblock.craftscore.utils.Validator;
import de.craftsblock.craftsnet.api.websocket.*;
import de.craftsblock.craftsnet.events.sockets.ClientDisconnectEvent;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Socket(path = "/v1/cosmetic")
public class CosmeticSocket implements SocketHandler, ListenerAdapter {

    public static final ConcurrentHashMap<WebSocketClient, ClientMapping> clients = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, ClientMapping> cosmetics = new ConcurrentHashMap<>();

    @MessageReceiver
    public void handleMessage(SocketExchange exchange, String message) throws IOException {
        WebSocketClient client = exchange.client();
        if (!Validator.isJsonValid(message)) {
            client.sendMessage(JsonParser.parse("{}")
                    .set("error", "Data, which are sent, must be in json format!")
                    .asString());
            return;
        }

        Json data = JsonParser.parse(message);
        if (data.contains("cape") && data.contains("uuid")) {
            UUID uuid = UUID.fromString(data.getString("uuid"));
            ConcurrentHashMap<String, Object> cosmetics = new ConcurrentHashMap<>();
            data.get("cosmetics").getAsJsonObject().entrySet().forEach(entry -> cosmetics.put(entry.getKey(), entry.getValue()));
            ClientMapping mapping = new ClientMapping(uuid, cosmetics);
            exchange.broadcast(JsonParser.parse("{}")
                    .set("uuid", uuid.toString())
                    .set("cosmetics", bakeData(cosmetics))
                    .asString());
            clients.put(client, mapping);
            CosmeticSocket.cosmetics.put(uuid, mapping);
        }

        JsonArray array = new JsonArray();
        if (data.contains("data")) {
            for (UUID uuid : data.getStringList("data").stream().map(UUID::fromString).toList())
                if (cosmetics.containsKey(uuid)) {
                    ClientMapping mapping = cosmetics.get(uuid);
                    Json target = JsonParser.parse("{}");
                    target.set("uuid", uuid.toString());
                    target.set("cosmetics", bakeData(mapping.cosmetics()));
                    array.add(target.getObject());
                }
            client.sendMessage(JsonParser.parse("{}")
                    .set("data", array)
                    .asString());
        }
    }

    private JsonElement bakeData(ConcurrentHashMap<String, Object> elements) {
        Json object = JsonParser.parse("{}");
        elements.forEach(object::set);
        return object.getObject();
    }

    @EventHandler
    public void handleDisconnect(ClientDisconnectEvent event) {
        WebSocketClient client = event.getExchange().client();
        if (!clients.containsKey(client))  return;
        ClientMapping mapping = clients.remove(client);
        cosmetics.remove(mapping.uuid());
    }
}
