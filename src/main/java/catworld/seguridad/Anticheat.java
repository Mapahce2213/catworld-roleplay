package catworld.seguridad;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;

import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import catworld.seguridad.Joined;

public class Anticheat {


    private static final double MAX_HORIZONTAL_SPEED_PER_TICK = 0.6;

    private static final double MAX_VERTICAL_RISE_PER_TICK = 0.5;

    private static final int FLY_SUSPICION_TICKS = 10;

    private static final long MIN_BREAK_INTERVAL_MS = 50;

    private static final int MAX_PING_MS = 1000;


    private static final Map<UUID, Vec3d> lastPosition = new HashMap<>();
    private static final Map<UUID, Integer> flySuspicionCounter = new HashMap<>();
    private static final Map<UUID, Long> lastBreakTime = new HashMap<>();

    public static void init(MinecraftServer server) {
        registerEvents();
    }

    private static void registerEvents() {

        ServerTickEvents.END_SERVER_TICK.register(Anticheat::checkAllPlayers);

        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (!(player instanceof ServerPlayerEntity serverPlayer)) {
                return;
            }
            checkBreakSpeed(serverPlayer);
        });
    }

    private static void checkAllPlayers(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            checkFlight(player);
            checkSpeed(player);
            checkPing(player);
        }
    }

    private static void checkFlight(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        Vec3d current = player.getPos();
        Vec3d previous = lastPosition.getOrDefault(uuid, current);
        lastPosition.put(uuid, current);

        double verticalDelta = current.y - previous.y;

        boolean hasLegitimateFlightReason =
            player.getAbilities().flying 
                || player.isFallFlying() 
                || player.hasVehicle() 
                || player.isTouchingWater()
                || player.isInLava()
                || player.isClimbing()  
                || player.hasStatusEffect(StatusEffects.LEVITATION)
                || player.isSpectator();

        if (hasLegitimateFlightReason) {
            flySuspicionCounter.put(uuid, 0);
            return;
        }

        if (verticalDelta > MAX_VERTICAL_RISE_PER_TICK && !player.isOnGround()) {
            int count = flySuspicionCounter.getOrDefault(uuid, 0) + 1;
            flySuspicionCounter.put(uuid, count);

            if (count >= FLY_SUSPICION_TICKS) {
                banPlayer(player, "Vuelo no permitido detectado");
                flySuspicionCounter.put(uuid, 0);
            }
        } else {
            flySuspicionCounter.put(uuid, 0);
        }
    }

    private static void checkSpeed(ServerPlayerEntity player) {
        Vec3d current = player.getPos();
        Vec3d previous = lastPosition.getOrDefault(player.getUuid(), current);

        double horizontalDistance = Math.sqrt(
            Math.pow(current.x - previous.x, 2) + Math.pow(current.z - previous.z, 2)
        );

        boolean hasLegitimateSpeedReason =
            player.hasVehicle()
                || player.hasStatusEffect(StatusEffects.SPEED)
                || player.isFallFlying()
                || player.getAbilities().flying;

        if (hasLegitimateSpeedReason) {
            return;
        }

        if (horizontalDistance > MAX_HORIZONTAL_SPEED_PER_TICK) {
            banPlayer(player, "Velocidad de movimiento no permitida detectada");
        }
    }

    private static void checkBreakSpeed(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        long now = System.currentTimeMillis();
        Long last = lastBreakTime.get(uuid);
        lastBreakTime.put(uuid, now);

        if (last == null) {
            return;
        }

        long interval = now - last;
        if (interval < MIN_BREAK_INTERVAL_MS) {
            kickPlayer(player, "Velocidad de minado no permitida detectada");
        }
    }

    private static void checkPing(ServerPlayerEntity player) {
        int ping = player.networkHandler.getLatency();
        if (ping > MAX_PING_MS) {
            kickPlayer(player, "Ping demasiado alto (" + ping + "ms)");
        }
    }



  private static void banPlayer(ServerPlayerEntity player, String reason) {
    player.networkHandler.disconnect(Text.literal("Baneado: " + reason));

    String uuid = player.getUuid().toString();
    Map<String, Auth> authByUuid = Joined.getAuthByUuid();

    Auth auth = Auth.findByUuid(authByUuid, uuid);
    if (auth != null) {
        auth.setBanned(true);
        Auth.save(authByUuid);
    }
}

    private static void kickPlayer(ServerPlayerEntity player, String reason) {
        player.networkHandler.disconnect(Text.literal("Expulsado: " + reason));
    }
}
