package creeoer.plugins.in_blocks.adapter;

import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.massivecore.ps.PS;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Created by CREEOER on 11/26/2016.
 */
public class FactionsAdapter implements iChecker {
    public FactionsAdapter() {

    }

    @Override
    public boolean isValidPlacement(Location loc, Player player) {
        Faction wilderness = FactionColl.get().getNone();
        Faction safeZone = FactionColl.get().getSafezone();
        Faction warZone = FactionColl.get().getWarzone();

        //Ok, so if the faction were to be a safezone and/or warzone and the player was not op, return false
        if (BoardColl.get().getFactionAt(PS.valueOf(loc)) == safeZone || BoardColl.get().getFactionAt(PS.valueOf(loc)) == warZone)
            return false;


        if (BoardColl.get().getFactionAt(PS.valueOf(loc)) == wilderness)
            return true;

        MPlayer mplayer = MPlayer.get(player.getUniqueId());
        Faction pFaction = mplayer.getFaction();

        if (BoardColl.get().getFactionAt(PS.valueOf(loc)) == pFaction)
            return true;

        return false;
    }
}
