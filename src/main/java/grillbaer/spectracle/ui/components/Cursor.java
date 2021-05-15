package grillbaer.spectracle.ui.components;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.awt.*;
import java.util.function.Supplier;

@Getter
@Setter
public class Cursor {
    private final @NonNull String id;

    private double value;
    private Supplier<String> labelSupplier;
    private boolean draggable;
    private boolean dragging;

    private @NonNull Color color;
    private @NonNull Color draggingColor;
    private @NonNull Stroke stroke;
    private @NonNull Color bgColor;
    private @NonNull Stroke bgStroke;

    public Cursor(@NonNull String id, double value, Supplier<String> labelSupplier, @NonNull Color color) {
        this.id = id;
        this.value = value;
        this.labelSupplier = labelSupplier;
        this.draggable = false;
        this.dragging = false;

        this.color = color;
        this.draggingColor = new Color(255, 255, 255, 200);
        this.stroke = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0f, new float[]{5f, 8f}, 0f);

        this.bgColor = new Color(0, 0, 0, 50);
        this.bgStroke = new BasicStroke(3f);
    }

    public void draw(Graphics2D g2, int x0, int y0, int x1, int y1) {
        final var origStroke = g2.getStroke();

        g2.setColor(this.getBgColor());
        g2.setStroke(this.bgStroke);
        g2.drawLine(x0, y0, x1, y1);

        final var fgColor = dragging ? this.draggingColor : this.color;
        g2.setColor(fgColor);
        g2.setStroke(this.stroke);
        g2.drawLine(x0, y0, x1, y1);

        g2.setStroke(origStroke);

        final var label = this.labelSupplier != null ? this.labelSupplier.get() : null;
        if (label != null) {
            g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            final var fm = g2.getFontMetrics();
            final var labelX = Math.min(x0, x1) + 4;
            final var labelY = Math.min(y0, y1) + 4 + fm.getAscent();
            final var labelBounds = new Rectangle(labelX, labelY - fm.getAscent(),
                    fm.stringWidth(label), fm.getAscent() + fm.getDescent());
            g2.setColor(this.bgColor);
            g2.fillRect(labelBounds.x - 1, labelBounds.y - 1, labelBounds.width + 3, labelBounds.height + 2);
            g2.setColor(fgColor);
            g2.drawString(label, labelX, labelY);
        }
    }
}
