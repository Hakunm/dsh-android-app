# DeepSeek Harness Android Design System

This file is the persistent visual source of truth for `dsh-android-app`.

## Direction

- Product: mobile AI coding companion and authorized-root file workspace for DeepSeek Harness.
- Style: calm 90%+ negative space, borderless top chrome, grouped rounded cards, monochrome outline iconography, and Dual-Mode automatic system theme (`ThemeMode.SYSTEM`).
- Density: crisp `390dp`-normalized mobile proportion (`14sp` chat prose, `15sp` setting rows, `46dp` top bar, `48dp` floating capsule composer).

## Color Tokens

| Role | Light (Crisp Porcelain · 清透冰瓷) | Dark (Quiet Night Owl · 静谧夜枭) |
| --- | --- | --- |
| Primary canvas | `#F6F8FB` | `#171715` |
| Grouped settings / sheet canvas | `#EEF2F7` / `#EFF3F8` | `#141412` / `#1E1E1C` |
| Navigation drawer surface | `#F5F8FC` | `#1B1B19` |
| Grouped card surface | `#FFFFFF` | `#232320` |
| Pill / user bubble surface | `#E8EEF6` | `#262623` |
| Primary text | `#0F172A` | `#F2F0EA` |
| Secondary text | `#475569` | `#B5B1A8` |
| Primary pill CTA | `#0F172A` (fg `#FFFFFF`) | `#F2F0EA` (fg `#141413`) |
| Brand whale & accent | `#2563EB` | `#E8E5DC` (whale) / `#4C8BF5` (accent) |
| Hairline divider | `#E2E8F0` | `#2F2F2B` |

## Layout & Navigation

- Mobile navigation uses a borderless left `ModalNavigationDrawer` (`86%` width, `22dp` right corners) with DeepSeek wordmark, 4 primary destinations (`Chats / Workspaces / Code / Connection & Settings`), full-text session search, single-line recent sessions, and a pinned bottom `+ New chat` pill CTA.
- Wide layouts (`>= 840dp`) render a persistent `284dp` navigation rail alongside the active workspace pane.
- Conversation assistant Markdown renders directly on the calm canvas without card boxes; reasoning/context blocks use single-line collapsible headers with a `2dp` vertical rule when expanded; tool calls render as compact pills.
- Secondary input controls (camera, gallery photos, workspace files, workspace target, model/thinking effort, sandbox access mode) live inside the `+` `ModalBottomSheet` (`28dp` top corners).
- Grouped settings, workspaces, and file lists use `18dp` rounded cards with `18–19dp` monochrome outline icons.
