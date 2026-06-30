package catworld;

import catworld.realEstateSystem.Property;
import catworld.realEstateSystem.RealEstateManager;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public class blocker {


public static void blockblock() {
    AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
        if (world.isClient()) return ActionResult.PASS;

            
        if (!hasPermission(player.getUuid(), pos)) {
            player.sendMessage(Text.literal("§cAqui no puede destruir blockes >:("), false);
            return ActionResult.FAIL;
        }

        return ActionResult.PASS; 
    });
}


    public static void changeblock() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient()) return ActionResult.PASS;

            BlockPos pos = hitResult.getBlockPos();

            if (hasPermission(player.getUuid(), pos)) {
                return ActionResult.PASS;
            }

            BlockState state = world.getBlockState(pos);

            if (state.isIn(BlockTags.TRAPDOORS)) {
                player.sendMessage(Text.literal("§cAqui no puede interactuar >:("), false);
                return ActionResult.FAIL;
            }

            if (state.getBlock() instanceof net.minecraft.block.ChestBlock || 
                state.isOf(net.minecraft.block.Blocks.ENDER_CHEST) || 
                state.isIn(BlockTags.SHULKER_BOXES)) {
                player.sendMessage(Text.literal("§cAqui no puede abrir cofres >:("), false);
                return ActionResult.FAIL;
            }

            if (!player.getStackInHand(hand).isEmpty() && player.getStackInHand(hand).getItem() instanceof net.minecraft.item.BlockItem) {
                player.sendMessage(Text.literal("§cAqui no puede colocar bloques >:("), false);
                return ActionResult.FAIL;
            }

            return ActionResult.PASS;
        });
    }

  
  private static boolean hasPermission(UUID playerUuid, BlockPos pos) {
        for (Property property : RealEstateManager.getAllProperties()) {
            if (property.containsCoordinate(pos)) {
                UUID owner = property.getOwnerUUID();
                
                if(owner != null || owner.equals(playerUuid)) {
                 return true;
                }
                
            }


        }
        return false; 
    }


}

