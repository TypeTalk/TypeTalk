/*
 * Copyright 2016, TypeTalk <http://typetalk.github.io/TypeTalk>
 * 
 * This file is part of TypeTalk.
 *
 *  TypeTalk is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  TypeTalk is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with TypeTalk.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.typetalk;

import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JOptionPane;

import org.apache.commons.io.FilenameUtils;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.typetalk.speech.Speeker;
import org.typetalk.speech.VoiceDescription;
import org.typetalk.ui.ApplicationWindow;
import org.typetalk.ui.WelcomeScreen;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import marytts.exceptions.MaryConfigurationException;
import raging.goblin.swingutils.ApplicationInstanceManager;
import raging.goblin.swingutils.ScreenPositioner;
import raging.goblin.swingutils.SplashScreen;
import raging.goblin.swingutils.SwingUtils;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Application {

   public static final String DOWNLOAD_DIR = "download";
   public static final String INSTALLATION_DIR = "installed";
   public static final String LIB_DIR = "lib";

   private static final TypeTalkProperties PROPERTIES = TypeTalkProperties.getInstance();
   private static final Messages MESSAGES = Messages.getInstance();

   private static ApplicationWindow applicationWindow;

   public static void main(String[] args) {
      checkSingleInstance();
      SwingUtils.initAntiAliasing();
      SwingUtils.disablePaintSliderValue();
      SwingUtils.loadLaf(args, PROPERTIES);
      setLocale();

      SplashScreen splashScreen = new SplashScreen("TypeTalk", "/icons/sound.png");
      ScreenPositioner.centerOnScreen(splashScreen);
      splashScreen.setVisible(PROPERTIES.isSplashScreenEnabled());

      try {

         initDataFolder();
         initMaryProperties();
         initDefaultLanguagesAndVoices();
         loadLanguagesAndVoices();

      } catch (IOException | URISyntaxException | SecurityException | IllegalArgumentException
            | NoSuchMethodException e) {

         log.error("Initialization error, unable to start TypeTalk", e);
         splashScreen.setMessage(MESSAGES.get("initialization_error"));
         try {
            Thread.sleep(3000);
         } catch (InterruptedException ex) {
            log.error("Sleep interrupted", ex);
         } finally {
            System.exit(100);
         }
      }

      try {
         Speeker speeker = new Speeker();
         Runtime.getRuntime().addShutdownHook(new ShutdownHook(speeker));
         applicationWindow = new ApplicationWindow(speeker);
         splashScreen.setVisible(false);
         splashScreen.dispose();
         if (PROPERTIES.isWelcomeScreenEnabled()) {
            WelcomeScreen welcomeScreen = new WelcomeScreen();
            ScreenPositioner.centerOnScreen(welcomeScreen);
            welcomeScreen.setVisible(true);
         }
         applicationWindow.setVisible(true);
         if (PROPERTIES.isStartMinimized()) {
            applicationWindow.setExtendedState(Frame.ICONIFIED);
         }
      } catch (MaryConfigurationException e) {
         log.error("Unable to start Speeker", e);
         splashScreen.setMessage(MESSAGES.get("initialization_error"));
         try {
            Thread.sleep(3000);
         } catch (InterruptedException e1) {
            log.error(e.getMessage(), e);
         }
         System.exit(0);
      }
   }

   private static void loadLanguagesAndVoices() throws IOException, NoSuchMethodException, SecurityException {
      Path libDir = Paths.get(PROPERTIES.getSettingsDirectory() + File.separator + LIB_DIR);
      URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
      Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
      method.setAccessible(true);

      Files.walkFileTree(libDir, new SimpleFileVisitor<Path>() {

         @Override
         public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (FilenameUtils.getExtension(file.getFileName().toString()).equals("jar")) {
               try {
                  method.invoke(classLoader, file.toUri().toURL());
               } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                  throw new IOException(e);
               }
            }
            return super.visitFile(file, attrs);
         }
      });

      VoiceDescription.loadVoiceDescriptions();
   }

   private static void checkSingleInstance() {
      if (!ApplicationInstanceManager.registerInstance()) {
         log.warn("Another instance of this application is already running.");
         System.exit(0);
      }
      ApplicationInstanceManager.setSubListener(() -> {
         log.warn("The user tried to start the application twice");
         JOptionPane.showMessageDialog(applicationWindow, MESSAGES.get("single_instance"), MESSAGES.get("error"),
               JOptionPane.ERROR_MESSAGE);
      });
   }

   private static void initDataFolder() throws IOException {
      String configuredSettingsDirectory = PROPERTIES.getSettingsDirectory();
      if (configuredSettingsDirectory.equals(TypeTalkProperties.DEFAULT_SETTINGS_DIRECTORY)) {
         Path homeDirectory = Paths.get(System.getProperty("user.home")).normalize().toAbsolutePath();
         configuredSettingsDirectory = homeDirectory.toString() + File.separator
               + TypeTalkProperties.DEFAULT_SETTINGS_DIRECTORY;
         PROPERTIES.setSettingsDirectory(configuredSettingsDirectory);
      }
      Path settingsDirectory = Paths.get(PROPERTIES.getSettingsDirectory());
      if (!Files.exists(settingsDirectory)) {
         Files.createDirectories(settingsDirectory);
      } else if (!settingsDirectory.toFile().isDirectory()) {
         PROPERTIES.setSettingsDirectory(PROPERTIES.getSettingsDirectory() + "_dir");
         Files.createDirectories(Paths.get(PROPERTIES.getSettingsDirectory()));
      }
   }

   private static void initMaryProperties() {
      System.setProperty("mary.base", PROPERTIES.getSettingsDirectory());
      System.setProperty("mary.installedDir", PROPERTIES.getSettingsDirectory() + File.separator + INSTALLATION_DIR);
      System.setProperty("mary.downloadDir", PROPERTIES.getSettingsDirectory() + File.separator + DOWNLOAD_DIR);
   }

   private static void initDefaultLanguagesAndVoices() throws IOException, URISyntaxException {
      Path installationDir = Paths.get(PROPERTIES.getSettingsDirectory() + File.separator + INSTALLATION_DIR);
      Files.createDirectories(installationDir);
      Path libDir = Paths.get(PROPERTIES.getSettingsDirectory() + File.separator + LIB_DIR);
      Files.createDirectories(libDir);
      Path downloadDir = Paths.get(PROPERTIES.getSettingsDirectory() + File.separator + DOWNLOAD_DIR);
      Files.createDirectories(downloadDir);

      extractZipFromJar(PROPERTIES.getSettingsDirectory());
   }

   private static void extractZipFromJar(String outputFolder) throws IOException {
      InputStream is = Application.class.getResourceAsStream("/components/components.zip");
      try (ZipInputStream zis = new ZipInputStream(is)) {
         ZipEntry entry;
         while ((entry = zis.getNextEntry()) != null) {
            if (entry.isDirectory()) {
               Files.createDirectories(Paths.get(outputFolder + File.separator + entry.getName()));
            } else {
               Path outputFile = Paths.get(outputFolder + File.separator + entry.getName());
               if (!outputFile.toFile().exists()) {
                  Files.copy(zis, outputFile);
               }
            }
         }
      }
   }

   private static void setLocale() {
      String language = PROPERTIES.getDisplayLocaleLanguage();
      String country = PROPERTIES.getDisplayLocaleCountry();
      log.debug("Setting locale to: {}/{}", language, country);
      Locale.setDefault(new Locale(language, country));
   }

   @AllArgsConstructor
   private static class ShutdownHook extends Thread {

      private Speeker speeker;

      @Override
      public void run() {
         try {
            GlobalScreen.unregisterNativeHook();
            speeker.stop();
            log.info("TypeTalk shutdown complete");
         } catch (NativeHookException ex) {
            log.warn("Unable to unregister native hook");
         }
      }
   }
}
