#!/bin/bash
# Generate ICNS from iconset
cd "$(dirname "$0")"
iconutil -c icns brobot.iconset -o brobot.icns
echo "Created brobot.icns"
