package grillbaer.spectracle.spectrum;

import lombok.NonNull;
import org.opencv.core.Mat;

import java.util.Arrays;
import java.util.function.ToDoubleFunction;

/**
 * Taking samples from images.
 */
public final class Sampling {
    private Sampling() {
        // no instances
    }

    /**
     * Average all channels of a pixel.
     */
    public static final ToDoubleFunction<double[]> PIXEL_CHANNEL_AVERAGE = pixel -> {
        double value = 0.;
        for (double channel : pixel) {
            value += channel;
        }
        return value / pixel.length;
    };

    /**
     * Select the maximum channel of a pixel.
     */
    public static final ToDoubleFunction<double[]> PIXEL_CHANNEL_MAX = pixel -> {
        double value = 0.;
        for (double channel : pixel) {
            if (value < channel)
                value = channel;
        }
        return value;
    };

    /**
     * Take a sample line from a horizontal strip of an image pixel matrix.
     *
     * @param centerRow            the center pixel row of the sample strip within the matrix
     * @param rows                 the number of pixel rows to include in the sample strip
     * @param pixelToValueFunction function to aggregate the pixel's channels into one intensity value
     */
    public static SampleLine sampleLineFromMat(@NonNull Mat mat, int centerRow, int rows, @NonNull ToDoubleFunction<double[]> pixelToValueFunction) {
        final var values = new double[mat.cols()];
        final var overExposed = new boolean[mat.cols()];
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
                        overExposed[col] = true;
                    }
                }
            }

            values[col] = pixelToValueFunction.applyAsDouble(normPixel);
            if (values[col] < 0f) values[col] = 0f;
            if (values[col] > 1f) values[col] = 1f;
        }

        return new SampleLine(values, overExposed);
    }
}
