package creeoer.plugins.in_blocks.main;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import creeoer.plugins.in_blocks.listeners.SListener;
import creeoer.plugins.in_blocks.listeners.SignListener;
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

	@SuppressWarnings(value = "all")
	public void onEnable(){


		PluginManager pm = Bukkit.getPluginManager();

	    if (!setupEconomy()){
	    	Bukkit.getLogger().severe("Vault was not found, therfore plugin functionility is not possible");
	    	pm.disablePlugin(this);
            return;
	    }
	    if(!getDataFolder().exists()){
	    	getDataFolder().mkdirs();
	    	new File(getDataFolder() + File.separator + "schematics").mkdirs();
			try {

				new File(getDataFolder() + File.separator + "schematics.yml").createNewFile();
				saveDefaultConfig();
			} catch (IOException e) {
				e.printStackTrace();

			}
		}
		if(!new File(getDataFolder() + File.separator + "config.yml").exists())
				saveDefaultConfig();

         dependencies = new HashSet<>();
         getDependencies();
         config = YamlConfiguration.loadConfiguration(new File(getDataFolder() + File.separator + "config.yml"));
		 manager = new SchematicManager(this);
		 getCommand("in").setExecutor(new Commands(this));
		 rgManager = new RegionManager(this);
		 pm.registerEvents(new SListener(this), this);
		 pm.registerEvents(new SignListener(this), this);
	}

	public void onDisable() {
	}

	public boolean setupEconomy(){
		  if (getServer().getPluginManager().getPlugin("Vault") == null) {
	            return false;
	        }
	        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
	        if (rsp == null) 
	            return false;
	        
	        econ = rsp.getProvider();
	        return econ != null;
	}


	public SchematicManager getSchematicManager(){
		return manager;
	}

    private void getDependencies(){
        List<String> depends = java.util.Arrays.asList("WorldGuard", "PreciousStones",
                "Districts", "Factions", "Towny",
                "GriefPrevention");

        for(Plugin p: Bukkit.getPluginManager().getPlugins()) {
            if(depends.contains(p.getName())) {
                dependencies.add(p.getName());
            }
        }
    }

	public WorldEditPlugin getWorldEdit(){

		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");

		if(plugin == null || !(plugin instanceof WorldEditPlugin)){
			return null;
		}

		return (WorldEditPlugin) plugin;
	}


	public Economy getEcon(){
		return econ;
	}

    @Override
	public FileConfiguration getConfig(){
	return config;
	}

	public RegionManager getrManager(){
		return rgManager;
	}

}
