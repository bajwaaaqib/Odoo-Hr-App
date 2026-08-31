#!/bin/bash

# Geo Attendance App - Quick Start Script
# This script helps you set up and run the Flutter app

set -e

echo "🚀 Geo Attendance App - Setup & Run"
echo "===================================="
echo ""

# Check if Docker is available
if command -v docker &> /dev/null && command -v docker-compose &> /dev/null; then
    echo "✅ Docker and Docker Compose found"
    echo ""
    echo "Starting development environment..."
    docker-compose up --build -d
    echo ""
    echo "✅ Development container started!"
    echo ""
    echo "To access the container:"
    echo "  docker-compose exec flutter_dev bash"
    echo ""
    echo "Inside the container, run:"
    echo "  cd /app"
    echo "  flutter pub get"
    echo "  flutter run -d chrome  # For web testing"
    echo ""
else
    echo "⚠️  Docker not found. Checking for Flutter..."
    echo ""
    
    if command -v flutter &> /dev/null; then
        echo "✅ Flutter found: $(flutter --version | head -1)"
        echo ""
        echo "Installing dependencies..."
        flutter pub get
        echo ""
        echo "✅ Dependencies installed!"
        echo ""
        echo "Available devices:"
        flutter devices
        echo ""
        echo "To run the app:"
        echo "  flutter run                    # Default device"
        echo "  flutter run -d chrome          # Chrome browser"
        echo "  flutter run -d 'iPhone 15'     # iOS Simulator"
        echo "  flutter run -d emulator-5554   # Android Emulator"
        echo ""
    else
        echo "❌ Neither Docker nor Flutter found."
        echo ""
        echo "Please install one of the following:"
        echo "  1. Docker Desktop: https://www.docker.com/products/docker-desktop"
        echo "  2. Flutter SDK: https://docs.flutter.dev/get-started/install"
        echo ""
        exit 1
    fi
fi

echo "📱 For login, you'll need:"
echo "  - Odoo URL (e.g., ardperfumes.odoo.com)"
echo "  - Username/Email"
echo "  - Password"
echo ""
echo "Happy coding! 🎉"
