package creeoer.plugins.in_blocks.objects;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Enumeration of all localizable messages used throughout the plugin.
 * Messages can be customized via the lang.yml configuration file.
 *
 * <p>Each enum constant contains a configuration path and a default message value.
 * When {@link #toString()} is called, the message is retrieved from the language
 * file, falling back to the default if not configured.</p>
 *
 * <p>Placeholders:
 * <ul>
 *   <li>{@code %s} - Generic string replacement (schematic name, etc.)</li>
 *   <li>{@code %n} - Numeric replacement (time in seconds, etc.)</li>
 *   <li>{@code %d} - Direction replacement</li>
 * </ul>
 * </p>
 *
 * @author Creeoer
 * @version 2.0.0
 */
public enum Lang {

    /** Message when placement location is invalid or protected */
    BAD_PLACE("messages.no-place", "Can't place build block here!"),

    /** Success message when schematic is placed */
    SUCCESS_PLACE("messages.place", "Unloading preview... schematic placed successfully!"),

    /** Message when player lacks permission */
    NO_PERM("messages.no-perm", "You don't have permission for this!"),

    /** Message when shop sign has invalid syntax */
    INVALID_SIGN("messages.invalid-sign", "Incorrect sign syntax"),

    /** Success message when shop sign is created */
    VALID_SIGN("messages.valid-sign", "Shop sign successfully placed!"),

    /** Message when building is completed */
    COMPLETE("messages.building-completed", "Building completed successfully!"),

    /** Success message when purchasing from shop */
    BUY("messages.buy", "Build block bought successfully!"),

    /** Message when player cannot afford purchase */
    AFFORD("messages.afford", "You can't afford this build block!"),

    /** Message when command syntax is incorrect */
    SYNTAX("messages.syntax-error", "Incorrect syntax!"),

    /** Message showing available commands */
    COMMANDS("messages.commands", "Commands: /in [give|create|delete|list|reload]"),

    /** Message when schematic is created. Use %s for name and %d for direction */
    CREATE("messages.create", "The schematic '%s' was successfully created facing %d!"),

    /** Message when schematic does not exist */
    EXISTS("messages.exists", "This schematic does not exist!"),

    /** Message when schematic name is already taken */
    ALREADY_EXISTS("messages.already-exists", "This schematic already exists!"),

    /** Message when schematic is deleted. Use %s for name */
    DELETE("messages.delete", "The schematic '%s' has been successfully deleted!"),

    /** Message when console tries to use player-only command */
    CONSOLE("messages.console", "This command can only be used by players!"),

    /** Preview mode instructions. Use %n for timeout seconds */
    PREVIEW("messages.preview", "Preview mode! Type 'yes' within %n seconds to place, or anything else to cancel."),

    /** Message when trying to place while already in preview mode */
    PREVIEW_ERROR("messages.preview-error", "You can't place another build block while in preview mode!"),

    /** Message when placement is cancelled */
    CANCEL("messages.build-cancel", "Placement cancelled!"),

    /** Message when trying to break someone else's build chest */
    BREAK_CHEST_OWNER("messages.break-chest-owner", "You can't break a build chest that isn't yours!"),

    /** Message when owner breaks their build chest */
    BREAK_CHEST("messages.break-chest", "Build chest broken! Cancelling task and refunding items..."),

    /** Message when trying to interact with someone else's build chest */
    INTERACT_CHEST("messages.interact-chest", "You can't interact with this build chest!"),

    /** Message when trying to rename schematic items in anvil */
    ANVIL("messages.anvil", "You can't rename schematic blocks in anvils!"),

    /** Message when materials are needed */
    MATERIALS("messages.materials", "Not enough materials! Add items to the chest to continue building."),

    /** Message when WorldEdit clipboard is empty */
    EMPTY_CLIPBOARD("messages.empty-clipboard", "Your WorldEdit clipboard is empty!");

    /** The language configuration file */
    private static FileConfiguration langFile;

    /** The path in the configuration file */
    private final String path;

    /** The default message value */
    private final String defaultValue;

    /**
     * Creates a new Lang constant.
     *
     * @param path The configuration path (e.g., "messages.no-place")
     * @param defaultValue The default message if not configured
     */
    Lang(String path, String defaultValue) {
        this.path = path;
        this.defaultValue = defaultValue;
    }

    /**
     * Sets the language configuration file used for message lookup.
     * This should be called during plugin initialization.
     *
     * @param file The FileConfiguration containing localized messages
     */
    public static void setFile(FileConfiguration file) {
        langFile = file;
    }

    /**
     * Gets the default message value for this constant.
     *
     * @return The default message string
     */
    public String getDefault() {
        return defaultValue;
    }

    /**
     * Gets the configuration path for this message.
     *
     * @return The path in the language file
     */
    public String getPath() {
        return path;
    }

    /**
     * Gets the localized message from the language file.
     * Falls back to the default value if not configured or if the
     * language file has not been set.
     *
     * @return The localized message string
     */
    @Override
    public String toString() {
        if (langFile == null) {
            return defaultValue;
        }
        return langFile.getString(path, defaultValue);
    }
}
