package grillbaer.spectracle.spectrum;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.function.Function;

/**
 * Wavelength plus an optional readable name optionally in a group of named wavelengths.
 */
@AllArgsConstructor
@Getter
public class NamedWaveLength {
    private final NamedWaveLengthGroup group;
    private final String name;
    private final double nanoMeters;

    public NamedWaveLength(String name, double nanoMeters) {
        this.group = null;
        this.name = name;
        this.nanoMeters = nanoMeters;
    }

    public NamedWaveLength withGroup(NamedWaveLengthGroup group) {
        return new NamedWaveLength(group, this.name, this.nanoMeters);
    }

    public String format(@NonNull Component... components) {
        final var sb = new StringBuilder();
        for (Component component : components) {
            final var elementString = component.format(this);
            if (elementString != null) {
                if (sb.length() > 0) {
                    sb.append(' ');
                }
                sb.append(elementString);
            }
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return Formatting.formatWaveLength(this.nanoMeters);
    }

    @AllArgsConstructor
    public enum Component {
        WAVELENGTH(nwl -> Formatting.formatWaveLength(nwl.getNanoMeters())),
        UNIT(nwl -> "nm"),
        GROUP(nwl -> nwl.getGroup() != null ? nwl.getGroup().getGroupName() : null),
        NAME(NamedWaveLength::getName);

        private @NonNull Function<NamedWaveLength, String> elementToStringFunc;

        public String format(@NonNull NamedWaveLength namedWaveLength) {
            return this.elementToStringFunc.apply(namedWaveLength);
        }
    }
}
