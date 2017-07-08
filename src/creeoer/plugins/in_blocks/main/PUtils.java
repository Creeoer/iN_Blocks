package creeoer.plugins.in_blocks.main;

import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class PUtils {

	private static final BlockFace[] axis = { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };
	private static final BlockFace[] radial = { BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST };



	// I'm going to be quite surprised if this works
	public static BlockFace getCardinalDirection(float yaw, boolean useSubCardinalDirections) {
		if (useSubCardinalDirections)
			return radial[Math.round(yaw / 45f) & 0x7].getOppositeFace();

		return axis[Math.round(yaw / 90f) & 0x3];


	}


	public static BlockFace parseBlockFace(String direction){
		switch(direction){
			case "NORTH":
				return BlockFace.NORTH;

			case"WEST":
				return BlockFace.WEST;

			case "EAST" :
				return BlockFace.EAST;

			case "SOUTH":
				return BlockFace.SOUTH;


			default:
				return BlockFace.NORTH;
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
