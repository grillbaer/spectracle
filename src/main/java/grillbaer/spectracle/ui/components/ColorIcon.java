package grillbaer.spectracle.ui.components;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import javax.swing.*;
import java.awt.*;

@AllArgsConstructor
@Getter
public class ColorIcon implements Icon {
    private int iconWidth;
    private int iconHeight;
    private Color color;

    public ColorIcon(@NonNull Color color) {
        this(11, 7, color);
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        final var origColor = g.getColor();
        g.setColor(this.color);
        g.fillRect(x, y, this.iconWidth - 1, this.iconHeight - 1);
        g.setColor(UIManager.getColor("Button.disabledText"));
        g.drawRect(x, y, this.iconWidth - 1, this.iconHeight - 1);
        g.setColor(origColor);
    }
}
