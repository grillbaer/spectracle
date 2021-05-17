package grillbaer.spectracle;

import grillbaer.spectracle.model.Model;
import grillbaer.spectracle.settings.Settings;
import lombok.Getter;

/**
 * Shared context information for the whole application.
 */
public final class Context {
    @Getter
    private final Model model = new Model();

    @Getter
    private final Settings settings = new Settings();
}
