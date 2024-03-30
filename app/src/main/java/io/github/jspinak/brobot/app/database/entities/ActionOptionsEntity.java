package io.github.jspinak.brobot.app.database.entities;

import io.github.jspinak.brobot.app.database.embeddable.SearchRegionsEmbeddable;
import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.mouse.ClickType;
import jakarta.persistence.*;
import lombok.Data;
import org.sikuli.basics.Settings;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class ActionOptionsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private ActionOptions.Action action = ActionOptions.Action.FIND;
    private ActionOptions.ClickUntil clickUntil = ActionOptions.ClickUntil.OBJECTS_APPEAR;
    private ActionOptions.Find find = ActionOptions.Find.FIRST;
    @ElementCollection
    @CollectionTable(name = "findActions", joinColumns = @JoinColumn(name = "actionOptions_id"))
    private List<ActionOptions.Find> findActions = new ArrayList<>();
    private boolean keepLargerMatches = false;
    private ActionOptions.DoOnEach doOnEach = ActionOptions.DoOnEach.FIRST;
    private boolean captureImage = true;
    private boolean useDefinedRegion = false;
    private double similarity = Settings.MinSimilarity;
    private double pauseBeforeMouseDown = Settings.DelayBeforeMouseDown;
    private double pauseAfterMouseDown = BrobotSettings.delayAfterMouseDown;
    private float moveMouseDelay = Settings.MoveMouseDelay;
    private double pauseBeforeMouseUp = Settings.DelayBeforeDrop;
    private double pauseAfterMouseUp = 0;
    private ClickType.Type clickType = ClickType.Type.LEFT;
    private boolean moveMouseAfterClick = false;
    @OneToOne(cascade = CascadeType.ALL)
    private LocationEntity locationAfterAction = new LocationEntity();
    @OneToOne(cascade = CascadeType.ALL)
    private LocationEntity offsetLocationBy = new LocationEntity();
    @Embedded
    private SearchRegionsEmbeddable searchRegions = new SearchRegionsEmbeddable();
    private double pauseBeforeBegin = 0;
    private double pauseAfterEnd = 0;
    private double pauseBetweenIndividualActions = 0;
    private int timesToRepeatIndividualAction = 1;
    private int maxTimesToRepeatActionSequence = 1;
    private double pauseBetweenActionSequences = 0;
    private double maxWait = 0;
    private int maxMatchesToActOn = -1;
    private int dragToOffsetX = 0;
    private int dragToOffsetY = 0;
    private ActionOptions.DefineAs defineAs = ActionOptions.DefineAs.MATCH;
    private int addW = 0;
    private int addH = 0;
    private int absoluteW = -1;
    private int absoluteH = -1;
    private int addX = 0;
    private int addY = 0;
    private int addX2 = 0;
    private int addY2 = 0;
    private boolean highlightAllAtOnce = false;
    private double highlightSeconds = 1;
    private String highlightColor = "red";
    private ActionOptions.GetTextUntil getTextUntil = ActionOptions.GetTextUntil.NONE;
    private String textToAppearOrVanish = "";
    private double typeDelay = Settings.TypeDelay;
    private String modifiers = "";
    private ActionOptions.ScrollDirection scrollDirection = ActionOptions.ScrollDirection.UP;
    private ActionOptions.Color color = ActionOptions.Color.MU;
    private int diameter = 5;
    private int kmeans = 2;
    private int hueBins = 12;
    private int saturationBins = 2;
    private int valueBins = 1;
    private double minScore = .7;
    private int minArea = 1;
    private int maxArea = -1;
    private int maxMovement = 300;
    private ActionOptions.Illustrate illustrate = ActionOptions.Illustrate.MAYBE;
    private ActionOptions.MatchFusionMethod fusionMethod = ActionOptions.MatchFusionMethod.NONE;
    private int maxFusionDistanceX = 5;
    private int maxFusionDistanceY = 5;
    private int sceneToUseForCaptureAfterFusingMatches = 0;
}
