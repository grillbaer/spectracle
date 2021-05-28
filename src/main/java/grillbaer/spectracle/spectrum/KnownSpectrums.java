package grillbaer.spectracle.spectrum;

import java.util.ArrayList;
import java.util.List;

/**
 * Spectral data about things existing in reality.
 */
public class KnownSpectrums {

    private KnownSpectrums() {
        // no instances
    }

    /**
     * Main spectral light of common fluorescent tubes.
     *
     * @see <href a="https://commons.wikimedia.org/wiki/File:Fluorescent_lighting_spectrum_peaks_labelled.gif">Wikimedia Commons</href>
     */
    public static final NamedWaveLengthGroup FLUORESCENT_LAMP_WAVELENGTHS =
            new NamedWaveLengthGroup("Fluorescent Lamp", List.of(
                    new NamedWaveLength("Hg Purple", 404.7),
                    new NamedWaveLength("Hg Bright Blue", 435.8),
                    new NamedWaveLength("Tb³⁺ Broad Cyan Peak", 487.5),
                    new NamedWaveLength("Tb³⁺ Bright Green Left", 543),
                    new NamedWaveLength("Hg Brightest Green Right", 546.1),
                    new NamedWaveLength("Eu⁺³ Highest Yellow Peak", 587),
                    new NamedWaveLength("Eu⁺³ Bright Red-Orange", 611),
                    new NamedWaveLength("Eu⁺³ Red", 660),
                    new NamedWaveLength("Eu⁺³ Dark Red", 710),
                    new NamedWaveLength("Ar Infrared", 760)
            ));

    public static final NamedWaveLengthGroup FRAUNHOFER_WAVELENGTHS =
            new NamedWaveLengthGroup("Fraunhofer", List.of(
                    new NamedWaveLength("K Ca+", 393),
                    new NamedWaveLength("H Ca+", 397),
                    new NamedWaveLength("G Ca/Fe", 431),
                    new NamedWaveLength("F H", 486),
                    new NamedWaveLength("b2 Mg", 517),
                    new NamedWaveLength("b1 Mg", 518),
                    new NamedWaveLength("E Fe", 527),
                    new NamedWaveLength("D2 Na", 589),
                    new NamedWaveLength("D1 Na", 590),
                    new NamedWaveLength("C H", 656),
                    new NamedWaveLength("B O₂", 687),
                    new NamedWaveLength("A O₂", 759)
            ));

    private static double plancksRadiationLaw(double temperatureKelvin, double waveLengthMeter) {
        // Planck's law of radiation:
        // power of a certain wavelength radiated by a black body with a certain temperature
        final var c1 = 3.741771e-16;
        final var c2 = 1.438776e-2;
        return (c1 / Math.pow(waveLengthMeter, 5))
                * (1. / (Math.exp(c2 / (waveLengthMeter * temperatureKelvin)) - 1.));
    }

    public static Spectrum blackBodyRadiationSpectrum(int length, double temperatureKelvin, double beginNanoMeters, double endNanoMeters) {
        final var calibration =
                WaveLengthCalibration.create(
                        new WaveLengthCalibration.WaveLengthPoint(0.0, beginNanoMeters),
                        new WaveLengthCalibration.WaveLengthPoint(1.0, endNanoMeters));
        final var values = new double[length];
        double maxValue = 0f;
        for (int i = 0; i < length; i++) {
            values[i] = (float) plancksRadiationLaw(temperatureKelvin, calibration.indexToNanoMeters(length, i) * 1e-9);
            if (values[i] > maxValue)
                maxValue = values[i];
        }
        for (int i = 0; i < length; i++) {
            values[i] /= maxValue; // NOSONAR: maxValue will not be zero
        }

        return Spectrum.create(SampleLine.create(values), calibration);
    }

    public static List<NamedWaveLength> getCommonWaveLengths() {
        final var all = new ArrayList<NamedWaveLength>();
        all.addAll(FLUORESCENT_LAMP_WAVELENGTHS.getWaveLengthList());
        all.addAll(FRAUNHOFER_WAVELENGTHS.getWaveLengthList());

        return all;
    }
}
