package grillbaer.spectracle.spectrum;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static java.util.Comparator.comparing;

/**
 * Calibration mapping between a range from 0.0 to 1.0 and spectral wavelengths.
 * Immutable.
 */
@EqualsAndHashCode
@ToString
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, isGetterVisibility = NONE)
public final class WaveLengthCalibration {
    @JsonProperty("waveLengthPoints")
    private final List<Point> points = new ArrayList<>(); // ascending by ratio
    private final boolean nanoMetersAscending;

    private WaveLengthCalibration(@JsonProperty("waveLengthPoints") @NonNull Collection<Point> points) {
        if (points.size() < 2)
            throw new IllegalArgumentException("calibration requires at least two points, but "
                    + points.size() + " were passed");

        this.points.addAll(points);
        this.points.sort(comparing(Point::getRatio));
        if (!areRatioAndWaveLengthStrictlyMonotonic(this.points))
            throw new IllegalArgumentException("calibration points must be distinct and bijective");

        this.nanoMetersAscending = this.points.get(0).getNanoMeters() < this.points.get(1).getNanoMeters();
    }

    /**
     * Whether all points have distinct wavelengths and ratios, and both wavelengths and ratios are strictly monotonic in one or the other direction.
     *
     * @param sortedPoints points sorted either by ratio or by wavelength
     */
    public static boolean areRatioAndWaveLengthStrictlyMonotonic(@NonNull List<Point> sortedPoints) {
        boolean nanoMetersAscending = sortedPoints.get(0).getNanoMeters() < sortedPoints.get(1).getNanoMeters();
        boolean ratioAscending = sortedPoints.get(0).getRatio() < sortedPoints.get(1).getRatio();
        for (int i = 1; i < sortedPoints.size(); i++) {
            final var prevPoint = sortedPoints.get(i - 1);
            final var point = sortedPoints.get(i);
            if ((nanoMetersAscending && prevPoint.getNanoMeters() >= point.getNanoMeters())
                    || (!nanoMetersAscending && prevPoint.getNanoMeters() <= point.getNanoMeters())
                    || (ratioAscending && prevPoint.getRatio() >= point.getRatio())
                    || (!ratioAscending && prevPoint.getRatio() <= point.getRatio())) {
                return false;
            }
        }

        return true;
    }

    public static WaveLengthCalibration createDefault() {
        return new WaveLengthCalibration(List.of(
                new Point(0.2, 400.),
                new Point(0.8, 750.)));
    }


    public static WaveLengthCalibration create(@NonNull Collection<Point> calPoints) {
        return new WaveLengthCalibration(calPoints);
    }

    public Point getPoint(int pointIndex) {
        return this.points.get(pointIndex);
    }

    public int getSize() {
        return this.points.size();
    }

    public double ratioToNanoMeters(double ratio) {
        final int pointIndex0 = findPointIndex0ForRatio(ratio);
        final var point0 = getPoint(pointIndex0);
        final var point1 = getPoint(pointIndex0 + 1);

        return point0.getNanoMeters() + (ratio - point0.getRatio()) * getSlope(point0, point1);
    }

    public double nanoMetersToRatio(double nanoMeters) {
        final int pointIndex0 = findPointIndex0ForNanoMeters(nanoMeters);
        final var point0 = getPoint(pointIndex0);
        final var point1 = getPoint(pointIndex0 + 1);

        return point0.getRatio() + (nanoMeters - point0.getNanoMeters()) / getSlope(point0, point1);
    }

    public static double indexToRatio(int length, int index) {
        return (double) index / (length - 1);
    }

    public static int ratioToNextIndex(int length, double ratio) {
        return (int) Math.round(ratio * (length - 1));
    }

    public double indexToNanoMeters(int length, int index) {
        return ratioToNanoMeters(indexToRatio(length, index));
    }

    public int nanoMetersToNextIndex(int length, double nanoMeters) {
        return ratioToNextIndex(length, nanoMetersToRatio(nanoMeters));
    }

    private int findPointIndex0ForRatio(double ratio) {
        int pointIndex0 = 0;
        while (pointIndex0 < getSize() - 2 && getPoint(pointIndex0 + 1).getRatio() < ratio)
            pointIndex0++;

        return pointIndex0;
    }

    private double getSlope(Point point0, Point point1) {
        return (point1.getNanoMeters() - point0.getNanoMeters())
                / (point1.getRatio() - point0.getRatio());
    }

    private int findPointIndex0ForNanoMeters(double nanoMeters) {
        int pointIndex0 = 0;
        if (this.nanoMetersAscending) {
            while (pointIndex0 < getSize() - 2 && getPoint(pointIndex0 + 1).getNanoMeters() < nanoMeters)
                pointIndex0++;
        } else {
            while (pointIndex0 < getSize() - 2 && getPoint(pointIndex0 + 1).getNanoMeters() > nanoMeters)
                pointIndex0++;
        }

        return pointIndex0;
    }

    public double getBeginNanoMeters() {
        return ratioToNanoMeters(0.);
    }

    public double getEndNanoMeters() {
        return ratioToNanoMeters(1.);
    }

    public double getMinNanoMeters() {
        return Math.min(getBeginNanoMeters(), getEndNanoMeters());
    }

    public double getMaxNanoMeters() {
        return Math.max(getBeginNanoMeters(), getEndNanoMeters());
    }

    public double getNanoMeterRange() {
        return Math.abs(getEndNanoMeters() - getBeginNanoMeters());
    }

    @EqualsAndHashCode
    @Getter
    @ToString
    public static final class Point {
        /**
         * Ratio between begin of sample line at 0.0 and end of sample line at 1.0.
         */
        @JsonProperty("ratio")
        private final double ratio;
        @JsonProperty("nanoMeters")
        private final double nanoMeters;

        public Point(@JsonProperty("ratio") double ratio, @JsonProperty("nanoMeters") double nanoMeters) {
            if (ratio < 0. || ratio > 1.)
                throw new IllegalArgumentException("ratio must be between 0.0 and 1.0 but is " + ratio);
            this.ratio = ratio;
            this.nanoMeters = nanoMeters;
        }
    }
}
