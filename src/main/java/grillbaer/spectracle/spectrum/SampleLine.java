package grillbaer.spectracle.spectrum;

import lombok.NonNull;
import org.opencv.core.Mat;

import java.util.Arrays;
import java.util.function.Function;

/**
 * Line of sample values, used for the measurement points of a spectrum.
 */
public class SampleLine {
    private final float[] values;

    public static final Function<float[], Float> PIXEL_CHANNEL_AVERAGE = pixel -> {
        float value = 0f;
        for (float channel : pixel) {
            value += channel;
        }
        return value / pixel.length;
    };

    public static final Function<float[], Float> PIXEL_CHANNEL_MAX = pixel -> {
        float value = 0f;
        for (float channel : pixel) {
            if (value < channel)
                value = channel;
        }
        return value;
    };

    public SampleLine(float[] values) {
        this.values = values;
    }

    public SampleLine(@NonNull Mat mat, int centerRow, int rows, Function<float[], Float> pixelFunction) {
        this.values = new float[mat.cols()];
        final var rawPixel = new byte[mat.channels()];
        final var normPixel = new float[mat.channels()];

        for (var col = 0; col < mat.cols(); col++) {

            Arrays.fill(normPixel, 0f);
            for (var rowOffset = 0; rowOffset < rows; rowOffset++) {
                final var row = centerRow - rows / 2 + rowOffset;
                if (row < 0 || row >= mat.rows())
                    continue;

                mat.get(row, col, rawPixel);
                for (int i = 0; i < rawPixel.length; i++) {
                    normPixel[i] += (((int) rawPixel[i]) & 0xff) / 255f / rows;
                }
            }

            values[col] = pixelFunction.apply(normPixel);
            if (values[col] < 0f) values[col] = 0f;
            if (values[col] > 1f) values[col] = 1f;
        }

    }

    public int getLength() {
        return this.values.length;
    }

    public float getValue(int index) {
        return this.values[index];
    }
}
