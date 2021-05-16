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
    public static final Color CAL_0_COLOR = new Color(255, 100, 255);
    public static final Color CAL_1_COLOR = new Color(90, 220, 90);

    private final Context context;

    private final JPanel panel;

    private final JButton calibrateButton;

    private final WaveLengthSelector cal0WaveLengthSelector;
    private final WaveLengthSelector cal1WaveLengthSelector;
    private final JButton okButton;
    private final JButton cancelButton;

    private final SpectrumView spectrumView;
    private final Cursor cursor0;
    private final Cursor cursor1;

    public CalibrationPanel(@NonNull Context context, @NonNull SpectrumView spectrumView) {
        this.context = context;
        this.spectrumView = spectrumView;

        final var triangularRuler = "\uD83D\uDCD0";
        this.calibrateButton = new JButton(triangularRuler + " Calibrate");
        this.calibrateButton.addActionListener(e -> beginCalibration());

        this.cal0WaveLengthSelector = new WaveLengthSelector("λ₀=", CAL_0_COLOR);
        this.cal0WaveLengthSelector.getChangeObservers().add(wl -> updateEnabled());
        this.cal0WaveLengthSelector.addActionListener(e -> this.cal0WaveLengthSelector.transferFocus());

        this.cal1WaveLengthSelector = new WaveLengthSelector("λ₁=", CAL_1_COLOR);
        this.cal1WaveLengthSelector.getChangeObservers().add(wl -> updateEnabled());
        this.cal1WaveLengthSelector.addActionListener(e -> this.cal1WaveLengthSelector.transferFocus());

        this.okButton = Buttons.createOkButton();
        this.okButton.setToolTipText("Set new calibration!");
        this.okButton.addActionListener(e -> applyCalibration());

        this.cancelButton = Buttons.createCancelButton();
        this.cancelButton.setToolTipText("Abort calibration");
        this.cancelButton.addActionListener(e -> abortCalibration());

        this.panel = new JPanel(new FlowLayout());
        this.panel.add(this.calibrateButton);
        this.panel.add(this.cal0WaveLengthSelector.getComponent());
        this.panel.add(this.cal1WaveLengthSelector.getComponent());
        this.panel.add(this.okButton);
        this.panel.add(this.cancelButton);

        this.cursor0 = new Cursor("Cal0", 400.,
                () -> getCursorLabel(this.cal0WaveLengthSelector, "λ₀"), CAL_0_COLOR);
        this.cursor0.setDraggable(true);
        this.cursor1 = new Cursor("Cal1", 700.,
                () -> getCursorLabel(this.cal1WaveLengthSelector, "λ₁"), CAL_1_COLOR);
        this.cursor1.setDraggable(true);

        updateForCalibration(false);
        updateEnabled();
    }

    public JComponent getComponent() {
        return this.panel;
    }

    private static String getCursorLabel(@NonNull WaveLengthSelector waveLengthSelector, @NonNull String waveLengthLabel) {
        final var nanoMeters = waveLengthSelector.getValidWaveLength();
        final var baseLabel = "Cal. set " + waveLengthLabel;

        return nanoMeters != null ? baseLabel + "=" + WaveLengths.format(nanoMeters.getNanoMeters()) + " here" : baseLabel;
    }

    private void updateEnabled() {
        this.okButton.setEnabled(isValid());
    }

    private boolean isValid() {
        final var wl0 = this.cal0WaveLengthSelector.getValidWaveLength();
        final var wl1 = this.cal1WaveLengthSelector.getValidWaveLength();

        return wl0 != null && wl1 != null && wl0.getNanoMeters() != wl1.getNanoMeters();
    }

    private void beginCalibration() {
        final var calibration = this.context.getModel().getCalibration();
        final var waveLengthA = calibration.getWaveLengthCal0().getNanoMeters();
        final var waveLengthB = calibration.getWaveLengthCal1().getNanoMeters();
        this.cursor0.setValue(waveLengthA);
        this.cursor1.setValue(waveLengthB);
        this.cal0WaveLengthSelector.setWaveLength(waveLengthA);
        this.cal1WaveLengthSelector.setWaveLength(waveLengthB);

        updateForCalibration(true);
    }

    private void abortCalibration() {
        updateForCalibration(false);
    }

    private void applyCalibration() {
        final NamedWaveLength targetWLA = this.cal0WaveLengthSelector.getValidWaveLength();
        final NamedWaveLength targetWLB = this.cal1WaveLengthSelector.getValidWaveLength();
        if (targetWLA != null && targetWLB != null) {
            final var spectrum = this.spectrumView.getSpectrum();
            final var oldCal = this.context.getModel().getCalibration();
            final double targetRatioA = (double) oldCal.nanoMetersToIndex(spectrum.getLength(), cursor0.getValue())
                    / spectrum.getLength();
            final double targetRatioB = (double) oldCal.nanoMetersToIndex(spectrum.getLength(), cursor1.getValue())
                    / spectrum.getLength();
            final var newCal = Calibration.create(
                    new Calibration.Point(targetRatioA, targetWLA.getNanoMeters()),
                    new Calibration.Point(targetRatioB, targetWLB.getNanoMeters()));
            this.context.getModel().setCalibration(newCal);
        }

        updateForCalibration(false);
    }

    private void updateForCalibration(boolean active) {
        this.calibrateButton.setEnabled(!active);
        this.cal0WaveLengthSelector.getComponent().setVisible(active);
        this.cal1WaveLengthSelector.getComponent().setVisible(active);
        this.okButton.setVisible(active);
        this.cancelButton.setVisible(active);

        if (active) {
            this.spectrumView.putXCursor(cursor0);
            this.spectrumView.putXCursor(cursor1);
        } else {
            this.spectrumView.removeXCursor(cursor0.getId());
            this.spectrumView.removeXCursor(cursor1.getId());
        }
    }
}
