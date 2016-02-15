package creeoer.plugins.in_blocks.main;

import java.io.File;
import java.io.IOException;

import com.massivecraft.factions.Factions;
import com.palmergames.bukkit.towny.Towny;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

import com.wasteofplastic.districts.Districts;
import creeoer.plugins.in_blocks.listeners.SListener;
import creeoer.plugins.in_blocks.listeners.SignListener;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.milkbowl.vault.economy.Economy;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class iN_Blocks extends JavaPlugin {

	private Economy econ = null;
	private SchematicManager manager;
	private FileConfiguration config;
	private RegionManager rgManager;

	@SuppressWarnings(value = "all")
	public void onEnable(){


		PluginManager pm = Bukkit.getPluginManager();

	    if (!setupEconomy()){
	    	Bukkit.getLogger().severe("Vault was not found, therfore plugin functionility is not possible");
	    	pm.disablePlugin(this);
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


	public Districts getDistricts(){
		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("Districts");

		if(plugin == null || !(plugin instanceof Districts)){
			return null;
		}
		return (Districts) plugin;
	}

	public SchematicManager getSchematicManager(){
		return manager;
	}
	public WorldGuardPlugin getWorldGuard(){
		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
		if(plugin == null || !(plugin instanceof WorldGuardPlugin)){
			return null;
		}
		return (WorldGuardPlugin) plugin;
	}

    public PreciousStones getStones() {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("PreciousStones");
        if(plugin == null || !(plugin instanceof Factions)){
            return null;
        }
        return (PreciousStones) plugin;
    }

	public Factions getFactions(){
		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("Factions");
		if(plugin == null || !(plugin instanceof Factions)){
			return null;
		}
		return (Factions) plugin;
	}

	public GriefPrevention getGriefPrevention(){
		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("GriefPrevention");
		if(plugin == null || !(plugin instanceof GriefPrevention)){
			return null;
		}
		return (GriefPrevention) plugin;

	}
	public Towny getTowny(){
		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("Towny");
		if(plugin == null || !(plugin instanceof Towny)){
			return null;
		}
		return (Towny) plugin;

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
