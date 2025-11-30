package creeoer.plugins.in_blocks.adapter;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * GriefPrevention protection checker adapter.
 * Checks if a player owns or has permission to build in a claim.
 *
 * @author Creeoer
 * @version 2.0.0
 */
public class GriefPreventionChecker implements iChecker {

    /**
     * Creates a new GriefPreventionChecker.
     */
    public GriefPreventionChecker() {
    }

    @Override
    public boolean isValidPlacement(Location location, Player player) {
        try {
            // Get the claim at this location
            Claim claim = GriefPrevention.instance.dataStore.getClaimAt(location, false, null);

            // No claim = can build
            if (claim == null) {
                return true;
            }

            // Check if player is the owner
            if (claim.getOwnerName() != null && claim.getOwnerName().equals(player.getName())) {
                return true;
            }

            // Check if player has build permission in the claim
            String denyReason = claim.allowBuild(player, null);
            return denyReason == null;

        } catch (Exception e) {
            // If check fails, deny by default for safety
            return false;
        }
    }
}
