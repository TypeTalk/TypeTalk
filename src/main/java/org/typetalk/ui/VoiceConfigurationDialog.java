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

package org.typetalk.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.sound.sampled.AudioInputStream;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import org.typetalk.Messages;
import org.typetalk.TypeTalkProperties;
import org.typetalk.speech.Language;
import org.typetalk.speech.SoundEffect;
import org.typetalk.speech.VoiceDescription;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import marytts.LocalMaryInterface;
import marytts.datatypes.MaryDataType;
import marytts.exceptions.MaryConfigurationException;
import marytts.modules.synthesis.Voice;
import marytts.util.data.audio.AudioPlayer;
import raging.goblin.swingutils.DoubleSlider;
import raging.goblin.swingutils.Icon;
import raging.goblin.swingutils.ScreenPositioner;
import raging.goblin.swingutils.StringSeparator;

@Slf4j
public class VoiceConfigurationDialog extends JDialog {

   private static final Messages MESSAGES = Messages.getInstance();
   private static final TypeTalkProperties PROPERTIES = TypeTalkProperties.getInstance();

   private JComboBox<Language> languagesBox;
   private JComboBox<Voice> voicesBox;
   private JTextField previewField = new JTextField();;
   private List<EffectPanel> effectPanels = new ArrayList<>();
   LocalMaryInterface marytts;

   @Getter
   private boolean okPressed;

   public VoiceConfigurationDialog(JFrame parent) {
      super(parent, MESSAGES.get("voice_config_title"), true);
      setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      setSize(1000, 450);
      ScreenPositioner.centerOnScreen(this);
      initMaryTts();
      initActionsPanel();
      initConfigPanel();
   }

   private void initMaryTts() {
      try {
         marytts = new LocalMaryInterface();
      } catch (MaryConfigurationException e) {
         JOptionPane.showMessageDialog(VoiceConfigurationDialog.this, MESSAGES.get("init_marytts_error"),
               MESSAGES.get("error"), JOptionPane.ERROR_MESSAGE);
         log.error("Unable to start marytts", e);
      }
   }

   private void initActionsPanel() {
      JPanel actionPanel = new JPanel();
      getContentPane().add(actionPanel, BorderLayout.SOUTH);
      FormLayout layout = new FormLayout(
            new ColumnSpec[] { ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC,
                  FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
                  FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(40dlu;default)"),
                  FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
                  ColumnSpec.decode("max(40dlu;default)"), FormFactory.RELATED_GAP_COLSPEC,
                  ColumnSpec.decode("max(5dlu;default)"), },
            new RowSpec[] { FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
                  RowSpec.decode("max(5dlu;default)"), });
      actionPanel.setLayout(layout);

      JButton btnCancel = new JButton(MESSAGES.get("cancel"));
      btnCancel.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            setVisible(false);
         }
      });
      actionPanel.add(btnCancel, "7, 1, 2, 1");

      JButton btnDefaults = new JButton(MESSAGES.get("load_defaults"));
      btnDefaults.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            setDefaults();
         }
      });
      actionPanel.add(btnDefaults, "3, 1");

      JButton btnOk = new JButton(MESSAGES.get("ok"));
      btnOk.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            saveConfiguration();
            okPressed = true;
            setVisible(false);
         }
      });
      actionPanel.add(btnOk, "11, 1, 2, 1");
   }

   private void initConfigPanel() {
      JPanel configPanel = new JPanel();
      getContentPane().add(configPanel, BorderLayout.CENTER);
      configPanel.setLayout(new FormLayout(new ColumnSpec[] {

            ColumnSpec.decode("max(61dlu;min)"), ColumnSpec.decode("max(100dlu;default)"),
            FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("center:150px"), FormFactory.RELATED_GAP_COLSPEC,

            ColumnSpec.decode("max(30dlu;default)"),

            FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
            ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
            FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),

            FormFactory.RELATED_GAP_COLSPEC,

            ColumnSpec.decode("max(15dlu;default)"), FormFactory.RELATED_GAP_COLSPEC,
            ColumnSpec.decode("max(15dlu;default)"), FormFactory.RELATED_GAP_COLSPEC },

            new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
                  FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
                  FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
                  FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
                  FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
                  FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
                  FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
                  FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
                  FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
                  FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

      StringSeparator stringSeparator = new StringSeparator(MESSAGES.get("voice"));
      configPanel.add(stringSeparator, "1, 2, 18, 1");

      languagesBox = new JComboBox<>();
      marytts.getAvailableLocales().stream().filter(l -> !marytts.getAvailableVoices(l).isEmpty())
            .forEach(l -> languagesBox.addItem(new Language(l)));
      configPanel.add(languagesBox, "2, 4, 13, 1");

      voicesBox = new JComboBox<>();
      marytts.getAvailableVoices().forEach(v -> voicesBox.addItem(Voice.getVoice((v))));
      configPanel.add(voicesBox, "2, 6, 13, 1");

      JButton descriptionVoiceButton = new JButton(Icon.getIcon("/icons/help.png"));
      descriptionVoiceButton.addActionListener(a -> JOptionPane.showMessageDialog(VoiceConfigurationDialog.this,
            VoiceDescription.getVoiceDescription(((Voice) voicesBox.getSelectedItem()).getName()).getDescription(),
            ((Voice) voicesBox.getSelectedItem()).getName(), JOptionPane.INFORMATION_MESSAGE));
      configPanel.add(descriptionVoiceButton, "18, 6, center, center");

      languagesBox.addActionListener(a -> loadVoicesToVoicesBox());
      languagesBox.setSelectedItem(
            new Language(new Locale(PROPERTIES.getVoiceLocaleLanguage(), PROPERTIES.getVoiceLocaleCountry())));
      languagesBox.addActionListener(a -> updateExampleText());
      voicesBox.setSelectedItem(PROPERTIES.getVoice());

      StringSeparator effectsSeparator = new StringSeparator(MESSAGES.get("effects"));
      configPanel.add(effectsSeparator, "1, 8, 18, 1");

      configPanel.add(new JLabel(MESSAGES.get("name")), "2, 10, 1, 1");
      configPanel.add(new JLabel(MESSAGES.get("enabled")), "4, 10, 1, 1");
      configPanel.add(new JLabel(MESSAGES.get("parameters")), "6, 10, 8, 1, left, default");

      configPanel.add(new JLabel(" "), "2, 12, 1, 1");

      configPanel.add(new JSeparator(), "2, 13, 17, 1");
      for (int i = 0; i < SoundEffect.getAllSoundEffects().size(); i++) {
         EffectPanel effectPanel = new EffectPanel(SoundEffect.getAllSoundEffects().get(i));
         configPanel.add(effectPanel, String.format("2, %d, 17, 1", i * 2 + 14));
         effectPanels.add(effectPanel);
         if (i < SoundEffect.getAllSoundEffects().size() - 1) {
            configPanel.add(new JSeparator(), String.format("2, %d, 17, 1", i * 2 + 15));
         }
      }

      configPanel.add(new StringSeparator(MESSAGES.get("preview")), "1, 24, 18, 1");

      updateExampleText();
      configPanel.add(previewField, "2, 26, 13, 1");

      JButton previewButton = new JButton(Icon.getIcon("/icons/control_play.png"));
      previewButton.addActionListener(a -> {
         try {
            Voice voice = (Voice) voicesBox.getSelectedItem();
            marytts.setLocale(voice.getLocale());
            marytts.setVoice(voice.getName());
            String effectsString = effectPanels.stream().filter(ep -> ep.isEnabled()).map(ep -> ep.getEffectString())
                  .collect(Collectors.joining("+"));
            marytts.setAudioEffects(effectsString);
            AudioInputStream audio = marytts.generateAudio(previewField.getText().toLowerCase());
            AudioPlayer player = new AudioPlayer(audio);
            player.start();
            player.join();
         } catch (Exception e) {
            JOptionPane.showMessageDialog(VoiceConfigurationDialog.this, MESSAGES.get("init_marytts_error"),
                  MESSAGES.get("error"), JOptionPane.ERROR_MESSAGE);
         }
      });
      configPanel.add(previewButton, "18, 26, center, center");
   }

   private void updateExampleText() {
      previewField.setText(
            MaryDataType.getExampleText(MaryDataType.TEXT, ((Language) languagesBox.getSelectedItem()).getLocale()));
   }

   private void loadVoicesToVoicesBox() {
      voicesBox.removeAllItems();
      marytts.getAvailableVoices(((Language) languagesBox.getSelectedItem()).getLocale())
            .forEach(v -> voicesBox.addItem(Voice.getVoice((v))));
      if (voicesBox.getItemCount() > 0) {
         voicesBox.setSelectedIndex(0);
      }
   }

   private void setDefaults() {
      languagesBox.setSelectedItem(new Language(Locale.getDefault()));
      voicesBox.setSelectedItem(Voice.getDefaultVoice(Locale.getDefault()));
      effectPanels.forEach(ep -> ep.setDefaults());
   }

   private void saveConfiguration() {
      PROPERTIES.setVoiceLocaleLanguage(((Language) languagesBox.getSelectedItem()).getLocale().getLanguage());
      PROPERTIES.setVoiceLocaleCountry(((Language) languagesBox.getSelectedItem()).getLocale().getCountry());
      PROPERTIES.setVoice((voicesBox.getSelectedItem().toString()));
      effectPanels.forEach(ep -> ep.saveEffect());
   }

   private class EffectPanel extends JPanel {

      private SoundEffect soundEffect;
      private JCheckBox enabledBox;
      private List<LevelPanel> levelPanels = new ArrayList<>();

      public EffectPanel(SoundEffect soundEffect) {
         this.soundEffect = soundEffect;
         initPanel();
      }

      public void setDefaults() {
         enabledBox.setSelected(false);
         levelPanels.forEach(lp -> lp.setDefaultLevel());
      }

      public void saveEffect() {
         soundEffect.setEnabled(enabledBox.isSelected());
         levelPanels.forEach(lp -> lp.saveValue());
      }

      public String getEffectString() {
         List<Double> values = levelPanels.stream().map(lp -> (lp.getLevelSlider().getDoubleValue()))
               .collect(Collectors.toList());
         return String.format(soundEffect.getFormat(), values.toArray());
      }

      @Override
      public boolean isEnabled() {
         return enabledBox.isSelected();
      }

      private void initPanel() {
         setLayout(new FormLayout(new ColumnSpec[] {

               ColumnSpec.decode("max(100dlu;default)"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("150px"),
               FormFactory.RELATED_GAP_COLSPEC,

               ColumnSpec.decode("max(30dlu;default)"),

               FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
               ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
               FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),

               FormFactory.RELATED_GAP_COLSPEC,

               ColumnSpec.decode("max(15dlu;default)"), FormFactory.RELATED_GAP_COLSPEC,
               ColumnSpec.decode("max(15dlu;default)") },

               new RowSpec[] { FormFactory.DEFAULT_ROWSPEC }));

         JLabel l = new JLabel(soundEffect.getName());
         add(l, "1, 1, left, top");

         enabledBox = new JCheckBox();
         enabledBox.setSelected(soundEffect.isEnabled());
         enabledBox.addChangeListener(cl -> levelPanels.forEach(lp -> lp.setEnabled(enabledBox.isSelected())));
         add(enabledBox, "3, 1, center, top");

         JButton helpButton = new JButton(Icon.getIcon("/icons/help.png"));
         helpButton.addActionListener(al -> {
            JOptionPane.showMessageDialog(null, soundEffect.getHelpText(), soundEffect.getName(),
                  JOptionPane.INFORMATION_MESSAGE);
         });
         add(helpButton, "17, 1, center, top");

         JPanel levelsPanel = new JPanel();
         levelsPanel.setLayout(new BoxLayout(levelsPanel, BoxLayout.PAGE_AXIS));
         List<String> subEffectsKeys = soundEffect.getSubEffectKeys();
         for (int i = 0; i < subEffectsKeys.size(); i++) {
            LevelPanel levelPanel = new LevelPanel(soundEffect, subEffectsKeys.get(i));
            levelsPanel.add(levelPanel);
            levelPanels.add(levelPanel);
         }
         CellConstraints cc = new CellConstraints();
         add(levelsPanel, cc.xyw(5, 1, 10, "fill, top"));
         levelPanels.forEach(lp -> lp.setEnabled(enabledBox.isSelected()));
      }
   }

   private class LevelPanel extends JPanel {

      private SoundEffect soundEffect;
      private String subEffectKey;
      private JLabel lowerBoundLabel;
      private JLabel higherBoundLabel;
      private JLabel subEffectNameLabel;

      @Getter
      private DoubleSlider levelSlider;

      public LevelPanel(SoundEffect soundEffect, String subEffectKey) {
         this.soundEffect = soundEffect;
         this.subEffectKey = subEffectKey;
         initUi();
      }

      @Override
      public void setEnabled(boolean enabled) {
         lowerBoundLabel.setEnabled(enabled);
         higherBoundLabel.setEnabled(enabled);
         subEffectNameLabel.setEnabled(enabled);
         levelSlider.setEnabled(enabled);
      }

      public void setDefaultLevel() {
         levelSlider.setDoubleValue(soundEffect.getDefaultLevel(subEffectKey));
      }

      private void initUi() {
         this.setLayout(new FormLayout(
               new ColumnSpec[] { ColumnSpec.decode("default"), ColumnSpec.decode("default"),
                     ColumnSpec.decode("default:grow"), ColumnSpec.decode("default") },
               new RowSpec[] { FormFactory.DEFAULT_ROWSPEC }));
         lowerBoundLabel = new JLabel(soundEffect.getSubEffects().get(subEffectKey).getLowerBoundName());
         higherBoundLabel = new JLabel(soundEffect.getSubEffects().get(subEffectKey).getHigherBoundName());
         subEffectNameLabel = new JLabel(soundEffect.getSubEffects().get(subEffectKey).getName() + ": ");
         levelSlider = new DoubleSlider(soundEffect.getLevel(subEffectKey), soundEffect.getMinimumValue(subEffectKey),
               soundEffect.getMaximumValue(subEffectKey), soundEffect.getStepSize(subEffectKey));
         levelSlider.addChangeListener(cl -> saveValue());
         add(lowerBoundLabel, "1, 1");
         add(higherBoundLabel, "4, 1");
         if (soundEffect.getSubEffects().size() == 1) {
            add(levelSlider, "2, 1, 2, 1");
         } else {
            add(subEffectNameLabel, "2 ,1");
            add(levelSlider, "3, 1");
         }
      }

      public void saveValue() {
         soundEffect.setLevel(subEffectKey, levelSlider.getDoubleValue());
      }
   }

   public static void main(String[] args) throws MalformedURLException, NoSuchMethodException, SecurityException,
         IllegalAccessException, IllegalArgumentException, InvocationTargetException {
      new VoiceConfigurationDialog(null).setVisible(true);
   }
}