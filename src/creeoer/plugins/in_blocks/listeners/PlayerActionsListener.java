package creeoer.plugins.in_blocks.listeners;

import com.sk89q.worldedit.event.platform.BlockInteractEvent;
import com.sk89q.worldedit.world.DataException;
import creeoer.plugins.in_blocks.builders.BlockBuilder;
import creeoer.plugins.in_blocks.main.iN_Blocks;
import creeoer.plugins.in_blocks.objects.BuildManager;
import creeoer.plugins.in_blocks.objects.BuildTask;
import creeoer.plugins.in_blocks.objects.IgnoredMaterial;
import creeoer.plugins.in_blocks.objects.Lang;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.util.List;

/**
 * Created by Frank on 4/25/2018.
 */
public class PlayerActionsListener implements Listener {


    private iN_Blocks main;
    private YamlConfiguration config;
    private BuildManager buildManager;
    private Player player;

    public PlayerActionsListener(iN_Blocks mainInstance){
        main = mainInstance;
        config = (YamlConfiguration) main.getConfig();

        buildManager = main.getBuildManager();
    }


    @EventHandler
    public void onChestBreak(BlockBreakEvent e) {
        player = e.getPlayer();
        if (e.getBlock().getState() instanceof Chest) {
           Chest buildChest = (Chest) e.getBlock().getState();
            for (BuildTask task : buildManager.getAllTasks()) {
                if (buildChest.getLocation().equals(task.getBuildChest().getChestLocation())) {

                    if(!task.getOwnerName().equals(player.getName())) {
                        player.sendMessage(ChatColor.RED + Lang.BREAK_CHEST_OWNER.toString());
                        e.setCancelled(true);
                        return;
                    }

                    player.sendMessage(ChatColor.YELLOW + Lang.BREAK_CHEST.toString());
                    refundBuildChest(task);
                    dropItemsOnGround(task.getCurrentBlocksInBuild(), player.getLocation());
                    task.clearBuild();
                    buildManager.removeTask(task);
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onChestInteraction(PlayerInteractEvent e){
        if(e.getAction() == Action.RIGHT_CLICK_BLOCK){
            Player actor = e.getPlayer();
            Block block = e.getClickedBlock();
            if(block.getState() instanceof Chest){
                for(BuildTask task: buildManager.getAllTasks()){
                    if (block.getLocation().equals(task.getBuildChest().getChestLocation())) {
                        if (!task.getOwnerName().equals(actor.getName())) {
                            actor.sendMessage(ChatColor.RED + Lang.INTERACT_CHEST.toString());
                            e.setCancelled(true);
                            return;
                        }
                    }
                }
            }
        }
    }


    private void dropItemsOnGround(List<ItemStack> itemStackList, Location locationToDrop){
        for(ItemStack stack: itemStackList){
            if(stack != null && !IgnoredMaterial.isIgnoredMaterial(stack.getType())){
                player.getWorld().dropItem(locationToDrop, stack);
            }
        }
    }

    private void refundBuildChest(BuildTask task){
        BlockBuilder blockBuilder = new BlockBuilder(task.getSchematic().getName(), main);
        blockBuilder.setAmount(1);
        ItemStack buildChest = blockBuilder.build();

        player.getInventory().addItem(buildChest);
        player.updateInventory();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) throws IOException, DataException {
        if (buildManager.hasTask(e.getPlayer().getName())) {
            buildManager.startTask(e.getPlayer().getName());
            e.getPlayer().sendMessage(ChatColor.YELLOW + "Your build was resumed!");
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        for (BuildTask task : buildManager.getAllTasks()) {
            if (task.getOwnerName().equals(e.getPlayer().getName())) {
                buildManager.saveTask(task);
                break;
            }
        }

    }



    @EventHandler
    public void antiAnvil(InventoryClickEvent e) {
        if (e.getClickedInventory() instanceof AnvilInventory) {
            InventoryView view = e.getView();
            for (int i = 0; i < 3; i++) {
                if (view.getItem(i) != null) {
                    ItemStack item = view.getItem(i);
                    if (item.hasItemMeta()) {
                        if (item.getItemMeta().hasDisplayName()) {
                            if (item.getItemMeta().getDisplayName().contains("schematic")) {
                                e.getWhoClicked().sendMessage(ChatColor.RED + Lang.ANVIL.toString());
                                e.setCancelled(true);
                            }
                        } else if (item.getItemMeta().hasLore()) {
                            if (item.getItemMeta().getLore().contains("schematic")) {
                                e.getWhoClicked().sendMessage(ChatColor.RED + Lang.ANVIL.toString());
                                e.setCancelled(true);
                            }

                        }
                    }
                }

            }
        }

    }


}
