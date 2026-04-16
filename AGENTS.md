# Yakak Project Specification & Design Manifesto

This document serves as the primary reference for the Yakak project, ensuring consistency in architecture, functionality, and its unique visual identity.

## 1. Project Identity
**Yakak** is a "Swiss-Army-Knife" productivity application for Android. It aims to provide a seamless, integrated experience for managing tasks, schedules, and daily utilities through a highly interactive and visually daring interface.

## 2. Technical Stack
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (Modern, declarative UI).
- **Material Design:** Strict adherence to **Material 3 Expressive** (M3).
- **Architecture:** MVVM (Model-View-ViewModel) with Unidirectional Data Flow (UDF).
- **Dependency Injection:** Hilt.
- **Persistence:** Room (SQL based).
- **Navigation:** Compose Navigation using a `HorizontalPager` for primary feature switching.

## 3. Design Manifesto: "Expressive Bold"
Yakak is a laboratory for **daring and assumed design**. It must never feel "standard" or "generic."

### 3.1 Material 3 Expressive Guidelines
- **Modern Components:** Prioritize new M3 components (e.g., `HorizontalFloatingToolbar`, `FloatingActionButtonMenu`, `SegmentedButton`).
- **Motion Identity:** Use `MotionScheme.expressive()` globally. Transitions must be fluid, using Spring-based specs for a "bouncy" and organic feel.
- **Expressive Loading:** Use the new `LoadingIndicator` and `DeterministicDotLoadingIndicator` for all asynchronous states.

### 3.2 Graphical Identity (The "Bold" Path)
- **Bold Typography:** Leverage "Display" and "Headline" scales. Don't be afraid of oversized titles to create strong visual hierarchy.
- **Morphing & Shapes:** Components should feel alive. Use `Modifier.graphicsLayer` and `Animatable` to morph shapes (e.g., a card expanding into a full-screen view with a shared element feel).
- **Assumed Layouts:** Experiment with asymmetric grids and organic spacing. The UI should feel like a custom-crafted interface, not a template.
- **Vibrant Color Palette:** While supporting Dynamic Color, define strong accent colors that give Yakak its own "graphical leg." Use gradients and blurs (Glassmorphism where appropriate) to add depth.

## 4. Core Features
- **Agenda/Calendar:** Custom grid implementation with interactive day nodes and event indicators.
- **Task Management:** Advanced list interactions including "Swipe-to-Action" and "Pull-to-Reveal" for secondary views (like completed tasks).
- **Maps:** Integrated location services via `osmdroid`.
- **Secret Utility:** Hidden features (like the experimental Calculator) serve as testing grounds for even bolder UI experiments.

## 5. Coding Standards & AI Guidelines
- **Strict Typing:** Use sealed classes/interfaces for UI states and navigation events.
- **Component Driven:** Wrap standard M3 components in project-specific "Yakak" design tokens to ensure global style changes are easy.
- **Haptic Feedback:** Integrate `LocalHapticFeedback` for every significant user interaction (clicks, long presses, swipes).
- **State Lifecycle:** Always use `collectAsStateWithLifecycle()` to ensure resource efficiency.

---
*Created for the Yakak Development Team.*
