package grillbaer.spectracle.spectrum;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.math3.distribution.NormalDistribution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Comparator.comparing;

/**
 * Calculations on spectra and sample line vectors.
 */
public final class Calculations {

    private Calculations() {
        // no instances
    }

    /**
     * Calibrate a spectrum by applying the wavelength dependent factors from a sensitivity calibration profile.
     */
    public static Spectrum applySensitivityCalibration(@NonNull Spectrum spectrum, @NonNull Spectrum sensitivityCalibration) {
        var newValues = spectrum.getSampleLine().getCopyOfValues();
        for (int i = 0; i < newValues.length; i++) {
            final var nanoMeters = spectrum.getNanoMetersAtIndex(i);
            final var calibrationFactor = sensitivityCalibration.getValueAtNanoMeters(nanoMeters);
            newValues[i] *= calibrationFactor;
        }

        return Spectrum.create(new SampleLine(newValues, spectrum.getSampleLine()
                .getOverExposed()), spectrum.getCalibration());
    }

    /**
     * Calculate a spectrum of sensitivity correction factors by comparing a measured spectrum obtained from the camera
     * with a reference light source's idealized spectrum.
     */
    public static Spectrum calcSensitivityCalibration(@NonNull Spectrum cameraSpectrum, @NonNull Spectrum referenceLightSpectrum,
                                                      double calBeginNanos, double calEndNanos) {

        final var length = cameraSpectrum.getLength();
        final var calBeginIndex = cameraSpectrum.getCalibration().nanoMetersToNextIndex(length, calBeginNanos);
        final var calEndIndex = cameraSpectrum.getCalibration().nanoMetersToNextIndex(length, calEndNanos);

        final var smoothedCameraSpectrum = Calculations.gaussianSmooth(cameraSpectrum.getSampleLine(), 3.0);

        final var correctionFactors = new double[length];
        double maxCorrectionFactorInCalRange = 0.;
        for (int i = 0; i < length; i++) {
            final var nanoMeters = cameraSpectrum.getNanoMetersAtIndex(i);
            final var cameraValue = smoothedCameraSpectrum.getValue(i);
            final var targetValue = referenceLightSpectrum.getValueAtNanoMeters(nanoMeters);
            correctionFactors[i] = Math.max(0.01, targetValue / cameraValue);
            if (calBeginIndex <= i && i <= calEndIndex && maxCorrectionFactorInCalRange < correctionFactors[i]) {
                maxCorrectionFactorInCalRange = correctionFactors[i];
            }
        }

        for (int i = calBeginIndex; i <= calEndIndex; i++) {
            correctionFactors[i] = Math.min(1., correctionFactors[i] / maxCorrectionFactorInCalRange);
        }
        for (int i = 0; i < calBeginIndex; i++) {
            correctionFactors[i] = correctionFactors[calBeginIndex];
        }
        for (int i = calEndIndex + 1; i < length; i++) {
            correctionFactors[i] = correctionFactors[calEndIndex];
        }

        return Spectrum.create(SampleLine.create(correctionFactors), cameraSpectrum.getCalibration());
    }

    /**
     * Normalize the values of the sample line so that the maximum value will be 1.0.
     * If the whole sample contains only values <= 0.0, it will not be changed.
     */
    public static SampleLine normalize(@NonNull SampleLine input) {
        final var maxValue = input.getMaxValue();
        if (maxValue <= 0.0)
            return input;

        final var newValues = input.getCopyOfValues();
        for (int i = 0; i < newValues.length; i++) {
            newValues[i] /= maxValue;
        }

        return new SampleLine(newValues, input.getOverExposed());
    }


    /**
     * Calculate a time averaged sample line vector with exponential annealing.
     *
     * @param newSample           new sample line to take into calculation
     * @param lastAveragedSample  previous averaged sample line
     * @param timeAveragingFactor Ratio between 0.0 and 1.0 that determines the share to use from the previous sample line. 0.0 does no time averaging at all and uses the new sample only. 1.0 only takes the previos sample and does not use the new sample at all.
     * @param peakHold            hold peaks, i.e. immediately take new peaks, decay with time averaging if new values are below
     */
    public static SampleLine timeAverage(@NonNull SampleLine newSample, SampleLine lastAveragedSample,
                                         double timeAveragingFactor, boolean peakHold) {
        if (lastAveragedSample == null
                || lastAveragedSample.getLength() != newSample.getLength()) {
            return newSample;
        }

        final var newAveraged = lastAveragedSample.getCopyOfValues();
        for (int i = 0; i < newAveraged.length; i++) {
            final var oldValue = newAveraged[i];
            final var newValue = newSample.getValue(i);
            if (peakHold && newValue > oldValue) {
                newAveraged[i] = newValue;
            } else {
                newAveraged[i] = oldValue * timeAveragingFactor + newValue * (1. - timeAveragingFactor);
            }
        }

        return new SampleLine(newAveraged, newSample.getOverExposed());
    }


    /**
     * Calculate a sample line vector with gaussian smoothing.
     */
    public static SampleLine gaussianSmooth(@NonNull SampleLine input, double sigmaInIndexSteps) {
        if (sigmaInIndexSteps <= 0)
            return input;

        final double[] smoothArray = gaussianHalfArray(sigmaInIndexSteps, (int) sigmaInIndexSteps * 3 + 2);
        final var sum = smoothArray[0] + 2. * Arrays.stream(smoothArray).skip(1).sum();

        final var result = new double[input.getLength()];
        for (int i = 0; i < input.getLength(); i++) {
            result[i] = smoothArray[0] * input.getValue(i);
            for (int j = 1; j < smoothArray.length; j++) {
                result[i] += smoothArray[j] * (input.getValue(max(0, i - j)) + input.getValue(min(input.getLength() - 1, i + j)));
            }
            result[i] /= sum;
        }

        return new SampleLine(result, input.getOverExposed());
    }

    /**
     * Calculate a sample line vector with gaussian smoothing over the neighbour values excluding the center value itself.
     */
    public static SampleLine gaussianNeighbourhood(@NonNull SampleLine input, double sigmaInIndexSteps) {
        if (sigmaInIndexSteps <= 0)
            return input;

        final double[] smoothArray = gaussianHalfArray(sigmaInIndexSteps, (int) sigmaInIndexSteps * 3 + 2);
        final var sum = 2. * Arrays.stream(smoothArray).skip(1).sum();

        final var result = new double[input.getLength()];
        for (int i = 0; i < input.getLength(); i++) {
            for (int j = 1; j < smoothArray.length; j++) {
                result[i] += smoothArray[j] * (input.getValue(max(0, i - j)) + input.getValue(min(input.getLength() - 1, i + j)));
            }
            result[i] /= sum;
        }

        return new SampleLine(result, input.getOverExposed());
    }

    private static double[] gaussianHalfArray(double sigmaInIndexSteps, int length) {
        final var distrib = new NormalDistribution(0., sigmaInIndexSteps);
        final var smoothArray = new double[length];
        for (int i = 0; i < smoothArray.length; i++) {
            smoothArray[i] = distrib.density(i);
        }

        return smoothArray;
    }

    /**
     * Find local minimums and maximums in a sample line.
     * The extrema are returned in descending level order.
     */
    public static Extrema findLocalExtrema(@NonNull SampleLine input, double noiseSigmaIndexSteps, double baseSigmaIndexSteps, int maxMinima, int maxMaxima) {
        final var denoised = gaussianSmooth(input, noiseSigmaIndexSteps);
        final var baseLevelNarrow = gaussianNeighbourhood(input, baseSigmaIndexSteps);
        final var baseLevelWide = gaussianNeighbourhood(input, baseSigmaIndexSteps * 5.);
        final var levelFactor = 100.;
        final var levelLimit = 0.3;

        final var minima = new ArrayList<Extremum>();
        final var maxima = new ArrayList<Extremum>();

        for (int i = 1; i < denoised.getLength() - 1; i++) {
            if (denoised.getValue(i - 1) < denoised.getValue(i) && denoised.getValue(i) > denoised.getValue(i + 1)) {
                final var level = levelFactor *
                        max(denoised.getValue(i) - baseLevelNarrow.getValue(i), denoised.getValue(i) - baseLevelWide.getValue(i));
                if (level >= levelLimit) {
                    maxima.add(new Extremum(i, level));
                }
            }
            if (denoised.getValue(i - 1) > denoised.getValue(i) && denoised.getValue(i) < denoised.getValue(i + 1)) {
                final var level = levelFactor *
                        min(denoised.getValue(i) - baseLevelNarrow.getValue(i), denoised.getValue(i) - baseLevelWide.getValue(i));
                if (level <= -levelLimit) {
                    minima.add(new Extremum(i, level));
                }
            }
        }

        return new Extrema(
                minima.stream().sorted(comparing(Extremum::getLevel)).limit(maxMinima).toList(),
                maxima.stream().sorted(comparing(Extremum::getLevel).reversed()).limit(maxMaxima).toList());
    }

    @AllArgsConstructor
    @Getter
    public static class Extrema {
        private final List<Extremum> minima;
        private final List<Extremum> maxima;
    }

    @AllArgsConstructor
    @Getter
    public static class Extremum {
        private final int index;
        private final double level;

        public boolean isMinimum() {
            return level < 0.;
        }

        public boolean isMaximum() {
            return level > 0.;
        }
    }
}
