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
        updateImage();
    }

    private void updateImage() {
        if (mat.rows() == 0 || mat.cols() == 0) {
            this.image = null;
            return;
        }

        final int imageType = mat.channels() == 1 ? BufferedImage.TYPE_BYTE_GRAY : BufferedImage.TYPE_3BYTE_BGR;
        if (this.image == null || this.image.getType() != imageType || this.image.getWidth() != mat.cols() || this.image
                .getHeight() != mat.rows()) {
            this.image = new BufferedImage(mat.cols(), mat.rows(), imageType);
        }

        final var imageBuffer = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        mat.get(0, 0, imageBuffer);
    }

    public int getWidth() {
        return this.mat.cols();
    }

    public int getHeight() {
        return this.mat.rows();
    }
}
