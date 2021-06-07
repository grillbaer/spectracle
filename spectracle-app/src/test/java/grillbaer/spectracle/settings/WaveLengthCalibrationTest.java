package grillbaer.spectracle.settings;

import grillbaer.spectracle.spectrum.WaveLengthCalibration;
import grillbaer.spectracle.spectrum.WaveLengthCalibration.Point;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WaveLengthCalibrationTest {

    @Test
    void indexToRatio() {
        assertEquals(0.0, WaveLengthCalibration.indexToRatio(400, 0));
        assertEquals(1.0, WaveLengthCalibration.indexToRatio(400, 399));
    }

    @Test
    void ratioToIndex() {
        assertEquals(0, WaveLengthCalibration.ratioToNextIndex(400, 0.0));
        assertEquals(100, WaveLengthCalibration.ratioToNextIndex(400, 0.25));
        assertEquals(200, WaveLengthCalibration.ratioToNextIndex(400, 0.5));
        assertEquals(299, WaveLengthCalibration.ratioToNextIndex(400, 0.75));
        assertEquals(399, WaveLengthCalibration.ratioToNextIndex(400, 1.0));
    }

    @Test
    void indexToNanoMetersAtCalPoints2() {
        final var cal = WaveLengthCalibration.create(List.of(
                new Point(WaveLengthCalibration.indexToRatio(400, 100), 400.),
                new Point(WaveLengthCalibration.indexToRatio(400, 333), 800.)));
        assertEquals(400., cal.indexToNanoMeters(400, 100));
        assertEquals(800., cal.indexToNanoMeters(400, 333));
    }

    @Test
    void indexToNanoMetersInterpolation2() {
        final var cal = WaveLengthCalibration.create(List.of(
                new Point(WaveLengthCalibration.indexToRatio(400, 100), 400.),
                new Point(WaveLengthCalibration.indexToRatio(400, 299), 800.)));
        assertEquals(200., cal.indexToNanoMeters(400, 0), 1.5);
        assertEquals(400., cal.indexToNanoMeters(400, 100));
        assertEquals(600., cal.indexToNanoMeters(400, 200), 1.5);
        assertEquals(800., cal.indexToNanoMeters(400, 299));
        assertEquals(1000., cal.indexToNanoMeters(400, 399), 1.5);
    }

    @Test
    void nanoMetersToIndex2() {
        final var cal = WaveLengthCalibration.create(List.of(
                new Point(WaveLengthCalibration.indexToRatio(400, 100), 400.),
                new Point(WaveLengthCalibration.indexToRatio(400, 299), 800.)));
        assertEquals(0, cal.nanoMetersToNextIndex(400, 200.));
        assertEquals(100, cal.nanoMetersToNextIndex(400, 400.));
        assertEquals(200, cal.nanoMetersToNextIndex(400, 600.));
        assertEquals(299, cal.nanoMetersToNextIndex(400, 800.));
        assertEquals(399, cal.nanoMetersToNextIndex(400, 1000.));
    }

    @Test
    void ratioToNanoMeters3() {
        final var cal = WaveLengthCalibration.create(List.of(
                new Point(0.2, 100. + 400.),
                new Point(0.4, 100. + 800.),
                new Point(0.7, 100. + 1000.)));
        assertEquals(100. - 400., cal.ratioToNanoMeters(-0.2), 0.01);
        assertEquals(100., cal.ratioToNanoMeters(0.0), 0.01);
        assertEquals(100. + 200., cal.ratioToNanoMeters(0.1), 0.01);
        assertEquals(100. + 400., cal.ratioToNanoMeters(0.2), 0.01);
        assertEquals(100. + 600., cal.ratioToNanoMeters(0.3), 0.01);
        assertEquals(100. + 700., cal.ratioToNanoMeters(0.35), 0.01);
        assertEquals(100. + 800., cal.ratioToNanoMeters(0.4), 0.01);
        assertEquals(100. + 1000., cal.ratioToNanoMeters(0.7), 0.01);
        assertEquals(100. + 1066.667, cal.ratioToNanoMeters(0.8), 0.01);
        assertEquals(100. + 1200., cal.ratioToNanoMeters(1.0), 0.01);
        assertEquals(100. + 1400., cal.ratioToNanoMeters(1.3), 0.01);
    }

    @Test
    void nanoMetersToRatio() {
        final var cal = WaveLengthCalibration.create(List.of(
                new Point(0.2, 100. + 400.),
                new Point(0.4, 100. + 800.),
                new Point(0.7, 100. + 1000.)));
        assertEquals(-0.2, cal.nanoMetersToRatio(100. - 400.), 1e-5);
        assertEquals(0.0, cal.nanoMetersToRatio(100.), 1e-5);
        assertEquals(0.1, cal.nanoMetersToRatio(100. + 200.), 1e-5);
        assertEquals(0.2, cal.nanoMetersToRatio(100. + 400.), 1e-5);
        assertEquals(0.3, cal.nanoMetersToRatio(100. + 600.), 1e-5);
        assertEquals(0.35, cal.nanoMetersToRatio(100. + 700.), 1e-5);
        assertEquals(0.4, cal.nanoMetersToRatio(100. + 800.), 1e-5);
        assertEquals(0.7, cal.nanoMetersToRatio(100. + 1000.), 1e-5);
        assertEquals(0.8, cal.nanoMetersToRatio(100. + 1066.667), 1e-5);
        assertEquals(1.0, cal.nanoMetersToRatio(100. + 1200.), 1e-5);
        assertEquals(1.3, cal.nanoMetersToRatio(100. + 1400.), 1e-5);
    }
}