package creeoer.plugins.in_blocks.objects;

import org.bukkit.Material;


public enum IgnoredMaterial {


    AIR(Material.AIR), GRASS (Material.LONG_GRASS), TALL_GRASS(Material.DOUBLE_PLANT), LAVA(Material.STATIONARY_LAVA), GRASS_BLOCK(Material.GRASS), ;



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

    public static boolean isIgnoredMaterial(Material material){
        for(IgnoredMaterial ignoredMaterials: values()) {
            if(material.equals(ignoredMaterials.getMaterial())) {
                return true;
            }
        }
        return false;

    }
}
