package abyssalmc;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class GlobalDataHandler {
    private static final String DATA_KEY = "customData";
    private static final Path GLOBAL_DATA_PATH = Path.of("fruitbridge_data/cache/global_data.nbt");
    private static int pb = 0;

    private static boolean distanceutils = false;
    private static boolean pbhud = true;
    private static boolean distancehud = true;

    private static boolean customplacesounds = false;

    private static boolean autosaves = true;

    // Load data from file
    public static void loadGlobalData() {
        try {
            if (Files.exists(GLOBAL_DATA_PATH)) {
                NbtCompound nbt = NbtIo.read(GLOBAL_DATA_PATH);
                pb = nbt.getInt("customPb");
                distanceutils = nbt.getBoolean("utils");
                pbhud = nbt.getBoolean("centre");
                distancehud = nbt.getBoolean("dist");
                customplacesounds = nbt.getBoolean("cps");
                autosaves = nbt.getBoolean("autosaves");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Save data to file
    public static void saveGlobalData() {
        try {
            Files.createDirectories(GLOBAL_DATA_PATH.getParent()); // Ensure directory exists

            NbtCompound nbt = new NbtCompound();
            nbt.putInt("customPb", pb);
            nbt.putBoolean("utils", distanceutils);
            nbt.putBoolean("centre", pbhud);
            nbt.putBoolean("dist", distancehud);
            nbt.putBoolean("cps", customplacesounds);
            nbt.putBoolean("autosaves", autosaves);

            NbtIo.write(nbt, GLOBAL_DATA_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static int getPb() { return pb; }
    public static boolean getUtils() { return distanceutils; }
    public static boolean getPbHud() { return pbhud; }
    public static boolean getDist() { return distancehud; }
    public static boolean getPlaceSounds() { return customplacesounds; }
    public static boolean getAutosaves() { return autosaves; }

    public static void setPb(int newpb) { pb = newpb; }
    public static void setUtils(boolean utils) { distanceutils = utils; }
    public static void setPbHud(boolean centre) { pbhud = centre; }
    public static void setDist(boolean dist) { distancehud = dist; }
    public static void setPlaceSounds(boolean cbs) { customplacesounds = cbs; }
    public static void setAutosaves(boolean autosave) { autosaves = autosave; }
}