package creeoer.plugins.in_blocks.main;

import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.clipboard.io.SchematicReader;
import com.sk89q.worldedit.function.mask.ExistingBlockMask;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldedit.world.registry.LegacyWorldData;
import creeoer.plugins.in_blocks.objects.Lang;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.zip.GZIPInputStream;

//Where all the magic happens
public class ISchematic {

    private String sName;
    private iN_Blocks main;
    private FileConfiguration config;
    private int minX, minY, minZ, maxX, maxY, maxZ;
    private Clipboard board;
    private Extent source;
    private Vector min, max;

    //To be used for block-by-block placement
    private BaseBlock[][][] blockArray;


        //TODO Update with new worldedit api - DONE!
        public ISchematic(String sName, iN_Blocks instance) throws IOException, DataException {
            this.sName = sName;
            main = instance;
            config = YamlConfiguration.loadConfiguration(new File(main.getDataFolder() + File.separator + "config.yml"));
            File sFile = new File(main.getDataFolder() + File.separator + "schematics" + File.separator + sName + ".schematic");
            NBTInputStream nbtStream = new NBTInputStream(new GZIPInputStream
                    (new BufferedInputStream(new FileInputStream(sFile))));
            ClipboardReader reader = new SchematicReader(nbtStream);
            board = reader.read(LegacyWorldData.getInstance());
            
            min = board.getMinimumPoint();
            max = board.getMaximumPoint();
                    
            source = board;

            minX = min.getBlockX();
            minY = min.getBlockY();
            minZ = min.getBlockZ();
            maxX = max.getBlockX();
            maxY = max.getBlockY();
            maxZ = max.getBlockZ();
            
            blockArray = initBlocks();
            
    }


    public void paste(final Location l, final Player p) throws IOException, DataException, MaxChangedBlocksException{
        if(config.getBoolean("Options.block-by-block")){

            HashMap<Block,BaseBlock> blocks = new HashMap<>();
            List<Block> originalBlocks = new ArrayList<>();

            //Map real-world block equivalents to base blocks
            for(int x= minX;x<maxX;x++){
                for(int y=minY;y<maxY;y++){
                    for(int z=minZ;z<maxZ;z++){
                        blocks.put(l.clone().add(x, y, z).getBlock(), blockArray[x][y][z]);
                        originalBlocks.add(l.clone().add(x, y, z).getBlock());
                    }
                }
            }

            //Sort based on Y level for bottom-to-top placement
            Collections.sort(originalBlocks, (o1, o2) -> Double.compare(o1.getY(), o2.getY()));

            final int size = blocks.size();
            final int blocksPerSecond = config.getInt("Options.blocksPerSecond");
            new BukkitRunnable(){
                int place=0;
                @Override
                public void run(){
                    for(int i=0;i<blocksPerSecond;i++){
                        if(place < size) {
                            //For each BaseBlock get the vector of the player and place the corresponding block
                            Block block = originalBlocks.get(place);
                            BaseBlock base = blocks.get(block);
                            if(Material.getMaterial(base.getType()) != Material.AIR){

                                //Disabled by default to make plugin backwards compatible
                                if(config.getBoolean("Options.sound"))
                                p.playSound(l, Sound.BLOCK_GLASS_STEP, 1, 0);

                                block.setTypeIdAndData(block.getTypeId(), (byte) block.getData(), true);
                            }
                            place++;
                        }else{
                            p.sendMessage(ChatColor.GREEN + Lang.COMPLETE.toString());
                            this.cancel();
                            return;
                        }

                    }
                }
            }.runTaskTimer(main,20L,1L);

        }else{

            EditSession es = new EditSession(new BukkitWorld(l.getWorld()), 99999999);
            ForwardExtentCopy copy = new ForwardExtentCopy(source, board.getRegion(), board.getOrigin(), es, BukkitUtil.toVector(l));
            copy.setSourceMask(new ExistingBlockMask(source));
            Operations.completeLegacy(copy);
            es.flushQueue();


        }
    }

    public void preview(Player p, Location l) throws IOException, DataException, MaxChangedBlocksException, NoSuchFieldException, IllegalAccessException {
        for(int x = minX; x < maxX; x++ ) {
            for(int y= minY; y < maxY;y ++) {
                for(int z = minZ; z < maxZ; z ++) {
                    p.sendBlockChange(l.clone().add(x, y, z), blockArray[x][y][z].getType(), (byte) blockArray[x][y][z].getData());
                }
            }
        }
    }

    public void unloadPreview(Player p, Location l) {
        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                for (int z = minZ; z < maxZ; z++) {
                    Location temp = l.clone().add(x, y, z);
                    Block b = temp.getBlock();
                    p.sendBlockChange(temp, b.getType(), (byte) 0);
                }
            }
        }

    }

    public String getName(){
        return sName;
    }

//Return sorted list

    public List<String> getBlockRequirements() {
        
        List<String> matList = new ArrayList<>();
        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                for (int z = minZ; z < maxZ; z++) {
                    //Ok I have a list of materials
                    Material material = Material.getMaterial(blockArray[x][y][z].getType());
                    if(material != Material.AIR)
                    matList.add(material.name().toUpperCase());
                }
            }
        }
            //Sort materials in order to get them into an ascending order
            int j = 0;
            boolean flag=true; //Determines when the sort is finished
            String holderName;
            while(flag){
                flag=false;
                for(j = 0; j < matList.size() - 1; j ++) {
                    //If the first material name is greater than the second go on
                    if(matList.get(j).compareToIgnoreCase(matList.get(j+1)) > 0) {
                        holderName = matList.get(j);
                        matList.set(j, matList.get(j + 1).toUpperCase());       //Swap them, this will make them go into ascending order
                        matList.set(j + 1, holderName.toUpperCase());
                        flag = true;
                    }
                }
            }
        return matList;
    }



    public BaseBlock[][][] initBlocks(){
        BaseBlock[][][] blocks = new BaseBlock[maxX][maxY][maxZ];
        for(int x = minX; x < maxX; x++ ) {
            for(int y= minY; y < maxY; y ++) {
                for(int z = minZ; z < maxZ; z ++) {
                    blocks[x][y][z] = board.getBlock(new Vector(x, y, z));
                }
            }
        }

        return blocks;
    }

    public Clipboard getBoard(){
        return board;
    }



}
