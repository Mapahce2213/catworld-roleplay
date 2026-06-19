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

            return TypedActionResult.pass(stack);
        });
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {

            dispatcher.register(
                CommandManager.literal("tellraw")
                    .requires(source -> false) // никто и никогда не сможет её выполнить
            );

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
