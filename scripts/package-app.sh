#!/bin/bash
# Build a macOS .app runnable from 10.13 onwards. Run ON a Mac.
# Uses the system Java (no bundled runtime) for max compatibility + tiny size.
set -eu
cd "$(dirname "$0")/.."
VERSION="0.99-safety"
JAR="target/ZoomFirmwareEditor-${VERSION}.jar"

if [ ! -f "$JAR" ]; then
  echo "Building JAR first…"
  mvn -q package
fi

APP="dist/ZoomFirmwareEditor.app"
rm -rf dist
mkdir -p "$APP/Contents/MacOS" "$APP/Contents/Resources"

cp "packaging/macos/Info.plist" "$APP/Contents/Info.plist"
cp "$JAR" "$APP/Contents/Resources/ZoomFirmwareEditor.jar"
# copy icons if icns exists; harmless if missing
cp packaging/macos/*.icns "$APP/Contents/Resources/" 2>/dev/null || true

cat > "$APP/Contents/MacOS/ZoomFirmwareEditor" <<'EOF'
#!/bin/bash
cd "$(dirname "$0")/../Resources"
exec java -jar ZoomFirmwareEditor.jar
EOF
chmod +x "$APP/Contents/MacOS/ZoomFirmwareEditor"

echo "Built $APP (LSMinimumSystemVersion=$(grep -A1 LSMinimumSystemVersion Contents/Info.plist 2>/dev/null || grep -A1 LSMinimumSystemVersion "$APP/Contents/Info.plist"))"
echo "Run: open \"$APP\""
echo "Note: needs any Java 8+ installed (java -version). For a self-contained"
echo ".app, rebuild with jpackage + Temurin 11 (10.13-compatible runtime)."
