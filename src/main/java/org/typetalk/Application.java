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
import java.util.Locale;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.typetalk.speech.Speeker;
import org.typetalk.ui.ClientWindow;
import org.typetalk.ui.ScreenPositioner;
import org.typetalk.ui.SplashScreen;
import org.typetalk.ui.WelcomeScreen;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import marytts.exceptions.MaryConfigurationException;

@Slf4j
public class Application {

   private static final UIProperties PROPERTIES = UIProperties.getInstance();
   private static final Messages MESSAGES = Messages.getInstance();

   public static void main(String[] args) {
      loadLaf(args);
      setLocale();

      SplashScreen splashScreen = new SplashScreen();
      ScreenPositioner.centerOnScreen(splashScreen);
      splashScreen.setVisible(PROPERTIES.isSplashScreenEnabled());

      try {
         Speeker speeker = new Speeker();
         Runtime.getRuntime().addShutdownHook(new ShutdownHook(speeker));
         ClientWindow clientWindow = new ClientWindow(speeker);
         splashScreen.setVisible(false);
         splashScreen.dispose();
         if (PROPERTIES.isWelcomeScreenEnabled()) {
            WelcomeScreen welcomeScreen = new WelcomeScreen();
            ScreenPositioner.centerOnScreen(welcomeScreen);
            welcomeScreen.setVisible(true);
         }
         clientWindow.setVisible(true);
         if (PROPERTIES.isStartMinimized()) {
            clientWindow.setExtendedState(Frame.ICONIFIED);
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

   private static void setLocale() {
      String language = PROPERTIES.getLocaleLanguage();
      String country = PROPERTIES.getLocaleCountry();
      log.debug(String.format("Setting locale to: %s/%s", language, country));
      Locale.setDefault(new Locale(language, country));
   }

   private static void loadLaf(String[] args) {
      try {
         if (args.length >= 1) {
            log.debug("Setting look and feel manually to " + args[0]);
            switch (args[0].toLowerCase()) {
            case "metal":
               UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
               return;
            case "nimbus":
               UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
               return;
            case "motif":
               UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
               return;
            case "gtk":
               UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
               return;
            }
         }

         if (PROPERTIES.getLaf().equals(UIProperties.DEFAULT_LAF)) {
            if (UIManager.getSystemLookAndFeelClassName().contains("Metal")) {
               PROPERTIES.setLaf("Nimbus");
            } else {
               for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                  if (info.getClassName().equals(UIManager.getSystemLookAndFeelClassName())) {
                     PROPERTIES.setLaf(info.getName());
                  }
               }
            }
         }
         
         for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if(info.getName().equals(PROPERTIES.getLaf())) {
               UIManager.setLookAndFeel(info.getClassName());
               log.debug("Setting system look and feel: " + info.getClassName());
            }
         }
      } catch (Exception e) {
         log.error("Could not set look and feel");
         log.debug(e.getMessage(), e);
      }
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
