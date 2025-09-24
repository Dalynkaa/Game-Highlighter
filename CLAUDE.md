# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Highlighter is a Minecraft Fabric mod for highlighting chat messages and players based on prefixes. It supports multiple Minecraft versions using Stonecutter for version management.

## Build Commands

- `./gradlew build` - Build the mod for the current version
- `./gradlew runClient` - Run the Minecraft client for testing
- `./gradlew chiseledBuild` - Build for all supported Minecraft versions
- `./gradlew publishToModrinth` - Publish to Modrinth (requires token)
- `./gradlew chiseledPublishToModrinth` - Publish all versions to Modrinth

## Architecture

### Multi-version Structure
- Uses Stonecutter to manage multiple Minecraft versions (1.21.1-1.21.8)
- Active version is defined in `stonecutter.gradle.kts` (currently 1.21.1)
- Version-specific properties in `versions/{version}/gradle.properties`
- Run configurations point to `runs/{version}/` directories

### Core Components

**Main Classes:**
- `me.dalynkaa.highlighter.Highlighter` - Main mod initializer
- `me.dalynkaa.highlighter.client.HighlighterClient` - Client-side initialization

**Configuration System:**
- `ModConfig` - Main mod configuration using AutoConfig/Cloth Config
- `StorageManager` - Manages prefix and server data persistence
- `PrefixStorage` & `ServerStorage` - Handle data storage and retrieval
- Migration system in `config/migrations/` for configuration updates

**GUI System:**
- Uses OWO UI library for modern GUI components
- `HighlightScreen` - Main configuration screen
- Custom widgets in `gui/widgets/` for specialized UI elements
- Color picker and dropdown components

**Core Features:**
- Chat message highlighting via `OnChatMessage` listener
- Player highlighting in tab list via `PlayerListMixin`
- Prefix-based configuration with priority system
- Online configuration loading via `BackendConfigurationLoader`

### Key Dependencies
- **Fabric API** - Core Fabric mod functionality
- **OWO UI** - Modern UI library for GUIs
- **Cloth Config** - Configuration screen generation
- **ModMenu** - Mod configuration integration
- **Adventure Platform** - Text component handling

## Development Notes

### Mixin Usage
- Client-side mixins defined in `highlighter.client.mixins.json`
- Main mixins target chat handling and player list rendering
- Uses Lombok for data classes with proper annotation processing

### Testing Environment
- DevAuth integration for development authentication
- Separate run directories per Minecraft version
- Client run configuration pre-configured with development flags

### Storage & Persistence
- JSON-based configuration storage in `.minecraft/config/highlighter/`
- Server-specific prefix configurations
- Migration system handles configuration format changes

### Code Style
- Java 21 target compatibility
- Uses Lombok annotations for boilerplate reduction
- Custom event system for internal communication

1.21.1 - сломано
