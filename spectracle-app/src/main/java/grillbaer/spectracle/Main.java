package grillbaer.spectracle;

import com.formdev.flatlaf.FlatDarkLaf;
import grillbaer.spectracle.camera.Camera;
import grillbaer.spectracle.ui.MainPanel;
import nu.pattern.OpenCV;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Application entry point.
 */
public class Main {
    private final static Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws InterruptedException, InvocationTargetException {
        LOG.info("Spectracle starting up ...");
        LOG.info("OS Name: {}", System.getProperty("os.name"));
        LOG.info("OS Arch: {}", System.getProperty("os.arch"));
        LOG.info("OS Version: {}", System.getProperty("os.version"));
        LOG.info("Java Version: {}", System.getProperty("java.version"));

        Thread.setDefaultUncaughtExceptionHandler((t, e) -> LOG.error("Uncaught exception from thread {}", t.getName(), e));

        LOG.info("Initializing OpenCV ...");
        OpenCV.loadLocally();

        final var context = new Context();
        if (context.getModel().getCameraId() == null) {
            context.getModel().setCamera(new Camera(0));
        }
        context.getModel().grabSingleFrame();
        context.getModel().setCameraPaused(false);

        SwingUtilities.invokeAndWait(() -> {
            LOG.info("UI starting up ...");
            FlatDarkLaf.install();
            final var mainPanel = new MainPanel(context);
            final var frame = new JFrame("Spectracle");
            frame.setIconImages(getWindowIcons());
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.getContentPane().add(mainPanel.getComponent(), BorderLayout.CENTER);
            setDefaultWindowBounds(frame);
            LOG.info("Setting main window visible");
            frame.setVisible(true);
        });
    }

    private static List<? extends Image> getWindowIcons() {
        return Stream.of(16, 24, 32, 48, 64, 128, 256)
                .map(size -> {
                    try {
                        return ImageIO.read(
                                Main.class.getClassLoader().getResource("icons/icon-" + size + ".png"));
                    } catch (IOException e) {
                        throw new IllegalStateException("missing window icon", e);
                    }
                }).collect(Collectors.toList());
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
