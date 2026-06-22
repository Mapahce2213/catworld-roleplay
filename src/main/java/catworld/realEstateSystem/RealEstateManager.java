package catworld.realEstateSystem;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

public class RealEstateManager {

    private static final Map<BlockPos, Property> propertiesBySign = new HashMap<>();
    private static final Map<String, Property> propertiesById = new HashMap<>();

    /**
     * initializes the manager
     */
    public static void init() {

        registerProperty(new Property(
            "Apartment #001",
            new BlockPos(149, 4, 154), 
            new BlockPos(148, 3, 150),
            new BlockPos(156, 6, 167), 
            10 
        ));

        registerProperty(new Property(
            "Apartment #002",
            new BlockPos(142, 4, 154),
            new BlockPos(132, 3, 150),
            new BlockPos(140, 6, 167),
            12
        ));

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient()) {
                return ActionResult.PASS;
            }

            BlockPos clickedPos = hitResult.getBlockPos();

            // chcks if hthe selected block is one of the registered signs
            if (propertiesBySign.containsKey(clickedPos)) {
                Property property = propertiesBySign.get(clickedPos);
                UUID playerUuid = player.getUuid();

                if (property.getOwnerUUID() == null) {

                    // buy logic
                    property.setOwnerUUID(playerUuid);
                    player.sendMessage(Text.literal("¡Has comprado " + property.getId() + "!")
                            .formatted(Formatting.GREEN), false);
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

    /**
     * deletes an existing property
     * @param property .
     */
    public static void deleteProperty(Property property) {
        for(var prop: propertiesBySign.values()) {
            if(prop == property) {
                propertiesBySign.remove(property.getSignPosition());
                break;
            }

        propertiesBySign.remove(property.getSignPosition());
        propertiesById.remove(property.getId(), property);
        }
    }

    /**
     * registers a new property
     * @param property .
     */
    public static boolean registerProperty(Property property) {
        for(var prop: propertiesBySign.values()) {
            if(prop == property) {
                return false;
            }
        }
        propertiesBySign.put(property.getSignPosition(), property);
        propertiesById.put(property.getId(), property);
        return true;
    }

    public static boolean canBuildHere(UUID playerUuid, BlockPos pos) {
        for(var property: propertiesBySign.values()) {
            if(property.getSignPosition() == pos) {
                return true;
            }
        }
        return false;
    }
}
