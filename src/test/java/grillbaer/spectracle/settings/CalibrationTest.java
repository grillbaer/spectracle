package grillbaer.spectracle.settings;

import grillbaer.spectracle.spectrum.Calibration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalibrationTest {

    @Test
    void indexToRatio() {
        assertEquals(0.0, Calibration.indexToRatio(400, 0));
        assertEquals(1.0, Calibration.indexToRatio(400, 399));
    }

    @Test
    void ratioToIndex() {
        assertEquals(0, Calibration.ratioToIndex(400, 0.0));
        assertEquals(100, Calibration.ratioToIndex(400, 0.25));
        assertEquals(200, Calibration.ratioToIndex(400, 0.5));
        assertEquals(299, Calibration.ratioToIndex(400, 0.75));
        assertEquals(399, Calibration.ratioToIndex(400, 1.0));
    }

    @Test
    void indexToNanoMetersAtCalPoints() {
        final var cal = Calibration.createDefault();
        assertEquals(cal.getWaveLengthCal0().getNanoMeters(),
                cal.indexToNanoMeters(400, 0));
        assertEquals(cal.getWaveLengthCal1().getNanoMeters(),
                cal.indexToNanoMeters(400, 399));
    }

    @Test
    void indexToNanoMetersInterpolation() {
        final var cal = Calibration.create(
                new Calibration.Point(Calibration.indexToRatio(400, 100), 400.),
                new Calibration.Point(Calibration.indexToRatio(400, 299), 800.));
        assertEquals(200., cal.indexToNanoMeters(400, 0), 1.5);
        assertEquals(400., cal.indexToNanoMeters(400, 100));
        assertEquals(600., cal.indexToNanoMeters(400, 200), 1.5);
        assertEquals(800., cal.indexToNanoMeters(400, 299));
        assertEquals(1000., cal.indexToNanoMeters(400, 399), 1.5);
    }

    @Test
    void nanoMetersToIndex() {
        final var cal = Calibration.create(
                new Calibration.Point(Calibration.indexToRatio(400, 100), 400.),
                new Calibration.Point(Calibration.indexToRatio(400, 299), 800.));
        assertEquals(0, cal.nanoMetersToIndex(400, 200.));
        assertEquals(100, cal.nanoMetersToIndex(400, 400.));
        assertEquals(200, cal.nanoMetersToIndex(400, 600.));
        assertEquals(299, cal.nanoMetersToIndex(400, 800.));
        assertEquals(399, cal.nanoMetersToIndex(400, 1000.));
    }
}