package grillbaer.spectracle.spectrum;

import lombok.NonNull;
import org.opencv.core.Mat;

import java.util.Arrays;
import java.util.function.ToDoubleFunction;

/**
 * Line of sample values, used for the measurement points of a spectrum.
 */
public class SampleLine {
    private final double[] values;

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

    public SampleLine(double[] values) {
        this.values = values;
    }

    public SampleLine(@NonNull Mat mat, int centerRow, int rows, ToDoubleFunction<double[]> pixelToValueFunction) {
        this.values = new double[mat.cols()];
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
                    normPixel[i] += (((int) rawPixel[i]) & 0xff) / 255. / rows;
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
}
