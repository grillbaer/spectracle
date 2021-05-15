package grillbaer.spectracle.camera;

import grillbaer.spectracle.model.Observers;
import lombok.Getter;
import lombok.NonNull;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.Closeable;
import java.lang.reflect.Modifier;

public final class Camera implements Closeable {
    @Getter
    private final int id;
    private final VideoCapture videoCapture;
    private final Mat frameMat;

    @Getter
    private Observers<Camera> frameGrabbedObservers = new Observers<>();

    public Camera(int id) {
        this.id = id;
        this.videoCapture = new VideoCapture(id);
        this.frameMat = new Mat();
        // FIXME: find better location for res change
        setCameraProps(getCameraProps().withFrameWidth(1280).withFrameHeight(720));
    }

    public boolean isOpen() {
        return this.videoCapture.isOpened();
    }

    @Override
    public synchronized void close() {
        this.videoCapture.release();
    }

    public synchronized void grabNextFrame() {
        videoCapture.read(this.frameMat);
        this.frameGrabbedObservers.fire(this);
    }

    public synchronized Mat getFrameMat() {
        return this.frameMat;
    }

    public synchronized BufferedImage getFrameImage() {
        return convertMatToImage(this.frameMat);
    }

    public synchronized void setCameraProps(@NonNull CameraProps cameraProps) {
        setProp(Videoio.CAP_PROP_FRAME_WIDTH, cameraProps.getFrameWidth());
        setProp(Videoio.CAP_PROP_FRAME_HEIGHT, cameraProps.getFrameHeight());
        setProp(Videoio.CAP_PROP_EXPOSURE, cameraProps.getExposure());
    }

    public synchronized CameraProps getCameraProps() {
        return new CameraProps(
                (int) getProp(Videoio.CAP_PROP_FRAME_WIDTH),
                (int) getProp(Videoio.CAP_PROP_FRAME_HEIGHT),
                getProp(Videoio.CAP_PROP_EXPOSURE));
    }

    private void setProp(int propId, double value) {
        if (getProp(propId) != value) {
            this.videoCapture.set(propId, value);
        }
    }

    private double getProp(int propId) {
        return this.videoCapture.get(propId);
    }

    private static BufferedImage convertMatToImage(Mat mat) {
        final int type = mat.channels() == 1 ? BufferedImage.TYPE_BYTE_GRAY : BufferedImage.TYPE_3BYTE_BGR;
        final var buffer = new byte[mat.width() * mat.channels() * mat.height()];
        mat.get(0, 0, buffer);
        final var image = new BufferedImage(mat.cols(), mat.rows(), type);
        final var imageBuffer = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        //TODO: this certainly works without intermediate copy?
        System.arraycopy(buffer, 0, imageBuffer, 0, buffer.length);

        return image;
    }

    public synchronized void printAllProps() {
        System.out.println("backend = " + this.videoCapture.getBackendName());
        System.out.println("nativeObjectAddr = " + this.videoCapture.getNativeObjAddr());
        for (var field : Videoio.class.getFields()) {
            if (field.getName().startsWith("CAP_PROP_") && field.getType() == int.class
                    && Modifier.isFinal(field.getModifiers())
                    && Modifier.isStatic(field.getModifiers())
                    && Modifier.isPublic(field.getModifiers())) {
                final var name = field.getName();
                final int id;
                try {
                    id = field.getInt(null);
                    System.out.printf("prop %-40s %5d = %f\n", name, id, videoCapture.get(id));
                } catch (IllegalAccessException e) {
                    //ignore
                }
                // prop CAP_PROP_POS_MSEC          0 =    0,000000
                // PROP_POS_FRAMES                 1 =    0,000000
                // prop CAP_PROP_FRAME_WIDTH       3 =  640,000000
                // prop CAP_PROP_FRAME_HEIGHT      4 =  480,000000
                // prop CAP_PROP_FPS               5 =   30,000000
                // prop CAP_PROP_FOURCC            6 =   20,000000
                // prop CAP_PROP_MODE              9 =    0,000000
                // prop CAP_PROP_BRIGHTNESS       10 =    0,000000
                // prop CAP_PROP_CONTRAST         11 =   32,000000
                // prop CAP_PROP_SATURATION       12 =   60,000000
                // prop CAP_PROP_HUE              13 =    0,000000
                // prop CAP_PROP_GAIN             14 =    0,000000
                // prop CAP_PROP_EXPOSURE         15 =   -6,000000
                // prop CAP_PROP_CONVERT_RGB      16 =    1,000000
                // prop CAP_PROP_SHARPNESS        20 =    2,000000
                // prop CAP_PROP_AUTO_EXPOSURE    21 =    0,000000
                // prop CAP_PROP_GAMMA            22 =  100,000000
                // prop CAP_PROP_TEMPERATURE      23 = 4600,000000
                // prop CAP_PROP_BACKLIGHT        32 =    1,000000
                // prop CAP_PROP_SAR_NUM          40 =    1,000000
                // prop CAP_PROP_SAR_DEN          41 =    1,000000
                // prop CAP_PROP_BACKEND          42 = 1400,000000
                // prop CAP_PROP_ORIENTATION_AUTO 49 =   -1,000000
            }
        }
    }
}
