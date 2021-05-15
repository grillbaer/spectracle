package grillbaer.spectracle;

import grillbaer.spectracle.model.Model;
import grillbaer.spectracle.settings.Settings;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shared context information for the whole application.
 */
public final class Context {
    private static final Logger LOG = LoggerFactory.getLogger(Context.class);

    @Getter
    private final Model model = new Model();

    @Getter
    private final Settings settings = new Settings();
}
