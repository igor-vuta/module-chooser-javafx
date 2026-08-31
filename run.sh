#!/bin/bash
# Build + run the ModuleChooser JavaFX app.
# Java 25 does not bundle JavaFX, so we fetch the three modules we need from
# Maven Central on first run and cache them in ./.fx (gitignore this).
set -euo pipefail

HERE="$(cd "$(dirname "$0")" && pwd)"
SRC="$HERE/CTEC2710Assignment/ModuleChooser/src"
OUT="$HERE/CTEC2710Assignment/ModuleChooser/bin"
FX="$HERE/.fx"
FX_VERSION="${FX_VERSION:-25}"

# Apple Silicon vs Intel
case "$(uname -s)-$(uname -m)" in
  Darwin-arm64)  CLASSIFIER=mac-aarch64 ;;
  Darwin-*)      CLASSIFIER=mac ;;
  Linux-aarch64) CLASSIFIER=linux-aarch64 ;;
  *)             CLASSIFIER=linux ;;
esac

mkdir -p "$FX"
for module in base graphics controls; do
  jar="$FX/javafx-$module-$FX_VERSION-$CLASSIFIER.jar"
  if [ ! -f "$jar" ]; then
    echo "Fetching javafx-$module $FX_VERSION ($CLASSIFIER)..."
    curl -sSL -o "$jar" \
      "https://repo1.maven.org/maven2/org/openjfx/javafx-$module/$FX_VERSION/javafx-$module-$FX_VERSION-$CLASSIFIER.jar"
  fi
done

echo "Compiling..."
rm -rf "$OUT"
mkdir -p "$OUT"
javac --module-path "$FX" --add-modules javafx.controls -d "$OUT" \
  $(find "$SRC" -name '*.java')

# academic.css is loaded as a classpath resource, so it must sit next to the packages
cp "$SRC/academic.css" "$OUT/"

if [ "${1:-}" = "--compile-only" ]; then
  echo "Compile OK."
  exit 0
fi

echo "Running..."
exec java --module-path "$FX" --add-modules javafx.controls -cp "$OUT" main.ApplicationLoader
