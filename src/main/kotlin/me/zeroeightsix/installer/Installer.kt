package me.zeroeightsix.installer

import org.xml.sax.SAXException
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.event.ActionEvent
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.Arrays
import java.util.Locale
import java.util.Objects
import java.util.Random
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

fun main() {
    val kamiLogo = Installer::class.java.getResourceAsStream("/assets/kami/installer/pixel_kami_logo.png")
    val frame = JFrame("KAMI Installer")
    frame.iconImage = ImageIO.read(kamiLogo)
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.contentPane.add(Installer())
    frame.pack()
    frame.isResizable = false
    frame.isVisible = true
}

class Installer() : JPanel() {
    /**
     * MultiMC is proving to be difficult it will be added in a separate pr.
     *
     * MULTIMC CHECKLIST
     *
     * - Get instances folder for osx (Fixing getInstacesFolder())
     * - Un-center button
     * - Some way to choose directory
     * - Some way to choose instance
     */
    private val INSTALL_BUTTON_TEXT_OFFSET = 55
    private val AMOUNT_OF_BACKGROUNDS = 6
    private val INSTALL_BUTTON_X_POS = 195
    private val JETBRAINS = Installer::class.java.getResourceAsStream("/assets/kami/Jetbrains.ttf")


    @Throws(IOException::class, SAXException::class, ParserConfigurationException::class, InterruptedException::class)
    private fun installKami(directory: String) {
        if (!hasLatestFabric(directory.substring(0, directory.length - 5))) {
            val process = installFabric()
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val exitVal = process.waitFor()
            println("Exited fabric installation with exit code $exitVal")
            reader.close()
            File(System.getProperty("user.dir") + File.separator + "fabric-installer.jar").delete()
        }
        val latestVersion = latestKami
        val kamiMod = Paths.get(directory + "kami-" + latestVersion + ".jar")
        val `in` =
            URL("https://github.com/zeroeightysix/KAMI/releases/download/$latestVersion/kami-$latestVersion.jar").openStream()
        Arrays.stream(Objects.requireNonNull(File(directory).list())).forEach { mod: String ->
            if (mod.contains("kami-") && mod != "kami-$latestVersion.jar") {
                val modFile = File(directory + mod)
                if (!modFile.isDirectory) {
                    JOptionPane.showMessageDialog(
                        null,
                        "Outdated KAMI ver. " + mod.substring(0, mod.length - 4) + " detected. Deleting..."
                    )
                    modFile.delete()
                }
            }
        }
        if (File(kamiMod.toString()).exists()) {
            JOptionPane.showMessageDialog(null, "It looks like KAMI is already installed.")
        } else {
            Files.copy(`in`, kamiMod, StandardCopyOption.REPLACE_EXISTING)
            JOptionPane.showMessageDialog(null, "Installed KAMI to:\n$directory")
        }
    }

    @Throws(IOException::class, ParserConfigurationException::class, SAXException::class)
    private fun installFabric(): Process {
        val latestVersion = latestFabric
        JOptionPane.showMessageDialog(
            null,
            "It looks like fabric is not installed, this will prompt you to install fabric version $latestVersion"
        )
        val fabricInstall = Paths.get(System.getProperty("user.dir") + File.separator + "fabric-installer.jar")
        val `in` =
            URL("https://maven.fabricmc.net/net/fabricmc/fabric-installer/$latestVersion/fabric-installer-$latestVersion.jar").openStream()
        Files.copy(`in`, fabricInstall, StandardCopyOption.REPLACE_EXISTING)
        return Runtime.getRuntime().exec("java -jar fabric-installer.jar")
    }

    /**
     * Fetches the latest version of KAMI from releases
     * As is the case with the Kami Blue installer,
     * because we can't import libraries that minecraft has and because
     * the game isn't initialized we **have** to hard code this 😔
     *
     * @return The version name of the latest release
     * @throws IOException
     */
    @get:Throws(IOException::class)
    private val latestKami: String
        private get() {
            URL("https://api.github.com/repos/zeroeightysix/KAMI/releases").openStream().use { `is` ->
                val reader = BufferedReader(InputStreamReader(`is`, StandardCharsets.UTF_8))
                val builder = StringBuilder()
                var currentCharacter: Int
                while (reader.read().also { currentCharacter = it } != -1) {
                    builder.append(currentCharacter.toChar())
                }
                val jsonText = builder.toString()
                return jsonText.split("tag_name").toTypedArray()[1].split("\"")
                    .toTypedArray()[2]
            }
        }

    /**
     * Checks if there is the latest version of fabric-loader
     * @param directory The minecraft directory to check for fabric
     * @return If the directory has the latest version of fabric-loader
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    @Throws(ParserConfigurationException::class, SAXException::class, IOException::class)
    private fun hasLatestFabric(directory: String): Boolean {
        val latestFabricLoader = latestFabricLoader
        for (file in Objects.requireNonNull(File(directory + "versions").listFiles())) {
            val version = File(file.toString()).name
            if (version.contains("fabric-loader") && version.contains(latestFabricLoader)) return true
        }
        return false
    }

    /**
     * Fetches the build number of the latest version of fabric
     * @return The build number of the latest version of fabric
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    @get:Throws(IOException::class, ParserConfigurationException::class, SAXException::class)
    private val latestFabric: String
        private get() {
            val xmlURL = URL("https://maven.fabricmc.net/net/fabricmc/fabric-installer/maven-metadata.xml")
            val xml = xmlURL.openStream()
            val latestVersion =
                DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xml).getElementsByTagName("latest")
                    .item(0).textContent
            xml.close()
            return latestVersion
        }

    /**
     * Fetches the build id of the latest version of fabric-loader
     * @return The build id of the latest version of fabric-loader
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    @get:Throws(IOException::class, ParserConfigurationException::class, SAXException::class)
    private val latestFabricLoader: String
        private get() {
            val xmlURL = URL("https://maven.fabricmc.net/net/fabricmc/fabric-loader/maven-metadata.xml")
            val xml = xmlURL.openStream()
            val latestVersion =
                DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xml).getElementsByTagName("latest")
                    .item(0).textContent
            xml.close()
            return latestVersion.substring(latestVersion.indexOf("build"))
        }

    /**
     * Fetches location of '.minecraft' folder
     * @return Location of '.minecraft' folder on user's computer
     */
    private val minecraftFolder: String
        private get() {
            val operatingSystemName = System.getProperty("os.name").toLowerCase(Locale.ROOT)
            if (operatingSystemName.contains("nux")) {
                return System.getProperty("user.home") + "/.minecraft/mods/"
            } else if (operatingSystemName.contains("mac") || operatingSystemName.contains("darwin")) {
                return System.getProperty("user.home") + "/Library/Application Support/minecraft/mods/"
            } else if (operatingSystemName.contains("win")) {
                return System.getenv("APPDATA") + File.separator + ".minecraft" + File.separator + "mods" + File.separator
            }
            throw RuntimeException("Cannot find minecraft folder.")
        }

    /**
     * Broken on MacOs: Fetches location of 'instances' folder on user's computer
     * @return (MultiMC) Location of 'instances' folder on user's computer
     */
    private val instacesFolder: String
        private get() {
            val operatingSystemName = System.getProperty("os.name").toLowerCase(Locale.ROOT)
            if (operatingSystemName.contains("nux")) {
                return System.getProperty("user.home") + "/.local/share/multimc/instances"
            } else if (operatingSystemName.contains("mac") || operatingSystemName.contains("darwin")) {
                return "TODO"
            } else if (operatingSystemName.contains("win")) {
                return System.getenv("PROGRAMFILES") + File.separator + "MultiMC" + File.separator + "instances" + File.separator
            }
            throw RuntimeException("Cannot find instaces folder.")
        }

    init {
        val installButton = JButton()
        installButton.isOpaque = false
        installButton.isContentAreaFilled = false
        installButton.isBorderPainted = false
        installButton.toolTipText = "KAMI INSTALL"
        var jetbrainsFont = Font.createFont(Font.TRUETYPE_FONT, JETBRAINS)
        jetbrainsFont = jetbrainsFont.deriveFont(22f)
        val backgroundImage = Installer::class.java.getResourceAsStream(
            "/assets/kami/installer/backgrounds/" + Random().nextInt(AMOUNT_OF_BACKGROUNDS) + ".png"
        )
        val backgroundPane = JLabel(ImageIcon(ImageIO.read(backgroundImage)))
        val installButtonImage = javaClass.getResourceAsStream("/assets/kami/installer/buttons/red.png")
        val installIcon = JLabel(ImageIcon(ImageIO.read(installButtonImage)))
        val installText = JLabel()
        installText.text = "Install"
        installText.font = jetbrainsFont
        installText.foreground = Color.WHITE
        val kamiImage = javaClass.getResourceAsStream("/assets/kami/installer/pixel_kami_logo.png")
        val kamiIcon = JLabel(ImageIcon(ImageIO.read(kamiImage)))
        preferredSize = Dimension(600, 335)
        layout = null
        add(installText)
        add(installButton)
        add(installIcon)
        add(kamiIcon)
        add(backgroundPane) // Add this *LAST* so renders over everything else.
        installIcon.setBounds(INSTALL_BUTTON_X_POS, 245, 200, 50)
        installButton.setBounds(INSTALL_BUTTON_X_POS, 245, 200, 50)
        installText.setBounds(INSTALL_BUTTON_X_POS + INSTALL_BUTTON_TEXT_OFFSET, 245, 200, 50)
        kamiIcon.setBounds(236, 70, 128, 128)
        backgroundPane.setBounds(0, 0, 600, 355)
        installButton.addActionListener { event: ActionEvent? ->
            installButton.isEnabled = false
            installIcon.isOpaque = false
            try {
                installKami(minecraftFolder)
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: SAXException) {
                e.printStackTrace()
            } catch (e: ParserConfigurationException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            System.exit(0)
        }
    }
}