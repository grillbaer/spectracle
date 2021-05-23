package grillbaer.spectracle.model;

import grillbaer.spectracle.camera.Camera;
import grillbaer.spectracle.camera.CameraProps;
import grillbaer.spectracle.camera.Frame;
import grillbaer.spectracle.spectrum.SampleLine;
import grillbaer.spectracle.spectrum.Spectrum;
import grillbaer.spectracle.spectrum.WaveLengthCalibration;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

import javax.swing.*;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

/**
 * Model containing a camera, the frames and spectra obtained from it, properties and user interface states.
 * The model must only be accessed from the AWT EventDispatchThread.
 */
@Getter
public final class Model {
    private Camera camera;
    private final Observers<Camera> cameraObservers = new Observers<>();

    private boolean cameraPaused = true;
    private final Observers<Boolean> cameraPausedObservers = new Observers<>();

    @Getter(AccessLevel.NONE)
    private Frame grabbingFrame = new Frame();
    private Frame currentFrame = new Frame();
    private final Observers<Frame> frameGrabbedObservers = new Observers<>();

    @Getter(AccessLevel.NONE)
    private final Map<Integer, CameraProps> cameraPropsByCameraId = new TreeMap<>();
    private final Observers<CameraProps> cameraPropsObservers = new Observers<>();

    @Getter(AccessLevel.NONE)
    private final Map<Integer, WaveLengthCalibration> calibrationByCameraId = new TreeMap<>();
    private final Observers<WaveLengthCalibration> calibrationObservers = new Observers<>();

    private Spectrum spectrum;
    private final Observers<Spectrum> spectrumObservers = new Observers<>();
    private double sampleRowPosRatio = 0.5;
    private int sampleRows = 10;

    public Model() {
        getFrameGrabbedObservers().add(cam -> updateSampleLineFromGrabbedFrame());
    }

    private void updateSampleLineFromGrabbedFrame() {
        setSampleLine(
                new SampleLine(this.currentFrame.getMat(),
                        (int) (this.currentFrame.getMat().rows() * getSampleRowPosRatio()),
                        getSampleRows(), SampleLine.PIXEL_CHANNEL_MAX));
    }

    /**
     * Set a camera to use.
     * Note: it's the callers responsibility to have the new camera successfully opened and to cleanly close
     * the previous camera.
     */
    public void setCamera(Camera camera) {
        if (this.camera != camera) {
            this.camera = camera;
            final var cameraProps = getCameraProps();
            if (cameraProps != null) {
                this.camera.setCameraProps(cameraProps);
            }
            this.cameraObservers.fire(this.camera);
            this.calibrationObservers.fire(getCalibration());
            this.cameraPropsObservers.fire(cameraProps);
            triggerNextFrameIfNotPaused();
        }
    }

    public Integer getCameraId() {
        return this.camera != null ? this.camera.getId() : null;
    }

    public void setCameraPaused(boolean paused) {
        if (this.cameraPaused != paused) {
            this.cameraPaused = paused;
            this.cameraPausedObservers.fire(paused);
            triggerNextFrameIfNotPaused();
        }
    }

    private void triggerNextFrameIfNotPaused() {
        if (!this.cameraPaused) {
            triggerNextFrame();
        }
    }

    private void triggerNextFrame() {
        if (this.camera != null) {
            CompletableFuture.runAsync(() -> {
                this.camera.grabNextFrame(this.grabbingFrame);
                SwingUtilities.invokeLater(() -> {
                    setCurrentFrame(this.grabbingFrame);
                    triggerNextFrameIfNotPaused();
                });
            });
        }
    }

    public void grabSingleFrame() {
        this.camera.grabNextFrame(this.grabbingFrame);
        setCurrentFrame(this.grabbingFrame);
    }

    private void setCurrentFrame(@NonNull Frame newlyGrabbedFrame) {
        this.grabbingFrame = this.currentFrame;
        this.currentFrame = newlyGrabbedFrame;
        this.frameGrabbedObservers.fire(this.currentFrame);
    }

    public CameraProps getCameraProps(Integer cameraId) {
        return this.cameraPropsByCameraId.get(cameraId);
    }

    public CameraProps getCameraProps() {
        if (getCameraId() == null)
            return null;

        final var props = getCameraProps(getCameraId());
        if (props != null)
            return props;

        return getCamera().getBackendCameraProps();
    }

    public void setCameraProps(int cameraId, CameraProps cameraProps) {
        final var oldProps = this.cameraPropsByCameraId.put(cameraId, cameraProps);
        if (Objects.equals(cameraId, getCameraId()) && !Objects.equals(oldProps, cameraProps)) {
            if (cameraProps != null) {
                this.camera.setCameraProps(cameraProps);
            }
            this.cameraPropsObservers.fire(cameraProps);
        }
    }

    public void setCameraProps(@NonNull CameraProps cameraProps) {
        if (getCameraId() != null) {
            setCameraProps(getCameraId(), cameraProps);
        }
    }

    public WaveLengthCalibration getCalibration(Integer cameraId) {
        return this.calibrationByCameraId.get(cameraId);
    }

    public WaveLengthCalibration getCalibration() {
        final var calibration = getCalibration(getCameraId());
        return calibration != null ? calibration : WaveLengthCalibration.createDefault();
    }

    public void setCalibration(int cameraId, WaveLengthCalibration waveLengthCalibration) {
        final var oldCalibration = this.calibrationByCameraId.put(cameraId, waveLengthCalibration);
        if (Objects.equals(cameraId, getCameraId()) && !Objects.equals(oldCalibration, waveLengthCalibration)) {
            if (waveLengthCalibration == null) {
                waveLengthCalibration = WaveLengthCalibration.createDefault();
            }
            if (!Objects.equals(oldCalibration, waveLengthCalibration)) {
                if (this.spectrum != null) {
                    setSampleLine(this.spectrum.getSampleLine());
                }
                this.calibrationObservers.fire(waveLengthCalibration);
            }
        }
    }

    public void setCalibration(@NonNull WaveLengthCalibration waveLengthCalibration) {
        if (getCameraId() != null) {
            setCalibration(getCameraId(), waveLengthCalibration);
        }
    }

    public void setSampleLine(SampleLine sampleLine) {
        if (sampleLine != null) {
            this.spectrum = Spectrum.create(sampleLine, getCalibration());
            this.spectrumObservers.fire(this.spectrum);
        } else if (this.spectrum != null) {
            this.spectrum = null;
            this.spectrumObservers.fire(null);
        }
    }

    public Settings createSettings() {
        final var settings = new Settings();
        settings.setSelectedCameraId(getCameraId());
        for (Entry<Integer, CameraProps> entry : this.cameraPropsByCameraId.entrySet()) {
            settings.getOrCreateCamera(entry.getKey()).setCameraProps(entry.getValue());
        }
        for (Entry<Integer, WaveLengthCalibration> entry : this.calibrationByCameraId.entrySet()) {
            settings.getOrCreateCamera(entry.getKey()).setWaveLengthCalibration(entry.getValue());
        }

        return settings;
    }

    public void applySettings(@NonNull Settings settings) {
        for (Settings.Camera cameraSettings : settings.getCamerasAsList()) {
            if (cameraSettings.getCameraProps() != null) {
                setCameraProps(cameraSettings.getId(), cameraSettings.getCameraProps());
            }
            if (cameraSettings.getWaveLengthCalibration() != null) {
                setCalibration(cameraSettings.getId(), cameraSettings.getWaveLengthCalibration());
            }
        }

        if (settings.getSelectedCameraId() != null) {
            final var newCam = new Camera(settings.getSelectedCameraId()); // NOSONAR: needs to stay open
            if (newCam.isOpen()) {
                final var oldCam = getCamera();
                setCamera(newCam);
                if (oldCam != null) {
                    oldCam.close();
                }
            }
        }
    }
}
