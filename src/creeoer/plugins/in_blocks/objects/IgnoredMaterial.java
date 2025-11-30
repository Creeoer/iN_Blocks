package creeoer.plugins.in_blocks.objects;

import org.bukkit.Material;

import java.util.EnumSet;
import java.util.Set;

/**
 * Enumeration of materials that should be ignored during survival mode building.
 * These materials do not require items in the chest to be placed.
 *
 * <p>Updated for Minecraft 1.21+ material names.</p>
 *
 * @author Creeoer
 * @version 2.0.0
 */
public enum IgnoredMaterial {

    /** Air blocks - always ignored */
    AIR(Material.AIR),
    CAVE_AIR(Material.CAVE_AIR),
    VOID_AIR(Material.VOID_AIR),

    /** Grass and plants - decorative blocks */
    SHORT_GRASS(Material.SHORT_GRASS),
    TALL_GRASS(Material.TALL_GRASS),
    FERN(Material.FERN),
    LARGE_FERN(Material.LARGE_FERN),

    /** Fluids - cannot be placed from inventory */
    LAVA(Material.LAVA),
    WATER(Material.WATER),

    /** Natural blocks */
    GRASS_BLOCK(Material.GRASS_BLOCK);

    private final Material material;

    /**
     * Set of all ignored materials for O(1) lookup performance.
     */
    private static final Set<Material> IGNORED_MATERIALS = EnumSet.noneOf(Material.class);

    static {
        for (IgnoredMaterial ignored : values()) {
            IGNORED_MATERIALS.add(ignored.material);
        }
    }

    /**
     * Creates an ignored material entry.
     *
     * @param material The Bukkit material to ignore
     */
    IgnoredMaterial(Material material) {
        this.material = material;
    }

    /**
     * Gets the Bukkit material for this ignored material.
     *
     * @return The Bukkit Material
     */
    public Material getMaterial() {
        return material;
    }

    /**
     * Returns the string representation of the material.
     *
     * @return The material name as a string
     */
    @Override
    public String toString() {
        return material.toString();
    }

    /**
     * Checks if a given material should be ignored during survival building.
     * Uses O(1) lookup via EnumSet for optimal performance.
     *
     * @param material The material to check
     * @return true if the material should be ignored, false otherwise
     */
    public static boolean isIgnoredMaterial(Material material) {
        if (material == null) {
            return true;
        }
        return IGNORED_MATERIALS.contains(material) || !material.isItem();
    }
}
