package io.github.jspinak.brobot.action.basic.find;

import lombok.Getter;

@Getter
public final class HSVBinOptions {
    private final int hueBins;
    private final int saturationBins;
    private final int valueBins;

    private HSVBinOptions(Builder builder) {
        this.hueBins = builder.hueBins;
        this.saturationBins = builder.saturationBins;
        this.valueBins = builder.valueBins;
    }

    public static class Builder {
        private int hueBins = 12;
        private int saturationBins = 2;
        private int valueBins = 1;

        /**
         * Creates a new Builder instance pre-populated with values from an existing
         * HSVBinOptions object.
         *
         * @param original The HSVBinOptions instance to copy.
         */
        public Builder(HSVBinOptions original) {
            if (original != null) {
                this.hueBins = original.hueBins;
                this.saturationBins = original.saturationBins;
                this.valueBins = original.valueBins;
            }
        }

        public Builder() { }

        public Builder setHueBins(int bins) { this.hueBins = bins; return this; }
        public Builder setSaturationBins(int bins) { this.saturationBins = bins; return this; }
        public Builder setValueBins(int bins) { this.valueBins = bins; return this; }

        public HSVBinOptions build() {
            return new HSVBinOptions(this);
        }
    }
}
