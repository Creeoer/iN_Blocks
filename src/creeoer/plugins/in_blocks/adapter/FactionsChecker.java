package creeoer.plugins.in_blocks.adapter;

import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.massivecore.ps.PS;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Legacy MassiveCraft Factions protection checker adapter.
 * Use this checker for older Factions implementations.
 *
 * <p>Allows building in:
 * <ul>
 *   <li>Wilderness (unclaimed land)</li>
 *   <li>Player's own faction territory</li>
 * </ul>
 * Denies building in safezones and warzones.</p>
 *
 * @author Creeoer
 * @version 2.0.0
 */
public class FactionsChecker implements iChecker {

    /**
     * Creates a new FactionsChecker.
     */
    public FactionsChecker() {
    }

    @Override
    public boolean isValidPlacement(Location location, Player player) {
        try {
            // Get special factions
            Faction wilderness = FactionColl.get().getNone();
            Faction safeZone = FactionColl.get().getSafezone();
            Faction warZone = FactionColl.get().getWarzone();

            // Get the faction at this location
            PS position = PS.valueOf(location);
            Faction factionAtLocation = BoardColl.get().getFactionAt(position);

            // Deny in safezone or warzone
            if (factionAtLocation == safeZone || factionAtLocation == warZone) {
                return false;
            }

            // Allow in wilderness
            if (factionAtLocation == wilderness) {
                return true;
            }

            // Get player's faction
            MPlayer mPlayer = MPlayer.get(player.getUniqueId());
            Faction playerFaction = mPlayer.getFaction();

            // Allow in own faction territory
            return factionAtLocation.equals(playerFaction);

        } catch (Exception e) {
            // If check fails, deny by default
            return false;
        }
    }
}
