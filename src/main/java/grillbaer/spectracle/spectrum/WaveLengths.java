package grillbaer.spectracle.spectrum;

import java.util.Locale;

public class WaveLengths {
    private WaveLengths() {
        // no instances
    }

    public static String format(double nanoMeters) {
        return String.format(Locale.ROOT, "%.1f", nanoMeters);
    }
}
