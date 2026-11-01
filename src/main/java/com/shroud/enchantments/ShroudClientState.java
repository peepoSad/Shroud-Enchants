
package com.shroud.enchantments;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class ShroudClientState {

    private static final Set<UUID> HIDDEN_PLAYERS = new HashSet<>();

    private ShroudClientState() {
    }

    public static void setHidden(UUID playerId, boolean hidden) {
        if (hidden) {
            HIDDEN_PLAYERS.add(playerId);
        } else {
            HIDDEN_PLAYERS.remove(playerId);
        }
    }

    public static boolean isHidden(UUID playerId) {
        return HIDDEN_PLAYERS.contains(playerId);
    }

    public static void clear() {
        HIDDEN_PLAYERS.clear();
    }
}
