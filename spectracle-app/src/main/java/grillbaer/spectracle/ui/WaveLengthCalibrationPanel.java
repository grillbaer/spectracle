package grillbaer.spectracle.ui;

import grillbaer.spectracle.Context;
import grillbaer.spectracle.spectrum.WaveLengthCalibration;
import grillbaer.spectracle.spectrum.WaveLengthCalibration.Point;
import grillbaer.spectracle.ui.components.Cursor;
import grillbaer.spectracle.ui.components.*;
import lombok.NonNull;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static grillbaer.spectracle.spectrum.Formatting.colorForWaveLength;

public class WaveLengthCalibrationPanel {
    private static final int MIN_CALIBRATION_POINTS = 2;
    private static final int MAX_CALIBRATION_POINTS = 5;

    private final Context context;

    private final JPanel panel;

    private final JButton calibrateButton;

    private final List<WaveLengthSelector> waveLengthSelectors = new ArrayList<>();
    private final JButton addCalibrationPointButton;
    private final JButton removeCalibrationPointButton;
    private final JButton okButton;
    private final JButton cancelButton;

    private final SpectrumGraphView spectrumGraphView;
    private final List<Cursor> cursors = new ArrayList<>();

    private boolean active;
    private final JPanel waveLengthSelectorPanel;

    public WaveLengthCalibrationPanel(@NonNull Context context, @NonNull SpectrumGraphView spectrumGraphView) {
        this.context = context;
        this.spectrumGraphView = spectrumGraphView;

        final var triangularRuler = "\uD83D\uDCD0";
        this.calibrateButton = new JButton(triangularRuler + " Calibrate λ");
        this.calibrateButton.addActionListener(e -> beginCalibration());

        this.addCalibrationPointButton = new JButton("Add Point");
        this.addCalibrationPointButton.addActionListener(e -> addCalibrationPoint());
        this.removeCalibrationPointButton = new JButton("Remove Point");
        this.removeCalibrationPointButton.addActionListener(e -> removeCalibrationPoint());

        this.okButton = Buttons.createOkButton();
        this.okButton.setToolTipText("Set new calibration!");
        this.okButton.addActionListener(e -> applyCalibration());

        this.cancelButton = Buttons.createCancelButton();
        this.cancelButton.setToolTipText("Abort calibration");
        this.cancelButton.addActionListener(e -> abortCalibration());

        this.waveLengthSelectorPanel = new JPanel(new MigLayout());
        addCalibrationPoint();
        addCalibrationPoint();

        this.panel = new JPanel(new FlowLayout());
        this.panel.add(this.calibrateButton);
        this.panel.add(waveLengthSelectorPanel);
        this.panel.add(this.okButton);
        this.panel.add(this.cancelButton);

        updateForCalibration();
    }

    public JComponent getComponent() {
        return this.panel;
    }

    private void addCalibrationPoint() {
        removeNumberOfCalibrationPointsButtons();

        final var index = this.waveLengthSelectors.size();
        final var label = "λ" + (char) ('₁' + index);
        final var newNanoMeters = switch (index) {
            case 0 -> WaveLengthCalibration.createDefault().getPoint(0).getNanoMeters();
            case 1 -> WaveLengthCalibration.createDefault().getPoint(1).getNanoMeters();
            default -> (this.cursors.get(index - 1).getValue() + this.spectrumGraphView.getCalibration()
                    .getEndNanoMeters()) / 2.;
        };

        final var selector = new WaveLengthSelector(label, null);
        selector.setWaveLength(newNanoMeters);
        selector.getChangeObservers().add(wl -> waveLengthSelectionChanged(index));
        selector.addActionListener(e -> selector.transferFocus());
        this.waveLengthSelectorPanel.add(selector.getComponent(), "span 2, wrap 0");
        this.waveLengthSelectors.add(selector);

        final var cursor = new Cursor("Cal" + index, 400. + index * 100.,
                () -> getCursorLabel(selector, label), new Color(255, 100, 255), Color.WHITE);
        cursor.setDraggable(true);
        cursor.setValue(newNanoMeters);
        this.cursors.add(cursor);
        updateCursors();

        addNumberOfCalibrationPointsButtons();
        this.waveLengthSelectorPanel.revalidate();
        waveLengthSelectionChanged(index);
    }

    private void removeCalibrationPoint() {
        removeNumberOfCalibrationPointsButtons();

        final var selector = this.waveLengthSelectors.remove(this.waveLengthSelectors.size() - 1);
        this.waveLengthSelectorPanel.remove(selector.getComponent());

        final var cursor = this.cursors.remove(this.cursors.size() - 1);
        this.spectrumGraphView.removeXCursor(cursor.getId());
        updateCursors();

        addNumberOfCalibrationPointsButtons();
        this.waveLengthSelectorPanel.revalidate();
    }

    private void addNumberOfCalibrationPointsButtons() {
        if (this.waveLengthSelectors.size() < MAX_CALIBRATION_POINTS) {
            this.waveLengthSelectorPanel.add(this.addCalibrationPointButton);
        }
        if (this.waveLengthSelectors.size() > MIN_CALIBRATION_POINTS) {
            this.waveLengthSelectorPanel.add(this.removeCalibrationPointButton);
        }
    }

    private void removeNumberOfCalibrationPointsButtons() {
        this.waveLengthSelectorPanel.remove(this.addCalibrationPointButton);
        this.waveLengthSelectorPanel.remove(this.removeCalibrationPointButton);
    }

    private int getNumberOfCalibrationPoints() {
        return this.waveLengthSelectors.size();
    }

    private void setNumberOfCalibrationPoints(int size) {
        while (getNumberOfCalibrationPoints() < size) addCalibrationPoint();
        while (getNumberOfCalibrationPoints() > size) removeCalibrationPoint();
    }

    private static String getCursorLabel(@NonNull WaveLengthSelector waveLengthSelector, @NonNull String waveLengthLabel) {
        final var baseLabel = "<html>Calibration<br>Set " + waveLengthLabel;

        final var waveLength = waveLengthSelector.getValidWaveLength();
        return waveLength != null ? baseLabel + " = " + waveLength.getWaveLengthNameString(true) + " here" : baseLabel;
    }

    private void waveLengthSelectionChanged(int index) {
        final var selector = this.waveLengthSelectors.get(index);
        final var waveLength = selector.getValidWaveLength();
        final var color = waveLength != null
                ? RenderUtils.whitenColor(colorForWaveLength(waveLength.getNanoMeters()), 0.6)
                : null;
        selector.setLabelColor(color);
        this.cursors.get(index).setLabelColor(color != null ? color : Color.LIGHT_GRAY);

        this.okButton.setEnabled(isValid());
        updateCursors();
    }

    private boolean isValid() {
        return getValidWaveLengthPoints() != null;
    }

    private void beginCalibration() {
        this.active = true;

        final var calibration = this.context.getModel().getWaveLengthCalibration();
        setNumberOfCalibrationPoints(calibration.getSize());
        for (int i = 0; i < calibration.getSize(); i++) {
            final var nanoMeters = calibration.getPoint(i).getNanoMeters();
            this.waveLengthSelectors.get(i).setWaveLength(nanoMeters);
            this.cursors.get(i).setValue(nanoMeters);
        }

        updateForCalibration();
    }

    private void abortCalibration() {
        this.active = false;
        updateForCalibration();
    }

    private void applyCalibration() {
        this.active = false;

        if (isValid()) {
            final var calPoints = getValidWaveLengthPoints();
            if (calPoints != null) {
                final var newCal = WaveLengthCalibration.create(calPoints);
                this.context.getModel().setWaveLengthCalibration(newCal);
            }
        }


        updateForCalibration();
    }

    private List<Point> getValidWaveLengthPoints() {
        if (getNumberOfCalibrationPoints() < 2)
            return null; // NOSONAR want this for invalid

        final var oldCal = this.context.getModel().getWaveLengthCalibration();
        final var calPoints = new ArrayList<Point>();
        for (int i = 0; i < getNumberOfCalibrationPoints(); i++) {
            final var targetWaveLength = this.waveLengthSelectors.get(i).getValidWaveLength();
            if (targetWaveLength == null)
                return null; // NOSONAR want this for invalid

            final double targetRatio = oldCal.nanoMetersToRatio(this.cursors.get(i).getValue());
            calPoints.add(new Point(targetRatio, targetWaveLength.getNanoMeters()));
        }

        calPoints.sort(Comparator.comparing(Point::getNanoMeters));
        if (!WaveLengthCalibration.areRatioAndWaveLengthStrictlyMonotonic(calPoints))
            return null; // NOSONAR want this for invalid

        return calPoints;
    }

    private void updateForCalibration() {
        this.calibrateButton.setEnabled(!this.active);
        this.waveLengthSelectorPanel.setVisible(this.active);
        this.okButton.setVisible(this.active);
        this.cancelButton.setVisible(this.active);

        updateCursors();
    }

    private void updateCursors() {
        if (this.active) {
            this.cursors.forEach(this.spectrumGraphView::putXCursor);
        } else {
            this.cursors.forEach(c -> this.spectrumGraphView.removeXCursor(c.getId()));
        }
    }
}
