# Images Directory - Tutorial Basics

Place your screenshot images here for pattern matching in the Tutorial Basics example.

## Expected Structure

This example uses a 3-state navigation system (Home, World, Island) with the following image organization:

```
images/
├── home/
│   ├── to_world_button.png
│   ├── home_logo.png
│   ├── menu_bar.png
│   └── search_button.png
├── world/
│   ├── search_button.png
│   ├── island_castle.png
│   ├── island_mines.png
│   ├── island_farms.png
│   ├── island_forest.png
│   └── home_button.png
├── island/
│   ├── castle.png
│   ├── mines.png
│   ├── farms.png
│   ├── forest.png
│   ├── mountains.png
│   ├── lakes.png
│   ├── gold_icon.png
│   ├── wood_icon.png
│   ├── stone_icon.png
│   ├── build_button.png
│   ├── back_to_world.png
│   └── island_name.png
└── common/
    └── (shared UI elements across states)
```

## State-Image Mapping

### HomeState (images/home/)
- `to_world_button.png` - Button to navigate to World state
- `home_logo.png` - Home screen logo (for verification)
- `menu_bar.png` - Main menu bar
- `search_button.png` - Search functionality

### WorldState (images/world/)
- `search_button.png` - World map search button
- `island_castle.png` - Castle island thumbnail
- `island_mines.png` - Mines island thumbnail
- `island_farms.png` - Farms island thumbnail
- `island_forest.png` - Forest island thumbnail
- `home_button.png` - Return to home button

### IslandState (images/island/)
- `castle.png` - Castle building
- `mines.png` - Mines building
- `farms.png` - Farms building
- `forest.png` - Forest area
- `mountains.png` - Mountain area
- `lakes.png` - Lake area
- `gold_icon.png` - Gold resource icon
- `wood_icon.png` - Wood resource icon
- `stone_icon.png` - Stone resource icon
- `build_button.png` - Build/construct button
- `back_to_world.png` - Return to world map button
- `island_name.png` - Island name text area (for OCR)

## Mock Mode

This example runs in **mock mode by default** (configured in `application.yml`):
- No actual GUI interaction occurs
- Actions are simulated with configured delays
- Perfect for testing and understanding the API
- **No image files are required for mock mode**

## To Use Real GUI Automation

1. **Set mock mode to false** in `src/main/resources/application.yml`:
   ```yaml
   brobot:
     mock: false
   ```

2. **Capture screenshots** of your target application:
   - Use Brobot's screenshot capture tool or your OS utility
   - Capture only the UI element with a small margin (5-10 pixels)
   - Save as PNG format with the exact filenames listed above

3. **Organize by state**: Place each image in its corresponding state directory

4. **Run the application**:
   ```bash
   ./gradlew bootRun
   ```

## Capturing Screenshots

### Best Practices:
- **Resolution**: Capture at the same screen resolution where automation will run
- **Format**: PNG (lossless compression)
- **Size**: Minimum 20x20 pixels, crop tightly around the element
- **Naming**: Use descriptive kebab-case names (e.g., `back_to_world.png`)
- **Uniqueness**: Ensure each image is visually distinct
- **Consistency**: Capture in the same application state/theme

### Tips:
- For buttons: Include the entire button with border
- For icons: Include just the icon, no surrounding UI
- For text: Include the full text area with padding
- Test with various similarity thresholds (0.7-0.95) to find optimal matching

## Troubleshooting

### Image Not Found
- Verify the filename matches exactly (case-sensitive)
- Check the image is in the correct state directory
- Ensure PNG format (not JPG or other formats)
- Verify image isn't corrupted (can you open it?)

### Pattern Matching Issues
- Try adjusting similarity threshold in PatternFindOptions
- Capture a fresh screenshot with current application version
- Ensure no UI theme changes have occurred
- Check for dynamic elements (tooltips, animations) in screenshot

### Performance Issues
- Use smaller images when possible
- Limit search regions using StateRegion
- Consider using fixed positions for known locations
- Enable pattern matching caching

## Related Documentation

- **State Management**: See `/docs/02-core-concepts/states.md`
- **Pattern Matching**: See `/docs/02-core-concepts/pattern-matching.md`
- **Mock Mode**: See `/docs/04-testing/mock-mode-guide.md`
- **Image Capture**: See `/docs/05-guides/capturing-screenshots.md`

## Example Code Reference

The images in this directory correspond to StateImage definitions in:
- `src/main/java/com/example/basics/states/HomeState.java`
- `src/main/java/com/example/basics/states/WorldState.java`
- `src/main/java/com/example/basics/states/IslandState.java`
