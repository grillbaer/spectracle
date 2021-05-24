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

public class SpectrumPanel {
    private final Context context;

    private final JPanel panel;
    private final SpectrumGraphView spectrumGraphView;
    private final WaveLengthCalibrationPanel waveLengthCalibrationPanel;
    private final SensitivityCalibrationPanel sensitivityCalibrationPanel;
    private final List<Cursor> commonWaveLengthCursors = new ArrayList<>();

    public SpectrumPanel(@NonNull Context context) {
        this.context = context;

        this.panel = new JPanel(new BorderLayout());
        this.spectrumGraphView = new SpectrumGraphView();

        final var knownWaveLengthsComboBox = new JComboBox<>(new NamedWaveLengthGroup[]{
                new NamedWaveLengthGroup("No Known Wavelengths", List.of()),
                KnownSpectrums.FLUORESCENT_LAMP_WAVELENGTHS,
                KnownSpectrums.FRAUNHOFER_WAVELENGTHS
        });
        knownWaveLengthsComboBox.addActionListener(
                e -> showKnownWaveLengths(((NamedWaveLengthGroup) knownWaveLengthsComboBox.getSelectedItem())));

        this.waveLengthCalibrationPanel = new WaveLengthCalibrationPanel(this.context, this.spectrumGraphView);
        this.sensitivityCalibrationPanel = new SensitivityCalibrationPanel(this.context, this.spectrumGraphView);

        final var controlPanel = new JPanel(new FlowLayout());
        controlPanel.add(this.waveLengthCalibrationPanel.getComponent());
        controlPanel.add(this.sensitivityCalibrationPanel.getComponent());
        controlPanel.add(knownWaveLengthsComboBox);

        this.panel.add(this.spectrumGraphView, BorderLayout.CENTER);
        this.panel.add(controlPanel, BorderLayout.SOUTH);

        this.context.getModel().getSpectrumObservers()
                .add(this.spectrumGraphView::setSpectrum);
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
                    () -> waveLengthGroup.getGroupName() + " " + waveLength.getName() + " "
                            + Formatting.formatWaveLength(waveLength.getNanoMeters()),
                    new Color(255, 255, 255, 180), RenderUtils.whitenColor(waveLengthColor, 0.3));
            this.commonWaveLengthCursors.add(cursor);
            this.spectrumGraphView.putXCursor(cursor);
        }
    }
}
