package io.github.jspinak.brobot.actions.methods.basicactions.find.histogram;

import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
import io.github.jspinak.brobot.reports.Report;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
public class HistogramMatches {

    private StateImageObject stateImageObject;
    private LinkedHashMap<Region, Double> regionsScores = new LinkedHashMap<>();

    public HistogramMatches() {}

    public HistogramMatches(StateImageObject stateImageObject, LinkedHashMap<Region, Double> regionsScores) {
        this.stateImageObject = stateImageObject;
        this.regionsScores = regionsScores;
    }

    public Map<Region, Double> getResults(double minCorrelation) {
          return regionsScores.entrySet().stream()
                  .filter(entry -> entry.getValue() >= minCorrelation)
                  .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<Region, Double> getResults(int maxResults) {
        Map<Region, Double> topRegions = new HashMap<>();
        regionsScores.forEach((k, v) -> {if (topRegions.size() < maxResults) topRegions.put(k, v);});
        return topRegions;
    }

    public void addRegion(Region region, Double correlation) {
        regionsScores.put(region, correlation);
    }

    public void addRegions(LinkedHashMap<Region, Double> toAdd) {
        regionsScores.putAll(toAdd);
    }

    public void sortHighToLow() {
        regionsScores = regionsScores.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));
    }

    public double getNthCorrelation(int n) {
        sortHighToLow();
        Double[] correlations = regionsScores.values().toArray(new Double[0]);
        if (correlations.length < n) return 0;
        return correlations[n];
    }

    public void printFirst(int maxToPrint) {
        Report.print("x.y_corr: ");
        int i=0;
        for (Map.Entry<Region, Double> regCorr : regionsScores.entrySet()) {
            Report.format("%d.%d:%,.2f ", regCorr.getKey().x, regCorr.getKey().y, regCorr.getValue());
            i++;
            if (i >= maxToPrint) break;
        }
    }
}
