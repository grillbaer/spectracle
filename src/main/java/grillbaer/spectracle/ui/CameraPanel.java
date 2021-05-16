package grillbaer.spectracle.ui;

import grillbaer.spectracle.Context;
import grillbaer.spectracle.camera.Camera;
import grillbaer.spectracle.camera.CameraProps;
import grillbaer.spectracle.ui.components.CameraView;
import lombok.NonNull;

import javax.swing.*;
import java.awt.*;

public class CameraPanel {
    private final Context context;

    private final JPanel panel;
    private final CameraView cameraView;
    private final JSlider exposureSlider;
    private final JButton cycleCameraButton;
    private final JButton playPauseButton;

    public CameraPanel(@NonNull Context context) {
        this.context = context;

        this.cameraView = new CameraView();
        this.cameraView.setSampleRowPosRatio(this.context.getModel().getSampleRowPosRatio());
        this.cameraView.setSampleRows(this.context.getModel().getSampleRows());

        this.exposureSlider = new JSlider(SwingConstants.HORIZONTAL, -15, 10, 0);
        this.exposureSlider.addChangeListener(e -> panelToCameraProps());

        this.cycleCameraButton = new JButton();
        this.cycleCameraButton.addActionListener(a -> cycleToNextCamera());

        this.playPauseButton = new JButton();
        this.playPauseButton.addActionListener(e -> toggleCameraPaused());

        final var controlPanel = new JPanel(new FlowLayout());
        controlPanel.add(this.cycleCameraButton);
        controlPanel.add(this.exposureSlider);
        controlPanel.add(this.playPauseButton);

        this.panel = new JPanel(new BorderLayout());
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

    private void playPausedToPanel(boolean paused) {
        this.playPauseButton.setText(paused ? "▶ Play" : "⏹ Stop");
    }

    private void cameraToPanel(Camera camera) {
        final var camText = camera != null ? "⭮ Cam " + camera.getId() : "⭮ No Cam";
        this.cycleCameraButton.setText(camText);
        this.cameraView.setCamera(camera);
        this.cameraPropsToPanel(camera != null ? camera.getCameraProps() : null);
    }

    private void cameraPropsToPanel(CameraProps props) {
        this.exposureSlider.setEnabled(props != null);
        if (props != null) {
            this.exposureSlider.setValue((int) props.getExposure());
        }
    }

    private void panelToCameraProps() {
        final var oldProps = this.context.getModel().getCameraProps();
        if (oldProps != null) {
            this.context.getModel().setCameraProps(
                    oldProps.withExposure(this.exposureSlider.getValue()));
        }
    }

    private void cycleToNextCamera() {
        final var lastCam = this.cameraView.getCamera();
        var nextCam = new Camera(lastCam.isOpen() ? lastCam.getId() + 1 : 0);
        if (!nextCam.isOpen()) {
            nextCam.close();
            nextCam = new Camera(0);
        }
        this.context.getModel().setCamera(nextCam);
        lastCam.close();
    }
}
