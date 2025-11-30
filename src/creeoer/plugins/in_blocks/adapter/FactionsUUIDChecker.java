package creeoer.plugins.in_blocks.adapter;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * FactionsUUID protection checker adapter.
 * Use this checker for modern UUID-based Factions implementations.
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
public class FactionsUUIDChecker implements iChecker {

    /**
     * Creates a new FactionsUUIDChecker.
     */
    public FactionsUUIDChecker() {
    }

    @Override
    public boolean isValidPlacement(Location location, Player player) {
        try {
            // Get the FPlayer for this player
            FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);

            // Get the faction at this location
            FLocation fLocation = new FLocation(location);
            Faction factionAtLocation = Board.getInstance().getFactionAt(fLocation);

            // Deny in safezone or warzone
            if (factionAtLocation.isWarZone() || factionAtLocation.isSafeZone()) {
                return false;
            }

            // Allow in wilderness
            if (factionAtLocation.isWilderness()) {
                return true;
            }

            // Allow in own faction territory
            return fPlayer.getFaction().equals(factionAtLocation);

        } catch (Exception e) {
            // If check fails, deny by default
            return false;
        }
    }
}
