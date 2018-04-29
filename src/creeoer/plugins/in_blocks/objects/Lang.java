package creeoer.plugins.in_blocks.objects;

import org.bukkit.configuration.file.FileConfiguration;


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
    CREATE("messages.create", "The schematic of %s was successfully created with the direction of %d!"),
    EXISTS("messages.exists", "This schematic does not exists!"),
    ALREADY_EXISTS("messages.already-exists", "This schematic already exists!"),
    DELETE("messages.delete", "The schematic of %s has been successfully deleted!"),
    CONSOLE("messages.console", "You can't do this command as the server!"),
    PREVIEW("messages.preview", "Now in preview mode! Type yes in %n seconds to place or anything else to cancel!"),
    PREVIEW_ERROR("messages.preview-error", "Can't place another build block while in preview mode!"),
    CANCEL("messages.build-cancel", "Placement cancelled!"),
    BREAK_CHEST_OWNER("messages.break-chest-owner", "You can't break a build chest that isn't owned by you!"),
    BREAK_CHEST("messages.break-chest", "You've broken the build chest! Cancelling task and refunding items..."),
    INTERACT_CHEST("messages.interact-chest", "You can't interact with this build chest!"),
    ANVIL("messages.anvil", "You can't use schematic blocks in anvils! ):<"),
    MATERIALS("messages.materials", "Not enough materials to complete! You have 3 minutes to provide the chest with material"),
    EMPTY_CLIPBOARD("messages.empty-clipboard", "Your clipboard is empty!");






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
