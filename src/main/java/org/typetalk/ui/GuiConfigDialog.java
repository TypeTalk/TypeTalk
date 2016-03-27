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
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.jnativehook.keyboard.NativeKeyEvent;
import org.typetalk.Messages;
import org.typetalk.TypeTalkProperties;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import lombok.Getter;
import raging.goblin.swingutils.Icon;
import raging.goblin.swingutils.LookAndFeel;
import raging.goblin.swingutils.ScreenPositioner;
import raging.goblin.swingutils.StringSeparator;

public class GuiConfigDialog extends JDialog {

   private static final Messages MESSAGES = Messages.getInstance();
   private static final TypeTalkProperties PROPERTIES = TypeTalkProperties.getInstance();

   @Getter
   private boolean okPressed;
   @Getter
   private boolean settingsChanged;
   private int[] nativeHookKeyCodes = PROPERTIES.getNativeHookKeyCodes();

   private JCheckBox chckbxSplashScreenEnabled;
   private JCheckBox chckbxWelcomeScreenEnabled;
   private JCheckBox chckbxStartMinimized;
   private JLabel nativeHookLabel;
   private JComboBox<LookAndFeel> lafComboBox;

   public GuiConfigDialog(JFrame parent) {
      super(parent, MESSAGES.get("gui_config_title"), true);
      setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      setSize(450, 450);
      ScreenPositioner.centerOnScreen(this);
      initActionsPanel();
      initConfigPanel(parent);
   }

   private void initConfigPanel(JFrame parent) {
      JPanel configPanel = new JPanel();
      configPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
      getContentPane().add(configPanel, BorderLayout.CENTER);
      configPanel
            .setLayout(
                  new FormLayout(
                        new ColumnSpec[] { ColumnSpec.decode("default:grow"), ColumnSpec.decode("default:grow"),
                              ColumnSpec.decode("default:grow"), ColumnSpec.decode("right:default"),
                              ColumnSpec.decode("right:default"),
                              ColumnSpec
                                    .decode("right:default") },
            new RowSpec[] { FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
                  FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
                  FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
                  FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
                  FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
                  FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
                  FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
                  FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
                  FormFactory.DEFAULT_ROWSPEC, RowSpec.decode("default:grow"), }));

      StringSeparator lafSeparator = new StringSeparator(MESSAGES.get("laf"));
      configPanel.add(lafSeparator, "1, 7, 5, 1");

      LookAndFeel[] allLafs = LookAndFeel.getAll();
      lafComboBox = new JComboBox<LookAndFeel>(allLafs);
      LookAndFeel selectedLaf = Arrays.stream(allLafs).filter(l -> l.getName().equals(PROPERTIES.getLaf())).findFirst()
            .get();
      lafComboBox.setSelectedItem(selectedLaf);
      configPanel.add(lafComboBox, "3, 9, 3, 1");

      StringSeparator splashScreenSeparator = new StringSeparator(MESSAGES.get("splash_screen"));
      configPanel.add(splashScreenSeparator, "1, 7, 5, 1");

      JLabel lblSplashScreenEnabled = new JLabel(MESSAGES.get("enabled"));
      configPanel.add(lblSplashScreenEnabled, "3, 9");

      chckbxSplashScreenEnabled = new JCheckBox();
      chckbxSplashScreenEnabled.setSelected(PROPERTIES.isSplashScreenEnabled());
      configPanel.add(chckbxSplashScreenEnabled, "4, 9");

      StringSeparator welcomeScreenSeparator = new StringSeparator(MESSAGES.get("welcome_screen"));
      configPanel.add(welcomeScreenSeparator, "1, 11, 5, 1");

      JLabel lblShowWelcomeScreen = new JLabel(MESSAGES.get("enabled"));
      configPanel.add(lblShowWelcomeScreen, "4, 13");

      chckbxWelcomeScreenEnabled = new JCheckBox();
      chckbxWelcomeScreenEnabled.setSelected(PROPERTIES.isWelcomeScreenEnabled());
      configPanel.add(chckbxWelcomeScreenEnabled, "5, 13");

      StringSeparator startMinimizedSeparator = new StringSeparator(MESSAGES.get("start_minimized"));
      configPanel.add(startMinimizedSeparator, "1, 15, 5, 1");

      JLabel lblStartMinized = new JLabel(MESSAGES.get("enabled"));
      configPanel.add(lblStartMinized, "4, 17");

      chckbxStartMinimized = new JCheckBox();
      chckbxStartMinimized.setSelected(PROPERTIES.isStartMinimized());
      configPanel.add(chckbxStartMinimized, "5, 17");

      StringSeparator nativeHookSeparator = new StringSeparator(MESSAGES.get("native_hook"));
      configPanel.add(nativeHookSeparator, "1, 19, 5, 1");

      nativeHookLabel = new JLabel(getNativeHookText(nativeHookKeyCodes));
      configPanel.add(nativeHookLabel, "2, 23");

      JButton btnRecordNativeHook = new JButton(Icon.getIcon("/icons/pencil.png"));
      configPanel.add(btnRecordNativeHook, "4, 23, 2, 1, fill, default");
      btnRecordNativeHook.addActionListener(e -> {
         RecordNativeHookDialog dialog = new RecordNativeHookDialog(parent);
         dialog.setVisible(true);
         if (dialog.isOkButtonPressed()) {
            nativeHookKeyCodes = dialog.getNativeHookKeyCodes();
            nativeHookLabel.setText(getNativeHookText(nativeHookKeyCodes));
         }
         dialog.dispose();
      });
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
      btnCancel.addActionListener(e -> setVisible(false));
      actionPanel.add(btnCancel, "7, 1, 2, 1");

      JButton btnOk = new JButton(MESSAGES.get("ok"));
      btnOk.addActionListener(e -> {
         checkSettingsChanged();
         saveConfiguration();
         okPressed = true;
         setVisible(false);
      });
      actionPanel.add(btnOk, "11, 1, 2, 1");
   }

   private void saveConfiguration() {
      PROPERTIES.setSplashScreenEnabled(chckbxSplashScreenEnabled.isSelected());
      PROPERTIES.setWelcomeScreenEnabled(chckbxWelcomeScreenEnabled.isSelected());
      PROPERTIES.setStartMinimized(chckbxStartMinimized.isSelected());
      PROPERTIES.setNativeHookEnabled(nativeHookKeyCodes.length > 0);
      PROPERTIES.setNativeHookKeyCodes(nativeHookKeyCodes);
      PROPERTIES.setLaf(((LookAndFeel) lafComboBox.getSelectedItem()).getName());
   }

   private void checkSettingsChanged() {
      settingsChanged = !Arrays.equals(PROPERTIES.getNativeHookKeyCodes(), nativeHookKeyCodes)
            || !((LookAndFeel) lafComboBox.getSelectedItem()).getName().equals(PROPERTIES.getLaf());
   }

   private String getNativeHookText(int[] nativeHookKeyCodes) {
      return Arrays.stream(nativeHookKeyCodes).mapToObj(kc -> NativeKeyEvent.getKeyText(kc))
            .collect(Collectors.joining(", "));
   }
}
