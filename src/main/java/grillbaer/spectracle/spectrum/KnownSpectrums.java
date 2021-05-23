package grillbaer.spectracle.spectrum;

import java.util.List;

/**
 * Spectral data about things existing in reality.
 */
public class KnownSpectrums {

    private KnownSpectrums() {
        // no instances
    }

    public static final List<NamedWaveLength> COMMON_WAVELENGTHS =
            List.of(
                    new NamedWaveLength("FL Hg Purple", 405),
                    new NamedWaveLength("FL Hg Bright Blue", 436),
                    new NamedWaveLength("FL Hg Brightest Green", 546),
                    new NamedWaveLength("FL Eu Bright Red-Orange", 612),
                    new NamedWaveLength("Fraunhofer K Ca+", 393),
                    new NamedWaveLength("Fraunhofer H Ca+", 397),
                    new NamedWaveLength("Fraunhofer G Ca/Fe", 431),
                    new NamedWaveLength("Fraunhofer F H", 486),
                    new NamedWaveLength("Fraunhofer b2 Mg", 517),
                    new NamedWaveLength("Fraunhofer b1 Mg", 518),
                    new NamedWaveLength("Fraunhofer E Fe", 527),
                    new NamedWaveLength("Fraunhofer D2 Na", 589),
                    new NamedWaveLength("Fraunhofer D1 Na", 590),
                    new NamedWaveLength("Fraunhofer C H", 656),
                    new NamedWaveLength("Fraunhofer B O₂", 687),
                    new NamedWaveLength("Fraunhofer A O₂", 759)
            );

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
        final var values = new float[length];
        float maxValue = 0f;
        for (int i = 0; i < length; i++) {
            values[i] = (float) plancksRadiationLaw(temperatureKelvin, calibration.indexToNanoMeters(length, i) * 1e-9);
            if (values[i] > maxValue)
                maxValue = values[i];
        }
        for (int i = 0; i < length; i++) {
            values[i] /= maxValue; // NOSONAR: maxValue will not be zero
        }

        return Spectrum.create(new SampleLine(values), calibration);
    }
}
