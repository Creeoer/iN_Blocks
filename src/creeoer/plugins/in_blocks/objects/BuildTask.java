package creeoer.plugins.in_blocks.objects;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import creeoer.plugins.in_blocks.main.BuildSchematic;
import creeoer.plugins.in_blocks.main.iN_Blocks;
import org.bukkit.*;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.util.*;

/**
 * Created by black on 6/28/2017.
 */
public class BuildTask extends BukkitRunnable {

    private BuildSchematic schematic;
    private int place, sizeX, sizeY, sizeZ;
    private BaseBlock[][][] blockArray;
    private Player player;
    private FileConfiguration config;

    private iN_Blocks main;
    private int buildTaskID;

    private BuildChest buildChest;
    private Location placementLocation;
    private List<Block> originalBlocks;
    private HashMap<Block, Material> originalBlockMaterials;

    int size;
    private boolean isLatest;
    HashMap<Block, BaseBlock> blocks;

    public BuildTask(BuildSchematic schematic, Block chestBlock, String pName, iN_Blocks instance) {
        isLatest = instance.is112();
        this.schematic = schematic;
        sizeX = schematic.sizeX;
        sizeY = schematic.sizeY;
        sizeZ = schematic.sizeZ;

        Chest chest = null;

        if(chestBlock.getType() != Material.CHEST)
        chestBlock.getLocation().getBlock().setType(Material.CHEST);


        chest = (Chest) chestBlock.getState();
        placementLocation = chest.getLocation().clone().add(1, 0 , 1);

        blockArray = schematic.loadBlocks();

        player = Bukkit.getServer().getPlayer(pName);
        main = instance;
        buildTaskID = 0;
        config = main.getConfig();

        buildChest = new BuildChest(chest, getBuildTaskID());

        buildChest.setName(ChatColor.GREEN + schematic.getName());
        buildChest.update();


        blocks = new HashMap<>();
        originalBlocks = new ArrayList<>();
        originalBlockMaterials = new HashMap<>();

        //Map real-world block equivalents to base blocks
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                for (int z = 0; z < sizeZ; z++) {
                    blocks.put(placementLocation.clone().add(x, y, z).getBlock(), blockArray[x][y][z]);
                    originalBlocks.add(placementLocation.clone().add(x, y, z).getBlock());
                    originalBlockMaterials.put(placementLocation.clone().add(x, y, z).getBlock(), placementLocation.clone().add(x, y, z).getBlock().getType());
                }
            }
        }
        //Sort based on Y level for bottom-to-top placement
        Collections.sort(originalBlocks, (o1, o2) -> Double.compare(o1.getY(), o2.getY()));
        place = 0;
        size = blocks.size();
    }

    public void run(){
        if (place < size) {

            if (player == null || !player.isOnline()) {
                main.getBuildManager().saveTask(this);
                cancel();
            }

            //For each BaseBlock get the vector of the player and place the corresponding block

            Block block = originalBlocks.get(place);
            BaseBlock base = blocks.get(block);


                //Disabled by default to make plugin backwards compatible
                if (config.getBoolean("Options.sound"))
                    player.playSound(placementLocation, Sound.BLOCK_GLASS_STEP, 1, 0);




            if (config.getBoolean("Options.survival-mode")) {

                if (base.getType() == Material.WALL_SIGN.getId())
                    base.setType(Material.SIGN.getId());

                if (base.getType() == Material.WOODEN_DOOR.getId())
                    base.setType(Material.WOOD_DOOR.getId());


                ItemStack stack = new ItemStack(base.getType(), 1);
                boolean isIgnoredMaterial = IgnoredMaterial.isIgnoredMaterial(stack.getType());

                if (!buildChest.containsRequirement(stack) && !isIgnoredMaterial) {
                    String newName = ChatColor.GREEN + schematic.getName() + ChatColor.RED + " Requires: " + stack.getType().toString();
                    if(!buildChest.getName().equals(newName)) {
                        buildChest.setName(newName);
                        buildChest.update();
                    }
                } else if (isIgnoredMaterial){
                        buildChest.setName(ChatColor.GREEN + schematic.getName());
                        buildChest.update();
                        place++;
                        block.setTypeIdAndData(base.getType(), (byte) base.getData(), false);
                } else {
                    buildChest.setName(ChatColor.GREEN + schematic.getName());
                    buildChest.removeItemStack(stack);
                    buildChest.update();
                    place++;
                    block.setTypeIdAndData(base.getType(), (byte) base.getData(), false);
                }


            } else {
                place++;
                block.setTypeIdAndData(base.getType(), (byte) base.getData(), false);
            }
        } else {
            player.sendMessage(ChatColor.GREEN + Lang.COMPLETE.toString());
            main.getBuildManager().removeTask(this);

            this.cancel();
        }
    }


    public boolean isPlayerTaskOwner(String playerName){
        return playerName.equals(player.getName());
    }

    public void clearBuild(){
        for(Block b: originalBlocks){
            Material mat = originalBlockMaterials.get(b);
            b.getLocation().getBlock().setType(mat);
        }
    }


    public List<ItemStack> getCurrentBlocksInBuild(){
        //create a for loop ending at place and get all of the blocks in the region
        //use original blocks list and return all items on the floor.
        List<ItemStack> currentItemStacks = new ArrayList<>();
        for(Block block: originalBlocks){
            Block currentBlock = block.getLocation().getBlock();
            ItemStack stack = new ItemStack(currentBlock.getType(), 1);
            currentItemStacks.add(stack);
        }
        return currentItemStacks;
    }




    public void setPlace(int place) {
        this.place = place;
    }

    public int getPlace() {
        return place;
    }

    public BuildSchematic getSchematic() {
        return schematic;
    }

    public Location getLocation() {
        return placementLocation;
    }

    public String getOwnerName() {
        return player.getName();
    }

    public void setBuildTaskID(int buildTaskID){ this.buildTaskID = buildTaskID; }

    public int getBuildTaskID(){ return buildTaskID; }

    public BuildChest getBuildChest(){return buildChest; }

}
