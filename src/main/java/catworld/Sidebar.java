package catworld;

import java.util.Optional;
import java.util.UUID;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.network.packet.s2c.play.ScoreboardDisplayS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardObjectiveUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardScoreUpdateS2CPacket;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import catworld.ciudadania.Ciudadanos;
import catworld.seguridad.Auth;
import catworld.seguridad.Joined;

public class Sidebar {

    private static final String OBJECTIVE_NAME = "catworld_sidebar";
    private static final int UPDATE_INTERVAL_TICKS = 100;

    public static final Map<String, Ciudadanos> ciudadanosByCedula = new ConcurrentHashMap<>();

    private static final Map<UUID, Ciudadanos> ciudadanosCache = new ConcurrentHashMap<>();
    private static final Map<UUID, String> donationCache = new ConcurrentHashMap<>();
    
    private static class SidebarState {
        String lastNombre = "";
        String lastApedido = "";
        String lastEdad = "";
        String lastDonacion = "";
        String lastFraccion = "";
        boolean isInitialized = false;
    }

    private static final Map<UUID, SidebarState> playerStates = new ConcurrentHashMap<>();

    public static void register() {

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.player;
            if (player == null) return;
            
            UUID uuid = player.getUuid();
            String nickname = player.getName().getString();
            playerStates.put(uuid, new SidebarState());

            Thread.startVirtualThread(() -> {
                Ciudadanos ciudadano = Ciudadanos.findByNickname(ciudadanosByCedula, nickname);
                if (ciudadano != null) {
                    ciudadanosCache.put(uuid, ciudadano);
                }

                String status = "No hay";
                try {
                    Auth auth = Auth.findByUuid(Joined.getAuthByUuid(), uuid.toString());
                    if (auth != null && auth.getStatus() != null) {
                        status = auth.getStatus();
                    }
                } catch (Exception e) {
                    status = "Error404";
                }
                donationCache.put(uuid, status);
                
                server.execute(() -> update(player));
            });
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            if (handler.player != null) {
                UUID uuid = handler.player.getUuid();
                hide(handler.player);
                playerStates.remove(uuid);
                donationCache.remove(uuid);
                ciudadanosCache.remove(uuid);
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTicks() % UPDATE_INTERVAL_TICKS != 0) return;

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (player != null) {
                    update(player);
                }
            }
        });
    }

    public static void reloadPlayerCache(ServerPlayerEntity player) {
        if (player == null) return;
        UUID uuid = player.getUuid();
        String nickname = player.getName().getString();

        Ciudadanos ciudadano = Ciudadanos.findByNickname(ciudadanosByCedula, nickname);
        if (ciudadano != null) {
            ciudadanosCache.put(uuid, ciudadano);
        } else {
            ciudadanosCache.remove(uuid);
        }
        update(player);
    }

    public static void update(ServerPlayerEntity player) {
        if (player == null) return;

        Ciudadanos ciudadano = ciudadanosCache.get(player.getUuid());
        String donacionStatus = donationCache.getOrDefault(player.getUuid(), "No hay");

        show(player, ciudadano, donacionStatus);
    }

    public static void show(ServerPlayerEntity player, Ciudadanos ciudadano, String donacionStatus) {
        UUID uuid = player.getUuid();
        SidebarState state = playerStates.computeIfAbsent(uuid, k -> new SidebarState());

        String nombre = (ciudadano != null && ciudadano.getName() != null) ? ciudadano.getName() : "???";
        String apedido = (ciudadano != null && ciudadano.getApedido() != null) ? ciudadano.getApedido() : "???";
        String edad = (ciudadano != null) ? String.valueOf(ciudadano.getEdad()) : "???";
        String fraccion = (ciudadano != null && ciudadano.getTrabajo() != null && ciudadano.getTrabajo().getFraccion() != null)
            ? ciudadano.getTrabajo().getFraccion()
            : "No hay";

        ScoreboardObjective objective = new ScoreboardObjective(
            null, OBJECTIVE_NAME, ScoreboardCriterion.DUMMY,
            Text.literal("Catworld RP").formatted(Formatting.GOLD, Formatting.BOLD),
            ScoreboardCriterion.RenderType.INTEGER, false, null
        );

        if (!state.isInitialized) {
            player.networkHandler.sendPacket(new ScoreboardObjectiveUpdateS2CPacket(objective, 0));
            player.networkHandler.sendPacket(new ScoreboardDisplayS2CPacket(ScoreboardDisplaySlot.SIDEBAR, objective));
            
            sendLine(player, objective, "line8", 8, "§7-----------------------");
            sendLine(player, objective, "line7", 7, "§eNombre: §f" + nombre);
            sendLine(player, objective, "line6", 6, "§eApedido: §f" + apedido);
            sendLine(player, objective, "line5", 5, "§eEdad: §f" + edad);
            sendLine(player, objective, "line4", 4, "§eDonacion: §f" + donacionStatus);
            sendLine(player, objective, "line3", 3, "§eFraccion: §f" + fraccion);
            sendLine(player, objective, "line2", 2, "§7  -------------------");
            sendLine(player, objective, "line1", 1, "§6[CatWorld RP]");

            state.lastNombre = nombre;
            state.lastApedido = apedido;
            state.lastEdad = edad;
            state.lastDonacion = donacionStatus;
            state.lastFraccion = fraccion;
            state.isInitialized = true;
            return;
        }

        if (!state.lastNombre.equals(nombre)) {
            sendLine(player, objective, "line7", 7, "§eNombre: §f" + nombre);
            state.lastNombre = nombre;
        }
        if (!state.lastApedido.equals(apedido)) {
            sendLine(player, objective, "line6", 6, "§eApedido: §f" + apedido);
            state.lastApedido = apedido;
        }
        if (!state.lastEdad.equals(edad)) {
            sendLine(player, objective, "line5", 5, "§eEdad: §f" + edad);
            state.lastEdad = edad;
        }
        if (!state.lastDonacion.equals(donacionStatus)) {
            sendLine(player, objective, "line4", 4, "§eDonacion: §f" + donacionStatus);
            state.lastDonacion = donacionStatus;
        }
        if (!state.lastFraccion.equals(fraccion)) {
            sendLine(player, objective, "line3", 3, "§eFraccion: §f" + fraccion);
            state.lastFraccion = fraccion;
        }
    }

    private static void sendLine(ServerPlayerEntity player, ScoreboardObjective objective,
                                  String owner, int score, String text) {
        player.networkHandler.sendPacket(
            new ScoreboardScoreUpdateS2CPacket(
                owner,
                objective.getName(),
                score,
                Optional.of(Text.literal(text)),
                Optional.empty()
            )
        );
    }

    public static void hide(ServerPlayerEntity player) {
        ScoreboardObjective objective = new ScoreboardObjective(
            null, OBJECTIVE_NAME, ScoreboardCriterion.DUMMY,
            Text.literal(""), ScoreboardCriterion.RenderType.INTEGER, false, null
        );

        player.networkHandler.sendPacket(
            new ScoreboardObjectiveUpdateS2CPacket(objective, 1)
        );
    }
}

