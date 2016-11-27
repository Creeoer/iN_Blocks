package creeoer.plugins.in_blocks.main;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.world.DataException;
import creeoer.plugins.in_blocks.adapter.*;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class RegionManager {

    iN_Blocks main = null;
    List<iChecker> checkers;


    FileConfiguration config;

    protected RegionManager(iN_Blocks instance){
        main = instance;
        config = main.getConfig();
        checkers = new ArrayList<>();

        //This takes the instances provided and, depending on the plugin, adds the method reference
        //Prevents redundant checks
       for(String plugin: main.dependencies) {
           if(plugin.equals("WorldGuard")) {
               checkers.add(new WorldGuardAdapter());
           }
           else if(plugin.equals("Districts")) {
               checkers.add(new DAdapter());
           }
           else if(plugin.equals("PreciousStones")) {
               checkers.add(new PsAdapter());
           }
           else if(plugin.equals("GriefPrevention")) {
               checkers.add(new WorldGuardAdapter());
           }
           else if(plugin.equals("Towny")){
               checkers.add(new TownyAdapter());
           }
           else if(plugin.equals("Factions")){
               checkers.add(new FactionsAdapter());
           }
       }


    }


    public  boolean canPlayerPlace(final Player p, ISchematic sch) throws DataException, IOException{

        if(p.isOp()) return true;

        //Welp no dependencies, MY WORK IS DONE HERE
        if(main.dependencies.isEmpty())
            return true;



        //This generates a list of locations to go by
        List<Location> locs = generateLocs(p.getLocation(), sch.getBoard());


        List<Boolean> bools = new ArrayList<>();
        //Checks to make sure all claims are valid
        for(Location loc: locs){
           for(iChecker check: checkers) {
               bools.add(check.isValidPlacement(loc, p));
           }
        }

        if(bools.contains(false)) return false;

        return true;

    }

    //TODO Get around to replacing this...
    @Deprecated
    public List<Location> generateLocs(Location l, Clipboard board) throws DataException, IOException{
        List<Location> locs = new ArrayList<>();

        Vector min, max;
        int minX, minY, minZ, maxX, maxY, maxZ;

        min = board.getMinimumPoint();
        max = board.getMaximumPoint();

        minX = min.getBlockX();
        minY = min.getBlockY();
        minZ = min.getBlockZ();
        maxX = max.getBlockX();
        maxY = max.getBlockY();
        maxZ = max.getBlockZ();

        int cRadius = config.getInt("Options.check-radius");
        double xx = l.getX();
        double yy = l.getY();
        double zz = l.getZ();
        for(int x = minX; x <= maxX + cRadius; x++){
            for(int y = minY; y <= maxY + cRadius; y++){
                for(int z = minZ; z <= maxZ + cRadius; z++){
                    Location loc1 = new Location(l.getWorld(), xx + x, yy + y, zz + z);
                    locs.add(loc1);
                    Location loc2 = new Location(l.getWorld(), xx - x, yy - y, zz - z);
                    locs.add(loc2);
                    Location loc3 = new Location(l.getWorld(), xx - x, yy - y, zz + z);
                    locs.add(loc3);
                    Location loc4 = new Location(l.getWorld(), xx + x, yy - y, zz + z);
                    locs.add(loc4);
                    Location loc5 = new Location(l.getWorld(), xx - x, yy + y, zz - z);
                    locs.add(loc5);
                    Location loc6 = new Location(l.getWorld(), xx - x, yy - y, zz + z);
                    locs.add(loc6);
                    Location loc7 = new Location(l.getWorld(), xx - x, yy + y, zz + z);
                    locs.add(loc7);
                    Location loc8 = new Location(l.getWorld(), xx + x, yy + y, zz - z);
                    locs.add(loc8);
                }
            }
        }
        return locs;
    }
}
