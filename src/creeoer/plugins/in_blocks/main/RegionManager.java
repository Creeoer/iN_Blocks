package creeoer.plugins.in_blocks.main;

import com.massivecraft.factions.Factions;
import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.massivecore.ps.PS;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.wasteofplastic.districts.DistrictRegion;
import com.wasteofplastic.districts.Districts;
import com.wasteofplastic.districts.GridManager;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


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
       for(Plugin plugin: main.dependencies) {
           if(plugin instanceof WorldGuardPlugin) {
               checkers.add(this::isPlayerRegion);
           }
           else if(plugin instanceof Districts) {
               checkers.add(this::isPlayerDistrict);
           }
           else if(plugin instanceof PreciousStones) {
               checkers.add(this::isPlayerStone);
           }
           else if(plugin instanceof GriefPrevention) {
               checkers.add(this::isPlayerRegion);
           }
           else if(plugin instanceof Towny){
               checkers.add(this::isPlayerTown);
           }
           else if(plugin instanceof Factions) {
               checkers.add(this::isPlayerFac);
           }
       }
    }


    public  boolean canPlayerPlace(final Player p, ISchematic sch) throws DataException, IOException{

        if(p.isOp()) return true;

        //Welp no dependencies, MY WORK IS DONE HERE
        if(main.dependencies.isEmpty()) return true;

        //This generates a list of locations to go by
        List<Location> locs = generateLocs(p.getLocation(), sch.getRegion());

        //Checks to make sure all claims are valid
        for(Location loc: locs){
           for(iChecker check: checkers) {
               check.isValidPlacement(loc, p);
           }
        }


        return true;

    }




    public boolean isPlayerRegion(Location loc, Player player) {
        WorldGuardPlugin instance = WorldGuardPlugin.inst();
        LocalPlayer p = instance.wrapPlayer(player);
        com.sk89q.worldguard.protection.managers.RegionManager regionManager = instance.getRegionManager(player.getWorld());
        if (regionManager == null)
            return true;

        if (regionManager.getApplicableRegions(loc).size() == 0)
            return true;

        return  regionManager.getApplicableRegions(loc).canBuild(p);
    }


    public boolean isPlayerFac(Location loc  , Player player){
        Faction wilderness = FactionColl.get().getNone();
        Faction safeZone = FactionColl.get().getSafezone();
        Faction warZone = FactionColl.get().getWarzone();

        //Ok, so if the faction were to be a safezone and/or warzone and the player was not op, return false
        if(BoardColl.get().getFactionAt(PS.valueOf(loc)) == safeZone || BoardColl.get().getFactionAt(PS.valueOf(loc)) == warZone)
            return false;


        if(BoardColl.get().getFactionAt(PS.valueOf(loc)) == wilderness)
            return true;

        MPlayer mplayer = MPlayer.get(player.getUniqueId());
        Faction pFaction = mplayer.getFaction();

        if(BoardColl.get().getFactionAt(PS.valueOf(loc)) == pFaction)
            return true;

        return false;


    }

    public boolean isPlayerArea(Location loc, Player player){
        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(loc, true, null);

        if(claim == null)
            return true;

        return claim.getOwnerName().equals(player.getName());

    }


    public boolean isPlayerTown(Location loc, Player player){

try{
    Resident r=TownyUniverse.getDataSource().getResident(player.getName());
    if(TownyUniverse.getTownName(loc)==null)
        return true;
    if(TownyUniverse.getTownName(loc)!=null&!r.hasTown())
        return false;
    if(!r.getTown().getName().equals(TownyUniverse.getTownName(loc)))
        return false;
    return true;

    //Fuck you towny exceptions
} catch (Exception e) {}
        return false;
    }


    public boolean isPlayerStone(Location loc, Player p){
        return PreciousStones.API().canPlace(p, loc);
    }



    public boolean isPlayerDistrict(Location loc, Player p){
        GridManager manager = Districts.getPlugin().getGrid(p.getWorld().getName());
        if(manager.districtAtLocation(loc)) {
            DistrictRegion region = manager.getDistrictRegionAt(loc);
            if (region.getOwner().equals(p.getUniqueId()))
                return true;
            for (UUID trusted : region.getOwnerTrustedUUID()) {
                if (trusted.equals(p.getUniqueId()))
                    return true;
            }
            return false;
        }
        return true;
    }


    //TODO Get around to replacing this...
    @Deprecated
    public List<Location> generateLocs(Location l, Region region) throws DataException, IOException{
        List<Location> locs = new ArrayList<>();
        int xWidth, zLength, yHeight;
        xWidth = region.getWidth();
        yHeight = region.getHeight();
        zLength = region.getLength();
        int cRadius = config.getInt("Options.check-radius");
        double xx = l.getX();
        double yy = l.getY();
        double zz = l.getZ();
        for(int x = 0; x <= xWidth + cRadius; x++){
            for(int y = 0; y <= yHeight + cRadius; y++){
                for(int z = 0; z <= zLength + cRadius; z++){
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
