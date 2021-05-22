package grillbaer.spectracle;

import grillbaer.spectracle.model.Model;
import grillbaer.spectracle.model.Settings;
import lombok.Getter;
import net.harawata.appdirs.AppDirsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Shared context information for the whole application.
 */
public final class Context {
    private static final Logger LOG = LoggerFactory.getLogger(Context.class);

    @Getter
    private final Model model = new Model();

    public Context() {
        loadSettings();

        Runtime.getRuntime().addShutdownHook(new Thread("shutdown-settings-writer") {
            @Override
            public void run() {
                storeSettings();
            }
        });
    }

    private void loadSettings() {
        final var file = getSettingsFile();
        try {
            final var settings = Settings.readJson(file);
            model.applySettings(settings);
            LOG.info("Loaded settings from {}", file);
        } catch (FileNotFoundException e) {
            LOG.warn("No settings file {}", file);
        } catch (IOException e) {
            LOG.error("Failed to load settings from {}", file, e);
        }
    }

    private void storeSettings() {
        final var file = getSettingsFile();
        try {
            Files.createDirectories(file.getParent());
            final var settings = model.createSettings();
            settings.writeJson(file);
            LOG.info("Stored settings to {}", file);
        } catch (IOException e) {
            LOG.error("Failed to store settings to {}", file, e);
        }
    }

    private Path getSettingsFile() {
        final var configDir = Paths.get(AppDirsFactory.getInstance()
                .getUserConfigDir("spectracle", "1", "grillbaer"));

        return configDir.resolve("settings.json");
    }
}
