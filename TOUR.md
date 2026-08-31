# ModuleChooser: how it runs

A walkthrough of the CTEC2710 Course and Module Selection Tool — what happens, in what
order, and which Java/JavaFX mechanism is doing the work at each step.

**Running it:** `./run.sh` from the project root. Java 25 no longer bundles JavaFX (it was
split out at Java 11), so the script fetches `javafx-base`, `javafx-graphics` and
`javafx-controls` from Maven Central into `.fx/` on first run, compiles all 13 sources into
`CTEC2710Assignment/ModuleChooser/bin/`, and launches. The two flags that make it work:

- `--module-path .fx` — where the JavaFX modules live
- `--add-modules javafx.controls` — pull them into the module graph, since this app has no
  `module-info.java`

---

## 1. Boot sequence

`main()` calls `launch(args)` (`main/ApplicationLoader.java:36`). That one call hands control
to the JavaFX runtime, which then calls **your** code back at two points:

```
launch(args)
   │
   ├─► init()          on the launcher thread — no UI exists yet
   │     model = new StudentProfile()          ← empty data holder
   │     view  = new ModuleChooserRootPane()   ← builds the entire widget tree
   │     new ModuleChooserController(model, view)   ← wires them together
   │
   └─► start(stage)    on the FX Application Thread — the only thread allowed to touch UI
         Scene scene = new Scene(view, 800, 600)
         scene.getStylesheets().add(academic.css)
         stage.setTitle(...); stage.setScene(scene); stage.show()
```

Two things worth re-anchoring:

- **`Stage` is the OS window. `Scene` is the contents.** One `Scene` wraps one root node —
  here `view`, the `ModuleChooserRootPane`.
- The controller constructor result is **discarded** (`ApplicationLoader.java:20`). That's
  intentional: the controller does all its work as a side effect in its constructor,
  attaching itself to the view's buttons. Nobody needs to hold it afterwards.

## 2. The widget tree

`ModuleChooserRootPane extends BorderPane` — a layout with five slots
(top/bottom/left/right/center). It uses two:

```
BorderPane
├── top    → ModuleChooserMenuBar        File(Load, Save, ─, Exit)  Help(About)
└── center → TabPane  (TabClosingPolicy.UNAVAILABLE — tabs can't be closed)
              ├── [0] "Create Profile"     CreateStudentProfilePane  extends GridPane
              ├── [1] "Select Modules"     SelectModulesPane         extends VBox
              ├── [2] "Reserve Modules"    ReserveModulesPane        extends VBox
              └── [3] "Overview Selection" OverviewSelectionPane     extends VBox
```

Each pane **extends** its layout rather than containing one. So `CreateStudentProfilePane`
*is a* `GridPane` — that's why its constructor calls `this.add(lblPnumber, 0, 1)` directly
(`col, row`).

`changeTab(int)` (`view/ModuleChooserRootPane.java:61`) is how the controller drives the user
forward: `tp.getSelectionModel().select(index)`.

## 3. How view and controller talk

This is the pattern the whole app is built on. The view **never** knows what a button does.
It only offers a socket:

```java
// view/SelectModulesPane.java:120
public void setSubmitButtonHandler(EventHandler<ActionEvent> handler) {
    btnSubmit.setOnAction(handler);
}
```

```java
// controller/ModuleChooserController.java:174
view.getSelectModulesPane().setSubmitButtonHandler(event -> {
    ...                        // ↑ this lambda IS an EventHandler<ActionEvent>
});
```

`EventHandler<ActionEvent>` has exactly one abstract method, `handle(ActionEvent)`. That makes
it a **functional interface**, which is why a lambda can stand in for it — the compiler
expands `event -> {...}` into an implementation of `handle`. The lambda closes over `model`
and `view` (the controller's fields), so it can reach both when it fires later.

The controller constructor (`:44-54`) is just eleven of these in a row:

```java
this.attachCreateStudentProfileHandler();
this.attachResetHandler();
this.attachAddHandlers();
this.attachRemoveHandlers();
this.attachSubmitHandler();
this.attachConfirmHandler();
this.attachSaveOverviewHandler();
this.attachSaveHandler();
this.attachLoadHandler();
this.attachAboutHandler();
this.attachExitHandler();
```

By the time `start()` shows the window, every button already has its behavior bolted on.

## 4. The data that exists before the user does anything

`generateAndGetCourses()` (`:462`) hardcodes nine `Module` objects and two `Course` objects.
Every module is 30 credits.

```
Module(code, name, credits, mandatory, runPlan)

              BLOCK_1      BLOCK_2      BLOCK_3_4
Comp Sci      CTEC3701     CTEC3702     CTEC3451*, CTEC3704, CTEC3705, IMAT3711, IMAT3722
Soft Eng      CTEC3701     CTEC3703     CTEC3451*, CTEC3704, CTEC3705, CTEC3706
                                          * mandatory
```

Note the same `Module` **objects** are shared between both courses — `ctec3701` is added to
`compSci` and `softEng`. Java objects are handled by reference, so those are one instance in
two maps, not copies.

The controller pushes these into the combo box immediately (`:42`), before any handler is
attached.

**Why the credit arithmetic works out:** Block 1 (30) + Block 2 (30) = 60 automatic. The
target is 120. Every Block 3/4 module is 30. So the user must move exactly **two** modules
into the selected list. Everything left over becomes the reserve pool.

## 5. Tab 1 — Create Profile

`CreateStudentProfilePane` is a two-column `GridPane`: right-aligned labels in column 0,
inputs in column 1.

```
Select course:   [ ComboBox<Course>  ▾ ]
Input P number:  [ TextField ]
Input first name:[ TextField ]
Input surname:   [ TextField ]
Input email:     [ TextField ]
Input date:      [ DatePicker ]
                 [ Create Profile ]
```

The combo box holds `Course` **objects**, not strings — `ComboBox<Course>`. It displays
"Computer Science" because `Course.toString()` returns `courseName` (`model/Course.java:42`).
JavaFX calls `toString()` on items when there's no custom cell factory. The same trick makes
the module lists readable: `Module.toString()` returns
`"CTEC3701 : Software Development..."` (`model/Module.java:70`), while the *real* debug
representation lives in a separate `actualToString()`.

`Create Profile` (`:59`) runs four guard clauses, each with an early `return`:

| Check  | Rule                                                                    |
| ------ | ----------------------------------------------------------------------- |
| `:66`  | P number non-empty and starts with `p`/`P`                              |
| `:75`  | Both names match `^[A-Z][a-zA-Z-]*$` — capital first letter, then letters/hyphens |
| `:85`  | Email contains `@`                                                      |
| `:94`  | A date was picked (`DatePicker.getValue()` returns `null` if untouched)  |

Any failure pops an `Alert(AlertType.ERROR)` and `showAndWait()` — modal, blocks until
dismissed — then bails without touching the model.

If all four pass, it commits (`:104-116`): writes the five fields into `StudentProfile`, wipes
both module sets, fills tab 2, refreshes tab 4, recomputes credits, clears tab 3's lists, and
jumps to `changeTab(1)`.

**Note the ordering:** `getStudentName()` (`view/CreateStudentProfilePane.java:91`) constructs
a *fresh* `new Name(first, surname)` on every call. The pane doesn't hold a `Name` — it holds
two `TextField`s and assembles one on demand.

## 6. Tab 2 — Select Modules

`SelectModulesPane` is a `VBox` stacking three things: a `GridPane`, a credits row, then a
button row.

```
┌───────────────────────┬────────────────────────────┐
│ Selected Block 1      │ Unselected Block 3/4       │
│ ┌───────────────────┐ │ ┌────────────────────────┐ │
│ │ CTEC3701 : ...    │ │ │ CTEC3451 : ...         │ │
│ └───────────────────┘ │ │ CTEC3704 : ...         │ │
│ Selected Block 2      │ │ CTEC3705 : ...         │ │
│ ┌───────────────────┐ │ └────────────────────────┘ │
│ │ CTEC3702 : ...    │ │       [Add]  [Remove]      │
│ └───────────────────┘ │ Selected Block 3/4         │
│                       │ ┌────────────────────────┐ │
│                       │ └────────────────────────┘ │
└───────────────────────┴────────────────────────────┘
              Current credits: [ 60 ]
                 [Reset]  [Submit]
```

The left column comes from the grid directly; the right column is a nested `VBox` spanning 4
rows (`mainGridPane.add(secondColumnVBox, 1, 0, 1, 4)` — the last two args are
colspan/rowspan).

**`populateSelectModulesPane(course)`** (`:340`) is the router. It clears all four lists, then
walks the course's modules and sorts them by enum:

```java
for (Module module : selectedCourse.getAllModulesOnCourse()) {
    if      (module.getRunPlan() == Block.BLOCK_1)   → block1Modules
    else if (module.getRunPlan() == Block.BLOCK_2)   → block2Modules
    else if (module.getRunPlan() == Block.BLOCK_3_4) → block3_4Unselected
}
```

Enum constants are singletons, so `==` is the correct comparison here. Blocks 1 and 2 land
pre-filled and there's no UI to change them — they're mandatory by construction. Only Block
3/4 is interactive.

**Add / Remove** (`:134` and `:154`) are mirror images. Pull the highlighted item out of the
selection model, remove it from one `ListView`, add it to the other, recompute credits:

```java
Module selectedModule = ...getBlock3_4ModulesUnselected().getSelectionModel().getSelectedItem();
if (selectedModule != null) {                    // null = nothing highlighted
    ...Unselected().getItems().remove(selectedModule);
    ...Selected().getItems().add(selectedModule);
    updateCurrentCredits();
}
```

`getItems()` returns an `ObservableList` — the live backing list. Mutating it repaints the
`ListView` automatically. That's the whole reason there's no "refresh" call anywhere.

**`updateCurrentCredits()`** (`:450`) sums three streams and writes the total into the
read-only text field:

```java
block3_4Selected + block1 + block2, each  .stream().mapToInt(e -> e.getModuleCredits()).sum()
```

**Reset** (`:123`) clears the two Block 3/4 lists and re-runs `populateSelectModulesPane` on
the current course — back to square one.

**Submit** (`:174`) is the gate. It reads the credit total, and if it isn't exactly 120 it
alerts and stops. On success it:

1. `model.clearSelectedModules()` — wipe, don't merge
2. Pours all three "selected" lists into the model via `forEach(model::addSelectedModule)` — a
   **method reference**, shorthand for `m -> model.addSelectedModule(m)`
3. Clears tab 3's two lists, then copies the *leftover* Block 3/4 unselected list into tab 3's
   left list — this is the handoff between tabs
4. Refreshes the overview, `changeTab(2)`

## 7. Tab 3 — Reserve Modules

Two lists side by side, three buttons. Same add/remove shuttle as before (`:143` and `:163`),
but no credit tracking — reserves don't count toward 120.

**Confirm** (`:209`) enforces exactly one:

```java
if (reservedModulesListView.getItems().size() != 1)  → error alert
else  → model.clearReservedModules(); copy in; refresh overview; changeTab(3)
```

## 8. Tab 4 — Overview

Three non-editable `TextArea`s and a save button. All the content is produced by
**`populateOverviewPane()`** (`:359`), which is called from four different places — after
profile creation, after submit, after confirm, and after load — so it always reflects the
current model.

It builds three blocks of text: profile details via string concatenation, then selected and
reserved modules via `StringBuilder` in a loop:

```java
StringBuilder selectedModulesText = new StringBuilder("Selected modules:\n==========\n");
for (Module module : model.getAllSelectedModules()) {
    selectedModulesText.append("Module code: ").append(module.getModuleCode())
                       .append(", Module name: ").append(module.getModuleName())
                       ...
}
```

`StringBuilder` mutates in place, so appending in a loop doesn't allocate a new `String` each
iteration. Each `.append()` returns the same builder, which is what lets them chain.

**Save Overview** (`:229`) opens a `FileChooser`, glues the three text areas together with
blank lines between, and writes plain text with a `PrintWriter` in a **try-with-resources**
block:

```java
try (PrintWriter out = new PrintWriter(file)) { ... }
```

The resource declared in the parentheses is closed automatically when the block exits, success
or exception.

## 9. Save / Load — the menu bar path

`ModuleChooserMenuBar` builds File and Help menus with keyboard accelerators via
`KeyCombination.keyCombination("SHORTCUT+S")`. `SHORTCUT` resolves to ⌘ on macOS and Ctrl
elsewhere — that's why it's spelled that way instead of `META`.

**Save** (`:262`) serializes the entire object graph in one line:

```java
try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
    oos.writeObject(model);
}
```

That single `writeObject` walks the whole graph — `StudentProfile` → `Name`, `Course`, and
every `Module` in both `TreeSet`s — and writes it all as binary. This works because every one
of those classes implements `Serializable`, and each declares
`private static final long serialVersionUID = 1L`, the version stamp that has to match on the
way back in.

**Load** (`:292`) reverses it, casts the result, and replaces the controller's model
reference:

```java
model = (StudentProfile) ois.readObject();
populateViewWithProfileData();
```

`readObject()` returns `Object`, so the cast is required, and `ClassNotFoundException` gets
caught alongside `IOException` in a **multi-catch** (`:309`).

**`populateViewWithProfileData()`** (`:392`) is the inverse of everything above — it rebuilds
the UI from the model:

1. Refills the four form fields and reselects the course in the combo box
2. Splits `getAllSelectedModules()` three ways by block, using streams:
   ```java
   model.getAllSelectedModules().stream()
        .filter(module -> module.getRunPlan() == Block.BLOCK_1)
        .toList()
   ```
   and pushes each into its list with `setAll(...)`
3. Reconstructs the *unselected* Block 3/4 list by subtraction — every Block 3/4 module on the
   course that isn't in the selected set (`:419`)
4. Re-syncs the model from those lists, restores the reserve lists, recomputes total credits,
   and calls `populateOverviewPane()`

**About** (`:322`) shows an info alert. **Exit** (`:335`) is `e -> System.exit(0)`.

## 10. The model, and why it's shaped that way

```
StudentProfile
├── String    studentPnumber
├── Name      studentName        ── firstName, familyName, getFullName()
├── String    studentEmail
├── LocalDate studentDate
├── Course    studentCourse      ── courseName + HashMap<String code, Module>
├── Set<Module> selectedModules   ← TreeSet
└── Set<Module> reservedModules   ← TreeSet
```

The one piece worth understanding properly: **`TreeSet` is why
`Module implements Comparable<Module>`.**

A `TreeSet` keeps elements sorted, which means it needs to know how to order any two elements.
It gets that from `compareTo` (`model/Module.java:79`), which cascades through five
tie-breakers — code, then credits, then mandatory flag, then name, then block — returning the
first non-zero result:

```java
int result = this.moduleCode.compareTo(other.moduleCode);
if (result == 0) {
    result = Integer.compare(this.moduleCredits, other.moduleCredits);
    if (result == 0) { ... }
}
return result;
```

Two consequences fall out of this:

- The overview always lists modules in code order, without anything sorting them explicitly.
  The `TreeSet` did it on insertion.
- Duplicates are impossible. `addSelectedModule` returns `boolean` — `false` means the set
  already had it. A `TreeSet` decides "already had it" by `compareTo(...) == 0`, not by
  `equals`.

`Module` also implements `equals` and `hashCode` (`:103`, `:111`) — those are what
`ObservableList.remove()`, `removeAll()`, and `contains()` use in the view layer.

---

## The one-paragraph version

`launch()` → `init()` builds an empty `StudentProfile`, the full widget tree, and a controller
that wires eleven lambdas onto the view's buttons → `start()` puts it on screen. The user
fills the form; validation passes; the model gets populated and the course's modules get
routed into three `ListView`s by their `Block` enum. Add/Remove shuttle modules between two
observable lists while a stream re-sums the credits after every move. Submit gates on exactly
120, dumps the lists into the model's `TreeSet`, and hands the leftovers to tab 3. Confirm
gates on exactly one reserve. Every gate that passes calls `populateOverviewPane()`, which
rebuilds tab 4's text from the model. Save writes the whole object graph in one `writeObject`;
Load reads it back and runs the entire pipeline in reverse.

The invariant holding it together: **the model is the truth, the views are dumb sockets, and
the controller is the only thing that knows both.**

---

## Appendix: file map

```
CTEC2710Assignment/ModuleChooser/src/
├── main/ApplicationLoader.java              39 lines   entry point, Application lifecycle
├── model/
│   ├── Block.java                            5 lines   enum BLOCK_1, BLOCK_2, BLOCK_3_4
│   ├── Name.java                            52 lines   firstName + familyName
│   ├── Module.java                         115 lines   Comparable + equals/hashCode
│   ├── Course.java                          56 lines   name + HashMap<code, Module>
│   └── StudentProfile.java                 104 lines   aggregate root, Serializable
├── view/
│   ├── ModuleChooserRootPane.java           64 lines   BorderPane + TabPane
│   ├── ModuleChooserMenuBar.java            63 lines   File / Help menus
│   ├── CreateStudentProfilePane.java       129 lines   tab 0
│   ├── SelectModulesPane.java              123 lines   tab 1
│   ├── ReserveModulesPane.java              83 lines   tab 2
│   └── OverviewSelectionPane.java           62 lines   tab 3
├── controller/ModuleChooserController.java 503 lines   all eleven handlers + data
└── academic.css                             83 lines   stylesheet
```
