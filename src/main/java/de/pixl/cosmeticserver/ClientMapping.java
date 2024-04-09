package de.pixl.cosmeticserver;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public record ClientMapping(UUID uuid, ConcurrentHashMap<String, Object> cosmetics) {
}
