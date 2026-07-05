package catworld;

import catworld.welcome;
import catworld.seguridad.Auth;
import catworld.seguridad.Joined;
import catworld.ciudadania.Ciudadanos;

import java.util.Map;
import java.util.HashMap;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.screen.HorseScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;

public class commandos {

    private static final Map<String, Ciudadanos> ciudadanosByCedula = new HashMap<>();

static {
    Ciudadanos.loadAll(ciudadanosByCedula);
}


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
                    player.getCommandSource(), "namenu"
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
        .then(CommandManager.argument("args", StringArgumentType.greedyString())
            .executes(context -> {
                context.getSource().sendError(Text.literal("Command prohibed! NO PROMOTIONS"));
                return 0;
            })
        )
        .executes(context -> {
            context.getSource().sendError(Text.literal("Command prohibed! NO PROMOTIONS"));
            return 0;
        })
); // No promotions of hosting

dispatcher.register(CommandManager.literal("namenu").executes(context -> {
    ServerPlayerEntity player = context.getSource().getPlayer();
    if (player == null) return 0;

    SimpleInventory feagueInventory = new SimpleInventory(2);

    ItemStack lightBlueGlass = new ItemStack(Items.LIGHT_BLUE_STAINED_GLASS_PANE);
    lightBlueGlass.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§bJugar!"));

    ItemStack redGlass = new ItemStack(Items.RED_STAINED_GLASS_PANE);
    redGlass.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§cDonacion"));

    feagueInventory.setStack(0, lightBlueGlass);
    feagueInventory.setStack(1, redGlass);

    AbstractHorseEntity gingerHorse = new HorseEntity(EntityType.HORSE, player.getWorld());

    player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
        (syncId, playerInv, p) -> new HorseScreenHandler(syncId, playerInv, feagueInventory, gingerHorse, 2),
        Text.literal("Menu of server")
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
    if (player == null) return 0;

    String playerUuid = player.getUuid().toString();
    Ciudadanos ciudadano = Ciudadanos.findByPassport(ciudadanosByCedula, playerUuid);

    if (ciudadano == null) {
        player.sendMessage(
            Text.literal("Por favor escribe /register para registrar!").formatted(Formatting.GOLD, Formatting.BOLD),
            false
        );
        return 1;
    }

    player.getInventory().main.clear();
    DefaultedList<ItemStack> worldInv = welcome.getWorldInventory(player.getUuid());
    for (int i = 0; i < worldInv.size(); i++) {
        player.getInventory().setStack(i, worldInv.get(i));
    }

    ServerWorld world = player.getServerWorld();
    BlockPos pos = new BlockPos(40, 1, 68);
    player.teleport(world, pos.getX(), pos.getY(), pos.getZ(), player.getYaw(), player.getPitch());
    player.sendMessage(Text.literal("Bien provecho!").formatted(Formatting.GOLD), false);

    return 1;
}));


// auth register <email> <contrasena> <repita>
// auth login <constrasena>

// /auth register <email> <contrasena> <repita>
// /auth login <contrasena>
dispatcher.register(
    CommandManager.literal("auth")
        .then(CommandManager.literal("register")
            .then(CommandManager.argument("email", StringArgumentType.word())
                .then(CommandManager.argument("contrasena", StringArgumentType.word())
                    .then(CommandManager.argument("repita", StringArgumentType.word())
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayer();
                            String email = StringArgumentType.getString(context, "email");
                            String password = StringArgumentType.getString(context, "contrasena");
                            String repeat = StringArgumentType.getString(context, "repita");

                            if (!password.equals(repeat)) {
                                player.sendMessage(
                                    Text.literal("Las contraseñas no coinciden").formatted(Formatting.RED),
                                    false
                                );
                                return 0;
                            }

                            String uuid = player.getUuid().toString();
                            String ip = player.getIp();
                            Map<String, Auth> authByUuid = Joined.getAuthByUuid();

                            boolean success = Auth.register(authByUuid, uuid, email, password, ip);

                            if (!success) {
                                player.sendMessage(
                                    Text.literal("Ya estás registrado!").formatted(Formatting.RED),
                                    false
                                );
                                return 0;
                            }

                            Joined.markAuthenticated(player.getUuid());

                            player.sendMessage(
                                Text.literal("Registro exitoso!").formatted(Formatting.GREEN),
                                false
                            );

                            return 1;
                        })
                    )
                )
            )
        )
        .then(CommandManager.literal("login")
            .then(CommandManager.argument("contrasena", StringArgumentType.word())
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    String password = StringArgumentType.getString(context, "contrasena");
                    String uuid = player.getUuid().toString();
                    String ip = player.getIp();
                    Map<String, Auth> authByUuid = Joined.getAuthByUuid();

                    boolean success = Auth.login(authByUuid, uuid, password, ip);

                    if (!success) {
                        player.sendMessage(
                            Text.literal("Contraseña incorrecta o no estás registrado").formatted(Formatting.RED),
                            false
                        );
                        return 0;
                    }

                    Joined.markAuthenticated(player.getUuid());

                    player.sendMessage(
                        Text.literal("Has iniciado sesión correctamente!").formatted(Formatting.GREEN),
                        false
                    );

                    return 1;
                })
            )
        )
);
            
            
            
 // register <nombre> <apedido> <edad> [Male\Female]
dispatcher.register(
    CommandManager.literal("register")
        .then(CommandManager.argument("nombre", StringArgumentType.word())
            .then(CommandManager.argument("apedido", StringArgumentType.word())
                .then(CommandManager.argument("edad", IntegerArgumentType.integer(0, 120))
                    .then(CommandManager.argument("gender", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            builder.suggest("Male");
                            builder.suggest("Female");
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayer();
                            String nombre = StringArgumentType.getString(context, "nombre");
                            String apedido = StringArgumentType.getString(context, "apedido");
                            int edad = IntegerArgumentType.getInteger(context, "edad");
                            String gender = StringArgumentType.getString(context, "gender");

                            if (!gender.equalsIgnoreCase("Male") && !gender.equalsIgnoreCase("Female")) {
                                player.sendMessage(
                                    Text.literal("Gender debe ser Male o Female").formatted(Formatting.RED),
                                    false
                                );
                                return 0;
                            }

                            String playerUuid = player.getUuid().toString();

                            if (Ciudadanos.findByPassport(ciudadanosByCedula, playerUuid) != null) {
                                player.sendMessage(
                                    Text.literal("Ya estás registrado!").formatted(Formatting.RED),
                                    false
                                );
                                return 0;
                            }

                            Ciudadanos ciudadano = new Ciudadanos();
                            ciudadano.setName(nombre);
                            ciudadano.setApedido(apedido);
                            ciudadano.setNickname(player.getGameProfile().getName());
                            ciudadano.setEdad(edad);
                            ciudadano.setGender(gender);
                            ciudadano.setPassport(playerUuid);
                            ciudadano.setCedula(playerUuid);

                            ciudadanosByCedula.put(ciudadano.getCedula(), ciudadano);
                            Ciudadanos.save(ciudadanosByCedula);

                            Text message = Text.literal(
                                player.getName().getString() + " se registró como " + nombre + " " + apedido +
                                " (" + edad + " años, " + gender + ")"
                            ).formatted(Formatting.YELLOW);

                            player.sendMessage(message, false);

                            player.getServer().getCommandManager().executeWithPrefix(
                                player.getCommandSource(), "game"
                            );

                            return 1;
                        })
                    )
                )
            )
        )
);

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
