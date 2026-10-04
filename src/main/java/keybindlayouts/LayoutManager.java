package keybindlayouts;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class LayoutManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("keybindlayouts.json");

    private static final Path MKB = FabricLoader.getInstance().getConfigDir().resolve("multi-key-bindings.json");
    private static final String MKB_KEY = "__mkb__";

    // layout name -> (keybind id -> key translation key)
    private static Map<String, Map<String, String>> layouts = new LinkedHashMap<>();

    public static void load() {
        try {
            if (Files.exists(FILE)) {
                try (Reader r = Files.newBufferedReader(FILE)) {
                    Map<String, Map<String, String>> data = GSON.fromJson(r,
                            new TypeToken<LinkedHashMap<String, LinkedHashMap<String, String>>>() {}.getType());
                    if (data != null) layouts = data;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void write() {
        try (Writer w = Files.newBufferedWriter(FILE)) {
            GSON.toJson(layouts, w);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Map<String, Map<String, String>> all() {
        return layouts;
    }

    public static void saveCurrent(String name) {
        MinecraftClient mc = MinecraftClient.getInstance();
        Map<String, String> map = new LinkedHashMap<>();
        for (KeyBinding kb : mc.options.allKeys) {
            map.put(kb.getId(), kb.getBoundKeyTranslationKey());
        }
        try {
            if (Files.exists(MKB)) {
                map.put(MKB_KEY, Files.readString(MKB));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        layouts.put(name, map);
        write();
    }

    /** @return true if Multi Key Bindings data was written (game restart needed) */
    public static boolean apply(String name) {
        Map<String, String> map = layouts.get(name);
        if (map == null) return false;
        MinecraftClient mc = MinecraftClient.getInstance();
        for (KeyBinding kb : mc.options.allKeys) {
            String key = map.get(kb.getId());
            if (key != null) {
                kb.setBoundKey(InputUtil.fromTranslationKey(key));
            }
        }
        KeyBinding.updateKeysByCode();
        mc.options.write();

        String mkb = map.get(MKB_KEY);
        if (mkb != null) {
            try {
                Files.writeString(MKB, mkb);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static void delete(String name) {
        layouts.remove(name);
        write();
    }
}
