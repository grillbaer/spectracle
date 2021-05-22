package grillbaer.spectracle.spectrum;

import lombok.NonNull;
import org.opencv.core.Mat;

import java.util.function.Function;

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

    public SampleLine(@NonNull Mat mat, int centerRow, int rows, Function<float[], Float> pixelFunction) {
        this.values = new float[mat.cols()];
        final var rawPixel = new byte[mat.channels()];
        final var normPixel = new float[mat.channels()];

        for (var rowOffset = 0; rowOffset < rows; rowOffset++) {

            final var row = centerRow - rows / 2 + rowOffset;
            if (row < 0 || row >= mat.rows())
                continue;

            for (var col = 0; col < mat.cols(); col++) {

                mat.get(row, col, rawPixel);
                for (int i = 0; i < rawPixel.length; i++) {
                    normPixel[i] = (((int) rawPixel[i]) & 0xff) / 256f;
                }

                var value = pixelFunction.apply(normPixel);

                values[col] += value / rows;
            }
        }
    }

    public int getLength() {
        return this.values.length;
    }

    public float getValue(int index) {
        return this.values[index];
    }
}
