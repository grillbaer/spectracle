package grillbaer.spectracle.spectrum;

import lombok.NonNull;

import java.util.Arrays;

/**
 * Line of sample values, used for the measurement points of a spectrum.
 * Immutable.
 */
public class SampleLine {
    private final double[] values;
    private final boolean[] overExposed;

    private Double cachedMinValue;
    private Double cachedMaxValue;

    /**
     * Creates new sample line without overexposure information.
     * Values array will be copied.
     */
    public static SampleLine create(@NonNull double[] values) {
        return new SampleLine(Arrays.copyOf(values, values.length), null);
    }

    /**
     * Creates new sample line with optional overexposure information.
     * Arrays will be copied.
     */
    public static SampleLine create(@NonNull double[] values, boolean[] overExposed) {
        return new SampleLine(
                Arrays.copyOf(values, values.length),
                overExposed != null ? Arrays.copyOf(overExposed, overExposed.length) : null);
    }

    /**
     * Creates new sample line with overexposure information.
     * <p>
     * NOTE: No defensive copying here! The caller is responsible not to alter the arrays any more!
     */
    SampleLine(@NonNull double[] values, boolean[] overExposed) {
        if (overExposed != null && values.length != overExposed.length)
            throw new IllegalArgumentException("values and overExposed arrays have different lengths "
                    + values.length + " and " + overExposed.length);
        this.values = values;
        this.overExposed = overExposed;
    }

    public int getLength() {
        return this.values.length;
    }

    public double getValue(int index) {
        return this.values[index];
    }

    /**
     * Returns the value vector.
     * <p>
     * NOTE: No defensive copying here! The caller is responsible not to alter the array!
     */
    double[] getValues() {
        return Arrays.copyOf(this.values, this.values.length);
    }

    public double[] getCopyOfValues() {
        return Arrays.copyOf(this.values, this.values.length);
    }

    public double getMinValue() {
        if (this.cachedMinValue == null) {
            this.cachedMinValue = Arrays.stream(this.values).min().orElse(0.);
        }

        return this.cachedMinValue;
    }

    public double getMaxValue() {
        if (this.cachedMaxValue == null) {
            this.cachedMaxValue = Arrays.stream(this.values).max().orElse(0.);
        }

        return this.cachedMaxValue;
    }

    /**
     * Returns the overexposure vector.
     * <p>
     * NOTE: No defensive copying here! The caller is responsible not to alter the array!
     */
    boolean[] getOverExposed() {
        return this.overExposed;
    }

    public boolean[] getCopyOfOverExposed() {
        if (this.overExposed != null) {
            return Arrays.copyOf(this.overExposed, this.overExposed.length);
        } else {
            return null; // NOSONAR: missing overexposure information is marked as null
        }
    }

    public boolean isOverExposed(int index) {
        return this.overExposed != null && this.overExposed[index];
    }
}
