package creeoer.plugins.in_blocks.adapter;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Towny protection checker adapter.
 * Checks if a player can build in a town's territory.
 *
 * @author Creeoer
 * @version 2.0.0
 */
public class TownyChecker implements iChecker {

    /**
     * Creates a new TownyChecker.
     */
    public TownyChecker() {
    }

    @Override
    public boolean isValidPlacement(Location location, Player player) {
        try {
            TownyAPI towny = TownyAPI.getInstance();

            // Get the town block at this location
            TownBlock townBlock = towny.getTownBlock(location);

            // No town block = wilderness, can build
            if (townBlock == null) {
                return true;
            }

            // Get the town that owns this block
            Town town = townBlock.getTownOrNull();
            if (town == null) {
                return true;
            }

            // Get the resident for this player
            Resident resident = towny.getResident(player);
            if (resident == null) {
                return false;
            }

            // Check if player has a town
            if (!resident.hasTown()) {
                return false;
            }

            // Check if player's town matches the territory's town
            Town playerTown = resident.getTownOrNull();
            if (playerTown == null) {
                return false;
            }

            return playerTown.getName().equals(town.getName());

        } catch (Exception e) {
            // If check fails, deny by default
            return false;
        }
    }
}
