---
name: Titanium Navy Precision
colors:
  surface: '#0d141e'
  surface-dim: '#091423'
  surface-bright: '#333a45'
  surface-container-lowest: '#070e19'
  surface-container-low: '#151c26'
  surface-container: '#19202b'
  surface-container-high: '#232a35'
  surface-container-highest: '#2e3540'
  on-surface: '#dce3f2'
  on-surface-variant: '#bdc8d1'
  inverse-surface: '#dce3f2'
  inverse-on-surface: '#2a313c'
  outline: '#87929a'
  outline-variant: '#3e484f'
  surface-tint: '#7bd0ff'
  primary: '#8ed5ff'
  on-primary: '#00354a'
  primary-container: '#38bdf8'
  on-primary-container: '#004965'
  inverse-primary: '#00668a'
  secondary: '#b4c5ff'
  on-secondary: '#002a78'
  secondary-container: '#0053db'
  on-secondary-container: '#cdd7ff'
  tertiary: '#54ddfc'
  on-tertiary: '#003640'
  tertiary-container: '#29c1df'
  on-tertiary-container: '#004b58'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#c4e7ff'
  primary-fixed-dim: '#7bd0ff'
  on-primary-fixed: '#001e2c'
  on-primary-fixed-variant: '#004c69'
  secondary-fixed: '#dbe1ff'
  secondary-fixed-dim: '#b4c5ff'
  on-secondary-fixed: '#00174b'
  on-secondary-fixed-variant: '#003ea8'
  tertiary-fixed: '#acedff'
  tertiary-fixed-dim: '#4cd7f6'
  on-tertiary-fixed: '#001f26'
  on-tertiary-fixed-variant: '#004e5c'
  background: '#0d141e'
  on-background: '#dce3f2'
  surface-variant: '#2e3540'
  surface-card: '#0C1625'
  surface-elevated: '#111F33'
  surface-panel: '#172942'
  surface-highlight: '#1F3656'
  primary-metallic: '#60A5FA'
  primary-deep: '#1D4ED8'
  accent-cyan: '#22D3EE'
  shift-day: '#06B6D4'
  shift-night: '#6366F1'
  shift-off: '#334155'
  shift-leave: '#F59E0B'
  text-primary: '#F8FAFC'
  text-secondary: '#94A3B8'
  text-muted: '#64748B'
typography:
  headline-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 32px
    fontWeight: '800'
    lineHeight: 38px
  headline-lg-mobile:
    fontFamily: Plus Jakarta Sans
    fontSize: 26px
    fontWeight: '800'
    lineHeight: 32px
  headline-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 22px
    fontWeight: '700'
    lineHeight: 28px
  title-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 18px
    fontWeight: '700'
    lineHeight: 24px
  title-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '600'
    lineHeight: 22px
  body-lg:
    fontFamily: Inter
    fontSize: 15px
    fontWeight: '600'
    lineHeight: 22px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  body-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
  label-lg:
    fontFamily: JetBrains Mono
    fontSize: 13px
    fontWeight: '600'
    lineHeight: 18px
  label-md:
    fontFamily: JetBrains Mono
    fontSize: 11px
    fontWeight: '600'
    lineHeight: 14px
  label-sm:
    fontFamily: JetBrains Mono
    fontSize: 10px
    fontWeight: '700'
    lineHeight: 12px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1rem
  gutter-sm: 0.75rem
  margin: 1rem
  margin-tablet: 1.5rem
  margin-desktop: 2rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 0.75rem
  space-lg: 1rem
  space-xl: 1.25rem
---

## Brand & Style

This design system delivers an industrial, high-precision mobile tactile aesthetic tailored for shift-duty personnel—including healthcare workers, emergency response forces, tactical operators, and industrial technicians. The experience combines cold-rolled brushed titanium-navy surfaces with high-visibility electroluminescent neon accents to simulate heavy-duty tactical equipment.

The emotional signature is unyielding reliability, instant military-grade legibility under erratic ambient lighting, and mechanical confidence. Interfaces eschew flat paper metaphors in favor of chiseled physical instrument panels: chamfered bevels, specular top-edge rim reflections, and dense tactile telemetry. Interaction states mirror weighted instrument controls, grounding digital shift rotations into palpable, dependable apparatus.

## Colors

The system is architected primarily in an ultra-low-light dark spectrum, mitigating night-shift pupil fatigue while maintaining strict WCAG AAA contrast for field operation. The obsidian-navy base tones (`#070E18` through `#172942`) represent the physical chassis. Functional illumination relies on a multi-tiered blue spectrum: electric sky neon (`#38BDF8`) for live telemetry and focus indicators, reinforced by cobalt (`#2563EB`) and deep mechanical sapphire (`#1D4ED8`).

Shift states utilize distinct tactical codings:
- **Day Shift:** Cyan (`#06B6D4`), rendering daylight clarity through semi-transparent cyan tinting.
- **Night Shift:** Electric Indigo (`#6366F1`), balanced to maintain legibility without glare.
- **Off Duty:** Hard Steel Slate (`#334155`), recessed and low-energy.
- **Leave / Medical / Alert:** High-visibility Amber (`#F59E0B`).

Background surfaces should always use the dark titanium hierarchy (`#070E18` -> `#0C1625` -> `#111F33`) rather than pure monochrome blacks, preserving metallic optical depth across display panels.

## Typography

Typography functions as instrumentation readouts. Three type engines divide operational duties:

1. **Plus Jakarta Sans (Display & Headlines):** Engineered with crisp, geometric geometry and heavy weights (`700` and `800`) to provide high glanceability for core status readouts, countdowns, and section titles.
2. **Inter (Body & Administrative):** Provides hyper-legible, neutral typographic infrastructure for descriptions, legal summaries, shift logs, and settings.
3. **JetBrains Mono (Telemetry, Timers, Shift Badges):** Fixed-width numerical structure ensures that countdown clocks, shift hours, calendar cells, and data columns do not produce horizontal jitter during real-time updates.

Small text labels should default to uppercase styling with slight positive letter spacing (`0.04em`) to maintain clarity against illuminated metal surfaces.

## Layout & Spacing

The layout is built upon an 8px rhythmic foundation with tight 4px precision sub-units, suited for information-dense mobile dashboards. A fluid, screen-constrained grid ensures essential shift metrics remain above the visual fold.

### Breakpoints and Adaptation
- **Mobile Handheld (360px - 480px):** Single-column stack, 16px lateral safe canvas margins, and 12px card gaps. Primary telemetry cards fill full screen width.
- **Tablet & Cockpit Mounts (600px - 840px):** Two-column split-dashboard; calendar matrices shift to a 7-column grid with dedicated right-side inspection panels. 24px margins.
- **Desktop / Terminal (> 1024px):** Fixed-width centered container (`max-width: 1200px`) structured across 12 fluid columns for comprehensive timesheet management and rotation scheduling.

Touch targets across all interactive modules maintain a minimum hardware envelope of 48px × 48px to accommodate glove usage and active field environments.

## Elevation & Depth

Visual hierarchy is constructed through metallic material layering, ambient light simulation, and chiseled edges:

1. **Specular Bevels:** The defining physical trait of this design system is the top-edge specular highlight. Cards, sheets, and interactive keys feature an interior top border highlight (`inset 0 1px 0 rgba(255, 255, 255, 0.25)` to `0.35`), simulating machined brushed metal catching an overhead directional light source.
2. **Surface Stratification:**
   - **Chassis Base (`#070E18`):** Deepest structural canvas.
   - **Trough/Wells (`#091423`):** Recessed states, unselected calendar dates, and input wells.
   - **Plates (`#0C1625`):** Standard resting state for content modules.
   - **Elevated Controls (`#111F33`):** Active interactive triggers and raised floating sheets.
   - **Illuminated Layer:** Accented surfaces featuring neon cyan ambient underglow (`0 0 20px rgba(56, 189, 248, 0.25)`).
3. **Drop Shadows:** Shadows are heavy, dark, and tight (`0 8px 24px -4px rgba(2, 6, 23, 0.75)`), avoiding soft watercolor spreads in favor of dense mechanical separation.

## Shapes

The design system employs controlled geometric curvature. Sharp corners are avoided to prevent visual friction during rapid eye tracking, while overly organic pills are reserved exclusively for status badges and dock elements.

- **Primary Cards & Panels:** 14px border radius (`rounded-md`), striking a balance between structural solidity and ergonomic handholding.
- **Dialogs & Sheet Containers:** 20px to 26px curvature (`rounded-lg` to `rounded-xl`) defining macro modular zones.
- **Shift Badges & Telemetry Pills:** Fully radiused capsules (`9999px`) to visually isolate status indicators from background panel grids.
- **Interactive Tiles & Keys:** 10px to 14px curvature paired with 1.5px metallic outer outlines.

## Components

### Buttons
- **Primary Hardware Button:** Filled with a directional metallic blue gradient (`135deg, #38BDF8 0%, #2563EB 50%, #1D4ED8 100%`). Topped with a 1px specular white rim (`rgba(255, 255, 255, 0.35)`). Typography is 15px bold white, accompanied by an intense localized blue glow on active press.
- **Secondary Steel Button:** Outlined brushed finish (`#111F33` to `#0C1625`), bordered by a 1.5px metallic stroke (`rgba(147, 197, 253, 0.25)`), text rendered in icy blue (`#BAE6FD`).

### Shift Badges & Status Pills
- Compact capsules with semi-transparent tinted backgrounds (16% alpha) and 1px matching solid borders.
- Monospaced typography with accompanying status icon: Day (☀️ `#06B6D4`), Night (🌙 `#6366F1`), Off Duty (☕ `#334155`), and Leave/Alert (🏥 `#F59E0B`).

### Data Cards & Hero Dial
- **Live Duty Tracker (Hero):** Circular instrumentation gauge housed within a `#0C1625` metallic plate. Features an interior radial glow behind an SVG countdown progress ring.
- **Stat Cards:** Compact, two-tone tiles displaying single-glance metrics with muted label keys and high-contrast bold values.

### Calendar Grid Matrix
- **Date Cells:** Chamfered steel blocks with 1px border lines. Unselected states blend into background steel; active "Today" state triggers a neon cyan outline with subtle outer glow. Shift markers render as miniature rounded color bars pinned to the bottom of the date cell.

### Floating Dock Navigation
- Suspended 16px above the device navigation bar.
- Background uses frosted titanium navy (`rgba(12, 22, 37, 0.88)` with `20px` background blur) and a crisp 1px perimeter border.
- Active navigation item reveals an electroluminescent blue background pill with icon illumination.

### Inputs & Toggles
- **Input Fields:** Recessed well styling (`#091423`) with a crisp inset drop-shadow, transitioning to a `#38BDF8` border when focused.
- **Toggles:** Heavy tactile switches with metallic thumb handles that lock with crisp neon illumination when engaged.