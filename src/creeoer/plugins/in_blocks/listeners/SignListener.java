package creeoer.plugins.in_blocks.listeners;

import creeoer.plugins.in_blocks.main.SchematicManager;
import creeoer.plugins.in_blocks.main.iN_Blocks;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;


public class SignListener implements Listener {
    iN_Blocks main;
    SchematicManager manager;

    public SignListener(iN_Blocks main){
        this.main = main;


       manager =  this.main.getSchematicManager();
    }
@EventHandler
    public void onSignDefine(SignChangeEvent e){

        if(e.getLine(0).equalsIgnoreCase(ChatColor.stripColor(main.getConfig().getString("Options.sign-name")))){
            Player p = e.getPlayer();

            if(!p.hasPermission("in.sign.define")){
                p.sendMessage(ChatColor.RED + "You don't have permission to define this sign!");
                return;
            }
            String[] lines = e.getLines();

  //          if(lines.length != 3){
       //         p.sendMessage(ChatColor.RED + "Invalid sign syntax!");
         //       return;
    //        }

            double amount = 0;
            try {
                amount = Integer.parseInt(lines[1]);
            } catch (NumberFormatException ignored){
                p.sendMessage(ChatColor.RED + "Invalid sign syntax!");
                return;
            }

            if(manager.getSchematic(lines[2]) == null){
                p.sendMessage(ChatColor.RED + "Can not find schematic of this name!");
                return;
            }

            //We're assuming if they got this far the sign is in fact perfectly valid



            e.setLine(0, main.getConfig().getString("Options.sign-name"));

        }
    }

@EventHandler
    public void onSignClick(PlayerInteractEvent e) {
        if(e.getAction() == Action.RIGHT_CLICK_BLOCK){

            BlockState state = e.getClickedBlock().getState();

            if(state instanceof Sign){
                Sign sign = (Sign) state;
                Player p = e.getPlayer();
                if(sign.getLine(0).equals(main.getConfig().getString("Options.sign-name"))){

                    if(!p.hasPermission("in.sign.use")) {
                        p.sendMessage(ChatColor.RED + "You don't have permission to use this!");
                        return;
                    }

                    double amount = Integer.parseInt(sign.getLine(1));
                    EconomyResponse trans = main.getEcon().withdrawPlayer(p.getName(), amount);

                    if(!trans.transactionSuccess()){
                        p.sendMessage(ChatColor.RED + "You don't have enough funds to afford this!");
                        return;
                    }

                    Material mat = Material.getMaterial(main.getConfig().getString("Options.material"));

                    ItemStack stack = new ItemStack(mat);
                    ItemMeta meta = stack.getItemMeta();


                    if(!main.getConfig().getBoolean("Options.use-lore")){
                        meta.setDisplayName(ChatColor.YELLOW+sign.getLine(2)+" schematic");
                    } else{
                        List<String> lore = new ArrayList<>();
                        lore.add(ChatColor.YELLOW + sign.getLine(2) + " schematic");
                        meta.setLore(lore);
                    }

                    stack.setItemMeta(meta);
                    p.getInventory().addItem(stack);
                    p.updateInventory();
                    p.sendMessage(ChatColor.AQUA + "Successfully purchased schematic block!");
                }





            }

        }
    }

}
