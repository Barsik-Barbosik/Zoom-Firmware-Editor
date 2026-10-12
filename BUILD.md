# BUILD

## Requirements

- JDK 8+ to compile (bytecode targets 1.8 on purpose).
- Maven 3.8+.
- No network needed except first Maven dependency fetch (JUnit).

## Build the runnable JAR

```bash
cd Zoom-Firmware-Editor
mvn -q package
java -jar target/ZoomFirmwareEditor-0.99-safety.jar
```

The JAR is self-contained (shade plugin) and a few hundred KB — intentionally
**not** committed to the repo. No `ZoomFirmwareEditor-setup.exe` / embedded-JVM
blob is shipped: Excelsior JET is dead, and a 25MB+ binary does not belong in
git. Build it from source instead (below).

> Note on the odd layout: upstream packages are `main.java.*` with runtime
> resource paths `main/resources/*`. `pom.xml` preserves that (`<directory>src`
> with `main/resources/**`) so `app.config`, `messages.properties` and
> `icons/` resolve exactly as the code expects.

## Windows `.exe` (<25MB policy)

No `.exe` is committed: even a minimal jpackage bundle with a runtime is
~40–80MB, over the 25MB limit, and must be built on Windows anyway.

On a Windows machine with Temurin 11 + WiX:

```bat
mvn package
jpackage --type exe --name ZoomFirmwareEditor --app-version 0.99 ^
  --input target --main-jar ZoomFirmwareEditor-0.99-safety.jar ^
  --main-class main.java.ZoomFirmwareEditor --win-console
```

## macOS `.app` (10.13 High Sierra onwards)

No `.app` is committed either (same size reason + platform-specific).
Build it on a Mac with `./scripts/package-app.sh`:

```bash
./scripts/package-app.sh
open dist/ZoomFirmwareEditor.app
```

How 10.13 compatibility is achieved:

- Bytecode is **1.8** (`maven.compiler source/target 1.8`), so it loads on
  any JRE 8+ present on 10.13 through current macOS.
- `packaging/macos/Info.plist` sets `LSMinimumSystemVersion = 10.13`,
  `NSHighResolutionCapable`, no hardened-entitlement or Metal requirements.
- The script prefers a **system-JRE `.app`** (no bundled runtime, ~1MB) for
  max compatibility. If you bundle a runtime, use Temurin **8 or 11**
  (OpenJDK 17+ needs macOS 10.15+ and breaks the 10.13 promise).

## Tests

```bash
mvn test   # SafetyServiceTest: family blocks, CMN_DRV, GUARD, overflow
```
