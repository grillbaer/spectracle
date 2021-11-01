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
import java.util.Map;
import java.util.TreeMap;

public final class Camera implements Closeable {
    private static final Logger LOG = LoggerFactory.getLogger(Camera.class);

    @Getter
    private final int id;
    private final VideoCapture videoCapture;
    private CameraProps cameraProps;

    private static final Map<Integer, String> PROP_NAMES_BY_ID = new TreeMap<>();

    static {
        for (var field : Videoio.class.getFields()) {
            if (field.getName().startsWith("CAP_PROP_") && field.getType() == int.class
                    && Modifier.isFinal(field.getModifiers())
                    && Modifier.isStatic(field.getModifiers())
                    && Modifier.isPublic(field.getModifiers())) {
                final var name = field.getName();
                try {
                    final var propId = field.getInt(null);
                    PROP_NAMES_BY_ID.put(propId, name);
                } catch (IllegalAccessException e) {
                    LOG.error("Unexpected exception", e);
                }
            }
        }
    }

    public Camera(int id) {
        LOG.info("Camera id={}: opening ...", id);
        this.id = id;
        this.videoCapture = new VideoCapture(id);

        if (isOpen()) {
            LOG.info("Camera id={}: opened successfully", id);
            logAllProps();
            disableAutomatics();
            disableSharpening();
            setCameraProps(getBackendCameraProps().withFrameWidth(1920).withFrameHeight(1080));
        } else {
            LOG.warn("Camera id={}: opening failed", id);
        }
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

    private void disableAutomatics() {
        setProp(Videoio.CAP_PROP_AUTO_WB, 0., true);
        setProp(Videoio.CAP_PROP_AUTO_EXPOSURE, 0., true);
        setProp(Videoio.CAP_PROP_GAIN, 0., true);
        setProp(Videoio.CAP_PROP_TEMPERATURE, 5000., true);
        setProp(Videoio.CAP_PROP_WB_TEMPERATURE, 5000., true);
    }

    private void disableSharpening() {
        setProp(Videoio.CAP_PROP_SHARPNESS, 0., true);
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
            final boolean accepted = this.videoCapture.set(propId, value);
            if (LOG.isInfoEnabled()) {
                LOG.info("Camera id={}: Setting {}   => accepted={}, reread={}",
                        getId(), formatPropValue(propId, value), accepted, getProp(id));
            }
        }
    }

    private double getProp(int propId) {
        return this.videoCapture.get(propId);
    }

    public synchronized void logAllProps() {
        if (LOG.isInfoEnabled()) {
            LOG.info("Properties of camera id={} backend={} nativeObjectAddr={} :",
                    getId(), this.videoCapture.getBackendName(), this.videoCapture.getNativeObjAddr());
            for (var propId : PROP_NAMES_BY_ID.keySet()) {
                LOG.info(formatPropValue(propId, videoCapture.get(propId)));
            }
        }
    }

    private String formatPropValue(int propId, double value) {
        return String.format(Locale.ROOT, "%-40s %5d = %6.1f",
                PROP_NAMES_BY_ID.get(propId), propId, value);
    }

    @Override
    public String toString() {
        return "Camera[" + "id=" + this.id + ", cameraProps=" + this.cameraProps + "]";
    }
}
