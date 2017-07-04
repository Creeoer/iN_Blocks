package creeoer.plugins.in_blocks.objects;

import com.sk89q.worldedit.world.DataException;
import creeoer.plugins.in_blocks.main.ISchematic;
import creeoer.plugins.in_blocks.main.iN_Blocks;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by black on 6/28/2017.
 */
public class BuildManager {
    //Manages all build tasks & creates or deletes themc
    private Set<BuildTask> tasks;
    private iN_Blocks main;
    private File buildFile;
    private YamlConfiguration currentBuilds;

    public BuildManager(iN_Blocks main) {
        this.main = main;
        buildFile = new File(main.getDataFolder() + File.separator + "currentBuilds.yml");

        if (!buildFile.exists())
            try {
                buildFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }


        tasks = new HashSet<>();
        currentBuilds = YamlConfiguration.loadConfiguration(buildFile);
    }

    public BuildTask createNewTask(ISchematic schematicName, Location l, String pName) {

        BuildTask task = new BuildTask(schematicName, l, pName, main);
        tasks.add(task);

        return task;
    }

    public void initTasks() throws IOException, DataException {
        //Deserealize tempFile, reinit all tasks
        for (String id : currentBuilds.getKeys(false)) {
            BuildTask task = new BuildTask(new ISchematic(currentBuilds.getString(id + ".name"), main), (Location) currentBuilds.get(id + ".location"),
                    currentBuilds.getString(id + ".player"), main);
            task.setPlace(currentBuilds.getInt(id + ".place"));
            task.runTaskTimer(main, 0, 20 / main.getConfig().getInt("Options.blocksPerSecond"));
            task.setId(Integer.parseInt(id));
            tasks.add(task);
        }
    }

    public void removeTask(BuildTask task) throws IOException {
        tasks.remove(task);
        if(currentBuilds.getKeys(false) != null || !currentBuilds.getKeys(false).isEmpty()) {
            if(currentBuilds.getKeys(false).contains(Integer.toString(task.getId()))){
                currentBuilds.set(Integer.toString(task.getId()), null);
                currentBuilds.save(buildFile);
            }
        }
    }

    public Set<BuildTask> getAllTasks() {
        return tasks;
    }


    public void saveAllTasks() throws IOException {
        for (BuildTask task : tasks) {
            //Serealize schematic name, location, player name
            currentBuilds.set(task.getTaskId() + ".name", task.getSchematic().getName());
            currentBuilds.set(task.getTaskId() + ".location", task.getLocation());
            currentBuilds.set(task.getTaskId() + ".player", task.getPName());
            currentBuilds.set(task.getTaskId() + ".place", task.getPlace());
        }

        currentBuilds.save(buildFile);

    }

    public void saveTask(BuildTask task){
        currentBuilds.set(task.getTaskId() + ".name", task.getSchematic().getName());
        currentBuilds.set(task.getTaskId() + ".location", task.getLocation());
        currentBuilds.set(task.getTaskId() + ".player", task.getPName());
        currentBuilds.set(task.getTaskId() + ".place", task.getPlace());


        try {
            currentBuilds.save(buildFile);
        } catch (IOException e) {
            Bukkit.getServer().getLogger().severe("Could not save build task!");
        }
    }

}
