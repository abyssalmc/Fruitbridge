package abyssalmc.fruit.command;

import abyssalmc.GlobalDataHandler;
import abyssalmc.fruit.Fruit;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static abyssalmc.fruit.Fruit.sessionpb;

public class mmt {

    public static int textx = 0;
    public static int texty = 0;
    public static String textstring = "a";


    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("mmt").then(CommandManager.argument("momentum threshold", DoubleArgumentType.doubleArg()).executes(mmt::mmthreshold)));

        dispatcher.register(CommandManager.literal("distanceutils").then(CommandManager.literal("enable").executes(mmt::posd)));
        dispatcher.register(CommandManager.literal("distanceutils").then(CommandManager.literal("disable").executes(mmt::negd)));

        dispatcher.register(CommandManager.literal("pbhud").then(CommandManager.literal("enable").executes(mmt::posc)));
        dispatcher.register(CommandManager.literal("pbhud").then(CommandManager.literal("disable").executes(mmt::negc)));

        dispatcher.register(CommandManager.literal("distancehud").then(CommandManager.literal("enable").executes(mmt::posh)));
        dispatcher.register(CommandManager.literal("distancehud").then(CommandManager.literal("disable").executes(mmt::negh)));

        dispatcher.register(CommandManager.literal("placesounds").then(CommandManager.literal("enable").executes(mmt::posp)));
        dispatcher.register(CommandManager.literal("placesounds").then(CommandManager.literal("disable").executes(mmt::negp)));

        dispatcher.register(CommandManager.literal("tierlist").executes(mmt::tierlist));

        dispatcher.register(CommandManager.literal("pb").then(CommandManager.literal("session").then(CommandManager.literal("read").executes(mmt::psr))));
        dispatcher.register(CommandManager.literal("pb").then(CommandManager.literal("local").then(CommandManager.literal("read").executes(mmt::plr))));
        dispatcher.register(CommandManager.literal("pb").then(CommandManager.literal("session").then(CommandManager.literal("write").then(CommandManager.argument("new pb", IntegerArgumentType.integer()).executes(mmt::psw)))));
        dispatcher.register(CommandManager.literal("pb").then(CommandManager.literal("local").then(CommandManager.literal("write").then(CommandManager.argument("new pb", IntegerArgumentType.integer()).executes(mmt::plw)))));

        //dispatcher.register(CommandManager.literal("text").then(CommandManager.argument("textx", IntegerArgumentType.integer()).then(CommandManager.argument("texty", IntegerArgumentType.integer()).then(CommandManager.argument("textstring", StringArgumentType.string()).executes(mmt::text)))));

        dispatcher.register(CommandManager.literal("autosave").then(CommandManager.literal("enable").executes(mmt::enableautosave)));
        dispatcher.register(CommandManager.literal("autosave").then(CommandManager.literal("disable").executes(mmt::disableautosave)));

    }

    public static double momentumthreshold = 0.003;

    private static int mmthreshold(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        momentumthreshold = DoubleArgumentType.getDouble(context, "momentum threshold");
        context.getSource().getPlayer().sendMessage(Text.literal("§aMomentum threshold set to " + DoubleArgumentType.getDouble(context, "momentum threshold") + ". (default 0.003)"));
        return 1;
    }

    private static int posd(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        GlobalDataHandler.setUtils(true);
        context.getSource().getPlayer().sendMessage(Text.literal("§aDistance utils enabled. Note: typically only works for fruitbridge."));

        return 1;
    }
    private static int negd(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        GlobalDataHandler.setUtils(false);
        context.getSource().getPlayer().sendMessage(Text.literal("§aDistance utils disabled."));

        return 1;
    }

    private static int posh(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        GlobalDataHandler.setDist(true);
        context.getSource().getPlayer().sendMessage(Text.literal("§aDistance hud enabled. Note: typically only works for fruitbridge."));

        return 1;
    }
    private static int negh(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        GlobalDataHandler.setDist(false);
        context.getSource().getPlayer().sendMessage(Text.literal("§aDistance hud disabled."));

        return 1;
    }

    private static int posc(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        GlobalDataHandler.setPbHud(true);
        context.getSource().getPlayer().sendMessage(Text.literal("§aRecords hud enabled."));

        return 1;
    }
    private static int negc(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        GlobalDataHandler.setPbHud(false);
        context.getSource().getPlayer().sendMessage(Text.literal("§aRecords hud disabled."));

        return 1;
    }

    private static int posp(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        GlobalDataHandler.setPlaceSounds(true);
        context.getSource().getPlayer().sendMessage(Text.literal("§aPlacesounds hud enabled."));

        return 1;
    }
    private static int negp(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        GlobalDataHandler.setPlaceSounds(false);
        context.getSource().getPlayer().sendMessage(Text.literal("§aPlacesounds hud disabled."));

        return 1;
    }

    private static int text(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        textx = IntegerArgumentType.getInteger(context, "textx");
        texty = IntegerArgumentType.getInteger(context, "texty");
        textstring = StringArgumentType.getString(context, "textstring");
        return 1;
    }


    private static int tierlist(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        try {
            Util.getOperatingSystem().open(new URI("https://docs.google.com/spreadsheets/d/15NLKcBn6Chex1ppa0UivJ4PzQF_AFx8on7XoAJT6LJY/"));
            context.getSource().getPlayer().sendMessage(Text.literal("Opened §aFruitbridging Tierlists Spreadsheet§r!"));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return 1;
    }

    private static int psr(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        context.getSource().sendMessage(Text.literal("Session PB is §e" + sessionpb + "§r blocks."));
        return 1;
    }
    private static int plr(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        context.getSource().sendMessage(Text.literal("Local PB is §e" + GlobalDataHandler.getPb() + "§r blocks."));
        return 1;
    }
    private static int psw(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        sessionpb = IntegerArgumentType.getInteger(context, "new pb");
        context.getSource().sendMessage(Text.literal("Session PB updated to §e" + sessionpb + "§r blocks."));
        return 1;
    }
    private static int plw(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        GlobalDataHandler.setPb(IntegerArgumentType.getInteger(context, "new pb"));
        context.getSource().sendMessage(Text.literal("Local PB updated to §e" + GlobalDataHandler.getPb() + "§r blocks."));
        return 1;
    }

    private static int enableautosave(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        GlobalDataHandler.setAutosaves(true);
        context.getSource().sendMessage(Text.literal("§aAutosaving is now set to enabled."));
        return 1;
    }
    private static int disableautosave(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        GlobalDataHandler.setAutosaves(false);
        context.getSource().sendMessage(Text.literal("§aAutosaving is now set to disabled."));
        return 1;
    }
}
