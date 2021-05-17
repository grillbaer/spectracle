package grillbaer.spectracle.camera;

import lombok.Getter;
import lombok.NonNull;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

/**
 * Grabbed frame from camera.
 */
@Getter
public final class Frame {
    private final Mat mat = new Mat();
    private BufferedImage image;


    public void grabFrom(@NonNull VideoCapture videoCapture) {
        videoCapture.read(this.mat);
        this.image = convertMatToImage(this.mat);
    }

    private static BufferedImage convertMatToImage(Mat mat) {
        if (mat.rows() == 0 || mat.cols() == 0)
            return null;

        final int type = mat.channels() == 1 ? BufferedImage.TYPE_BYTE_GRAY : BufferedImage.TYPE_3BYTE_BGR;
        final var buffer = new byte[mat.width() * mat.channels() * mat.height()];
        mat.get(0, 0, buffer);
        final var image = new BufferedImage(mat.cols(), mat.rows(), type);
        final var imageBuffer = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();

        System.arraycopy(buffer, 0, imageBuffer, 0, buffer.length);

        return image;
    }

    public int getWidth() {
        return this.mat.cols();
    }

    public int getHeight() {
        return this.mat.rows();
    }
}
