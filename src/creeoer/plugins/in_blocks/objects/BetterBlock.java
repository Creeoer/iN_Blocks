package creeoer.plugins.in_blocks.objects;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;

/**
 * Created by CREEOER on 11/24/2016.
 */
public class BetterBlock extends BaseBlock{

    private Vector loc;


    //Unused class but will be used for later api implementation
    public BetterBlock(BaseBlock other, Vector loc){
        super(other);
        this.loc = loc; //I guarantee it

    }

    public Vector getLocation(){
        return loc;
    }

}
