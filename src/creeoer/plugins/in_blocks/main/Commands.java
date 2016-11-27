package creeoer.plugins.in_blocks.main;

import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.DataException;
import creeoer.plugins.in_blocks.builders.BlockFactory;
import creeoer.plugins.in_blocks.objects.Lang;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Commands implements CommandExecutor {

	private SchematicManager manager = null;
    private iN_Blocks main = null;
	private static List<String> cmdList = Arrays.asList("delete", "give", "create");
	public Commands(iN_Blocks instance){
		main = instance;
		manager = main.getSchematicManager();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if(cmd.getName().equalsIgnoreCase("in")){
			if(args.length == 0){
				sender.sendMessage(ChatColor.RED + Lang.SYNTAX.toString());
				return false;
			}

		if(!cmdList.contains(args[0])){
				sender.sendMessage(ChatColor.RED + Lang.COMMANDS.toString());
		        return false;
			}
			
			if(args[0].equalsIgnoreCase("create")){
				//in create House

				if(!(sender instanceof Player)){
					sender.sendMessage(ChatColor.RED + Lang.CONSOLE.toString());
					return false;
				}
				Player p = (Player) sender;

				if(!p.hasPermission("in.create")) {
					p.sendMessage(ChatColor.RED + Lang.NO_PERM.toString());
					return false;
				}

				if (args.length != 2){
					p.sendMessage(ChatColor.RED + Lang.SYNTAX.toString());
					return false;
				}

				String name = args[1];
				String direction = PUtils.getCardinalDirection(p);
				//TODO If a player reference can't get me their dection then ima use this

				/*String direction = args[2];
				if(!direction.equalsIgnoreCase("north") || !direction.equalsIgnoreCase("west") ||
						!direction.equalsIgnoreCase("south") || !direction.equalsIgnoreCase("east")) {
					p.sendMessage(ChatColor.RED + "That is not a valid direction!");
					return false;
				}

				*/

               if (manager == null) {
				   main.getLogger().severe("Manager is null");
				   return false;
			   }

				if(manager.doesExist(name)){
					p.sendMessage(ChatColor.RED + Lang.EXISTS.toString());
					return false;
				}

				LocalSession session = WorldEdit.getInstance().getSession(p.getName());

				if(session == null){
					p.sendMessage(ChatColor.RED + "No worldedit session found!");
					return false;
				}

				LocalPlayer player = main.getWorldEdit().wrapPlayer(p);

				ClipboardHolder ch = null;

						try {
							ch = session.getClipboard();
						} catch (EmptyClipboardException e){
							p.sendMessage(ChatColor.RED + "Your clipboard is empty!");
							return false;
						}

				try {
					manager.createSchematic(player, name, direction);
				} catch (Exception e){
					e.printStackTrace();
					return false;
				}

				//If all went well..
				p.sendMessage(ChatColor.AQUA + Lang.CREATE.toString().replace("%s", name));
				p.sendMessage(ChatColor.AQUA + Lang.REGISTER.toString() + ChatColor.YELLOW + direction.toUpperCase());
				return true;


			}
			CommandSender p = sender;
			if(args[0].equalsIgnoreCase("delete")){
				if(!sender.hasPermission("in.remove")){
					p.sendMessage(ChatColor.RED + Lang.NO_PERM.toString());
					return false;
				}

				if (args.length != 2){
					p.sendMessage(ChatColor.RED + Lang.SYNTAX.toString());
					return false;
				}

				if(!manager.doesExist(args[1])){
					p.sendMessage(ChatColor.RED + Lang.EXISTS.toString());
					return false;
				}
				try {
					manager.deleteSchematic(args[1]);
					p.sendMessage(ChatColor.AQUA + Lang.DELETE.toString().replace("%s", args[1]));
				} catch (Exception ignored) {}
			}


			if (args[0].equalsIgnoreCase("give")){

				if (!sender.hasPermission("in.give")){
					p.sendMessage(ChatColor.RED + Lang.NO_PERM.toString());
					return false;
				}
//in give pName sName 30
				if (args.length < 3){
					p.sendMessage(ChatColor.RED + Lang.SYNTAX.toString());
					return false;
				}

				Player target = Bukkit.getPlayer(args[1]);

				String sName = args[2];

				if(!manager.doesExist(sName)) {
                    p.sendMessage(ChatColor.RED + Lang.AFFORD.toString());
					return false;
				}

				if(target == null){
                   p.sendMessage(ChatColor.RED + "Can't find this player in your server!");
					return false;
				}

             ISchematic schematic = null;
     try{
       schematic=new ISchematic(sName,main);
    } catch (IOException|DataException e) {e.printStackTrace(); return false;}

                BlockFactory factory = new BlockFactory(sName, main);

                if(main.getConfig().getBoolean("Options.survival-mode"))
                    factory.setRequirements(schematic.getBlockRequirements());


				if (args.length == 4){
					int amount = Integer.parseInt(args[3]);
                    factory.setAmount(amount);
			        target.getInventory().addItem(factory.build());
					target.updateInventory();
					return true;
				}

                factory.setAmount(1);
				target.getInventory().addItem(factory.build());
				target.updateInventory();

			}
		}
		
		
		
		return false;
	}

}
