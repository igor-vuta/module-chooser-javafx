<div align="center">

# 🧩 ModuleChooser

**A JavaFX desktop app for building a student's module selection — strict MVC, no build tool, one script to run.**

[![Build](https://github.com/igor-vuta/module-chooser-javafx/actions/workflows/build.yml/badge.svg)](https://github.com/igor-vuta/module-chooser-javafx/actions/workflows/build.yml)

*Java · JavaFX · MVC · plain `javac`*

</div>

---

## ✨ What it does

A course-and-module selection tool: create a student profile, pick a course, select and reserve term modules against credit limits, review an overview, and save/load the whole profile to disk via Java object serialization.

- 👤 **Profile tab** — student details with validated input
- ✅ **Selection tabs** — mandatory modules pre-selected; add, remove and reserve optional modules with live credit tracking
- 📋 **Overview tab** — full summary of the final selection
- 💾 **File menu** — save/load the profile (`Serializable` aggregate), styled with an external JavaFX stylesheet

## 🚀 Quick start

```bash
./run.sh
```

That's the whole build system. The script fetches the three JavaFX modules for your OS/arch from Maven Central into `.fx/` (cached after the first run), compiles the 13 sources with plain `javac`, and launches. Tested on macOS (Apple Silicon) and Linux; `FX_VERSION=17.0.13 ./run.sh` pins an older JavaFX for JDK 17. CI compiles every push with `./run.sh --compile-only`.

## 🏛️ Architecture

Strict model–view–controller with constructor wiring and zero framework magic:

```
src/
├── main/ApplicationLoader.java      boots JavaFX, wires M+V+C
├── model/                           Course, Module, Block, Name, StudentProfile (aggregate root)
├── view/                            RootPane (TabPane) + one pane per tab + menu bar
├── controller/ModuleChooserController.java   all event handlers + data population
└── academic.css                     external stylesheet, loaded as a classpath resource
```

The model layer is pure Java (no JavaFX imports); views expose typed attach-handler methods; the controller binds them with lambdas and populates data through stream pipelines over `ObservableList`s.

**📖 Want the deep dive?** [`TOUR.md`](TOUR.md) walks the entire runtime path — boot sequence, thread model, event flow for every handler, and the serialization round-trip, with file:line references.

## 📦 Provenance

BSc Computer Science coursework (object-oriented design and development, Level 5), built solo.

## 👤 Author

**Igor Vuta** — [github.com/igor-vuta](https://github.com/igor-vuta) · [portfolio](https://igor-vuta.github.io/portfolio/)
