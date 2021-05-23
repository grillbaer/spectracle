package grillbaer.spectracle.spectrum;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.awt.*;
import java.util.List;
import java.util.Locale;

/**
 * Helpers for formatting and displaying.
 */
public final class Viewing {
    private Viewing() {
        // no instances
    }

    // violet  380-450 nm  270°
    // blue    450-485 nm  240°
    // cyan    485-500 nm  180°
    // green   500-565 nm  120°
    // yellow  565-590 nm   60°
    // orange  590-625 nm   30°
    // red     625-700 nm    0°

    private static final List<WaveLengthColor> SPECTRUM_GRADIENT = List.of(
            new WaveLengthColor(300., 300f, 0.2f),
            new WaveLengthColor(380., 280f, 0.4f),
            new WaveLengthColor(390., 280f, 0.9f),
            new WaveLengthColor(415., 270f, 1f),
            new WaveLengthColor(468., 240f, 1f),
            new WaveLengthColor(492., 180f, 1f),
            new WaveLengthColor(532., 120f, 1f),
            new WaveLengthColor(577., 60f, 1f),
            new WaveLengthColor(607., 30f, 1f),
            new WaveLengthColor(657., 0f, 1f),
            new WaveLengthColor(690., -10f, 0.9f),
            new WaveLengthColor(700., -10f, 0.7f),
            new WaveLengthColor(750., -10f, 0.4f),
            new WaveLengthColor(1200., 0f, 0.2f)
    );

    public static Color colorForWaveLength(double nanoMeter) {
        if (nanoMeter <= SPECTRUM_GRADIENT.get(0).getNanoMeter()) {
            final var begin = SPECTRUM_GRADIENT.get(0);
            return Color.getHSBColor(begin.getHueDegrees() / 360f, 1f, begin.brightness);
        }

        if (nanoMeter >= SPECTRUM_GRADIENT.get(SPECTRUM_GRADIENT.size() - 1).getNanoMeter()) {
            final var end = SPECTRUM_GRADIENT.get(SPECTRUM_GRADIENT.size() - 1);
            return Color.getHSBColor(end.getHueDegrees() / 360f, 1f, end.brightness);
        }

        int beginIndex;
        int endIndex;
        for (beginIndex = 0, endIndex = 1; endIndex < SPECTRUM_GRADIENT.size(); beginIndex++, endIndex++) {
            if (SPECTRUM_GRADIENT.get(beginIndex).getNanoMeter() <= nanoMeter &&
                    nanoMeter < SPECTRUM_GRADIENT.get(endIndex).getNanoMeter()) break;
        }

        final var begin = SPECTRUM_GRADIENT.get(beginIndex);
        final var end = SPECTRUM_GRADIENT.get(endIndex);

        final var deltaBeginNanoMeter = nanoMeter - begin.getNanoMeter();
        final var deltaNanoMeter = end.getNanoMeter() - begin.getNanoMeter();
        final var deltaHueDegrees = end.getHueDegrees() - begin.getHueDegrees();
        final var deltaBrightness = end.getBrightness() - begin.getBrightness();

        final float hueDegrees = (float) (begin.getHueDegrees() + deltaBeginNanoMeter / deltaNanoMeter * deltaHueDegrees);
        final float brightness = (float) (begin.getBrightness() + deltaBeginNanoMeter / deltaNanoMeter * deltaBrightness);

        return Color.getHSBColor(hueDegrees / 360f, 1f, brightness);
    }

    public static String formatWaveLength(double nanoMeters) {
        return String.format(Locale.ROOT, "%.0f", nanoMeters);
    }

    @AllArgsConstructor
    @Getter
    private static final class WaveLengthColor {
        private final double nanoMeter;
        private final float hueDegrees;
        private final float brightness;
    }
}
