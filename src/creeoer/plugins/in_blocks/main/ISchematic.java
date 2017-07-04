package creeoer.plugins.in_blocks.main;

import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.worldedit.CuboidClipboard;
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
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldedit.world.registry.LegacyWorldData;
import org.bukkit.*;
import org.bukkit.block.Block;
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

//Where all the magic happens
public class ISchematic {

    private String sName;
    private iN_Blocks main;
    private FileConfiguration config;
    public int sizeX, sizeY, sizeZ;
    private Clipboard board;
    private Extent source;
    private CuboidClipboard cc;
    private BukkitTask task;
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
        cc = SchematicFormat.MCEDIT.load(sFile);
        source = board;

        //Still using cuboid clipboard as that's the only thing that works apparently
        sizeX = cc.getWidth();
        sizeY = cc.getHeight();
        sizeZ = cc.getLength();

        blockArray = loadBlocks();

    }


    public void paste(final Location l, final Player p) throws IOException, DataException, MaxChangedBlocksException {
        if (config.getBoolean("Options.block-by-block")) {
            main.getBuildManager().createNewTask(this, l, p.getName()).runTaskTimer(main, 0, 20 / config.getInt("Options.blocksPerSecond"));
        } else {
            EditSession es = new EditSession(new BukkitWorld(l.getWorld()), 99999999);
            ForwardExtentCopy copy = new ForwardExtentCopy(source, board.getRegion(), board.getOrigin(), es, BukkitUtil.toVector(l));
            copy.setSourceMask(new ExistingBlockMask(source));
            Operations.completeLegacy(copy);
            es.flushQueue();
        }
    }

    public void preview(Player p, Location l) throws IOException, DataException, MaxChangedBlocksException, NoSuchFieldException, IllegalAccessException {
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                for (int z = 0; z < sizeZ; z++) {
                    p.sendBlockChange(l.clone().add(x, y, z), blockArray[x][y][z].getType(), (byte) blockArray[x][y][z].getData());
                }
            }
        }
    }

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
                    if (material != Material.AIR)
                        matList.add(material.name().toUpperCase());
                }
            }
        }

        //Sort materials in order to get them into an ascending order
        int j = 0;
        boolean flag = true; //Determines when the sort is finished
        String holderName;
        while (flag) {
            flag = false;
            for (j = 0; j < matList.size() - 1; j++) {
                //If the first material name is greater than the second go on
                if (matList.get(j).compareToIgnoreCase(matList.get(j + 1)) > 0) {
                    holderName = matList.get(j);
                    matList.set(j, matList.get(j + 1).toUpperCase());       //Swap them, this will make them go into ascending order
                    matList.set(j + 1, holderName.toUpperCase());
                    flag = true;
                }
            }
        }
        return matList;
    }

    public CuboidClipboard getRegion() {
        return cc;
    }

    public BaseBlock[][][] loadBlocks() {
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


}