package creeoer.plugins.in_blocks.main;

import org.bukkit.block.BlockFace;

import java.util.EnumMap;
import java.util.Map;

/**
 * Utility class for directional calculations and BlockFace operations.
 * Provides methods for converting player yaw to cardinal directions and
 * calculating rotation values between directions.
 *
 * @author Creeoer
 * @version 2.0.0
 */
public final class PUtils {

    /** Cardinal directions in clockwise order (N, E, S, W) */
    private static final BlockFace[] CARDINAL_DIRECTIONS = {
            BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST
    };

    /** All 8 directions including intercardinal (NE, SE, SW, NW) */
    private static final BlockFace[] ALL_DIRECTIONS = {
            BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST,
            BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST
    };

    /** Precomputed rotation lookup table for performance */
    private static final Map<BlockFace, Map<BlockFace, Integer>> ROTATION_TABLE;

    static {
        ROTATION_TABLE = new EnumMap<>(BlockFace.class);
        initializeRotationTable();
    }

    /**
     * Private constructor to prevent instantiation.
     */
    private PUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Initializes the rotation lookup table.
     */
    private static void initializeRotationTable() {
        // Define rotation values from each direction to each other direction
        int[][] rotations = {
                // TO:  N,   E,   S,   W
                {0, 90, 180, 270},  // FROM: N
                {270, 0, 90, 180},  // FROM: E
                {180, 270, 0, 90},  // FROM: S
                {90, 180, 270, 0}   // FROM: W
        };

        for (int from = 0; from < CARDINAL_DIRECTIONS.length; from++) {
            Map<BlockFace, Integer> toMap = new EnumMap<>(BlockFace.class);
            for (int to = 0; to < CARDINAL_DIRECTIONS.length; to++) {
                toMap.put(CARDINAL_DIRECTIONS[to], rotations[from][to]);
            }
            ROTATION_TABLE.put(CARDINAL_DIRECTIONS[from], toMap);
        }
    }

    /**
     * Gets the cardinal direction from a player's yaw angle.
     *
     * <p>The yaw angle in Minecraft:
     * <ul>
     *   <li>0 or 360 = South</li>
     *   <li>90 = West</li>
     *   <li>180 = North</li>
     *   <li>270 = East</li>
     * </ul>
     * </p>
     *
     * @param yaw The yaw angle in degrees
     * @param useSubCardinal If true, returns one of 8 directions; if false, one of 4
     * @return The BlockFace representing the direction
     */
    public static BlockFace getCardinalDirection(float yaw, boolean useSubCardinal) {
        // Normalize yaw to 0-360 range
        yaw = ((yaw % 360) + 360) % 360;

        if (useSubCardinal) {
            // 8 directions, each covering 45 degrees
            int index = Math.round(yaw / 45f) & 0x7;
            return ALL_DIRECTIONS[index].getOppositeFace();
        } else {
            // 4 directions, each covering 90 degrees
            int index = Math.round(yaw / 90f) & 0x3;
            return CARDINAL_DIRECTIONS[index];
        }
    }

    /**
     * Parses a direction string into a BlockFace.
     *
     * @param direction The direction string (NORTH, SOUTH, EAST, WEST)
     * @return The corresponding BlockFace, defaults to NORTH if invalid
     */
    public static BlockFace parseBlockFace(String direction) {
        if (direction == null) {
            return BlockFace.NORTH;
        }

        try {
            return BlockFace.valueOf(direction.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            return BlockFace.NORTH;
        }
    }

    /**
     * Calculates the rotation angle needed to transform from one direction to another.
     *
     * @param from The starting direction
     * @param to The target direction
     * @return The rotation angle in degrees (0, 90, 180, or 270)
     */
    public static int getRotateValue(BlockFace from, BlockFace to) {
        // Validate inputs - only cardinal directions are supported
        if (!isCardinalDirection(from) || !isCardinalDirection(to)) {
            return 0;
        }

        Map<BlockFace, Integer> toMap = ROTATION_TABLE.get(from);
        if (toMap != null) {
            Integer rotation = toMap.get(to);
            if (rotation != null) {
                return rotation;
            }
        }

        return 0;
    }

    /**
     * Checks if a BlockFace is a cardinal direction (N, E, S, W).
     *
     * @param face The BlockFace to check
     * @return true if the face is a cardinal direction
     */
    public static boolean isCardinalDirection(BlockFace face) {
        return face == BlockFace.NORTH || face == BlockFace.EAST ||
                face == BlockFace.SOUTH || face == BlockFace.WEST;
    }

    /**
     * Gets the opposite cardinal direction.
     *
     * @param face The BlockFace
     * @return The opposite direction
     */
    public static BlockFace getOpposite(BlockFace face) {
        return face.getOppositeFace();
    }

    /**
     * Rotates a BlockFace by the specified degrees clockwise.
     *
     * @param face The BlockFace to rotate
     * @param degrees The rotation in degrees (must be multiple of 90)
     * @return The rotated BlockFace
     */
    public static BlockFace rotate(BlockFace face, int degrees) {
        if (!isCardinalDirection(face)) {
            return face;
        }

        int index = -1;
        for (int i = 0; i < CARDINAL_DIRECTIONS.length; i++) {
            if (CARDINAL_DIRECTIONS[i] == face) {
                index = i;
                break;
            }
        }

        if (index == -1) {
            return face;
        }

        int rotations = ((degrees % 360) + 360) / 90;
        int newIndex = (index + rotations) % 4;

        return CARDINAL_DIRECTIONS[newIndex];
    }
}
