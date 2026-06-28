package catworld;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;

public class tablist {

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.player;
            Scoreboard scoreboard = server.getScoreboard();
            String playerName = player.getName().getString();

            Team team = scoreboard.getTeam(playerName);
            if (team == null) {
                team = scoreboard.addTeam(playerName);
            }

            scoreboard.addScoreHolderToTeam(playerName, team);

            team.setPrefix(
                Text.empty()
                    .append(Text.literal("Online ").formatted(Formatting.DARK_GREEN, Formatting.BOLD))
                    .append(Text.literal("| ").formatted(Formatting.BOLD))
                    .append(Text.literal("CATWORLD.RP ").styled(s -> s.withColor(0xFF7D00).withBold(true)))
                    .append(Text.literal("| ").formatted(Formatting.BOLD))
            );
            
            team.setColor(Formatting.WHITE); 
        });
    }
}

