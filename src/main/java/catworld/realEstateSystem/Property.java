package catworld.realEstateSystem;

import java.util.UUID;

import net.minecraft.util.math.BlockPos;

/**
 * Represents a property in the real estate system.
 */
public class Property {

    private final String id;
    private final BlockPos signPosition;
    private final BlockPos minPosition;
    private final BlockPos maxPosition;
    private UUID ownerUUID;   // null if the property is unowned
    private final int price;

    /**
     * Constructs a new Property object with the specified parameters.
     * @param id .
     * @param signPosition .
     * @param minPosition .
     * @param maxPosition .
     * @param price .
     */
    public Property(String id, BlockPos signPosition, BlockPos minPosition, BlockPos maxPosition, int price) {
        this.id = id;
        this.signPosition = signPosition;
        this.minPosition = minPosition;
        this.maxPosition = maxPosition;
        this.price = price;
    }

    /**
     * Checks if the given coordinate is within the bounds of the property.
     * @param coordinate .
     * @return true if the coordinate is within the property bounds, false otherwise.
     */
    public boolean containsCoordinate(BlockPos coordinate) {
        return coordinate.getX() >= this.minPosition.getX() && coordinate.getX() <= this.maxPosition.getX() &&
               coordinate.getY() >= this.minPosition.getY() && coordinate.getY() <= this.maxPosition.getY() &&
               coordinate.getZ() >= this.minPosition.getZ() && coordinate.getZ() <= this.maxPosition.getZ();
    }

    public String getId() {
        return id;
    }

    public BlockPos getSignPosition() {
        return signPosition;
    }

    public BlockPos getMinPosition() {
        return minPosition;
    }

    public BlockPos getMaxPosition() {
        return maxPosition;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public void setOwnerUUID(UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
    }

    public int getPrice() {
        return price;
    }
}
