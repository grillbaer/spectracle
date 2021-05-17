package grillbaer.spectracle.ui;

import grillbaer.spectracle.Context;
import lombok.NonNull;

import javax.swing.*;
import java.awt.*;

public class MainPanel {
    private final Context context;

    private final JPanel panel;
    private final JSplitPane topBottomSplit;
    private final CameraPanel cameraPanel;
    private final SpectrumPanel spectrumPanel;

    public MainPanel(@NonNull Context context) {
        this.context = context;

        this.cameraPanel = new CameraPanel(this.context);
        this.spectrumPanel = new SpectrumPanel(this.context);

        this.topBottomSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        this.topBottomSplit.setContinuousLayout(true);
        this.topBottomSplit.setTopComponent(this.spectrumPanel.getComponent());
        this.topBottomSplit.setBottomComponent(this.cameraPanel.getComponent());
        this.topBottomSplit.setResizeWeight(0.7);

        this.panel = new JPanel(new BorderLayout());
        this.panel.add(this.topBottomSplit, BorderLayout.CENTER);
    }

    public JComponent getComponent() {
        return this.panel;
    }
}
