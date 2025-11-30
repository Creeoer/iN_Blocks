package creeoer.plugins.in_blocks.main;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 * Main plugin class for iN_Blocks.
 * iN_Blocks is a schematic building plugin that allows players to place
 * pre-made structures in survival mode, requiring actual materials.
 *
 * <p>Features:
 * <ul>
 *   <li>Create schematics from WorldEdit selections</li>
 *   <li>Place schematics with material requirements</li>
 *   <li>Integration with protection plugins</li>
 *   <li>Economy support via Vault</li>
 *   <li>Shop signs for selling schematics</li>
 * </ul>
 * </p>
 *
 * @author Creeoer
 * @version 2.0.0
 */
public class iN_Blocks extends JavaPlugin {

    /** Supported protection plugin names */
    private static final List<String> SUPPORTED_PROTECTIONS = Arrays.asList(
            "WorldGuard", "PreciousStones", "Districts", "Factions",
            "Towny", "GriefPrevention", "RedProtect"
    );

    private Economy economy;
    private SchematicManager schematicManager;
    private RegionManager regionManager;
    private BuildManager buildManager;
    private FileConfiguration config;
    private FileConfiguration langConfig;
    private File langFile;

    /** Set of detected protection plugin dependencies */
    private Set<String> dependencies;

    @Override
    public void onEnable() {
        PluginManager pluginManager = Bukkit.getPluginManager();

        // Initialize plugin folders and files
        initializeFiles();

        // Load language configuration
        loadLanguageConfig();

        // Load main configuration
        if (!new File(getDataFolder(), "config.yml").exists()) {
            saveDefaultConfig();
        }
        config = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "config.yml"));

        // Initialize managers
        buildManager = new BuildManager(this);
        detectDependencies();
        schematicManager = new SchematicManager(this);

        // Register commands
        Commands commandHandler = new Commands(this);
        getCommand("in").setExecutor(commandHandler);
        getCommand("in").setTabCompleter(commandHandler);

        // Setup economy if enabled
        if (config.getBoolean("Options.economy-enabled", true)) {
            if (!setupEconomy()) {
                getLogger().warning("Vault not found! Economy features will be disabled.");
            }
        }

        // Initialize existing build tasks
        try {
            buildManager.initTasks();
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "Failed to load existing build tasks", e);
        }

        // Initialize region manager
        regionManager = new RegionManager(this);

        // Register event listeners
        pluginManager.registerEvents(new BlockListener(this), this);
        pluginManager.registerEvents(new SignListener(this), this);
        pluginManager.registerEvents(new PlayerActionsListener(this), this);

        getLogger().info("iN_Blocks v" + getDescription().getVersion() + " enabled!");
    }

    @Override
    public void onDisable() {
        if (buildManager != null) {
            buildManager.saveAllTasks();
            getLogger().info("Saved all active build tasks.");
        }
    }

    /**
     * Initializes plugin folders and required files.
     */
    private void initializeFiles() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        File schematicsDir = new File(getDataFolder(), "schematics");
        if (!schematicsDir.exists()) {
            schematicsDir.mkdirs();
        }

        File schematicsConfig = new File(getDataFolder(), "schematics.yml");
        if (!schematicsConfig.exists()) {
            try {
                schematicsConfig.createNewFile();
            } catch (IOException e) {
                getLogger().log(Level.WARNING, "Failed to create schematics.yml", e);
            }
        }
    }

    /**
     * Loads and initializes the language configuration file.
     */
    private void loadLanguageConfig() {
        langFile = new File(getDataFolder(), "lang.yml");

        if (!langFile.exists()) {
            try {
                langFile.createNewFile();
                langConfig = YamlConfiguration.loadConfiguration(langFile);

                // Save default messages
                for (Lang value : Lang.values()) {
                    langConfig.set(value.getPath(), value.getDefault());
                }
                langConfig.save(langFile);
            } catch (IOException e) {
                getLogger().log(Level.WARNING, "Failed to create lang.yml", e);
            }
        }

        langConfig = YamlConfiguration.loadConfiguration(langFile);
        Lang.setFile(langConfig);
    }

    /**
     * Sets up the Vault economy integration.
     *
     * @return true if economy was successfully set up
     */
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp =
                getServer().getServicesManager().getRegistration(Economy.class);

        if (rsp == null) {
            return false;
        }

        economy = rsp.getProvider();
        return economy != null;
    }

    /**
     * Detects installed protection plugins.
     */
    private void detectDependencies() {
        dependencies = new HashSet<>();

        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if (SUPPORTED_PROTECTIONS.contains(plugin.getName())) {
                dependencies.add(plugin.getName());
                getLogger().info("Detected protection plugin: " + plugin.getName());
            }
        }
    }

    /**
     * Gets the WorldEdit plugin instance.
     *
     * @return The WorldEditPlugin, or null if not available
     */
    public WorldEditPlugin getWorldEdit() {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        if (plugin instanceof WorldEditPlugin) {
            return (WorldEditPlugin) plugin;
        }
        return null;
    }

    /**
     * Gets the schematic manager.
     *
     * @return The SchematicManager instance
     */
    public SchematicManager getSchematicManager() {
        return schematicManager;
    }

    /**
     * Gets the region manager.
     *
     * @return The RegionManager instance
     */
    public RegionManager getRegionManager() {
        return regionManager;
    }

    /**
     * Gets the build manager.
     *
     * @return The BuildManager instance
     */
    public BuildManager getBuildManager() {
        return buildManager;
    }

    /**
     * Gets the Vault economy provider.
     *
     * @return The Economy instance, or null if not available
     */
    public Economy getEconomy() {
        return economy;
    }

    /**
     * Gets the set of detected protection plugin dependencies.
     *
     * @return Set of plugin names
     */
    public Set<String> getDependencies() {
        return dependencies;
    }

    @Override
    public FileConfiguration getConfig() {
        return config;
    }

    /**
     * @deprecated Use {@link #getRegionManager()} instead
     */
    @Deprecated
    public RegionManager getrManager() {
        return regionManager;
    }
}
