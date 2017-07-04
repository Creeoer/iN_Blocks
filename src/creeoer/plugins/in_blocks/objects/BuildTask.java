package creeoer.plugins.in_blocks.objects;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import creeoer.plugins.in_blocks.main.ISchematic;
import creeoer.plugins.in_blocks.main.iN_Blocks;
import org.bukkit.*;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.util.*;

/**
 * Created by black on 6/28/2017.
 */
public class BuildTask extends BukkitRunnable {

    private ISchematic schematic;
    private int place, sizeX, sizeY, sizeZ;
    private BaseBlock[][][] blockArray;
    private Player p;
    private FileConfiguration config;
    private Location l;
    private iN_Blocks main;
    private int id;
    private Chest chest;
    private List<Block> worldBlocks;
    private boolean wasRun, buildFulfilled, cancel;

    public BuildTask(ISchematic schematic, Location l, String pName, iN_Blocks instance) {
        this.schematic = schematic;
        place = 0;
        sizeX = schematic.sizeX;
        sizeY = schematic.sizeY;
        sizeZ = schematic.sizeZ;

        BaseBlock[][][] blocks = new BaseBlock[sizeX][sizeY][sizeZ];
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                for (int z = 0; z < sizeZ; z++) {
                    blocks[x][y][z] = schematic.getRegion().getBlock(new Vector(x, y, z));
                }
            }
        }
        blockArray = blocks;
        p = Bukkit.getServer().getPlayer(pName);
        this.l = l;
        main = instance;
        id = 0;
        config = main.getConfig();

        Block b = l.getBlock();
        b.setType(Material.CHEST);
        chest = (Chest) b;
        chest.setCustomName(ChatColor.GREEN + schematic.getName());
        wasRun = false;
        cancel = false;
        buildFulfilled = true;
    }

    public void run() {

        HashMap<Block, BaseBlock> blocks = new HashMap<>();
        List<Block> originalBlocks = new ArrayList<>();

        //Map real-world block equivalents to base blocks
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                for (int z = 0; z < sizeZ; z++) {
                    blocks.put(l.clone().add(x, y, z).getBlock(), blockArray[x][y][z]);
                    originalBlocks.add(l.clone().add(x, y, z).getBlock());
                }
            }
        }

        worldBlocks = originalBlocks;
        //Sort based on Y level for bottom-to-top placement
        Collections.sort(originalBlocks, (o1, o2) -> Double.compare(o1.getY(), o2.getY()));


        final int size = blocks.size();
        if (place < size) {
            //For each BaseBlock get the vector of the player and place the corresponding block
            Block block = originalBlocks.get(place);
            BaseBlock base = blocks.get(block);
            if (Material.getMaterial(base.getType()) != Material.AIR) {

                //Disabled by default to make plugin backwards compatible
                if (config.getBoolean("Options.sound"))
                    p.playSound(l, Sound.BLOCK_GLASS_STEP, 1, 0);

                if (config.getBoolean("Options.survival-mode")) {

                    if (base.getType() == Material.WALL_SIGN.getId())
                        base.setType(Material.SIGN.getId());

                    ItemStack stack = new ItemStack(base.getType(), 1);


                    if (p == null || !p.isOnline()) {
                        main.getBuildManager().saveTask(this);
                        cancel();
                    }

                    if (!chest.getInventory().containsAtLeast(stack, 1)) {

                        buildFulfilled = false;
                        if (!wasRun) {
                            p.sendMessage(ChatColor.RED + Lang.MATERIALS.toString());
                            wasRun = true;
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    if (!buildFulfilled) {
                                        clearBuild();
                                        cancel = true;
                                        p.sendMessage(ChatColor.RED + "Time has run out, cancelling build..");
                                    }
                                }
                            }.runTaskLater(main, 3600L);
                        }


                    } else {
                        buildFulfilled = true;
                        chest.getInventory().removeItem(stack);
                        place++;
                    }

                    if(cancel) {
                        try {
                            main.getBuildManager().removeTask(this);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        cancel();
                    }

                    block.setTypeIdAndData(base.getType(), (byte) base.getData(), false);
                }
            }
        } else {
            p.sendMessage(ChatColor.GREEN + Lang.COMPLETE.toString());
            try {
                main.getBuildManager().removeTask(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.cancel();
        }
    }


    public void clearBuild(){
        for(Block b: worldBlocks){
            b.setType(Material.AIR);
        }
    }


    public void setPlace(int place) {
        this.place = place;
    }

    public int getPlace() {
        return place;
    }

    public ISchematic getSchematic() {
        return schematic;
    }

    public Location getLocation() {
        return l;
    }

    public String getPName() {
        return p.getName();
    }

    public void setId(int id){ this.id = id; }

    public int getId(){ return id; }

    public Chest getChest(){return chest;}

}
