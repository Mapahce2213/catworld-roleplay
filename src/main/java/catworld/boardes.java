package catworld;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.lang.reflect.Field;

public class boardes {

    private static TrackedData<Text> TEXT_DATA_KEY = null;
    private static TrackedData<Byte> BILLBOARD_DATA_KEY = null;
    private static TrackedData<Integer> BACKGROUND_DATA_KEY = null;

    static {
        try {
            // 1. Достаем ключ ТЕКСТА
            Field textField;
            try {
                textField = DisplayEntity.TextDisplayEntity.class.getDeclaredField("TEXT");
            } catch (NoSuchFieldException e) {
                textField = DisplayEntity.TextDisplayEntity.class.getDeclaredField("field_42525");
            }
            textField.setAccessible(true);
            TEXT_DATA_KEY = (TrackedData<Text>) textField.get(null);

            // 2. Достаем ключ БИЛЛБОРДА (он находится в родительском классе DisplayEntity)
            Field billboardField;
            try {
                billboardField = DisplayEntity.class.getDeclaredField("BILLBOARD");
            } catch (NoSuchFieldException e) {
                billboardField = DisplayEntity.class.getDeclaredField("field_42403");
            }
            billboardField.setAccessible(true);
            BILLBOARD_DATA_KEY = (TrackedData<Byte>) billboardField.get(null);

            // 3. Достаем ключ ФОНА
            Field bgField;
            try {
                bgField = DisplayEntity.TextDisplayEntity.class.getDeclaredField("BACKGROUND");
            } catch (NoSuchFieldException e) {
                bgField = DisplayEntity.TextDisplayEntity.class.getDeclaredField("field_42523");
            }
            bgField.setAccessible(true);
            BACKGROUND_DATA_KEY = (TrackedData<Integer>) bgField.get(null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void reglas() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            ServerWorld world = server.getOverworld();
            if (world != null) {
                spawnLine(world, -32, 48.6, 343, "line1", "CatWorld RP", Formatting.GOLD, true);
                spawnLine(world, -32, 48.9, 343, "line2", "Es un servidor bueno", Formatting.WHITE, false);
            }
        });
    }

    private static void spawnLine(ServerWorld world, double x, double y, double z,
                                   String tag, String text, Formatting color, boolean bold) {
        

        // Создаем новую сущность Text Display
        DisplayEntity.TextDisplayEntity textDisplay = new DisplayEntity.TextDisplayEntity(EntityType.TEXT_DISPLAY, world);
        
        // Задаем координаты
        textDisplay.refreshPositionAndAngles(x, y, z, 0.0F, 0.0F);

        // Собираем чистый текст средствами Java (никакого JSON-парсинга строк в игре)
        Text textComponent = Text.literal(text).styled(style -> style.withColor(color).withBold(bold));
        
        // Безопасно пихаем все данные в DataTracker через вскрытые ключи
        if (TEXT_DATA_KEY != null) {
            textDisplay.getDataTracker().set(TEXT_DATA_KEY, textComponent);
        }
        if (BILLBOARD_DATA_KEY != null) {
            textDisplay.getDataTracker().set(BILLBOARD_DATA_KEY, (byte) 2); // 2 — это режим CENTER
        }
        if (BACKGROUND_DATA_KEY != null) {
            textDisplay.getDataTracker().set(BACKGROUND_DATA_KEY, 1073741824); // Цвет фона
        }

        // Добавляем командный тег
        textDisplay.addCommandTag(tag);

        // Спавним в мир
        world.spawnEntity(textDisplay);
    }
}
