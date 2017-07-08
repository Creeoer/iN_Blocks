package creeoer.plugins.in_blocks.adapter;

import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Created by CREEOER on 11/26/2016.
 */
public class TownyAdapter implements iChecker{

    public TownyAdapter(){

    }

    @Override
    public boolean isValidPlacement(Location loc,Player player){

        try{
            Resident r=TownyUniverse.getDataSource().getResident(player.getName());
            if(TownyUniverse.getTownName(loc)==null)
                return true;
            if(TownyUniverse.getTownName(loc)!=null && !r.hasTown())
                return false;
            if(!r.getTown().getName().equals(TownyUniverse.getTownName(loc)))
                return false;
            return true;

            //Fuck you towny exceptions
        } catch (Exception e) {}
        return false;
    }
}
