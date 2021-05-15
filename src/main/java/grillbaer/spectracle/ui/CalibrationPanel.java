package grillbaer.spectracle.ui;

import grillbaer.spectracle.Context;
import grillbaer.spectracle.spectrum.Calibration;
import grillbaer.spectracle.spectrum.NamedWaveLength;
import grillbaer.spectracle.spectrum.WaveLengths;
import grillbaer.spectracle.ui.components.Buttons;
import grillbaer.spectracle.ui.components.Cursor;
import grillbaer.spectracle.ui.components.SpectrumView;
import grillbaer.spectracle.ui.components.WaveLengthSelector;
import lombok.NonNull;

import javax.swing.*;
import java.awt.*;

public class CalibrationPanel {
    public static final Color CAL_A_COLOR = new Color(255, 100, 255);
    public static final Color CAL_B_COLOR = new Color(90, 220, 90);

    private final Context context;

    private final JPanel panel;

    private final JButton calibrateButton;

    private final WaveLengthSelector calAWaveLengthSelector;
    private final WaveLengthSelector calBWaveLengthSelector;
    private final JButton okButton;
    private final JButton cancelButton;

    private final SpectrumView spectrumView;
    private Cursor cursorA;
    private Cursor cursorB;

    public CalibrationPanel(@NonNull Context context, @NonNull SpectrumView spectrumView) {
        this.context = context;
        this.spectrumView = spectrumView;

        this.calibrateButton = new JButton("\uD83D\uDCD0 Calibrate");
        this.calibrateButton.addActionListener(e -> setCalibrationMode(true));

        this.calAWaveLengthSelector = new WaveLengthSelector("<html> λ<sub>A </sub></html>", CAL_A_COLOR);
        this.calBWaveLengthSelector = new WaveLengthSelector("<html> λ<sub>B </sub></html>", CAL_B_COLOR);

        this.okButton = Buttons.createOkButton();
        this.okButton.setToolTipText("Set new calibration!");
        this.okButton.addActionListener(e -> {
            applyCalibration();
            setCalibrationMode(false);
        });
        this.cancelButton = Buttons.createCancelButton();
        this.cancelButton.setToolTipText("Abort calibration");
        this.cancelButton.addActionListener(e -> setCalibrationMode(false));

        this.panel = new JPanel(new FlowLayout());
        this.panel.add(this.calibrateButton);
        this.panel.add(this.calAWaveLengthSelector.getComponent());
        this.panel.add(this.calBWaveLengthSelector.getComponent());
        this.panel.add(this.okButton);
        this.panel.add(this.cancelButton);

        this.cursorA = new Cursor("CalA", 400., () -> "Cal A " + WaveLengths.format(this.cursorA.getValue()), CAL_A_COLOR);
        this.cursorA.setDraggable(true);
        this.cursorB = new Cursor("CalB", 700., () -> "Cal B " + WaveLengths.format(this.cursorB.getValue()), CAL_B_COLOR);
        this.cursorB.setDraggable(true);

        setCalibrationMode(false);
    }

    public JComponent getComponent() {
        return this.panel;
    }

    private void setCalibrationMode(boolean active) {
        this.calibrateButton.setEnabled(!active);
        this.calAWaveLengthSelector.getComponent().setVisible(active);
        this.calBWaveLengthSelector.getComponent().setVisible(active);
        this.okButton.setVisible(active);
        this.cancelButton.setVisible(active);

        if (active) {
            this.spectrumView.putXCursor(cursorA);
            this.spectrumView.putXCursor(cursorB);
        } else {
            this.spectrumView.removeXCursor("CalA");
            this.spectrumView.removeXCursor("CalB");
        }
    }

    private void applyCalibration() {
        final NamedWaveLength targetWLA = this.calAWaveLengthSelector.getValidWaveLength();
        final NamedWaveLength targetWLB = this.calBWaveLengthSelector.getValidWaveLength();
        if (targetWLA != null && targetWLB != null) {
            final var spectrum = this.spectrumView.getSpectrum();
            final var oldCal = this.context.getModel().getCalibration();
            final double targetRatioA = (double) oldCal.nanoMetersToIndex(spectrum.getLength(), cursorA.getValue())
                    / spectrum.getLength();
            final double targetRatioB = (double) oldCal.nanoMetersToIndex(spectrum.getLength(), cursorB.getValue())
                    / spectrum.getLength();
            final var newCal = Calibration.create(
                    new Calibration.Point(targetRatioA, targetWLA.getNanoMeters()),
                    new Calibration.Point(targetRatioB, targetWLB.getNanoMeters()));
            this.context.getModel().setCalibration(newCal);
        }
    }
}
