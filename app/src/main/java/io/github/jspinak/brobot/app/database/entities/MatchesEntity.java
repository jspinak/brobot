package io.github.jspinak.brobot.app.database.entities;

import io.github.jspinak.brobot.app.database.embeddable.RegionEmbeddable;
import io.github.jspinak.brobot.actions.actionExecution.actionLifecycle.ActionLifecycle;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.SceneAnalysisCollection;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.primitives.text.Text;
import jakarta.persistence.*;
import lombok.Data;
import org.bytedeco.opencv.opencv_core.Mat;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
public class MatchesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String actionDescription = "";
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "matches_matchList",
            joinColumns = @JoinColumn(name = "matches_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "match_id", referencedColumnName = "id"))
    private List<MatchEntity> matchList = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "matches_initialMatchList",
            joinColumns = @JoinColumn(name = "matches_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "match_id", referencedColumnName = "id"))
    private List<MatchEntity> initialMatchList = new ArrayList<>();
    @OneToOne(cascade = CascadeType.ALL)
    private ActionOptionsEntity actionOptions;
    @ElementCollection
    @CollectionTable(name = "activeStates", joinColumns = @JoinColumn(name = "matches_id"))
    private Set<String> activeStates = new HashSet<>();
    @Transient
    private Text text = new Text();
    private String selectedText = "";
    @Transient
    private Duration duration = Duration.ZERO;
    private LocalDateTime startTime = LocalDateTime.now();
    private LocalDateTime endTime;
    private boolean success = false;
    @ElementCollection
    @CollectionTable(name = "definedRegions", joinColumns = @JoinColumn(name = "matches_id"))
    private List<RegionEmbeddable> definedRegions = new ArrayList<>();
    private int maxMatches = -1; // not used when <= 0
    @Transient
    private SceneAnalysisCollection sceneAnalysisCollection = new SceneAnalysisCollection();
    @Transient
    private Mat mask;
    private String outputText = "";
    @Transient
    private ActionLifecycle actionLifecycle;
}
