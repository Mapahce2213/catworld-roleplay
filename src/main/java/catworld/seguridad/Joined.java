package catworld.seguridad;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;

public class Joined {

    private static final Map<String, Auth> authByUuid = new HashMap<>();

    private static final Set<UUID> unauthenticated = new HashSet<>();

    private static final Map<UUID, Vec3d> frozenPosition = new HashMap<>();

    public static void init(MinecraftServer server) {
        Auth.loadAll(authByUuid);
        registerEvents();
    }

    private static void registerEvents() {

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            String uuid = player.getUuid().toString();

            if (!authByUuid.containsKey(uuid)) {
                unauthenticated.add(player.getUuid());
                frozenPosition.put(player.getUuid(), player.getPos());

                player.sendMessage(
                    Text.literal("Por favor escribe /auth register <email> <contrasena> <repita contrasena>").formatted(Formatting.RED),
                    false
                );
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (UUID uuid : unauthenticated) {
                ServerPlayerEntity player = server.getPlayerManager().getPlayer(uuid);
                if (player == null) continue;

                Vec3d frozen = frozenPosition.get(uuid);
                if (frozen != null) {
                    player.teleport(player.getServerWorld(), frozen.x, frozen.y, frozen.z, player.getYaw(), player.getPitch());
                }
            }
        });


        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (unauthenticated.contains(player.getUuid())) {
                return TypedActionResult.fail(player.getStackInHand(hand));
            }
            return TypedActionResult.pass(player.getStackInHand(hand));
        });
    }

    public static void markAuthenticated(UUID uuid) {
        unauthenticated.remove(uuid);
        frozenPosition.remove(uuid);
    }

    public static Map<String, Auth> getAuthByUuid() {
        return authByUuid;
    }
}
