package creeoer.plugins.in_blocks.objects;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Created by CREEOER on 11/26/2016.
 */
public enum Lang{

    BAD_PLACE("messages.no-place", "Can't place build block here!"),
    SUCCESS_PLACE("messages.place", "Unloading preview....schematic placed successfully!"),
    NO_PERM("messages.no-perm", "You have no permission for this!!1!"),
    INVALID_SIGN("messages.invalid-sign", "Incorrect sign syntax"),
    VALID_SIGN("messages.valid-sign", "Shop sign successfully placed!"),
    COMPLETE("messages.building-completed", "Building completed successfully!"),
    BUY("messages.buy", "Build block bought successfully!"),
    AFFORD("messages.afford", "Can't afford build block!"),
    SYNTAX("messages.syntax-error", "Incorrect syntax!"),
    COMMANDS("messages.commands", "Commands: /in [give/create/delete]"),
    CREATE("messages.create", "The schematic of %s was successfully created!"),
    EXISTS("messages.exists", "This schematic does not exists!"),
    ALREADY_EXISTS("messages.already-exists", "This schematic already exists!"),
    DELETE("messages.delete", "The schematic of %s has been successfully deleted!"),
    REGISTER("messages.register", "Registered schematic with direction of: "),
    CONSOLE("messages.console", "You can't do this command as the server!"),
    PREVIEW("messages.preview", "Now in preview mode! Type yes in %n seconds to place or anything else to cancel!"),
    PREVIEW_ERROR("messages.preview-error", "Can't place another build block while in preview mode!"),
    CANCEL("messages.build-cancel", "Placement cancelled!"),
    ANVIL("messages.anvil", "You can't use schematic blocks in anvils! ):<"),
    MATERIALS("messages.materials", "Not enough materials to complete! You have 3 minutes to provide the chest with material"),;






    private static FileConfiguration LangFile;
    private  String path, def;

     Lang(String path, String def) {
        this.path = path;
         this.def = def;
    }



    public static void setFile(FileConfiguration file) {
        LangFile = file;
    }


    public String getDefault(){
        return def;
    }

    public String getPath(){
        return path;
    }

    public String toString(){
        return LangFile.getString(this.path, def);
    }

}
