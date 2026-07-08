package catworld;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class tablist {

    private static class PlayerActivity {
        double x, y, z;
        float yaw, pitch;
        long lastActiveTime;
        boolean isAfk;

        PlayerActivity(ServerPlayerEntity player) {
            this.x = player.getX();
            this.y = player.getY();
            this.z = player.getZ();
            this.yaw = player.getYaw();
            this.pitch = player.getPitch();
            this.lastActiveTime = System.currentTimeMillis();
            this.isAfk = false;
        }

        boolean hasMoved(ServerPlayerEntity player) {
            return this.x != player.getX() || this.y != player.getY() || this.z != player.getZ() ||
                   this.yaw != player.getYaw() || this.pitch != player.getPitch();
        }

        void updatePosition(ServerPlayerEntity player) {
            this.x = player.getX();
            this.y = player.getY();
            this.z = player.getZ();
            this.yaw = player.getYaw();
            this.pitch = player.getPitch();
        }
    }
    
    private static final Map<UUID, PlayerActivity> playerActivityMap = new HashMap<>();
    private static final long AFK_TIMEOUT_MS = 60000; 

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.player;
            playerActivityMap.put(player.getUuid(), new PlayerActivity(player));
            updatePlayerTab(server, player, false);
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            playerActivityMap.remove(handler.player.getUuid());
        });

        ServerTickEvents.START_SERVER_TICK.register(server -> {
            if (server.getTicks() % 20 != 0) return;

            long currentTime = System.currentTimeMillis();

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                UUID uuid = player.getUuid();
                PlayerActivity activity = playerActivityMap.get(uuid);

                if (activity == null) {
                    activity = new PlayerActivity(player);
                    playerActivityMap.put(uuid, activity);
                    continue;
                }

                if (activity.hasMoved(player)) {
                    activity.updatePosition(player);
                    activity.lastActiveTime = currentTime;

                    if (activity.isAfk) {
                        activity.isAfk = false;
                        updatePlayerTab(server, player, false);
                    }
                } else {
                    if (!activity.isAfk && (currentTime - activity.lastActiveTime >= AFK_TIMEOUT_MS)) {
                        activity.isAfk = true;
                        updatePlayerTab(server, player, true);
                    }
                }
            }
        });
    }

    private static void updatePlayerTab(MinecraftServer server, ServerPlayerEntity player, boolean isAfk) {
        Scoreboard scoreboard = server.getScoreboard();
        String playerName = player.getName().getString();

        Team oldTeam = scoreboard.getTeam(playerName);
        if (oldTeam != null) {
            scoreboard.removeTeam(oldTeam);
        }

        Team team = scoreboard.addTeam(playerName);
        scoreboard.addScoreHolderToTeam(playerName, team);

        Text statusText = isAfk 
            ? Text.literal("AFK ").formatted(Formatting.RED, Formatting.BOLD)
            : Text.literal("Online ").formatted(Formatting.GREEN, Formatting.BOLD);

        team.setPrefix(
            Text.empty()
                .append(statusText)
                .append(Text.literal("| ").formatted(Formatting.BOLD))
                .append(Text.literal("CATWORLD.RP ").styled(s -> s.withColor(0xFF7D00).withBold(true)))
                .append(Text.literal("| ").formatted(Formatting.BOLD))
                .append(Text.literal("Desconosido").formatted(Formatting.WHITE))
        );

        team.setSuffix(
            Text.empty()
                .append(
                    Text.literal("\uE000\uE001")
                        .styled(style -> style
                            .withFont(Identifier.of("minecraft", "donate"))
                            .withBold(true)
                            .withColor(Formatting.GOLD)
                        )
                )
        );

        team.setColor(Formatting.DARK_GRAY);
    }
}

