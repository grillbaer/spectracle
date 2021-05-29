package grillbaer.spectracle.model;

import grillbaer.spectracle.camera.Camera;
import grillbaer.spectracle.camera.CameraProps;
import grillbaer.spectracle.camera.Frame;
import grillbaer.spectracle.model.Settings.SensitivityCalibration;
import grillbaer.spectracle.spectrum.*;
import grillbaer.spectracle.spectrum.Calculations.Extrema;
import grillbaer.spectracle.spectrum.WaveLengthCalibration.WaveLengthPoint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

import javax.swing.*;
import java.util.HashMap;
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

    private double sampleRowPosRatio = 0.5;
    private int sampleRows = 10;
    private double timeAveragingFactor = 0.;
    private boolean peakHold;
    private double smoothIndexSteps = 0;

    private boolean normalizeSampleValues;
    private final Observers<Boolean> normalizeSampleValuesObservers = new Observers<>();

    private Double hoverCursorWaveLength;
    private final Observers<Double> hoverCursorWaveLengthObservers = new Observers<>();


    /**
     * Spectrum as it came from the last camera frame.
     */
    private Spectrum rawSpectrum;
    /**
     * Spectrum with quality improvements like time averaging and noise removal.
     */
    private Spectrum purifiedSpectrum;
    /**
     * Intensity calibrated, normalized, smoothed spectrum to be shown in the spectrum graph.
     */
    private Spectrum spectrum;
    private final Observers<Spectrum> spectrumObservers = new Observers<>();

    private Map<String, String> lastUsedDirectories = new HashMap<>();

    public Model() {
        getFrameGrabbedObservers().add(cam -> updateSampleLineFromGrabbedFrame());
    }

    private void updateSampleLineFromGrabbedFrame() {
        if (this.currentFrame == null)
            return;

        setRawSampleLine(
                Sampling.sampleLineFromMat(this.currentFrame.getMat(),
                        (int) (this.currentFrame.getMat().rows() * getSampleRowPosRatio()),
                        getSampleRows(), Sampling.PIXEL_CHANNEL_AVERAGE));
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

    public void setCurrentFrame(@NonNull Frame newlyGrabbedFrame) {
        this.grabbingFrame = this.currentFrame;
        this.currentFrame = newlyGrabbedFrame;
        this.frameGrabbedObservers.fire(this.currentFrame);
    }

    public void clearCurrentFrame() {
        setCurrentFrame(new Frame());
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

    public void setSensitivityCalibration(Spectrum sensitivityCalibration) {
        if (getCameraId() != null) {
            setSensitivityCalibration(getCameraId(), sensitivityCalibration);
        }
    }

    public void setRawSampleLine(SampleLine rawSampleLine) {
        if (rawSampleLine != null) {
            this.rawSpectrum = Spectrum.create(rawSampleLine, getWaveLengthCalibration());
            this.purifiedSpectrum = calcPurifiedSpectrum(this.rawSpectrum, this.purifiedSpectrum, this.timeAveragingFactor, this.peakHold);
            this.spectrum = calcProcessedSpectrum(this.purifiedSpectrum, getSensitivityCalibration(), this.smoothIndexSteps, this.normalizeSampleValues);
            this.spectrumObservers.fire(this.spectrum);
        } else if (this.spectrum != null) {
            this.rawSpectrum = null;
            this.purifiedSpectrum = null;
            this.spectrum = null;
            this.spectrumObservers.fire(null);
        }
    }

    private static Spectrum calcPurifiedSpectrum(Spectrum rawSpectrum, Spectrum purifiedSpectrum,
                                                 double timeAveragingFactor, boolean peakHold) {
        if (rawSpectrum == null)
            return null;

        final var purifiedSampleLine = purifiedSpectrum != null ? purifiedSpectrum.getSampleLine() : null;

        return Spectrum.create(Calculations.timeAverage(rawSpectrum.getSampleLine(), purifiedSampleLine,
                timeAveragingFactor, peakHold), rawSpectrum.getCalibration());
    }

    private static Spectrum calcProcessedSpectrum(Spectrum purifiedSpectrum, Spectrum sensitivityCalibration, double smoothIndexSteps, boolean normalizeSampleValues) {
        if (purifiedSpectrum == null)
            return null;

        final Spectrum calibrated = sensitivityCalibration != null
                ? Calculations.applySensitivityCalibration(purifiedSpectrum, sensitivityCalibration) : purifiedSpectrum;
        final SampleLine smoothed = Calculations.gaussianSmooth(calibrated.getSampleLine(), smoothIndexSteps);
        final SampleLine normalized = normalizeSampleValues ? Calculations.normalize(smoothed) : smoothed;

        return Spectrum.create(normalized, purifiedSpectrum.getCalibration());
    }

    public Extrema getExtrema() {
        if (this.spectrum == null)
            return null;

        // values have been determined for a 1280 resolution
        final var lengthRatio = this.spectrum.getLength() / 1280.;
        return Calculations.findLocalExtrema(this.spectrum.getSampleLine(),
                0.6 * lengthRatio, 2. * lengthRatio, 16, 16);
    }

    public void setNormalizeSampleValues(boolean normalize) {
        if (this.normalizeSampleValues != normalize) {
            this.normalizeSampleValues = normalize;
            recalcSampleLineFromRaw();
            this.normalizeSampleValuesObservers.fire(this.normalizeSampleValues);
        }
    }

    public void setHoverCursorWaveLength(Double hoverCursorWaveLength) {
        if (!Objects.equals(this.hoverCursorWaveLength, hoverCursorWaveLength)) {
            this.hoverCursorWaveLength = hoverCursorWaveLength;
            this.hoverCursorWaveLengthObservers.fire(hoverCursorWaveLength);
        }
    }

    public void setSmoothIndexSteps(double smoothIndexSteps) {
        if (this.smoothIndexSteps != smoothIndexSteps) {
            this.smoothIndexSteps = smoothIndexSteps;
            recalcSampleLineFromRaw();
        }
    }

    public void setTimeAveragingFactor(double timeAveragingFactor) {
        if (this.timeAveragingFactor != timeAveragingFactor) {
            this.timeAveragingFactor = timeAveragingFactor;
            recalcSampleLineFromRaw();
        }
    }

    public void setPeakHold(boolean peakHold) {
        if (this.peakHold != peakHold) {
            this.peakHold = peakHold;
            recalcSampleLineFromRaw();
        }
    }

    private void recalcSampleLineFromRaw() {
        if (this.rawSpectrum != null) {
            setRawSampleLine(this.rawSpectrum.getSampleLine());
        }
    }

    public String getLastUsedDirectory(@NonNull String contentName) {
        return this.lastUsedDirectories.get(contentName);
    }

    public void setLastUsedDirectory(@NonNull String contentName, String directory) {
        if (directory != null) {
            this.lastUsedDirectories.put(contentName, directory);
        } else {
            this.lastUsedDirectories.remove(contentName);
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
                            correctionFactors.getSampleLine().getCopyOfValues()));
        }

        settings.setLastUsedDirectories(this.lastUsedDirectories);

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
                        Spectrum.create(SampleLine.create(corrFactors), wlCal));
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

        if (settings.getLastUsedDirectories() != null) {
            this.lastUsedDirectories.putAll(settings.getLastUsedDirectories());
        }
    }
}
