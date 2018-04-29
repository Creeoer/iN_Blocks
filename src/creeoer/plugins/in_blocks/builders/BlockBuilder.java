package creeoer.plugins.in_blocks.builders;

import creeoer.plugins.in_blocks.main.BuildSchematic;
import creeoer.plugins.in_blocks.main.iN_Blocks;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;


public class BlockBuilder {


    private ItemStack stack;
    private ItemMeta meta;
    private BuildSchematic buildSchematic;

    public BlockBuilder(String blockName, iN_Blocks main) {

        this.stack = new ItemStack(Material.CHEST);
        ItemMeta meta = stack.getItemMeta();
        buildSchematic = new BuildSchematic(blockName, main);

        List<String> lore = new ArrayList<>();

        if (!main.getConfig().getBoolean("Options.use-lore")) {
            meta.setDisplayName(ChatColor.YELLOW + blockName + " schematic");
        } else {
            lore.add(ChatColor.YELLOW + blockName + " schematic");
        }
        meta.setLore(lore);
        this.meta = meta;


        if(main.getConfig().getBoolean("Options.survival-mode"))
            setRequirements();



    }


    public ItemStack build() {
        stack.setItemMeta(this.meta);
        return stack;
    }


    private BlockBuilder setRequirements() {
        List<String> lore = new ArrayList<>();
        List<String> requirements = buildSchematic.getBlockRequirements();

        int num = 0;
        String holder = requirements.get(0);

        for (String material : requirements) {
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

        if(meta == null)
            Bukkit.getLogger().info("Meta is null!");

        meta.setLore(lore);

        return this;
    }


    public BlockBuilder setAmount(int amount) {
        stack.setAmount(amount);
        return this;
    }


}
