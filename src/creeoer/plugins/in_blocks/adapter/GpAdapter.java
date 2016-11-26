package creeoer.plugins.in_blocks.adapter;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Created by CREEOER on 11/26/2016.
 */
public class GpAdapter implements iChecker{
    public GpAdapter(){

    }
    @Override
    public boolean isValidPlacement(Location loc ,Player player){
        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(loc, true, null);

        if(claim == null)
            return true;

        return claim.getOwnerName().equals(player.getName());

    }
}
