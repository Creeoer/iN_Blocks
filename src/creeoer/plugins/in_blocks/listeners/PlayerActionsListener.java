package creeoer.plugins.in_blocks.listeners;

import creeoer.plugins.in_blocks.builders.BlockBuilder;
import creeoer.plugins.in_blocks.main.iN_Blocks;
import creeoer.plugins.in_blocks.objects.BuildManager;
import creeoer.plugins.in_blocks.objects.BuildTask;
import creeoer.plugins.in_blocks.objects.IgnoredMaterial;
import creeoer.plugins.in_blocks.objects.Lang;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

/**
 * Handles player actions related to build tasks.
 * Includes chest interaction protection, task resumption, and anvil prevention.
 *
 * @author Creeoer
 * @version 2.0.0
 */
public class PlayerActionsListener implements Listener {

    private final iN_Blocks plugin;
    private final BuildManager buildManager;

    /**
     * Creates a new PlayerActionsListener.
     *
     * @param plugin The main plugin instance
     */
    public PlayerActionsListener(iN_Blocks plugin) {
        this.plugin = plugin;
        this.buildManager = plugin.getBuildManager();
    }

    /**
     * Handles build chest breaking.
     * Only the owner can break their build chest, which cancels the task.
     *
     * @param event The block break event
     */
    @EventHandler
    public void onChestBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        if (!(block.getState() instanceof Chest)) {
            return;
        }

        Chest chest = (Chest) block.getState();

        // Check if this is a build chest
        for (BuildTask task : buildManager.getAllTasks()) {
            if (!chest.getLocation().equals(task.getBuildChest().getChestLocation())) {
                continue;
            }

            // Check ownership
            if (!task.getOwnerName().equals(player.getName())) {
                player.sendMessage(ChatColor.RED + Lang.BREAK_CHEST_OWNER.toString());
                event.setCancelled(true);
                return;
            }

            // Owner breaking their own chest - cancel build and refund
            player.sendMessage(ChatColor.YELLOW + Lang.BREAK_CHEST.toString());

            // Refund the schematic item
            refundSchematicItem(player, task);

            // Drop placed blocks
            dropPlacedBlocks(player, task);

            // Clear the build and remove task
            task.clearBuild();
            buildManager.removeTask(task);
            return;
        }
    }

    /**
     * Handles build chest interaction.
     * Only the owner can interact with their build chest.
     *
     * @param event The player interact event
     */
    @EventHandler
    public void onChestInteraction(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null || !(block.getState() instanceof Chest)) {
            return;
        }

        Player player = event.getPlayer();

        // Check if this is a build chest
        for (BuildTask task : buildManager.getAllTasks()) {
            if (!block.getLocation().equals(task.getBuildChest().getChestLocation())) {
                continue;
            }

            // Check ownership
            if (!task.getOwnerName().equals(player.getName())) {
                player.sendMessage(ChatColor.RED + Lang.INTERACT_CHEST.toString());
                event.setCancelled(true);
                return;
            }

            // Owner can interact
            return;
        }
    }

    /**
     * Resumes saved build tasks when a player joins.
     *
     * @param event The player join event
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();

        if (!buildManager.hasTask(playerName)) {
            return;
        }

        try {
            BuildTask task = buildManager.startTask(playerName);
            if (task != null) {
                player.sendMessage(ChatColor.YELLOW + "Your build has been resumed!");
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to resume build for " + playerName, e);
        }
    }

    /**
     * Saves build tasks when a player leaves.
     *
     * @param event The player quit event
     */
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        String playerName = event.getPlayer().getName();

        for (BuildTask task : buildManager.getAllTasks()) {
            if (task.getOwnerName().equals(playerName)) {
                buildManager.saveTask(task);
                return;
            }
        }
    }

    /**
     * Prevents renaming schematic items in anvils.
     *
     * @param event The inventory click event
     */
    @EventHandler
    public void onAnvilUse(InventoryClickEvent event) {
        if (!(event.getClickedInventory() instanceof AnvilInventory)) {
            return;
        }

        InventoryView view = event.getView();

        // Check all slots in the anvil (input and output)
        for (int slot = 0; slot < 3; slot++) {
            ItemStack item = view.getItem(slot);
            if (item == null || !item.hasItemMeta()) {
                continue;
            }

            if (isSchematicItem(item)) {
                event.getWhoClicked().sendMessage(ChatColor.RED + Lang.ANVIL.toString());
                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * Checks if an item is a schematic item.
     *
     * @param item The item to check
     * @return true if the item is a schematic
     */
    private boolean isSchematicItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        if (meta.hasDisplayName() && meta.getDisplayName().contains("schematic")) {
            return true;
        }

        if (meta.hasLore()) {
            List<String> lore = meta.getLore();
            if (lore != null) {
                for (String line : lore) {
                    if (line.contains("schematic")) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Refunds the schematic item to the player.
     *
     * @param player The player to refund
     * @param task The build task
     */
    private void refundSchematicItem(Player player, BuildTask task) {
        BlockBuilder builder = new BlockBuilder(task.getSchematic().getName(), plugin);
        builder.setAmount(1);
        player.getInventory().addItem(builder.build());
        player.updateInventory();
    }

    /**
     * Drops the blocks that were placed during the build.
     *
     * @param player The player (for drop location)
     * @param task The build task
     */
    private void dropPlacedBlocks(Player player, BuildTask task) {
        Location dropLocation = player.getLocation();

        for (ItemStack item : task.getCurrentBlocksInBuild()) {
            if (item != null && !IgnoredMaterial.isIgnoredMaterial(item.getType())) {
                player.getWorld().dropItem(dropLocation, item);
            }
        }
    }
}
