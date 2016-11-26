package creeoer.plugins.in_blocks.main;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.DataException;
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
	FileConfiguration sFile = null;
	List<String> permSchematics = null;

	protected SchematicManager(iN_Blocks instance) {
		main = instance;
		cFile = new File(main.getDataFolder() + File.separator + "schematics.yml");
		permSchematics = new ArrayList<>();
		dir = new File(main.getDataFolder() + File.separator + "schematics");
		sFile = YamlConfiguration.loadConfiguration(cFile);
		config = YamlConfiguration.loadConfiguration(new File(main.getDataFolder() + File.separator + "config.yml"));

		String perm = config.getString("Options.permissions");
		String mPerm = perm.replace("]", "").replace("[", "");

		//Multiple schematics check
		if(mPerm.contains(",")){
			String[] strings = mPerm.split(",");
			for(String s: strings){
				permSchematics.add(s.trim());
			}
			return;
		}

		permSchematics.add(mPerm);

	}


	public void createSchematic(LocalPlayer p,String sName,String direction) throws IOException, DataException, EmptyClipboardException{
        File out = new File(main.getDataFolder() + File.separator + "schematics" + File.separator + sName + ".schematic");
        out.createNewFile();

        LocalSession session =WorldEdit.getInstance().getSessionManager().get(p);
        ClipboardHolder holder = session.getClipboard();
        Clipboard board = holder.getClipboard();
        EditSession es = session.createEditSession(p);


        Vector min = board.getMinimumPoint();
        Vector max = board.getMaximumPoint();
        es.enableQueue();

        //Load CuboidClipboard selection from board
        CuboidClipboard cc = new CuboidClipboard(max.subtract(min).add(new Vector(1, 1, 1)), min);
        cc.copy(es);
        SchematicFormat.MCEDIT.save(cc, out);

        //Not sure why this doesnt work as expected...
/*
            ClipboardHolder holder = session.getClipboard();
            Clipboard board = holder.getClipboard();
            Transform transform = holder.getTransform();
            ClipboardFormat format = ClipboardFormat.findByAlias("mce");
            BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(out));
            Clipboard board = session.getClipboard().getClipboard();
            ClipboardWriter writer = format.getWriter(output);
            writer.write(board, session.getClipboard().getWorldData());
            Bukkit.broadcastMessage("whaddup");
            */


			sFile.set("Schematics." + sName, direction.toUpperCase());
			sFile.save(cFile);
	}

	public void deleteSchematic(String sName) throws IOException {
		if(dir.listFiles().length == 0) return;

		for (File f : dir.listFiles()) {
			if (f.getName().replace(".schematic", "").equals(sName)) {
				f.delete();
				sFile.set("Schematics." + sName, null);
				sFile.save(cFile);
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

	public ISchematic getSchematic(String sName){
		ISchematic sch = null;
		try {
			 sch = new ISchematic(sName, main);
			return sch;
		} catch (Exception e){
            e.printStackTrace();
			return null;
		}
	}


	public boolean hasPermission(String sName){
	if(permSchematics.contains(sName)) return true;
		return false;
	}

}
