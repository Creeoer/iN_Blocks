package creeoer.plugins.in_blocks.objects;

import org.bukkit.Material;

/**
 * Created by Frank on 7/6/2017.
 */
public enum IgnoredMaterial {


    AIR(Material.AIR), GRASS (Material.LONG_GRASS), TALLGRASS(Material.DOUBLE_PLANT);



    private Material mat;


    IgnoredMaterial(Material material){
        this.mat = material;
    }

    public String toString(){
        return mat.toString();
    }


    public Material getMaterial(){
        return mat;
    }
}
