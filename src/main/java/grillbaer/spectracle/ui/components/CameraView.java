package grillbaer.spectracle.ui.components;

import grillbaer.spectracle.camera.Frame;

import java.awt.*;

public class CameraView extends SpectralXView {
    private Frame frame;

    private Double sampleRowRatio;
    private int sampleRows = 3;
    private Color sampleRowColor = new Color(255, 255, 255, 128);
    private Stroke sampleRowStroke = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0f,
            new float[]{4f, 8f}, 0f);

    public CameraView() {
        super(0);
    }

    public void setFrame(Frame frame) {
        if (this.frame != frame) {
            final var oldFrame = this.frame;
            this.frame = frame;

            if (oldFrame == null || frame == null
                    || oldFrame.getWidth() != this.frame.getWidth()
                    || oldFrame.getHeight() != this.frame.getHeight()) {
                revalidate();
            }
            repaint();
        }
    }

    public void setSampleRowPosRatio(Double sampleRowRatio) {
        this.sampleRowRatio = sampleRowRatio;
        repaint();
    }

    public void setSampleRows(int sampleRowThickness) {
        this.sampleRows = sampleRowThickness;
        repaint();
    }

    @Override
    protected void drawView(Graphics2D g2) {
        if (this.frame != null) {
            final var insets = getInsets();
            final var image = this.frame.getImage();
            if (image != null) {
                final int availableWidth = getWidth() - insets.left - insets.right;
                final int availableHeight = getHeight() - insets.top - insets.bottom;
                final Dimension renderDim = Geometry.scaleToFitWidth(
                        image.getWidth(), image.getHeight(), availableWidth);
                final var imageY0 = insets.top + (availableHeight - renderDim.height) / 2;
                final var imageX0 = insets.left + (availableWidth - renderDim.width) / 2;

                g2.drawImage(image, imageX0, imageY0,
                        renderDim.width, renderDim.height, null);

                drawXGridOverlay(g2);
                Rectangle bounds = calcSampleRowBounds(imageX0, imageY0, renderDim.width, renderDim.height);
                if (bounds != null) {
                    final var origClip = g2.getClip();
                    g2.clipRect(bounds.x, bounds.y - 3, bounds.width, bounds.height + 6);
                    g2.drawImage(image, imageX0, imageY0,
                            renderDim.width, renderDim.height, null);
                    g2.setClip(origClip);
                    drawSampleRowBounds(g2, bounds);
                }
            }
        }
    }

    private Rectangle calcSampleRowBounds(int imageX0, int imageY0, int imageWidth, int imageHeight) {
        if (this.sampleRowRatio == null)
            return null;

        final int y = imageY0 + (int) (imageHeight * this.sampleRowRatio);
        final int y0 = y - this.sampleRows / 2;

        return new Rectangle(imageX0, y0, imageWidth, this.sampleRows);
    }

    private void drawSampleRowBounds(Graphics2D g2, Rectangle bounds) {
        if (bounds == null)
            return;

        final var origColor = g2.getColor();
        final var origStroke = g2.getStroke();
        g2.setColor(this.sampleRowColor);
        g2.setStroke(this.sampleRowStroke);

        final var x0 = bounds.x;
        final var x1 = bounds.x + bounds.width - 1;
        final var y0 = bounds.y - 1;
        final var y1 = bounds.y + bounds.height;
        g2.drawLine(x0, y0, x1, y0);
        g2.drawLine(x0, y1, x1, y1);

        g2.setStroke(origStroke);
        g2.setColor(origColor);
    }


    @Override
    public Dimension getPreferredSize() {
        final var insets = getInsets();
        if (this.frame != null) {
            return new Dimension(insets.left + this.frame.getWidth() + insets.right,
                    insets.top + this.frame.getHeight() + insets.bottom);
        } else {
            return new Dimension(insets.left + 640 + insets.right,
                    insets.top + 480 + insets.bottom);
        }
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(200, 80);
    }
}
