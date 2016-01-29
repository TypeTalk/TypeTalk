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

package org.typetalk.speech;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import org.typetalk.Messages;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class SoundEffect {

   public enum Effect {
      TRACT_SCALER, F0_SCALE, F0_ADD, RATE, ROBOT, WHISPER, STADIUM, CHORUS, FIR_FILTER, JET_PILOT
   };

   private static final Preferences PREFERENCES = Preferences.userNodeForPackage(SoundEffect.class);
   private static final Messages MESSAGES = Messages.getInstance();
   private static final List<SoundEffect> EFFECTS = new ArrayList<>();

   private Effect effect;
   private String name;
   private String helpText;
   private String format;
   private Map<String, List<Double>> defaultLevels;

   public static List<SoundEffect> getAllSoundEffects() {
      if (EFFECTS.isEmpty()) {
         EFFECTS.add(createTractScaler());
         EFFECTS.add(createF0Add());
         EFFECTS.add(createF0Scale());
         EFFECTS.add(createRobot());
         EFFECTS.add(createWhisper());
         EFFECTS.add(createStadium());
         EFFECTS.add(createChorus());
         EFFECTS.add(createFirFilter());
         EFFECTS.add(createJetPilot());
      }
      return EFFECTS;
   }

   public static String toMaryTTSString() {
      return getAllSoundEffects().stream().filter(e -> e.isEnabled()).map(e -> e.getMaryTTSString())
            .collect(Collectors.joining("+"));
   }

   public List<String> getLevelKeys() {
      return new ArrayList<>(defaultLevels.keySet());
   }

   public void setLevel(String key, double value) {
      if (defaultLevels.containsKey(key)) {
         PREFERENCES.putDouble(effect + "_" + key, value);
      }
   }

   public double getLevel(String key) {
      return PREFERENCES.getDouble(effect + "_" + key, defaultLevels.get(key).get(0));
   }

   public void setDefaultLevels() {
      for (String key : defaultLevels.keySet()) {
         setLevel(key, defaultLevels.get(key).get(0));
      }
   }

   public boolean isEnabled() {
      return PREFERENCES.getBoolean(effect.name() + "_enabled", false);
   }

   public void setEnabled(boolean enabled) {
      PREFERENCES.putBoolean(effect.name() + "_enabled", enabled);
   }

   private static SoundEffect createTractScaler() {
      return new SoundEffect(Effect.TRACT_SCALER, MESSAGES.get("tract_scaler"), MESSAGES.get("tract_scaler_help"),
            "TractScaler(amount=%.2f;)", createDefaultLevelsMap(Arrays.asList("amount"),
                  Arrays.asList(Arrays.asList(1.5, 0.25, 4.0, 0.1))));
   }

   private static SoundEffect createJetPilot() {
      return new SoundEffect(Effect.JET_PILOT, MESSAGES.get("jet_pilot"), MESSAGES.get("jet_pilot_help"), "JetPilot",
            new HashMap<>());
   }

   private static SoundEffect createF0Scale() {
      return new SoundEffect(Effect.F0_SCALE, MESSAGES.get("f0_scaling"), MESSAGES.get("f0_scaling_help"),
            "F0Scale(f0Scale=%.2f;)", createDefaultLevelsMap(Arrays.asList("f0Scale"),
                  Arrays.asList(Arrays.asList(2.0, 0.0, 3.0, 0.1))));
   }

   private static SoundEffect createF0Add() {
      return new SoundEffect(Effect.F0_ADD, MESSAGES.get("f0_add"), MESSAGES.get("f0_add_help"), "F0Add(f0Add=%.2f;)",
            createDefaultLevelsMap(Arrays.asList("f0Add"), Arrays.asList(Arrays.asList(50.0, -300.0, 300.0, 10.0))));
   }

   private static SoundEffect createRate() {
      return new SoundEffect(Effect.RATE, MESSAGES.get("rate"), MESSAGES.get("rate_help"), "Rate(durScale=%.2f;)",
            createDefaultLevelsMap(Arrays.asList("durScale"), Arrays.asList(Arrays.asList(1.5, 0.1, 3.0, 0.1))));
   }

   private static SoundEffect createRobot() {
      return new SoundEffect(Effect.ROBOT, MESSAGES.get("robot"), MESSAGES.get("robot_help"), "Robot(amount=%.2f;)",
            createDefaultLevelsMap(Arrays.asList("amount"), Arrays.asList(Arrays.asList(100.0, 0.0, 100.0, 10.0))));
   }

   private static SoundEffect createWhisper() {
      return new SoundEffect(Effect.WHISPER, MESSAGES.get("whisper"), MESSAGES.get("whisper_help"),
            "Whisper(amount=%.2f;)", createDefaultLevelsMap(Arrays.asList("amount"),
                  Arrays.asList(Arrays.asList(100.0, 0.0, 100.0, 10.0))));
   }

   private static SoundEffect createStadium() {
      return new SoundEffect(Effect.STADIUM, MESSAGES.get("stadium"), MESSAGES.get("stadium_help"),
            "Stadium(amount=%.2f;)", createDefaultLevelsMap(Arrays.asList("amount"),
                  Arrays.asList(Arrays.asList(100.0, 0.0, 200.0, 10.0))));
   }

   private static SoundEffect createChorus() {
      return new SoundEffect(Effect.CHORUS, MESSAGES.get("chorus"), MESSAGES.get("chorus_help"),
            "Chorus(delay1=%.2f; amp1=%.2f; delay2=%.2f; amp2=%.2f; delay3=%.2f; amp3=%.2f;)", createDefaultLevelsMap(
                  Arrays.asList("delay1", "amp1", "delay2", "amp2", "delay3", "amp3"),
                  Arrays.asList(Arrays.asList(466.0, 0.0, 5000.0, 100.0), Arrays.asList(0.54, -5.0, 5.0, 0.1),
                        Arrays.asList(600.0, 0.0, 5000.0, 100.0), Arrays.asList(-0.10, -5.0, 5.0, 0.1),
                        Arrays.asList(250.0, 0.0, 5000.0, 100.0), Arrays.asList(0.30, -5.0, 5.0, 0.1))));
   }

   private static SoundEffect createFirFilter() {
      return new SoundEffect(Effect.FIR_FILTER, MESSAGES.get("fir_filter"), MESSAGES.get("fir_filter_help"),
            "FIRFilter(type=%.2f; fc1=%.2f; fc2=%.2f;)", createDefaultLevelsMap(
                  Arrays.asList("type", "fc1", "fc2"),
                  Arrays.asList(Arrays.asList(3.0, 1.0, 4.0, 1.0), Arrays.asList(500.0, 0.0, 20000.0, 100.0),
                        Arrays.asList(2000.0, 0.0, 20000.0, 100.0))));
   }

   private static Map<String, List<Double>> createDefaultLevelsMap(List<String> keys, List<List<Double>> values) {
      Map<String, List<Double>> defaultLevels = new LinkedHashMap<>();
      for (int i = 0; i < keys.size(); i++) {
         defaultLevels.put(keys.get(i), values.get(i));
      }
      return defaultLevels;
   }

   private String getMaryTTSString() {
      List<Double> values = defaultLevels.keySet().stream().map(k -> getLevel(k)).collect(Collectors.toList());
      return String.format(format, values.toArray());
   }
}
