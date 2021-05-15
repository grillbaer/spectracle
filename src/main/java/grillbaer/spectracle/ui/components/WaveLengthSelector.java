package grillbaer.spectracle.ui.components;

import grillbaer.spectracle.spectrum.NamedWaveLength;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.regex.Pattern;

public class WaveLengthSelector {
    private final JPanel panel;
    private final JComboBox<NamedWaveLength> comboBox;
    private static final Pattern WAVE_LENGTH_PATTERN = Pattern.compile("\\s*[0-9]+([.,][0-9]*)?\\s*");

    public WaveLengthSelector(String labelText, Color labelColor) {
        this.panel = new JPanel(new FlowLayout());

        this.comboBox = new JComboBox<>();
        this.comboBox.setEditable(true);
        this.comboBox.addActionListener(e -> verifyInput());
        this.comboBox.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                verifyInput();
            }
        });

        if (labelText != null) {
            final var label = new JLabel(labelText);
            if (labelColor != null) {
                label.setForeground(labelColor);
            }
            label.setLabelFor(this.comboBox);
            this.panel.add(label);
        }

        this.panel.add(comboBox);
        this.panel.add(new JLabel("nm"));
    }

    public JComponent getComponent() {
        return this.panel;
    }

    public NamedWaveLength getValidWaveLength() {
        final var selected = this.comboBox.getSelectedItem();
        if (selected instanceof NamedWaveLength namedWaveLength) {
            return namedWaveLength;
        } else if (selected instanceof String text) {
            return new NamedWaveLength("entered", textToWaveLength(text));
        } else {
            return null;
        }
    }

    private void verifyInput() {
        final boolean valid =
                this.comboBox.getSelectedItem() instanceof NamedWaveLength
                        || (this.comboBox.getSelectedItem() instanceof String text && textToWaveLength(text) != null);
        this.comboBox.setBackground(valid ? null : Color.RED.darker());
    }

    private Double textToWaveLength(String text) {
        if (text == null)
            return null;
        if (!WAVE_LENGTH_PATTERN.matcher(text).matches())
            return null;

        var waveLength = Double.parseDouble(text.trim().replace(',', '.'));

        if (waveLength < 200 || waveLength > 1500)
            return null;

        return waveLength;
    }
}
