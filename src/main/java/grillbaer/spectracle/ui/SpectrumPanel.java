package grillbaer.spectracle.ui;

import grillbaer.spectracle.Context;
import grillbaer.spectracle.spectrum.Formatting;
import grillbaer.spectracle.spectrum.KnownSpectrums;
import grillbaer.spectracle.spectrum.NamedWaveLength;
import grillbaer.spectracle.spectrum.NamedWaveLengthGroup;
import grillbaer.spectracle.ui.components.Cursor;
import grillbaer.spectracle.ui.components.RenderUtils;
import grillbaer.spectracle.ui.components.SpectrumGraphView;
import lombok.NonNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static grillbaer.spectracle.spectrum.Formatting.formatWaveLength;
import static java.lang.Math.exp;
import static javax.swing.SwingConstants.HORIZONTAL;

public class SpectrumPanel {
    private final Context context;

    private final JPanel panel;
    private final SpectrumGraphView spectrumGraphView;
    private final WaveLengthCalibrationPanel waveLengthCalibrationPanel;
    private final SensitivityCalibrationPanel sensitivityCalibrationPanel;
    private final JLabel smoothLabel;
    private final JLabel timeAverageLabel;

    private final List<Cursor> commonWaveLengthCursors = new ArrayList<>();

    public SpectrumPanel(@NonNull Context context) {
        this.context = context;

        this.panel = new JPanel(new BorderLayout());
        this.spectrumGraphView = new SpectrumGraphView();

        this.waveLengthCalibrationPanel = new WaveLengthCalibrationPanel(this.context, this.spectrumGraphView);
        this.sensitivityCalibrationPanel = new SensitivityCalibrationPanel(this.context, this.spectrumGraphView);

        final var knownWaveLengthsComboBox = new JComboBox<>(new NamedWaveLengthGroup[]{
                new NamedWaveLengthGroup("No Known Wavelengths", List.of()),
                KnownSpectrums.FLUORESCENT_LAMP_WAVELENGTHS,
                KnownSpectrums.FRAUNHOFER_WAVELENGTHS
        });
        knownWaveLengthsComboBox.addActionListener(
                e -> showKnownWaveLengths(((NamedWaveLengthGroup) knownWaveLengthsComboBox.getSelectedItem())));

        final JToggleButton drawMaximaButton = new JToggleButton("⋀");
        drawMaximaButton.addActionListener(e -> this.spectrumGraphView.setDrawMaxima(drawMaximaButton.isSelected()));
        drawMaximaButton.setSelected(true);
        this.spectrumGraphView.setDrawMaxima(true);

        final JToggleButton drawMinimaButton = new JToggleButton("⋁");
        drawMinimaButton.addActionListener(e -> this.spectrumGraphView.setDrawMinima(drawMinimaButton.isSelected()));

        final JSlider timeAverageSlider = new JSlider(HORIZONTAL, 0, 100, 0);
        this.timeAverageLabel = new JLabel();
        timeAverageSlider.addChangeListener(e -> {
            this.context.getModel().setTimeAveragingFactor(getExpSliderValue(timeAverageSlider, 0.95, 0., true));
            updateProcessingLabels();
        });

        final JSlider smoothSlider = new JSlider(HORIZONTAL, 0, 100, 0);
        this.smoothLabel = new JLabel();
        smoothSlider.addChangeListener(e -> {
            this.context.getModel().setSmoothIndexSteps(getExpSliderValue(smoothSlider, 0., 5., false));
            updateProcessingLabels();
        });

        final var controlPanel = new JPanel(new FlowLayout());
        controlPanel.add(this.waveLengthCalibrationPanel.getComponent());
        controlPanel.add(this.sensitivityCalibrationPanel.getComponent());
        controlPanel.add(knownWaveLengthsComboBox);
        controlPanel.add(drawMaximaButton);
        controlPanel.add(drawMinimaButton);
        controlPanel.add(timeAverageSlider);
        controlPanel.add(this.timeAverageLabel);
        controlPanel.add(smoothSlider);
        controlPanel.add(this.smoothLabel);

        this.panel.add(this.spectrumGraphView, BorderLayout.CENTER);
        this.panel.add(controlPanel, BorderLayout.SOUTH);

        this.context.getModel().getSpectrumObservers()
                .add(spectrum -> {
                    this.spectrumGraphView.setSpectrum(spectrum);
                    this.spectrumGraphView.setExtrema(this.context.getModel().getExtrema());
                });

        updateProcessingLabels();
    }

    private double getExpSliderValue(@NonNull JSlider slider, double begin, double end, boolean inverse) {
        var ratio = (double) (slider.getValue() - slider.getMinimum())
                / (slider.getMaximum() - slider.getMinimum());
        if (inverse) {
            ratio = 1. - ratio;
        }

        return (exp(ratio) - 1.) / (Math.E - 1.) * (end - begin) + begin;
    }

    private void updateProcessingLabels() {
        this.timeAverageLabel.setText(String.format("%.2f", this.context.getModel().getTimeAveragingFactor()));
        this.smoothLabel.setText(String.format("%.1f", this.context.getModel().getSmoothIndexSteps()));
    }

    public JComponent getComponent() {
        return this.panel;
    }

    private void showKnownWaveLengths(@NonNull NamedWaveLengthGroup waveLengthGroup) {
        this.commonWaveLengthCursors.forEach(cursor -> this.spectrumGraphView.removeXCursor(cursor.getId()));
        this.commonWaveLengthCursors.clear();

        for (NamedWaveLength waveLength : waveLengthGroup.getWaveLengthList()) {
            final var waveLengthColor = Formatting.colorForWaveLength(waveLength.getNanoMeters());
            final var cursor = new Cursor("common_wl_" + waveLength.getName(), waveLength.getNanoMeters(),
                    () -> formatWaveLength(waveLength.getNanoMeters()) + " "
                            + waveLengthGroup.getGroupName() + " " + waveLength.getName(),
                    new Color(255, 255, 255, 180), RenderUtils.whitenColor(waveLengthColor, 0.3));
            this.commonWaveLengthCursors.add(cursor);
            this.spectrumGraphView.putXCursor(cursor);
        }
    }
}
