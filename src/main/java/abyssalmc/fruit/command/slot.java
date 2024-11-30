package abyssalmc.fruit.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static abyssalmc.fruit.command.metronome.isInteger;
import static abyssalmc.fruit.Fruit.hotkeyIndex;

public class slot {

    public static String hotkeystring;
    public static boolean validargs = true;
    public static boolean autohotkeyenabled = false;
    public static int[] slotArray;
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("switchslot").executes(slot::switchslot));

        dispatcher.register(CommandManager.literal("autohotkey").then(CommandManager.literal("set").then(CommandManager.argument("hotkeys", StringArgumentType.string()).executes(slot::autoHotkey))));
        dispatcher.register(CommandManager.literal("autohotkey").then(CommandManager.literal("stop").executes(slot::stopHotkey)));
    }

    private static int switchslot(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity p = context.getSource().getPlayer();
        p.getInventory().selectedSlot = 2;

        p.networkHandler.sendPacket(new UpdateSelectedSlotS2CPacket(2));

        return 1;
    }

    private static int autoHotkey(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        hotkeystring = StringArgumentType.getString(context, "hotkeys");
        String[] stringArray = hotkeystring.split("-");
        ServerPlayerEntity p = context.getSource().getPlayer();

        validargs = true;
        for (int i = 0; i < stringArray.length; i++) {
            if (!isInteger(stringArray[i])){validargs=false;
            }else{
                if (Integer.parseInt(stringArray[i]) >= 10){
                    validargs = false;
                }
                if (Integer.parseInt(stringArray[i]) == 0){
                    validargs = false;
                }
            }
        }
        if (validargs){
            slotArray = new int[stringArray.length];
            for (int i = 0; i < stringArray.length; i++) {
                slotArray[i] = Integer.parseInt(stringArray[i]);
            }
            autohotkeyenabled = true;
            hotkeyIndex = 0;
            p.sendMessage(Text.literal("§aAutomatic hotkeys set to " + hotkeystring + ". Place a block to begin!"));
        } else{
            p.sendMessage(Text.literal("§cInvalid args. \"/autohotkey set 1-3\" alternates between the first and third slots."));
        }
        return 1;
    }
    private static int stopHotkey(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        autohotkeyenabled = false;
        return 1;
    }
}
