#!/bin/bash
# ThunderJS / Tode — Executable JAR Build Script
set -e

# Resolve the project root directory (one level up from src/)
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$ROOT_DIR"

# Clean up previous build outputs at root
rm -rf out
rm -f tode.jar

# Compile Java source files to out/
echo "⚡ Compiling Java source files..."
mkdir -p out
javac -d out $(find src -name "*.java")

# Package into executable JAR at root
echo "📦 Packaging executable tode.jar..."
jar cfe tode.jar thunderjs.Main -C out .

# Clean up temporary compiled class files
rm -rf out

echo "✓ Build successful! You can now run Tode using:"
echo "  java -jar tode.jar <file.js>"
