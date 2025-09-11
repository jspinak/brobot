package io.github.jspinak.brobot.persistence.database.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import io.github.jspinak.brobot.test.BrobotTestBase;

/** Unit tests for database entities. */
@DisplayName("Database Entity Tests")
class DatabaseEntityTest extends BrobotTestBase {

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
    }

    @Nested
    @DisplayName("RecordingSessionEntity Tests")
    class RecordingSessionEntityTests {

        private RecordingSessionEntity entity;

        @BeforeEach
        void setup() {
            entity = new RecordingSessionEntity();
        }

        @Test
        @DisplayName("Should initialize with default values")
        void testDefaultValues() {
            assertNull(entity.getId());
            assertNull(entity.getSessionId());
            assertEquals(RecordingSessionEntity.SessionStatus.ACTIVE, entity.getStatus());
            assertFalse(entity.isImported());
            assertEquals(0, entity.getTotalActions());
            assertEquals(0, entity.getSuccessfulActions());
            assertEquals(0, entity.getFailedActions());
            assertEquals(0.0, entity.getSuccessRate());
            assertNotNull(entity.getActionRecords());
            assertTrue(entity.getActionRecords().isEmpty());
        }

        @Test
        @DisplayName("Should set and get all properties")
        void testProperties() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            Duration duration = Duration.ofMinutes(5);

            // When
            entity.setId(1L);
            entity.setSessionId("session-123");
            entity.setName("Test Session");
            entity.setApplication("TestApp");
            entity.setDescription("Test description");
            entity.setStartTime(now);
            entity.setEndTime(now.plusMinutes(5));
            entity.setDuration(duration);
            entity.setStatus(RecordingSessionEntity.SessionStatus.COMPLETED);
            entity.setImported(true);
            entity.setTotalActions(10);
            entity.setSuccessfulActions(8);
            entity.setFailedActions(2);
            entity.setSuccessRate(80.0);

            // Then
            assertEquals(1L, entity.getId());
            assertEquals("session-123", entity.getSessionId());
            assertEquals("Test Session", entity.getName());
            assertEquals("TestApp", entity.getApplication());
            assertEquals("Test description", entity.getDescription());
            assertEquals(now, entity.getStartTime());
            assertEquals(now.plusMinutes(5), entity.getEndTime());
            assertEquals(duration, entity.getDuration());
            assertEquals(RecordingSessionEntity.SessionStatus.COMPLETED, entity.getStatus());
            assertTrue(entity.isImported());
            assertEquals(10, entity.getTotalActions());
            assertEquals(8, entity.getSuccessfulActions());
            assertEquals(2, entity.getFailedActions());
            assertEquals(80.0, entity.getSuccessRate());
        }

        @Test
        @DisplayName("Should handle onCreate lifecycle callback")
        void testOnCreate() {
            // When
            entity.onCreate();

            // Then
            assertNotNull(entity.getCreatedAt());
            assertNotNull(entity.getUpdatedAt());
            assertNotNull(entity.getStartTime());
            // These should be very close in time (within 1 second)
            assertTrue(
                    Math.abs(
                                    entity.getCreatedAt().toEpochSecond(java.time.ZoneOffset.UTC)
                                            - entity.getUpdatedAt()
                                                    .toEpochSecond(java.time.ZoneOffset.UTC))
                            <= 1);
        }

        @Test
        @DisplayName("Should handle onUpdate lifecycle callback")
        void testOnUpdate() {
            // Given
            entity.onCreate();
            LocalDateTime originalUpdatedAt = entity.getUpdatedAt();

            // Wait a tiny bit to ensure time difference
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                // Ignore
            }

            // When
            entity.onUpdate();

            // Then
            assertNotNull(entity.getUpdatedAt());
            assertTrue(entity.getUpdatedAt().isAfter(originalUpdatedAt));
        }

        @Test
        @DisplayName("Should manage action records relationship")
        void testActionRecordsRelationship() {
            // Given
            ActionRecordEntity record1 = new ActionRecordEntity();
            record1.setId(1L);
            record1.setSession(entity);

            ActionRecordEntity record2 = new ActionRecordEntity();
            record2.setId(2L);
            record2.setSession(entity);

            // When
            entity.getActionRecords().add(record1);
            entity.getActionRecords().add(record2);

            // Then
            assertEquals(2, entity.getActionRecords().size());
            assertTrue(entity.getActionRecords().contains(record1));
            assertTrue(entity.getActionRecords().contains(record2));
        }

        @Test
        @DisplayName("Should set success rate correctly")
        void testSuccessRateCalculation() {
            // Given
            entity.setTotalActions(100);
            entity.setSuccessfulActions(75);
            entity.setFailedActions(25);

            // When
            entity.setSuccessRate(75.0);

            // Then
            assertEquals(75.0, entity.getSuccessRate(), 0.01);
        }
    }

    @Nested
    @DisplayName("ActionRecordEntity Tests")
    class ActionRecordEntityTests {

        private ActionRecordEntity entity;
        private RecordingSessionEntity session;

        @BeforeEach
        void setup() {
            entity = new ActionRecordEntity();
            session = new RecordingSessionEntity();
            session.setId(1L);
            session.setSessionId("test-session");
        }

        @Test
        @DisplayName("Should initialize with default values")
        void testDefaultValues() {
            assertNull(entity.getId());
            assertNull(entity.getSession());
            assertFalse(entity.isActionSuccess());
            assertEquals(0, entity.getDuration());
            assertNotNull(entity.getMatches());
            assertTrue(entity.getMatches().isEmpty());
        }

        @Test
        @DisplayName("Should set and get all properties")
        void testProperties() {
            // Given
            LocalDateTime timestamp = LocalDateTime.now();

            // When
            entity.setId(1L);
            entity.setSession(session);
            entity.setActionConfigType("CLICK");
            entity.setActionConfigJson("config json");
            entity.setActionSuccess(true);
            entity.setDuration(100L);
            entity.setText("Test action");
            entity.setStateName("TestState");
            entity.setObjectName("button");
            entity.setScreenshot("screenshot.png");
            entity.setTimestamp(timestamp);

            // Then
            assertEquals(1L, entity.getId());
            assertEquals(session, entity.getSession());
            assertEquals("CLICK", entity.getActionConfigType());
            assertEquals("config json", entity.getActionConfigJson());
            assertTrue(entity.isActionSuccess());
            assertEquals(100L, entity.getDuration());
            assertEquals("Test action", entity.getText());
            assertEquals("TestState", entity.getStateName());
            assertEquals("button", entity.getObjectName());
            assertEquals("screenshot.png", entity.getScreenshot());
            assertEquals(timestamp, entity.getTimestamp());
        }

        @Test
        @DisplayName("Should manage matches relationship")
        void testMatchesRelationship() {
            // Given
            MatchEntity match1 = new MatchEntity();
            match1.setId(1L);
            match1.setActionRecord(entity);
            match1.setSimScore(0.95);

            MatchEntity match2 = new MatchEntity();
            match2.setId(2L);
            match2.setActionRecord(entity);
            match2.setSimScore(0.85);

            // When
            entity.getMatches().add(match1);
            entity.getMatches().add(match2);

            // Then
            assertEquals(2, entity.getMatches().size());
            assertTrue(entity.getMatches().contains(match1));
            assertTrue(entity.getMatches().contains(match2));
        }

        @Test
        @DisplayName("Should handle onCreate lifecycle callback")
        void testOnCreate() {
            // When
            entity.onCreate();

            // Then
            assertNotNull(entity.getTimestamp());
            assertNotNull(entity.getCreatedAt());
        }
    }

    @Nested
    @DisplayName("MatchEntity Tests")
    class MatchEntityTests {

        private MatchEntity entity;
        private ActionRecordEntity actionRecord;

        @BeforeEach
        void setup() {
            entity = new MatchEntity();
            actionRecord = new ActionRecordEntity();
            actionRecord.setId(1L);
        }

        @Test
        @DisplayName("Should initialize with default values")
        void testDefaultValues() {
            assertNull(entity.getId());
            assertNull(entity.getActionRecord());
            assertEquals(0, entity.getX());
            assertEquals(0, entity.getY());
            assertEquals(0, entity.getWidth());
            assertEquals(0, entity.getHeight());
            assertEquals(0.0, entity.getSimScore());
        }

        @Test
        @DisplayName("Should set and get all properties")
        void testProperties() {
            // When
            entity.setId(1L);
            entity.setActionRecord(actionRecord);
            entity.setX(100);
            entity.setY(200);
            entity.setWidth(300);
            entity.setHeight(400);
            entity.setSimScore(0.95);
            entity.setMatchIndex(0);
            entity.setName("button");

            // Then
            assertEquals(1L, entity.getId());
            assertEquals(actionRecord, entity.getActionRecord());
            assertEquals(100, entity.getX());
            assertEquals(200, entity.getY());
            assertEquals(300, entity.getWidth());
            assertEquals(400, entity.getHeight());
            assertEquals(0.95, entity.getSimScore());
            assertEquals(0, entity.getMatchIndex());
            assertEquals("button", entity.getName());
        }
    }

    @Nested
    @DisplayName("Entity Relationship Tests")
    class EntityRelationshipTests {

        @Test
        @DisplayName("Should maintain bidirectional relationship between session and records")
        void testSessionRecordRelationship() {
            // Given
            RecordingSessionEntity session = new RecordingSessionEntity();
            session.setSessionId("test-session");

            ActionRecordEntity record1 = new ActionRecordEntity();
            record1.setSession(session);

            ActionRecordEntity record2 = new ActionRecordEntity();
            record2.setSession(session);

            // When
            session.getActionRecords().add(record1);
            session.getActionRecords().add(record2);

            // Then
            assertEquals(2, session.getActionRecords().size());
            assertEquals(session, record1.getSession());
            assertEquals(session, record2.getSession());
        }

        @Test
        @DisplayName("Should maintain bidirectional relationship between record and matches")
        void testRecordMatchRelationship() {
            // Given
            ActionRecordEntity record = new ActionRecordEntity();

            MatchEntity match1 = new MatchEntity();
            match1.setActionRecord(record);
            match1.setSimScore(0.95);

            MatchEntity match2 = new MatchEntity();
            match2.setActionRecord(record);
            match2.setSimScore(0.85);

            // When
            record.getMatches().add(match1);
            record.getMatches().add(match2);

            // Then
            assertEquals(2, record.getMatches().size());
            assertEquals(record, match1.getActionRecord());
            assertEquals(record, match2.getActionRecord());
        }
    }
}
