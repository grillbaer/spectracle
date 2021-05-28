package grillbaer.spectracle.ui;

import grillbaer.spectracle.Context;
import grillbaer.spectracle.camera.Camera;
import grillbaer.spectracle.camera.CameraProps;
import grillbaer.spectracle.model.SpectrumDataFiles;
import grillbaer.spectracle.ui.components.CameraView;
import grillbaer.spectracle.ui.components.SpectrumReproductionView;
import lombok.NonNull;

import javax.swing.*;
import java.awt.*;

public class CameraPanel {
    private final Context context;

    private final JPanel panel;
    private final SpectrumReproductionView spectrumReproductionView;
    private final CameraView cameraView;

    private static final int EXPOSURE_RESOLUTION = 1;
    private final JButton exposureMinusButton;
    private final JSlider exposureSlider;
    private final JButton exposurePlusButton;

    private final JButton cycleCameraButton;
    private final JButton playPauseButton;
    private final JButton saveButton;
    private final JButton loadButton;

    private final JToggleButton normalizeSampleValuesButton;

    public CameraPanel(@NonNull Context context) {
        this.context = context;

        this.cameraView = new CameraView();
        this.cameraView.setSampleRowPosRatio(this.context.getModel().getSampleRowPosRatio());
        this.cameraView.setSampleRows(this.context.getModel().getSampleRows());

        this.cameraView.setCalibration(this.context.getModel().getWaveLengthCalibration());
        this.context.getModel().getWaveLengthCalibrationObservers().add(this.cameraView::setCalibration);

        this.cameraView.setFrame(this.context.getModel().getCurrentFrame());
        this.context.getModel().getFrameGrabbedObservers().add(this.cameraView::setFrame);
        this.context.getModel().getSpectrumObservers().add(this.cameraView::setSpectrum);

        this.spectrumReproductionView = new SpectrumReproductionView();
        this.spectrumReproductionView.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        this.spectrumReproductionView.setSpectrum(this.context.getModel().getSpectrum());
        this.context.getModel().getSpectrumObservers().add(this.spectrumReproductionView::setSpectrum);

        this.cycleCameraButton = new JButton();
        this.cycleCameraButton.addActionListener(a -> cycleToNextCamera());

        this.exposureSlider = new JSlider(SwingConstants.HORIZONTAL, -15 * EXPOSURE_RESOLUTION, 10 * EXPOSURE_RESOLUTION, 0);
        this.exposureSlider.addChangeListener(e -> panelToCameraProps());
        this.exposureMinusButton = new JButton("🔅");
        this.exposureMinusButton.addActionListener(e -> this.exposureSlider.setValue(this.exposureSlider.getValue() - 1));
        this.exposurePlusButton = new JButton("\uD83D\uDD06");
        this.exposurePlusButton.addActionListener(e -> this.exposureSlider.setValue(this.exposureSlider.getValue() + 1));

        this.normalizeSampleValuesButton = new JToggleButton("⭱ Normalize");
        this.normalizeSampleValuesButton.addActionListener(e -> this.context.getModel()
                .setNormalizeSampleValues(this.normalizeSampleValuesButton.isSelected()));

        this.playPauseButton = new JButton();
        this.playPauseButton.addActionListener(e -> toggleCameraPaused());

        this.saveButton = new JButton("\uD83E\uDC46\uD83D\uDDCE Save");
        this.saveButton.addActionListener(e -> saveData());
        this.loadButton = new JButton("\uD83D\uDDCE\uD83E\uDC46 Load");
        this.loadButton.addActionListener(e -> loadData());

        final var controlPanel = new JPanel(new FlowLayout());
        controlPanel.add(this.cycleCameraButton);
        controlPanel.add(this.exposureMinusButton);
        controlPanel.add(this.exposureSlider);
        controlPanel.add(this.exposurePlusButton);
        controlPanel.add(this.normalizeSampleValuesButton);
        controlPanel.add(this.playPauseButton);
        controlPanel.add(this.saveButton);
        controlPanel.add(this.loadButton);

        this.panel = new JPanel(new BorderLayout());
        this.panel.add(this.spectrumReproductionView, BorderLayout.NORTH);
        this.panel.add(this.cameraView, BorderLayout.CENTER);
        this.panel.add(controlPanel, BorderLayout.SOUTH);

        cameraToPanel(this.context.getModel().getCamera());
        playPausedToPanel(this.context.getModel().isCameraPaused());

        this.context.getModel().getCameraObservers().add(this::cameraToPanel);
        this.context.getModel().getCameraPropsObservers().add(this::cameraPropsToPanel);
        this.context.getModel().getCameraPausedObservers().add(this::playPausedToPanel);
    }

    public JComponent getComponent() {
        return this.panel;
    }

    private void toggleCameraPaused() {
        this.context.getModel().setCameraPaused(!this.context.getModel().isCameraPaused());
    }

    private void saveData() {
        new SpectrumDataFiles().promptAndSaveFile(this.context, getComponent());
    }

    private void loadData() {
        new SpectrumDataFiles().promptAndLoadFile(this.context, getComponent());
    }

    private void playPausedToPanel(boolean paused) {
        this.playPauseButton.setText(paused ? "▶ Play" : "⏹ Stop");
        this.saveButton.setEnabled(paused);
        this.loadButton.setEnabled(paused);
    }

    private void cameraToPanel(Camera camera) {
        final var camText = camera != null ? "⭮ Cam " + camera.getId() : "⭮ No Cam";
        this.cycleCameraButton.setText(camText);
        cameraPropsToPanel(camera != null ? camera.getCameraProps() : null);
    }

    private void cameraPropsToPanel(CameraProps props) {
        this.exposureSlider.setEnabled(props != null);
        if (props != null) {
            this.exposureSlider.setValue((int) Math.round(props.getExposure() * EXPOSURE_RESOLUTION));
        }
    }

    private void panelToCameraProps() {
        final var oldProps = this.context.getModel().getCameraProps();
        if (oldProps != null) {
            this.context.getModel().setCameraProps(
                    oldProps.withExposure((double) this.exposureSlider.getValue() / EXPOSURE_RESOLUTION));
        }
    }

    private void cycleToNextCamera() {
        final var lastCam = this.context.getModel().getCamera();
        var nextCam = new Camera(lastCam.isOpen() ? lastCam.getId() + 1 : 0);
        if (!nextCam.isOpen()) {
            nextCam.close();
            nextCam = new Camera(0);
        }
        this.context.getModel().setCamera(nextCam);
        lastCam.close();
    }
}
