# Troubleshooting

This page is meant to explain how to solve common issues when installing or using KAMI. If you experience an issue and it's not listed here, please [open a new issue](https://github.com/zeroeightysix/KAMI/issues/new) and a contributor will help you further.

## Setup
###### Could not find tools.jar
If you encounter this error when building, you most likely don't have the Java Development Kit (JDK) installed.
Head over to [here](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) to download the oracle JDK. Install it and try again.

###### Java error when using OpenJDK
Please downgrade to OpenJDK 8u232, as newer versions don't work. This issue is described [here](https://github.com/SquidDev-CC/CC-Tweaked/issues/402)

###### Minecraft not loading at all
What `.jar` are you using in your `mods` folder? Make sure to use the one that ends with `-full` (`KAMI-MCVER-KAMIVER-full.jar`)

## Crashes in-game

**Please make sure you're on the latest version of forge before proceeding!**
