package creeoer.plugins.in_blocks.listeners;

import com.sk89q.worldedit.world.DataException;
import creeoer.plugins.in_blocks.builders.BlockBuilder;
import creeoer.plugins.in_blocks.main.ISchematic;
import creeoer.plugins.in_blocks.main.SchematicManager;
import creeoer.plugins.in_blocks.main.iN_Blocks;
import creeoer.plugins.in_blocks.objects.Lang;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.io.IOException;


public class SignListener implements Listener {
    iN_Blocks main;
    SchematicManager manager;

    public SignListener(iN_Blocks main) {
        this.main = main;


        manager = this.main.getSchematicManager();
    }

    @EventHandler
    public void onSignDefine(SignChangeEvent e) {

        if (e.getLine(0).equalsIgnoreCase(ChatColor.stripColor(main.getConfig().getString("Options.sign-name")))) {
            Player p = e.getPlayer();

            if (!p.hasPermission("in.sign.define")) {
                p.sendMessage(ChatColor.RED + Lang.NO_PERM.toString());
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
            } catch (NumberFormatException ignored) {
                p.sendMessage(ChatColor.RED + Lang.INVALID_SIGN.toString());
                return;
            }

            if (manager.getSchematic(lines[2]) == null) {
                p.sendMessage(ChatColor.RED + Lang.EXISTS.toString());
                return;
            }

            //We're assuming if they got this far the sign is in fact perfectly valid


            p.sendMessage(ChatColor.GREEN + Lang.VALID_SIGN.toString());
            e.setLine(0, main.getConfig().getString("Options.sign-name"));

        }
    }

    @EventHandler
    public void onSignClick(PlayerInteractEvent e) throws DataException, IOException {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            BlockState state = e.getClickedBlock().getState();
            if (state instanceof Sign) {
                Sign sign = (Sign) state;
                Player p = e.getPlayer();
                if (sign.getLine(0).equals(main.getConfig().getString("Options.sign-name"))) {

                    if (!p.hasPermission("in.sign.use")) {
                        p.sendMessage(ChatColor.RED + Lang.NO_PERM.toString());
                        return;
                    }

                    double amount = Integer.parseInt(sign.getLine(1));
                    EconomyResponse trans = main.getEcon().withdrawPlayer(p.getName(), amount);

                    if (!trans.transactionSuccess()) {
                        p.sendMessage(ChatColor.RED + Lang.AFFORD.toString());
                        return;
                    }
                    ISchematic schematic = new ISchematic(sign.getLine(2), main);

                    BlockBuilder builder = new BlockBuilder(schematic.getName(), main);

                    if (main.getConfig().getBoolean("Options.survival-mode"))
                        builder.setRequirements(schematic.getBlockRequirements());

                    builder.setAmount(1);
                    p.getInventory().addItem(builder.build());
                    p.updateInventory();
                    p.sendMessage(ChatColor.AQUA + Lang.BUY.toString());
                }


            }

        }
    }

}
