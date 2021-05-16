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

    public double getBeginNanoMeters() {
        return getNanoMetersAtIndex(0);
    }

    public double getEndNanoMeters() {
        return getNanoMetersAtIndex(sampleLine.getLength() - 1);
    }

    public double getMinNanoMeters() {
        return Math.min(getBeginNanoMeters(), getEndNanoMeters());
    }

    public double getMaxNanoMeters() {
        return Math.max(getBeginNanoMeters(), getEndNanoMeters());
    }

    public double getNanoMeterRange() {
        return Math.abs(getMaxNanoMeters() - getMinNanoMeters());
    }
}
