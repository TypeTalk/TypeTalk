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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.typetalk.Messages;
import org.typetalk.ui.ToastWindow;

import lombok.extern.slf4j.Slf4j;
import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.exceptions.MaryConfigurationException;
import marytts.exceptions.SynthesisException;
import marytts.util.data.audio.AudioPlayer;

@Slf4j
public class Speeker {

   private static final Messages MESSAGES = Messages.getInstance();

   private volatile boolean speeking;
   private boolean isLastSentence;
   private boolean isLastWord;
   private AudioPlayer currentlySpeeking;
   private Set<EndOfSpeechListener> endOfSpeechListeners = new HashSet<>();
   private MaryInterface marytts;

   public Speeker() throws MaryConfigurationException {
      initMaryTTS();
   }

   public void stop() {
      log.info("Stopping Speeker");
      stopSpeeking();
   }

   public void stopSpeeking() {
      speeking = false;
      if (currentlySpeeking != null) {
         currentlySpeeking.cancel();
      }
   }

   public void speek(List<String> speeches) {
      new Thread() {

         @Override
         public void run() {
            speeking = true;
            isLastSentence = false;
            isLastWord = false;

            for (int i = 0; i < speeches.size(); i++) {
               if (i == speeches.size() - 1) {
                  isLastSentence = true;
               }
               if (speeking) {
                  speek(speeches.get(i).trim().toLowerCase());
               }
            }
         };
      }.start();
   }

   public void save(String speech, File file) {
      new Thread("Saving speeches") {

         @Override
         public void run() {
            if (!speech.trim().isEmpty()) {
               ToastWindow toast = ToastWindow.showToast(MESSAGES.get("saving"), false);
               try {
                  AudioInputStream audio = marytts.generateAudio(speech.toLowerCase());
                  AudioSystem.write(audio, AudioFileFormat.Type.WAVE, file);
                  toast.setVisible(false);
                  toast.dispose();
                  ToastWindow.showToast(MESSAGES.get("ready_saving"), true);
               } catch (SynthesisException | IOException e) {
                  log.error("Unable to save speech", e);
                  toast.setVisible(false);
                  toast.dispose();
                  SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,
                        MESSAGES.get("offending_speech"), MESSAGES.get("error"), JOptionPane.ERROR_MESSAGE));
               }
            }
         }
      }.start();
   }

   public void initMaryTTS() throws MaryConfigurationException {
      log.debug("Initialiazing MaryTTS");
      marytts = new LocalMaryInterface();

      String voice = Voice.getSelectedVoice().getName();
      log.debug("Setting voice to: " + voice);
      marytts.setVoice(voice);

      String effects = SoundEffect.toMaryTTSString();
      log.debug("Setting effects to: " + effects);
      marytts.setAudioEffects(effects);
   }

   public void addEndOfSpeechListener(EndOfSpeechListener listener) {
      endOfSpeechListeners.add(listener);
   }

   private void speek(String speech) {
      log.debug("Preparing to speek: " + speech);

      try {
         executeSpeaking(speech);

      } catch (Exception e) {
         log.error("Unable to speek: " + speech, e);
         List<String> words = Arrays.asList(speech.split(" "));
         if (words.size() > 1) {
            speekWordsSeparately(words);
         } else if (words.size() == 1) {
            ToastWindow.showToast(String.format(MESSAGES.get("offending_word"), words.get(0)), true);
         }

      } finally {
         if (readyWithSpeeking()) {
            notifyEndOfSpeechListeners();
         }
      }
   }

   private void speekWordsSeparately(List<String> words) {
      if (isLastSentence) {
         isLastSentence = false;
         for (int i = 0; i < words.size(); i++) {
            if (i == words.size() - 1) {
               isLastWord = true;
            }
            speek(words.get(i));
         }

      } else {
         words.stream().forEach(w -> speek(w));
      }
   }

   private void executeSpeaking(String speech) throws SynthesisException, InterruptedException {
      AudioInputStream audio = marytts.generateAudio(speech);
      currentlySpeeking = new AudioPlayer(audio);
      log.debug("Speeking: " + speech);
      currentlySpeeking.start();
      currentlySpeeking.join();
   }

   private boolean readyWithSpeeking() {
      return isLastSentence || isLastWord;
   }

   private void notifyEndOfSpeechListeners() {
      endOfSpeechListeners.stream().forEach(l -> l.endOfSpeech());
   }

   public interface EndOfSpeechListener {
      void endOfSpeech();
   }
}
