package creeoer.plugins.in_blocks.builders;

import creeoer.plugins.in_blocks.main.BuildSchematic;
import creeoer.plugins.in_blocks.main.iN_Blocks;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder class for creating schematic item stacks.
 * Creates chest items that represent placeable schematics.
 *
 * <p>The resulting item contains:
 * <ul>
 *   <li>A display name or lore identifying the schematic</li>
 *   <li>Material requirements in the lore (if survival mode)</li>
 * </ul>
 * </p>
 *
 * @author Creeoer
 * @version 2.0.0
 */
public class BlockBuilder {

    private final ItemStack itemStack;
    private final ItemMeta itemMeta;
    private final BuildSchematic schematic;
    private final iN_Blocks plugin;

    /**
     * Creates a new BlockBuilder for the specified schematic.
     *
     * @param schematicName The name of the schematic
     * @param plugin The main plugin instance
     */
    public BlockBuilder(String schematicName, iN_Blocks plugin) {
        this.plugin = plugin;
        this.itemStack = new ItemStack(Material.CHEST);
        this.itemMeta = itemStack.getItemMeta();
        this.schematic = new BuildSchematic(schematicName, plugin);

        List<String> lore = new ArrayList<>();

        // Set display name or lore based on config
        boolean useLore = plugin.getConfig().getBoolean("Options.use-lore", false);
        String schematicLabel = ChatColor.YELLOW + schematicName + " schematic";

        if (useLore) {
            lore.add(schematicLabel);
        } else {
            itemMeta.setDisplayName(schematicLabel);
        }

        // Add material requirements if in survival mode
        if (plugin.getConfig().getBoolean("Options.survival-mode", true)) {
            addRequirementsToLore(lore);
        }

        itemMeta.setLore(lore);
    }

    /**
     * Adds material requirements to the lore list.
     *
     * @param lore The lore list to modify
     */
    private void addRequirementsToLore(List<String> lore) {
        List<String> requirements = schematic.getBlockRequirements();

        if (requirements.isEmpty()) {
            return;
        }

        // Count occurrences of each material
        Map<String, Integer> materialCounts = new HashMap<>();
        for (String material : requirements) {
            materialCounts.merge(material.toUpperCase(), 1, Integer::sum);
        }

        // Sort materials alphabetically and add to lore
        materialCounts.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    String line = ChatColor.GOLD + String.valueOf(entry.getValue()) + " " + entry.getKey();
                    lore.add(line);
                });
    }

    /**
     * Builds and returns the schematic item.
     *
     * @return The configured ItemStack
     */
    public ItemStack build() {
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    /**
     * Sets the stack amount.
     *
     * @param amount The number of items in the stack
     * @return This builder for method chaining
     */
    public BlockBuilder setAmount(int amount) {
        itemStack.setAmount(Math.min(64, Math.max(1, amount)));
        return this;
    }

    /**
     * Sets a custom display name.
     *
     * @param displayName The custom name
     * @return This builder for method chaining
     */
    public BlockBuilder setDisplayName(String displayName) {
        itemMeta.setDisplayName(displayName);
        return this;
    }

    /**
     * Adds additional lore lines.
     *
     * @param lines The lines to add
     * @return This builder for method chaining
     */
    public BlockBuilder addLore(String... lines) {
        List<String> lore = itemMeta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }
        for (String line : lines) {
            lore.add(line);
        }
        itemMeta.setLore(lore);
        return this;
    }

    /**
     * Gets the schematic associated with this builder.
     *
     * @return The BuildSchematic
     */
    public BuildSchematic getSchematic() {
        return schematic;
    }
}
