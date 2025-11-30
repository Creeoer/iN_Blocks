package creeoer.plugins.in_blocks.objects;

import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Wrapper class for a chest used in schematic building.
 * Provides methods for managing the chest's inventory and display.
 *
 * <p>The BuildChest is the central material storage for a build task.
 * Players add required materials to the chest, and the build task
 * consumes them as blocks are placed.</p>
 *
 * @author Creeoer
 * @version 2.0.0
 */
public class BuildChest {

    private final Chest chest;
    private final int chestId;

    /**
     * Creates a new BuildChest wrapper.
     *
     * @param chest The Bukkit Chest block state
     * @param chestId The ID of the associated build task
     */
    public BuildChest(Chest chest, int chestId) {
        this.chest = chest;
        this.chestId = chestId;
    }

    /**
     * Updates the chest's block state to persist changes.
     * Preserves the current inventory contents.
     */
    public void update() {
        // Save current contents before update
        ItemStack[] contents = getCurrentChestInventoryContents();

        // Update block state (this refreshes the chest)
        chest.update();

        // Restore contents
        Inventory inventory = chest.getInventory();
        for (ItemStack stack : contents) {
            if (stack != null) {
                inventory.addItem(stack);
            }
        }
    }

    /**
     * Gets a copy of the current chest inventory contents.
     *
     * @return Array of ItemStacks (may contain nulls)
     */
    public ItemStack[] getCurrentChestInventoryContents() {
        return chest.getBlockInventory().getContents().clone();
    }

    /**
     * Checks if the chest contains at least one of the required item.
     *
     * @param requiredItem The item to check for
     * @return true if the chest contains the item
     */
    public boolean containsRequirement(ItemStack requiredItem) {
        return chest.getInventory().containsAtLeast(requiredItem, 1);
    }

    /**
     * Sets the custom display name of the chest.
     *
     * @param newName The new display name
     */
    public void setName(String newName) {
        chest.setCustomName(newName);
    }

    /**
     * Gets the custom display name of the chest.
     *
     * @return The custom name, or null if not set
     */
    public String getName() {
        return chest.getCustomName();
    }

    /**
     * Gets the location of this chest.
     *
     * @return The chest's Location
     */
    public Location getChestLocation() {
        return chest.getLocation();
    }

    /**
     * Gets the chest's inventory.
     *
     * @return The Inventory
     */
    public Inventory getInventory() {
        return chest.getInventory();
    }

    /**
     * Gets the block inventory (excludes double chest partner).
     *
     * @return The block's Inventory
     */
    public Inventory getBlockInventory() {
        return chest.getBlockInventory();
    }

    /**
     * Removes one item of the specified type from the chest.
     *
     * @param targetItem The item type to remove
     * @return true if an item was removed
     */
    public boolean removeItemStack(ItemStack targetItem) {
        Inventory inventory = chest.getBlockInventory();

        for (ItemStack itemStack : inventory.getContents()) {
            if (itemStack == null) {
                continue;
            }

            if (itemStack.getType().equals(targetItem.getType())) {
                if (itemStack.getAmount() > 1) {
                    itemStack.setAmount(itemStack.getAmount() - 1);
                } else {
                    inventory.remove(itemStack);
                }
                return true;
            }
        }

        return false;
    }

    /**
     * Gets the total count of a specific material in the chest.
     *
     * @param targetItem The item type to count
     * @return The total count
     */
    public int countMaterial(ItemStack targetItem) {
        int count = 0;
        for (ItemStack itemStack : chest.getBlockInventory().getContents()) {
            if (itemStack != null && itemStack.getType().equals(targetItem.getType())) {
                count += itemStack.getAmount();
            }
        }
        return count;
    }

    /**
     * Checks if the chest inventory is empty.
     *
     * @return true if the chest is empty
     */
    public boolean isEmpty() {
        for (ItemStack itemStack : chest.getBlockInventory().getContents()) {
            if (itemStack != null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the associated build task ID.
     *
     * @return The chest ID
     */
    public int getChestId() {
        return chestId;
    }
}
