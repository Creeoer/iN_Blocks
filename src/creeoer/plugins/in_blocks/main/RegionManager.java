package creeoer.plugins.in_blocks.main;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.world.DataException;
import creeoer.plugins.in_blocks.adapter.*;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class RegionManager {

    private iN_Blocks main;
    private List<iChecker> checkers;
    private FileConfiguration config;


    protected RegionManager(iN_Blocks instance) {
        main = instance;
        config = main.getConfig();
        checkers = new ArrayList<>();

        //This takes the instances provided and, depending on the plugin, adds the method reference
        //Prevents redundant checks
        registerProtectionDependencies();
    }



    public boolean isFactionsUUID(){
        Class<?> factionsClass = null;
        try {
            factionsClass = Class.forName("com.massivecraft.factions.entity.BoardColl");
            return true;
            //Ok no factions class was found
        } catch (Exception e) {
            factionsClass = null;
            return false;
        }
    }


    public boolean canPlayerPlace(final Player p, BuildSchematic sch, Location blockL) throws DataException, IOException {

        if (p.isOp())
            return true;

        //Welp no dependencies, MY WORK IS DONE HERE
        if (main.dependencies.isEmpty())
            return true;



        List<Location> locs = generateLocs(blockL, sch.getRegion());
        List<Boolean> bools = new ArrayList<>();

        for (Location loc : locs) {
            for (iChecker check : checkers) {
                bools.add(check.isValidPlacement(loc, p));
            }
        }

        if (bools.contains(false))
            return false;

        return true;
    }

    private void registerProtectionDependencies(){
        for (String plugin : main.dependencies) {
            if (plugin.equals("WorldGuard")) {
                checkers.add(new WorldGuardChecker());
            } else if (plugin.equals("PreciousStones")) {
                checkers.add(new PreciousStonesChecker());
            } else if (plugin.equals("GriefPrevention")) {
                checkers.add(new GriefPreventionChecker());
            } else if (plugin.equals("Towny")) {
                checkers.add(new TownyChecker());
            } else if (plugin.equals("RedProtect")) {
                checkers.add(new RedProtectChecker());
            } else if (plugin.equals("Factions")) {
                if(isFactionsUUID()) {
                    checkers.add(new FactionsUUIDChecker());
                } else {
                    checkers.add(new FactionsChecker());
                }
            }
        }
    }

    //TODO Get around to replacing this...
    @Deprecated
    public List<Location> generateLocs(Location l, CuboidClipboard region) throws DataException, IOException {
        List<Location> locs = new ArrayList<>();
        int xWidth, zLength, yHeight;
        xWidth = region.getWidth();
        yHeight = region.getHeight();
        zLength = region.getLength();
        int cRadius = config.getInt("Options.check-radius");


        for (int x = 0; x <= xWidth + cRadius; x++) {
            for (int y = 0; y <=  yHeight + cRadius; y++) {
                for (int z = 0; z <=  zLength + cRadius; z++) {
                    locs.add(l.clone().add(x, y, z));
                }
            }
        }
        return locs;
    }
}
