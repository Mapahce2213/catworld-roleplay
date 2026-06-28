package catworld.realEstateSystem;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

public class RealEstateManager {

    private static final Map<BlockPos, Property> propertiesBySign = new HashMap<>();
    private static final Map<String, Property> propertiesById = new HashMap<>();

    public static void init(MinecraftServer server) {
        Property.loadAll(propertiesBySign, propertiesById);

        for (Property property : propertiesById.values()) {
            spawnSign(server, property);
        }

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient()) {
                return ActionResult.PASS;
            }

            BlockPos clickedPos = hitResult.getBlockPos();

            if (propertiesBySign.containsKey(clickedPos)) {
                Property property = propertiesBySign.get(clickedPos);
                UUID playerUuid = player.getUuid();

                if (property.getOwnerUUID() == null) {
                    property.setOwnerUUID(playerUuid);
                    player.sendMessage(Text.literal("¡Has comprado " + property.getId() + "!")
                            .formatted(Formatting.GREEN), false);
                    Property.save(propertiesById);
                    spawnSign(player.getServer(), property);

                } else if (property.getOwnerUUID().equals(playerUuid)) {
                    player.sendMessage(Text.literal("Ya eres el dueño de esta propiedad.")
                            .formatted(Formatting.YELLOW), false);
                } else {
                    player.sendMessage(Text.literal("Esta propiedad ya tiene dueño.")
                            .formatted(Formatting.RED), false);
                }
                return ActionResult.SUCCESS;
            }

            return ActionResult.PASS;
        });
    }

  private static void spawnSign(MinecraftServer server, Property property) {
    BlockPos pos = property.getSignPosition();
    int x = pos.getX();
    int y = pos.getY();
    int z = pos.getZ();

    net.minecraft.server.world.ServerWorld world = server.getOverworld();
    String facing = "north";
    
    if (world != null) {
        if (world.getBlockState(pos.south()).isSolidBlock(world, pos.south())) {
            facing = "north";
        } else if (world.getBlockState(pos.north()).isSolidBlock(world, pos.north())) {
            facing = "south";
        } else if (world.getBlockState(pos.east()).isSolidBlock(world, pos.east())) {
            facing = "west";
        } else if (world.getBlockState(pos.west()).isSolidBlock(world, pos.west())) {
            facing = "east";
        }
    }

    run(server, String.format("setblock %d %d %d minecraft:oak_wall_sign[facing=%s]", x, y, z, facing));

    String line2 = "";
    String line2Color = "white";
    String line3 = "";
    String line3Color = "white";

    UUID owner = property.getOwnerUUID();
    
    if (owner == null) {
        line2 = property.getPrice() + "$";
        line2Color = "green";
        line3 = "selling";
        line3Color = "red";
    } else {
        ServerPlayerEntity ownerPlayer = server.getPlayerManager().getPlayer(owner);
        line2 = ownerPlayer != null ? ownerPlayer.getName().getString() : owner.toString();
        line2Color = "yellow";
        
        if (line2.length() > 15) {
            line2 = line2.substring(0, 15);
        }
        
        line3 = ""; 
        line3Color = "white";
    }

    String dataCmd = String.format(
        "data merge block %d %d %d {front_text:{messages:['{\"text\":\"%s\",\"color\":\"aqua\",\"bold\":true}','{\"text\":\"%s\",\"color\":\"%s\"}','{\"text\":\"%s\",\"color\":\"%s\"}','{\"text\":\"\"}']}}",
        x, y, z, property.getId(), line2, line2Color, line3, line3Color
    );
    run(server, dataCmd);
}


    private static void run(MinecraftServer server, String command) {
        server.getCommandManager().executeWithPrefix(server.getCommandSource(), command);
    }

    public static void deleteProperty(Property property) {
        propertiesBySign.remove(property.getSignPosition());
        propertiesById.remove(property.getId(), property);
        Property.save(propertiesById);
    }

    public static boolean registerProperty(Property property) {
        if (propertiesById.containsKey(property.getId())) {
            return false;
        }
        propertiesBySign.put(property.getSignPosition(), property);
        propertiesById.put(property.getId(), property);
        Property.save(propertiesById);
        return true;
    }

    public static Collection<Property> getAllProperties() {
        return propertiesById.values();
    }

    public static boolean canBuildHere(UUID playerUuid, BlockPos pos) {
        for (Property property : propertiesBySign.values()) {
            if (property.getSignPosition().equals(pos)) {
                return true;
            }
        }
        return false;
    }
}

