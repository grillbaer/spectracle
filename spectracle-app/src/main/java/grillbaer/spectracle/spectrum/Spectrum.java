package grillbaer.spectracle.spectrum;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import static java.lang.Math.*;

/**
 * Spectrum with a sample line of values over a range of wavelengths.
 * Usually, the values are spectral intensities, but for sensitivity corrections they are spectral correction factors.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class Spectrum {
    private final @NonNull SampleLine sampleLine;
    private final @NonNull WaveLengthCalibration calibration;

    public static Spectrum create(@NonNull SampleLine sampleLine, @NonNull WaveLengthCalibration waveLengthCalibration) {
        return new Spectrum(sampleLine, waveLengthCalibration);
    }

    public int getLength() {
        return this.sampleLine.getLength();
    }

    public double getValueAtIndex(int index) {
        return this.sampleLine.getValue(index);
    }

    public double getNanoMetersAtIndex(int index) {
        return this.calibration.indexToNanoMeters(this.sampleLine.getLength(), index);
    }

    /**
     * Value at wavelength, linearly interpolated between next defined values.
     * Values are considered constant outside of defined range.
     */
    public double getValueAtNanoMeters(double nanoMeters) {
        final double fractionalIndex = (getLength() - 1) * this.calibration.nanoMetersToRatio(nanoMeters);
        final int index0 = max(0, min(getLength() - 1, (int) floor(fractionalIndex)));
        final int index1 = max(0, min(getLength() - 1, (int) ceil(fractionalIndex)));
        if (index0 == index1) {
            return getValueAtIndex(index0);
        } else {
            final double value0 = getValueAtIndex(index0);
            final double value1 = getValueAtIndex(index1);

            return (value0 * (index1 - fractionalIndex) + value1 * (fractionalIndex - index0));
        }
    }
}
