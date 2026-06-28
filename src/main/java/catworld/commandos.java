package catworld;
import catworld.welcome;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.screen.HorseScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.Hand;

import net.minecraft.item.Items;
import net.minecraft.component.DataComponentTypes;

import com.mojang.brigadier.arguments.StringArgumentType;

public class commandos {

    public static void item() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);

            if (stack.getItem() == Items.EMERALD &&
                stack.get(DataComponentTypes.CUSTOM_NAME) != null &&
                stack.get(DataComponentTypes.CUSTOM_NAME).getString().contains("Join to game")) {

                player.getServer().getCommandManager().executeWithPrefix(
                    player.getCommandSource(), "game"
                );

                return TypedActionResult.success(stack);
            }
            
            
                  if (stack.getItem() == Items.COMPASS &&
                stack.get(DataComponentTypes.CUSTOM_NAME) != null &&
                stack.get(DataComponentTypes.CUSTOM_NAME).getString().contains("Navigation")) {

                player.getServer().getCommandManager().executeWithPrefix(
                    player.getCommandSource(), "menu"
                );

                return TypedActionResult.success(stack);
            }
            

            return TypedActionResult.pass(stack);
        });
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {

            dispatcher.register(
                CommandManager.literal("tellraw")
                    .requires(source -> false)
            );


dispatcher.register(CommandManager.literal("menu").executes(context -> {
    ServerPlayerEntity player = context.getSource().getPlayer();
    if (player == null) return 0;

    SimpleInventory menuInventory = new SimpleInventory(54);

    ItemStack lightBlueGlass = new ItemStack(Items.LIGHT_BLUE_STAINED_GLASS_PANE);
    lightBlueGlass.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§bJugar!"));

    ItemStack redGlass = new ItemStack(Items.RED_STAINED_GLASS_PANE);
    redGlass.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§cDonacion"));

    ItemStack blueGlass = new ItemStack(Items.BLUE_STAINED_GLASS_PANE);
    blueGlass.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§9Ayuda"));

    ItemStack borderGlass = new ItemStack(Items.BLACK_STAINED_GLASS_PANE);
    borderGlass.set(DataComponentTypes.CUSTOM_NAME, Text.empty());

    for (int row = 0; row < 6; row++) {
        for (int col = 0; col < 9; col++) {
            int slot = row * 9 + col;

            if (col < 2 || col > 6) {
                menuInventory.setStack(slot, borderGlass.copy());
                continue;
            }

            if (row == 0 || row == 1) {
                menuInventory.setStack(slot, lightBlueGlass.copy());
            } else if (row >= 2 && row <= 4) {
                menuInventory.setStack(slot, redGlass.copy());
            } else if (row == 5) {
                menuInventory.setStack(slot, blueGlass.copy());
            }
        }
    }

      player.openHandledScreen(new net.minecraft.screen.SimpleNamedScreenHandlerFactory(
        (syncId, playerInv, p) -> net.minecraft.screen.GenericContainerScreenHandler.createGeneric9x6(syncId, playerInv, menuInventory),
        Text.literal("Catworld Menu")
    ));

    return 1;
}));

            
            
            // /spawn
            dispatcher.register(CommandManager.literal("spawn").executes(context -> {
                ServerPlayerEntity player = context.getSource().getPlayer();
                if (player != null) {
                    DefaultedList<ItemStack> currentInv = DefaultedList.ofSize(player.getInventory().main.size(), ItemStack.EMPTY);
                    for (int i = 0; i < player.getInventory().main.size(); i++) {
                        currentInv.set(i, player.getInventory().main.get(i).copy());
                    }
                    welcome.saveWorldInventory(player.getUuid(), currentInv);

                    DefaultedList<ItemStack> spawnInv = welcome.getSpawnInv();
                    player.getInventory().main.clear();
                    for (int i = 0; i < spawnInv.size(); i++) {
                        player.getInventory().setStack(i, spawnInv.get(i));
                    }

                    ServerWorld world = player.getServerWorld();
                    BlockPos pos = new BlockPos(-38, 47, 343);
                    player.teleport(world, pos.getX(), pos.getY(), pos.getZ(), player.getYaw(), player.getPitch());
                    player.sendMessage(Text.literal("Has teleportado al spawn").formatted(Formatting.GOLD), false);
                }
                return 1;
            }));

            // /game
            dispatcher.register(CommandManager.literal("game").executes(context -> {
                ServerPlayerEntity player = context.getSource().getPlayer();
                if (player != null) {
                    player.getInventory().main.clear();
                    DefaultedList<ItemStack> worldInv = welcome.getWorldInventory(player.getUuid());
                    for (int i = 0; i < worldInv.size(); i++) {
                        player.getInventory().setStack(i, worldInv.get(i));
                    }

                    ServerWorld world = player.getServerWorld();
                    BlockPos pos = new BlockPos(40, 1, 68);
                    player.teleport(world, pos.getX(), pos.getY(), pos.getZ(), player.getYaw(), player.getPitch());
                    player.sendMessage(Text.literal("Bien provecho!").formatted(Formatting.GOLD), false);
                }
                return 1;
            }));

            // /rpme <action>
            dispatcher.register(
                CommandManager.literal("rpme")
                    .then(CommandManager.argument("action", StringArgumentType.greedyString())
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayer();
                            String action = StringArgumentType.getString(context, "action");

                            Text message = Text.literal(player.getName().getString() + " " + action)
                                .formatted(Formatting.LIGHT_PURPLE);

                            for (ServerPlayerEntity target : player.getServer().getPlayerManager().getPlayerList()) {
                                double distance = player.getBlockPos().getManhattanDistance(target.getBlockPos());
                                if (distance <= 20) {
                                    target.sendMessage(message, false);
                                }
                            }
                            return 1;
                        })
                    )
            );

            // /rpdo <action>
            dispatcher.register(
                CommandManager.literal("rpdo")
                    .then(CommandManager.argument("action", StringArgumentType.greedyString())
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayer();
                            String action = StringArgumentType.getString(context, "action");

                            Text message = Text.literal(action)
                              .formatted(Formatting.YELLOW);

                            for (ServerPlayerEntity target : player.getServer().getPlayerManager().getPlayerList()) {
                                double distance = player.getBlockPos().getManhattanDistance(target.getBlockPos());
                                if (distance <= 20) {
                                    target.sendMessage(message, false);
                                }
                            }
                            return 1;
                        })
                    )
            );
        });
    }
}
