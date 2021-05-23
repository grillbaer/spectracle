package grillbaer.spectracle.spectrum;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

/**
 * Spectrum with a sample line over a range of wavelengths.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class Spectrum {
    private final @NonNull SampleLine sampleLine;
    private final @NonNull WaveLengthCalibration waveLengthCalibration;

    public static Spectrum create(@NonNull SampleLine sampleLine, @NonNull WaveLengthCalibration waveLengthCalibration) {
        return new Spectrum(sampleLine, waveLengthCalibration);
    }

    public int getLength() {
        return this.sampleLine.getLength();
    }

    public float getValueAtIndex(int index) {
        return this.sampleLine.getValue(index);
    }

    public double getNanoMetersAtIndex(int index) {
        return this.waveLengthCalibration.indexToNanoMeters(this.sampleLine.getLength(), index);
    }
}
