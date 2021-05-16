package grillbaer.spectracle.ui.components;

import javax.swing.*;
import java.awt.*;

public final class Buttons {
    private Buttons() {
        // no instances
    }

    public static JButton createFlatButton(String text, Icon icon) {
        final var button = new JButton(text, icon);
        button.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        button.setBackground(null);

        return button;
    }

    public static JButton createOkButton() {
        final var button = new JButton("✅", null);
        button.setFont(new Font(Font.DIALOG, Font.PLAIN, 16));
        button.setForeground(Color.GREEN.darker());

        return button;
    }

    public static JButton createCancelButton() {
        final var button = new JButton("❌", null);
        button.setFont(new Font(Font.DIALOG, Font.PLAIN, 16));
        button.setForeground(Color.RED.darker());

        return button;
    }
}
