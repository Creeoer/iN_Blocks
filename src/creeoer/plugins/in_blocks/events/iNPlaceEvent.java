package creeoer.plugins.in_blocks.events;

import creeoer.plugins.in_blocks.main.BuildSchematic;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Custom event fired when a player places an iN_Blocks schematic.
 * This event is cancellable, allowing other plugins to prevent placement.
 *
 * <p>This event is fired after the player confirms placement but before
 * the build task is created. Cancelling this event prevents the build
 * from starting.</p>
 *
 * @author Creeoer
 * @version 2.0.0
 */
public class iNPlaceEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final String schematicName;
    private final Player player;
    private final Location location;
    private final Block block;
    private final BuildSchematic schematic;
    private boolean cancelled;

    /**
     * Creates a new iNPlaceEvent.
     *
     * @param schematicName The name of the schematic being placed
     * @param player The player placing the schematic
     * @param location The location where the schematic will be placed
     * @param block The block where the build chest will be created
     * @param schematic The BuildSchematic instance
     */
    public iNPlaceEvent(String schematicName, Player player, Location location,
                        Block block, BuildSchematic schematic) {
        this.schematicName = schematicName;
        this.player = player;
        this.location = location;
        this.block = block;
        this.schematic = schematic;
        this.cancelled = false;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Gets the handler list for this event type.
     *
     * @return The static handler list
     */
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    /**
     * Gets the block where the build chest will be created.
     *
     * @return The Block
     */
    public Block getBlock() {
        return block;
    }

    /**
     * Gets the name of the schematic being placed.
     *
     * @return The schematic name
     */
    public String getName() {
        return schematicName;
    }

    /**
     * Gets the schematic name (alias for getName()).
     *
     * @return The schematic name
     */
    public String getSchematicName() {
        return schematicName;
    }

    /**
     * Gets the BuildSchematic instance.
     *
     * @return The BuildSchematic
     */
    public BuildSchematic getSchematic() {
        return schematic;
    }

    /**
     * Gets the player placing the schematic.
     *
     * @return The Player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the player placing the schematic (alias for getPlayer()).
     *
     * @return The Player
     */
    public Player getPlacer() {
        return player;
    }

    /**
     * Gets the placement location.
     *
     * @return The Location
     */
    public Location getLocation() {
        return location;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
