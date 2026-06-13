#!/bin/bash
# ThunderJS / Tode — Executable JAR Build Script
set -e

# Resolve script directory (absolute path)
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

# Clean up previous builds
rm -rf out
rm -f tode.jar

# Compile Java source files to out/
echo "⚡ Compiling Java source files..."
mkdir -p out
javac -d out $(find src -name "*.java")

# Package into executable JAR
echo "📦 Packaging executable tode.jar..."
jar cfe tode.jar thunderjs.Main -C out .

echo "✓ Build successful! You can now run Tode using:"
echo "  java -jar tode.jar <file.js>"
