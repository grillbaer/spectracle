package grillbaer.spectracle.ui.components;

import grillbaer.spectracle.ui.components.RenderUtils.Alignment;
import grillbaer.spectracle.ui.components.RenderUtils.Direction;
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

    private @NonNull Color lineColor;
    private @NonNull Color draggingColor;
    private @NonNull Stroke stroke;
    private @NonNull Color bgColor;
    private @NonNull Stroke bgStroke;
    private @NonNull Color labelColor;
    private @NonNull Font labelFont;

    public Cursor(@NonNull String id, double value, Supplier<String> labelSupplier, @NonNull Color lineColor, Color labelColor) {
        this.id = id;
        this.value = value;
        this.labelSupplier = labelSupplier;
        this.draggable = false;
        this.dragging = false;

        this.lineColor = lineColor;
        this.draggingColor = new Color(255, 255, 255, 220);
        this.stroke = new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                0f, new float[]{0f, 6f}, 0f);

        this.bgColor = new Color(0, 0, 0, 120);
        this.bgStroke = new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                0f, new float[]{0f, 6f}, 3f);

        this.labelColor = labelColor != null ? labelColor : lineColor;
        this.labelFont = new Font(Font.SANS_SERIF, Font.PLAIN, 11);
    }

    public void draw(Graphics2D g2, int x0, int y0, int x1, int y1, Rectangle overrideLabelBounds) {
        final var origStroke = g2.getStroke();

        final var label = this.labelSupplier != null ? this.labelSupplier.get() : null;
        final Rectangle labelBounds;
        if (overrideLabelBounds != null) {
            labelBounds = overrideLabelBounds;
        } else if (label != null) {
            labelBounds = calcDefaultLabelBounds(g2, x0, y0, x1, y1, label);
        } else {
            labelBounds = null;
        }

        final var origHints = g2.getRenderingHints();
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (label != null && labelBounds != null) {
            g2.setFont(this.labelFont);
            g2.setColor(this.bgColor);
            g2.fillRect(labelBounds.x - 1, labelBounds.y - 1, labelBounds.width + 2, labelBounds.height + 2);
            g2.setColor(this.labelColor);
            RenderUtils.drawHtml(g2, label, labelBounds, Alignment.NORTH_WEST, Direction.RIGHT, false, false);
        }

        g2.setColor(this.getBgColor());
        g2.setStroke(this.bgStroke);
        g2.drawLine(x0, y0, x1, y1);

        final var fgColor = dragging ? this.draggingColor : this.lineColor;
        g2.setColor(fgColor);
        g2.setStroke(this.stroke);
        g2.drawLine(x0, y0, x1, y1);

        g2.setStroke(origStroke);
        g2.setRenderingHints(origHints);
    }

    public Rectangle calcDefaultLabelBounds(Graphics2D g2, int x0, int y0, int x1, int y1, String label) {
        final var labelX = Math.min(x0, x1);
        final var labelY = Math.min(y0, y1) + 4;
        final var labelDim = RenderUtils.calcTextDimension(g2, getLabelFont(), label);

        return new Rectangle(labelX, labelY, labelDim.width, labelDim.height);
    }
}
