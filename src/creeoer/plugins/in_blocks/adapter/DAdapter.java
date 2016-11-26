package creeoer.plugins.in_blocks.adapter;

import com.wasteofplastic.districts.DistrictRegion;
import com.wasteofplastic.districts.Districts;
import com.wasteofplastic.districts.GridManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Created by CREEOER on 11/26/2016.
 */
public class DAdapter implements iChecker{

    public DAdapter(){

    }

    @Override
    public boolean isValidPlacement(Location loc,Player p){
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
}
