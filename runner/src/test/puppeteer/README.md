# Desktop Runner Test

This test verifies that the Brobot Desktop Runner application starts successfully.

## Running the Test

From the puppeteer test directory:
```bash
node desktop-runner-test.js
```

Or with npm (after installing dependencies):
```bash
npm test
```

## Test Details

The test:
1. Launches the Brobot Runner using Gradle
2. Waits for Spring Boot to start
3. Waits for JavaFX application to initialize
4. Verifies the process is running
5. Cleans up by terminating the process

## Success Criteria

The test passes if:
- Spring Boot starts successfully
- JavaFX application initializes without errors
- The application process remains running for at least 5 seconds

## Troubleshooting

If the test fails due to database lock errors:
1. Kill any existing brobot processes: `pkill -f "brobot.*runner"`
2. Delete the database lock file if it exists
3. Run the test again

The application now uses H2's AUTO_SERVER mode to allow multiple connections.