package catworld.realEstateSystem;

import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;

/**
 * Represents a property in the real estate system.
 */
public class Property {

    private final String id;
    private final BlockPos signPosition;
    private final BlockPos minPosition;
    private final BlockPos maxPosition;
    private Uuids ownerUUID;   // null if the property is unowned
    private final int price;

    /**
     * Constructs a new Property object with the specified parameters.
     * @param id .
     * @param signPosition .
     * @param minPosition .
     * @param maxPosition .
     * @param ownerUUID .
     * @param price .
     */
    public Property(String id, BlockPos signPosition, BlockPos minPosition, BlockPos maxPosition, Uuids ownerUUID, int price) {
        this.id = id;
        this.signPosition = signPosition;
        this.minPosition = minPosition;
        this.maxPosition = maxPosition;
        this.ownerUUID = ownerUUID;
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

    public Uuids getOwnerUUID() {
        return ownerUUID;
    }

    public void setOwnerUUID(Uuids ownerUUID) {
        this.ownerUUID = ownerUUID;
    }

    public int getPrice() {
        return price;
    }
}
