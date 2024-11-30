package abyssalmc.fruit.command;

import abyssalmc.fruit.Fruit;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.*;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class mapgen {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("mapgen").executes(mapgen::gen));
        dispatcher.register(CommandManager.literal("setupgen").then(CommandManager.argument("setup id", StringArgumentType.string()).executes(mapgen::setup)));
    }

    private static List<String> readTextFile(String filePath) {
        List<String> lines = new ArrayList<>();

        // Load the file from resources
        try (InputStream inputStream = Fruit.class.getClassLoader().getResourceAsStream(filePath)) {
            if (inputStream == null) {
                System.out.println("File not found: " + filePath);
                return lines;
            }

            // Use BufferedReader to read each line and add it to the list
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return lines;
    }

    public static String prevtier = "LT5";


    private static int setup(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {


        String setupid = StringArgumentType.getString(context, "setup id");


        BlockPos platform = new BlockPos(context.getSource().getPlayer().getBlockPos().getX(), context.getSource().getPlayer().getBlockPos().getY(), context.getSource().getPlayer().getBlockPos().getZ());

        ServerWorld world = context.getSource().getWorld();

        BlockState tierblock = Blocks.WHITE_CONCRETE.getDefaultState();
        Item itierblockx = Items.WHITE_WOOL;


        String[] idparts = setupid.split("-");
        String itemstr = idparts[0];
        String varstr = idparts[1];


        // ITEM READER
        String[] itemlist = itemstr.split("S");
        BlockPos cmd = platform.add(0,-2,0);
        context.getSource().getServer().getCommandManager().getDispatcher().execute("gamerule sendCommandFeedback false", context.getSource());
        context.getSource().getServer().getCommandManager().getDispatcher().execute("gamerule commandBlockOutput false", context.getSource());
        context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " +  cmd.getY() + " " + cmd.getZ() + " repeating_command_block[facing=south]{auto:1b,Command:\"execute as @a at @s if entity @s[y="+(platform.getY()-3)+",dy=0.5]\"}", context.getSource());

        cmd = cmd.add(0,0,1);
        context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " +  cmd.getY() + " " + cmd.getZ() + " comparator", context.getSource());
        context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " +  (cmd.getY()-1) + " " + cmd.getZ() + " iron_trapdoor[half=top]", context.getSource());
        cmd = cmd.add(0,0,1);
        context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " +  cmd.getY() + " " + cmd.getZ() + " command_block[facing=south]{Command:\"tp @p " + platform.getX() + " " + platform.getY() + " " + (platform.getZ()-0.65) + " 0 77\"}", context.getSource());

        cmd = cmd.add(0,0,1);
        context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " +  cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"clear @p\"}", context.getSource());


        for (String item : itemlist) {
            cmd = cmd.add(0,0,1);
            String invslot = "";

            String itemname = "";
            int itemcount = 1;
            if (Character.isDigit(item.charAt(0))) {
                String[] splitproc1 = item.split("(?<=\\d)(?=\\D)", 2);

                int itemslot = Integer.parseInt(splitproc1[0]);
                String[] splitproc2 = splitproc1[1].split("(?=\\d+$)", 2);
                itemname = splitproc2[0];

                invslot = "container." + itemslot;

                itemcount = Integer.parseInt(splitproc2[1]);
            }else{
                String itemslot = item.substring(0, 1);

                String splitproc1 = item.substring(1);
                String[] splitproc2 = splitproc1.split("(?=\\d+$)", 2);
                itemname = splitproc2[0];
                itemcount = Integer.parseInt(splitproc2[1]);

                if (itemslot.contains("h")) {
                    invslot = "armor.head";
                }
                if (itemslot.contains("c")) {
                    invslot = "armor.chest";
                }
                if (itemslot.contains("l")) {
                    invslot = "armor.legs";
                }
                if (itemslot.contains("f")) {
                    invslot = "armor.feet";
                }
                if (itemslot.contains("o")) {
                    invslot = "weapon.offhand";
                }
            }


            int damage = 0;

            if (itemname.contains("D")) {
                String[] duraitem = itemname.split("D");
                damage = Integer.parseInt(duraitem[1]);
                itemname = duraitem[0];
            }


            String itemclause = itemname;

            Identifier itemId = Identifier.tryParse("minecraft", itemname);

            Item regitem = Registries.ITEM.get(itemId);
            ItemStack itemstack = new ItemStack(regitem, 1);

            if (itemname.equals("b")) {
                itemclause = "white_wool";
            }
            if (itemname.equals("c")) {
                itemclause = "white_concrete_powder";
            }
            if (itemname.equals("p")) {
                itemclause = "glass_pane";
            }
            if (itemname.equals("k")) {
                itemclause = "white_carpet";
            }
            if (itemname.equals("ishulker_box")) {
                itemclause = "shulker_box[container=[{slot:22,item:{id:\\\"minecraft:white_wool\\\",count:1}}]]";
            }
            if (itemname.equals("iishulker_box")) {
                itemclause = "shulker_box[container=[{slot:22,item:{id:\\\"minecraft:white_wool\\\",count:2}}]]";
            }
            if (itemname.equals("cbundle")) {
                itemclause = "bundle[bundle_contents=[{id:\\\"minecraft:orange_wool\\\",count:3},{id:\\\"minecraft:yellow_wool\\\",count:3},{id:\\\"minecraft:lime_wool\\\",count:3},{id:\\\"minecraft:blue_wool\\\",count:1}]]";
            }
            if (itemname.equals("tbundle")) {
                itemclause = "bundle[bundle_contents=[{id:\\\"minecraft:orange_wool\\\",count:2},{id:\\\"minecraft:yellow_wool\\\",count:2},{id:\\\"minecraft:lime_wool\\\",count:2},{id:\\\"minecraft:blue_wool\\\",count:2},{id:\\\"minecraft:purple_wool\\\",count:2},{id:\\\"minecraft:magenta_wool\\\",count:1}]]";
            }
            if (itemname.equals("iron_boots")) {
                itemclause = "iron_boots[enchantments={levels:{\\\"minecraft:soul_speed\\\":3}}]";
            }
            if (itemname.equals("golden_boots")) {
                itemclause = "iron_boots[enchantments={levels:{\\\"minecraft:frost_walker\\\":2}}]";
            }
            if (itemname.equals("crossbow")) {
                itemclause = "crossbow[charged_projectiles=[{id:\\\"minecraft:arrow\\\",count:1}]]";
            }
            if (itemstack.getItem() instanceof ToolItem) {

                itemclause = itemclause + "[enchantments={levels:{\\\"minecraft:efficiency\\\":5}}]";

            }
            if (damage != 0) {
                if (String.valueOf(itemclause.charAt(itemclause.length() - 1)).equals("]")){

                    itemclause = itemclause.substring(0, itemclause.length() - 1) + ",damage=" + (itemstack.getMaxDamage() - damage) + "]";
                }else{
                    itemclause = itemclause + "[damage=" + (itemstack.getMaxDamage() - damage) + "]";
                }

            }
            System.out.println("setblock " + cmd.getX() + " " +  cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"item replace entity @p " + invslot + " with " + itemclause + " " + itemcount + "\"}");
            context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " +  cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"item replace entity @p " + invslot + " with " + itemclause + " " + itemcount + "\"}", context.getSource());

        }

        // VAR READER

        varstr = String.format("%6s", varstr).replace(' ', '0');

        String vari = "";
        int varindex2 = 0;
        for (int varindex = 0; varindex < 6; varindex++) {
            vari = String.valueOf(varstr.charAt(varindex2));

                if (varindex2 == 1) {
                //STARTING BLOCK

                String blockstr = "";
                //0 = no starting block, 1 = jukebox, 2 = glass, 3 = waterlogged leaves, 4 = ps, 5 = stairs, 6 = redstone block, 7 = target, 8 = moss block, 9 = oak planks, A = trappedchest, B = sponge, C = forward waterlogged mangrove roots
                if (vari.contains("1")) {
                    world.setBlockState(platform.north().north().down(), Blocks.JUKEBOX.getDefaultState());
                    blockstr = "jukebox";
                }
                if (vari.contains("2")) {
                    world.setBlockState(platform.north().north().down(), Blocks.GLASS.getDefaultState());
                    blockstr = "glass";
                }
                if (vari.contains("3")) {
                    world.setBlockState(platform.north().north().down(), Blocks.AZALEA_LEAVES.getDefaultState().with(Properties.WATERLOGGED, true).with(Properties.PERSISTENT, true));
                    blockstr = "azalea_leaves[waterlogged=true]";
                }
                if (vari.contains("4")) {
                    world.setBlockState(platform.north().north().down(), Blocks.POWDER_SNOW.getDefaultState());
                    blockstr = "powder_snow";
                }
                if (vari.contains("5")) {
                    world.setBlockState(platform.north().north().down(), Blocks.OAK_SLAB.getDefaultState());
                    blockstr = "oak_slab";
                }
                if (vari.contains("6")) {
                    world.setBlockState(platform.north().north().down(), Blocks.REDSTONE_BLOCK.getDefaultState());
                    blockstr = "redstone_block";
                }
                if (vari.contains("7")) {
                    world.setBlockState(platform.north().north().down(), Blocks.TARGET.getDefaultState());
                    blockstr = "target";
                }
                if (vari.contains("8")) {
                    world.setBlockState(platform.north().north().down(), Blocks.MOSS_BLOCK.getDefaultState());
                    blockstr = "moss_block";
                }
                if (vari.contains("9")) {
                    world.setBlockState(platform.north().north().down(), Blocks.OAK_PLANKS.getDefaultState());
                    blockstr = "oak_planks";
                }
                if (vari.contains("A")) {
                    world.setBlockState(platform.north().north().down(), Blocks.TRAPPED_CHEST.getDefaultState());
                    blockstr = "trapped_chest";
                }
                if (vari.contains("B")) {
                    world.setBlockState(platform.north().north().down(), Blocks.SPONGE.getDefaultState());
                    blockstr = "sponge";
                }
                int shifted = 0;
                if (vari.contains("C")) {
                    world.setBlockState(platform.north().down(), Blocks.MANGROVE_ROOTS.getDefaultState().with(Properties.WATERLOGGED, true));
                    blockstr = "mangrove_roots[waterlogged=true]";
                    shifted = 1;
                }
                if (vari.contains("D")) {
                    world.setBlockState(platform.north().north().down(), Blocks.JUNGLE_LOG.getDefaultState());
                    blockstr = "jungle_log";
                }
                if (vari.contains("E")) {
                    world.setBlockState(platform.north().north().down(), Blocks.SCULK_SENSOR.getDefaultState());
                    blockstr = "sculk_sensor";
                }
                if (!vari.contains("0")) {
                    if (shifted == 0) {
                        System.out.println("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"setblock " + platform.getX() + " " + platform.down().getY() + " " + platform.north().north().getZ() + " " + blockstr + "\"}");
                        cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"setblock " + platform.getX() + " " + platform.down().getY() + " " + platform.north().north().getZ() + " " + blockstr + "\"}", context.getSource());
                    } else {
                        cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"setblock " + platform.getX() + " " + platform.down().getY() + " " + platform.north().getZ() + " " + blockstr + "\"}", context.getSource());
                    }
                }

            }
            if (varindex2 == 2) {
                //EFFECTS

                String effect = "";
                if (!vari.contains("0")) {
                    if (vari.contains("1")){effect = "speed infinite 1";}
                    if (vari.contains("2")){effect = "jump_boost infinite 0";}
                    if (vari.contains("3")){effect = "jump_boost infinite 1";}
                    if (vari.contains("4")){effect = "blindness infinite 0";}
                    if (vari.contains("5")){effect = "slow_falling infinite 0";}
                    if (vari.contains("6")){effect = "invisibility infinite 0";}
                    if (vari.contains("7")){effect = "strength infinite 255";}

                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"effect give @p " + effect + "\"}", context.getSource());
                }
            }
            if (varindex2 == 3) {
                //ELEVATION

                cmd=cmd.add(0,0,1);
                context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"fill " + (platform.getX()+2) + " " + -64 + " " + (platform.getZ()-3) + " " + (platform.getX()-2) + " " + (platform.getY()+8) + " " + (platform.getZ()-14) + " air\"}", context.getSource());
                cmd=cmd.add(0,0,1);
                context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"tp @e[type=!player] 9999 0 0\"}", context.getSource());
            }
            if (varindex2 == 4) {
                //VAR

                // FORWARD IS NEG Z
                // RIGHT IS POS X
                if (vari.contains("1")) {
                    // classical lava
                    world.setBlockState(platform.add(-2, 0, -4), Blocks.LAVA_CAULDRON.getDefaultState());
                    world.setBlockState(platform.add(2, 0, -7), Blocks.LAVA_CAULDRON.getDefaultState());
                    world.setBlockState(platform.add(-2, 0, -10), Blocks.LAVA_CAULDRON.getDefaultState());
                    world.setBlockState(platform.add(2, 0, -13), Blocks.LAVA_CAULDRON.getDefaultState());

                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"setblock " + (platform.getX()-2) + " " + platform.getY() + " " + (platform.getZ()-4) + " lava_cauldron\"}", context.getSource());
                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"setblock " + (platform.getX()+2) + " " + platform.getY() + " " + (platform.getZ()-7) + " lava_cauldron\"}", context.getSource());
                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"setblock " + (platform.getX()+2) + " " + platform.getY() + " " + (platform.getZ()-13) + " lava_cauldron\"}", context.getSource());
                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"setblock " + (platform.getX()-2) + " " + platform.getY() + " " + (platform.getZ()-10) + " lava_cauldron\"}", context.getSource());
                }
                if (vari.contains("2")) {
                    // classical water
                    world.setBlockState(platform.add(-2, 0, -4), Blocks.WATER_CAULDRON.getDefaultState().with(Properties.LEVEL_3, 3));
                    world.setBlockState(platform.add(2, 0, -7), Blocks.WATER_CAULDRON.getDefaultState().with(Properties.LEVEL_3, 3));
                    world.setBlockState(platform.add(-2, 0, -10), Blocks.WATER_CAULDRON.getDefaultState().with(Properties.LEVEL_3, 3));
                    world.setBlockState(platform.add(2, 0, -13), Blocks.WATER_CAULDRON.getDefaultState().with(Properties.LEVEL_3, 3));

                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"setblock " + (platform.getX()-2) + " " + platform.getY() + " " + (platform.getZ()-4) + " water_cauldron[level=3]\"}", context.getSource());
                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"setblock " + (platform.getX()+2) + " " + platform.getY() + " " + (platform.getZ()-7) + " water_cauldron[level=3]\"}", context.getSource());
                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"setblock " + (platform.getX()-2) + " " + platform.getY() + " " + (platform.getZ()-10) + " water_cauldron[level=3]\"}", context.getSource());
                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"setblock " + (platform.getX()+2) + " " + platform.getY() + " " + (platform.getZ()-13) + " water_cauldron[level=3]\"}", context.getSource());
                }
                if (vari.contains("3")) {
                    // true water
                    world.setBlockState(platform.add(-2, 0, -3), Blocks.WATER_CAULDRON.getDefaultState().with(Properties.LEVEL_3, 3));
                    world.setBlockState(platform.add(2, 0, -5), Blocks.WATER_CAULDRON.getDefaultState().with(Properties.LEVEL_3, 3));
                    world.setBlockState(platform.add(-2, 0, -7), Blocks.WATER_CAULDRON.getDefaultState().with(Properties.LEVEL_3, 3));
                    world.setBlockState(platform.add(2, 0, -9), Blocks.WATER_CAULDRON.getDefaultState().with(Properties.LEVEL_3, 3));
                    world.setBlockState(platform.add(-2, 0, -11), Blocks.WATER_CAULDRON.getDefaultState().with(Properties.LEVEL_3, 3));
                    world.setBlockState(platform.add(2, 0, -13), Blocks.WATER_CAULDRON.getDefaultState().with(Properties.LEVEL_3, 3));

                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"setblock " + (platform.getX()-2) + " " + (platform.getY()) + " " + (platform.getZ()-3) + " water_cauldron[level=3]\"}", context.getSource());
                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"setblock " + (platform.getX()+2) + " " + (platform.getY()) + " " + (platform.getZ()-5) + " water_cauldron[level=3]\"}", context.getSource());
                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"setblock " + (platform.getX()-2) + " " + (platform.getY()) + " " + (platform.getZ()-7) + " water_cauldron[level=3]\"}", context.getSource());
                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"setblock " + (platform.getX()+2) + " " + (platform.getY()) + " " + (platform.getZ()-9) + " water_cauldron[level=3]\"}", context.getSource());
                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"setblock " + (platform.getX()-2) + " " + (platform.getY()) + " " + (platform.getZ()-11) + " water_cauldron[level=3]\"}", context.getSource());
                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"setblock " + (platform.getX()+2) + " " + (platform.getY()) + " " + (platform.getZ()-13) + " water_cauldron[level=3]\"}", context.getSource());
                }
                if (vari.contains("4")) {
                    // classical chest
                    world.setBlockState(platform.add(-2, 0, -4), Blocks.CHEST.getDefaultState());
                    ((ChestBlockEntity) world.getBlockEntity(platform.add(-2, 0, -4))).setStack(22, new ItemStack(itierblockx, 3));
                    world.setBlockState(platform.add(2, 0, -7), Blocks.CHEST.getDefaultState());
                    ((ChestBlockEntity) world.getBlockEntity(platform.add(2, 0, -7))).setStack(22, new ItemStack(itierblockx, 3));
                    world.setBlockState(platform.add(-2, 0, -10), Blocks.CHEST.getDefaultState());
                    ((ChestBlockEntity) world.getBlockEntity(platform.add(-2, 0, -10))).setStack(22, new ItemStack(itierblockx, 3));
                    world.setBlockState(platform.add(2, 0, -13), Blocks.CHEST.getDefaultState());
                    ((ChestBlockEntity) world.getBlockEntity(platform.add(2, 0, -13))).setStack(22, new ItemStack(itierblockx, 1));

                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"setblock " + (platform.getX()-2) + " " + (platform.getY()) + " " + (platform.getZ()-4) + " chest{Items:[{Slot:22b,id:\"minecraft:white_wool\",count:1}]}\"}", context.getSource());
                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"setblock " + (platform.getX()+2) + " " + (platform.getY()) + " " + (platform.getZ()-7) + " chest{Items:[{Slot:22b,id:\"minecraft:white_wool\",count:1}]}\"}", context.getSource());
                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"setblock " + (platform.getX()-2) + " " + (platform.getY()) + " " + (platform.getZ()-10) + " chest{Items:[{Slot:22b,id:\"minecraft:white_wool\",count:1}]}\"}", context.getSource());
                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"setblock " + (platform.getX()+2) + " " + (platform.getY()) + " " + (platform.getZ()-13) + " chest{Items:[{Slot:22b,id:\"minecraft:white_wool\",count:1}]}\"}", context.getSource());
                }
                if (vari.contains("5")) {
                    // moving piston
                    world.setBlockState(platform.add(0, 0, -2), Blocks.MOVING_PISTON.getDefaultState());
                    world.setBlockState(platform.add(0, 0, -3), Blocks.MOVING_PISTON.getDefaultState());
                    world.setBlockState(platform.add(0, 0, -4), Blocks.MOVING_PISTON.getDefaultState());
                    world.setBlockState(platform.add(0, 0, -5), Blocks.MOVING_PISTON.getDefaultState());
                    world.setBlockState(platform.add(0, 0, -6), Blocks.MOVING_PISTON.getDefaultState());
                    world.setBlockState(platform.add(0, 0, -7), Blocks.MOVING_PISTON.getDefaultState());
                    world.setBlockState(platform.add(0, 0, -8), Blocks.MOVING_PISTON.getDefaultState());
                    world.setBlockState(platform.add(0, 0, -9), Blocks.MOVING_PISTON.getDefaultState());
                    world.setBlockState(platform.add(0, 0, -10), Blocks.MOVING_PISTON.getDefaultState());
                    world.setBlockState(platform.add(0, 0, -11), Blocks.MOVING_PISTON.getDefaultState());
                    world.setBlockState(platform.add(0, 0, -12), Blocks.MOVING_PISTON.getDefaultState());
                    world.setBlockState(platform.add(0, 0, -13), Blocks.MOVING_PISTON.getDefaultState());
                    world.setBlockState(platform.add(0, 0, -14), Blocks.MOVING_PISTON.getDefaultState());

                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"fill " + platform.getX() + " " + platform.getY() + " " + (platform.getZ()-2) + "" + platform.getX() + " " + platform.getY() + " " + (platform.getZ()-14) + " moving_piston\"}", context.getSource());
                }
                if (vari.contains("6")) {
                    // spleef classical chest
                    world.setBlockState(platform.add(-2, 0, -4), Blocks.CHEST.getDefaultState());
                    world.setBlockState(platform.add(-2, 1, -4), Blocks.SLIME_BLOCK.getDefaultState());
                    ((ChestBlockEntity) world.getBlockEntity(platform.add(-2, 0, -4))).setStack(22, new ItemStack(itierblockx, 3));
                    world.setBlockState(platform.add(2, 0, -7), Blocks.CHEST.getDefaultState());
                    world.setBlockState(platform.add(2, 1, -7), Blocks.SLIME_BLOCK.getDefaultState());
                    ((ChestBlockEntity) world.getBlockEntity(platform.add(2, 0, -7))).setStack(22, new ItemStack(itierblockx, 3));
                    world.setBlockState(platform.add(-2, 0, -10), Blocks.CHEST.getDefaultState());
                    world.setBlockState(platform.add(-2, 1, -10), Blocks.SLIME_BLOCK.getDefaultState());
                    ((ChestBlockEntity) world.getBlockEntity(platform.add(-2, 0, -10))).setStack(22, new ItemStack(itierblockx, 3));
                    world.setBlockState(platform.add(2, 0, -13), Blocks.CHEST.getDefaultState());
                    world.setBlockState(platform.add(2, 1, -13), Blocks.SLIME_BLOCK.getDefaultState());
                    ((ChestBlockEntity) world.getBlockEntity(platform.add(2, 0, -13))).setStack(22, new ItemStack(itierblockx, 1));

                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"setblock " + (platform.getX()-2) + " " + (platform.getY()) + " " + (platform.getZ()-4) + " chest{Items:[{Slot:22b,id:\"minecraft:white_wool\",count:1}]}\"}", context.getSource());
                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"setblock " + (platform.getX()+2) + " " + (platform.getY()) + " " + (platform.getZ()-7) + " chest{Items:[{Slot:22b,id:\"minecraft:white_wool\",count:1}]}\"}", context.getSource());
                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"setblock " + (platform.getX()-2) + " " + (platform.getY()) + " " + (platform.getZ()-10) + " chest{Items:[{Slot:22b,id:\"minecraft:white_wool\",count:1}]}\"}", context.getSource());
                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"setblock " + (platform.getX()+2) + " " + (platform.getY()) + " " + (platform.getZ()-13) + " chest{Items:[{Slot:22b,id:\"minecraft:white_wool\",count:1}]}\"}", context.getSource());

                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"setblock " + (platform.getX()-2) + " " + (platform.getY()+1) + " " + (platform.getZ()-4) + " slime_block\"}", context.getSource());
                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"setblock " + (platform.getX()+2) + " " + (platform.getY()+1) + " " + (platform.getZ()-7) + " slime_block\"}", context.getSource());
                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"setblock " + (platform.getX()-2) + " " + (platform.getY()+1) + " " + (platform.getZ()-10) + " slime_block\"}", context.getSource());
                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"setblock " + (platform.getX()+2) + " " + (platform.getY()+1) + " " + (platform.getZ()-13) + " slime_block\"}", context.getSource());
                }
                if (vari.contains("7")) {
                    // villager
                    context.getSource().getServer().getCommandManager().getDispatcher().execute("summon villager " + (platform.getX()-0.7) + " " + platform.getY() + " " + (platform.getZ()-4) + " {NoAI:1b,Rotation:[180F,0F],VillagerData:{profession:\"minecraft:librarian\"},Offers:{Recipes:[{maxUses:1,buy:{id:\"minecraft:emerald\",count:9},sell:{id:\"minecraft:bookshelf\",count:1}},{buy:{id:\"minecraft:paper\",count:24},sell:{id:\"minecraft:emerald\",count:1}}]}}",context.getSource());
                    context.getSource().getServer().getCommandManager().getDispatcher().execute("summon villager " + (platform.getX()+1.7) + " " + platform.getY() + " " + (platform.getZ()-7) + " {NoAI:1b,Rotation:[180F,0F],VillagerData:{profession:\"minecraft:librarian\"},Offers:{Recipes:[{maxUses:1,buy:{id:\"minecraft:emerald\",count:9},sell:{id:\"minecraft:bookshelf\",count:1}},{buy:{id:\"minecraft:paper\",count:24},sell:{id:\"minecraft:emerald\",count:1}}]}}",context.getSource());
                    context.getSource().getServer().getCommandManager().getDispatcher().execute("summon villager " + (platform.getX()-0.7) + " " + platform.getY() + " " + (platform.getZ()-10) + " {NoAI:1b,Rotation:[180F,0F],VillagerData:{profession:\"minecraft:librarian\"},Offers:{Recipes:[{maxUses:1,buy:{id:\"minecraft:emerald\",count:9},sell:{id:\"minecraft:bookshelf\",count:1}},{buy:{id:\"minecraft:paper\",count:24},sell:{id:\"minecraft:emerald\",count:1}}]}}",context.getSource());
                    context.getSource().getServer().getCommandManager().getDispatcher().execute("summon villager " + (platform.getX()+1.7) + " " + platform.getY() + " " + (platform.getZ()-13) + " {NoAI:1b,Rotation:[180F,0F],VillagerData:{profession:\"minecraft:librarian\"},Offers:{Recipes:[{maxUses:1,buy:{id:\"minecraft:emerald\",count:9},sell:{id:\"minecraft:bookshelf\",count:1}},{buy:{id:\"minecraft:paper\",count:24},sell:{id:\"minecraft:emerald\",count:1}}]}}",context.getSource());

                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"summon villager " + (platform.getX()-0.7) + " " + platform.getY() + " " + (platform.getZ()-4) + " {NoAI:1b,Rotation:[180F,0F],VillagerData:{profession:\\\"minecraft:librarian\\\"},Offers:{Recipes:[{maxUses:1,buy:{id:\\\"minecraft:emerald\\\",count:9},sell:{id:\\\"minecraft:bookshelf\\\",count:1}},{buy:{id:\\\"minecraft:paper\\\",count:24},sell:{id:\\\"minecraft:emerald\\\",count:1}}]}}\"}", context.getSource());
                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"summon villager " + (platform.getX()+1.7) + " " + platform.getY() + " " + (platform.getZ()-7) + " {NoAI:1b,Rotation:[180F,0F],VillagerData:{profession:\\\"minecraft:librarian\\\"},Offers:{Recipes:[{maxUses:1,buy:{id:\\\"minecraft:emerald\\\",count:9},sell:{id:\\\"minecraft:bookshelf\\\",count:1}},{buy:{id:\\\"minecraft:paper\\\",count:24},sell:{id:\\\"minecraft:emerald\\\",count:1}}]}}\"}", context.getSource());
                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"summon villager " + (platform.getX()-0.7) + " " + platform.getY() + " " + (platform.getZ()-10) + " {NoAI:1b,Rotation:[180F,0F],VillagerData:{profession:\\\"minecraft:librarian\\\"},Offers:{Recipes:[{maxUses:1,buy:{id:\\\"minecraft:emerald\\\",count:9},sell:{id:\\\"minecraft:bookshelf\\\",count:1}},{buy:{id:\\\"minecraft:paper\\\",count:24},sell:{id:\\\"minecraft:emerald\\\",count:1}}]}}\"}", context.getSource());
                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"summon villager " + (platform.getX()+1.7) + " " + platform.getY() + " " + (platform.getZ()-13) + " {NoAI:1b,Rotation:[180F,0F],VillagerData:{profession:\\\"minecraft:librarian\\\"},Offers:{Recipes:[{maxUses:1,buy:{id:\\\"minecraft:emerald\\\",count:9},sell:{id:\\\"minecraft:bookshelf\\\",count:1}},{buy:{id:\\\"minecraft:paper\\\",count:24},sell:{id:\\\"minecraft:emerald\\\",count:1}}]}}\"}", context.getSource());
                }
                if (vari.contains("8")) {
                    // celestial villager
                    context.getSource().getServer().getCommandManager().getDispatcher().execute("summon villager " + (platform.getX()-0.7) + " " + platform.getY() + " " + (platform.getZ()-4) + " {NoAI:1b,Rotation:[180F,0F],VillagerData:{profession:\"minecraft:librarian\"},Offers:{Recipes:[{maxUses:1,specialPrice:-6,buy:{id:\"minecraft:emerald\",count:9},sell:{id:\"minecraft:bookshelf\",count:1}},{maxUses:1,specialPrice:-13,buy:{id:\"minecraft:paper\",count:24},sell:{id:\"minecraft:emerald\",count:1}}]}}",context.getSource());
                    context.getSource().getServer().getCommandManager().getDispatcher().execute("summon villager " + (platform.getX()+1.7) + " " + platform.getY() + " " + (platform.getZ()-7) + " {NoAI:1b,Rotation:[180F,0F],VillagerData:{profession:\"minecraft:librarian\"},Offers:{Recipes:[{maxUses:1,specialPrice:-6,buy:{id:\"minecraft:emerald\",count:9},sell:{id:\"minecraft:bookshelf\",count:1}},{maxUses:1,specialPrice:-13,buy:{id:\"minecraft:paper\",count:24},sell:{id:\"minecraft:emerald\",count:1}}]}}",context.getSource());
                    context.getSource().getServer().getCommandManager().getDispatcher().execute("summon villager " + (platform.getX()-0.7) + " " + platform.getY() + " " + (platform.getZ()-10) + " {NoAI:1b,Rotation:[180F,0F],VillagerData:{profession:\"minecraft:librarian\"},Offers:{Recipes:[{maxUses:1,specialPrice:-6,buy:{id:\"minecraft:emerald\",count:9},sell:{id:\"minecraft:bookshelf\",count:1}},{maxUses:1,specialPrice:-13,buy:{id:\"minecraft:paper\",count:24},sell:{id:\"minecraft:emerald\",count:1}}]}}",context.getSource());
                    context.getSource().getServer().getCommandManager().getDispatcher().execute("summon villager " + (platform.getX()+1.7) + " " + platform.getY() + " " + (platform.getZ()-13) + " {NoAI:1b,Rotation:[180F,0F],VillagerData:{profession:\"minecraft:librarian\"},Offers:{Recipes:[{maxUses:1,specialPrice:-6,buy:{id:\"minecraft:emerald\",count:9},sell:{id:\"minecraft:bookshelf\",count:1}},{maxUses:1,specialPrice:-13,buy:{id:\"minecraft:paper\",count:24},sell:{id:\"minecraft:emerald\",count:1}}]}}",context.getSource());

                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"summon villager " + (platform.getX()-0.7) + " " + platform.getY() + " " + (platform.getZ()-4) + " {NoAI:1b,Rotation:[180F,0F],VillagerData:{profession:\"minecraft:librarian\"},Offers:{Recipes:[{maxUses:1,specialPrice:-6,buy:{id:\"minecraft:emerald\",count:9},sell:{id:\"minecraft:bookshelf\",count:1}},{maxUses:1,specialPrice:-13,buy:{id:\"minecraft:paper\",count:24},sell:{id:\"minecraft:emerald\",count:1}}]}}\"}", context.getSource());
                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"summon villager " + (platform.getX()+1.7) + " " + platform.getY() + " " + (platform.getZ()-7) + " {NoAI:1b,Rotation:[180F,0F],VillagerData:{profession:\"minecraft:librarian\"},Offers:{Recipes:[{maxUses:1,specialPrice:-6,buy:{id:\"minecraft:emerald\",count:9},sell:{id:\"minecraft:bookshelf\",count:1}},{maxUses:1,specialPrice:-13,buy:{id:\"minecraft:paper\",count:24},sell:{id:\"minecraft:emerald\",count:1}}]}}\"}", context.getSource());
                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"summon villager " + (platform.getX()-0.7) + " " + platform.getY() + " " + (platform.getZ()-10) + " {NoAI:1b,Rotation:[180F,0F],VillagerData:{profession:\"minecraft:librarian\"},Offers:{Recipes:[{maxUses:1,specialPrice:-6,buy:{id:\"minecraft:emerald\",count:9},sell:{id:\"minecraft:bookshelf\",count:1}},{maxUses:1,specialPrice:-13,buy:{id:\"minecraft:paper\",count:24},sell:{id:\"minecraft:emerald\",count:1}}]}}\"}", context.getSource());
                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"summon villager " + (platform.getX()+1.7) + " " + platform.getY() + " " + (platform.getZ()-13) + " {NoAI:1b,Rotation:[180F,0F],VillagerData:{profession:\"minecraft:librarian\"},Offers:{Recipes:[{maxUses:1,specialPrice:-6,buy:{id:\"minecraft:emerald\",count:9},sell:{id:\"minecraft:bookshelf\",count:1}},{maxUses:1,specialPrice:-13,buy:{id:\"minecraft:paper\",count:24},sell:{id:\"minecraft:emerald\",count:1}}]}}\"}", context.getSource());

                }
                if (vari.contains("9")) {
                    // allay
                    context.getSource().getServer().getCommandManager().getDispatcher().execute("summon allay " + platform.getX() + ".0" + " " + platform.getY() + " " + (platform.getZ()-2) + ".0 {NoAI:1b,Rotation:[180F,0F],HandItems:[{id:\"minecraft:white_wool\",count:1},{}]}",context.getSource());
                    context.getSource().getServer().getCommandManager().getDispatcher().execute("summon allay " + (platform.getX()+1) + ".0" + " " + platform.getY() + " " + (platform.getZ()-3) + ".0 {NoAI:1b,Rotation:[180F,0F],HandItems:[{id:\"minecraft:white_wool\",count:1},{}]}",context.getSource());
                    context.getSource().getServer().getCommandManager().getDispatcher().execute("summon allay " + platform.getX() + ".0" + " " + platform.getY() + " " + (platform.getZ()-4) + ".0 {NoAI:1b,Rotation:[180F,0F],HandItems:[{id:\"minecraft:white_wool\",count:1},{}]}",context.getSource());
                    context.getSource().getServer().getCommandManager().getDispatcher().execute("summon allay " + (platform.getX()+1) + ".0" + " " + platform.getY() + " " + (platform.getZ()-5) + ".0 {NoAI:1b,Rotation:[180F,0F],HandItems:[{id:\"minecraft:white_wool\",count:1},{}]}",context.getSource());
                    context.getSource().getServer().getCommandManager().getDispatcher().execute("summon allay " + platform.getX() + ".0" + " " + platform.getY() + " " + (platform.getZ()-6) + ".0 {NoAI:1b,Rotation:[180F,0F],HandItems:[{id:\"minecraft:white_wool\",count:1},{}]}",context.getSource());
                    context.getSource().getServer().getCommandManager().getDispatcher().execute("summon allay " + (platform.getX()+1) + ".0" + " " + platform.getY() + " " + (platform.getZ()-7) + ".0 {NoAI:1b,Rotation:[180F,0F],HandItems:[{id:\"minecraft:white_wool\",count:1},{}]}",context.getSource());
                    context.getSource().getServer().getCommandManager().getDispatcher().execute("summon allay " + platform.getX() + ".0" + " " + platform.getY() + " " + (platform.getZ()-8) + ".0 {NoAI:1b,Rotation:[180F,0F],HandItems:[{id:\"minecraft:white_wool\",count:1},{}]}",context.getSource());
                    context.getSource().getServer().getCommandManager().getDispatcher().execute("summon allay " + (platform.getX()+1) + ".0" + " " + platform.getY() + " " + (platform.getZ()-9) + ".0 {NoAI:1b,Rotation:[180F,0F],HandItems:[{id:\"minecraft:white_wool\",count:1},{}]}",context.getSource());
                    context.getSource().getServer().getCommandManager().getDispatcher().execute("summon allay " + platform.getX() + ".0" + " " + platform.getY() + " " + (platform.getZ()-10) + ".0 {NoAI:1b,Rotation:[180F,0F],HandItems:[{id:\"minecraft:white_wool\",count:1},{}]}",context.getSource());
                    context.getSource().getServer().getCommandManager().getDispatcher().execute("summon allay " + (platform.getX()+1) + ".0" + " " + platform.getY() + " " + (platform.getZ()-11) + ".0 {NoAI:1b,Rotation:[180F,0F],HandItems:[{id:\"minecraft:white_wool\",count:1},{}]}",context.getSource());
                    context.getSource().getServer().getCommandManager().getDispatcher().execute("summon allay " + platform.getX() + ".0" + " " + platform.getY() + " " + (platform.getZ()-12) + ".0 {NoAI:1b,Rotation:[180F,0F],HandItems:[{id:\"minecraft:white_wool\",count:1},{}]}",context.getSource());

                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"summon allay " + platform.getX() + ".0" + " " + platform.getY() + " " + (platform.getZ()-2) + ".0 {NoAI:1b,Rotation:[180F,0F],HandItems:[{id:\\\"minecraft:white_wool\\\",count:1},{}]}\"}", context.getSource());
                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"summon allay " + (platform.getX()+1) + ".0" + " " + platform.getY() + " " + (platform.getZ()-3) + ".0 {NoAI:1b,Rotation:[180F,0F],HandItems:[{id:\\\"minecraft:white_wool\\\",count:1},{}]}\"}", context.getSource());
                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"summon allay " + platform.getX() + ".0" + " " + platform.getY() + " " + (platform.getZ()-4) + ".0 {NoAI:1b,Rotation:[180F,0F],HandItems:[{id:\\\"minecraft:white_wool\\\",count:1},{}]}\"}", context.getSource());
                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"summon allay " + (platform.getX()+1) + ".0" + " " + platform.getY() + " " + (platform.getZ()-5) + ".0 {NoAI:1b,Rotation:[180F,0F],HandItems:[{id:\\\"minecraft:white_wool\\\",count:1},{}]}\"}", context.getSource());
                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"summon allay " + platform.getX() + ".0" + " " + platform.getY() + " " + (platform.getZ()-6) + ".0 {NoAI:1b,Rotation:[180F,0F],HandItems:[{id:\\\"minecraft:white_wool\\\",count:1},{}]}\"}", context.getSource());
                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"summon allay " + (platform.getX()+1) + ".0" + " " + platform.getY() + " " + (platform.getZ()-7) + ".0 {NoAI:1b,Rotation:[180F,0F],HandItems:[{id:\\\"minecraft:white_wool\\\",count:1},{}]}\"}", context.getSource());
                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"summon allay " + platform.getX() + ".0" + " " + platform.getY() + " " + (platform.getZ()-8) + ".0 {NoAI:1b,Rotation:[180F,0F],HandItems:[{id:\\\"minecraft:white_wool\\\",count:1},{}]}\"}", context.getSource());
                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"summon allay " + (platform.getX()+1) + ".0" + " " + platform.getY() + " " + (platform.getZ()-9) + ".0 {NoAI:1b,Rotation:[180F,0F],HandItems:[{id:\\\"minecraft:white_wool\\\",count:1},{}]}\"}", context.getSource());
                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"summon allay " + platform.getX() + ".0" + " " + platform.getY() + " " + (platform.getZ()-10) + ".0 {NoAI:1b,Rotation:[180F,0F],HandItems:[{id:\\\"minecraft:white_wool\\\",count:1},{}]}\"}", context.getSource());
                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"summon allay " + (platform.getX()+1) + ".0" + " " + platform.getY() + " " + (platform.getZ()-11) + ".0 {NoAI:1b,Rotation:[180F,0F],HandItems:[{id:\\\"minecraft:white_wool\\\",count:1},{}]}\"}", context.getSource());
                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"summon allay " + platform.getX() + ".0" + " " + platform.getY() + " " + (platform.getZ()-12) + ".0 {NoAI:1b,Rotation:[180F,0F],HandItems:[{id:\\\"minecraft:white_wool\\\",count:1},{}]}\"}", context.getSource());


                }
                if (vari.contains("A")) {
                    // lantern
                    for (int filly = 0; filly < 2; filly++) {
                        for (int fillz = -3; fillz > -15; fillz--) {
                            world.setBlockState(platform.add(0, filly, fillz), Blocks.CHAIN.getDefaultState());
                            world.setBlockState(platform.add(0, filly, fillz), Blocks.CHAIN.getDefaultState());
                            world.setBlockState(platform.add(0, filly, fillz), Blocks.CHAIN.getDefaultState());
                            world.setBlockState(platform.add(0, filly, fillz), Blocks.CHAIN.getDefaultState());
                            world.setBlockState(platform.add(0, filly, fillz), Blocks.CHAIN.getDefaultState());
                            world.setBlockState(platform.add(0, filly, fillz), Blocks.CHAIN.getDefaultState());
                            world.setBlockState(platform.add(0, filly, fillz), Blocks.CHAIN.getDefaultState());
                            world.setBlockState(platform.add(0, filly, fillz), Blocks.CHAIN.getDefaultState());
                            world.setBlockState(platform.add(0, filly, fillz), Blocks.CHAIN.getDefaultState());
                            world.setBlockState(platform.add(0, filly, fillz), Blocks.CHAIN.getDefaultState());
                            world.setBlockState(platform.add(0, filly, fillz), Blocks.CHAIN.getDefaultState());
                            world.setBlockState(platform.add(0, filly, fillz), Blocks.CHAIN.getDefaultState());
                        }
                    }
                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"fill " + platform.getX() + " " + (platform.getY()) + " " + (platform.getZ()-3) + "" + platform.getX() + " " + (platform.getY()+1) + " " + (platform.getZ()-14) + " chain\"}", context.getSource());

                }
                if (vari.contains("B")) {
                    // ender chest
                    context.getSource().getServer().getCommandManager().getDispatcher().execute("item replace entity @a enderchest.14 with red_wool",context.getSource());
                    context.getSource().getServer().getCommandManager().getDispatcher().execute("item replace entity @a enderchest.12 with orange_wool",context.getSource());
                    context.getSource().getServer().getCommandManager().getDispatcher().execute("item replace entity @a enderchest.13 with yellow_wool",context.getSource());
                    context.getSource().getServer().getCommandManager().getDispatcher().execute("item replace entity @a enderchest.23 with lime_wool",context.getSource());
                    context.getSource().getServer().getCommandManager().getDispatcher().execute("item replace entity @a enderchest.21 with blue_wool",context.getSource());
                    context.getSource().getServer().getCommandManager().getDispatcher().execute("item replace entity @a enderchest.22 with purple_wool",context.getSource());

                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"item replace entity @a enderchest.14 with red_wool\"}", context.getSource());
                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"item replace entity @a enderchest.12 with orange_wool\"}", context.getSource());
                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"item replace entity @a enderchest.13 with yellow_wool\"}", context.getSource());
                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"item replace entity @a enderchest.23 with lime_wool\"}", context.getSource());
                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"item replace entity @a enderchest.21 with blue_wool\"}", context.getSource());
                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"item replace entity @a enderchest.22 with purple_wool\"}", context.getSource());
                }
                if (vari.contains("C")) {
                    // butane
                    world.setBlockState(platform.add(2, 0, -4), Blocks.LAVA_CAULDRON.getDefaultState());
                    world.setBlockState(platform.add(-2, 0, -7), Blocks.LAVA_CAULDRON.getDefaultState());
                    world.setBlockState(platform.add(2, 0, -10), Blocks.LAVA_CAULDRON.getDefaultState());
                    world.setBlockState(platform.add(-2, 0, -13), Blocks.LAVA_CAULDRON.getDefaultState());

                    world.setBlockState(platform.add(-2, 0, -4), Blocks.WATER_CAULDRON.getDefaultState().with(Properties.LEVEL_3, 3));
                    world.setBlockState(platform.add(2, 0, -7), Blocks.WATER_CAULDRON.getDefaultState().with(Properties.LEVEL_3, 3));
                    world.setBlockState(platform.add(-2, 0, -10), Blocks.WATER_CAULDRON.getDefaultState().with(Properties.LEVEL_3, 3));
                    world.setBlockState(platform.add(2, 0, -13), Blocks.WATER_CAULDRON.getDefaultState().with(Properties.LEVEL_3, 3));

                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"setblock " + (platform.getX()+2) + " " + platform.getY() + " " + (platform.getZ()-4) + " lava_cauldron\"}", context.getSource());
                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"setblock " + (platform.getX()-2) + " " + platform.getY() + " " + (platform.getZ()-7) + " lava_cauldron\"}", context.getSource());
                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"setblock " + (platform.getX()+2) + " " + platform.getY() + " " + (platform.getZ()-10) + " lava_cauldron\"}", context.getSource());
                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"setblock " + (platform.getX()-2) + " " + platform.getY() + " " + (platform.getZ()-13) + " lava_cauldron\"}", context.getSource());

                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"setblock " + (platform.getX()-2) + " " + platform.getY() + " " + (platform.getZ()-4) + " water_cauldron[level=3]\"}", context.getSource());
                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"setblock " + (platform.getX()+2) + " " + platform.getY() + " " + (platform.getZ()-7) + " water_cauldron[level=3]\"}", context.getSource());
                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"setblock " + (platform.getX()-2) + " " + platform.getY() + " " + (platform.getZ()-10) + " water_cauldron[level=3]\"}", context.getSource());
                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"setblock " + (platform.getX()+2) + " " + platform.getY() + " " + (platform.getZ()-13) + " water_cauldron[level=3]\"}", context.getSource());
                }
                if (vari.contains("D")) {
                    // waterlog ps
                    world.setBlockState(platform.add(-2, 0, -3), Blocks.LAVA_CAULDRON.getDefaultState());
                    world.setBlockState(platform.add(2, 0, -6), Blocks.LAVA_CAULDRON.getDefaultState());
                    world.setBlockState(platform.add(-2, 0, -9), Blocks.LAVA_CAULDRON.getDefaultState());
                    world.setBlockState(platform.add(2, 0, -12), Blocks.LAVA_CAULDRON.getDefaultState());

                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"setblock " + (platform.getX()-2) + " " + platform.getY() + " " + (platform.getZ()-3) + " lava_cauldron\"}", context.getSource());
                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"setblock " + (platform.getX()+2) + " " + platform.getY() + " " + (platform.getZ()-6) + " lava_cauldron\"}", context.getSource());
                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"setblock " + (platform.getX()+2) + " " + platform.getY() + " " + (platform.getZ()-12) + " lava_cauldron\"}", context.getSource());
                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"setblock " + (platform.getX()-2) + " " + platform.getY() + " " + (platform.getZ()-9) + " lava_cauldron\"}", context.getSource());
                }
                if (vari.contains("E")) {
                    // andromeda
                    world.setBlockState(platform.add(0, 3, -3), tierblock);
                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"setblock " + platform.getX() + " " + (platform.getY()+2) + " " + (platform.getZ()-3) + " white_wool\"}", context.getSource());
                }
            }
            if (varindex2 == 5) {
                //GAMEMODE
                if (vari.contains("0")){
                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"gamemode survival @p\"}", context.getSource());
                }
                if (vari.contains("1")){
                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"gamemode creative @p\"}", context.getSource());
                }
                if (vari.contains("2")){
                    cmd=cmd.add(0,0,1);context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + cmd.getX() + " " + cmd.getY() + " " + cmd.getZ() + " chain_command_block[facing=south]{auto:1b,Command:\"gamemode adventure @p\"}", context.getSource());
                }
            }
            varindex2++;

        }

        return 1;
    }

    private static int gen(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        List<String> lines = readTextFile("data.txt");
        Collections.reverse(lines);

        int index = 0;
        context.getSource().getServer().getCommandManager().getDispatcher().execute("gamerule sendCommandFeedback false", context.getSource());
        context.getSource().getServer().getCommandManager().getDispatcher().execute("gamerule commandBlockOutput false", context.getSource());

        BlockPos platform = new BlockPos(0, 0, 0);
        platform = new BlockPos(0, 0, 0);

        for (String line : lines) {
            ServerWorld world = context.getSource().getWorld();

            String[] parts = line.split("\t");
            double subtier = Double.parseDouble(parts[0]);
            String methodname = parts[1];
            String setupid = parts[2];

            BlockState tierblock = Blocks.LIGHT_BLUE_CONCRETE.getDefaultState();
            BlockState tierpane = Blocks.LIGHT_BLUE_STAINED_GLASS_PANE.getDefaultState().with(Properties.EAST, true).with(Properties.WEST, true);
            BlockState tiershulker = Blocks.LIGHT_BLUE_SHULKER_BOX.getDefaultState();
            Item itierblockx = Items.LIGHT_BLUE_WOOL;
            Item itierconcxx = Items.LIGHT_BLUE_CONCRETE_POWDER;
            Item itiercarpet = Items.LIGHT_BLUE_CARPET;
            Item itierpanexx = Items.LIGHT_BLUE_STAINED_GLASS_PANE;
            String itierblockname = "light_blue_wool";

            if (subtier >= 2.0) {
                tierblock = Blocks.YELLOW_CONCRETE.getDefaultState();
                tierpane = Blocks.YELLOW_STAINED_GLASS_PANE.getDefaultState().with(Properties.EAST, true).with(Properties.WEST, true);
                tiershulker = Blocks.YELLOW_SHULKER_BOX.getDefaultState();
                itierblockx = Items.YELLOW_WOOL;
                itierconcxx = Items.YELLOW_CONCRETE_POWDER;
                itiercarpet = Items.YELLOW_CARPET;
                itierpanexx = Items.YELLOW_STAINED_GLASS_PANE;
                itierblockname = "yellow_wool";
                if (subtier >= 3.0) {
                    tierblock = Blocks.ORANGE_CONCRETE.getDefaultState();
                    tierpane = Blocks.ORANGE_STAINED_GLASS_PANE.getDefaultState().with(Properties.EAST, true).with(Properties.WEST, true);
                    tiershulker = Blocks.ORANGE_SHULKER_BOX.getDefaultState();
                    itierblockx = Items.ORANGE_WOOL;
                    itierconcxx = Items.ORANGE_CONCRETE_POWDER;
                    itiercarpet = Items.ORANGE_CARPET;
                    itierpanexx = Items.ORANGE_STAINED_GLASS_PANE;
                    itierblockname = "orange_wool";
                    if (subtier >= 4.0) {
                        tierblock = Blocks.MAGENTA_CONCRETE.getDefaultState();
                        tierpane = Blocks.MAGENTA_STAINED_GLASS_PANE.getDefaultState().with(Properties.EAST, true).with(Properties.WEST, true);
                        tiershulker = Blocks.MAGENTA_SHULKER_BOX.getDefaultState();
                        itierblockx = Items.MAGENTA_WOOL;
                        itierconcxx = Items.MAGENTA_CONCRETE_POWDER;
                        itiercarpet = Items.MAGENTA_CARPET;
                        itierpanexx = Items.MAGENTA_STAINED_GLASS_PANE;
                        itierblockname = "magenta_wool";
                        if (subtier >= 5.0) {
                            tierblock = Blocks.PURPLE_CONCRETE.getDefaultState();
                            tierpane = Blocks.PURPLE_STAINED_GLASS_PANE.getDefaultState().with(Properties.EAST, true).with(Properties.WEST, true);
                            tiershulker = Blocks.PURPLE_SHULKER_BOX.getDefaultState();
                            itierblockx = Items.PURPLE_WOOL;
                            itierconcxx = Items.PURPLE_CONCRETE_POWDER;
                            itiercarpet = Items.PURPLE_CARPET;
                            itierpanexx = Items.PURPLE_STAINED_GLASS_PANE;
                            itierblockname = "purple_wool";
                        }
                    }
                }
            }


            String pasttier = "LT5";
            if (prevtier != null) {
                pasttier = prevtier;
            }

            if (subtier != 5.9) {
                prevtier = "HT1";
                if (subtier >= 1.5) {
                    prevtier = "LT1";
                    if (subtier >= 2.0) {
                        prevtier = "HT2";
                        if (subtier >= 2.5) {
                            prevtier = "LT2";
                            if (subtier >= 3.0) {
                                prevtier = "HT3";
                                if (subtier >= 3.5) {
                                    prevtier = "LT3";
                                    if (subtier >= 4.0) {
                                        prevtier = "HT4";
                                        if (subtier >= 4.5) {
                                            prevtier = "LT4";
                                            if (subtier >= 5.0) {
                                                prevtier = "HT5";
                                                if (subtier >= 5.5) {
                                                    prevtier = "LT5";
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                prevtier = "LT5";
            }
            if (pasttier != prevtier) {
                platform = platform.add(-platform.getX(), 0, -36);
            }

            //region Starting platform
            world.setBlockState(platform.down(), tierblock);

            world.setBlockState(platform.down().north(), Blocks.WHITE_CONCRETE.getDefaultState());
            world.setBlockState(platform.down().east(), Blocks.WHITE_CONCRETE.getDefaultState());
            world.setBlockState(platform.down().south(), Blocks.WHITE_CONCRETE.getDefaultState());
            world.setBlockState(platform.down().west(), Blocks.WHITE_CONCRETE.getDefaultState());
            world.setBlockState(platform.down().north().east(), Blocks.WHITE_CONCRETE.getDefaultState());
            world.setBlockState(platform.down().north().west(), Blocks.WHITE_CONCRETE.getDefaultState());
            world.setBlockState(platform.down().south().east(), Blocks.WHITE_CONCRETE.getDefaultState());
            world.setBlockState(platform.down().south().west(), Blocks.WHITE_CONCRETE.getDefaultState());

            world.setBlockState(platform.down().north().north(), tierblock);
            world.setBlockState(platform.down().north().north().east(), tierblock);
            world.setBlockState(platform.down().north().north().east().east(), tierblock);
            world.setBlockState(platform.down().north().north().west(), tierblock);
            world.setBlockState(platform.down().north().north().west().west(), tierblock);
            world.setBlockState(platform.down().south().south(), tierblock);
            world.setBlockState(platform.down().south().south().east(), tierblock);
            world.setBlockState(platform.down().south().south().east().east(), tierblock);
            world.setBlockState(platform.down().south().south().west(), tierblock);
            world.setBlockState(platform.down().south().south().west().west(), tierblock);
            world.setBlockState(platform.down().east().east(), tierblock);
            world.setBlockState(platform.down().east().east().north(), tierblock);
            world.setBlockState(platform.down().east().east().south(), tierblock);
            world.setBlockState(platform.down().west().west(), tierblock);
            world.setBlockState(platform.down().west().west().north(), tierblock);
            world.setBlockState(platform.down().west().west().south(), tierblock);

            world.setBlockState(platform.down().down().north().north(), tierblock);
            world.setBlockState(platform.down().down().north().north().east(), tierblock);
            world.setBlockState(platform.down().down().north().north().east().east(), tierblock);
            world.setBlockState(platform.down().down().north().north().west(), tierblock);
            world.setBlockState(platform.down().down().north().north().west().west(), tierblock);
            world.setBlockState(platform.down().down().south().south(), tierpane);
            world.setBlockState(platform.down().down().south().south().east(), tierpane);
            world.setBlockState(platform.down().down().south().south().east().east(), tierblock);
            world.setBlockState(platform.down().down().south().south().west(), tierpane);
            world.setBlockState(platform.down().down().south().south().west().west(), tierblock);
            world.setBlockState(platform.down().down().east().east(), tierblock);
            world.setBlockState(platform.down().down().east().east().north(), tierblock);
            world.setBlockState(platform.down().down().east().east().south(), tierblock);
            world.setBlockState(platform.down().down().west().west(), tierblock);
            world.setBlockState(platform.down().down().west().west().north(), tierblock);
            world.setBlockState(platform.down().down().west().west().south(), tierblock);
            context.getSource().getServer().getCommandManager().getDispatcher().execute("setblock " + (platform.getX()-1) + " " + (platform.getY()-2) + " " + (platform.getZ()+1) + " repeating_command_block[facing=south]{auto:1b,Command:\"function fruitbridge:platform\"}", context.getSource());


            world.setBlockState(platform.down().down().down(), tierblock);
            world.setBlockState(platform.down().down().down().north(), Blocks.WHITE_CONCRETE.getDefaultState());
            world.setBlockState(platform.down().down().down().east(), Blocks.WHITE_CONCRETE.getDefaultState());
            world.setBlockState(platform.down().down().down().south(), Blocks.WHITE_CONCRETE.getDefaultState());
            world.setBlockState(platform.down().down().down().west(), Blocks.WHITE_CONCRETE.getDefaultState());
            world.setBlockState(platform.down().down().down().north().east(), Blocks.WHITE_CONCRETE.getDefaultState());
            world.setBlockState(platform.down().down().down().north().west(), Blocks.WHITE_CONCRETE.getDefaultState());
            world.setBlockState(platform.down().down().down().south().east(), Blocks.WHITE_CONCRETE.getDefaultState());
            world.setBlockState(platform.down().down().down().south().west(), Blocks.WHITE_CONCRETE.getDefaultState());
            world.setBlockState(platform.down().down().down().north().north(), tierblock);
            world.setBlockState(platform.down().down().down().north().north().east(), tierblock);
            world.setBlockState(platform.down().down().down().north().north().east().east(), tierblock);
            world.setBlockState(platform.down().down().down().north().north().west(), tierblock);
            world.setBlockState(platform.down().down().down().north().north().west().west(), tierblock);
            world.setBlockState(platform.down().down().down().south().south(), tierblock);
            world.setBlockState(platform.down().down().down().south().south().east(), tierblock);
            world.setBlockState(platform.down().down().down().south().south().east().east(), tierblock);
            world.setBlockState(platform.down().down().down().south().south().west(), tierblock);
            world.setBlockState(platform.down().down().down().south().south().west().west(), tierblock);
            world.setBlockState(platform.down().down().down().east().east(), tierblock);
            world.setBlockState(platform.down().down().down().east().east().north(), tierblock);
            world.setBlockState(platform.down().down().down().east().east().south(), tierblock);
            world.setBlockState(platform.down().down().down().west().west(), tierblock);
            world.setBlockState(platform.down().down().down().west().west().north(), tierblock);
            world.setBlockState(platform.down().down().down().west().west().south(), tierblock);


            //endregion

            world.setBlockState(platform.down().down().south(), tiershulker.with(Properties.FACING, Direction.SOUTH));
            world.setBlockState(platform.down().down().south().east(), tiershulker.with(Properties.FACING, Direction.SOUTH));
            ShulkerBoxBlockEntity shulker1 = (ShulkerBoxBlockEntity) world.getBlockEntity(platform.down().down().south());
            ShulkerBoxBlockEntity shulker2 = (ShulkerBoxBlockEntity) world.getBlockEntity(platform.down().down().south().east());


            String[] idparts = setupid.split("-");
            String itemstr = idparts[0];
            String varstr = idparts[1];


            // ITEM READER
            String[] itemlist = itemstr.split("S");
            for (String item : itemlist) {

                if (Character.isDigit(item.charAt(0))) {
                    // reg slot
                    String[] splitproc1 = item.split("(?<=\\d)(?=\\D)", 2);

                    int itemslot = Integer.parseInt(splitproc1[0]);
                    String[] splitproc2 = splitproc1[1].split("(?=\\d+$)", 2);
                    String itemname = splitproc2[0];

                    int itemcount = Integer.parseInt(splitproc2[1]);


                    if (itemslot >= 0 && itemslot < shulker1.size()) {


                        int damage = 0;

                        if (itemname.contains("D")) {
                            String[] duraitem = itemname.split("D");
                            damage = Integer.parseInt(duraitem[1]);
                            itemname = duraitem[0];
                        }


                        Identifier itemId = Identifier.tryParse("minecraft", itemname);

                        Item regitem = Registries.ITEM.get(itemId);

                        if (itemname.equals("b")) {
                            regitem = itierblockx;
                        }
                        if (itemname.equals("c")) {
                            regitem = itierconcxx;
                        }
                        if (itemname.equals("p")) {
                            regitem = itierpanexx;
                        }
                        if (itemname.equals("k")) {
                            regitem = itiercarpet;
                        }
                        int shulkeritems = 0;
                        if (itemname.equals("ishulker_box")) {
                            regitem = Items.SHULKER_BOX;
                            shulkeritems=1;
                        }
                        if (itemname.equals("iishulker_box")) {
                            regitem = Items.SHULKER_BOX;
                            shulkeritems=2;
                        }

                        int bundlestate = 0;
                        if (itemname.equals("cbundle")) {
                            regitem = Items.BUNDLE;
                            bundlestate = 1;
                        }
                        if (itemname.equals("tbundle")) {
                            regitem = Items.BUNDLE;
                            bundlestate = 2;
                        }


                        if (item != null) {
                            ItemStack itemstack = new ItemStack(regitem, itemcount);

                            if (damage != 0) {
                                itemstack.setDamage(itemstack.getMaxDamage() - damage);
                            }


                            if (itemstack.getItem().equals(Items.CROSSBOW)) {
                                itemstack.set(DataComponentTypes.CHARGED_PROJECTILES, ChargedProjectilesComponent.of(new ItemStack(Items.ARROW)));
                            }
                            if (itemstack.getItem().equals(Items.SHULKER_BOX)) {
                                List<ItemStack> stacks = new ArrayList<>(27);
                                for (int i = 0; i < 27; i++) {
                                    stacks.add(ItemStack.EMPTY);
                                }
                                stacks.set(22, new ItemStack(itierblockx, shulkeritems));
                                itemstack.set(DataComponentTypes.CONTAINER, ContainerComponent.fromStacks(stacks));
                            }
                            if (itemstack.getItem().equals(Items.BUNDLE)) {
                                List<ItemStack> stacks = new ArrayList<>(27);
                                if (bundlestate == 1){
                                    stacks.add(new ItemStack(Items.ORANGE_WOOL, 3));
                                    stacks.add(new ItemStack(Items.YELLOW_WOOL, 3));
                                    stacks.add(new ItemStack(Items.LIME_WOOL, 3));
                                    stacks.add(new ItemStack(Items.BLUE_WOOL, 1));
                                }
                                if (bundlestate == 2){
                                    stacks.add(new ItemStack(Items.ORANGE_WOOL, 2));
                                    stacks.add(new ItemStack(Items.YELLOW_WOOL, 2));
                                    stacks.add(new ItemStack(Items.LIME_WOOL, 2));
                                    stacks.add(new ItemStack(Items.BLUE_WOOL, 2));
                                    stacks.add(new ItemStack(Items.PURPLE_WOOL, 2));
                                    stacks.add(new ItemStack(Items.MAGENTA_WOOL, 1));
                                }
                                itemstack.set(DataComponentTypes.BUNDLE_CONTENTS, new BundleContentsComponent(stacks));
                            }
                            if (itemstack.getItem().equals(Items.IRON_BOOTS)) {
                                RegistryEntry<Enchantment> ench = context.getSource().getServer().getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(Enchantments.SOUL_SPEED).get();
                                itemstack.addEnchantment(ench, 3);
                            }
                            if (itemstack.getItem().equals(Items.GOLDEN_BOOTS)) {
                                RegistryEntry<Enchantment> ench = context.getSource().getServer().getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(Enchantments.FROST_WALKER).get();
                                itemstack.addEnchantment(ench, 2);
                            }
                            if (itemstack.getItem() instanceof ToolItem) {
                                RegistryEntry<Enchantment> ench = context.getSource().getServer().getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(Enchantments.EFFICIENCY).get();
                                itemstack.addEnchantment(ench, 5);
                            }

                            shulker1.setStack(itemslot, itemstack);

                        }

                        shulker1.markDirty();
                        world.updateListeners(platform.down().down().south(), world.getBlockState(platform.down().down().south()), world.getBlockState(platform.down().down().south()), 3);


                    } else {


                        int damage = 0;

                        if (itemname.contains("D")) {
                            String[] duraitem = itemname.split("D");
                            damage = Integer.parseInt(duraitem[1]);
                            itemname = duraitem[0];
                        }

                        Identifier itemId = Identifier.tryParse("minecraft", itemname);

                        Item regitem = Registries.ITEM.get(itemId);

                        if (itemname.equals("b")) {
                            regitem = itierblockx;
                        }
                        if (itemname.equals("c")) {
                            regitem = itierconcxx;
                        }
                        if (itemname.equals("p")) {
                            regitem = itierpanexx;
                        }
                        if (itemname.equals("k")) {
                            regitem = itiercarpet;
                        }
                        int shulkeritems = 0;
                        if (itemname.equals("ishulker_box")) {
                            regitem = Items.SHULKER_BOX;
                            shulkeritems=1;
                        }
                        if (itemname.equals("iishulker_box")) {
                            regitem = Items.SHULKER_BOX;
                            shulkeritems=2;
                        }

                        int bundlestate = 0;
                        if (itemname.equals("cbundle")) {
                            regitem = Items.BUNDLE;
                            bundlestate = 1;
                        }
                        if (itemname.equals("tbundle")) {
                            regitem = Items.BUNDLE;
                            bundlestate = 2;
                        }


                        if (item != null) {
                            ItemStack itemstack = new ItemStack(regitem, itemcount);

                            if (damage != 0) {
                                itemstack.setDamage(itemstack.getMaxDamage() - damage);
                            }


                            if (itemstack.getItem().equals(Items.CROSSBOW)) {
                                itemstack.set(DataComponentTypes.CHARGED_PROJECTILES, ChargedProjectilesComponent.of(new ItemStack(Items.ARROW)));
                            }
                            if (itemstack.getItem().equals(Items.SHULKER_BOX)) {
                                List<ItemStack> stacks = new ArrayList<>(27);
                                for (int i = 0; i < 27; i++) {
                                    stacks.add(ItemStack.EMPTY);
                                }
                                stacks.set(22, new ItemStack(itierblockx, shulkeritems));
                                itemstack.set(DataComponentTypes.CONTAINER, ContainerComponent.fromStacks(stacks));
                            }
                            if (itemstack.getItem().equals(Items.BUNDLE)) {
                                List<ItemStack> stacks = new ArrayList<>(27);
                                if (bundlestate == 1){
                                    stacks.add(new ItemStack(Items.ORANGE_WOOL, 3));
                                    stacks.add(new ItemStack(Items.YELLOW_WOOL, 3));
                                    stacks.add(new ItemStack(Items.LIME_WOOL, 3));
                                    stacks.add(new ItemStack(Items.BLUE_WOOL, 1));
                                }
                                if (bundlestate == 2){
                                    stacks.add(new ItemStack(Items.ORANGE_WOOL, 2));
                                    stacks.add(new ItemStack(Items.YELLOW_WOOL, 2));
                                    stacks.add(new ItemStack(Items.LIME_WOOL, 2));
                                    stacks.add(new ItemStack(Items.BLUE_WOOL, 2));
                                    stacks.add(new ItemStack(Items.PURPLE_WOOL, 2));
                                    stacks.add(new ItemStack(Items.MAGENTA_WOOL, 1));
                                }
                                itemstack.set(DataComponentTypes.BUNDLE_CONTENTS, new BundleContentsComponent(stacks));
                            }
                            if (itemstack.getItem().equals(Items.IRON_BOOTS)) {
                                RegistryEntry<Enchantment> ench = context.getSource().getServer().getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(Enchantments.SOUL_SPEED).get();
                                itemstack.addEnchantment(ench, 3);
                            }
                            if (itemstack.getItem().equals(Items.GOLDEN_BOOTS)) {
                                RegistryEntry<Enchantment> ench = context.getSource().getServer().getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(Enchantments.FROST_WALKER).get();
                                itemstack.addEnchantment(ench, 2);
                            }
                            if (itemstack.getItem() instanceof ToolItem) {
                                RegistryEntry<Enchantment> ench = context.getSource().getServer().getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(Enchantments.EFFICIENCY).get();
                                itemstack.addEnchantment(ench, 5);
                            }

                            shulker2.setStack(itemslot - 27, itemstack);

                        }

                        shulker2.markDirty();
                        world.updateListeners(platform.down().down().south().east(), world.getBlockState(platform.down().down().south().east()), world.getBlockState(platform.down().down().south().east()), 3);

                    }

                } else {
                    // offhand/armor


                    String itemslot = item.substring(0, 1);

                    String splitproc1 = item.substring(1);
                    String[] splitproc2 = splitproc1.split("(?=\\d+$)", 2);
                    String itemname = splitproc2[0];
                    int itemcount = Integer.parseInt(splitproc2[1]);

                    int shulkerslot = 9;

                    if (itemslot.contains("h")) {
                        shulkerslot = 10;
                    }
                    if (itemslot.contains("c")) {
                        shulkerslot = 11;
                    }
                    if (itemslot.contains("l")) {
                        shulkerslot = 12;
                    }
                    if (itemslot.contains("f")) {
                        shulkerslot = 13;
                    }

                    int damage = 0;

                    if (itemname.contains("D")) {
                        String[] duraitem = itemname.split("D");
                        damage = Integer.parseInt(duraitem[1]);
                        itemname = duraitem[0];
                    }

                    Identifier itemId = Identifier.tryParse("minecraft", itemname);

                    Item regitem = Registries.ITEM.get(itemId);

                    if (itemname.equals("b")) {
                        regitem = itierblockx;
                    }
                    if (itemname.equals("c")) {
                        regitem = itierconcxx;
                    }
                    if (itemname.equals("p")) {
                        regitem = itierpanexx;
                    }
                    if (itemname.equals("k")) {
                        regitem = itiercarpet;
                    }
                    int shulkeritems = 0;
                    if (itemname.equals("ishulker_box")) {
                        regitem = Items.SHULKER_BOX;
                        shulkeritems=1;
                    }
                    if (itemname.equals("iishulker_box")) {
                        regitem = Items.SHULKER_BOX;
                        shulkeritems=2;
                    }

                    int bundlestate = 0;
                    if (itemname.equals("cbundle")) {
                        regitem = Items.BUNDLE;
                        bundlestate = 1;
                    }
                    if (itemname.equals("tbundle")) {
                        regitem = Items.BUNDLE;
                        bundlestate = 2;
                    }



                    if (item != null) {
                        ItemStack itemstack = new ItemStack(regitem, itemcount);

                        if (damage != 0) {
                            itemstack.setDamage(itemstack.getMaxDamage() - damage);
                        }


                        if (itemstack.getItem().equals(Items.CROSSBOW)) {
                            itemstack.set(DataComponentTypes.CHARGED_PROJECTILES, ChargedProjectilesComponent.of(new ItemStack(Items.ARROW)));
                        }
                        if (itemstack.getItem().equals(Items.SHULKER_BOX)) {
                            List<ItemStack> stacks = new ArrayList<>(27);
                            for (int i = 0; i < 27; i++) {
                                stacks.add(ItemStack.EMPTY);
                            }
                            stacks.set(22, new ItemStack(itierblockx, shulkeritems));
                            itemstack.set(DataComponentTypes.CONTAINER, ContainerComponent.fromStacks(stacks));
                        }
                        if (itemstack.getItem().equals(Items.BUNDLE)) {
                            List<ItemStack> stacks = new ArrayList<>(27);
                            if (bundlestate == 1){
                                stacks.add(new ItemStack(Items.ORANGE_WOOL, 3));
                                stacks.add(new ItemStack(Items.YELLOW_WOOL, 3));
                                stacks.add(new ItemStack(Items.LIME_WOOL, 3));
                                stacks.add(new ItemStack(Items.BLUE_WOOL, 1));
                            }
                            if (bundlestate == 2){
                                stacks.add(new ItemStack(Items.ORANGE_WOOL, 2));
                                stacks.add(new ItemStack(Items.YELLOW_WOOL, 2));
                                stacks.add(new ItemStack(Items.LIME_WOOL, 2));
                                stacks.add(new ItemStack(Items.BLUE_WOOL, 2));
                                stacks.add(new ItemStack(Items.PURPLE_WOOL, 2));
                                stacks.add(new ItemStack(Items.MAGENTA_WOOL, 1));
                            }
                            itemstack.set(DataComponentTypes.BUNDLE_CONTENTS, new BundleContentsComponent(stacks));
                        }
                        if (itemstack.getItem().equals(Items.IRON_BOOTS)) {
                            RegistryEntry<Enchantment> ench = context.getSource().getServer().getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(Enchantments.SOUL_SPEED).get();
                            itemstack.addEnchantment(ench, 3);
                        }
                        if (itemstack.getItem().equals(Items.GOLDEN_BOOTS)) {
                            RegistryEntry<Enchantment> ench = context.getSource().getServer().getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(Enchantments.FROST_WALKER).get();
                            itemstack.addEnchantment(ench, 2);
                        }
                        if (itemstack.getItem() instanceof ToolItem) {
                            RegistryEntry<Enchantment> ench = context.getSource().getServer().getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(Enchantments.EFFICIENCY).get();
                            itemstack.addEnchantment(ench, 5);
                        }

                        shulker2.setStack(shulkerslot, itemstack);

                    }

                    shulker2.markDirty();
                    world.updateListeners(platform.down().down().south().east(), world.getBlockState(platform.down().down().south().east()), world.getBlockState(platform.down().down().south().east()), 3);

                }


            }

            // VAR READER
            varstr = String.format("%6s", varstr).replace(' ', '0');

            String vari = "";
            int varindex2 = 0;
            BlockState varblock = Blocks.WHITE_WOOL.getDefaultState();
            int platformelevation = 0;
            for (int varindex = 0; varindex < 6; varindex++) {
                vari = String.valueOf(varstr.charAt(varindex2));

                if (vari.contains("0")) {
                    varblock = Blocks.WHITE_WOOL.getDefaultState();
                }
                if (vari.contains("1")) {
                    varblock = Blocks.LIGHT_GRAY_WOOL.getDefaultState();
                }
                if (vari.contains("2")) {
                    varblock = Blocks.GRAY_WOOL.getDefaultState();
                }
                if (vari.contains("3")) {
                    varblock = Blocks.BLACK_WOOL.getDefaultState();
                }
                if (vari.contains("4")) {
                    varblock = Blocks.BROWN_WOOL.getDefaultState();
                }
                if (vari.contains("5")) {
                    varblock = Blocks.RED_WOOL.getDefaultState();
                }
                if (vari.contains("6")) {
                    varblock = Blocks.ORANGE_WOOL.getDefaultState();
                }
                if (vari.contains("7")) {
                    varblock = Blocks.YELLOW_WOOL.getDefaultState();
                }
                if (vari.contains("8")) {
                    varblock = Blocks.LIME_WOOL.getDefaultState();
                }
                if (vari.contains("9")) {
                    varblock = Blocks.GREEN_WOOL.getDefaultState();
                }
                if (vari.contains("A")) {
                    varblock = Blocks.CYAN_WOOL.getDefaultState();
                }
                if (vari.contains("B")) {
                    varblock = Blocks.LIGHT_BLUE_WOOL.getDefaultState();
                }
                if (vari.contains("C")) {
                    varblock = Blocks.BLUE_WOOL.getDefaultState();
                }
                if (vari.contains("D")) {
                    varblock = Blocks.PURPLE_WOOL.getDefaultState();
                }
                if (vari.contains("E")) {
                    varblock = Blocks.MAGENTA_WOOL.getDefaultState();
                }
                if (vari.contains("F")) {
                    varblock = Blocks.PINK_WOOL.getDefaultState();
                }
                if (varindex2 == 0) {
                    //TIME
                    world.setBlockState(platform.down().down().north().west(), varblock);
                }
                if (varindex2 == 1) {
                    //STARTING BLOCK
                    //0 = no starting block, 1 = jukebox, 2 = glass, 3 = waterlogged leaves, 4 = ps, 5 = stairs, 6 = redstone block, 7 = target, 8 = moss block, 9 = oak planks, A = trappedchest, B = sponge, C = forward waterlogged mangrove roots
                    world.setBlockState(platform.down().down().north(), varblock);
                    if (vari.contains("1")) {
                        world.setBlockState(platform.north().north().down(), Blocks.JUKEBOX.getDefaultState());
                    }
                    if (vari.contains("2")) {
                        world.setBlockState(platform.north().north().down(), Blocks.GLASS.getDefaultState());
                    }
                    if (vari.contains("3")) {
                        world.setBlockState(platform.north().north().down(), Blocks.AZALEA_LEAVES.getDefaultState().with(Properties.WATERLOGGED, true).with(Properties.PERSISTENT, true));
                    }
                    if (vari.contains("4")) {
                        world.setBlockState(platform.north().north().down(), Blocks.POWDER_SNOW.getDefaultState());
                    }
                    if (vari.contains("5")) {
                        world.setBlockState(platform.north().north().down(), Blocks.OAK_SLAB.getDefaultState()/*.with(Properties.FACING, Direction.SOUTH)*/);
                    }
                    if (vari.contains("6")) {
                        world.setBlockState(platform.north().north().down(), Blocks.REDSTONE_BLOCK.getDefaultState());
                    }
                    if (vari.contains("7")) {
                        world.setBlockState(platform.north().north().down(), Blocks.TARGET.getDefaultState());
                    }
                    if (vari.contains("8")) {
                        world.setBlockState(platform.north().north().down(), Blocks.MOSS_BLOCK.getDefaultState());
                    }
                    if (vari.contains("9")) {
                        world.setBlockState(platform.north().north().down(), Blocks.OAK_PLANKS.getDefaultState());
                    }
                    if (vari.contains("A")) {
                        world.setBlockState(platform.north().north().down(), Blocks.TRAPPED_CHEST.getDefaultState());
                    }
                    if (vari.contains("B")) {
                        world.setBlockState(platform.north().north().down(), Blocks.SPONGE.getDefaultState());
                    }
                    if (vari.contains("C")) {
                        world.setBlockState(platform.north().down(), Blocks.MANGROVE_ROOTS.getDefaultState().with(Properties.WATERLOGGED, true));
                    }
                    if (vari.contains("D")) {
                        world.setBlockState(platform.north().north().down(), Blocks.JUNGLE_LOG.getDefaultState());
                    }
                    if (vari.contains("E")) {
                        world.setBlockState(platform.north().north().down(), Blocks.SCULK_SENSOR.getDefaultState());
                    }

                }
                if (varindex2 == 2) {
                    //EFFECTS
                    world.setBlockState(platform.down().down().north().east(), varblock);
                }
                if (varindex2 == 3) {
                    //ELEVATION
                    world.setBlockState(platform.down().down().west(), varblock);
                    if (vari.contains("1")) { /*daybridge*/
                        platformelevation = 4;
                    }
                    if (vari.contains("2")) { /*quasi*/
                        platformelevation = 6;
                    }
                    if (vari.contains("3")) { /*door daybridge*/
                        platformelevation = 8;
                    }

                }
                if (varindex2 == 4) {
                    //VAR
                    world.setBlockState(platform.down().down(), varblock);

                    // FORWARD IS NEG Z
                    // RIGHT IS POS X
                    if (vari.contains("1")) {
                        // classical lava
                        world.setBlockState(platform.add(-2, 0, -4), Blocks.LAVA_CAULDRON.getDefaultState());
                        world.setBlockState(platform.add(2, 0, -7), Blocks.LAVA_CAULDRON.getDefaultState());
                        world.setBlockState(platform.add(-2, 0, -10), Blocks.LAVA_CAULDRON.getDefaultState());
                        world.setBlockState(platform.add(2, 0, -13), Blocks.LAVA_CAULDRON.getDefaultState());
                    }
                    if (vari.contains("2")) {
                        // classical water
                        world.setBlockState(platform.add(-2, 0, -4), Blocks.WATER_CAULDRON.getDefaultState().with(Properties.LEVEL_3, 3));
                        world.setBlockState(platform.add(2, 0, -7), Blocks.WATER_CAULDRON.getDefaultState().with(Properties.LEVEL_3, 3));
                        world.setBlockState(platform.add(-2, 0, -10), Blocks.WATER_CAULDRON.getDefaultState().with(Properties.LEVEL_3, 3));
                        world.setBlockState(platform.add(2, 0, -13), Blocks.WATER_CAULDRON.getDefaultState().with(Properties.LEVEL_3, 3));
                    }
                    if (vari.contains("3")) {
                        // true water
                        world.setBlockState(platform.add(-2, 0, -3), Blocks.WATER_CAULDRON.getDefaultState().with(Properties.LEVEL_3, 3));
                        world.setBlockState(platform.add(2, 0, -5), Blocks.WATER_CAULDRON.getDefaultState().with(Properties.LEVEL_3, 3));
                        world.setBlockState(platform.add(-2, 0, -7), Blocks.WATER_CAULDRON.getDefaultState().with(Properties.LEVEL_3, 3));
                        world.setBlockState(platform.add(2, 0, -9), Blocks.WATER_CAULDRON.getDefaultState().with(Properties.LEVEL_3, 3));
                        world.setBlockState(platform.add(-2, 0, -11), Blocks.WATER_CAULDRON.getDefaultState().with(Properties.LEVEL_3, 3));
                        world.setBlockState(platform.add(2, 0, -13), Blocks.WATER_CAULDRON.getDefaultState().with(Properties.LEVEL_3, 3));
                    }
                    if (vari.contains("4")) {
                        // classical chest
                        world.setBlockState(platform.add(-2, 0, -4), Blocks.CHEST.getDefaultState());
                        ((ChestBlockEntity) world.getBlockEntity(platform.add(-2, 0, -4))).setStack(22, new ItemStack(itierblockx, 3));
                        world.setBlockState(platform.add(2, 0, -7), Blocks.CHEST.getDefaultState());
                        ((ChestBlockEntity) world.getBlockEntity(platform.add(2, 0, -7))).setStack(22, new ItemStack(itierblockx, 3));
                        world.setBlockState(platform.add(-2, 0, -10), Blocks.CHEST.getDefaultState());
                        ((ChestBlockEntity) world.getBlockEntity(platform.add(-2, 0, -10))).setStack(22, new ItemStack(itierblockx, 3));
                        world.setBlockState(platform.add(2, 0, -13), Blocks.CHEST.getDefaultState());
                        ((ChestBlockEntity) world.getBlockEntity(platform.add(2, 0, -13))).setStack(22, new ItemStack(itierblockx, 1));
                    }
                    if (vari.contains("5")) {
                        // moving piston
                        world.setBlockState(platform.add(0, 0, -2), Blocks.MOVING_PISTON.getDefaultState());
                        world.setBlockState(platform.add(0, 0, -3), Blocks.MOVING_PISTON.getDefaultState());
                        world.setBlockState(platform.add(0, 0, -4), Blocks.MOVING_PISTON.getDefaultState());
                        world.setBlockState(platform.add(0, 0, -5), Blocks.MOVING_PISTON.getDefaultState());
                        world.setBlockState(platform.add(0, 0, -6), Blocks.MOVING_PISTON.getDefaultState());
                        world.setBlockState(platform.add(0, 0, -7), Blocks.MOVING_PISTON.getDefaultState());
                        world.setBlockState(platform.add(0, 0, -8), Blocks.MOVING_PISTON.getDefaultState());
                        world.setBlockState(platform.add(0, 0, -9), Blocks.MOVING_PISTON.getDefaultState());
                        world.setBlockState(platform.add(0, 0, -10), Blocks.MOVING_PISTON.getDefaultState());
                        world.setBlockState(platform.add(0, 0, -11), Blocks.MOVING_PISTON.getDefaultState());
                        world.setBlockState(platform.add(0, 0, -12), Blocks.MOVING_PISTON.getDefaultState());
                        world.setBlockState(platform.add(0, 0, -13), Blocks.MOVING_PISTON.getDefaultState());
                        world.setBlockState(platform.add(0, 0, -14), Blocks.MOVING_PISTON.getDefaultState());
                    }
                    if (vari.contains("6")) {
                        // spleef classical chest
                        world.setBlockState(platform.add(-2, 0, -4), Blocks.CHEST.getDefaultState());
                        world.setBlockState(platform.add(-2, 1, -4), Blocks.SLIME_BLOCK.getDefaultState());
                        ((ChestBlockEntity) world.getBlockEntity(platform.add(-2, 0, -4))).setStack(22, new ItemStack(itierblockx, 3));
                        world.setBlockState(platform.add(2, 0, -7), Blocks.CHEST.getDefaultState());
                        world.setBlockState(platform.add(2, 1, -7), Blocks.SLIME_BLOCK.getDefaultState());
                        ((ChestBlockEntity) world.getBlockEntity(platform.add(2, 0, -7))).setStack(22, new ItemStack(itierblockx, 3));
                        world.setBlockState(platform.add(-2, 0, -10), Blocks.CHEST.getDefaultState());
                        world.setBlockState(platform.add(-2, 1, -10), Blocks.SLIME_BLOCK.getDefaultState());
                        ((ChestBlockEntity) world.getBlockEntity(platform.add(-2, 0, -10))).setStack(22, new ItemStack(itierblockx, 3));
                        world.setBlockState(platform.add(2, 0, -13), Blocks.CHEST.getDefaultState());
                        world.setBlockState(platform.add(2, 1, -13), Blocks.SLIME_BLOCK.getDefaultState());
                        ((ChestBlockEntity) world.getBlockEntity(platform.add(2, 0, -13))).setStack(22, new ItemStack(itierblockx, 1));
                    }
                    if (vari.contains("7")) {
                        // villager
                        context.getSource().getServer().getCommandManager().getDispatcher().execute("summon villager " + (platform.getX()-0.7) + " 0 " + (platform.getZ()-4) + " {NoAI:1b,Rotation:[180F,0F],VillagerData:{profession:\"minecraft:librarian\"},Offers:{Recipes:[{maxUses:1,buy:{id:\"minecraft:emerald\",count:9},sell:{id:\"minecraft:bookshelf\",count:1}},{buy:{id:\"minecraft:paper\",count:24},sell:{id:\"minecraft:emerald\",count:1}}]}}",context.getSource());
                        context.getSource().getServer().getCommandManager().getDispatcher().execute("summon villager " + (platform.getX()+1.7) + " 0 " + (platform.getZ()-7) + " {NoAI:1b,Rotation:[180F,0F],VillagerData:{profession:\"minecraft:librarian\"},Offers:{Recipes:[{maxUses:1,buy:{id:\"minecraft:emerald\",count:9},sell:{id:\"minecraft:bookshelf\",count:1}},{buy:{id:\"minecraft:paper\",count:24},sell:{id:\"minecraft:emerald\",count:1}}]}}",context.getSource());
                        context.getSource().getServer().getCommandManager().getDispatcher().execute("summon villager " + (platform.getX()-0.7) + " 0 " + (platform.getZ()-10) + " {NoAI:1b,Rotation:[180F,0F],VillagerData:{profession:\"minecraft:librarian\"},Offers:{Recipes:[{maxUses:1,buy:{id:\"minecraft:emerald\",count:9},sell:{id:\"minecraft:bookshelf\",count:1}},{buy:{id:\"minecraft:paper\",count:24},sell:{id:\"minecraft:emerald\",count:1}}]}}",context.getSource());
                        context.getSource().getServer().getCommandManager().getDispatcher().execute("summon villager " + (platform.getX()+1.7) + " 0 " + (platform.getZ()-13) + " {NoAI:1b,Rotation:[180F,0F],VillagerData:{profession:\"minecraft:librarian\"},Offers:{Recipes:[{maxUses:1,buy:{id:\"minecraft:emerald\",count:9},sell:{id:\"minecraft:bookshelf\",count:1}},{buy:{id:\"minecraft:paper\",count:24},sell:{id:\"minecraft:emerald\",count:1}}]}}",context.getSource());
                    }
                    if (vari.contains("8")) {
                        // celestial villager
                        context.getSource().getServer().getCommandManager().getDispatcher().execute("summon villager " + (platform.getX()-0.7) + " 0 " + (platform.getZ()-4) + " {NoAI:1b,Rotation:[180F,0F],VillagerData:{profession:\"minecraft:librarian\"},Offers:{Recipes:[{maxUses:1,specialPrice:-6,buy:{id:\"minecraft:emerald\",count:9},sell:{id:\"minecraft:bookshelf\",count:1}},{maxUses:1,specialPrice:-13,buy:{id:\"minecraft:paper\",count:24},sell:{id:\"minecraft:emerald\",count:1}}]}}",context.getSource());
                        context.getSource().getServer().getCommandManager().getDispatcher().execute("summon villager " + (platform.getX()+1.7) + " 0 " + (platform.getZ()-7) + " {NoAI:1b,Rotation:[180F,0F],VillagerData:{profession:\"minecraft:librarian\"},Offers:{Recipes:[{maxUses:1,specialPrice:-6,buy:{id:\"minecraft:emerald\",count:9},sell:{id:\"minecraft:bookshelf\",count:1}},{maxUses:1,specialPrice:-13,buy:{id:\"minecraft:paper\",count:24},sell:{id:\"minecraft:emerald\",count:1}}]}}",context.getSource());
                        context.getSource().getServer().getCommandManager().getDispatcher().execute("summon villager " + (platform.getX()-0.7) + " 0 " + (platform.getZ()-10) + " {NoAI:1b,Rotation:[180F,0F],VillagerData:{profession:\"minecraft:librarian\"},Offers:{Recipes:[{maxUses:1,specialPrice:-6,buy:{id:\"minecraft:emerald\",count:9},sell:{id:\"minecraft:bookshelf\",count:1}},{maxUses:1,specialPrice:-13,buy:{id:\"minecraft:paper\",count:24},sell:{id:\"minecraft:emerald\",count:1}}]}}",context.getSource());
                        context.getSource().getServer().getCommandManager().getDispatcher().execute("summon villager " + (platform.getX()+1.7) + " 0 " + (platform.getZ()-13) + " {NoAI:1b,Rotation:[180F,0F],VillagerData:{profession:\"minecraft:librarian\"},Offers:{Recipes:[{maxUses:1,specialPrice:-6,buy:{id:\"minecraft:emerald\",count:9},sell:{id:\"minecraft:bookshelf\",count:1}},{maxUses:1,specialPrice:-13,buy:{id:\"minecraft:paper\",count:24},sell:{id:\"minecraft:emerald\",count:1}}]}}",context.getSource());

                    }
                    if (vari.contains("9")) {
                        // allay

                        context.getSource().getServer().getCommandManager().getDispatcher().execute("summon allay " + platform.getX() + ".0 0 " + (platform.getZ()-2) + ".0 {NoAI:1b,Rotation:[180F,0F],HandItems:[{id:\"minecraft:" + itierblockname + "\",count:1},{}]}",context.getSource());
                        context.getSource().getServer().getCommandManager().getDispatcher().execute("summon allay " + (platform.getX()+1) + ".0 0 " + (platform.getZ()-3) + ".0 {NoAI:1b,Rotation:[180F,0F],HandItems:[{id:\"minecraft:" + itierblockname + "\",count:1},{}]}",context.getSource());
                        context.getSource().getServer().getCommandManager().getDispatcher().execute("summon allay " + platform.getX() + ".0 0 " + (platform.getZ()-4) + ".0 {NoAI:1b,Rotation:[180F,0F],HandItems:[{id:\"minecraft:" + itierblockname + "\",count:1},{}]}",context.getSource());
                        context.getSource().getServer().getCommandManager().getDispatcher().execute("summon allay " + (platform.getX()+1) + ".0 0 " + (platform.getZ()-5) + ".0 {NoAI:1b,Rotation:[180F,0F],HandItems:[{id:\"minecraft:" + itierblockname + "\",count:1},{}]}",context.getSource());
                        context.getSource().getServer().getCommandManager().getDispatcher().execute("summon allay " + platform.getX() + ".0 0 " + (platform.getZ()-6) + ".0 {NoAI:1b,Rotation:[180F,0F],HandItems:[{id:\"minecraft:" + itierblockname + "\",count:1},{}]}",context.getSource());
                        context.getSource().getServer().getCommandManager().getDispatcher().execute("summon allay " + (platform.getX()+1) + ".0 0 " + (platform.getZ()-7) + ".0 {NoAI:1b,Rotation:[180F,0F],HandItems:[{id:\"minecraft:" + itierblockname + "\",count:1},{}]}",context.getSource());
                        context.getSource().getServer().getCommandManager().getDispatcher().execute("summon allay " + platform.getX() + ".0 0 " + (platform.getZ()-8) + ".0 {NoAI:1b,Rotation:[180F,0F],HandItems:[{id:\"minecraft:" + itierblockname + "\",count:1},{}]}",context.getSource());
                        context.getSource().getServer().getCommandManager().getDispatcher().execute("summon allay " + (platform.getX()+1) + ".0 0 " + (platform.getZ()-9) + ".0 {NoAI:1b,Rotation:[180F,0F],HandItems:[{id:\"minecraft:" + itierblockname + "\",count:1},{}]}",context.getSource());
                        context.getSource().getServer().getCommandManager().getDispatcher().execute("summon allay " + platform.getX() + ".0 0 " + (platform.getZ()-10) + ".0 {NoAI:1b,Rotation:[180F,0F],HandItems:[{id:\"minecraft:" + itierblockname + "\",count:1},{}]}",context.getSource());
                        context.getSource().getServer().getCommandManager().getDispatcher().execute("summon allay " + (platform.getX()+1) + ".0 0 " + (platform.getZ()-11) + ".0 {NoAI:1b,Rotation:[180F,0F],HandItems:[{id:\"minecraft:" + itierblockname + "\",count:1},{}]}",context.getSource());
                        context.getSource().getServer().getCommandManager().getDispatcher().execute("summon allay " + platform.getX() + ".0 0 " + (platform.getZ()-12) + ".0 {NoAI:1b,Rotation:[180F,0F],HandItems:[{id:\"minecraft:" + itierblockname + "\",count:1},{}]}",context.getSource());
                    }
                    if (vari.contains("A")) {
                        // lantern
                        for (int filly = 0; filly < 2; filly++) {
                            for (int fillz = -3; fillz > -15; fillz--) {
                                world.setBlockState(platform.add(0, filly, fillz), Blocks.CHAIN.getDefaultState());
                                world.setBlockState(platform.add(0, filly, fillz), Blocks.CHAIN.getDefaultState());
                                world.setBlockState(platform.add(0, filly, fillz), Blocks.CHAIN.getDefaultState());
                                world.setBlockState(platform.add(0, filly, fillz), Blocks.CHAIN.getDefaultState());
                                world.setBlockState(platform.add(0, filly, fillz), Blocks.CHAIN.getDefaultState());
                                world.setBlockState(platform.add(0, filly, fillz), Blocks.CHAIN.getDefaultState());
                                world.setBlockState(platform.add(0, filly, fillz), Blocks.CHAIN.getDefaultState());
                                world.setBlockState(platform.add(0, filly, fillz), Blocks.CHAIN.getDefaultState());
                                world.setBlockState(platform.add(0, filly, fillz), Blocks.CHAIN.getDefaultState());
                                world.setBlockState(platform.add(0, filly, fillz), Blocks.CHAIN.getDefaultState());
                                world.setBlockState(platform.add(0, filly, fillz), Blocks.CHAIN.getDefaultState());
                                world.setBlockState(platform.add(0, filly, fillz), Blocks.CHAIN.getDefaultState());
                            }
                        }
                    }
                    if (vari.contains("B")) {
                        // ender chest
                        context.getSource().getServer().getCommandManager().getDispatcher().execute("item replace entity @a enderchest.14 with red_wool",context.getSource());
                        context.getSource().getServer().getCommandManager().getDispatcher().execute("item replace entity @a enderchest.12 with orange_wool",context.getSource());
                        context.getSource().getServer().getCommandManager().getDispatcher().execute("item replace entity @a enderchest.13 with yellow_wool",context.getSource());
                        context.getSource().getServer().getCommandManager().getDispatcher().execute("item replace entity @a enderchest.23 with lime_wool",context.getSource());
                        context.getSource().getServer().getCommandManager().getDispatcher().execute("item replace entity @a enderchest.21 with blue_wool",context.getSource());
                        context.getSource().getServer().getCommandManager().getDispatcher().execute("item replace entity @a enderchest.22 with purple_wool",context.getSource());
                    }
                    if (vari.contains("C")) {
                        // butane
                        world.setBlockState(platform.add(2, 0, -4), Blocks.LAVA_CAULDRON.getDefaultState());
                        world.setBlockState(platform.add(-2, 0, -7), Blocks.LAVA_CAULDRON.getDefaultState());
                        world.setBlockState(platform.add(2, 0, -10), Blocks.LAVA_CAULDRON.getDefaultState());
                        world.setBlockState(platform.add(-2, 0, -13), Blocks.LAVA_CAULDRON.getDefaultState());

                        world.setBlockState(platform.add(-2, 0, -4), Blocks.WATER_CAULDRON.getDefaultState().with(Properties.LEVEL_3, 3));
                        world.setBlockState(platform.add(2, 0, -7), Blocks.WATER_CAULDRON.getDefaultState().with(Properties.LEVEL_3, 3));
                        world.setBlockState(platform.add(-2, 0, -10), Blocks.WATER_CAULDRON.getDefaultState().with(Properties.LEVEL_3, 3));
                        world.setBlockState(platform.add(2, 0, -13), Blocks.WATER_CAULDRON.getDefaultState().with(Properties.LEVEL_3, 3));
                    }
                    if (vari.contains("D")) {
                        // waterlog ps
                        world.setBlockState(platform.add(-2, 0, -3), Blocks.LAVA_CAULDRON.getDefaultState());
                        world.setBlockState(platform.add(2, 0, -6), Blocks.LAVA_CAULDRON.getDefaultState());
                        world.setBlockState(platform.add(-2, 0, -9), Blocks.LAVA_CAULDRON.getDefaultState());
                        world.setBlockState(platform.add(2, 0, -12), Blocks.LAVA_CAULDRON.getDefaultState());
                    }
                    if (vari.contains("E")) {
                        // andromeda
                        world.setBlockState(platform.add(0, 3, -3), Blocks.LIGHT_BLUE_WOOL.getDefaultState());
                    }

                }
                if (varindex2 == 5) {
                    //GAMEMODE
                    world.setBlockState(platform.down().down().east(), varblock);
                }
                varindex2++;
            }

            //region Finish platform
            world.setBlockState(platform.add(0, platformelevation, -18).down(), Blocks.GOLD_BLOCK.getDefaultState());

            world.setBlockState(platform.add(0, platformelevation, -18).down().north(), Blocks.GOLD_BLOCK.getDefaultState());
            world.setBlockState(platform.add(0, platformelevation, -18).down().east(), Blocks.GOLD_BLOCK.getDefaultState());
            world.setBlockState(platform.add(0, platformelevation, -18).down().south(), Blocks.GOLD_BLOCK.getDefaultState());
            world.setBlockState(platform.add(0, platformelevation, -18).down().west(), Blocks.GOLD_BLOCK.getDefaultState());
            world.setBlockState(platform.add(0, platformelevation, -18).down().north().east(), Blocks.GOLD_BLOCK.getDefaultState());
            world.setBlockState(platform.add(0, platformelevation, -18).down().north().west(), Blocks.GOLD_BLOCK.getDefaultState());
            world.setBlockState(platform.add(0, platformelevation, -18).down().south().east(), Blocks.GOLD_BLOCK.getDefaultState());
            world.setBlockState(platform.add(0, platformelevation, -18).down().south().west(), Blocks.GOLD_BLOCK.getDefaultState());

            world.setBlockState(platform.add(0, platformelevation, -18).down().north().north(), tierblock);
            world.setBlockState(platform.add(0, platformelevation, -18).down().north().north().east(), tierblock);
            world.setBlockState(platform.add(0, platformelevation, -18).down().north().north().east().east(), tierblock);
            world.setBlockState(platform.add(0, platformelevation, -18).down().north().north().west(), tierblock);
            world.setBlockState(platform.add(0, platformelevation, -18).down().north().north().west().west(), tierblock);
            world.setBlockState(platform.add(0, platformelevation, -18).down().south().south(), tierblock);
            world.setBlockState(platform.add(0, platformelevation, -18).down().south().south().east(), tierblock);
            world.setBlockState(platform.add(0, platformelevation, -18).down().south().south().east().east(), tierblock);
            world.setBlockState(platform.add(0, platformelevation, -18).down().south().south().west(), tierblock);
            world.setBlockState(platform.add(0, platformelevation, -18).down().south().south().west().west(), tierblock);
            world.setBlockState(platform.add(0, platformelevation, -18).down().east().east(), tierblock);
            world.setBlockState(platform.add(0, platformelevation, -18).down().east().east().north(), tierblock);
            world.setBlockState(platform.add(0, platformelevation, -18).down().east().east().south(), tierblock);
            world.setBlockState(platform.add(0, platformelevation, -18).down().west().west(), tierblock);
            world.setBlockState(platform.add(0, platformelevation, -18).down().west().west().north(), tierblock);
            world.setBlockState(platform.add(0, platformelevation, -18).down().west().west().south(), tierblock);

            world.setBlockState(platform.add(0, platformelevation, -18).down().down().north().north(), tierblock);
            world.setBlockState(platform.add(0, platformelevation, -18).down().down().north().north().east(), tierblock);
            world.setBlockState(platform.add(0, platformelevation, -18).down().down().north().north().east().east(), tierblock);
            world.setBlockState(platform.add(0, platformelevation, -18).down().down().north().north().west(), tierblock);
            world.setBlockState(platform.add(0, platformelevation, -18).down().down().north().north().west().west(), tierblock);
            world.setBlockState(platform.add(0, platformelevation, -18).down().down().south().south(), tierblock);
            world.setBlockState(platform.add(0, platformelevation, -18).down().down().south().south().east(), tierblock);
            world.setBlockState(platform.add(0, platformelevation, -18).down().down().south().south().east().east(), tierblock);
            world.setBlockState(platform.add(0, platformelevation, -18).down().down().south().south().west(), tierblock);
            world.setBlockState(platform.add(0, platformelevation, -18).down().down().south().south().west().west(), tierblock);
            world.setBlockState(platform.add(0, platformelevation, -18).down().down().east().east(), tierblock);
            world.setBlockState(platform.add(0, platformelevation, -18).down().down().east().east().north(), tierblock);
            world.setBlockState(platform.add(0, platformelevation, -18).down().down().east().east().south(), tierblock);
            world.setBlockState(platform.add(0, platformelevation, -18).down().down().west().west(), tierblock);
            world.setBlockState(platform.add(0, platformelevation, -18).down().down().west().west().north(), tierblock);
            world.setBlockState(platform.add(0, platformelevation, -18).down().down().west().west().south(), tierblock);

            world.setBlockState(platform.add(0, platformelevation, -18).down().down().down(), Blocks.GOLD_BLOCK.getDefaultState());
            world.setBlockState(platform.add(0, platformelevation, -18).down().down().down().north(), Blocks.GOLD_BLOCK.getDefaultState());
            world.setBlockState(platform.add(0, platformelevation, -18).down().down().down().east(), Blocks.GOLD_BLOCK.getDefaultState());
            world.setBlockState(platform.add(0, platformelevation, -18).down().down().down().south(), Blocks.GOLD_BLOCK.getDefaultState());
            world.setBlockState(platform.add(0, platformelevation, -18).down().down().down().west(), Blocks.GOLD_BLOCK.getDefaultState());
            world.setBlockState(platform.add(0, platformelevation, -18).down().down().down().north().east(), Blocks.GOLD_BLOCK.getDefaultState());
            world.setBlockState(platform.add(0, platformelevation, -18).down().down().down().north().west(), Blocks.GOLD_BLOCK.getDefaultState());
            world.setBlockState(platform.add(0, platformelevation, -18).down().down().down().south().east(), Blocks.GOLD_BLOCK.getDefaultState());
            world.setBlockState(platform.add(0, platformelevation, -18).down().down().down().south().west(), Blocks.GOLD_BLOCK.getDefaultState());
            world.setBlockState(platform.add(0, platformelevation, -18).down().down().down().north().north(), tierblock);
            world.setBlockState(platform.add(0, platformelevation, -18).down().down().down().north().north().east(), tierblock);
            world.setBlockState(platform.add(0, platformelevation, -18).down().down().down().north().north().east().east(), tierblock);
            world.setBlockState(platform.add(0, platformelevation, -18).down().down().down().north().north().west(), tierblock);
            world.setBlockState(platform.add(0, platformelevation, -18).down().down().down().north().north().west().west(), tierblock);
            world.setBlockState(platform.add(0, platformelevation, -18).down().down().down().south().south(), tierblock);
            world.setBlockState(platform.add(0, platformelevation, -18).down().down().down().south().south().east(), tierblock);
            world.setBlockState(platform.add(0, platformelevation, -18).down().down().down().south().south().east().east(), tierblock);
            world.setBlockState(platform.add(0, platformelevation, -18).down().down().down().south().south().west(), tierblock);
            world.setBlockState(platform.add(0, platformelevation, -18).down().down().down().south().south().west().west(), tierblock);
            world.setBlockState(platform.add(0, platformelevation, -18).down().down().down().east().east(), tierblock);
            world.setBlockState(platform.add(0, platformelevation, -18).down().down().down().east().east().north(), tierblock);
            world.setBlockState(platform.add(0, platformelevation, -18).down().down().down().east().east().south(), tierblock);
            world.setBlockState(platform.add(0, platformelevation, -18).down().down().down().west().west(), tierblock);
            world.setBlockState(platform.add(0, platformelevation, -18).down().down().down().west().west().north(), tierblock);
            world.setBlockState(platform.add(0, platformelevation, -18).down().down().down().west().west().south(), tierblock);
            //endregion


            // METHOD LABEL
            DisplayEntity.TextDisplayEntity textDisplay = new DisplayEntity.TextDisplayEntity(EntityType.TEXT_DISPLAY, world);
            textDisplay.setPos(0, -1.5, 0);


            NbtCompound nbtData = new NbtCompound();
            nbtData.putString("text", "{\"text\":\"" + "[" + subtier + "] " + methodname + "\"}");
            nbtData.putFloat("view_range", 2.0f);
            nbtData.putInt("background", 16711680);
            NbtList rotationList = new NbtList();
            rotationList.add(NbtFloat.of(180.0f));
            rotationList.add(NbtFloat.of(-90.0f));
            nbtData.put("Rotation", rotationList);
            textDisplay.readNbt(nbtData);

            world.spawnEntity(textDisplay);
            textDisplay.setPos(platform.getX() + 0.5, platform.getY() + 0.01, platform.getZ() + 2.55);


            platform = platform.add(18, 0, 0);
            index++;



        }
        return 1;
    }
}

