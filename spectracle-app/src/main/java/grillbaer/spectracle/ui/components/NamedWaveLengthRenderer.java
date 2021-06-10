package grillbaer.spectracle.ui.components;

import grillbaer.spectracle.spectrum.Formatting;
import grillbaer.spectracle.spectrum.NamedWaveLength;
import lombok.AllArgsConstructor;

import javax.swing.*;
import java.awt.*;

import static grillbaer.spectracle.spectrum.NamedWaveLength.Component.*;

@AllArgsConstructor
public class NamedWaveLengthRenderer extends DefaultListCellRenderer {

    private boolean withName;

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        final var label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof NamedWaveLength namedWaveLength) {
            label.setText(namedWaveLength.format(WAVELENGTH, GROUP, NAME));
            label.setIcon(new ColorIcon(Formatting.colorForWaveLength(namedWaveLength.getNanoMeters())));
        }

        return label;
    }
}
