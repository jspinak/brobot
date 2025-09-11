package io.github.jspinak.brobot.persistence.provider;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.github.jspinak.brobot.model.action.ActionHistory;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.state.StateObject;
import io.github.jspinak.brobot.persistence.config.PersistenceConfiguration;
import io.github.jspinak.brobot.persistence.model.SessionData;

import lombok.extern.slf4j.Slf4j;

/**
 * File-based persistence provider. Stores action records in JSON or CSV files without requiring a
 * database.
 */
@Slf4j
public class FileBasedPersistenceProvider extends AbstractPersistenceProvider {

    private static final String SESSIONS_DIR = "sessions";
    private static final String METADATA_FILE = "metadata.json";
    private static final String RECORDS_FILE = "records";
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final ObjectMapper objectMapper;
    private final Path basePath;
    private final Map<String, SessionData> sessionCache = new ConcurrentHashMap<>();

    public FileBasedPersistenceProvider(PersistenceConfiguration configuration) {
        super(configuration);

        this.basePath = Paths.get(configuration.getFile().getBasePath());
        this.objectMapper = createObjectMapper();

        initializeStorage();
        loadExistingSessions();
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        if (configuration.getFile().isPrettyPrint()) {
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
        }

        return mapper;
    }

    private void initializeStorage() {
        try {
            Files.createDirectories(basePath);
            Files.createDirectories(basePath.resolve(SESSIONS_DIR));
            log.info("Initialized file storage at: {}", basePath);
        } catch (IOException e) {
            log.error("Failed to initialize storage directories", e);
            throw new RuntimeException("Cannot initialize file storage", e);
        }
    }

    private void loadExistingSessions() {
        Path sessionsPath = basePath.resolve(SESSIONS_DIR);

        try (Stream<Path> sessionDirs = Files.list(sessionsPath)) {
            List<Path> dirs = sessionDirs.filter(Files::isDirectory).collect(Collectors.toList());
            log.debug("Found {} session directories to load", dirs.size());
            dirs.forEach(this::loadSessionMetadata);
        } catch (IOException e) {
            log.error("Failed to load existing sessions", e);
        }
    }

    private void loadSessionMetadata(Path sessionDir) {
        Path metadataPath = sessionDir.resolve(METADATA_FILE);

        if (Files.exists(metadataPath)) {
            try {
                SessionMetadata metadata =
                        objectMapper.readValue(metadataPath.toFile(), SessionMetadata.class);

                SessionData sessionData = new SessionData();
                sessionData.setMetadata(metadata);
                sessionData.setPath(sessionDir);

                sessionCache.put(metadata.getSessionId(), sessionData);
                log.debug("Loaded session: {}", metadata.getName());

            } catch (IOException e) {
                log.error("Failed to load session metadata from: {}", metadataPath, e);
            }
        }
    }

    @Override
    protected void doStartSession(String sessionId, SessionMetadata metadata) {
        // Create session directory
        String dirName =
                String.format(
                        "%s_%s",
                        metadata.getStartTime().format(TIMESTAMP_FORMAT),
                        sanitizeFilename(metadata.getName()));

        Path sessionPath = basePath.resolve(SESSIONS_DIR).resolve(dirName);

        try {
            Files.createDirectories(sessionPath);

            // Save metadata
            Path metadataPath = sessionPath.resolve(METADATA_FILE);
            objectMapper.writeValue(metadataPath.toFile(), metadata);

            // Create session data
            SessionData sessionData = new SessionData();
            sessionData.setMetadata(metadata);
            sessionData.setPath(sessionPath);
            sessionData.setRecords(new ArrayList<>());

            sessionCache.put(sessionId, sessionData);

            log.debug("Created session directory: {}", sessionPath);

        } catch (IOException e) {
            log.error("Failed to create session", e);
            throw new RuntimeException("Cannot create session", e);
        }
    }

    @Override
    protected void doStopSession(String sessionId, SessionMetadata metadata) {
        SessionData sessionData = sessionCache.get(sessionId);

        if (sessionData != null) {
            try {
                // Update metadata with final stats
                Path metadataPath = sessionData.getPath().resolve(METADATA_FILE);
                objectMapper.writeValue(metadataPath.toFile(), metadata);

                // Flush any remaining records
                flushSessionRecords(sessionData);

                log.debug("Finalized session: {}", sessionId);

            } catch (IOException e) {
                log.error("Failed to finalize session", e);
            }
        }
    }

    @Override
    protected void doRecordAction(String sessionId, ActionRecord record, StateObject stateObject) {
        SessionData sessionData = sessionCache.get(sessionId);

        if (sessionData == null) {
            log.warn("Session not found: {}", sessionId);
            return;
        }

        // Add record to buffer
        sessionData.getRecords().add(record);

        // Check if we should flush
        if (sessionData.getRecords().size() >= configuration.getPerformance().getBufferSize()) {
            flushSessionRecords(sessionData);
        }
    }

    @Override
    protected SessionMetadata doGetSessionMetadata(String sessionId) {
        SessionData sessionData = sessionCache.get(sessionId);
        return sessionData != null ? sessionData.getMetadata() : null;
    }

    @Override
    public ActionHistory exportSession(String sessionId) {
        SessionData sessionData = sessionCache.get(sessionId);

        if (sessionData == null) {
            log.warn("Session not found for export: {}", sessionId);
            return null;
        }

        ActionHistory history = new ActionHistory();

        // Load all records for the session
        List<ActionRecord> records = loadSessionRecords(sessionData);

        for (ActionRecord record : records) {
            history.addSnapshot(record);
        }

        log.info("Exported session {} with {} records", sessionId, records.size());
        return history;
    }

    @Override
    public String importSession(ActionHistory history, String sessionName) {
        String sessionId = UUID.randomUUID().toString();

        SessionMetadata metadata = new SessionMetadata(sessionId, sessionName, "Imported");
        metadata.setStartTime(LocalDateTime.now());
        metadata.setEndTime(LocalDateTime.now());
        metadata.setTotalActions(history.getSnapshots().size());
        metadata.setSuccessfulActions(
                (int)
                        history.getSnapshots().stream()
                                .filter(ActionRecord::isActionSuccess)
                                .count());

        doStartSession(sessionId, metadata);

        SessionData sessionData = sessionCache.get(sessionId);
        if (sessionData != null) {
            sessionData.setRecords(new ArrayList<>(history.getSnapshots()));
            flushSessionRecords(sessionData);
        }

        doStopSession(sessionId, metadata);

        log.info("Imported session {} with {} records", sessionName, history.getSnapshots().size());
        return sessionId;
    }

    @Override
    public List<String> getAllSessions() {
        return new ArrayList<>(sessionCache.keySet());
    }

    @Override
    public void deleteSession(String sessionId) {
        SessionData sessionData = sessionCache.remove(sessionId);

        if (sessionData != null && sessionData.getPath() != null) {
            try {
                deleteDirectory(sessionData.getPath());
                log.info("Deleted session: {}", sessionId);
            } catch (IOException e) {
                log.error("Failed to delete session directory", e);
            }
        }
    }

    private void flushSessionRecords(SessionData sessionData) {
        if (sessionData.getRecords().isEmpty()) {
            return;
        }

        PersistenceConfiguration.FileFormat format = configuration.getFile().getFormat();
        String extension = format == PersistenceConfiguration.FileFormat.CSV ? "csv" : "json";

        // Create filename with timestamp and unique ID to avoid overwrites
        String filename =
                String.format(
                        "%s_%s_%s.%s",
                        RECORDS_FILE,
                        LocalDateTime.now().format(TIMESTAMP_FORMAT),
                        UUID.randomUUID().toString().substring(0, 8),
                        extension);

        if (configuration.getFile().isCompressExports()) {
            filename += ".gz";
        }

        Path recordsPath = sessionData.getPath().resolve(filename);

        try {
            switch (format) {
                case JSON:
                    writeJsonRecords(recordsPath, sessionData.getRecords());
                    break;
                case CSV:
                    writeCsvRecords(recordsPath, sessionData.getRecords());
                    break;
                default:
                    log.warn("Unsupported format: {}", format);
            }

            int recordCount = sessionData.getRecords().size();
            sessionData.getRecords().clear();
            log.debug("Flushed {} records to {}", recordCount, recordsPath);

        } catch (IOException e) {
            log.error("Failed to flush records", e);
        }
    }

    private void writeJsonRecords(Path path, List<ActionRecord> records) throws IOException {
        try (OutputStream os = createOutputStream(path);
                Writer writer =
                        new OutputStreamWriter(
                                os, Charset.forName(configuration.getFile().getEncoding()))) {

            objectMapper.writeValue(writer, records);
        }
    }

    private void writeCsvRecords(Path path, List<ActionRecord> records) throws IOException {
        try (OutputStream os = createOutputStream(path);
                Writer writer =
                        new OutputStreamWriter(
                                os, Charset.forName(configuration.getFile().getEncoding()));
                CSVPrinter csvPrinter =
                        new CSVPrinter(
                                writer,
                                CSVFormat.DEFAULT.withHeader(
                                        "Timestamp",
                                        "ActionType",
                                        "Success",
                                        "Duration",
                                        "State",
                                        "Object",
                                        "Text",
                                        "MatchCount"))) {

            for (ActionRecord record : records) {
                csvPrinter.printRecord(
                        LocalDateTime.now(),
                        record.getActionConfig() != null
                                ? record.getActionConfig().getClass().getSimpleName()
                                : "",
                        record.isActionSuccess(),
                        record.getDuration(),
                        "", // State name would need to be passed
                        "", // Object name would need to be passed
                        record.getText(),
                        record.getMatchList() != null ? record.getMatchList().size() : 0);
            }
        }
    }

    private List<ActionRecord> loadSessionRecords(SessionData sessionData) {
        List<ActionRecord> allRecords = new ArrayList<>();

        // Add any buffered records
        allRecords.addAll(sessionData.getRecords());

        // Load all record files
        try (Stream<Path> files = Files.list(sessionData.getPath())) {
            files.filter(p -> p.getFileName().toString().startsWith(RECORDS_FILE))
                    .sorted()
                    .forEach(
                            file -> {
                                try {
                                    List<ActionRecord> records = readRecordsFile(file);
                                    allRecords.addAll(records);
                                } catch (IOException e) {
                                    log.error("Failed to read records file: {}", file, e);
                                }
                            });
        } catch (IOException e) {
            log.error("Failed to list record files", e);
        }

        return allRecords;
    }

    private List<ActionRecord> readRecordsFile(Path path) throws IOException {
        String filename = path.getFileName().toString();

        if (filename.endsWith(".json") || filename.endsWith(".json.gz")) {
            return readJsonRecords(path);
        } else if (filename.endsWith(".csv") || filename.endsWith(".csv.gz")) {
            return readCsvRecords(path);
        }

        return Collections.emptyList();
    }

    private List<ActionRecord> readJsonRecords(Path path) throws IOException {
        try (InputStream is = createInputStream(path)) {
            return objectMapper.readValue(
                    is,
                    objectMapper
                            .getTypeFactory()
                            .constructCollectionType(List.class, ActionRecord.class));
        }
    }

    private List<ActionRecord> readCsvRecords(Path path) throws IOException {
        List<ActionRecord> records = new ArrayList<>();

        try (InputStream is = createInputStream(path);
                Reader reader =
                        new InputStreamReader(
                                is, Charset.forName(configuration.getFile().getEncoding()))) {

            Iterable<CSVRecord> csvRecords =
                    CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);

            for (CSVRecord csvRecord : csvRecords) {
                // Basic CSV parsing - would need enhancement for full ActionRecord
                ActionRecord record =
                        new ActionRecord.Builder()
                                .setActionSuccess(Boolean.parseBoolean(csvRecord.get("Success")))
                                .setDuration(Double.parseDouble(csvRecord.get("Duration")))
                                .setText(csvRecord.get("Text"))
                                .build();

                records.add(record);
            }
        }

        return records;
    }

    private OutputStream createOutputStream(Path path) throws IOException {
        OutputStream os = Files.newOutputStream(path);

        if (path.toString().endsWith(".gz")) {
            return new GZIPOutputStream(os);
        }

        return os;
    }

    private InputStream createInputStream(Path path) throws IOException {
        InputStream is = Files.newInputStream(path);

        if (path.toString().endsWith(".gz")) {
            return new GZIPInputStream(is);
        }

        return is;
    }

    private String sanitizeFilename(String name) {
        return name.replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    private void deleteDirectory(Path directory) throws IOException {
        Files.walk(directory)
                .sorted(Comparator.reverseOrder())
                .forEach(
                        path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                log.warn("Failed to delete: {}", path);
                            }
                        });
    }
}
