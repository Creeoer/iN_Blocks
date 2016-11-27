package creeoer.plugins.in_blocks.builders;

import creeoer.plugins.in_blocks.main.iN_Blocks;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CREEOER on 11/25/2016.
 */
public class BlockFactory{
    //Will be used in future to build items using factory

    private ItemStack stack;
    private ItemMeta meta;
    private List<String> requirements;

    public BlockFactory(String blockName,iN_Blocks main){

        this.stack=new ItemStack(Material.getMaterial(main.getConfig().getString("Options.material")));
        ItemMeta meta=stack.getItemMeta();

        List<String> lore=new ArrayList<>();
        if(!main.getConfig().getBoolean("Options.use-lore")){
            meta.setDisplayName(ChatColor.YELLOW+blockName+" schematic");
        }else{
            lore.add(ChatColor.YELLOW+blockName+" schematic");
        }

        meta.setLore(lore);
        this.meta=meta;

    }


    public ItemStack build(){
        return stack;
    }



    public void setRequirements(List<String> requirements){
        List<String> lore = meta.getLore() == null ? new ArrayList<>() : meta.getLore();

        int num=0;
        String holder=requirements.get(0);

        for(String material : requirements){
            if(material.equalsIgnoreCase(holder)){
                num++;
            }else{
                lore.add(lore.size(),ChatColor.GOLD+Integer.toString(num)+" "+holder.toUpperCase());
                num=1;
                holder=material;
            }
        }
        if(num>0)
            lore.add(lore.size(),ChatColor.GOLD+Integer.toString(num)+" "+holder.toUpperCase());

        meta.setLore(lore);
    }



    public void setAmount(int amount){
        stack.setAmount(amount);
    }






}
