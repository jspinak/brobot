package io.github.jspinak.brobot.action.basic.find;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true, builderClassName = "HSVBinOptionsBuilder")
public final class HSVBinOptions {
    @Builder.Default private final int hueBins = 12;
    @Builder.Default private final int saturationBins = 2;
    @Builder.Default private final int valueBins = 1;
}
