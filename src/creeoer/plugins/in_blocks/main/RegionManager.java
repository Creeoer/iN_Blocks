package creeoer.plugins.in_blocks.main;

import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.massivecore.ps.PS;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldguard.LocalPlayer;
import com.wasteofplastic.districts.DistrictRegion;
import com.wasteofplastic.districts.Districts;
import com.wasteofplastic.districts.GridManager;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;


public class RegionManager {

    iN_Blocks main = null;

    //TODO TO be used when someone nags that my plugin is causing lag issues
    private boolean wgPresent = false;
    private boolean townPresent = false;
    private boolean facPresent = false;
    private boolean districtsPresent = false;

    FileConfiguration config;

    protected RegionManager(iN_Blocks instance){
        main = instance;
        config = main.getConfig();


        //Protection services initialization
        if(main.getDistricts() != null)
            districtsPresent = true;
        if(main.getWorldGuard() != null)
            wgPresent = true;
        if(main.getTowny() != null)
            townPresent = true;
        if(main.getFactions() != null)
            facPresent = true;
    }


    public  boolean canPlayerPlace(final Player p, ISchematic sch) throws DataException, IOException, NotRegisteredException {
        List<Location> locs = generateLocs(p.getLocation(), sch.getData());

        /* TODO Do not use redundant checks if plugin is not present
        Add precious stones support
          */




        for(Location loc: locs){
            if (!isPlayerTown(loc, p) ||
             !isPlayerFac(loc, p)  ||
             !isPlayerArea(loc, p)  ||
             !isPlayerStone(loc, p) ||
             !isPlayerRegion(loc, p)  ||
             !isPlayerDistrict(loc, p) ) return false;
        }


        return true;

    }



    public boolean isPlayerRegion(Location loc, Player player) {

        if (main.getWorldGuard() == null)
            return true;

        if(player.isOp())
            return true;


        LocalPlayer p = main.getWorldGuard().wrapPlayer(player);
        com.sk89q.worldguard.protection.managers.RegionManager regionManager = main.getWorldGuard().getRegionManager(player.getWorld());
        if (regionManager == null)
            return true;

        if (regionManager.getApplicableRegions(loc) == null)
            return true;

        if (regionManager.getApplicableRegions(loc).isOwnerOfAll(p) || regionManager.getApplicableRegions(loc).isMemberOfAll(p))
            return true;


        return false;
    }


    public boolean isPlayerFac(Location loc  , Player player){
        if(main.getFactions() == null)
            return true;

        if(player.isOp())
            return true;


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

        if(BoardColl.get().getFactionAt(PS.valueOf(loc)) == pFaction){
            return true;
        }
        return false;


    }

    public boolean isPlayerArea(Location loc, Player player){
        if(main.getGriefPrevention() == null)
            return true;

        if(player.isOp())
            return true;

        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(loc, true, null);

        if(claim == null)
            return true;

        if(claim.getOwnerName().equals( player.getName())){
            return true;
        }
        return false;
    }

    public boolean isPlayerTown(Location loc, Player player) throws NotRegisteredException{
        if(main.getTowny() == null)
            return true;

        if(player.isOp())
            return true;


        Resident r = TownyUniverse.getDataSource().getResident(player.getName());

        if(!r.hasTown())
            return false;

        if(TownyUniverse.getTownName(loc) == null)
            return false;

        if(!r.getTown().getName().equals(TownyUniverse.getTownName(loc)))
            return false;

        return true;

    }

    public boolean isPlayerStone(Location loc, Player p){
       if(main.getStones() == null) return true;

        if(PreciousStones.API().canPlace(p, loc))  return true;

        return false;
    }

    public boolean isPlayerDistrict(Location loc, Player p){

        if(main.getDistricts() == null)
            return true;


        GridManager manager = Districts.getPlugin().getGrid(p.getWorld().getName());

        if(p.isOp())
            return true;

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


    public List<Location> generateLocs(Location l, CuboidClipboard cc) throws DataException, IOException{
        List<Location> locs = new ArrayList<>();
        int xWidth  = cc.getWidth();
        int cRadius = config.getInt("Options.check-radius");
        int zLength = cc.getLength();
        int yHeight = cc.getHeight();
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
