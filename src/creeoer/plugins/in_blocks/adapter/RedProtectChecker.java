package creeoer.plugins.in_blocks.adapter;


import br.net.fabiozumbi12.RedProtect.Bukkit.API.RedProtectAPI;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Created by Frank on 7/8/2017.
 */
public class RedProtectChecker implements iChecker{
    @Override
    public boolean isValidPlacement(Location l, Player p) {
        Region region = RedProtect.get().getAPI().getRegion(l);

        if(region == null)
            return true;

        return region.canBuild(p);
    }
}
