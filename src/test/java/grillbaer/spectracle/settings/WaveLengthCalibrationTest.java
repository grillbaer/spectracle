package grillbaer.spectracle.settings;

import grillbaer.spectracle.spectrum.WaveLengthCalibration;
import org.junit.jupiter.api.Test;

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
    void indexToNanoMetersAtCalPoints() {
        final var cal = WaveLengthCalibration.create(
                new WaveLengthCalibration.WaveLengthPoint(WaveLengthCalibration.indexToRatio(400, 100), 400.),
                new WaveLengthCalibration.WaveLengthPoint(WaveLengthCalibration.indexToRatio(400, 333), 800.));
        assertEquals(400., cal.indexToNanoMeters(400, 100));
        assertEquals(800., cal.indexToNanoMeters(400, 333));
    }

    @Test
    void indexToNanoMetersInterpolation() {
        final var cal = WaveLengthCalibration.create(
                new WaveLengthCalibration.WaveLengthPoint(WaveLengthCalibration.indexToRatio(400, 100), 400.),
                new WaveLengthCalibration.WaveLengthPoint(WaveLengthCalibration.indexToRatio(400, 299), 800.));
        assertEquals(200., cal.indexToNanoMeters(400, 0), 1.5);
        assertEquals(400., cal.indexToNanoMeters(400, 100));
        assertEquals(600., cal.indexToNanoMeters(400, 200), 1.5);
        assertEquals(800., cal.indexToNanoMeters(400, 299));
        assertEquals(1000., cal.indexToNanoMeters(400, 399), 1.5);
    }

    @Test
    void nanoMetersToIndex() {
        final var cal = WaveLengthCalibration.create(
                new WaveLengthCalibration.WaveLengthPoint(WaveLengthCalibration.indexToRatio(400, 100), 400.),
                new WaveLengthCalibration.WaveLengthPoint(WaveLengthCalibration.indexToRatio(400, 299), 800.));
        assertEquals(0, cal.nanoMetersToNextIndex(400, 200.));
        assertEquals(100, cal.nanoMetersToNextIndex(400, 400.));
        assertEquals(200, cal.nanoMetersToNextIndex(400, 600.));
        assertEquals(299, cal.nanoMetersToNextIndex(400, 800.));
        assertEquals(399, cal.nanoMetersToNextIndex(400, 1000.));
    }
}