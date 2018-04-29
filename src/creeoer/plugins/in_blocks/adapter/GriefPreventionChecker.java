package creeoer.plugins.in_blocks.adapter;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Location;
import org.bukkit.entity.Player;


public class GriefPreventionChecker implements iChecker{
    public GriefPreventionChecker(){

    }
    @Override
    public boolean isValidPlacement(Location loc ,Player player){
        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(loc, false, null);

                    if (claim == null)
                       return true;

                    return claim.getOwnerName().equals(player.getName());
    }
}
