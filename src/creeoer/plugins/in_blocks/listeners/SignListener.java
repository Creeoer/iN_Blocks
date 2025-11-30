package creeoer.plugins.in_blocks.listeners;

import creeoer.plugins.in_blocks.builders.BlockBuilder;
import creeoer.plugins.in_blocks.main.BuildSchematic;
import creeoer.plugins.in_blocks.main.SchematicManager;
import creeoer.plugins.in_blocks.main.iN_Blocks;
import creeoer.plugins.in_blocks.objects.Lang;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Handles shop sign creation and interaction.
 * Players can buy schematic items from signs by right-clicking them.
 *
 * <p>Sign format:
 * <ul>
 *   <li>Line 1: [iN_Blocks] (configurable)</li>
 *   <li>Line 2: Price</li>
 *   <li>Line 3: Schematic name</li>
 * </ul>
 * </p>
 *
 * @author Creeoer
 * @version 2.0.0
 */
public class SignListener implements Listener {

    private final iN_Blocks plugin;
    private final SchematicManager schematicManager;

    /**
     * Creates a new SignListener.
     *
     * @param plugin The main plugin instance
     */
    public SignListener(iN_Blocks plugin) {
        this.plugin = plugin;
        this.schematicManager = plugin.getSchematicManager();
    }

    /**
     * Handles shop sign creation.
     *
     * @param event The sign change event
     */
    @EventHandler
    public void onSignDefine(SignChangeEvent event) {
        String signIdentifier = ChatColor.stripColor(
                plugin.getConfig().getString("Options.sign-name", "[iN_Blocks]")
        );

        // Check if this is an iN_Blocks shop sign
        if (!event.getLine(0).equalsIgnoreCase(signIdentifier)) {
            return;
        }

        Player player = event.getPlayer();

        // Check permission
        if (!player.hasPermission("in.sign.define")) {
            player.sendMessage(ChatColor.RED + Lang.NO_PERM.toString());
            event.setCancelled(true);
            return;
        }

        // Validate price (line 2)
        String priceString = event.getLine(1);
        double price;
        try {
            price = Double.parseDouble(priceString);
            if (price < 0) {
                throw new NumberFormatException("Negative price");
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + Lang.INVALID_SIGN.toString());
            player.sendMessage(ChatColor.RED + "Line 2 must be a valid price!");
            event.setCancelled(true);
            return;
        }

        // Validate schematic name (line 3)
        String schematicName = event.getLine(2);
        if (schematicName == null || schematicName.isEmpty()) {
            player.sendMessage(ChatColor.RED + Lang.INVALID_SIGN.toString());
            player.sendMessage(ChatColor.RED + "Line 3 must be a schematic name!");
            event.setCancelled(true);
            return;
        }

        // Check if schematic exists
        if (!schematicManager.doesExist(schematicName)) {
            player.sendMessage(ChatColor.RED + Lang.EXISTS.toString());
            event.setCancelled(true);
            return;
        }

        // Format the sign
        event.setLine(0, plugin.getConfig().getString("Options.sign-name"));
        event.setLine(1, ChatColor.GREEN + "$" + String.format("%.2f", price));
        event.setLine(2, ChatColor.YELLOW + schematicName);

        player.sendMessage(ChatColor.GREEN + Lang.VALID_SIGN.toString());
    }

    /**
     * Handles shop sign interaction (purchasing).
     *
     * @param event The player interact event
     */
    @EventHandler
    public void onSignClick(PlayerInteractEvent event) {
        // Only handle right-click on block
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (event.getClickedBlock() == null) {
            return;
        }

        BlockState state = event.getClickedBlock().getState();
        if (!(state instanceof Sign)) {
            return;
        }

        Sign sign = (Sign) state;
        String signIdentifier = plugin.getConfig().getString("Options.sign-name");

        // Check if this is an iN_Blocks shop sign
        if (!sign.getLine(0).equals(signIdentifier)) {
            return;
        }

        Player player = event.getPlayer();

        // Check permission
        if (!player.hasPermission("in.sign.use")) {
            player.sendMessage(ChatColor.RED + Lang.NO_PERM.toString());
            return;
        }

        // Check if economy is available
        Economy economy = plugin.getEconomy();
        if (economy == null) {
            player.sendMessage(ChatColor.RED + "Economy system not available!");
            return;
        }

        // Parse price
        double price;
        try {
            String priceText = ChatColor.stripColor(sign.getLine(1))
                    .replace("$", "").trim();
            price = Double.parseDouble(priceText);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid sign price!");
            return;
        }

        // Get schematic name
        String schematicName = ChatColor.stripColor(sign.getLine(2));
        if (!schematicManager.doesExist(schematicName)) {
            player.sendMessage(ChatColor.RED + Lang.EXISTS.toString());
            return;
        }

        // Process transaction
        EconomyResponse response = economy.withdrawPlayer(player, price);

        if (!response.transactionSuccess()) {
            player.sendMessage(ChatColor.RED + Lang.AFFORD.toString());
            return;
        }

        // Give schematic item to player
        BlockBuilder builder = new BlockBuilder(schematicName, plugin);
        builder.setAmount(1);
        player.getInventory().addItem(builder.build());
        player.updateInventory();

        player.sendMessage(ChatColor.AQUA + Lang.BUY.toString());
    }
}
