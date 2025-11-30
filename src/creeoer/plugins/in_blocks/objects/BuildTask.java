package creeoer.plugins.in_blocks.objects;

import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import creeoer.plugins.in_blocks.main.BuildSchematic;
import creeoer.plugins.in_blocks.main.iN_Blocks;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Represents an active schematic building task.
 * Runs as a scheduled task that places blocks one at a time from bottom to top.
 *
 * <p>In survival mode, the task checks for required materials in the build chest
 * before placing each block. The task can be paused when the player goes offline
 * and resumed when they return.</p>
 *
 * @author Creeoer
 * @version 2.0.0
 */
public class BuildTask extends BukkitRunnable {

    private final BuildSchematic schematic;
    private final iN_Blocks plugin;
    private final FileConfiguration config;
    private final UUID playerUUID;
    private final String playerName;

    private int currentIndex;
    private int totalBlocks;
    private int buildTaskID;

    private final BuildChest buildChest;
    private final Location placementLocation;
    private final List<Block> blocksToPlace;
    private final Map<Block, BlockState> blockStateMap;
    private final Map<Block, BlockData> originalBlockData;

    /**
     * Creates a new build task.
     *
     * @param schematic The schematic to build
     * @param chestBlock The block where the build chest will be placed
     * @param playerName The name of the player who started the build
     * @param plugin The main plugin instance
     */
    public BuildTask(BuildSchematic schematic, Block chestBlock, String playerName, iN_Blocks plugin) {
        this.schematic = schematic;
        this.plugin = plugin;
        this.playerName = playerName;
        this.config = plugin.getConfig();
        this.buildTaskID = 0;

        // Get player
        Player player = Bukkit.getServer().getPlayer(playerName);
        this.playerUUID = player != null ? player.getUniqueId() : null;

        // Set up the build chest
        if (chestBlock.getType() != Material.CHEST) {
            chestBlock.setType(Material.CHEST);
        }

        Chest chest = (Chest) chestBlock.getState();
        this.placementLocation = chest.getLocation().clone().add(1, 0, 1);
        this.buildChest = new BuildChest(chest, buildTaskID);
        this.buildChest.setName(ChatColor.GREEN + schematic.getName());
        this.buildChest.update();

        // Initialize block tracking collections
        this.blocksToPlace = new ArrayList<>();
        this.blockStateMap = new HashMap<>();
        this.originalBlockData = new HashMap<>();

        // Load blocks from schematic
        initializeBlocks();

        this.currentIndex = 0;
        this.totalBlocks = blocksToPlace.size();
    }

    /**
     * Initializes the block placement order from the schematic.
     * Blocks are sorted by Y-level for bottom-to-top placement.
     */
    private void initializeBlocks() {
        BlockState[][][] blockArray = schematic.getBlockArray();
        if (blockArray == null) {
            return;
        }

        int sizeX = schematic.getSizeX();
        int sizeY = schematic.getSizeY();
        int sizeZ = schematic.getSizeZ();

        // Map world blocks to schematic block states
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                for (int z = 0; z < sizeZ; z++) {
                    Location blockLoc = placementLocation.clone().add(x, y, z);
                    Block worldBlock = blockLoc.getBlock();

                    blocksToPlace.add(worldBlock);
                    blockStateMap.put(worldBlock, blockArray[x][y][z]);
                    originalBlockData.put(worldBlock, worldBlock.getBlockData().clone());
                }
            }
        }

        // Sort blocks by Y level (bottom to top)
        Collections.sort(blocksToPlace, (b1, b2) -> Double.compare(b1.getY(), b2.getY()));
    }

    @Override
    public void run() {
        // Check if we're done
        if (currentIndex >= totalBlocks) {
            completeTask();
            return;
        }

        // Check if player is online
        Player player = getPlayer();
        if (player == null || !player.isOnline()) {
            plugin.getBuildManager().saveTask(this);
            cancel();
            return;
        }

        // Get current block to place
        Block targetBlock = blocksToPlace.get(currentIndex);
        BlockState blockState = blockStateMap.get(targetBlock);

        if (blockState == null) {
            currentIndex++;
            return;
        }

        // Convert to Bukkit material
        Material material = blockStateToMaterial(blockState);
        if (material == null) {
            currentIndex++;
            return;
        }

        // Check if this is an ignored material
        boolean isIgnored = IgnoredMaterial.isIgnoredMaterial(material);

        // Play sound if enabled
        if (config.getBoolean("Options.sound", false)) {
            player.playSound(placementLocation, Sound.BLOCK_STONE_PLACE, 0.5f, 1.0f);
        }

        // Handle survival mode
        if (config.getBoolean("Options.survival-mode", true)) {
            Material itemMaterial = getItemMaterialForBlock(material);

            if (isIgnored) {
                // Place without requiring materials
                placeBlock(targetBlock, blockState);
                buildChest.setName(ChatColor.GREEN + schematic.getName());
                buildChest.update();
                currentIndex++;
            } else {
                ItemStack required = new ItemStack(itemMaterial, 1);

                if (buildChest.containsRequirement(required)) {
                    // Has materials, place the block
                    buildChest.removeItemStack(required);
                    placeBlock(targetBlock, blockState);
                    buildChest.setName(ChatColor.GREEN + schematic.getName());
                    buildChest.update();
                    currentIndex++;
                } else {
                    // Missing materials, update chest name to show requirement
                    String requiredName = ChatColor.GREEN + schematic.getName() +
                            ChatColor.RED + " Requires: " + itemMaterial.name();
                    if (!requiredName.equals(buildChest.getName())) {
                        buildChest.setName(requiredName);
                        buildChest.update();
                    }
                    // Don't increment - wait for materials
                }
            }
        } else {
            // Creative mode - just place the block
            placeBlock(targetBlock, blockState);
            currentIndex++;
        }
    }

    /**
     * Places a block in the world using the WorldEdit block state.
     *
     * @param targetBlock The world block to modify
     * @param blockState The WorldEdit block state to apply
     */
    private void placeBlock(Block targetBlock, BlockState blockState) {
        try {
            // Convert WorldEdit block state to Bukkit block data
            String blockString = blockState.getAsString();
            BlockData blockData = plugin.getServer().createBlockData(blockString);
            targetBlock.setBlockData(blockData, false);
        } catch (IllegalArgumentException e) {
            // Fallback: try to set just the material
            Material material = blockStateToMaterial(blockState);
            if (material != null && material.isBlock()) {
                targetBlock.setType(material, false);
            }
        }
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
     * Gets the item material that should be consumed for placing a block.
     * Handles special cases like wall signs, doors, etc.
     *
     * @param blockMaterial The block material being placed
     * @return The item material to consume from the chest
     */
    private Material getItemMaterialForBlock(Material blockMaterial) {
        // Handle wall variants
        String name = blockMaterial.name();

        // Wall signs -> regular signs
        if (name.endsWith("_WALL_SIGN")) {
            String signName = name.replace("_WALL_SIGN", "_SIGN");
            Material signMaterial = Material.matchMaterial(signName);
            return signMaterial != null ? signMaterial : blockMaterial;
        }

        // Wall torches -> regular torches
        if (name.equals("WALL_TORCH")) {
            return Material.TORCH;
        }
        if (name.equals("SOUL_WALL_TORCH")) {
            return Material.SOUL_TORCH;
        }
        if (name.equals("REDSTONE_WALL_TORCH")) {
            return Material.REDSTONE_TORCH;
        }

        // Wall banners -> regular banners
        if (name.endsWith("_WALL_BANNER")) {
            String bannerName = name.replace("_WALL_BANNER", "_BANNER");
            Material bannerMaterial = Material.matchMaterial(bannerName);
            return bannerMaterial != null ? bannerMaterial : blockMaterial;
        }

        // Wall heads -> regular heads
        if (name.endsWith("_WALL_HEAD")) {
            String headName = name.replace("_WALL_HEAD", "_HEAD");
            Material headMaterial = Material.matchMaterial(headName);
            return headMaterial != null ? headMaterial : blockMaterial;
        }

        return blockMaterial;
    }

    /**
     * Completes the build task successfully.
     */
    private void completeTask() {
        Player player = getPlayer();
        if (player != null && player.isOnline()) {
            player.sendMessage(ChatColor.GREEN + Lang.COMPLETE.toString());
        }
        plugin.getBuildManager().removeTask(this);
        cancel();
    }

    /**
     * Clears the build by restoring original blocks.
     */
    public void clearBuild() {
        for (Block block : blocksToPlace) {
            BlockData original = originalBlockData.get(block);
            if (original != null) {
                block.setBlockData(original, false);
            }
        }
    }

    /**
     * Gets all items that have been placed so far.
     *
     * @return List of ItemStacks representing placed blocks
     */
    public List<ItemStack> getCurrentBlocksInBuild() {
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < currentIndex && i < blocksToPlace.size(); i++) {
            Block block = blocksToPlace.get(i);
            Material material = block.getType();
            if (!IgnoredMaterial.isIgnoredMaterial(material) && material.isItem()) {
                items.add(new ItemStack(material, 1));
            }
        }
        return items;
    }

    /**
     * Gets the player who owns this task.
     *
     * @return The player, or null if offline
     */
    private Player getPlayer() {
        if (playerUUID != null) {
            return Bukkit.getPlayer(playerUUID);
        }
        return Bukkit.getPlayer(playerName);
    }

    /**
     * Checks if the given player name matches the task owner.
     *
     * @param name The player name to check
     * @return true if the player owns this task
     */
    public boolean isPlayerTaskOwner(String name) {
        return playerName.equals(name);
    }

    // Getters and setters

    /**
     * Sets the current placement index (for resuming).
     *
     * @param place The index to resume from
     */
    public void setPlace(int place) {
        this.currentIndex = place;
    }

    /**
     * Gets the current placement index.
     *
     * @return The current index
     */
    public int getPlace() {
        return currentIndex;
    }

    /**
     * Gets the schematic being built.
     *
     * @return The BuildSchematic
     */
    public BuildSchematic getSchematic() {
        return schematic;
    }

    /**
     * Gets the placement location.
     *
     * @return The Location where the schematic is being placed
     */
    public Location getLocation() {
        return placementLocation;
    }

    /**
     * Gets the owner's name.
     *
     * @return The player name
     */
    public String getOwnerName() {
        return playerName;
    }

    /**
     * Sets the build task ID.
     *
     * @param id The task ID
     */
    public void setBuildTaskID(int id) {
        this.buildTaskID = id;
    }

    /**
     * Gets the build task ID.
     *
     * @return The task ID
     */
    public int getBuildTaskID() {
        return buildTaskID;
    }

    /**
     * Gets the build chest wrapper.
     *
     * @return The BuildChest
     */
    public BuildChest getBuildChest() {
        return buildChest;
    }
}
