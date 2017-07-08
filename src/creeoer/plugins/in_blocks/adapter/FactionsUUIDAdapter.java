package creeoer.plugins.in_blocks.adapter;

import com.massivecraft.factions.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Created by black on 7/8/2017.
 */
public class FactionsUUIDAdapter implements iChecker {
    @Override
    public boolean isValidPlacement(Location l, Player p) {

        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(p);



        FLocation fLoc = new FLocation(l);
        Faction fac = Board.getInstance().getFactionAt(fLoc);

        if(Board.getInstance().getFactionAt(fLoc).isWarZone() || Board.getInstance().getFactionAt(fLoc).isSafeZone())
            return false;

        if(Board.getInstance().getFactionAt(fLoc).isWilderness()) {
            Bukkit.broadcastMessage("widrness");
            return true;
        }


        if(fPlayer.getFaction() == fac) {
            Bukkit.broadcastMessage("yuo");
            return true;
        }

        return false;
    }
}
