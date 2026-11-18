# WhereWasI

JetBrains plugin for sending code references to the active built-in Terminal tab.

Select code → `⌘⌥K` / `Ctrl+Alt+K` → a file reference with line numbers is pasted into the active terminal.

## What it does

1. Takes the current file and selected line range from the editor
2. Formats a reference, for example `@src/UserService.kt#L42-58`
3. Pastes it into the currently selected Terminal tab using bracketed paste
4. Brings the Terminal tool window forward without submitting the reference

**Example terminal input:**
```
@src/UserService.kt#L42-58
```

Files and folders can also be selected from the Project view. File references are project-relative when possible; line references are included when code is selected in the editor.

## Build

**Requirements:** internet connection (to download Gradle, the JDK 25 toolchain and the IntelliJ IDEA SDK)

```bash
# Build the plugin
./gradlew buildPlugin

# The plugin will be located at:
# build/distributions/where-was-i-plugin-1.1.0.zip
```

> The first build may take 5–10 minutes — it downloads the IntelliJ IDEA SDK (over 1 GB)

## Compatibility

Works with JetBrains IDEs version 2026.2 or newer that include the **Terminal** plugin:

- IntelliJ IDEA (Community & Ultimate)
- WebStorm
- PyCharm (Community & Professional)
- PhpStorm
- GoLand
- RubyMine
- CLion
- Rider
- RustRover
- DataGrip
- Android Studio

## Remote Development (Gateway / JetBrains Client)

The plugin is split into content modules so it works when the IDE runs remotely, for example a Windows JetBrains Client connected through Gateway to an IntelliJ IDEA backend on Linux:

| Module | Loads in | Contains |
|---|---|---|
| `shared` | both | `TerminalPasteApi`, an `@Rpc` interface: the client subscribes to the texts to paste |
| `backend` | remote IDE | the **Send to Terminal** action (editor selection, project-relative paths) and the `TerminalPasteApi` implementation |
| `frontend` | JetBrains Client | pastes into the active `TerminalView` (the Terminal UI only exists on the client) |

A regular local IDE loads all three modules in one process, so it behaves as before.

The RPC uses only public platform API, so the Plugin Verifier reports no internal API usage on 2026.2.x. The `rpc` compiler plugin has to match the Kotlin compiler version exactly, which is why the build uses Kotlin `2.4.0-RC` (2026.2 itself is built with Kotlin 2.4.0). Bump both together.

Install the plugin on the remote IDE (backend). The frontend module must also be present in the client. If **Send to Terminal** does nothing in the client, install the same zip in JetBrains Client too.

To try split mode locally:

```bash
./gradlew runIde -PsplitMode=true
```

## Installation in JetBrains IDEs

1. Open your JetBrains IDE → **Settings** → **Plugins**
2. Gear icon (⚙️) → **Install Plugin from Disk...**
3. Select `build/distributions/where-was-i-plugin-1.1.0.zip`
4. Restart the IDE

## Usage

- Select code → right-click → **Send to Terminal**
- Or use the hotkey: `⌘⌥K` (macOS) / `Ctrl+Alt+K` (Windows/Linux)
- Files and folders: select them in the **Project** view (`⌘1` / `Alt+1`) → right-click → **Send to Terminal** (or the same hotkey).
  Each one is pasted as `@src/Main.kt` or `@src/checkout/` (paths relative to the project).

## Changing the hotkey

**Settings** → **Keymap** → search for `Send to Terminal` → assign your own.

## Troubleshooting

**"No active Terminal tab found" notification?**
Open a terminal tab in the built-in Terminal tool window, then try again.
