package creeoer.plugins.in_blocks.main;


import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Created by CREEOER on 11/20/2016.
 */
public interface iChecker{

     //Interface allows checking methods to be grouped together
     boolean isValidPlacement(Location l,Player p);

}

