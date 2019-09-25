# KAMI
[![Build Status](https://travis-ci.com/zeroeightysix/KAMI.svg?branch=master)](https://travis-ci.com/zeroeightysix/KAMI)
[![Issues](https://img.shields.io/github/issues/zeroeightysix/kami.svg)](https://github.com/zeroeightysix/kami/issues)
[![Discord](https://img.shields.io/badge/chat-on%20discord-brightgreen.svg)](http://discord.gg/9hvwgeg)

A minecraft utility mod for anarchy servers.

This is by no means a finished project and is still in rapid development.

## Preview

<details>
 <summary>Click to view images</summary>

 ![GUI](.github/IMAGES/gui.png)
 
 ![CrystalAura](.github/IMAGES/crystalAura.png)

</details>

## Installing

KAMI is a forge mod. Start by downloading the latest version of [1.12.2 forge](https://files.minecraftforge.net/).
1. Install forge
2. Navigate to your `.minecraft` directory.
   * **Windows**: `%appdata%/.minecraft`
   * **Linux**: `~/.minecraft`
3. Navigate to the `mods` directory. If it doesn't exist, create it.
4. Obtain the KAMI `.jar` file.
   * By **downloading** it: see [releases](../../releases)
   * By **building** it: see [building](#building).
5. Place the `.jar` file in your mods directory.

## How do I

##### Open the GUI
Press Y.

##### Use commands
The default prefix is `.`. Commands are used through chat, use `.commands` for a list of commands.

##### Bind modules
Run `.bind <module> <key>`.

##### Change command prefix
By using the command `prefix <prefix>` or after having ran KAMI (make sure it's closed), editing your configuration file (find it using `config path` in-game) and changing the value of `commandPrefix` to change the prefix.

## Troubleshooting
Please reference the main [troubleshooting page](docs/TROUBLESHOOTING.md)

If you experience an issue and it's not listed there, please [open a new issue](../../issues/new) and a contributor will help you further.

## Contributing

You are free to clone, modify KAMI and make pull requests as you wish. To set up your development environment, make use of the following commands:

```
git clone https://github.com/zeroeightysix/KAMI/
cd KAMI
```

On GNU/Linux, run `chmod +x gradlew` and for the following commands use `./gradlew` instead of `gradlew.bat`

Of-course you can also use a Gradle installation if you for some reason want another version of gradle

```
gradlew.bat setupDecompWorkspace
```
Import KAMI into your IDE of choice. If you use IntelliJ, import from the `build.gradle` file and run `gradlew.bat genIntellijRuns`

If you do not wish to run from an IDE, use `gradlew.bat runClient` to run KAMI.

### Building
#### Windows
You can build by running `autobuild.bat` and then `autocopy.bat`, or if you prefer the manual way, do the following:

```
gradlew.bat build
cd build/libs
```
To copy the release  jar in `build/libs` you can run `autocopy.bat` or, if you prefer doing it manually, find a file `KAMI-<minecraftVersion>-<kamiVersion>-release.jar` which you can copy to the `mods` folder of a minecraft instance that has forge installed.
#### Linux
```
git clone https://github.com/zeroeightysix/KAMI.git
cd KAMI
chmod +x auto*
./autobuild
mkdir ~/.minecraft/mods
./autocopy
```
Note: This assumes your minecraft folder is in the default location under your home folder.

## Thank you
[ZeroMemes](https://github.com/ZeroMemes) for [Alpine](https://github.com/ZeroMemes/Alpine)

[ronmamo](https://github.com/ronmamo/) for [Reflections](https://github.com/ronmamo/reflections)

The [minecraft forge team](https://github.com/MinecraftForge) for [forge](https://files.minecraftforge.net/)
