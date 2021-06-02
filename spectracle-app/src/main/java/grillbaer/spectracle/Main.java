package grillbaer.spectracle;

import com.formdev.flatlaf.FlatDarkLaf;
import grillbaer.spectracle.camera.Camera;
import grillbaer.spectracle.ui.MainPanel;
import nu.pattern.OpenCV;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;

/**
 * Application entry point.
 */
public class Main {
    public static void main(String[] args) throws InterruptedException, InvocationTargetException {
        OpenCV.loadLocally();

        final var context = new Context();
        if (context.getModel().getCameraId() == null) {
            context.getModel().setCamera(new Camera(0));
        }
        context.getModel().grabSingleFrame();
        context.getModel().setCameraPaused(false);

        SwingUtilities.invokeAndWait(() -> {
            FlatDarkLaf.install();
            final var mainPanel = new MainPanel(context);
            final var frame = new JFrame("Spectracle");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.getContentPane().add(mainPanel.getComponent(), BorderLayout.CENTER);
            setDefaultWindowBounds(frame);
            frame.setVisible(true);
        });
    }

    private static void setDefaultWindowBounds(JFrame frame) {
        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        size.width *= 0.8;
        size.height *= 0.8;
        if ((double) size.width / size.height > 2) {
            // limit ratio on extremely broad screens
            size.width = size.height * 2;
        }
        frame.setSize(size.width, size.height);
        frame.setLocationRelativeTo(null);
    }
}
