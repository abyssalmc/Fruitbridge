package abyssalmc.fruit.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import static abyssalmc.fruit.Fruit.*;


public class metronome {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("metronome").then(CommandManager.literal("set").then(CommandManager.argument("period", StringArgumentType.string()).executes(metronome::metro))));
        dispatcher.register(CommandManager.literal("metronome").then(CommandManager.literal("stop").executes(metronome::metrostop)));
    }

    public static String period = "";
    public static int[] periodArray;

    public static boolean startMetro = false;
    public static boolean failedArgs = false;
    public static boolean intArgs = true;


    public static boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    private static int metro(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        period = StringArgumentType.getString(context, "period");
        String[] stringArray = period.split("-");

        intArgs = true;
        for (int i = 0; i < stringArray.length; i++) {
            if (!isInteger(stringArray[i])){intArgs=false;}
        }

        if (intArgs) {
            metronomeEnabled = true;

            // Create an array of integers
            periodArray = new int[stringArray.length];

            failedArgs = false;

            // Convert each string element to an integer
            for (int i = 0; i < stringArray.length; i++) {
                periodArray[i] = Integer.parseInt(stringArray[i]);
                if (periodArray[i] > 12) {
                    metronomeEnabled = false;
                }
            }
            metroClock = periodArray[0];
            PlayerEntity p = context.getSource().getPlayer();
            if (!metronomeEnabled) {
                p.sendMessage(Text.literal("§cA beat cannot last longer than 12 ticks."));
            } else {
                p.sendMessage(Text.literal("§aMetronome set to " + period + ". Place a block to begin!"));
            }
        }else{
            metronomeEnabled = false;
            PlayerEntity p = context.getSource().getPlayer();
            p.sendMessage(Text.literal("§cInvalid args. \"/metronome set 2-5\" sets an asymmetric metronome that alternates with beats of 2 and 5 ticks."));
        }



        //player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_BASEDRUM.value(), SoundCategory.MASTER, 999, 1);

        startMetro = true;
        isBridging = false;
        return 1;
    }

    private static int metrostop(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        metronomeEnabled = false;
        isBridging = false;
        return 1;
    }

}
