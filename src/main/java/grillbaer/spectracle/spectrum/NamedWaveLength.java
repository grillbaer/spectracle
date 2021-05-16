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
                    new NamedWaveLength("FL Eu Bright Red-Orange", 612)
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
