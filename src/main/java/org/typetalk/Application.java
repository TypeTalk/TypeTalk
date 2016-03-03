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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.typetalk.speech.Speeker;
import org.typetalk.ui.ApplicationWindow;
import org.typetalk.ui.WelcomeScreen;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import marytts.exceptions.MaryConfigurationException;
import raging.goblin.swingutils.ScreenPositioner;
import raging.goblin.swingutils.SplashScreen;
import raging.goblin.swingutils.SwingUtils;

@Slf4j
public class Application {

   private static final UIProperties PROPERTIES = UIProperties.getInstance();
   private static final Messages MESSAGES = Messages.getInstance();

   public static void main(String[] args) {
      initDataFolder();
      SwingUtils.initAntiAliasing();
      SwingUtils.disablePaintSliderValue();
      SwingUtils.loadLaf(args, PROPERTIES);
      setLocale();

      SplashScreen splashScreen = new SplashScreen("TypeTalk", "/icons/sound.png");
      ScreenPositioner.centerOnScreen(splashScreen);
      splashScreen.setVisible(PROPERTIES.isSplashScreenEnabled());

      try {
         Speeker speeker = new Speeker();
         Runtime.getRuntime().addShutdownHook(new ShutdownHook(speeker));
         ApplicationWindow applicationWindow = new ApplicationWindow(speeker);
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
            Thread.sleep(5000);
         } catch (InterruptedException e1) {
            log.error(e.getMessage(), e);
         }
         System.exit(0);
      }
   }

   private static void initDataFolder() {
      try {
         String configuredSettingsDirectory = PROPERTIES.getSettingsDirectory();
         if (configuredSettingsDirectory.equals(UIProperties.DEFAULT_SETTINGS_DIRECTORY)) {
            Path homeDirectory = Paths.get(System.getProperty("user.home")).normalize().toAbsolutePath();
            configuredSettingsDirectory = homeDirectory.toString() + File.separator
                  + UIProperties.DEFAULT_SETTINGS_DIRECTORY;
            PROPERTIES.setSettingsDirectory(configuredSettingsDirectory);
         }
         Path settingsDirectory = Paths.get(PROPERTIES.getSettingsDirectory());
         if (!Files.exists(settingsDirectory)) {
            Files.createDirectories(settingsDirectory);
         } else if (!settingsDirectory.toFile().isDirectory()) {
            PROPERTIES.setSettingsDirectory(PROPERTIES.getSettingsDirectory() + "_dir");
            Files.createDirectories(Paths.get(PROPERTIES.getSettingsDirectory()));
         }
      } catch (IOException e) {
         log.error("Unable to initialize settings directory");
      }
   }

   private static void setLocale() {
      String language = PROPERTIES.getLocaleLanguage();
      String country = PROPERTIES.getLocaleCountry();
      log.debug(String.format("Setting locale to: %s/%s", language, country));
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
