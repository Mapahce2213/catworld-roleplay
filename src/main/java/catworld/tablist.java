package catworld;
import catworld.welcome;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;

public class tablist {

    public static void register() {
        // Когда игрок заходит на сервер
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.player;
            Scoreboard scoreboard = server.getScoreboard();

Team team = scoreboard.getTeam(player.getName().getString());
if (team == null) {
    team = scoreboard.addTeam(player.getName().getString());
    // добавляем игрока в команду через список участников
    team.getPlayerList().add(player.getName().getString());
}

team.setPrefix(
    Text.empty()
        .append(Text.literal("Online ").formatted(Formatting.DARK_GREEN, Formatting.BOLD))
        .append(Text.literal("| ").formatted(Formatting.BOLD))
        .append(Text.literal("CATWORLD.RP ").styled(s -> s.withColor(0xFF7D00).withBold(true)))
        .append(Text.literal("| ").formatted(Formatting.BOLD))
);



        });
    }
}

