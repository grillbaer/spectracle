package grillbaer.spectracle.spectrum;

import lombok.NonNull;
import org.opencv.core.Mat;

public class SampleLine {
    private final float[] values;

    public SampleLine(@NonNull Mat mat, int centerRow, int rows) {
        this.values = new float[mat.cols()];
        final var pixel = new byte[mat.channels()];

        for (var rowOffset = 0; rowOffset < rows; rowOffset++) {

            final var row = centerRow - rows / 2 + rowOffset;
            if (row < 0 || row >= mat.rows())
                continue;

            for (var col = 0; col < mat.cols(); col++) {

                mat.get(row, col, pixel);
                var value = 0f;
                for (byte b : pixel) {
                    value += (((int) b) & 0xff) / 256f;
                }

                values[col] += value / pixel.length / rows;
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
