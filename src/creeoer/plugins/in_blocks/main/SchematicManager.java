package creeoer.plugins.in_blocks.main;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.DataException;
import creeoer.plugins.in_blocks.objects.Lang;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SchematicManager {


    private iN_Blocks main = null;
    File cFile = null;
    File dir = null;
    FileConfiguration config = null;
    FileConfiguration schematicsFile = null;
    List<String> permSchematics = null;

    protected SchematicManager(iN_Blocks instance) {
        main = instance;
        cFile = new File(main.getDataFolder() + File.separator + "schematics.yml");
        permSchematics = new ArrayList<>();
        dir = new File(main.getDataFolder() + File.separator + "schematics");
        schematicsFile = YamlConfiguration.loadConfiguration(cFile);
        config = YamlConfiguration.loadConfiguration(new File(main.getDataFolder() + File.separator + "config.yml"));

        String perm = config.getString("Options.permissions");
        String mPerm = perm.replace("]", "").replace("[", "");

        //Multiple schematics check
        if (mPerm.contains(",")) {
            String[] strings = mPerm.split(",");
            for (String s : strings) {
                permSchematics.add(s.trim());
            }
            return;
        }

        permSchematics.add(mPerm);

    }


    public void createSchematic(LocalPlayer p, String sName, String direction)  {
        File out = new File(main.getDataFolder() + File.separator + "schematics" + File.separator + sName + ".schematic");
        try {
            out.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        LocalSession session = WorldEdit.getInstance().getSessionManager().get(p);

        ClipboardHolder holder = null;
        try {
            holder = session.getClipboard();
        } catch (EmptyClipboardException e) {
            Bukkit.getPlayer(p.getName()).sendMessage(ChatColor.RED + Lang.EMPTY_CLIPBOARD.toString());
        }

        Clipboard board = holder.getClipboard();
        EditSession es = session.createEditSession(p);


        Vector min = board.getMinimumPoint();
        Vector max = board.getMaximumPoint();
        es.enableQueue();

        CuboidClipboard cc = new CuboidClipboard(max.subtract(min).add(new Vector(1, 1, 1)), min);
        cc.copy(es);


        schematicsFile.set("Schematics." + sName, direction.toUpperCase());

        try {
            SchematicFormat.MCEDIT.save(cc, out);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (com.sk89q.worldedit.data.DataException e) {
            e.printStackTrace();
        }


        try {
            schematicsFile.save(cFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    public void deleteSchematic(String sName) throws IOException {
        if (dir.listFiles().length == 0) return;
        for (File f : dir.listFiles()) {
            if (f.getName().replace(".schematic", "").equals(sName)) {
                f.delete();
                schematicsFile.set("Schematics." + sName, null);
                schematicsFile.save(cFile);
                break;
            }
        }
    }


    @SuppressWarnings(value = "all")
    public boolean doesExist(String sName) {

        for (File f : dir.listFiles()) {

            if (dir.listFiles().length == 0) return false;

            if (sName.equals(f.getName().replace(".schematic", ""))) return true;
        }
        return false;
    }

    public BuildSchematic getSchematic(String sName) {
        BuildSchematic sch = null;
        try {
            sch = new BuildSchematic(sName, main);
            return sch;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public boolean hasPermission(String sName) {
        if (permSchematics.contains(sName)) return true;
        return false;
    }

    public YamlConfiguration getSchematicsFile(){
        return (YamlConfiguration) schematicsFile;
    }

}
