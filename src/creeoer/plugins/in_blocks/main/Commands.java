package creeoer.plugins.in_blocks.main;

import creeoer.plugins.in_blocks.builders.BlockBuilder;
import creeoer.plugins.in_blocks.objects.Lang;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles all commands for the iN_Blocks plugin.
 * Implements both command execution and tab completion.
 *
 * <p>Available commands:
 * <ul>
 *   <li>/in create [name] - Create a schematic from WorldEdit selection</li>
 *   <li>/in delete [name] - Delete an existing schematic</li>
 *   <li>/in give [player] [name] [amount] - Give schematic item to player</li>
 *   <li>/in list - List all available schematics</li>
 *   <li>/in reload - Reload plugin configuration</li>
 * </ul>
 * </p>
 *
 * @author Creeoer
 * @version 2.0.0
 */
public class Commands implements CommandExecutor, TabCompleter {

    private static final List<String> SUBCOMMANDS = Arrays.asList("create", "delete", "give", "list", "reload");

    private final iN_Blocks plugin;
    private final SchematicManager schematicManager;

    /**
     * Creates a new Commands handler.
     *
     * @param plugin The main plugin instance
     */
    public Commands(iN_Blocks plugin) {
        this.plugin = plugin;
        this.schematicManager = plugin.getSchematicManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("in")) {
            return false;
        }

        // Show usage if no arguments
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload":
                return handleReload(sender);
            case "list":
                return handleList(sender);
            case "create":
                return handleCreate(sender, args);
            case "delete":
                return handleDelete(sender, args);
            case "give":
                return handleGive(sender, args);
            default:
                sender.sendMessage(ChatColor.RED + Lang.COMMANDS.toString());
                return true;
        }
    }

    /**
     * Handles the reload subcommand.
     *
     * @param sender The command sender
     * @return true if command was handled
     */
    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("in.reload")) {
            sender.sendMessage(ChatColor.RED + Lang.NO_PERM.toString());
            return true;
        }

        plugin.reloadConfig();
        schematicManager.reload();
        sender.sendMessage(ChatColor.GREEN + "Configuration reloaded successfully!");
        return true;
    }

    /**
     * Handles the list subcommand.
     *
     * @param sender The command sender
     * @return true if command was handled
     */
    private boolean handleList(CommandSender sender) {
        if (!sender.hasPermission("in.list")) {
            sender.sendMessage(ChatColor.RED + Lang.NO_PERM.toString());
            return true;
        }

        YamlConfiguration schematicFile = schematicManager.getSchematicsFile();
        ConfigurationSection section = schematicFile.getConfigurationSection("Schematics");

        if (section == null || section.getKeys(false).isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "No schematics available.");
            return true;
        }

        sender.sendMessage(ChatColor.AQUA + "=== Available Schematics ===");
        for (String schematic : section.getKeys(false)) {
            String direction = section.getString(schematic, "NORTH");
            sender.sendMessage(ChatColor.YELLOW + "- " + schematic + ChatColor.GRAY + " (facing: " + direction + ")");
        }

        return true;
    }

    /**
     * Handles the create subcommand.
     *
     * @param sender The command sender
     * @param args The command arguments
     * @return true if command was handled
     */
    private boolean handleCreate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + Lang.CONSOLE.toString());
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("in.create")) {
            player.sendMessage(ChatColor.RED + Lang.NO_PERM.toString());
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + Lang.SYNTAX.toString());
            player.sendMessage(ChatColor.AQUA + "Usage: /in create <name>");
            return true;
        }

        String schematicName = args[1];

        // Validate name
        if (!isValidSchematicName(schematicName)) {
            player.sendMessage(ChatColor.RED + "Invalid schematic name! Use only letters, numbers, and underscores.");
            return true;
        }

        if (schematicManager.doesExist(schematicName)) {
            player.sendMessage(ChatColor.RED + Lang.ALREADY_EXISTS.toString());
            return true;
        }

        // Get player's facing direction
        String direction = PUtils.getCardinalDirection(player.getLocation().getYaw(), false).toString();

        // Create the schematic
        if (schematicManager.createSchematic(player, schematicName, direction)) {
            String message = Lang.CREATE.toString()
                    .replace("%s", schematicName)
                    .replace("%d", direction);
            player.sendMessage(ChatColor.AQUA + message);
        }

        return true;
    }

    /**
     * Handles the delete subcommand.
     *
     * @param sender The command sender
     * @param args The command arguments
     * @return true if command was handled
     */
    private boolean handleDelete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("in.remove")) {
            sender.sendMessage(ChatColor.RED + Lang.NO_PERM.toString());
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + Lang.SYNTAX.toString());
            sender.sendMessage(ChatColor.AQUA + "Usage: /in delete <name>");
            return true;
        }

        String schematicName = args[1];

        if (!schematicManager.doesExist(schematicName)) {
            sender.sendMessage(ChatColor.RED + Lang.EXISTS.toString());
            return true;
        }

        if (schematicManager.deleteSchematic(schematicName)) {
            String message = Lang.DELETE.toString().replace("%s", schematicName);
            sender.sendMessage(ChatColor.AQUA + message);
        } else {
            sender.sendMessage(ChatColor.RED + "Failed to delete schematic!");
        }

        return true;
    }

    /**
     * Handles the give subcommand.
     *
     * @param sender The command sender
     * @param args The command arguments
     * @return true if command was handled
     */
    private boolean handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("in.give")) {
            sender.sendMessage(ChatColor.RED + Lang.NO_PERM.toString());
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + Lang.SYNTAX.toString());
            sender.sendMessage(ChatColor.AQUA + "Usage: /in give <player> <schematic> [amount]");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player '" + args[1] + "' not found!");
            return true;
        }

        String schematicName = args[2];
        if (!schematicManager.doesExist(schematicName)) {
            sender.sendMessage(ChatColor.RED + Lang.EXISTS.toString());
            return true;
        }

        int amount = 1;
        if (args.length >= 4) {
            try {
                amount = Integer.parseInt(args[3]);
                if (amount < 1 || amount > 64) {
                    sender.sendMessage(ChatColor.RED + "Amount must be between 1 and 64!");
                    return true;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid amount: " + args[3]);
                return true;
            }
        }

        BlockBuilder builder = new BlockBuilder(schematicName, plugin);
        builder.setAmount(amount);
        target.getInventory().addItem(builder.build());
        target.updateInventory();

        sender.sendMessage(ChatColor.GREEN + "Gave " + amount + "x " + schematicName + " to " + target.getName());
        target.sendMessage(ChatColor.GREEN + "You received " + amount + "x " + schematicName + " schematic!");

        return true;
    }

    /**
     * Sends usage information to the command sender.
     *
     * @param sender The command sender
     */
    private void sendUsage(CommandSender sender) {
        sender.sendMessage(ChatColor.AQUA + "=== iN_Blocks Commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/in create <name>" + ChatColor.GRAY + " - Create schematic from selection");
        sender.sendMessage(ChatColor.YELLOW + "/in delete <name>" + ChatColor.GRAY + " - Delete a schematic");
        sender.sendMessage(ChatColor.YELLOW + "/in give <player> <name> [amount]" + ChatColor.GRAY + " - Give schematic item");
        sender.sendMessage(ChatColor.YELLOW + "/in list" + ChatColor.GRAY + " - List all schematics");
        sender.sendMessage(ChatColor.YELLOW + "/in reload" + ChatColor.GRAY + " - Reload configuration");
    }

    /**
     * Validates a schematic name.
     *
     * @param name The name to validate
     * @return true if the name is valid
     */
    private boolean isValidSchematicName(String name) {
        return name != null && name.matches("^[a-zA-Z0-9_-]+$");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("in")) {
            return null;
        }

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Complete subcommands
            String partial = args[0].toLowerCase();
            completions.addAll(SUBCOMMANDS.stream()
                    .filter(s -> s.startsWith(partial))
                    .collect(Collectors.toList()));
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            String partial = args[1].toLowerCase();

            switch (subCommand) {
                case "delete":
                case "give":
                    // Complete with schematic names
                    completions.addAll(schematicManager.getSchematicNames().stream()
                            .filter(s -> s.toLowerCase().startsWith(partial))
                            .collect(Collectors.toList()));
                    break;
                case "create":
                    // Suggest a name
                    if (partial.isEmpty()) {
                        completions.add("<name>");
                    }
                    break;
            }

            // For give command, also suggest players
            if (subCommand.equals("give") && args.length == 2) {
                completions.clear();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().toLowerCase().startsWith(partial)) {
                        completions.add(player.getName());
                    }
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            // Complete schematic names for give command
            String partial = args[2].toLowerCase();
            completions.addAll(schematicManager.getSchematicNames().stream()
                    .filter(s -> s.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList()));
        } else if (args.length == 4 && args[0].equalsIgnoreCase("give")) {
            // Suggest amounts
            completions.addAll(Arrays.asList("1", "8", "16", "32", "64"));
        }

        return completions;
    }
}
