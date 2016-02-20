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
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import org.typetalk.Messages;

import com.google.common.collect.ImmutableMap;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class SoundEffect {

   public enum Effect {
      TRACT_SCALER, F0_SCALE, F0_ADD, RATE, ROBOT, WHISPER, STADIUM, CHORUS, FIR_FILTER
   };

   private static final Preferences PREFERENCES = Preferences.userNodeForPackage(SoundEffect.class);
   private static final Messages MESSAGES = Messages.getInstance();
   private static final List<SoundEffect> EFFECTS = new ArrayList<>();

   private Effect effect;
   private String name;
   private String helpText;
   private String format;
   private Map<String, SubEffect> subEffects;

   public static List<SoundEffect> getAllSoundEffects() {
      if (EFFECTS.isEmpty()) {
         EFFECTS.add(createTractScaler());
         EFFECTS.add(createF0Add());
         EFFECTS.add(createF0Scale());
         EFFECTS.add(createRate());
      }
      return EFFECTS;
   }

   public static String toMaryTTSString() {
      return getAllSoundEffects().stream().filter(e -> e.isEnabled()).map(e -> e.getMaryTTSString())
            .collect(Collectors.joining("+"));
   }

   public List<String> getSubEffectKeys() {
      return subEffects.keySet().stream().map(k -> subEffects.get(k).getKey()).collect(Collectors.toList());
   }

   public void setLevel(String key, double value) {
      if (subEffects.containsKey(key)) {
         PREFERENCES.putDouble(effect + "_" + key, value);
      }
   }

   public double getLevel(String key) {
      return PREFERENCES.getDouble(effect + "_" + key, subEffects.get(key).getDefaultValue());
   }

   public double getDefaultLevel(String key) {
      return subEffects.get(key).getDefaultValue();
   }

   public double getMinimumValue(String key) {
      return subEffects.get(key).getMinimumValue();
   }

   public double getMaximumValue(String key) {
      return subEffects.get(key).getMaximumValue();
   }

   public double getStepSize(String key) {
      return subEffects.get(key).getStepSize();
   }

   public void setDefaultLevels() {
      subEffects.keySet().forEach(k -> setLevel(k, subEffects.get(k).getDefaultValue()));
   }

   public boolean isEnabled() {
      return PREFERENCES.getBoolean(effect.name() + "_enabled", false);
   }

   public void setEnabled(boolean enabled) {
      PREFERENCES.putBoolean(effect.name() + "_enabled", enabled);
   }

   private static SoundEffect createTractScaler() {
      SubEffect subEffect = new SubEffect(1.5, 0.25, 4.0, 0.1, "amount", MESSAGES.get("tract_scaler_subeffect_name"),
            MESSAGES.get("tract_scaler_subeffect_lowerboundname"),
            MESSAGES.get("tract_scaler_subeffect_higherboundname"));
      return new SoundEffect(Effect.TRACT_SCALER, MESSAGES.get("tract_scaler"), MESSAGES.get("tract_scaler_help"),
            "TractScaler(amount=%.2f;)", ImmutableMap.of(subEffect.getKey(), subEffect));
   }

   private static SoundEffect createF0Scale() {
      SubEffect subEffect = new SubEffect(2.0, 0.0, 3.0, 0.1, "f0Scale", MESSAGES.get("f0_scaling"),
            MESSAGES.get("f0_scaling_subeffect_lowerboundname"), MESSAGES.get("f0_scaling_subeffect_higherboundname"));
      return new SoundEffect(Effect.F0_SCALE, MESSAGES.get("f0_scaling"), MESSAGES.get("f0_scaling_help"),
            "F0Scale(f0Scale=%.2f;)", ImmutableMap.of(subEffect.getKey(), subEffect));
   }

   private static SoundEffect createF0Add() {
      SubEffect subEffect = new SubEffect(50.0, -300.0, 300.0, 10.0, "f0_add", MESSAGES.get("f0_add"),
            MESSAGES.get("f0_add_subeffect_lowerboundname"), MESSAGES.get("f0_add_subeffect_higherboundname"));
      return new SoundEffect(Effect.F0_ADD, MESSAGES.get("f0_add"), MESSAGES.get("f0_add_help"), "F0Add(f0Add=%.2f;)",
            ImmutableMap.of(subEffect.getKey(), subEffect));
   }

   private static SoundEffect createRate() {
      SubEffect subEffect = new SubEffect(1.5, 0.1, 3.0, 0.1, "durScale", MESSAGES.get("rate"),
            MESSAGES.get("rate_subeffect_lowerboundname"), MESSAGES.get("rate_subeffect_higherboundname"));
      return new SoundEffect(Effect.RATE, MESSAGES.get("rate"), MESSAGES.get("rate_help"), "Rate(durScale=%.2f;)",
            ImmutableMap.of(subEffect.getKey(), subEffect));
   }

   private String getMaryTTSString() {
      List<Double> values = subEffects.keySet().stream().map(k -> getLevel(k)).collect(Collectors.toList());
      return String.format(format, values.toArray());
   }

   @AllArgsConstructor
   @Getter
   public static class SubEffect {

      private double defaultValue;
      private double minimumValue;
      private double maximumValue;
      private double stepSize;
      private String key;
      private String name;
      private String lowerBoundName;
      private String higherBoundName;

   }
}
