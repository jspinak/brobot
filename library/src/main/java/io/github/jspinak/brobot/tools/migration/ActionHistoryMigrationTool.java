package io.github.jspinak.brobot.tools.migration;

import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Command-line tool for migrating user codebases from ActionOptions to ActionConfig.
 * 
 * <p>This tool scans a codebase and performs automated migration of:
 * <ul>
 *   <li>Java source files using deprecated ActionHistory APIs</li>
 *   <li>JSON data files containing serialized ActionHistory</li>
 *   <li>Spring configuration files</li>
 *   <li>Test files</li>
 * </ul>
 * 
 * <p>Usage:
 * <pre>
 * java -jar brobot-migration-tool.jar \
 *   --path /path/to/project \
 *   --mode [analyze|migrate|both] \
 *   --backup true \
 *   --include "*.java,*.json" \
 *   --exclude "target/*,build/*"
 * </pre>
 * 
 * @since 1.2.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Profile("migration")
public class ActionHistoryMigrationTool implements CommandLineRunner {
    
    private final ActionHistoryMigrationService migrationService;
    private final ActionHistoryJsonConverter jsonConverter;
    private final DeprecationMetrics deprecationMetrics;
    
    /**
     * Migration modes.
     */
    public enum Mode {
        ANALYZE,    // Only analyze and report
        MIGRATE,    // Perform migration
        BOTH        // Analyze then migrate
    }
    
    /**
     * Migration configuration.
     */
    public static class MigrationConfig {
        public Path projectPath = Paths.get(".");
        public Mode mode = Mode.ANALYZE;
        public boolean createBackup = true;
        public List<String> includePatterns = Arrays.asList("*.java", "*.json");
        public List<String> excludePatterns = Arrays.asList("target/*", "build/*", ".git/*");
        public boolean dryRun = false;
        public Path outputPath = Paths.get("migration-report.txt");
    }
    
    /**
     * Migration results.
     */
    public static class MigrationResult {
        public int filesAnalyzed = 0;
        public int filesWithDeprecatedCode = 0;
        public int filesMigrated = 0;
        public int migrationErrors = 0;
        public List<String> errorFiles = new ArrayList<>();
        public Map<String, List<String>> deprecatedUsageByFile = new HashMap<>();
        public long startTime;
        public long endTime;
        
        public String getSummary() {
            long duration = (endTime - startTime) / 1000;
            return String.format(
                "Migration Summary:\n" +
                "  Files analyzed: %d\n" +
                "  Files with deprecated code: %d\n" +
                "  Files migrated: %d\n" +
                "  Migration errors: %d\n" +
                "  Duration: %d seconds",
                filesAnalyzed, filesWithDeprecatedCode, filesMigrated, migrationErrors, duration
            );
        }
    }
    
    @Override
    public void run(String... args) throws Exception {
        MigrationConfig config = parseArguments(args);
        
        log.info("Starting ActionHistory migration tool");
        log.info("Project path: {}", config.projectPath);
        log.info("Mode: {}", config.mode);
        log.info("Dry run: {}", config.dryRun);
        
        MigrationResult result = new MigrationResult();
        result.startTime = System.currentTimeMillis();
        
        try {
            if (config.mode == Mode.ANALYZE || config.mode == Mode.BOTH) {
                analyzeProject(config, result);
            }
            
            if (config.mode == Mode.MIGRATE || config.mode == Mode.BOTH) {
                if (config.createBackup) {
                    createBackup(config);
                }
                migrateProject(config, result);
            }
            
            result.endTime = System.currentTimeMillis();
            
            // Generate and save report
            String report = generateReport(config, result);
            Files.writeString(config.outputPath, report);
            
            log.info(result.getSummary());
            log.info("Report written to: {}", config.outputPath);
            
        } catch (Exception e) {
            log.error("Migration failed: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Analyzes the project for deprecated API usage.
     */
    private void analyzeProject(MigrationConfig config, MigrationResult result) throws IOException {
        log.info("Analyzing project for deprecated API usage...");
        
        Files.walkFileTree(config.projectPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (shouldProcessFile(file, config)) {
                    result.filesAnalyzed++;
                    
                    if (file.toString().endsWith(".java")) {
                        analyzeJavaFile(file, result);
                    } else if (file.toString().endsWith(".json")) {
                        analyzeJsonFile(file, result);
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }
    
    /**
     * Analyzes a Java file for deprecated API usage.
     */
    private void analyzeJavaFile(Path file, MigrationResult result) throws IOException {
        String content = Files.readString(file);
        List<String> deprecatedUsages = new ArrayList<>();
        
        // Check for deprecated method calls
        if (content.contains("getRandomSnapshot(ActionOptions.Action")) {
            deprecatedUsages.add("getRandomSnapshot(ActionOptions.Action)");
        }
        if (content.contains("getRandomMatchList(ActionOptions")) {
            deprecatedUsages.add("getRandomMatchList(ActionOptions)");
        }
        if (content.contains("getSimilarSnapshots(ActionOptions")) {
            deprecatedUsages.add("getSimilarSnapshots(ActionOptions)");
        }
        
        // Check for ActionOptions usage in ActionRecord creation
        if (content.contains("setActionOptions(")) {
            deprecatedUsages.add("ActionRecord.setActionOptions()");
        }
        
        // Check for ActionOptions.Action enum usage
        if (content.contains("ActionOptions.Action.")) {
            deprecatedUsages.add("ActionOptions.Action enum");
        }
        
        if (!deprecatedUsages.isEmpty()) {
            result.filesWithDeprecatedCode++;
            result.deprecatedUsageByFile.put(file.toString(), deprecatedUsages);
            log.debug("Found deprecated usage in {}: {}", file, deprecatedUsages);
        }
    }
    
    /**
     * Analyzes a JSON file for legacy ActionHistory format.
     */
    private void analyzeJsonFile(Path file, MigrationResult result) throws IOException {
        String content = Files.readString(file);
        
        if (content.contains("\"actionOptions\"") && !content.contains("\"actionConfig\"")) {
            result.filesWithDeprecatedCode++;
            result.deprecatedUsageByFile.put(file.toString(), 
                Arrays.asList("Legacy ActionOptions-based JSON"));
            log.debug("Found legacy JSON format in {}", file);
        }
    }
    
    /**
     * Migrates the project from deprecated to modern API.
     */
    private void migrateProject(MigrationConfig config, MigrationResult result) throws IOException {
        log.info("Starting project migration...");
        
        Files.walkFileTree(config.projectPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (shouldProcessFile(file, config) && 
                    result.deprecatedUsageByFile.containsKey(file.toString())) {
                    
                    try {
                        if (file.toString().endsWith(".java")) {
                            migrateJavaFile(file, config, result);
                        } else if (file.toString().endsWith(".json")) {
                            migrateJsonFile(file, config, result);
                        }
                    } catch (Exception e) {
                        log.error("Failed to migrate {}: {}", file, e.getMessage());
                        result.migrationErrors++;
                        result.errorFiles.add(file.toString());
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }
    
    /**
     * Migrates a Java source file.
     */
    private void migrateJavaFile(Path file, MigrationConfig config, MigrationResult result) 
            throws IOException {
        
        if (config.dryRun) {
            log.info("[DRY RUN] Would migrate: {}", file);
            return;
        }
        
        String content = Files.readString(file);
        String original = content;
        
        // Add necessary imports
        if (!content.contains("import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;") &&
            content.contains("ActionOptions.Action.FIND")) {
            content = addImportAfterPackage(content, 
                "import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;");
        }
        
        // Replace deprecated method calls
        content = content.replaceAll(
            "getRandomSnapshot\\(ActionOptions\\.Action\\.(\\w+)\\)",
            "getRandomSnapshot(create$1Config())"
        );
        
        // Replace ActionOptions.Action.FIND with PatternFindOptions
        content = content.replaceAll(
            "ActionOptions\\.Action\\.FIND",
            "new PatternFindOptions.Builder().build()"
        );
        
        // Replace ActionOptions.Action.CLICK with ClickOptions
        content = content.replaceAll(
            "ActionOptions\\.Action\\.CLICK",
            "new ClickOptions.Builder().build()"
        );
        
        // Replace setActionOptions with setActionConfig in ActionRecord.Builder
        content = migrateActionRecordBuilder(content);
        
        if (!content.equals(original)) {
            Files.writeString(file, content);
            result.filesMigrated++;
            log.info("Migrated: {}", file);
        }
    }
    
    /**
     * Migrates ActionRecord.Builder usage.
     */
    private String migrateActionRecordBuilder(String content) {
        // This is a simplified version - a real implementation would use proper AST parsing
        Pattern pattern = Pattern.compile(
            "\\.setActionOptions\\(new ActionOptions\\.Builder\\(\\)([^)]+)\\)",
            Pattern.DOTALL
        );
        
        return pattern.matcher(content).replaceAll(match -> {
            String optionsContent = match.group(1);
            
            if (optionsContent.contains("Action.FIND")) {
                return ".setActionConfig(new PatternFindOptions.Builder()" + 
                       convertOptionsToConfig(optionsContent) + ")";
            } else if (optionsContent.contains("Action.CLICK")) {
                return ".setActionConfig(new ClickOptions.Builder()" + 
                       convertOptionsToConfig(optionsContent) + ")";
            }
            
            return match.group(); // Return unchanged if not recognized
        });
    }
    
    /**
     * Converts ActionOptions builder calls to ActionConfig builder calls.
     */
    private String convertOptionsToConfig(String optionsContent) {
        return optionsContent
            .replaceAll("\\.setAction\\([^)]+\\)", "")
            .replaceAll("\\.setFind\\(ActionOptions\\.Find\\.(\\w+)\\)", 
                       ".setStrategy(PatternFindOptions.Strategy.$1)")
            .replaceAll("\\.setClickType\\(ActionOptions\\.ClickType\\.(\\w+)\\)",
                       ".setClickType(ClickOptions.Type.$1)");
    }
    
    /**
     * Migrates a JSON file.
     */
    private void migrateJsonFile(Path file, MigrationConfig config, MigrationResult result) 
            throws IOException {
        
        if (config.dryRun) {
            log.info("[DRY RUN] Would migrate JSON: {}", file);
            return;
        }
        
        boolean success = jsonConverter.migrateJsonFile(file);
        if (success) {
            result.filesMigrated++;
            log.info("Migrated JSON: {}", file);
        } else {
            result.migrationErrors++;
            result.errorFiles.add(file.toString());
        }
    }
    
    /**
     * Creates a backup of the project.
     */
    private void createBackup(MigrationConfig config) throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path backupPath = config.projectPath.resolveSibling(
            config.projectPath.getFileName() + "_backup_" + timestamp
        );
        
        log.info("Creating backup at: {}", backupPath);
        
        Files.walkFileTree(config.projectPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) 
                    throws IOException {
                Path targetDir = backupPath.resolve(config.projectPath.relativize(dir));
                Files.createDirectories(targetDir);
                return FileVisitResult.CONTINUE;
            }
            
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) 
                    throws IOException {
                Path targetFile = backupPath.resolve(config.projectPath.relativize(file));
                Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
        
        log.info("Backup completed");
    }
    
    /**
     * Checks if a file should be processed based on include/exclude patterns.
     */
    private boolean shouldProcessFile(Path file, MigrationConfig config) {
        String filePath = file.toString();
        
        // Check exclude patterns
        for (String pattern : config.excludePatterns) {
            if (matchesPattern(filePath, pattern)) {
                return false;
            }
        }
        
        // Check include patterns
        for (String pattern : config.includePatterns) {
            if (matchesPattern(filePath, pattern)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Simple pattern matching (supports * wildcard).
     */
    private boolean matchesPattern(String path, String pattern) {
        String regex = pattern.replace(".", "\\.")
                              .replace("*", ".*")
                              .replace("?", ".");
        return path.matches(regex);
    }
    
    /**
     * Adds an import statement after the package declaration.
     */
    private String addImportAfterPackage(String content, String importStatement) {
        int packageEnd = content.indexOf(";");
        if (packageEnd != -1) {
            return content.substring(0, packageEnd + 1) + "\n" + 
                   importStatement + "\n" + 
                   content.substring(packageEnd + 1);
        }
        return importStatement + "\n" + content;
    }
    
    /**
     * Generates a detailed migration report.
     */
    private String generateReport(MigrationConfig config, MigrationResult result) {
        StringBuilder report = new StringBuilder();
        
        report.append("=== ActionHistory Migration Report ===\n");
        report.append("Generated: ").append(LocalDateTime.now()).append("\n");
        report.append("Project: ").append(config.projectPath.toAbsolutePath()).append("\n");
        report.append("Mode: ").append(config.mode).append("\n");
        report.append("Dry run: ").append(config.dryRun).append("\n\n");
        
        report.append(result.getSummary()).append("\n\n");
        
        if (!result.deprecatedUsageByFile.isEmpty()) {
            report.append("=== Files with Deprecated Usage ===\n");
            result.deprecatedUsageByFile.forEach((file, usages) -> {
                report.append("\nFile: ").append(file).append("\n");
                usages.forEach(usage -> 
                    report.append("  - ").append(usage).append("\n"));
            });
        }
        
        if (!result.errorFiles.isEmpty()) {
            report.append("\n=== Migration Errors ===\n");
            result.errorFiles.forEach(file -> 
                report.append("  - ").append(file).append("\n"));
        }
        
        report.append("\n=== Deprecation Metrics ===\n");
        report.append(deprecationMetrics.generateReport());
        
        report.append("\n=== Recommendations ===\n");
        if (result.migrationErrors > 0) {
            report.append("- Review and manually fix files with migration errors\n");
        }
        if (result.filesWithDeprecatedCode > result.filesMigrated) {
            report.append("- Some files could not be automatically migrated\n");
            report.append("- Consider manual review or running with different settings\n");
        }
        report.append("- Run tests to ensure migration didn't break functionality\n");
        report.append("- Review generated code for correctness\n");
        
        return report.toString();
    }
    
    /**
     * Parses command line arguments.
     */
    private MigrationConfig parseArguments(String[] args) {
        MigrationConfig config = new MigrationConfig();
        
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--path":
                    config.projectPath = Paths.get(args[++i]);
                    break;
                case "--mode":
                    config.mode = Mode.valueOf(args[++i].toUpperCase());
                    break;
                case "--backup":
                    config.createBackup = Boolean.parseBoolean(args[++i]);
                    break;
                case "--include":
                    config.includePatterns = Arrays.asList(args[++i].split(","));
                    break;
                case "--exclude":
                    config.excludePatterns = Arrays.asList(args[++i].split(","));
                    break;
                case "--dry-run":
                    config.dryRun = Boolean.parseBoolean(args[++i]);
                    break;
                case "--output":
                    config.outputPath = Paths.get(args[++i]);
                    break;
                case "--help":
                    printHelp();
                    System.exit(0);
                    break;
            }
        }
        
        return config;
    }
    
    /**
     * Prints usage help.
     */
    private void printHelp() {
        System.out.println(
            "ActionHistory Migration Tool\n" +
            "\n" +
            "Usage: java -jar brobot-migration-tool.jar [options]\n" +
            "\n" +
            "Options:\n" +
            "  --path <path>      Project path (default: current directory)\n" +
            "  --mode <mode>      Migration mode: analyze, migrate, both (default: analyze)\n" +
            "  --backup <bool>    Create backup before migration (default: true)\n" +
            "  --include <list>   Comma-separated file patterns to include (default: *.java,*.json)\n" +
            "  --exclude <list>   Comma-separated patterns to exclude (default: target/*,build/*,.git/*)\n" +
            "  --dry-run <bool>   Perform dry run without changes (default: false)\n" +
            "  --output <path>    Output report path (default: migration-report.txt)\n" +
            "  --help             Show this help message\n" +
            "\n" +
            "Examples:\n" +
            "  # Analyze project for deprecated usage\n" +
            "  java -jar brobot-migration-tool.jar --path /my/project --mode analyze\n" +
            "\n" +
            "  # Perform full migration with backup\n" +
            "  java -jar brobot-migration-tool.jar --path /my/project --mode migrate --backup true\n" +
            "\n" +
            "  # Dry run to see what would be changed\n" +
            "  java -jar brobot-migration-tool.jar --path /my/project --mode migrate --dry-run true\n"
        );
    }
    
    /**
     * Main method for standalone execution.
     */
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ActionHistoryMigrationTool.class);
        app.setAdditionalProfiles("migration");
        app.run(args);
    }
}