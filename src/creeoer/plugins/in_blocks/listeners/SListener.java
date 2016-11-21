package creeoer.plugins.in_blocks.listeners;

import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.world.DataException;
import creeoer.plugins.in_blocks.events.iNPlaceEvent;
import creeoer.plugins.in_blocks.main.ISchematic;
import creeoer.plugins.in_blocks.main.RegionManager;
import creeoer.plugins.in_blocks.main.SchematicManager;
import creeoer.plugins.in_blocks.main.iN_Blocks;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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
    private ItemStack stack;
    private boolean isDone;
    private Block block;
    private String name;
    private Location pLoc;
    private FileConfiguration config;
    private RegionManager rgManager;

    public SListener(iN_Blocks instance){
        main = instance;
        manager = main.getSchematicManager();
        config = YamlConfiguration.loadConfiguration(new File(main.getDataFolder() + File.separator + "config.yml"));
        rgManager = main.getrManager();
        isDone = false;
    }



    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) throws DataException, IOException, MaxChangedBlocksException, IllegalAccessException, NoSuchFieldException{
        if(e.getItemInHand().hasItemMeta()) {
            ItemStack stack = e.getItemInHand();
            ItemMeta meta = stack.getItemMeta();
            final Player p = e.getPlayer();
            if (meta.hasDisplayName() || meta.hasLore()) {
                if ( meta.hasLore() && meta.getLore().get(0).contains("schematic") || meta.hasDisplayName() &&  meta.getDisplayName().charAt(0) == ChatColor.COLOR_CHAR && meta.getDisplayName().contains("schematic")) {
                   Location l = e.getBlock().getLocation();
                    e.setCancelled(true);

                    if (p.isConversing()) {
                        p.sendRawMessage(ChatColor.RED + "Can't place another schematic while in preview mode!");
                        return;
                    }

                    String[] split;


                    if(!main.getConfig().getBoolean("Options.use-lore")){
                         split = meta.getDisplayName().split("\\s+");
                    } else{
                         split = meta.getLore().get(0).split("\\s+");
                    }

                    name = ChatColor.stripColor(split[0]);
                    sch = manager.getSchematic(name);

                    if(sch == null){
                        p.sendMessage(ChatColor.RED + "What is this! Apparently the schematic you are trying to place does not exist!");
                        return;
                    }

                    this.stack = stack;
                    this.block = e.getBlockPlaced();

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


                    sch.preview(p, l);

                    pLoc = l;
                    initConvo(p).begin();
                    e.setCancelled(true);

                }

            }
        }

    }





    public Conversation initConvo(final Player p){
        ConversationFactory fac = new ConversationFactory(main).withFirstPrompt(new ValidatingPrompt() {
            @Override
            public String getPromptText(ConversationContext conversationContext){
                return ChatColor.GREEN + "Showing a rough preview of the schematic,type yes to place or anything else to cancel. You have " + config.getInt("Options.preview-time") + " seconds.";
            }

            @Override
            protected boolean isInputValid(ConversationContext conversationContext,String s){
                return true;
            }

            @Override
            protected Prompt acceptValidatedInput(ConversationContext conversationContext,String s){
                if(!s.equals("yes") && !s.equals("rotate")){
                    sch.unloadPreview(p,pLoc);
                    isDone = true;
                    p.sendRawMessage(ChatColor.RED+"Placement cancelled!");
                    return Prompt.END_OF_CONVERSATION;
                }

                isDone = true;
                iNPlaceEvent e = new iNPlaceEvent(name, p, pLoc, block, sch);
                Bukkit.getServer().getPluginManager().callEvent(e);
                sch.unloadPreview(p,pLoc);

               if(e.isCancelled()){
                   p.sendRawMessage(ChatColor.RED + "Block placement was cancelled!!");
                   return Prompt.END_OF_CONVERSATION;
               }

                try{
                    sch.paste(pLoc,p);
                }catch(Exception ignored){
                }
                p.sendRawMessage(ChatColor.YELLOW+"Unloading preview...schematic successfully placed!");
                if(stack.getAmount()>1){
                    ItemMeta meta=stack.getItemMeta();
                    meta.setDisplayName(ChatColor.YELLOW+sch.getName()+" schematic");
                    stack.setItemMeta(meta);
                    stack.setAmount(stack.getAmount()-1);
                    p.setItemInHand(stack);
                    p.updateInventory();
                }else{
                    p.getInventory().removeItem(stack);
                }

                return Prompt.END_OF_CONVERSATION;

            }
        }).withTimeout(config.getInt("Options.preview-time")).withModality(true).addConversationAbandonedListener(new ConversationAbandonedListener(){
            @Override
            public void conversationAbandoned(ConversationAbandonedEvent conversationAbandonedEvent) {
                if(!isDone){
                    sch.unloadPreview(p,pLoc);
                    p.sendRawMessage(ChatColor.RED+"Preview time out!");
                }
            }
        });
        return fac.buildConversation(p);
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
