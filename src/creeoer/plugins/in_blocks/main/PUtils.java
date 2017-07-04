package creeoer.plugins.in_blocks.main;

import org.bukkit.entity.Player;

public class PUtils {

	
	// I'm going to be quite surprised if this works
	public static String getCardinalDirection(Player p) {
		double rot = (p.getLocation().getYaw() - 90) % 360;
		if (rot < 0){
			rot += 360;
		}
		if (0 <= rot && rot < 22.5) {
			return "north";
		} else if (22.5 <= rot && rot < 67.5) {
			return "north";
		} else if (67.5 <= rot && rot < 112.5) {
			return "east";
		} else if (112.5 <= rot && rot < 157.5) {
			return "south";
		} else if (157.5 <= rot && rot < 202.5) {
			return "south";
		} else if (202.5 <= rot && rot < 247.5) {
			return "south";
		} else if (247.5 <= rot && rot < 292.5) {
			return "west";
		} else if (292.5 <= rot && rot < 337.5) {
			return "north";
		} else if (337.5 <= rot && rot < 360.0) {
			return "north";
		} else {
			return "No direction";
		}
	}
}
