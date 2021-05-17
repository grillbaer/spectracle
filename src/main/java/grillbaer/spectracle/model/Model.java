package grillbaer.spectracle.model;

import grillbaer.spectracle.camera.Camera;
import grillbaer.spectracle.camera.CameraProps;
import grillbaer.spectracle.camera.Frame;
import grillbaer.spectracle.spectrum.Calibration;
import grillbaer.spectracle.spectrum.SampleLine;
import grillbaer.spectracle.spectrum.Spectrum;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

import javax.swing.*;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Model containing a camera, the spectra obtained from it, properties and user interface states.
 */
@Getter
public final class Model {
    private Camera camera;
    private final Observers<Camera> cameraObservers = new Observers<>();
    private final Observers<CameraProps> cameraPropsObservers = new Observers<>();

    private boolean cameraPaused = true;
    private final Observers<Boolean> cameraPausedObservers = new Observers<>();

    @Getter(AccessLevel.NONE)
    private Frame grabbingFrame = new Frame();
    private Frame currentFrame = new Frame();
    private final Observers<Frame> frameGrabbedObservers = new Observers<>();

    private @NonNull Calibration calibration = Calibration.createDefault();
    private final Observers<Calibration> calibrationObservers = new Observers<>();

    private double sampleRowPosRatio = 0.5;
    private int sampleRows = 3;

    private Spectrum spectrum;

    private final Observers<Spectrum> spectrumObservers = new Observers<>();

    public Model() {
        getFrameGrabbedObservers().add(cam -> updateSampleLineFromGrabbedFrame());
    }

    private void updateSampleLineFromGrabbedFrame() {
        setSampleLine(
                new SampleLine(this.currentFrame.getMat(),
                        (int) (this.currentFrame.getMat().rows() * getSampleRowPosRatio()),
                        getSampleRows()));
    }

    public void setCamera(Camera camera) {
        if (this.camera != camera) {
            this.camera = camera;
            this.cameraObservers.fire(this.camera);
            triggerNextFrameIfNotPaused();
        }
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

    public CameraProps getCameraProps() {
        return this.camera != null ? this.camera.getCameraProps() : null;
    }

    public void setCameraProps(@NonNull CameraProps cameraProps) {
        if (this.camera != null) {
            final var oldProps = getCameraProps();
            this.camera.setCameraProps(cameraProps);
            final var newProps = getCameraProps();
            if (!Objects.equals(oldProps, newProps)) {
                this.cameraPropsObservers.fire(newProps);
            }
        }
    }

    public void setCalibration(@NonNull Calibration calibration) {
        if (!Objects.equals(this.calibration, calibration)) {
            this.calibration = calibration;
            this.calibrationObservers.fire(calibration);
            if (this.spectrum != null) {
                setSampleLine(this.spectrum.getSampleLine());
            }
        }
    }

    public void setSampleLine(SampleLine sampleLine) {
        if (sampleLine != null) {
            this.spectrum = Spectrum.create(sampleLine, this.calibration);
            this.spectrumObservers.fire(this.spectrum);
        } else if (this.spectrum != null) {
            this.spectrum = null;
            this.spectrumObservers.fire(null);
        }
    }
}
