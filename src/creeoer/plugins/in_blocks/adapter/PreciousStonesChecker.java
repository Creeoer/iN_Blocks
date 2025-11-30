package creeoer.plugins.in_blocks.adapter;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * PreciousStones protection checker adapter.
 * Uses the PreciousStones API to check build permissions.
 *
 * @author Creeoer
 * @version 2.0.0
 */
public class PreciousStonesChecker implements iChecker {

    /**
     * Creates a new PreciousStonesChecker.
     */
    public PreciousStonesChecker() {
    }

    @Override
    public boolean isValidPlacement(Location location, Player player) {
        try {
            return PreciousStones.API().canPlace(player, location);
        } catch (Exception e) {
            // If check fails, deny by default
            return false;
        }
    }
}
