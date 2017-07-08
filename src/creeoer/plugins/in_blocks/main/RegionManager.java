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
           else if(plugin.equals("PreciousStones")) {
               checkers.add(new PsAdapter());
           }
           else if(plugin.equals("GriefPrevention")) {
               checkers.add(new GpAdapter());
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
        List<Location> locs = generateLocs(p.getLocation(), sch.getRegion());


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
    public List<Location> generateLocs(Location l, CuboidClipboard region) throws DataException, IOException{
        List<Location> locs = new ArrayList<>();
        int xWidth, zLength, yHeight;
        xWidth = region.getWidth();
        yHeight = region.getHeight();
        zLength = region.getLength();
        int cRadius = config.getInt("Options.check-radius");
        l.add(xWidth, yHeight, zLength);
        int sX = l.getBlockX();
        int sY = l.getBlockY();
        int sZ = l.getBlockZ();

        for(int x = sX;  x <= l.getBlockX() + cRadius; x++){
            for(int y = sY ; y <= l.getBlockY()  + cRadius; y++){
                for(int z = sZ; z <= l.getBlockZ() + cRadius; z++){
                    locs.add(l.clone().add(x,y,z));
                }
            }
        }
        return locs;
    }
}
