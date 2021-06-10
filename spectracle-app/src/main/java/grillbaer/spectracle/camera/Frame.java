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
    private VideoCapture source;
    private Mat mat;
    private BufferedImage image;


    public void grabFrom(@NonNull VideoCapture videoCapture) {
        if (this.source != videoCapture) {
            // always use fresh mat for new source to avoid any concurrency issues
            this.source = videoCapture;
            this.mat = new Mat();
            this.image = null;
        }

        videoCapture.read(this.mat);
        updateImage();
    }

    private void updateImage() {
        if (mat == null || mat.rows() == 0 || mat.cols() == 0) {
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
