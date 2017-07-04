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
public class BlockBuilder {
    //Will be used in future to build items using factory

    private ItemStack stack;
    private ItemMeta meta;

    public BlockBuilder(String blockName, iN_Blocks main) {

        this.stack = new ItemStack(Material.CHEST);
        ItemMeta meta = stack.getItemMeta();

        List<String> lore = new ArrayList<>();
        if (!main.getConfig().getBoolean("Options.use-lore")) {
            meta.setDisplayName(ChatColor.YELLOW + blockName + " schematic");
        } else {
            lore.add(ChatColor.YELLOW + blockName + " schematic");
        }

        meta.setLore(lore);
        this.meta = meta;

    }


    public ItemStack build() {
        stack.setItemMeta(this.meta);
        return stack;
    }


    public BlockBuilder setRequirements(List<String> requirements) {
        List<String> lore = meta.getLore() == null ? new ArrayList<>() : meta.getLore();

        int num = 0;
        String holder = requirements.get(0);


        for (String material : requirements) {

            if (material.equalsIgnoreCase("WALL_SIGN"))
                material = "SIGN";

            if(material.contains("DOOR"))
                material = "DOOR";

            if(material.equalsIgnoreCase("LONG_GRASS"))
                continue;

            if (material.equalsIgnoreCase(holder)) {
                num++;
            } else {
                lore.add(lore.size(), ChatColor.GOLD + Integer.toString(num) + " " + holder.toUpperCase());
                num = 1;
                holder = material;
            }
        }
        if (num > 0)
            lore.add(lore.size(), ChatColor.GOLD + Integer.toString(num) + " " + holder.toUpperCase());

        meta.setLore(lore);

        return this;
    }


    public BlockBuilder setAmount(int amount) {
        stack.setAmount(amount);
        return this;
    }


}
