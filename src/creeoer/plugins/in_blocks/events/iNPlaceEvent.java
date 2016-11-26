package creeoer.plugins.in_blocks.events;

import creeoer.plugins.in_blocks.main.ISchematic;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;


public class iNPlaceEvent extends Event implements Cancellable{

    private static final HandlerList handlers = new HandlerList();
    private String name;
    private Block block;
    private boolean cancelled;
    private Player placer;
    private Location loc;
    private ISchematic schematic;

    public iNPlaceEvent(String n, Player p, Location l, Block b, ISchematic sch){
        name = n;
        placer = p;
        loc = l;
        cancelled = false;
        block = b;
        schematic = sch;
    }

    @Override
    public HandlerList getHandlers(){
        return handlers;
    }

    public static HandlerList getHandlerList(){
        return handlers;
    }

    public Block getBlock(){
        return block;
    }

    public String getName(){
        return name;
    }

    public ISchematic getSchematic() {
        return schematic;
    }

    public Player getPlacer(){
       return placer;
    }

    public Location getLocation(){
       return loc;
    }

    @Override
    public boolean isCancelled(){
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b){
          cancelled = b;
    }
}
