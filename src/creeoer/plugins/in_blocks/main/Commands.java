package creeoer.plugins.in_blocks.main;

import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import creeoer.plugins.in_blocks.builders.BlockBuilder;
import creeoer.plugins.in_blocks.objects.Lang;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class Commands implements CommandExecutor {

    private SchematicManager schematicManager = null;
    private iN_Blocks main = null;
    private static List<String> cmdList = Arrays.asList("delete", "give", "create", "list", "reload");

    public Commands(iN_Blocks instance) {
        main = instance;
        schematicManager = main.getSchematicManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("in")) {


            if (args.length == 0) {
                sender.sendMessage(ChatColor.RED + Lang.SYNTAX.toString());
                return false;
            }

            if (!cmdList.contains(args[0])) {
                sender.sendMessage(ChatColor.RED + Lang.COMMANDS.toString());
                return false;
            }


            if (args[0].equalsIgnoreCase("reload")) {

                if (!sender.hasPermission("in.reload")) {
                    sender.sendMessage(ChatColor.RED + Lang.NO_PERM.toString());
                    return false;
                }

                main.reloadConfig();
                sender.sendMessage(ChatColor.GREEN + "Config successfully reloaded!");

                return true;
            }


            if (args[0].equalsIgnoreCase("list")) {

                if (!sender.hasPermission("in.list")) {
                    sender.sendMessage(ChatColor.RED + Lang.NO_PERM.toString());
                    return false;
                }

                YamlConfiguration schematicFile = schematicManager.getSchematicsFile();

                if(schematicFile.getConfigurationSection("Schematics") == null){
                    sender.sendMessage(ChatColor.RED + "No schematics to list!");
                    return true;
                }

                for (String schematics : schematicFile.getConfigurationSection("Schematics").getKeys(false)) {
                    sender.sendMessage(ChatColor.YELLOW + schematics);
                }
                return true;

            }


            if (args[0].equalsIgnoreCase("create")) {

                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + Lang.CONSOLE.toString());
                    return false;
                }

                Player p = (Player) sender;

                if (!p.hasPermission("in.create")) {
                    p.sendMessage(ChatColor.RED + Lang.NO_PERM.toString());
                    return false;
                }

                if (args.length != 2) {
                    p.sendMessage(ChatColor.RED + Lang.SYNTAX.toString());
                    p.sendMessage(ChatColor.AQUA + "Usage: /in create [buildName]");
                    return false;
                }



                String schematicName = args[1];

                if (schematicManager.doesExist(schematicName)) {
                    p.sendMessage(ChatColor.RED + Lang.ALREADY_EXISTS.toString());
                    return false;
                }

                LocalSession session = WorldEdit.getInstance().getSession(p.getName());

                if (session == null) {
                    p.sendMessage(ChatColor.RED + "No worldedit session found!");
                    return false;
                }

                LocalPlayer player = main.getWorldEdit().wrapPlayer(p);


                String direction = PUtils.getCardinalDirection(p.getLocation().getYaw(), false).toString();
                schematicManager.createSchematic(player, schematicName,direction);

                p.sendMessage(ChatColor.AQUA + Lang.CREATE.toString().replace("%s", schematicName) + " " + direction.toUpperCase());
                return true;

            }


            CommandSender p = sender;
            if (args[0].equalsIgnoreCase("delete")) {
                if (!sender.hasPermission("in.remove")) {
                    p.sendMessage(ChatColor.RED + Lang.NO_PERM.toString());
                    return false;
                }

                if (args.length != 2) {
                    p.sendMessage(ChatColor.RED + Lang.SYNTAX.toString());
                    p.sendMessage(ChatColor.AQUA + "Usage: /in delete [buildName]");
                    return false;
                }

                if (!schematicManager.doesExist(args[1])) {
                    p.sendMessage(ChatColor.RED + Lang.EXISTS.toString());
                    return false;
                }
                try {
                    schematicManager.deleteSchematic(args[1]);
                    p.sendMessage(ChatColor.AQUA + Lang.DELETE.toString().replace("%s", args[1]));
                } catch (Exception ignored) {
                }
            }


            if (args[0].equalsIgnoreCase("give")) {

                if (!sender.hasPermission("in.give")) {
                    p.sendMessage(ChatColor.RED + Lang.NO_PERM.toString());
                    return false;
                }
//in give pName schematicName 30
                if (args.length < 3) {
                    p.sendMessage(ChatColor.RED + Lang.SYNTAX.toString());
                    p.sendMessage(ChatColor.AQUA + "Usage: /in give [player] [buildName] [amount]");
                    return false;
                }

                Player target = Bukkit.getPlayer(args[1]);

                String schematicName = args[2];

                if (!schematicManager.doesExist(schematicName)) {
                    p.sendMessage(ChatColor.RED + Lang.EXISTS.toString());
                    return false;
                }

                if (target == null) {
                    p.sendMessage(ChatColor.RED + "Can't find this player in your server!");
                    return false;
                }


                BlockBuilder builder = new BlockBuilder(schematicName, main);

                if (args.length == 4) {
                    int amount = Integer.parseInt(args[3]);
                    builder.setAmount(amount);
                    target.getInventory().addItem(builder.build());
                    target.updateInventory();
                    return true;
                }

                builder.setAmount(1);
                target.getInventory().addItem(builder.build());
                target.updateInventory();

            }
        }


        return false;
    }

}
