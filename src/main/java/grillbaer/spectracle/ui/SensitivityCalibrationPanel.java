package grillbaer.spectracle.ui;

import grillbaer.spectracle.Context;
import grillbaer.spectracle.spectrum.KnownSpectrums;
import grillbaer.spectracle.spectrum.Spectrum;
import grillbaer.spectracle.ui.components.Buttons;
import grillbaer.spectracle.ui.components.ColorTemperatureSelector;
import grillbaer.spectracle.ui.components.SpectrumGraphView;
import lombok.NonNull;

import javax.swing.*;
import java.awt.*;

/**
 * Calibration of camera specific sensitivity.
 */
public class SensitivityCalibrationPanel {
    private final Context context;

    private final JPanel panel;

    private final JButton calibrateButton;

    private final ColorTemperatureSelector colorTemperatureSelector;
    private final JButton okButton;
    private final JButton resetButton;
    private final JButton cancelButton;

    private final SpectrumGraphView spectrumGraphView;

    private boolean active;
    private Spectrum referenceLightSpectrum;

    public SensitivityCalibrationPanel(@NonNull Context context, @NonNull SpectrumGraphView spectrumGraphView) {
        this.context = context;
        this.spectrumGraphView = spectrumGraphView;

        final var triangularRuler = "\uD83D\uDCD0";
        this.calibrateButton = new JButton(triangularRuler + " Calibrate \uD83D\uDD06");
        this.calibrateButton.addActionListener(e -> beginCalibration());

        this.colorTemperatureSelector = new ColorTemperatureSelector("Reference Light");
        this.colorTemperatureSelector.getChangeObservers().add(wl -> colorTemperatureSelectionChanged());
        this.colorTemperatureSelector.addActionListener(e -> this.colorTemperatureSelector.transferFocus());

        this.okButton = Buttons.createOkButton();
        this.okButton.setToolTipText("Set measurement as reference light calibration!");
        this.okButton.addActionListener(e -> applyCalibration());

        this.resetButton = Buttons.createResetButton();
        this.resetButton.setToolTipText("Remove sensitivity corrections!");
        this.resetButton.addActionListener(e -> resetCalibration());

        this.cancelButton = Buttons.createCancelButton();
        this.cancelButton.setToolTipText("Abort sensitivity calibration");
        this.cancelButton.addActionListener(e -> abortCalibration());

        this.panel = new JPanel(new FlowLayout());
        this.panel.add(this.calibrateButton);
        this.panel.add(this.colorTemperatureSelector.getComponent());
        this.panel.add(this.okButton);
        this.panel.add(this.resetButton);
        this.panel.add(this.cancelButton);

        updateForCalibration();
        colorTemperatureSelectionChanged();
    }

    public JComponent getComponent() {
        return this.panel;
    }

    private void colorTemperatureSelectionChanged() {
        this.okButton.setEnabled(isValid());
        updateReferenceSpectrum();
    }

    private void updateReferenceSpectrum() {
        if (this.active && isValid()) {
            referenceLightSpectrum =
                    KnownSpectrums.blackBodyRadiationSpectrum(100,
                            colorTemperatureSelector.getValidKelvin(),
                            this.spectrumGraphView.getCalibration().getBeginNanoMeters(),
                            this.spectrumGraphView.getCalibration().getEndNanoMeters());
            this.spectrumGraphView.setReferenceLightSpectrum(referenceLightSpectrum);
            this.spectrumGraphView.setSensitivityCorrectionSpectrum(this.context.getModel()
                    .getSensitivityCalibration());
        } else {
            this.spectrumGraphView.setReferenceLightSpectrum(null);
            this.spectrumGraphView.setSensitivityCorrectionSpectrum(null);
        }
    }

    private boolean isValid() {
        return this.colorTemperatureSelector.getValidKelvin() != null;
    }

    private void beginCalibration() {
        this.active = true;
        updateForCalibration();
    }

    private void abortCalibration() {
        this.active = false;
        updateForCalibration();
    }

    private void applyCalibration() {
        this.active = false;

        if (this.referenceLightSpectrum != null) {
            this.context.getModel().calibrateSensitivityWithReferenceLight(this.referenceLightSpectrum);
        }

        updateForCalibration();
    }

    private void resetCalibration() {
        this.active = false;
        final var cameraId = this.context.getModel().getCameraId();
        if (cameraId != null) {
            this.context.getModel().setSensitivityCalibration(cameraId, null);
        }
        updateForCalibration();
    }

    private void updateForCalibration() {
        this.calibrateButton.setEnabled(!this.active);
        this.colorTemperatureSelector.getComponent().setVisible(this.active);
        this.okButton.setVisible(this.active);
        this.resetButton.setVisible(this.active);
        this.cancelButton.setVisible(this.active);
        updateReferenceSpectrum();
    }
}
