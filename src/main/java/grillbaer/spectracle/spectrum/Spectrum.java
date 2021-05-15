package grillbaer.spectracle.spectrum;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class Spectrum {
    private final @NonNull SampleLine sampleLine;
    private final @NonNull Calibration calibration;

    public static Spectrum create(@NonNull SampleLine sampleLine, @NonNull Calibration calibration) {
        return new Spectrum(sampleLine, calibration);
    }

    public int getLength() {
        return this.sampleLine.getLength();
    }

    public float getValueAtIndex(int index) {
        return this.sampleLine.getValue(index);
    }

    public double getNanoMetersAtIndex(int index) {
        return this.calibration.indexToNanoMeters(this.sampleLine.getLength(), index);
    }

    public double getMinNanoMeters() {
        return this.calibration.indexToNanoMeters(this.sampleLine.getLength(), 0);
    }

    public double getMaxNanoMeters() {
        return this.calibration.indexToNanoMeters(this.sampleLine.getLength(), sampleLine.getLength() - 1);
    }

    public double getNanoMeterRange() {
        return getMaxNanoMeters() - getMinNanoMeters();
    }
}
