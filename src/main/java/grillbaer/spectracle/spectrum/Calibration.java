package grillbaer.spectracle.spectrum;

import lombok.*;

/**
 * Calibration mapping between a range from 0.0 to 1.0 and to spectral wavelengths.
 * Immutable.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode
@ToString
public final class Calibration {
    private final @NonNull Point waveLengthCal0;
    private final @NonNull Point waveLengthCal1;

    public static Calibration createDefault() {
        return new Calibration(
                new Point(0.248, 436.),
                new Point(0.627, 612.));
    }

    public static Calibration create(@NonNull Point waveLengthCal0, @NonNull Point waveLengthCal1) {
        return new Calibration(waveLengthCal0, waveLengthCal1);
    }

    public static double indexToRatio(int sampleLength, int index) {
        return (double) index / (sampleLength - 1);
    }

    public static int ratioToIndex(int sampleLength, double ratio) {
        return (int) Math.round(ratio * (sampleLength - 1));
    }

    public double indexToNanoMeters(int sampleLength, int index) {
        return this.waveLengthCal0.getNanoMeters()
                + getDeltaNanoMeters() * ((indexToRatio(sampleLength, index) - this.waveLengthCal0.getRatio()) / getDeltaRatio());
    }

    public int nanoMetersToIndex(int sampleLength, double nanoMeters) {
        return ratioToIndex(sampleLength,
                this.waveLengthCal0.getRatio()
                        + getDeltaRatio() * (nanoMeters - this.waveLengthCal0.getNanoMeters()) / getDeltaNanoMeters());
    }

    private double getDeltaNanoMeters() {
        return this.waveLengthCal1.getNanoMeters() - this.waveLengthCal0.getNanoMeters();
    }

    private double getDeltaRatio() {
        return this.waveLengthCal1.getRatio() - this.waveLengthCal0.getRatio();
    }

    @AllArgsConstructor
    @EqualsAndHashCode
    @Getter
    @ToString
    public static final class Point {
        /**
         * Ratio between begin of sample line at 0.0 and end of sample line at 1.0.
         */
        private final double ratio;
        private final double nanoMeters;
    }
}
