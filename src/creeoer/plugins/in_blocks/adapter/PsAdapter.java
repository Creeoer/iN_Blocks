package creeoer.plugins.in_blocks.adapter;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Created by CREEOER on 11/26/2016.
 */
public class PsAdapter implements iChecker{



    public PsAdapter(){

    }

    @Override
    public boolean isValidPlacement(Location loc,Player p){
        return PreciousStones.API().canPlace(p, loc);
    }
}
