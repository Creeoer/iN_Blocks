package creeoer.plugins.in_blocks.main;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldedit.world.DataException;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ISchematic {

    private String sName;
    private iN_Blocks main;
    private File sFile;
    private FileConfiguration config;
    private int sizeX;
    private int sizeY;
    private int sizeZ;
    private CuboidClipboard cc;
    private BaseBlock[][][] baseBlocks;
    private Location pLoc;


    public ISchematic(String sName, iN_Blocks instance) throws IOException, DataException {

        this.sName = sName;
        main = instance;
        sFile = new File(main.getDataFolder() + File.separator + "schematics" + File.separator + sName + ".schematic");
        SchematicFormat format = SchematicFormat.getFormat(sFile);
        cc = format.load(sFile);
        config = YamlConfiguration.loadConfiguration(new File(main.getDataFolder() + File.separator + "config.yml"));
        sizeX = cc.getWidth();
        sizeY = cc.getHeight();
        sizeZ = cc.getLength();
        baseBlocks =  new BaseBlock[sizeX] [sizeY] [sizeZ];
    }

    public void paste(final Location l, final Player p) throws IOException, DataException{
        if(config.getBoolean("Options.block-by-block")){

            final List<Block> blocks=new ArrayList<>();
            final HashMap<Block,BaseBlock> allBlocks=new HashMap<>();
            for(int x=0;x<cc.getWidth();x++){
                for(int y=0;y<cc.getHeight();y++){
                    for(int z=0;z<cc.getLength();z++){
                        Block b= l.clone().add(x,y,z).getBlock();
                        blocks.add(b);
                        allBlocks.put(b,baseBlocks[x][y][z]);
                    }
                }
            }

            final int size = blocks.size();
            final int blocksPerSecond = config.getInt("Options.blocksPerSecond");

            //Sorted to make them go from bottom layer to top
            Collections.sort(blocks,new Comparator<Block>(){
                @Override
                public int compare(Block o1,Block o2){
                    return Double.compare(o1.getY(),o2.getY());
                }
            });


            new BukkitRunnable(){
                int place=0;
                @Override
                public void run(){
                    for(int i=0;i<blocksPerSecond;i++){
                        if(place < size) {
                            Block b=blocks.get(place);
                            BaseBlock base=allBlocks.get(b);

                            if(Material.getMaterial(base.getType()) != Material.AIR)
                                p.playSound(l, Sound.BLOCK_ANVIL_PLACE, 1, 0);

                            b.setTypeId(base.getType());
                            b.setData((byte)base.getData());
                            place+=1;

                        }else{
                            p.sendMessage(ChatColor.GREEN + "Building completed!");
                            this.cancel();
                            return;
                        }

                    }
                }
            }.runTaskTimer(main,20L,1L);

        }else{
        try {
            EditSession es = new EditSession(new BukkitWorld(l.getWorld()), 99999999);

            cc.paste(es, BukkitUtil.toVector(l), true);
            retainMetaData(cc, BukkitUtil.toVector(l), l.getWorld());
            es.flushQueue();
        } catch (MaxChangedBlocksException e) {
            e.printStackTrace();
        }

        }
    }

    public void preview(Player p, Location l) throws IOException, DataException, MaxChangedBlocksException, NoSuchFieldException, IllegalAccessException {
        pLoc = l;
        String sdir = YamlConfiguration.loadConfiguration(new File(main.getDataFolder() + File.separator + "schematics.yml")).getString("Schematics." + sName);
        String pdir = PUtils.getCardinalDirection(p).toUpperCase();
        if (pdir.equals("N") && sdir.equalsIgnoreCase("south")) {
            cc.rotate2D(180);
        } else if (pdir.equals("south") && sdir.equalsIgnoreCase("north")) {
            cc.rotate2D(180);
        } else if (pdir.equals("west") && sdir.equalsIgnoreCase("east")) {
            cc.rotate2D(180);
        } else if (pdir.equals("east") && sdir.equalsIgnoreCase("west")) {
            cc.rotate2D(180);
        } else if (pdir.equals("east") && sdir.equalsIgnoreCase("north")) {
            cc.rotate2D(90);
        } else if (pdir.equals("north") && sdir.equalsIgnoreCase("east")) {
            cc.rotate2D(-90);
        } else if (pdir.equals("north") && sdir.equalsIgnoreCase("west")) {
            cc.rotate2D(90);
        } else if (pdir.equals("west") && sdir.equalsIgnoreCase("north")) {
            cc.rotate2D(-90);
        } else if (pdir.equals("south") && sdir.equalsIgnoreCase("east")) {
            cc.rotate2D(90);
        } else if (pdir.equals("east") && sdir.equalsIgnoreCase("south")) {
            cc.rotate2D(-90);
        } else if (pdir.equals("south") && sdir.equalsIgnoreCase("west")) {
            cc.rotate2D(90);
        } else if (pdir.equals("west") && sdir.equalsIgnoreCase("south")) {
            cc.rotate2D(-90);
        }
        for (int x = 0; x < sizeX; x ++){
            for (int y = 0; y < sizeY; y ++){
                for (int z = 0; z < sizeZ; z ++){
                    baseBlocks [x][y][z] = cc.getBlock(new Vector(x,y,z));
                }
            }
        }

        for (int x = 0; x < sizeX; x ++){
            for (int y = 0; y < sizeY; y++) {
                for (int z = 0; z < sizeZ; z ++){
                    Material mat = Material.getMaterial(baseBlocks[x][y][z].getType());
                    Location temp = l.clone().add(x, y ,z);
                    p.sendBlockChange(temp, mat, (byte) baseBlocks[x][y][z].getData());

                }
            }
        }
    }


    public void retainMetaData(CuboidClipboard clipboard, Vector spawnLoc, org.bukkit.World world) {
        /**
         *In an attempt to fix block meta data not being retained upon a worldedit place,
         */
        for (int x = 0; x < clipboard.getWidth(); x ++ ) {
            for (int y = 0; y < clipboard.getHeight(); y ++ ) {
                for (int z = 0; z < clipboard.getLength(); z ++){

                    BaseBlock b = clipboard.getBlock(new Vector(x, y, z));
                    if(b.getId() == 64) {
                        Vector newVec = spawnLoc.add(x, y, z);
                        new Location(world, newVec.getX(), newVec.getY(), newVec.getZ()).getBlock()
                                .setTypeIdAndData(64, (byte) b.getData(), true);
                    }
                }
            }
        }
    }
//IF A PLAYER PUT A MATERIAL THERE DURING A PREVIEW , IT WOULDNT BE SYNCED UP

    public void unloadPreview(Player p, Location l) {

        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                for (int z = 0; z < sizeZ; z++) {
                    Location temp = l.clone().add(x, y, z);
                    Block b = temp.getBlock();
                    p.sendBlockChange(temp, b.getType(), (byte) 0);
                }
            }
        }

    }




     /* IN FUTURE MOVE ALL REGION LOOKUP METHODS TO SEPARATE MANAGER FOR MORE DIRECT USE.

     */

    public CuboidClipboard getData(){
        return cc;
    }


    public String getName(){
        return sName;
    }

    public Block findBlock(Block b){
        //REGION SEARCHUP


        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++){
                for(int z=0;z<sizeZ;z++){
                    Block block = pLoc.clone().add(x, y, z).getBlock();

                    if(block.getType() == b.getType()){
                        return block;
                    }

                }
                  }
                }
        return null;
    }


}
