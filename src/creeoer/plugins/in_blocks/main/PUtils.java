package creeoer.plugins.in_blocks.main;

import org.bukkit.block.BlockFace;
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


//Credit goes to andrepl
	public static int getRotateValue(BlockFace from, BlockFace to){
		switch(from){
			case NORTH:
				switch (to) {
					case NORTH:
						return 0;
					case EAST:
						return 90;
					case SOUTH:
						return 180;
					case WEST:
						return 270;
				}
				break;
			case EAST:
				switch (to) {
					case NORTH:
						return 270;
					case EAST:
						return 0;
					case SOUTH:
						return 90;
					case WEST:
						return 180;
				}
				break;
			case SOUTH:
				switch (to) {
					case NORTH:
						return 180;
					case EAST:
						return 270;
					case SOUTH:
						return 0;
					case WEST:
						return 90;
				}
				break;

			case WEST:
				switch (to) {
					case NORTH:
						return 90;
					case EAST:
						return 180;
					case SOUTH:
						return 270;
					case WEST:
						return 0;
				}
				break;
			default:
				return 0;


		}
		return 0;
	}
}
