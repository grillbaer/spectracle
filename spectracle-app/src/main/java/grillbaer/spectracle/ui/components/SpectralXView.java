package grillbaer.spectracle.ui.components;

import grillbaer.spectracle.model.Observers;
import grillbaer.spectracle.spectrum.Formatting;
import grillbaer.spectracle.spectrum.WaveLengthCalibration;
import lombok.Getter;
import lombok.NonNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.round;

/**
 * Basic view for spectral data over its X-axis with support for grid, cursors, labels etc.
 */
public abstract class SpectralXView extends JComponent {
    protected WaveLengthCalibration waveLengthCalibration;
    protected Rectangle viewArea;

    private boolean linearXAxis = true;
    protected int xAxisHeight;
    private final Map<String, Cursor> xCursorsById = new TreeMap<>();

    @Getter
    private int headRoomHeight;

    @Getter
    private Double hoverCursorWaveLength;
    @Getter
    private final Observers<Double> hoverCursorWaveLengthObservers = new Observers<>();
    private boolean showHoverCursorLabel;

    protected SpectralXView(int xAxisHeight) {
        this();
        this.xAxisHeight = xAxisHeight;
    }

    protected SpectralXView() {
        final var mouseHandler = new MouseHandler();
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
    }

    public void setHeadRoomHeight(int headRoomHeight) {
        if (this.headRoomHeight != headRoomHeight) {
            this.headRoomHeight = headRoomHeight;
            revalidate();
            repaint();
        }
    }

    public void setCalibration(WaveLengthCalibration waveLengthCalibration) {
        if (!Objects.equals(this.waveLengthCalibration, waveLengthCalibration)) {
            this.waveLengthCalibration = waveLengthCalibration;
            revalidate();
            repaint();
        }
    }

    public void setLinearXAxis(boolean linearXAxis) {
        if (this.linearXAxis != linearXAxis) {
            this.linearXAxis = linearXAxis;
            repaint();
        }
    }

    public WaveLengthCalibration getCalibration() {
        return this.waveLengthCalibration;
    }

    public void putXCursor(@NonNull Cursor cursor) {
        this.xCursorsById.put(cursor.getId(), cursor);
        repaint();
    }

    public Cursor getXCursor(String id) {
        return this.xCursorsById.get(id);
    }

    public Cursor removeXCursor(String id) {
        final var cursor = this.xCursorsById.remove(id);
        repaint();

        return cursor;
    }

    public void setHoverCursorWaveLength(Double hoverCursorWaveLength) {
        if (!Objects.equals(this.hoverCursorWaveLength, hoverCursorWaveLength)) {
            this.hoverCursorWaveLength = hoverCursorWaveLength;
            this.hoverCursorWaveLengthObservers.fire(hoverCursorWaveLength);
            repaint();
        }
    }

    public void setShowHoverCursorLabel(boolean showHoverCursorLabel) {
        if (this.showHoverCursorLabel != showHoverCursorLabel) {
            this.showHoverCursorLabel = showHoverCursorLabel;
            repaint();
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(100, 10 + this.headRoomHeight);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(10, 10 + this.headRoomHeight);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        this.viewArea = null;
    }

    protected Rectangle calcViewArea() {
        final var insets = getInsets();
        final int x = insets.left;
        final int y = insets.top + this.headRoomHeight;
        final int w = getWidth() - insets.left - insets.right;
        final int h = getHeight() - insets.top - insets.bottom - xAxisHeight - this.headRoomHeight;

        return new Rectangle(x, y, w, h);
    }

    private void updateViewArea() {
        if (this.viewArea == null) {
            this.viewArea = calcViewArea();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (this.waveLengthCalibration != null) {
            updateViewArea();
            final var g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

            drawView(g2);
        }
    }

    /**
     * Draw custom content.
     */
    protected abstract void drawView(Graphics2D g2);

    protected void clearHeadroomArea(Graphics2D g2, Color color) {
        g2.setColor(color);
        g2.fillRect(this.viewArea.x, this.viewArea.y - getHeadRoomHeight(),
                this.viewArea.width, this.viewArea.height);
    }

    protected void clearViewArea(Graphics2D g2, Color color) {
        g2.setColor(color);
        g2.fill(this.viewArea);
    }

    protected void drawXGridOverlay(Graphics2D g2) {
        g2.setColor(new Color(128, 128, 128, 40));
        drawXGrid(g2, 10.);
        g2.setColor(new Color(128, 128, 128, 50));
        drawXGrid(g2, 50.);
        g2.setColor(new Color(128, 128, 128, 90));
        drawXGrid(g2, 100.);
    }

    protected void drawXGrid(Graphics2D g2, double stepNanoMeters) {
        double nanoMeters = Math.ceil(this.waveLengthCalibration.getMinNanoMeters() / stepNanoMeters) * stepNanoMeters;
        while (nanoMeters <= this.waveLengthCalibration.getMaxNanoMeters()) {
            final var x = (int) waveLengthToX(nanoMeters);
            g2.drawLine(x, this.viewArea.y, x, this.viewArea.y + this.viewArea.height);
            nanoMeters += stepNanoMeters;
        }
    }

    protected void drawXAxis(Graphics2D g2) {
        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g2.setColor(getForeground());
        final var stepNanoMeters = 50.;
        double nanoMeters = Math.ceil(this.waveLengthCalibration.getMinNanoMeters() / stepNanoMeters) * stepNanoMeters;
        final var yBaseline = this.viewArea.y + this.viewArea.height + g2.getFontMetrics().getAscent();
        while (nanoMeters <= this.waveLengthCalibration.getMaxNanoMeters()) {
            final var x = (int) waveLengthToX(nanoMeters);
            final var label = String.format("%.0f", nanoMeters);
            final var labelWidth = g2.getFontMetrics().stringWidth(label);
            if (x - labelWidth / 2 > this.viewArea.x + 2
                    && x + labelWidth / 2 < this.viewArea.x + this.viewArea.width - 2) {
                g2.drawString(label, x - labelWidth / 2, yBaseline);
            }
            nanoMeters += stepNanoMeters;
        }
    }

    protected void drawXCursors(Graphics2D g2) {
        final var labelAvoidAreas = new ArrayList<Rectangle>();
        final var sortedCursors = this.xCursorsById.values()
                .stream()
                .sorted(Comparator.comparing(Cursor::getValue))
                .collect(Collectors.toList());
        for (Cursor c : sortedCursors) {
            drawXCursor(g2, c, labelAvoidAreas);
        }
    }

    protected void drawHoverCursor(Graphics2D g2) {
        if (this.hoverCursorWaveLength != null) {
            final var color = new Color(255, 255, 255, 150);
            final var label =
                    this.showHoverCursorLabel ? Formatting.formatWaveLength(this.hoverCursorWaveLength) : null;
            final var cursor = new Cursor("hover", this.hoverCursorWaveLength, () -> label, color, color);

            final var x = (int) round(waveLengthToX(cursor.getValue()));
            final var y0 = this.viewArea.y;
            var y1 = this.viewArea.y + this.viewArea.height + this.xAxisHeight - 2;
            Rectangle labelBounds = null;
            if (label != null) {
                labelBounds = cursor.calcDefaultLabelBounds(g2, x, y0, x, y1, label);
                labelBounds.y = y1 - labelBounds.height;
                y1 -= labelBounds.height;
            }
            cursor.draw(g2, x, y0, x, y1, labelBounds);
        }
    }

    protected void drawXCursor(Graphics2D g2, Cursor cursor, List<Rectangle> labelAvoidAreas) {
        final var x = (int) round(waveLengthToX(cursor.getValue()));
        var y0 = this.viewArea.y - getHeadRoomHeight();
        final var y1 = this.viewArea.y + this.viewArea.height + 5;
        Rectangle labelBounds = null;
        final var label = cursor.getLabelSupplier().get();
        if (label != null && labelAvoidAreas != null) {
            labelBounds = cursor.calcDefaultLabelBounds(g2, x, y0, x, y1, label);
            labelBounds.grow(1, 0);
            if (labelBounds.getMaxX() > this.viewArea.getMaxX()) {
                labelBounds.x = this.viewArea.x + this.viewArea.width - labelBounds.width;
            }
            moveDownOnCollision(labelAvoidAreas, labelBounds);
            labelAvoidAreas.add(labelBounds);
            y0 = labelBounds.y + labelBounds.height - 1;
        }
        cursor.draw(g2, x, y0, x, y1, labelBounds);
    }

    protected void moveDownOnCollision(List<Rectangle> collisionAreas, Rectangle rect) {
        // This kind of collision detection may have O(n^3) depending on the number of cursors n,
        // but since usually only one or two passes are needed, O(n^2) will be reached in practice
        // and the number of cursors is small enough.
        boolean collision = true;
        while (collision) {
            collision = false;
            for (Rectangle avoidArea : collisionAreas) {
                if (avoidArea.intersects(rect)) {
                    rect.y = avoidArea.y + avoidArea.height;
                    collision = true;
                }
            }
        }
    }

    protected void moveLeftOnCollision(List<Rectangle> collisionAreas, Rectangle rect) {
        // This kind of collision detection may have O(n^3) depending on the number of cursors n,
        // but since usually only one or two passes are needed, O(n^2) will be reached in practice
        // and the number of cursors is small enough.
        boolean collision = true;
        while (collision) {
            collision = false;
            for (Rectangle avoidArea : collisionAreas) {
                if (avoidArea.intersects(rect)) {
                    rect.x = avoidArea.x - rect.width;
                    collision = true;
                }
            }
        }
    }

    protected double waveLengthToX(double nanoMeters) {
        if (this.linearXAxis) {
            return viewArea.x + Math.abs(nanoMeters - waveLengthCalibration.getBeginNanoMeters()) / waveLengthCalibration
                    .getNanoMeterRange() * viewArea.width;
        } else {
            return viewArea.x + this.waveLengthCalibration.nanoMetersToIndex(viewArea.width, nanoMeters);
        }
    }

    protected double xToWaveLength(double x) {
        if (this.linearXAxis) {
            if (waveLengthCalibration.getBeginNanoMeters() < waveLengthCalibration.getEndNanoMeters()) {
                return waveLengthCalibration.getBeginNanoMeters() + (x - viewArea.x) / viewArea.width * waveLengthCalibration
                        .getNanoMeterRange();
            } else {
                return waveLengthCalibration.getBeginNanoMeters() - (x - viewArea.x) / viewArea.width * waveLengthCalibration
                        .getNanoMeterRange();
            }
        } else {
            return waveLengthCalibration.indexToNanoMeters(viewArea.width, x - viewArea.x);
        }
    }

    private class MouseHandler extends MouseAdapter {
        private Cursor draggingXCursor;

        @Override
        public void mousePressed(MouseEvent e) {
            if (!getBounds().contains(e.getPoint()))
                return;

            updateViewArea();
            this.draggingXCursor = findNearestDraggableXCursor(e.getX());
            if (this.draggingXCursor != null) {
                this.draggingXCursor.setDragging(true);
                clearHoverCursor();
                repaint();
            }
        }

        private Cursor findNearestDraggableXCursor(int x) {
            Cursor nearestCursor = null;
            double nearestDistance = Double.POSITIVE_INFINITY;
            for (Cursor cursor : xCursorsById.values()) {
                if (cursor.isDraggable()) {
                    final var cursorDistance = Math.abs(waveLengthToX(cursor.getValue()) - x);
                    if (cursorDistance <= nearestDistance) {
                        nearestCursor = cursor;
                        nearestDistance = cursorDistance;
                    }
                }
            }
            if (nearestDistance < 15) {
                return nearestCursor;
            } else {
                return null;
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (this.draggingXCursor != null) {
                this.draggingXCursor.setDragging(false);
                this.draggingXCursor = null;
                repaint();
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (!getBounds().contains(e.getPoint()))
                return;

            updateViewArea();
            if (this.draggingXCursor != null) {
                this.draggingXCursor.setValue(xToWaveLength(e.getX()));
                repaint();
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            setHoverCursor(e);
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            setHoverCursor(e);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            clearHoverCursor();
        }

        private void setHoverCursor(MouseEvent e) {
            updateViewArea();
            setHoverCursorWaveLength(xToWaveLength(e.getX()));
        }

        private void clearHoverCursor() {
            setHoverCursorWaveLength(null);
        }
    }
}
