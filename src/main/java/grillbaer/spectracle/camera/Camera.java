package grillbaer.spectracle.camera;

import lombok.Getter;
import lombok.NonNull;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.lang.reflect.Modifier;
import java.util.Locale;

public final class Camera implements Closeable {
    private static final Logger LOG = LoggerFactory.getLogger(Camera.class);

    @Getter
    private final int id;
    private final VideoCapture videoCapture;
    private CameraProps cameraProps;

    public Camera(int id) {
        this.id = id;
        this.videoCapture = new VideoCapture(id);

        disableAutoWhiteBalance();
        setCameraProps(getBackendCameraProps().withFrameWidth(1280).withFrameHeight(720));
    }

    public synchronized boolean isOpen() {
        return this.videoCapture.isOpened();
    }

    @Override
    public synchronized void close() {
        this.videoCapture.release();
    }

    public synchronized void grabNextFrame(@NonNull Frame targetFrame) {
        targetFrame.grabFrom(this.videoCapture);
    }

    private void disableAutoWhiteBalance() {
        setProp(Videoio.CAP_PROP_AUTO_WB, 0., true);
        setProp(Videoio.CAP_PROP_TEMPERATURE, 5000., true);
        setProp(Videoio.CAP_PROP_WB_TEMPERATURE, 5000., true);
    }

    public synchronized void setCameraProps(@NonNull CameraProps cameraProps) {
        this.cameraProps = cameraProps;
        setProp(Videoio.CAP_PROP_FRAME_WIDTH, cameraProps.getFrameWidth(), false);
        setProp(Videoio.CAP_PROP_FRAME_HEIGHT, cameraProps.getFrameHeight(), false);
        setProp(Videoio.CAP_PROP_EXPOSURE, cameraProps.getExposure(), true);
    }

    public synchronized CameraProps getCameraProps() {
        return this.cameraProps;
    }

    public synchronized CameraProps getBackendCameraProps() {
        return new CameraProps(
                (int) getProp(Videoio.CAP_PROP_FRAME_WIDTH),
                (int) getProp(Videoio.CAP_PROP_FRAME_HEIGHT),
                getProp(Videoio.CAP_PROP_EXPOSURE));
    }

    private void setProp(int propId, double value, boolean force) {
        if (force || this.videoCapture.get(propId) != value) {
            this.videoCapture.set(propId, value);
        }
    }

    private double getProp(int propId) {
        return this.videoCapture.get(propId);
    }

    public synchronized void printAllProps() {
        LOG.info("Properties of camera id={} backend={} nativeObjectAddr={} :",
                getId(), this.videoCapture.getBackendName(), this.videoCapture.getNativeObjAddr());
        for (var field : Videoio.class.getFields()) {
            if (field.getName().startsWith("CAP_PROP_") && field.getType() == int.class
                    && Modifier.isFinal(field.getModifiers())
                    && Modifier.isStatic(field.getModifiers())
                    && Modifier.isPublic(field.getModifiers())) {
                final var name = field.getName();
                final int propId;
                try {
                    propId = field.getInt(null);
                    if (LOG.isInfoEnabled()) {
                        LOG.info(String.format(Locale.ROOT, "  %-40s %5d = %f", name, propId, videoCapture.get(propId)));
                    }
                } catch (IllegalAccessException e) {
                    //ignore
                }
                // Property example of a test cam:
                // CAP_PROP_POS_MSEC          0 =    0,000000
                // CAP_PROP_POS_FRAMES        1 =    0,000000
                // CAP_PROP_FRAME_WIDTH       3 =  640,000000
                // CAP_PROP_FRAME_HEIGHT      4 =  480,000000
                // CAP_PROP_FPS               5 =   30,000000
                // CAP_PROP_FOURCC            6 =   20,000000
                // CAP_PROP_MODE              9 =    0,000000
                // CAP_PROP_BRIGHTNESS       10 =    0,000000
                // CAP_PROP_CONTRAST         11 =   32,000000
                // CAP_PROP_SATURATION       12 =   60,000000
                // CAP_PROP_HUE              13 =    0,000000
                // CAP_PROP_GAIN             14 =    0,000000
                // CAP_PROP_EXPOSURE         15 =   -6,000000
                // CAP_PROP_CONVERT_RGB      16 =    1,000000
                // CAP_PROP_SHARPNESS        20 =    2,000000
                // CAP_PROP_AUTO_EXPOSURE    21 =    0,000000
                // CAP_PROP_GAMMA            22 =  100,000000
                // CAP_PROP_TEMPERATURE      23 = 4600,000000
                // CAP_PROP_BACKLIGHT        32 =    1,000000
                // CAP_PROP_SAR_NUM          40 =    1,000000
                // CAP_PROP_SAR_DEN          41 =    1,000000
                // CAP_PROP_BACKEND          42 = 1400,000000
                // CAP_PROP_ORIENTATION_AUTO 49 =   -1,000000
            }
        }
    }
}
