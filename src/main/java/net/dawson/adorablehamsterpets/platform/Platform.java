package net.dawson.adorablehamsterpets.platform;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

/** Fabric Loader access used by shared mod code. */
public final class Platform {
    private Platform() {
    }

    public static boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    public static boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    public static Path getConfigFolder() {
        return FabricLoader.getInstance().getConfigDir();
    }

    public static Path getGameFolder() {
        return FabricLoader.getInstance().getGameDir();
    }

    public static ModInfo getMod(String modId) {
        return FabricLoader.getInstance().getModContainer(modId)
                .map(container -> new ModInfo(container.getMetadata().getVersion().getFriendlyString()))
                .orElseThrow(() -> new IllegalStateException("Mod is not loaded: " + modId));
    }

    public record ModInfo(String version) {
        public String getVersion() {
            return version;
        }
    }
}
