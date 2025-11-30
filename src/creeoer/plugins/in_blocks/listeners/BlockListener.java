package creeoer.plugins.in_blocks.listeners;

import creeoer.plugins.in_blocks.events.iNPlaceEvent;
import creeoer.plugins.in_blocks.main.BuildSchematic;
import creeoer.plugins.in_blocks.main.PUtils;
import creeoer.plugins.in_blocks.main.RegionManager;
import creeoer.plugins.in_blocks.main.SchematicManager;
import creeoer.plugins.in_blocks.main.iN_Blocks;
import creeoer.plugins.in_blocks.objects.BuildManager;
import creeoer.plugins.in_blocks.objects.Lang;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.ValidatingPrompt;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * Handles block placement events for schematic items.
 * When a player places a schematic chest, this listener shows a preview
 * and initiates a conversation for confirmation.
 *
 * @author Creeoer
 * @version 2.0.0
 */
public class BlockListener implements Listener {

    private final iN_Blocks plugin;
    private final SchematicManager schematicManager;
    private final RegionManager regionManager;
    private final BuildManager buildManager;
    private final FileConfiguration config;

    /**
     * Creates a new BlockListener.
     *
     * @param plugin The main plugin instance
     */
    public BlockListener(iN_Blocks plugin) {
        this.plugin = plugin;
        this.schematicManager = plugin.getSchematicManager();
        this.regionManager = plugin.getRegionManager();
        this.buildManager = plugin.getBuildManager();
        this.config = plugin.getConfig();
    }

    /**
     * Handles block placement events for schematic items.
     *
     * @param event The block place event
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        Player player = event.getPlayer();

        // Check if the placed item is a schematic chest
        if (!isSchematicItem(item)) {
            return;
        }

        // Always cancel the event - we handle placement ourselves
        event.setCancelled(true);

        // Extract schematic name from item
        String schematicName = extractSchematicName(item);
        if (schematicName == null) {
            return;
        }

        // Get the schematic
        BuildSchematic schematic = schematicManager.getSchematic(schematicName);
        if (schematic == null || !schematic.isValid()) {
            player.sendMessage(ChatColor.RED + Lang.EXISTS.toString());
            return;
        }

        // Check permission for protected schematics
        if (schematicManager.hasPermission(schematicName) &&
                !player.hasPermission("in." + schematicName)) {
            player.sendMessage(ChatColor.RED + Lang.NO_PERM.toString());
            return;
        }

        // Check if player is already in preview mode
        if (player.isConversing()) {
            player.sendRawMessage(ChatColor.RED + Lang.PREVIEW_ERROR.toString());
            return;
        }

        // Check if player already has a build in progress
        if (buildManager.hasTask(player.getName())) {
            player.sendRawMessage(ChatColor.RED + "You already have a build in progress!");
            return;
        }

        // Get placement location and facing direction
        Location placementLocation = event.getBlock().getLocation();
        Block chestBlock = placementLocation.getBlock();
        BlockFace facing = PUtils.getCardinalDirection(player.getLocation().getYaw(), false);

        // Rotate schematic to match player facing
        schematic.rotateAndSetBoardOrigin(placementLocation, facing);

        // Check region protection
        if (!regionManager.canPlayerPlace(player, schematic, placementLocation)) {
            player.sendMessage(ChatColor.RED + Lang.BAD_PLACE.toString());
            return;
        }

        // Show preview
        schematic.preview(player, placementLocation);

        // Start confirmation conversation
        Conversation conversation = createConfirmationConversation(
                player, schematic, placementLocation, chestBlock, item
        );
        conversation.begin();
    }

    /**
     * Checks if an ItemStack is a schematic item.
     *
     * @param item The item to check
     * @return true if the item is a schematic
     */
    private boolean isSchematicItem(ItemStack item) {
        if (item == null || item.getType() != Material.CHEST) {
            return false;
        }

        if (!item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        // Check display name for schematic marker
        if (meta.hasDisplayName() && meta.getDisplayName().contains("schematic")) {
            return true;
        }

        // Check lore for schematic marker
        if (meta.hasLore()) {
            List<String> lore = meta.getLore();
            if (lore != null && !lore.isEmpty() && lore.get(0).contains("schematic")) {
                return true;
            }
        }

        return false;
    }

    /**
     * Extracts the schematic name from an item's metadata.
     *
     * @param item The schematic item
     * @return The schematic name, or null if not found
     */
    private String extractSchematicName(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }

        String text;
        boolean useLore = config.getBoolean("Options.use-lore", false);

        if (useLore) {
            List<String> lore = meta.getLore();
            if (lore == null || lore.isEmpty()) {
                return null;
            }
            text = lore.get(0);
        } else {
            if (!meta.hasDisplayName()) {
                return null;
            }
            text = meta.getDisplayName();
        }

        // Extract name from "SchematicName schematic" format
        String[] parts = ChatColor.stripColor(text).split("\\s+");
        if (parts.length > 0) {
            return parts[0];
        }

        return null;
    }

    /**
     * Creates the confirmation conversation for schematic placement.
     *
     * @param player The player
     * @param schematic The schematic to place
     * @param location The placement location
     * @param chestBlock The chest block
     * @param item The schematic item
     * @return The configured Conversation
     */
    private Conversation createConfirmationConversation(
            Player player,
            BuildSchematic schematic,
            Location location,
            Block chestBlock,
            ItemStack item) {

        int previewTimeout = config.getInt("Options.preview-time", 10);
        final boolean[] conversationComplete = {false};

        ConversationFactory factory = new ConversationFactory(plugin)
                .withFirstPrompt(new ValidatingPrompt() {
                    @Override
                    public String getPromptText(ConversationContext context) {
                        return ChatColor.GREEN + Lang.PREVIEW.toString()
                                .replace("%n", String.valueOf(previewTimeout));
                    }

                    @Override
                    protected boolean isInputValid(ConversationContext context, String input) {
                        return true; // Accept any input
                    }

                    @Override
                    protected Prompt acceptValidatedInput(ConversationContext context, String input) {
                        if (!input.equalsIgnoreCase("yes")) {
                            // Cancel placement
                            schematic.unloadPreview(player, location);
                            player.sendRawMessage(ChatColor.RED + Lang.CANCEL.toString());
                            conversationComplete[0] = true;
                            return Prompt.END_OF_CONVERSATION;
                        }

                        // Fire custom event for other plugins
                        iNPlaceEvent placeEvent = new iNPlaceEvent(
                                schematic.getName(), player, location, chestBlock, schematic
                        );
                        Bukkit.getServer().getPluginManager().callEvent(placeEvent);

                        // Unload preview
                        schematic.unloadPreview(player, location);

                        // Check if event was cancelled
                        if (placeEvent.isCancelled()) {
                            player.sendRawMessage(ChatColor.RED + Lang.CANCEL.toString());
                            conversationComplete[0] = true;
                            return Prompt.END_OF_CONVERSATION;
                        }

                        // Create the build task
                        buildManager.createNewTask(schematic, chestBlock, player.getName());

                        // Success message
                        player.sendRawMessage(ChatColor.YELLOW + Lang.SUCCESS_PLACE.toString());

                        // Consume item
                        consumeItem(player, item);

                        conversationComplete[0] = true;
                        return Prompt.END_OF_CONVERSATION;
                    }
                })
                .withTimeout(previewTimeout)
                .withModality(true)
                .addConversationAbandonedListener(new ConversationAbandonedListener() {
                    @Override
                    public void conversationAbandoned(ConversationAbandonedEvent event) {
                        if (!conversationComplete[0]) {
                            schematic.unloadPreview(player, location);
                            player.sendRawMessage(ChatColor.RED + "Preview timed out!");
                        }
                    }
                });

        return factory.buildConversation(player);
    }

    /**
     * Consumes one schematic item from the player's inventory.
     *
     * @param player The player
     * @param item The item to consume
     */
    private void consumeItem(Player player, ItemStack item) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        // Check which hand has the item
        ItemStack targetItem = null;
        boolean isMainHand = false;

        if (mainHand != null && mainHand.isSimilar(item)) {
            targetItem = mainHand;
            isMainHand = true;
        } else if (offHand != null && offHand.isSimilar(item)) {
            targetItem = offHand;
        }

        if (targetItem == null) {
            return;
        }

        if (targetItem.getAmount() > 1) {
            targetItem.setAmount(targetItem.getAmount() - 1);
        } else {
            if (isMainHand) {
                player.getInventory().setItemInMainHand(null);
            } else {
                player.getInventory().setItemInOffHand(null);
            }
        }

        player.updateInventory();
    }
}
