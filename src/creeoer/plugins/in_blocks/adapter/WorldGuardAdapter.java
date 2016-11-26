package creeoer.plugins.in_blocks.adapter;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Created by CREEOER on 11/26/2016.
 */
public class WorldGuardAdapter implements iChecker{

    public WorldGuardAdapter(){

    }

    @Override
    public boolean isValidPlacement(Location loc,Player player){
        WorldGuardPlugin instance = WorldGuardPlugin.inst();
        LocalPlayer p = instance.wrapPlayer(player);
        com.sk89q.worldguard.protection.managers.RegionManager regionManager = instance.getRegionManager(player.getWorld());
        if (regionManager == null)
            return true;

        if (regionManager.getApplicableRegions(loc).size() == 0)
            return true;

        return  regionManager.getApplicableRegions(loc).canBuild(p);
    }
}
