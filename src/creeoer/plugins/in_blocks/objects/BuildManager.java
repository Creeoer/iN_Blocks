package creeoer.plugins.in_blocks.objects;

import com.sk89q.worldedit.world.DataException;
import creeoer.plugins.in_blocks.main.ISchematic;
import creeoer.plugins.in_blocks.main.iN_Blocks;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by black on 6/28/2017.
 */
public class BuildManager {
    //Manages all build tasks & creates or deletes themc
    private Set<BuildTask> tasks;
    private iN_Blocks main;
    private File buildFile;
    private int maxId;
    private YamlConfiguration currentBuilds;
    private List<Integer> ids;

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
        ids = new ArrayList<>();
        currentBuilds = YamlConfiguration.loadConfiguration(buildFile);
        maxId = 0;
    }

    public BuildTask createNewTask(ISchematic schematicName, Location l, String pName) {

        BuildTask task = new BuildTask(schematicName, l, pName, main);
        task.setId(getMaxId() + 1);
        tasks.add(task);
        this.saveTask(task);
        return task;
    }

    public int getMaxId(){
        List<Integer> allIds = new ArrayList<>();
        if(currentBuilds.getKeys(false) != null || !currentBuilds.getKeys(false).isEmpty()) {
            for (String id : currentBuilds.getKeys(false)) {
                allIds.add(Integer.parseInt(id));
            }
        }

        if(allIds.isEmpty())
            return 0;

        return Collections.max(allIds);
    }



    public boolean hasTask(String pName) {
        if(currentBuilds.getKeys(false) != null || !currentBuilds.getKeys(false).isEmpty()) {
            for (String id : currentBuilds.getKeys(false)) {
                if (currentBuilds.get(id + ".player").equals(pName)) {
                    return true;
                }

            }
        }
        return false;
    }
    public void initTasks() throws IOException, DataException {
        //Deserealize tempFile, reinit all tasks
        if(currentBuilds.getKeys(false) != null || !currentBuilds.getKeys(false).isEmpty()) {
        for (String id : currentBuilds.getKeys(false)) {
            long systemTime = currentBuilds.getLong(id + ".startTime");
            if(systemTime * 10000 > System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(main.getConfig().getInt("Options.exp-time"))){
                currentBuilds.set(id, null);
                currentBuilds.save(buildFile);
                main.getLogger().info("Removed task " + id + " as it expired");
            }
            /*
            BuildTask task = new BuildTask(new ISchematic(currentBuilds.getString(id + ".name"), main), (Location) currentBuilds.get(id + ".location"),
                    currentBuilds.getString(id + ".player"), main);
            task.setPlace(currentBuilds.getInt(id + ".place"));
            task.runTaskTimer(main, 0, 20 / main.getConfig().getInt("Options.blocksPerSecond"));
            task.setId(Integer.parseInt(id));
            tasks.add(task);
            */
            ids.add(Integer.parseInt(id));

         }
        }

        if(ids.isEmpty()) {
            maxId = 0;
            return;
        }


        if(Collections.max(ids) == null){
            for(int id: ids){
                maxId = id;
                break;
            }
            return;
        }

        maxId = Collections.max(ids);

    }

public BuildTask startTask(String pName) throws IOException, DataException{
        for(String id: currentBuilds.getKeys(false)){
            if(currentBuilds.get(id + ".player").equals(pName)) {
                BuildTask task = new BuildTask(new ISchematic(currentBuilds.getString(id + ".name"), main), (Location) currentBuilds.get(id + ".location"),
                        currentBuilds.getString(id + ".player"), main);
                task.setPlace(currentBuilds.getInt(id + ".place"));
                task.setId(Integer.parseInt(id));
                tasks.add(task);
                task.runTaskTimer(main, 0, 20 / main.getConfig().getInt("Options.blocksPerSecond"));
                return task;
            }
        }
        return null;
}

    public void removeTask(BuildTask task) throws IOException {
        tasks.remove(task);
        Bukkit.broadcastMessage(Integer.toString(task.getId()));
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
            currentBuilds.set(task.getId() + ".name", task.getSchematic().getName());
            currentBuilds.set(task.getId() + ".location", task.getLocation());
            currentBuilds.set(task.getId() + ".player", task.getPName());
            currentBuilds.set(task.getId() + ".place", task.getPlace());
        }

        currentBuilds.save(buildFile);

    }

    public void saveTask(BuildTask task){
        currentBuilds.set(task.getId() + ".name", task.getSchematic().getName());
        currentBuilds.set(task.getId() + ".location", task.getLocation());
        currentBuilds.set(task.getId() + ".player", task.getPName());
        currentBuilds.set(task.getId() + ".place", task.getPlace());
        currentBuilds.set(task.getId() + ".startTime", System.currentTimeMillis() /10000);


        try {
            currentBuilds.save(buildFile);
        } catch (IOException e) {
            Bukkit.getServer().getLogger().severe("Could not save build task!");
        }
    }

}
