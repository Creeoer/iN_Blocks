package creeoer.plugins.in_blocks.listeners;

import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.world.DataException;
import creeoer.plugins.in_blocks.main.ISchematic;
import creeoer.plugins.in_blocks.main.RegionManager;
import creeoer.plugins.in_blocks.main.SchematicManager;
import creeoer.plugins.in_blocks.main.iN_Blocks;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SListener implements Listener {

    private static List<String> previews = Collections.synchronizedList(new ArrayList<String>());

    private SchematicManager manager;
    private ISchematic sch;
    private iN_Blocks main;
    private BukkitTask task;
    private ItemStack stack;
    private Location pLoc;
    private FileConfiguration config;
    private RegionManager rgManager;

    public SListener(iN_Blocks instance){
        main = instance;
        manager = main.getSchematicManager();
        config = YamlConfiguration.loadConfiguration(new File(main.getDataFolder() + File.separator + "config.yml"));
        rgManager = main.getrManager();
    }



    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) throws DataException, IOException, MaxChangedBlocksException, IllegalAccessException, NoSuchFieldException{
        if(e.getItemInHand().hasItemMeta()) {
            ItemStack stack = e.getItemInHand();
            ItemMeta meta = stack.getItemMeta();
            final Player p = e.getPlayer();
            if (meta.hasDisplayName()) {
                String dName = meta.getDisplayName().trim();
                if (dName.charAt(0) == ChatColor.COLOR_CHAR && dName.contains("schematic")) {

                    e.setCancelled(true);

                    if (previews.contains(p.getName())) {
                        p.sendMessage(ChatColor.RED + "Can't place another schematic while in preview mode!");
                        return;
                    }

                    String[] split = meta.getDisplayName().split("\\s+");
                    sch = manager.getSchematic(ChatColor.stripColor(split[0]));

                    if(sch == null){
                        p.sendMessage(ChatColor.RED + "What is this! Apparently the schematic you are trying to place does not exist!");
                        return;
                    }


                    this.stack = stack;


                    if (manager.hasPermission(ChatColor.stripColor(split[0])) && !p.hasPermission("in." + ChatColor.stripColor(split[0]))){
                        p.sendMessage(ChatColor.RED + "You don't have permission to place this type of schematic!");
                        return;
                    }

                    try {
                        if (!rgManager.canPlayerPlace(p, sch)) {
                            p.sendMessage(ChatColor.RED + "Due to land restrictions, you can not place your schematic here");
                            return;
                        }
                    } catch (Exception ignored) {
                        return;
                    }

                    task = new BukkitRunnable() {

                        public void run() {

                            if (previews.contains(p.getName())) {

                                previews.remove(p.getName());
                                p.sendMessage(ChatColor.RED + "You took too long and cancelled placement");
                                sch.unloadPreview(p);
                                cancel();
                            }
                            cancel();
                        }

                    }.runTaskLater(main, config.getInt("Options.preview-time") * 20);

                    previews.add(p.getName());
                    sch.preview(p);
                    pLoc = p.getLocation();
                    p.sendMessage(ChatColor.AQUA + "A rough preview of the schematic is being shown, type yes to place it, type anything else to cancel. You have " + config.getInt("Options.preview-time") + " seconds." );
                    e.setCancelled(true);

                }

            }
        }

    }


    @EventHandler
    public void onChat(final AsyncPlayerChatEvent e){
        if(previews.contains(e.getPlayer().getName())){
            final Player p = e.getPlayer();
            final String message = e.getMessage();
            e.setCancelled(true);
            new BukkitRunnable(){
                public void run(){
                    if(message.equalsIgnoreCase("yes")) {
                        previews.remove(p.getName());
                        sch.unloadPreview(p);
                        task.cancel();
                        try {
                            sch.paste(pLoc, p);
                        } catch (Exception ignored) {
                        }
                        p.sendMessage(ChatColor.YELLOW + "Unloading preview...schematic successfully placed!");


                        if (stack.getAmount() > 1) {
                            ItemMeta meta = stack.getItemMeta();
                            meta.setDisplayName(ChatColor.YELLOW + sch.getName() + " schematic");
                            stack.setItemMeta(meta);
                            stack.setAmount(stack.getAmount() - 1);
                            p.setItemInHand(stack);
                            p.updateInventory();
                        } else {
                            p.getInventory().removeItem(stack);
                        }


                        cancel();
                        return;
                    }
                    previews.remove(p.getName());
                    task.cancel();
                    p.sendMessage(ChatColor.RED + "Schematic placement cancelled");
                    sch.unloadPreview(p);

                    cancel();
                }
            }.runTask(main);
        }
    }


    @EventHandler
    public void antiAnvil(InventoryClickEvent e){
        if(e.getClickedInventory() instanceof AnvilInventory){
            InventoryView view = e.getView();
            for (int i = 0; i < 3; i ++){
                if(view.getItem(i) != null){
                    ItemStack item = view.getItem(i);
                    if(item.hasItemMeta() && item.getItemMeta().hasDisplayName()){
                        if(item.getItemMeta().getDisplayName().contains("schematic")){
                            e.getWhoClicked().sendMessage(ChatColor.RED + "You can't use schematic blocks in anvils ):<");
                            e.setCancelled(true);
                        }
                    }
                }

            }
        }

    }
}
