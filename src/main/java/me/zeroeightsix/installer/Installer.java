package me.zeroeightsix.installer;


import java.io.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Random;

import org.xml.sax.SAXException;

public class Installer extends JPanel {
    private final int BUTTON_TEXT_OFFSET = 55;
    private final InputStream JETBRAINS = Installer.class.getResourceAsStream("/assets/kami/Jetbrains.ttf");

    public static void main(String[] args) throws IOException, FontFormatException {
        URL kamiLogo = Installer.class.getResource("/assets/kami/installer/pixel_kami_logo.png");
        JFrame frame = new JFrame("KAMI Installer");
        frame.setIconImage(ImageIO.read(kamiLogo));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new Installer());
        frame.pack();
        frame.setResizable(false);
        frame.setVisible(true);
    }

    private Installer() throws IOException, FontFormatException {
        JButton installButton = new JButton();
        installButton.setOpaque(false);
        installButton.setContentAreaFilled(false);
        installButton.setBorderPainted(false);
        installButton.setToolTipText("KAMI INSTALL");

        Font jetbrainsFont = Font.createFont(Font.TRUETYPE_FONT, JETBRAINS);
        jetbrainsFont = jetbrainsFont.deriveFont(jetbrainsFont.getSize() * 22F);

        URL backgroundImage = Installer.class.getResource("/assets/kami/installer/backgrounds/" + new Random().nextInt(2) + ".png");
        JLabel backgroundPane = new JLabel(new ImageIcon(ImageIO.read(backgroundImage)));

        URL installButtonImage = Installer.class.getResource("/assets/kami/installer/buttons/red.png");
        JLabel installIcon = new JLabel(new ImageIcon(ImageIO.read(installButtonImage)));

        JLabel installText = new JLabel();
        installText.setText("Install");
        installText.setFont(jetbrainsFont);
        installText.setForeground(Color.WHITE);

        URL kamiImage = Installer.class.getResource("/assets/kami/installer/pixel_kami_logo.png");
        JLabel kamiIcon = new JLabel(new ImageIcon(ImageIO.read(kamiImage)));

        setPreferredSize(new Dimension(600, 335));
        setLayout(null);

        add(installText);
        add(installButton);
        add(installIcon);
        add(kamiIcon);
        add(backgroundPane); // Add this *LAST* so renders over everything else.

        installIcon.setBounds(90, 245, 200, 50);
        installButton.setBounds(90, 245, 200, 50);
        installText.setBounds(90+BUTTON_TEXT_OFFSET, 245, 200, 50);
        kamiIcon.setBounds(236, 70, 128, 128);
        backgroundPane.setBounds(0, 0, 600, 355);

        installButton.addActionListener(event -> {
            installButton.setEnabled(false);
            installIcon.setOpaque(false);
            try {
                installKami(getModsFolder());
            } catch (IOException | SAXException | ParserConfigurationException e) {
                e.printStackTrace();
            }
            System.exit(0);
        });
    }

    private String getModsFolder() {
        String operatingSystemName = System.getProperty("os.name").toLowerCase(Locale.ROOT);

        if (operatingSystemName.contains("nux")) {
            return  System.getProperty("user.home") + "/.minecraft/mods/";
        } else if (operatingSystemName.contains("mac") || operatingSystemName.contains("darwin")) {
            return System.getProperty("user.home") + "/Library/Application Support/minecraft/mods/";
        } else if (operatingSystemName.contains("win")) {
            return System.getenv("APPDATA") + File.separator + ".minecraft" + File.separator + "mods" + File.separator;
        }

        throw new RuntimeException("Cannot find minecraft folder.");

    }

    private String getInstacesFolder() {
        String operatingSystemName = System.getProperty("os.name").toLowerCase(Locale.ROOT);

        if (operatingSystemName.contains("nux")) {
            return System.getProperty("user.home") + "/.local/share/multimc/instances";
        } else if (operatingSystemName.contains("mac") || operatingSystemName.contains("darwin")) {
            //TODO: get instances folder for osx
            return "TODO Mac multimc instances folder";
        } else if (operatingSystemName.contains("win")) {
            return System.getenv("PROGRAMFILES") + File.separator + "MultiMC" + File.separator + "instances" + File.separator;
        }

        throw new RuntimeException("Cannot find instaces folder.");
    }

    private void installKami(String directory) throws IOException, SAXException, ParserConfigurationException {
        //TODO: find and delete older kami versions
        if (!hasLatestFabric(directory.substring(0, directory.length() - 5))) installFabric();

        String latestVersion = getLatestKami();
        System.out.println(latestVersion);

        Path mods  = Paths.get(directory + latestVersion + ".jar");
        InputStream in = new URL("https://github.com/zeroeightysix/KAMI/releases/download/" + latestVersion + "/kami-" + latestVersion + ".jar").openStream();

        if (new File(String.valueOf(mods)).exists()) {
            JOptionPane.showMessageDialog(null, "It looks like KAMI is already installed.");
        } else {
            Files.copy(in, mods, StandardCopyOption.REPLACE_EXISTING);
            JOptionPane.showMessageDialog(null, "Installed KAMI to:\n"+directory);
        }

        //new File(System.getProperty("user.dir") + File.separator + "fabric-installer.jar").delete();
    }

    private boolean hasLatestFabric(String directory) {
        //TODO: CHECK FOR LATEST VERSION OF FABRIC
        Path fabric = Paths.get(directory+".fabric");
        System.out.println(fabric.toString());
        return Files.exists(fabric);

    }

    private void installFabric() throws IOException, SAXException, ParserConfigurationException {
        URL xmlURL = new URL("https://maven.fabricmc.net/net/fabricmc/fabric-installer/maven-metadata.xml");
        InputStream xml = xmlURL.openStream();
        String latestVersion = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xml).getElementsByTagName("latest").item(0).getTextContent();
        xml.close();

        JOptionPane.showMessageDialog(null,"It looks like fabric is not installed, this will prompt you to install fabric version " + latestVersion);

        Path fabricInstall = Paths.get(System.getProperty("user.dir") + File.separator + "fabric-installer.jar");
        InputStream in = new URL("https://maven.fabricmc.net/net/fabricmc/fabric-installer/"+latestVersion+"/fabric-installer-"+latestVersion+".jar").openStream();
        Files.copy(in, fabricInstall, StandardCopyOption.REPLACE_EXISTING);

        Runtime.getRuntime().exec("java -jar fabric-installer.jar");
    }

    private String getLatestKami() throws IOException {
        try (InputStream is = new URL("https://api.github.com/repos/zeroeightysix/KAMI/releases").openStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();

            int currentCharacter;
            while ((currentCharacter = reader.read()) != -1) {
                builder.append((char) currentCharacter);
            }
            String jsonText = builder.toString();

            // As is the case with the Kami Blue installer,
            // because we can't import libraries that minecraft has and because
            // the game isn't initialized we **have** to hard code this😔
            return jsonText.split("tag_name", 2)[1].split("\"", 6)[2];

        }
    }

}
