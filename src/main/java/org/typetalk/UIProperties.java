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

import java.util.Arrays;
import java.util.Locale;
import java.util.prefs.Preferences;

public class UIProperties {

   private static final String DEFAULT_NATIVE_HOOK_KEY_CODES = "29,56,57";

   public static final boolean DEFAULT_SPLASH_SCREEN_ENABLED = true;
   public static final boolean DEFAULT_WELCOME_SCREEN_ENABLED = true;
   public static final boolean DEFAULT_START_MINIMIZED = false;
   public static final String DEFAULT_VOICE = "DFKI_OBADIAH";
   public static final int DEFAULT_DOUBLE_CLICK_DELAY = 250;
   public static final int DEFAULT_TOAST_TIME = 3000;
   public static final String DEFAULT_LAF = "DEFAULT";

   private static final String KEY_LANGUAGE = "language";
   private static final String KEY_COUNTRY = "country";
   private static final String KEY_NATIVE_HOOK_KEY_CODES = "nativehookkeycodes";
   private static final String KEY_SPLASH_SCREEN_ENABLED = "splashscreenenabled";
   private static final String KEY_WELCOME_SCREEN_ENABLED = "welcomescreenenabled";
   private static final String KEY_START_MINIMIZED = "startminimized";
   private static final String KEY_VOICE = "voice";
   private static final String KEY_DOUBLE_CLICK_DELAY = "doubleclickdelay";
   private static final String KEY_TOAST_TIME = "toasttime";
   private static final String KEY_LAF = "lookandfeel";

   private static UIProperties instance;
   private boolean nativeHookEnabled = true;
   private int[] nativeHookKeyCodes;
   private static Preferences userPreferences = Preferences.userNodeForPackage(UIProperties.class);

   private UIProperties() {
      // Singleton
   }

   public static UIProperties getInstance() {
      if (instance == null) {
         instance = new UIProperties();
      }
      return instance;
   }

   public String getLocaleLanguage() {
      Locale currentLocale = Locale.getDefault();
      return userPreferences.get(KEY_LANGUAGE, currentLocale.getLanguage());
   }

   public void setLocaleLanguage(String language) {
      userPreferences.put(KEY_LANGUAGE, language);
   }

   public String getLocaleCountry() {
      Locale currentLocale = Locale.getDefault();
      return userPreferences.get(KEY_COUNTRY, currentLocale.getCountry());
   }

   public void setLocaleCountry(String country) {
      userPreferences.put(KEY_COUNTRY, country);
   }

   public void setSplashScreenEnabled(boolean splashScreenEnabled) {
      userPreferences.putBoolean(KEY_SPLASH_SCREEN_ENABLED, splashScreenEnabled);
   }

   public boolean isSplashScreenEnabled() {
      return userPreferences.getBoolean(KEY_SPLASH_SCREEN_ENABLED, DEFAULT_SPLASH_SCREEN_ENABLED);
   }

   public void setWelcomeScreenEnabled(boolean welcomeScreenEnabled) {
      userPreferences.putBoolean(KEY_WELCOME_SCREEN_ENABLED, welcomeScreenEnabled);
   }

   public boolean isWelcomeScreenEnabled() {
      return userPreferences.getBoolean(KEY_WELCOME_SCREEN_ENABLED, DEFAULT_WELCOME_SCREEN_ENABLED);
   }

   public void setStartMinimized(boolean startMinimized) {
      userPreferences.putBoolean(KEY_START_MINIMIZED, startMinimized);
   }

   public boolean isStartMinimized() {
      return userPreferences.getBoolean(KEY_START_MINIMIZED, DEFAULT_START_MINIMIZED);
   }

   public boolean isNativeHookEnabled() {
      return nativeHookEnabled;
   }

   public void setNativeHookEnabled(boolean nativeHookEnabled) {
      this.nativeHookEnabled = nativeHookEnabled;
   }

   public int[] getNativeHookKeyCodes() {
      if (nativeHookKeyCodes == null) {
         String[] keyCodes = userPreferences.get(KEY_NATIVE_HOOK_KEY_CODES, DEFAULT_NATIVE_HOOK_KEY_CODES).split(",");
         nativeHookKeyCodes = Arrays.stream(keyCodes).filter(v -> !v.trim().equals(""))
               .mapToInt(v -> Integer.parseInt(v.trim())).toArray();
      }
      return nativeHookKeyCodes;
   }

   public void setNativeHookKeyCodes(int[] keyCodes) {
      nativeHookKeyCodes = keyCodes;
      userPreferences.put(KEY_NATIVE_HOOK_KEY_CODES,
            Arrays.toString(keyCodes).replaceAll("\\[", "").replaceAll("\\]", ""));
   }

   public int[] getDefaultNativeHookKeyCodes() {
      return Arrays.stream(DEFAULT_NATIVE_HOOK_KEY_CODES.split(",")).mapToInt(v -> Integer.parseInt(v)).toArray();
   }

   public String getVoice() {
      return userPreferences.get(KEY_VOICE, DEFAULT_VOICE);
   }

   public void setVoice(String voice) {
      userPreferences.put(KEY_VOICE, voice);
   }

   public int getDoubleClickDelay() {
      return userPreferences.getInt(KEY_DOUBLE_CLICK_DELAY, DEFAULT_DOUBLE_CLICK_DELAY);
   }

   public void setDoubleClickDelay(int doubleClickDelay) {
      userPreferences.putInt(KEY_DOUBLE_CLICK_DELAY, doubleClickDelay);
   }

   public int getToastTime() {
      return userPreferences.getInt(KEY_TOAST_TIME, DEFAULT_TOAST_TIME);
   }

   public void setToastTime(int toastTime) {
      userPreferences.putInt(KEY_TOAST_TIME, toastTime);
   }
   
   public String getLaf() {
      return userPreferences.get(KEY_LAF, DEFAULT_LAF);
   }

   public void setLaf(String laf) {
      userPreferences.put(KEY_LAF, laf);
   }
}
