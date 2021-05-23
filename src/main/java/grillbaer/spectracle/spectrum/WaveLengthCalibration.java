package grillbaer.spectracle.spectrum;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

/**
 * Calibration mapping between a range from 0.0 to 1.0 and spectral wavelengths.
 * Immutable.
 */
@EqualsAndHashCode
@ToString
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, isGetterVisibility = NONE)
public final class WaveLengthCalibration {
    @JsonProperty("waveLengthPoints")
    private final List<WaveLengthPoint> waveLengthPoints = new ArrayList<>();

    private WaveLengthCalibration(@NonNull WaveLengthPoint waveLengthPoint0, @NonNull WaveLengthPoint waveLengthPoint1) {
        this(List.of(waveLengthPoint0, waveLengthPoint1));
    }

    private WaveLengthCalibration(@JsonProperty("waveLengthPoints") List<WaveLengthPoint> waveLengthPoints) {
        this.waveLengthPoints.addAll(waveLengthPoints);
    }

    public static WaveLengthCalibration createDefault() {
        return new WaveLengthCalibration(
                new WaveLengthPoint(0.15234, 393.),
                new WaveLengthPoint(0.72656, 656.));
    }

    public static WaveLengthCalibration create(@NonNull WaveLengthPoint waveLengthPoint0, @NonNull WaveLengthPoint waveLengthPoint1) {
        return new WaveLengthCalibration(waveLengthPoint0, waveLengthPoint1);
    }

    public WaveLengthPoint getWaveLengthPoint0() {
        return this.waveLengthPoints.get(0);
    }

    public WaveLengthPoint getWaveLengthPoint1() {
        return this.waveLengthPoints.get(1);
    }

    public static double indexToRatio(int length, int index) {
        return (double) index / (length - 1);
    }

    public static int ratioToIndex(int length, double ratio) {
        return (int) Math.round(ratio * (length - 1));
    }

    public double indexToNanoMeters(int length, int index) {
        return getWaveLengthPoint0().getNanoMeters()
                + getDeltaNanoMeters() * ((indexToRatio(length, index) - this.getWaveLengthPoint0()
                .getRatio()) / getDeltaRatio());
    }

    public int nanoMetersToIndex(int length, double nanoMeters) {
        return ratioToIndex(length, nanoMetersToRatio(nanoMeters));
    }

    public double nanoMetersToRatio(double nanoMeters) {
        return getWaveLengthPoint0().getRatio()
                + getDeltaRatio() * (nanoMeters - getWaveLengthPoint0().getNanoMeters()) / getDeltaNanoMeters();
    }

    private double getDeltaNanoMeters() {
        return getWaveLengthPoint1().getNanoMeters() - getWaveLengthPoint0().getNanoMeters();
    }

    private double getDeltaRatio() {
        return getWaveLengthPoint1().getRatio() - getWaveLengthPoint0().getRatio();
    }

    public double getBeginNanoMeters() {
        return indexToNanoMeters(2, 0);
    }

    public double getEndNanoMeters() {
        return indexToNanoMeters(2, 1);
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

    @EqualsAndHashCode
    @Getter
    @ToString
    public static final class WaveLengthPoint {
        /**
         * Ratio between begin of sample line at 0.0 and end of sample line at 1.0.
         */
        @JsonProperty("ratio")
        private final double ratio;
        @JsonProperty("nanoMeters")
        private final double nanoMeters;

        public WaveLengthPoint(@JsonProperty("ratio") double ratio, @JsonProperty("nanoMeters") double nanoMeters) {
            this.ratio = ratio;
            this.nanoMeters = nanoMeters;
        }
    }
}
