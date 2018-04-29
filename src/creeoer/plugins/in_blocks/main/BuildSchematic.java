package creeoer.plugins.in_blocks.main;

import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.worldedit.*;
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
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldedit.world.registry.LegacyWorldData;
import creeoer.plugins.in_blocks.objects.IgnoredMaterial;
import org.bukkit.*;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.EnderChest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.GZIPInputStream;

/*
Class purpose: The BuildSchematic class serves as a data structure with some limited functionality. It is mainly used to create a Build Task.
 */

public class BuildSchematic {

    private String sName;
    private iN_Blocks main;

    private File schematicFile;
    public int sizeX, sizeY, sizeZ;
    private Clipboard board;

    private CuboidClipboard cc;

    private BaseBlock[][][] blockArray;



    //TODO Update with new worldedit api - DONE!
    public BuildSchematic(String schematicsName, iN_Blocks instance)  {
        this.sName = schematicsName;
        main = instance;
        schematicFile = new File(main.getDataFolder() + File.separator + "schematics" + File.separator + schematicsName + ".schematic");
        NBTInputStream nbtStream = getNBTStream();

        ClipboardReader reader = new SchematicReader(nbtStream);

        board = null;
        cc = null;
        try {
            board = reader.read(LegacyWorldData.getInstance());
            cc = SchematicFormat.MCEDIT.load(schematicFile);
        } catch (IOException |DataException dataException) {
            dataException.printStackTrace();
        }

        //Still using cuboid clipboard as that's the only thing that works apparently

        blockArray = loadBlocks();
    }



    public void preview(Player p, Location placementLocation) throws IOException, DataException, MaxChangedBlocksException, NoSuchFieldException, IllegalAccessException {
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                for (int z = 0; z < sizeZ; z++) {
                    p.sendBlockChange(placementLocation.clone().add(x, y, z), blockArray[x][y][z].getType(), (byte) blockArray[x][y][z].getData());
                }
            }
        }


    }


    public void unloadPreview(Player p, Location placementLocation) {
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                for (int z = 0; z < sizeZ; z++) {
                    Location temp = placementLocation.clone().add(x, y, z);
                    Block b = temp.getBlock();
                    p.sendBlockChange(temp, b.getType(), (byte) 0);
                }
            }
        }

    }

    public String getName() {
        return sName;
    }


    public List<String> getBlockRequirements() {
        List<String> matList = new ArrayList<>();
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                for (int z = 0; z < sizeZ; z++) {
                    //Ok I have a list of materials
                    Material material = Material.getMaterial(blockArray[x][y][z].getType());
                    if (!IgnoredMaterial.isIgnoredMaterial(material))
                        matList.add(material.name().toUpperCase());
                }
            }
        }

        sortMaterials(matList);

        return matList;
    }


    public CuboidClipboard getRegion() {
        return cc;
    }

    
    private void sortMaterials(List<String> materialList) {
        int index = 0;
        boolean flag = true; //Determines when the sort is finished
        String holderName;
        while (flag) {
            flag = false;
            for (index = 0; index < materialList.size() - 1; index++) {
                //If the first material name is greater than the second go on
                if (materialList.get(index).compareToIgnoreCase(materialList.get(index + 1)) > 0) {
                    holderName = materialList.get(index);
                    materialList.set(index, materialList.get(index + 1).toUpperCase());       //Swap them, this will make them go into ascending order
                    materialList.set(index + 1, holderName.toUpperCase());
                    flag = true;
                }
            }
        }
    }
    
    public BaseBlock[][][] loadBlocks() {
        //If north blockface is null check which blockface isnt null and based on that rotate clipboard so that it isnt null
        //Reload sizes
        sizeX = cc.getWidth();
        sizeY = cc.getHeight();
        sizeZ = cc.getLength();
        BaseBlock[][][] blocks = new BaseBlock[sizeX][sizeY][sizeZ];
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                for (int z = 0; z < sizeZ; z++) {
                    blocks[x][y][z] = cc.getBlock(new Vector(x, y, z));
                }
            }
        }

        return blocks;
    }


    private NBTInputStream getNBTStream(){
        NBTInputStream nbtStream = null;
        try {
            nbtStream = new NBTInputStream(new GZIPInputStream
                    (new BufferedInputStream(new FileInputStream(schematicFile))));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return nbtStream;
    }


    public void rotateAndSetBoardOrigin(Location placementLocation, BlockFace facing){
        Block placementBlock = placementLocation.getBlock();
        String direction = YamlConfiguration.loadConfiguration(new File(main.getDataFolder() + File.separator + "schematics.yml")).getString("Schematics." + sName);
        int rotateValue = PUtils.getRotateValue(PUtils.parseBlockFace(direction), facing);
        cc.rotate2D(rotateValue);
        BlockVector offset = cc.getOffset().toBlockVector();
        cc.setOrigin(new BlockVector(placementBlock.getX() + offset.getBlockX(), placementBlock.getY() + offset.getBlockY(), placementBlock.getZ()
         + offset.getBlockZ()));


        blockArray = loadBlocks();


    }

}