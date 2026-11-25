<p align="center">
  <img src="assets/ss.png" alt="Skid Solutions Banner" width="350" />
  <br>
  <sub>all credits to SugarMountainfloor Aka. Showman</sub>
  <br><br>
  <a href="https://discord.gg/skidsolutions">
    <img src="https://img.shields.io/badge/Discord-Skid%20Solutions-18181b?style=for-the-badge&logo=discord&logoColor=ffffff&labelColor=09090b" alt="Discord" />
  </a>
  <img src="https://img.shields.io/badge/Version-10.10--2--56528ba-27272a?style=for-the-badge&labelColor=09090b" alt="Version 10.10-2-56528ba" />
  <img src="https://img.shields.io/badge/Java-8-3f3f46?style=for-the-badge&logo=openjdk&logoColor=ffffff&labelColor=09090b" alt="Java 8" />
  <img src="https://img.shields.io/badge/Gradle-8.14.3-52525b?style=for-the-badge&logo=gradle&logoColor=ffffff&labelColor=09090b" alt="Gradle 8.14.3" />
</p>

<p align="center">
  <a href="https://discord.gg/skidsolutions">https://discord.gg/skidsolutions</a>
</p>

<p align="center">
  <img src="assets/divider.svg" alt="" width="720" />
</p>

<p align="center">
  <img src="assets/jak.jpg" alt="jak" height="150" />
  <img src="assets/hack.jpg" alt="hack" height="150" />
  <img src="assets/deobf2.png" alt="deobf2" height="150" />
  <img src="assets/deobf.jpg" alt="deobf" height="150" />
</p>

<p align="center">
  <img src="assets/divider.svg" alt="" width="720" />
</p>

## How to build

### Prerequisites
- **Java Development Kit**: JDK 8 (compile toolchain) and JDK 17 (Gradle 8.14.3 launcher; newer JDKs such as JDK 25 are not supported by Gradle 8.14.3)

### Building OpenReflex

Run from this folder:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"

.\gradlew.bat prepareIde
.\gradlew.bat clean build
```

Generated `build/libs/`:
- `OpenReflex-10.10-2-56528ba.jar`
- `OpenReflex-10.10-2-56528ba-sources.jar`

### Installation

Copy `build\libs\OpenReflex-10.10-2-56528ba.jar` into your Spigot or Paper server's `plugins/` folder (Minecraft 1.8 to 1.13). Requires ProtocolLib; ViaVersion and PlaceholderAPI are optional.
