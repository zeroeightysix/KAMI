# KAMI
[![Build](https://github.com/zeroeightysix/KAMI/workflows/Fabric%20mod%20jar%20build/badge.svg)](https://github.com/zeroeightysix/KAMI/actions)
[![Issues](https://img.shields.io/github/issues/zeroeightysix/kami.svg)](https://github.com/zeroeightysix/kami/issues)
[![Discord](https://img.shields.io/badge/chat-on%20discord-brightgreen.svg)](http://discord.gg/9hvwgeg)

## A Minecraft utility mod for anarchy servers.

The Fabric 1.16.3 version is in active development.

## Preview

<details>
 <summary>Click to view images</summary>

 ![GUI](.github/IMAGES/gui.png)

 ![CrystalAura](.github/IMAGES/crystalAura.png)

</details>

## Installing

KAMI is a fabric mod. Download the [latest release](#downloading), or preferably, [**build it yourself**](#building). 

Download and run the installer for Fabric from [here](https://fabricmc.net/use/) if you do not have it already. KAMI does not require Fabric API.

Move the KAMI jar file to the `mods` folder in your `.minecraft` directory.

## Usage

While ingame, press <kbd>Y</kbd> to open the GUI. The wizard will help you further. He doesn't bite.

The command prefix is `.` by default. The autocompletion will list what commands are available.

## Troubleshooting
If you encounter bugs or crashes, please [open a new issue](../../issues/new/choose).

To ask for help, join the [Discord](http://discord.gg/9hvwgeg).

## Downloading

Note: You need to be logged into GitHub to do this. If you don't want to login, [build](#building) your own jar. 

1. Go to the [Actions](https://github.com/zeroeightysix/KAMI/actions) tab
2. Click on the latest action
3. Click on "KAMI mod jar"
4. Extract downloaded zip file
5. [Install](#installing)

<details>
 <summary>Images</summary>

 ![downloading](.github/IMAGES/downloading.png)

</details>

## Setting up

### IntelliJ
1. In Intellij, navigate to `File > New > Project from Version Control...`
2. Paste `https://github.com/zeroeightysix/KAMI/` in the `URL` field, and hit **Clone**.
3. Allow gradle to set up the project. This might take a while. *(~ 8 minutes)*

###### Optionally,
* To generate the run configurations, run the `idea` gradle task.
* To generate attachable sources, run the `genSources` task and attach the generated sources in your IDE.

## Building

1. Download or clone the KAMI repository.
2. Run `gradle build`
3. The built jar is in `build/libs/`. There are 2 jars, pick the in the format of `KAMI-version.jar`, not `KAMI-version-shadow.jar`.


## Thank you
[ZeroMemes](https://github.com/ZeroMemes) for [Alpine](https://github.com/ZeroMemes/Alpine)

[ronmamo](https://github.com/ronmamo/) for [Reflections](https://github.com/ronmamo/reflections)

The [Minecraft Forge team](https://github.com/MinecraftForge) for [Forge](https://files.minecraftforge.net/)

The [Fabric Team](https://github.com/FabricMC) for [Fabric](https://github.com/FabricMC/fabric-loader) and [Yarn](https://github.com/FabricMC/yarn)
