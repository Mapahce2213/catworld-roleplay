package catworld.realEstateSystem;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.util.math.BlockPos;

public class RealEstateManager {

    private static final Map<BlockPos, Property> properties = new HashMap<>();



    /**
     * deletes an existing property
     * @param property .
     */
    private static void deleteProperty(Property property) {
        for(var prop: properties.values()) {
            if(prop == property) {
                properties.remove(property.getSignPosition());
                break;
            }

        properties.remove(property.getSignPosition());
        }
    }

    /**
     * registers a new property
     * @param property .
     */
    private static boolean registerProperty(Property property) {
        for(var prop: properties.values()) {
            if(prop == property) {
                return false;
            }
        }
        properties.put(property.getSignPosition(), property);
        return true;
    }

    private static boolean canBuildHere(UUID playerUuid, BlockPos pos) {
        for(var property: properties.values()) {
            if(property.getSignPosition() == pos) {
                return true;
            }
        }
        return false;
    }
}
