package ru.maxthetomas.plugins.blackymare.noelytrainend;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public final class NoElytraInEnd extends JavaPlugin implements Listener {
    boolean enabled;
    boolean disableForOPs;
    String message;
    String messageIfFull;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        enabled = getConfig().getBoolean("enabled", false);
        disableForOPs = getConfig().getBoolean("disableForOPs", false);
        message = getConfig().getString("message", "You cannot use elytra in the end on this server.");
        messageIfFull = getConfig().getString("messageIfFull", "Your elytra were dropped on the ground because you didn't have enough space in your inventory.");

        if (enabled)
            Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler public void onMovement(PlayerMoveEvent e) {
        if (!enabled) return;

        if (e.getPlayer().getWorld().getEnvironment() != World.Environment.THE_END) return;
        if (e.getPlayer().isOp() && disableForOPs) return;

        if (e.getPlayer().getInventory().getChestplate() == null) return;

        if (e.getPlayer().getInventory().getChestplate().getType() == Material.ELYTRA) {
            // Save stack from chestplate slot and replace it with air
            ItemStack stack = e.getPlayer().getInventory().getChestplate();
            e.getPlayer().getInventory().setChestplate(new ItemStack(Material.AIR));

            // If it was dropped on the ground, notify player
            AtomicBoolean droppedOnTheGround = new AtomicBoolean(false);

            HashMap<Integer, ItemStack> didntFit = e.getPlayer().getInventory().addItem(stack);
            didntFit.forEach((integer, itemStack) -> {
                droppedOnTheGround.set(true);
                e.getPlayer().getWorld().dropItem(e.getPlayer().getLocation(), itemStack);
            });

            // Send messages
            e.getPlayer().sendMessage(Component.text(ChatColor.RED + message));
            if (droppedOnTheGround.get())
                e.getPlayer().sendMessage(Component.text(ChatColor.RED + messageIfFull));
        }
    }
}
