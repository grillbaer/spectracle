package grillbaer.spectracle.model;

import grillbaer.spectracle.camera.Camera;
import grillbaer.spectracle.camera.CameraProps;
import grillbaer.spectracle.camera.Frame;
import grillbaer.spectracle.model.Settings.SensitivityCalibration;
import grillbaer.spectracle.spectrum.SampleLine;
import grillbaer.spectracle.spectrum.Spectrum;
import grillbaer.spectracle.spectrum.WaveLengthCalibration;
import grillbaer.spectracle.spectrum.WaveLengthCalibration.WaveLengthPoint;
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
    private final Map<Integer, WaveLengthCalibration> waveLengthCalibrationByCameraId = new TreeMap<>();
    private final Observers<WaveLengthCalibration> waveLengthCalibrationObservers = new Observers<>();

    @Getter(AccessLevel.NONE)
    private final Map<Integer, Spectrum> sensitivityCalibrationByCameraId = new TreeMap<>();

    private boolean normalizeSampleValues;
    private final Observers<Boolean> normalizeSampleValuesObservers = new Observers<>();
    private Spectrum rawSpectrum;
    private Spectrum spectrum;
    private final Observers<Spectrum> spectrumObservers = new Observers<>();
    private double sampleRowPosRatio = 0.5;
    private int sampleRows = 10;

    public Model() {
        getFrameGrabbedObservers().add(cam -> updateSampleLineFromGrabbedFrame());
    }

    private void updateSampleLineFromGrabbedFrame() {
        setRawSampleLine(
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
            this.waveLengthCalibrationObservers.fire(getWaveLengthCalibration());
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

    public WaveLengthCalibration getWaveLengthCalibration(Integer cameraId) {
        return this.waveLengthCalibrationByCameraId.get(cameraId);
    }

    public WaveLengthCalibration getWaveLengthCalibration() {
        final var calibration = getWaveLengthCalibration(getCameraId());
        return calibration != null ? calibration : WaveLengthCalibration.createDefault();
    }

    public void setWaveLengthCalibration(int cameraId, WaveLengthCalibration waveLengthCalibration) {
        final var oldCalibration = this.waveLengthCalibrationByCameraId.put(cameraId, waveLengthCalibration);
        if (Objects.equals(cameraId, getCameraId()) && !Objects.equals(oldCalibration, waveLengthCalibration)) {
            if (waveLengthCalibration == null) {
                waveLengthCalibration = WaveLengthCalibration.createDefault();
            }
            if (!Objects.equals(oldCalibration, waveLengthCalibration)) {
                if (this.spectrum != null) {
                    setRawSampleLine(this.spectrum.getSampleLine());
                }
                this.waveLengthCalibrationObservers.fire(waveLengthCalibration);
            }
        }
    }

    public void setWaveLengthCalibration(@NonNull WaveLengthCalibration waveLengthCalibration) {
        if (getCameraId() != null) {
            setWaveLengthCalibration(getCameraId(), waveLengthCalibration);
        }
    }


    public Spectrum getSensitivityCalibration(Integer cameraId) {
        return this.sensitivityCalibrationByCameraId.get(cameraId);
    }

    public Spectrum getSensitivityCalibration() {
        return getSensitivityCalibration(getCameraId());
    }


    public void setSensitivityCalibration(int cameraId, Spectrum sensitivityCalibration) {
        final Spectrum oldCalibration;
        if (sensitivityCalibration != null) {
            oldCalibration = this.sensitivityCalibrationByCameraId.put(cameraId, sensitivityCalibration);
        } else {
            oldCalibration = this.sensitivityCalibrationByCameraId.remove(cameraId);
        }

        if (Objects.equals(cameraId, getCameraId())
                && !Objects.equals(oldCalibration, sensitivityCalibration)) {
            recalcSampleLineFromRaw();
        }
    }

    private void setSensitivityCalibration(Spectrum sensitivityCalibration) {
        if (getCameraId() != null) {
            setSensitivityCalibration(getCameraId(), sensitivityCalibration);
        }
    }

    public void setRawSampleLine(SampleLine rawSampleLine) {
        if (rawSampleLine != null) {
            this.rawSpectrum = Spectrum.create(rawSampleLine, getWaveLengthCalibration());
            this.spectrum = calcSensitivityCorrectedSpectrum(this.rawSpectrum);
            this.spectrumObservers.fire(this.spectrum);
        } else if (this.spectrum != null) {
            this.rawSpectrum = null;
            this.spectrum = null;
            this.spectrumObservers.fire(null);
        }
    }

    public void setNormalizeSampleValues(boolean normalize) {
        if (this.normalizeSampleValues != normalize) {
            this.normalizeSampleValues = normalize;
            recalcSampleLineFromRaw();
            this.normalizeSampleValuesObservers.fire(this.normalizeSampleValues);
        }
    }

    private void recalcSampleLineFromRaw() {
        if (this.rawSpectrum != null) {
            setRawSampleLine(this.rawSpectrum.getSampleLine());
        }
    }

    private Spectrum calcSensitivityCorrectedSpectrum(@NonNull Spectrum rawSpectrum) {
        final var sensitivityCalibration = getSensitivityCalibration();
        final var corrected = new double[rawSpectrum.getLength()];
        double maxCorrected = 0.;
        for (int i = 0; i < corrected.length; i++) {
            final var nanoMeters = rawSpectrum.getNanoMetersAtIndex(i);
            final var calibrationFactor =
                    sensitivityCalibration != null ? sensitivityCalibration.getValueAtNanoMeters(nanoMeters) : 1.;
            corrected[i] = rawSpectrum.getValueAtIndex(i) * calibrationFactor;
            if (corrected[i] > maxCorrected) {
                maxCorrected = corrected[i];
            }
        }

        if (this.normalizeSampleValues && maxCorrected > 0.) {
            for (int i = 0; i < corrected.length; i++) {
                corrected[i] /= maxCorrected;
            }
        }

        return Spectrum.create(new SampleLine(corrected, rawSpectrum.getSampleLine()
                .getOverExposed()), rawSpectrum.getCalibration());
    }

    /**
     * Calculate and set a new sensitivity correction spectrum by comparing the current (uncorrected)
     * spectrum obtained from the camera with the passed reference light source's idealized spectrum.
     */
    public void calibrateSensitivityWithReferenceLight(@NonNull Spectrum referenceLightSpectrum) {
        if (this.rawSpectrum != null) {
            final var correctionFactors = new double[this.rawSpectrum.getLength()];
            double maxCorrectionFactorInCalRange = 0.;
            for (int i = 0; i < this.rawSpectrum.getLength(); i++) {
                final var nanoMeters = this.rawSpectrum.getNanoMetersAtIndex(i);
                final var rawValue = this.rawSpectrum.getValueAtIndex(i);
                final var targetValue = referenceLightSpectrum.getValueAtNanoMeters(nanoMeters);
                correctionFactors[i] = targetValue / rawValue;
                if (nanoMeters >= 400. && nanoMeters <= 700 && maxCorrectionFactorInCalRange < correctionFactors[i]) {
                    maxCorrectionFactorInCalRange = correctionFactors[i];
                }
            }

            for (int i = 0; i < correctionFactors.length; i++) {
                correctionFactors[i] = Math.min(1., correctionFactors[i] / maxCorrectionFactorInCalRange);
            }

            //TODO: smoothen correction factor spectrum!

            final var sensitivityCalibration =
                    Spectrum.create(new SampleLine(correctionFactors), this.rawSpectrum.getCalibration());
            setSensitivityCalibration(sensitivityCalibration);
        }
    }

    public Settings createSettings() {
        final var settings = new Settings();
        settings.setSelectedCameraId(getCameraId());
        settings.setNormalizeSampleValues(isNormalizeSampleValues());

        for (Entry<Integer, CameraProps> entry : this.cameraPropsByCameraId.entrySet()) {
            settings.getOrCreateCamera(entry.getKey()).setCameraProps(entry.getValue());
        }
        for (Entry<Integer, WaveLengthCalibration> entry : this.waveLengthCalibrationByCameraId.entrySet()) {
            settings.getOrCreateCamera(entry.getKey()).setWaveLengthCalibration(entry.getValue());
        }
        for (Entry<Integer, Spectrum> entry : this.sensitivityCalibrationByCameraId.entrySet()) {
            final var correctionFactors = entry.getValue();
            settings.getOrCreateCamera(entry.getKey()).setSensitivityCalibration(
                    new SensitivityCalibration(correctionFactors.getCalibration().getBeginNanoMeters(),
                            correctionFactors.getCalibration().getEndNanoMeters(),
                            correctionFactors.getSampleLine().getValues()));
        }

        return settings;
    }

    public void applySettings(@NonNull Settings settings) {
        for (Settings.Camera cameraSettings : settings.getCamerasAsList()) {
            if (cameraSettings.getCameraProps() != null) {
                setCameraProps(cameraSettings.getId(), cameraSettings.getCameraProps());
            }
            if (cameraSettings.getWaveLengthCalibration() != null) {
                setWaveLengthCalibration(cameraSettings.getId(), cameraSettings.getWaveLengthCalibration());
            }
            if (cameraSettings.getSensitivityCalibration() != null) {
                final var sensCal = cameraSettings.getSensitivityCalibration();
                final var corrFactors = sensCal.getCorrectionFactors();
                final var wlCal =
                        WaveLengthCalibration.create(
                                new WaveLengthPoint(0.0, sensCal.getBeginNanoMeters()),
                                new WaveLengthPoint(1.0, sensCal.getEndNanoMeters()));
                setSensitivityCalibration(cameraSettings.getId(),
                        Spectrum.create(new SampleLine(corrFactors), wlCal));
            }
        }

        if (settings.getNormalizeSampleValues() != null) {
            setNormalizeSampleValues(settings.getNormalizeSampleValues());
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
