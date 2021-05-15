package grillbaer.spectracle.spectrum;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.awt.*;
import java.util.List;


public final class SpectralColors {

    private SpectralColors() {
        // no instances
    }

    // violet  380-450 nm  270°
    // blue    450-485 nm  240°
    // cyan    485-500 nm  180°
    // green   500-565 nm  120°
    // yellow  565-590 nm   60°
    // orange  590-625 nm   30°
    // red     625-700 nm    0°
    private static final List<WaveLengthColor> GRADIENT = List.of(
            new WaveLengthColor(300f, 300f, 0.1f),
            new WaveLengthColor(380f, 280f, 0.4f),
            new WaveLengthColor(390f, 280f, 0.9f),
            new WaveLengthColor(415f, 270f, 1f),
            new WaveLengthColor(468f, 240f, 1f),
            new WaveLengthColor(492f, 180f, 1f),
            new WaveLengthColor(532f, 120f, 1f),
            new WaveLengthColor(577f, 60f, 1f),
            new WaveLengthColor(607f, 30f, 1f),
            new WaveLengthColor(657f, 0f, 1f),
            new WaveLengthColor(690f, -10f, 0.9f),
            new WaveLengthColor(700f, -10f, 0.4f),
            new WaveLengthColor(1200f, 0f, 0.1f)
    );

    public static Color getColorForWaveLength(float nanoMeter) {
        if (nanoMeter <= GRADIENT.get(0).getNanoMeter()) {
            final var begin = GRADIENT.get(0);
            return Color.getHSBColor(begin.getHueDegrees() / 360f, 1f, begin.brightness);
        }

        if (nanoMeter >= GRADIENT.get(GRADIENT.size() - 1).getNanoMeter()) {
            final var end = GRADIENT.get(GRADIENT.size() - 1);
            return Color.getHSBColor(end.getHueDegrees() / 360f, 1f, end.brightness);
        }

        int beginIndex;
        int endIndex;
        for (beginIndex = 0, endIndex = 1; endIndex < GRADIENT.size(); beginIndex++, endIndex++) {
            if (GRADIENT.get(beginIndex).getNanoMeter() <= nanoMeter &&
                    nanoMeter < GRADIENT.get(endIndex).getNanoMeter()) break;
        }

        final var begin = GRADIENT.get(beginIndex);
        final var end = GRADIENT.get(endIndex);

        final var deltaBeginNanoMeter = nanoMeter - begin.getNanoMeter();
        final var deltaNanoMeter = end.getNanoMeter() - begin.getNanoMeter();
        final var deltaHueDegrees = end.getHueDegrees() - begin.getHueDegrees();
        final var deltaBrightness = end.getBrightness() - begin.getBrightness();

        final float hueDegrees = begin.getHueDegrees() + deltaBeginNanoMeter / deltaNanoMeter * deltaHueDegrees;
        final float brightness = begin.getBrightness() + deltaBeginNanoMeter / deltaNanoMeter * deltaBrightness;

        return Color.getHSBColor(hueDegrees / 360f, 1f, brightness);
    }

    @AllArgsConstructor
    @Getter
    private static final class WaveLengthColor {
        private final float nanoMeter;
        private final float hueDegrees;
        private final float brightness;
    }
}
