package grillbaer.spectracle.spectrum;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.ToDoubleFunction;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Line of sample values, used for the measurement points of a spectrum.
 */
public class SampleLine {
    private final double[] values;
    private final boolean[] overExposed;

    private Double minValue;
    private Double maxValue;


    public static final ToDoubleFunction<double[]> PIXEL_CHANNEL_AVERAGE = pixel -> {
        double value = 0.;
        for (double channel : pixel) {
            value += channel;
        }
        return value / pixel.length;
    };

    public static final ToDoubleFunction<double[]> PIXEL_CHANNEL_MAX = pixel -> {
        double value = 0.;
        for (double channel : pixel) {
            if (value < channel)
                value = channel;
        }
        return value;
    };

    public SampleLine(@NonNull double[] values) {
        this.values = values;
        this.overExposed = null;
    }

    public SampleLine(@NonNull double[] values, boolean[] overExposed) {
        if (overExposed != null && values.length != overExposed.length)
            throw new IllegalArgumentException("values and overExposed arrays have different lengths "
                    + values.length + " and " + overExposed.length);
        this.values = values;
        this.overExposed = overExposed;
    }

    public SampleLine(@NonNull Mat mat, int centerRow, int rows, ToDoubleFunction<double[]> pixelToValueFunction) {
        this.values = new double[mat.cols()];
        this.overExposed = new boolean[mat.cols()];
        final var rawPixel = new byte[mat.channels()];
        final var normPixel = new double[mat.channels()];

        for (var col = 0; col < mat.cols(); col++) {

            Arrays.fill(normPixel, 0f);
            for (var rowOffset = 0; rowOffset < rows; rowOffset++) {
                final var row = centerRow - rows / 2 + rowOffset;
                if (row < 0 || row >= mat.rows())
                    continue;

                mat.get(row, col, rawPixel);
                for (int i = 0; i < rawPixel.length; i++) {
                    final var channelValue = ((int) rawPixel[i]) & 0xff;
                    normPixel[i] += channelValue / 255. / rows;
                    if (channelValue >= 255) {
                        this.overExposed[col] = true;
                    }
                }
            }

            values[col] = pixelToValueFunction.applyAsDouble(normPixel);
            if (values[col] < 0f) values[col] = 0f;
            if (values[col] > 1f) values[col] = 1f;
        }
    }

    public int getLength() {
        return this.values.length;
    }

    public double getValue(int index) {
        return this.values[index];
    }

    public double[] getValues() {
        return Arrays.copyOf(this.values, this.values.length);
    }

    public double getMinValue() {
        if (this.minValue == null) {
            this.minValue = Arrays.stream(this.values).min().orElse(0.);
        }

        return this.minValue;
    }

    public double getMaxValue() {
        if (this.maxValue == null) {
            this.maxValue = Arrays.stream(this.values).max().orElse(0.);
        }

        return this.maxValue;
    }

    public boolean[] getOverExposed() {
        if (this.overExposed != null) {
            return Arrays.copyOf(this.overExposed, this.overExposed.length);
        } else {
            return null;
        }
    }

    public boolean isOverExposed(int index) {
        return this.overExposed != null && this.overExposed[index];
    }

    public SampleLine withGaussianSmooth(double sigmaInIndexSteps) {
        if (sigmaInIndexSteps <= 0)
            return this;

        final var distrib = new NormalDistribution(0., sigmaInIndexSteps);
        final var smoothArray = new double[(int) sigmaInIndexSteps * 3 + 2];
        var sum = 0.0;
        for (int i = 0; i < smoothArray.length; i++) {
            smoothArray[i] = distrib.density(i);
            sum += (i > 0 ? 2 : 1) * smoothArray[i];
        }
        final var result = new double[getLength()];
        for (int i = 0; i < getLength(); i++) {
            result[i] = smoothArray[0] * getValue(i);
            for (int j = 1; j < smoothArray.length; j++) {
                result[i] += smoothArray[j] * (getValue(max(0, i - j)) + getValue(min(getLength() - 1, i + j)));
            }
            result[i] /= sum;
        }

        return new SampleLine(result, this.overExposed);
    }

    public List<Extremum> findLocalExtrema(int noiseSigmaIndexSteps, int baseSigmaIndexSteps) {
        final var denoised = withGaussianSmooth(noiseSigmaIndexSteps);
        final var baseLevel = withGaussianSmooth(baseSigmaIndexSteps);
        final var delta = new double[getLength()];
        for (int i = 0; i < delta.length; i++) {
            delta[i] = denoised.getValue(i) - baseLevel.getValue(i);
        }

        final var extrema = new ArrayList<Extremum>();
        for (int i = 1; i < delta.length - 1; i++) {
            if ((denoised.getValue(i - 1) < denoised.getValue(i) && denoised.getValue(i) > denoised.getValue(i + 1))
                    || (denoised.getValue(i - 1) > denoised.getValue(i) && denoised.getValue(i) < denoised.getValue(i + 1))) {
                int fineIndex = getValue(i) > getValue(i - 1) ? i : i - 1;
                fineIndex = getValue(fineIndex) > getValue(i + 1) ? i : i + 1;
                extrema.add(new Extremum(fineIndex, 100 * delta[i]));
            }
        }

        return extrema;
    }

    @AllArgsConstructor
    @Getter
    public static class Extremum {
        private int index;
        private double level;
    }
}
