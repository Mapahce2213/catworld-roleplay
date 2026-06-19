package catworld;
import catworld.commandos;
import catworld.tablist;
import catworld.boardes;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Formatting;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.server.world.ServerWorld;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

import net.minecraft.util.collection.DefaultedList;

import net.minecraft.component.DataComponentTypes;


import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.Hand;

import net.minecraft.item.Items;
import net.minecraft.component.DataComponentTypes;

import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;


import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;



import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity.TextDisplayEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Formatting;



public class welcome implements ModInitializer {

private static final Map<UUID, DefaultedList<ItemStack>> spawnInventories = new HashMap<>();
private static final Map<UUID, DefaultedList<ItemStack>> worldInventories = new HashMap<>();



	@Override
    public void onInitialize() {
        System.out.println("Catworld модификация была активированна | El modo has activado");

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.player;




    DefaultedList<ItemStack> currentInv = DefaultedList.ofSize(player.getInventory().main.size(), ItemStack.EMPTY);
    for (int i = 0; i < player.getInventory().main.size(); i++) {
        currentInv.set(i, player.getInventory().main.get(i).copy());
    }
    saveWorldInventory(player.getUuid(), currentInv);

    // всегда загружаем одинаковый спавновский инвентарь
    DefaultedList<ItemStack> spawnInv = getSpawnInv();

    player.getInventory().main.clear();
    for (int i = 0; i < spawnInv.size(); i++) {
        player.getInventory().setStack(i, spawnInv.get(i));
    
    }
    
    
    
        ServerWorld world = player.getServerWorld();
    BlockPos pos = new BlockPos(-38, 47, 343);
    player.teleport(world, pos.getX(), pos.getY(), pos.getZ(), player.getYaw(), player.getPitch());
    
    
if (player.getStatHandler().getStat(net.minecraft.stat.Stats.CUSTOM.getOrCreateStat(net.minecraft.stat.Stats.PLAY_TIME)) > 0) {
    server.getPlayerManager().broadcast(Text.literal("[").append(Text.literal("+").formatted(Formatting.GREEN)).append(Text.literal("] El jugador has unido")), false);
} else {
    server.getPlayerManager().broadcast(Text.literal("[").append(Text.literal("+").formatted(Formatting.GREEN)).append(Text.literal("] Otro nuevo mas")), false);
}

            player.sendMessage(Text.literal("§l§6================CatWorld RP================"), false);
            player.sendMessage(Text.literal("§aBienvenido! Felicitamos que eligiste a nosotros!"), false);
            player.sendMessage(Text.literal("§e1. Descarge los modos"), false);
            player.sendMessage(Text.literal("§e2. Prende resourcepacks"), false);
            player.sendMessage(Text.literal("§e2. Usa commandos /me /do para RP!"), false);
            player.sendMessage(Text.literal("§e3. Tenemos grupo en Facebook"), false);
            player.sendMessage(Text.literal("§l§6================Buen provecho!================"), false);



        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            server.getPlayerManager().broadcast(Text.literal(""), false);
        });

        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            player.sendMessage(Text.literal("§cAqui no puede destruir blockes >:("), false);
            return ActionResult.FAIL;
        });

commandos.register();
boardes.reglas();
commandos.item();

tablist.register();


ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> {
    // Формируем текст
    Text localMessage = Text.literal("[" + sender.getName().getString() + "] " + "[ADMIN] " + message.getContent().getString());

    // Рассылаем только игрокам поблизости
    for (ServerPlayerEntity target : sender.getServer().getPlayerManager().getPlayerList()) {
        double distance = sender.getBlockPos().getManhattanDistance(target.getBlockPos());
        if (distance <= 20) {
            target.sendMessage(localMessage, false);
        }
    }
    // ❌ Никакого return — этот колбэк void
});




    
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            System.out.println("Мод был остановлен вручную | El modo has parado manual");
        });
    }
    
public static DefaultedList<ItemStack> getSpawnInv() {
    DefaultedList<ItemStack> inv = DefaultedList.ofSize(36, ItemStack.EMPTY);

    ItemStack emerald = new ItemStack(Items.EMERALD);
    emerald.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Join to game").formatted(Formatting.GOLD));
    inv.set(0, emerald);

    ItemStack compass = new ItemStack(Items.COMPASS);
    compass.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Navigation").formatted(Formatting.GREEN));
    inv.set(1, compass);

    return inv;
}


    public static void saveWorldInventory(UUID uuid, DefaultedList<ItemStack> inv) {
        worldInventories.put(uuid, inv);
    }

    public static DefaultedList<ItemStack> getWorldInventory(UUID uuid) {
        return worldInventories.getOrDefault(uuid, DefaultedList.ofSize(36, ItemStack.EMPTY));
    }


    
    
    
}
