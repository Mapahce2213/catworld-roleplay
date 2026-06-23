package catworld;

import catworld.welcome;
import catworld.realEstateSystem.Property;
import catworld.realEstateSystem.RealEstateManager;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public class blocker {

    public static void blockblock() {

        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (world.isClient()) return ActionResult.PASS;

            if (hasPermission(player.getUuid(), pos)) {
                return ActionResult.PASS;
            }

            player.sendMessage(Text.literal("§cAqui no puede destruir blockes >:("), false);
            return ActionResult.FAIL;
        });

    }

    public static void changeblock() {

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient()) return ActionResult.PASS;

            BlockPos pos = hitResult.getBlockPos();

            if (hasPermission(player.getUuid(), pos)) {
                return ActionResult.PASS;
            }

            player.sendMessage(Text.literal("§cAqui no puede colocar bloques o interactuar >:("), false);
            return ActionResult.FAIL;
        });

    }

    /**
     * Recorre las propiedades de realEstateSystem/Property.java y comprueba:
     * - si pos está dentro del área (min..max) de alguna propiedad,
     *   solo el dueño (ownerUUID) tiene permiso.
     * - si pos no pertenece a ninguna propiedad registrada, se permite.
     */
    private static boolean hasPermission(UUID playerUuid, BlockPos pos) {
        for (Property property : RealEstateManager.getAllProperties()) {
            if (property.containsCoordinate(pos)) {
                UUID owner = property.getOwnerUUID();
                return owner != null && owner.equals(playerUuid);
            }
        }
        return true;
    }

}
