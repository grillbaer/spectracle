package grillbaer.spectracle.ui.components;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import javax.swing.*;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;
import java.awt.*;

import static java.lang.Math.*;

/**
 * Helpers for rendering graphics.
 */
public final class RenderUtils {
    @AllArgsConstructor
    @Getter
    public enum Alignment {
        WEST(true, false, false, false),
        EAST(false, true, false, false),
        NORTH(false, false, true, false),
        SOUTH(false, false, false, true),
        NORTH_WEST(true, false, true, false),
        NORTH_EAST(false, true, true, false),
        SOUTH_WEST(true, false, false, true),
        SOUTH_EAST(false, true, false, true),
        CENTER(false, false, false, false);

        private final boolean west;
        private final boolean east;
        private final boolean north;
        private final boolean south;
    }

    @AllArgsConstructor
    @Getter
    public enum Direction {
        RIGHT(0., false),
        DOWN(0.5 * PI, true),
        LEFT(PI, false),
        UP(1.5 * PI, true);

        private final double clockwiseRadAngle;
        private final boolean vertical;

        public boolean isHorizontal() {
            return !isVertical();
        }
    }

    public enum Effect {
        PLAIN, SURROUNDED, SHADOW
    }

    private RenderUtils() {
        // no instances
    }

    /**
     * Draw a text into a bounding box in the desired direction and align it as desired.
     * Does not support HTML, but has come effects.
     */
    public static void drawText(@NonNull Graphics2D g2, String text, @NonNull Rectangle box,
                                @NonNull Alignment alignment, @NonNull Direction direction,
                                boolean clipToBox, boolean skipIfOutsideBox, @NonNull Effect effect) {
        if (text == null)
            return;

        // bounding box around text
        final var fm = g2.getFontMetrics();
        final var textWidth = fm.stringWidth(text);
        final var textHeight = fm.getAscent() + fm.getDescent();
        final var width = direction.isHorizontal() ? textWidth : textHeight;
        final var height = direction.isHorizontal() ? textHeight : textWidth;

        if (skipIfOutsideBox && (width > box.width || height > box.height))
            return;

        // calc text reference point
        int right = 0;
        int up = 0;
        switch (direction) {
            case UP:
                right = width;
                break;
            case LEFT:
                right = width;
                up = height;
                break;
            case DOWN:
                up = height;
                break;
            default:
                break;
        }

        // align text box as if reference point was bottom left
        var x = box.x + (int) ((box.width - width) / 2.0 + 0.5);
        var y = box.y + (int) ((box.height - height) / 2.0 + 0.5) + height;
        if (alignment.isWest())
            x = box.x;
        if (alignment.isEast())
            x = box.x + box.width - width;
        if (alignment.isNorth())
            y = box.y + height;
        if (alignment.isSouth())
            y = box.y + box.height;

        x += right;
        y -= up;

        final var originalClip = g2.getClipBounds();
        if (clipToBox) {
            g2.clipRect(box.x, box.y, box.width, box.height);
        }

        final var clockwiseRadAngle = direction.getClockwiseRadAngle();
        if (effect == Effect.PLAIN) {
            drawAngleString(g2, text, x, y, clockwiseRadAngle);
        } else if (effect == Effect.SURROUNDED) {
            final var oldPaint = g2.getPaint();
            g2.setPaint(g2.getBackground());
            for (int dx = -1; dx <= 1; dx += 1) {
                for (int dy = -1; dy <= 1; dy += 1) {
                    drawAngleString(g2, text, x + dx, y + dy, clockwiseRadAngle);
                }
            }
            g2.setPaint(oldPaint);
            drawAngleString(g2, text, x, y, clockwiseRadAngle);
        } else if (effect == Effect.SHADOW) {
            final var oldPaint = g2.getPaint();
            g2.setPaint(g2.getBackground());
            drawAngleString(g2, text, x + 1, y + 1, clockwiseRadAngle);
            g2.setPaint(oldPaint);
            drawAngleString(g2, text, x, y, clockwiseRadAngle);
        }

        g2.setClip(originalClip);
    }


    public static Dimension calcTextDimension(@NonNull Graphics2D g2, @NonNull Font font, String text) {
        if (BasicHTML.isHTMLString(text)) {
            final JPanel mockPanel = new JPanel();
            mockPanel.setFont(font);
            mockPanel.setForeground(g2.getColor());
            mockPanel.setBackground(g2.getBackground());

            final View view = BasicHTML.createHTMLView(mockPanel, text);

            final int width = (int) Math.ceil(view.getPreferredSpan(View.X_AXIS));
            final int height = (int) Math.ceil(view.getPreferredSpan(View.Y_AXIS));

            return new Dimension(width, height);

        } else {
            final var fm = g2.getFontMetrics(font);
            return new Dimension(text != null ? fm.stringWidth(text) : 0, fm.getAscent() +
                    fm.getDescent());
        }
    }


    /**
     * Render a string into a rectangle, render it as HTML if it starts with "&lt;html>", as plain text otherwise.
     */
    public static void drawHtml(@NonNull Graphics2D g2, String text, @NonNull Rectangle box,
                                @NonNull Alignment alignment, @NonNull Direction direction,
                                boolean clipToBox, boolean skipIfOutsideBox) {
        if (BasicHTML.isHTMLString(text)) {

            g2 = (Graphics2D) g2.create();

            final JPanel mockParent = new JPanel();
            mockParent.setFont(g2.getFont());
            mockParent.setForeground(g2.getColor());
            mockParent.setBackground(g2.getBackground());

            final View view = BasicHTML.createHTMLView(mockParent, text);

            // bounding box size
            final var textWidth = (int) Math.ceil(view.getPreferredSpan(View.X_AXIS));
            final var textHeight = (int) Math.ceil(view.getPreferredSpan(View.Y_AXIS));
            final var rightRect = new Rectangle(0, 0, textWidth, textHeight);
            final var width = direction.isHorizontal() ? textWidth : textHeight;
            final var height = direction.isHorizontal() ? textHeight : textWidth;

            if (skipIfOutsideBox && (width > box.width || height > box.height))
                return;

            // calc text reference point
            int right = 0;
            int up = 0;
            switch (direction) {
                case UP:
                    up = height;
                    break;
                case LEFT:
                    right = width;
                    up = height;
                    break;
                case DOWN:
                    right = width;
                    break;
                default:
                    break;
            }

            // align text box as if reference point was bottom left
            int x = box.x + (int) ((box.width - width) / 2.0 + 0.5);
            int y = box.y + (int) ((box.height - height) / 2.0 + 0.5);
            if (alignment.isWest())
                x = box.x;
            if (alignment.isEast())
                x = box.x + box.width - width;
            if (alignment.isNorth())
                y = box.y;
            if (alignment.isSouth())
                y = box.y + box.height - height;

            x += right;
            y += up;

            final var originalClip = g2.getClipBounds();
            if (clipToBox) {
                g2.clipRect(box.x, box.y, box.width, box.height);
            }

            final var origTransform = g2.getTransform();
            g2.translate(x, (double) y);
            g2.rotate(direction.getClockwiseRadAngle());
            view.paint(g2, rightRect);
            g2.setTransform(origTransform);

            g2.setClip(originalClip);

        } else if (text != null) {
            drawText(g2, text, box, alignment, direction, clipToBox, skipIfOutsideBox, Effect.PLAIN);
        }
    }

    /**
     * Render string in an angle, 0.0 is normal text direction to the right.
     */
    public static void drawAngleString(Graphics2D g2, String s, int x, int y, double clockwiseRadAngle) {
        final var fm = g2.getFontMetrics();
        final var origTransform = g2.getTransform();
        g2.translate((double) x, (double) y);
        g2.rotate(clockwiseRadAngle);
        g2.drawString(s, 0, -fm.getDescent() - 1);
        g2.setTransform(origTransform);
    }

    /**
     * Create brighter or darker tone of a color.
     *
     * @param factor less than 1 darker, greater than 1 brighter
     */
    public static Color brightenColor(Color col, double factor) {
        int red = max(0, min(255, (int) (col.getRed() * factor + 0.5)));
        int green = max(0, min(255, (int) (col.getGreen() * factor + 0.5)));
        int blue = max(0, min(255, (int) (col.getBlue() * factor + 0.5)));

        return new Color(red, green, blue, col.getAlpha());
    }

    /**
     * Create a white-blended tone of a color.
     *
     * @param factor 1 will yield white, 0 will keep the color as is.
     */
    public static Color whitenColor(Color col, double factor) {
        int red = max(0, min(255, (int) (col.getRed() + (255 - col.getRed()) * factor + 0.5)));
        int green = max(0, min(255, (int) (col.getGreen() + (255 - col.getGreen()) * factor + 0.5)));
        int blue = max(0, min(255, (int) (col.getBlue() + (255 - col.getBlue()) * factor + 0.5)));

        return new Color(red, green, blue, col.getAlpha());
    }
}
