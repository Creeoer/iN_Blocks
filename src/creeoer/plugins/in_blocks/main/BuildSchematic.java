package creeoer.plugins.in_blocks.main;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import creeoer.plugins.in_blocks.objects.IgnoredMaterial;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

/**
 * Represents a schematic that can be previewed and built in the world.
 * This class handles loading schematics from files, rotating them based on
 * player facing direction, and providing block data for building.
 *
 * <p>The BuildSchematic uses modern WorldEdit 7.x API with Clipboard and
 * BlockState for optimal compatibility with Minecraft 1.21+.</p>
 *
 * <p>Supported schematic formats:
 * <ul>
 *   <li>Sponge Schematic v3 (.schem)</li>
 *   <li>Sponge Schematic v2 (.schem)</li>
 *   <li>Legacy MCEdit (.schematic) - for backwards compatibility</li>
 * </ul>
 * </p>
 *
 * @author Creeoer
 * @version 2.0.0
 */
public class BuildSchematic {

    private final String schematicName;
    private final iN_Blocks plugin;
    private final File schematicFile;

    /** The dimensions of the schematic */
    private int sizeX;
    private int sizeY;
    private int sizeZ;

    /** The WorldEdit clipboard containing the schematic data */
    private Clipboard clipboard;

    /** 3D array of block states for iteration */
    private BlockState[][][] blockArray;

    /** Current rotation applied to the schematic in degrees */
    private int currentRotation = 0;

    /** The origin offset for placement */
    private BlockVector3 offset;

    /**
     * Creates a new BuildSchematic by loading from a file.
     * Supports both modern .schem and legacy .schematic formats.
     *
     * @param schematicName The name of the schematic (without extension)
     * @param plugin The main plugin instance
     * @throws IllegalArgumentException if the schematic file cannot be loaded
     */
    public BuildSchematic(String schematicName, iN_Blocks plugin) {
        this.schematicName = schematicName;
        this.plugin = plugin;

        // Try modern format first, then legacy
        File schemFile = new File(plugin.getDataFolder(), "schematics/" + schematicName + ".schem");
        if (!schemFile.exists()) {
            schemFile = new File(plugin.getDataFolder(), "schematics/" + schematicName + ".schematic");
        }
        this.schematicFile = schemFile;

        loadSchematic();
    }

    /**
     * Loads the schematic from file using auto-format detection.
     */
    private void loadSchematic() {
        if (!schematicFile.exists()) {
            plugin.getLogger().severe("Schematic file not found: " + schematicFile.getPath());
            return;
        }

        ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
        if (format == null) {
            plugin.getLogger().severe("Unknown schematic format: " + schematicFile.getName());
            return;
        }

        try (ClipboardReader reader = format.getReader(new FileInputStream(schematicFile))) {
            clipboard = reader.read();
            offset = clipboard.getOrigin().subtract(clipboard.getMinimumPoint());
            loadBlocksFromClipboard();
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load schematic: " + schematicName, e);
        }
    }

    /**
     * Loads block data from the clipboard into a 3D array for iteration.
     * This method updates the size fields and block array.
     */
    private void loadBlocksFromClipboard() {
        if (clipboard == null) {
            return;
        }

        BlockVector3 dimensions = clipboard.getDimensions();
        sizeX = dimensions.x();
        sizeY = dimensions.y();
        sizeZ = dimensions.z();

        blockArray = new BlockState[sizeX][sizeY][sizeZ];
        BlockVector3 min = clipboard.getMinimumPoint();

        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                for (int z = 0; z < sizeZ; z++) {
                    BlockVector3 pos = min.add(x, y, z);
                    blockArray[x][y][z] = clipboard.getBlock(pos);
                }
            }
        }
    }

    /**
     * Sends a preview of the schematic to the player using fake block changes.
     * These changes are client-side only and don't affect the actual world.
     *
     * @param player The player to show the preview to
     * @param placementLocation The location where the schematic would be placed
     */
    public void preview(Player player, Location placementLocation) {
        if (blockArray == null) {
            return;
        }

        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                for (int z = 0; z < sizeZ; z++) {
                    Location blockLoc = placementLocation.clone().add(x, y, z);
                    BlockData blockData = convertToBlockData(blockArray[x][y][z]);
                    if (blockData != null) {
                        player.sendBlockChange(blockLoc, blockData);
                    }
                }
            }
        }
    }

    /**
     * Removes the preview by sending the actual world blocks back to the player.
     *
     * @param player The player to remove the preview from
     * @param placementLocation The location where the preview was shown
     */
    public void unloadPreview(Player player, Location placementLocation) {
        if (blockArray == null) {
            return;
        }

        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                for (int z = 0; z < sizeZ; z++) {
                    Location blockLoc = placementLocation.clone().add(x, y, z);
                    Block actualBlock = blockLoc.getBlock();
                    player.sendBlockChange(blockLoc, actualBlock.getBlockData());
                }
            }
        }
    }

    /**
     * Converts a WorldEdit BlockState to a Bukkit BlockData.
     *
     * @param state The WorldEdit BlockState
     * @return The equivalent Bukkit BlockData, or null if conversion fails
     */
    private BlockData convertToBlockData(BlockState state) {
        if (state == null) {
            return null;
        }

        try {
            // Get the block state string and create BlockData from it
            String blockString = state.getAsString();
            return plugin.getServer().createBlockData(blockString);
        } catch (IllegalArgumentException e) {
            // Fallback: try to get just the material
            BlockType blockType = state.getBlockType();
            if (blockType != null) {
                Material material = Material.matchMaterial(blockType.id().replace("minecraft:", ""));
                if (material != null) {
                    return material.createBlockData();
                }
            }
            return null;
        }
    }

    /**
     * Gets the name of this schematic.
     *
     * @return The schematic name
     */
    public String getName() {
        return schematicName;
    }

    /**
     * Gets the X dimension (width) of the schematic.
     *
     * @return The width in blocks
     */
    public int getSizeX() {
        return sizeX;
    }

    /**
     * Gets the Y dimension (height) of the schematic.
     *
     * @return The height in blocks
     */
    public int getSizeY() {
        return sizeY;
    }

    /**
     * Gets the Z dimension (length) of the schematic.
     *
     * @return The length in blocks
     */
    public int getSizeZ() {
        return sizeZ;
    }

    /**
     * Gets a list of all materials required to build this schematic.
     * Materials are sorted alphabetically for consistent display.
     *
     * @return A sorted list of material names required for building
     */
    public List<String> getBlockRequirements() {
        List<String> materialList = new ArrayList<>();

        if (blockArray == null) {
            return materialList;
        }

        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                for (int z = 0; z < sizeZ; z++) {
                    Material material = blockStateToMaterial(blockArray[x][y][z]);
                    if (material != null && !IgnoredMaterial.isIgnoredMaterial(material)) {
                        materialList.add(material.name().toUpperCase());
                    }
                }
            }
        }

        Collections.sort(materialList, String.CASE_INSENSITIVE_ORDER);
        return materialList;
    }

    /**
     * Converts a WorldEdit BlockState to a Bukkit Material.
     *
     * @param state The WorldEdit BlockState
     * @return The equivalent Bukkit Material, or null if not found
     */
    private Material blockStateToMaterial(BlockState state) {
        if (state == null) {
            return null;
        }

        BlockType blockType = state.getBlockType();
        if (blockType == null) {
            return null;
        }

        String materialName = blockType.id().replace("minecraft:", "").toUpperCase();
        return Material.matchMaterial(materialName);
    }

    /**
     * Gets the 3D array of block states for this schematic.
     *
     * @return The block array, or null if loading failed
     */
    public BlockState[][][] getBlockArray() {
        return blockArray;
    }

    /**
     * Gets the WorldEdit Clipboard for this schematic.
     *
     * @return The clipboard, or null if loading failed
     */
    public Clipboard getClipboard() {
        return clipboard;
    }

    /**
     * Rotates the schematic to match the player's facing direction and sets up
     * the placement origin.
     *
     * @param placementLocation The location where the schematic will be placed
     * @param facing The direction the player is facing
     */
    public void rotateAndSetBoardOrigin(Location placementLocation, BlockFace facing) {
        if (clipboard == null) {
            return;
        }

        // Load the original direction from config
        File configFile = new File(plugin.getDataFolder(), "schematics.yml");
        String savedDirection = YamlConfiguration.loadConfiguration(configFile)
                .getString("Schematics." + schematicName, "NORTH");

        BlockFace originalFacing = PUtils.parseBlockFace(savedDirection);
        int rotateValue = PUtils.getRotateValue(originalFacing, facing);

        // Apply rotation if needed
        if (rotateValue != currentRotation) {
            applyRotation(rotateValue);
            currentRotation = rotateValue;
        }
    }

    /**
     * Applies a rotation transformation to the block array.
     *
     * @param degrees The rotation in degrees (0, 90, 180, or 270)
     */
    private void applyRotation(int degrees) {
        if (degrees == 0 || blockArray == null) {
            return;
        }

        int rotations = degrees / 90;
        for (int i = 0; i < rotations; i++) {
            blockArray = rotate90();
        }
    }

    /**
     * Rotates the block array 90 degrees clockwise.
     *
     * @return The rotated block array
     */
    private BlockState[][][] rotate90() {
        // Swap X and Z dimensions for 90-degree rotation
        BlockState[][][] rotated = new BlockState[sizeZ][sizeY][sizeX];

        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                for (int z = 0; z < sizeZ; z++) {
                    // Rotate 90 degrees: (x, y, z) -> (sizeZ - 1 - z, y, x)
                    rotated[sizeZ - 1 - z][y][x] = blockArray[x][y][z];
                }
            }
        }

        // Update dimensions
        int temp = sizeX;
        sizeX = sizeZ;
        sizeZ = temp;

        return rotated;
    }

    /**
     * Checks if the schematic was loaded successfully.
     *
     * @return true if the schematic is ready to use
     */
    public boolean isValid() {
        return clipboard != null && blockArray != null;
    }
}
