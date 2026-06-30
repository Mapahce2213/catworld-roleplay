package catworld.realEstateSystem;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.math.BlockPos;

public class Property {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("catworld_properties.json");

    private final String id;
    private final BlockPos signPosition;
    private final BlockPos minPosition;
    private final BlockPos maxPosition;
    private UUID ownerUUID;
    private final int price;

  public Property(String id, BlockPos signPosition, BlockPos minPosition, BlockPos maxPosition, int price) {
    this.id = id;
    this.signPosition = signPosition;
    this.price = price;

    this.minPosition = new BlockPos(
        Math.min(minPosition.getX(), maxPosition.getX()),
        Math.min(minPosition.getY(), maxPosition.getY()),
        Math.min(minPosition.getZ(), maxPosition.getZ())
    );

    this.maxPosition = new BlockPos(
        Math.max(minPosition.getX(), maxPosition.getX()),
        Math.max(minPosition.getY(), maxPosition.getY()),
        Math.max(minPosition.getZ(), maxPosition.getZ())
    );
}


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

    // =====================  JSON =====================

    public static void loadAll(Map<BlockPos, Property> propertiesBySign, Map<String, Property> propertiesById) {
        propertiesBySign.clear();
        propertiesById.clear();

        if (!Files.exists(FILE_PATH)) {
            System.out.println("[RealEstateSystem] " + FILE_PATH + " 404 error.");
            writeToDisk(propertiesById);
            return;
        }

        try (Reader reader = Files.newBufferedReader(FILE_PATH, StandardCharsets.UTF_8)) {
            JsonElement root = JsonParser.parseReader(reader);

            if (!root.isJsonArray()) {
                System.err.println("[RealEstateSystem] " + FILE_PATH + " broken.");
                return;
            }

            for (JsonElement element : root.getAsJsonArray()) {
                Property property = fromJson(element.getAsJsonObject());
                propertiesById.put(property.getId(), property);
                propertiesBySign.put(property.getSignPosition(), property);
            }

        } catch (IOException e) {
            System.err.println("[RealEstateSystem] 504 error " + FILE_PATH);
            e.printStackTrace();
        }
    }

    public static void save(Map<String, Property> propertiesById) {
        writeToDisk(propertiesById);
    }

    private static void writeToDisk(Map<String, Property> propertiesById) {
        JsonArray array = new JsonArray();
        for (Property property : propertiesById.values()) {
            array.add(property.toJson());
        }

        try {
            Files.createDirectories(FILE_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(FILE_PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(array, writer);
            }
        } catch (IOException e) {
            System.err.println("[RealEstateSystem] Can't save " + FILE_PATH);
            e.printStackTrace();
        }
    }

    private static Property fromJson(JsonObject obj) {
        String id = obj.get("id").getAsString();
        BlockPos sign = readPos(obj.getAsJsonObject("tablichka"));
        BlockPos min = readPos(obj.getAsJsonObject("minPosition"));
        BlockPos max = readPos(obj.getAsJsonObject("maxPosition"));
        int price = obj.get("price").getAsInt();

        Property property = new Property(id, sign, min, max, price);

        if (obj.has("ownerUUID") && !obj.get("ownerUUID").isJsonNull()) {
            property.setOwnerUUID(UUID.fromString(obj.get("ownerUUID").getAsString()));
        }

        return property;
    }

    private JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", this.id);
        obj.add("tablichka", writePos(this.signPosition));
        obj.add("minPosition", writePos(this.minPosition));
        obj.add("maxPosition", writePos(this.maxPosition));
        obj.addProperty("price", this.price);
        obj.add("ownerUUID", this.ownerUUID != null ? new JsonPrimitive(this.ownerUUID.toString()) : JsonNull.INSTANCE);
        return obj;
    }

    private static BlockPos readPos(JsonObject obj) {
        return new BlockPos(
            obj.get("x").getAsInt(),
            obj.get("y").getAsInt(),
            obj.get("z").getAsInt()
        );
    }

    private static JsonObject writePos(BlockPos pos) {
        JsonObject obj = new JsonObject();
        obj.addProperty("x", pos.getX());
        obj.addProperty("y", pos.getY());
        obj.addProperty("z", pos.getZ());
        return obj;
    }
}
