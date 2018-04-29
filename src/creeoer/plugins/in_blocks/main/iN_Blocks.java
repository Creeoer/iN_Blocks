package creeoer.plugins.in_blocks.main;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.world.DataException;
import creeoer.plugins.in_blocks.listeners.BlockListener;
import creeoer.plugins.in_blocks.listeners.PlayerActionsListener;
import creeoer.plugins.in_blocks.listeners.SignListener;
import creeoer.plugins.in_blocks.objects.BuildManager;
import creeoer.plugins.in_blocks.objects.Lang;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class iN_Blocks extends JavaPlugin {

    private Economy econ = null;
    private SchematicManager manager;
    private FileConfiguration config;
    private RegionManager rgManager;
    public Set<String> dependencies;
    private File lang_file;
    private FileConfiguration lang;
    private BuildManager buildManager;


    public void onEnable() {
        PluginManager pm = Bukkit.getPluginManager();

        if (!getDataFolder().exists())
            createPluginFolders();

        lang_file = new File(getDataFolder() + File.separator + "lang.yml");

        if (lang_file == null || !lang_file.exists()) {
            createLangFile();
            loadLangFileDefaults();
        }

        lang = YamlConfiguration.loadConfiguration(lang_file);
        Lang.setFile(lang);

        if (!new File(getDataFolder() + File.separator + "config.yml").exists())
            saveDefaultConfig();


        buildManager = new BuildManager(this);
        initDependencies();
        config = YamlConfiguration.loadConfiguration(new File(getDataFolder() + File.separator + "config.yml"));
        manager = new SchematicManager(this);
        getCommand("in").setExecutor(new Commands(this));


        if (!setupEconomy() && config.getBoolean("economy-enabled")) {
            Bukkit.getLogger().severe("Vault was not found, therefore plugin functionality is not possible");
            pm.disablePlugin(this);
            return;
        }


        try {
            buildManager.initTasks();
        } catch (IOException e) {
            Bukkit.getLogger().severe("There are invalid build tasks!");
            //clear tasks
        } catch (DataException e) {
            e.printStackTrace();
        }

        rgManager = new RegionManager(this);
        pm.registerEvents(new BlockListener(this), this);
        pm.registerEvents(new SignListener(this), this);
        pm.registerEvents(new PlayerActionsListener(this), this);
    }

    public void onDisable() {
        buildManager.saveAllTasks();
        getLogger().info("Saving all current build tasks!");
    }

    public boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null)
            return false;

        econ = rsp.getProvider();
        return econ != null;
    }



    public void createPluginFolders(){
        getDataFolder().mkdirs();
        new File(getDataFolder() + File.separator + "schematics").mkdirs();
        try {
            new File(getDataFolder() + File.separator + "schematics.yml").createNewFile();
            saveDefaultConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void createLangFile(){
        try {
            lang_file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        lang = YamlConfiguration.loadConfiguration(lang_file);
    }

    public void loadLangFileDefaults(){
        for (Lang value : Lang.values()) {
            lang.set(value.getPath(), value.getDefault());
        }
        try {
            lang.save(lang_file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SchematicManager getSchematicManager() {
        return manager;
    }

    private void initDependencies() {
        List<String> depends = java.util.Arrays.asList("WorldGuard", "PreciousStones",
                "Districts", "Factions", "Towny",
                "GriefPrevention", "RedProtect");
        dependencies = new HashSet<>();

        for (Plugin p : Bukkit.getPluginManager().getPlugins()) {
            if (depends.contains(p.getName())) {
                dependencies.add(p.getName());
            }
        }
    }

    public WorldEditPlugin getWorldEdit() {

        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");

        if (plugin == null || !(plugin instanceof WorldEditPlugin))
            return null;


        return (WorldEditPlugin) plugin;
    }


    public Economy getEcon() {
        return econ;
    }

    public boolean is112(){
        if(Bukkit.getVersion().contains("1.12"))
            return true;

        return false;
    }


    @Override
    public FileConfiguration getConfig() {
        return config;
    }

    public RegionManager getrManager() {
        return rgManager;
    }

    public BuildManager getBuildManager() {
        return buildManager;
    }
}
