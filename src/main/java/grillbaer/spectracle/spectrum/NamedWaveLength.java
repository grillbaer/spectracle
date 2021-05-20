package grillbaer.spectracle.spectrum;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class NamedWaveLength {
    private final String name;
    private final double nanoMeters;

    public static final List<NamedWaveLength> COMMON_WAVELENGTHS =
            List.of(
                    new NamedWaveLength("FL Hg Purple", 405),
                    new NamedWaveLength("FL Hg Bright Blue", 436),
                    new NamedWaveLength("FL Hg Brightest Green", 546),
                    new NamedWaveLength("FL Eu Bright Red-Orange", 612),
                    new NamedWaveLength("Fraunhofer K Ca+", 393),
                    new NamedWaveLength("Fraunhofer H Ca+", 397),
                    new NamedWaveLength("Fraunhofer G Ca/Fe", 431),
                    new NamedWaveLength("Fraunhofer F H", 486),
                    new NamedWaveLength("Fraunhofer b2 Mg", 517),
                    new NamedWaveLength("Fraunhofer b1 Mg", 518),
                    new NamedWaveLength("Fraunhofer E Fe", 527),
                    new NamedWaveLength("Fraunhofer D2 Na", 589),
                    new NamedWaveLength("Fraunhofer D1 Na", 590),
                    new NamedWaveLength("Fraunhofer C H", 656),
                    new NamedWaveLength("Fraunhofer B O₂", 687),
                    new NamedWaveLength("Fraunhofer A O₂", 759)
            );

    public String getWaveLengthNameString(boolean withUnit) {
        final var sb = new StringBuilder();
        sb.append(WaveLengths.format(this.nanoMeters));
        if (withUnit) {
            sb.append(" nm");
        }
        if (name != null) {
            sb.append(" ").append(getName());
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return WaveLengths.format(this.nanoMeters);
    }
}
