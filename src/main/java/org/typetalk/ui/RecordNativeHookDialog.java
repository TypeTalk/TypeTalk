/*
 * Copyright 2016, TypeTalk <http://typetalk.github.io/TypeTalk>
 * 
 * This file is part of OcNotes.
 *
 *  OcNotes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  OcNotes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with TypeTalk.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.typetalk.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.typetalk.Messages;
import org.typetalk.TypeTalkProperties;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import raging.goblin.swingutils.ScreenPositioner;

public class RecordNativeHookDialog extends JDialog {

   private static final Messages MESSAGES = Messages.getInstance();
   private static final TypeTalkProperties PROPERTIES = TypeTalkProperties.getInstance();

   private boolean okButtonPressed = false;
   private List<Integer> nativeHookKeyCodes = new ArrayList<>();
   private boolean recording = false;
   private JLabel nativeKeyCodesLabel;

   public RecordNativeHookDialog(JFrame parent) {
      super(parent, MESSAGES.get("record_native_hook_title"), true);
      nativeHookKeyCodes = IntStream.of(PROPERTIES.getNativeHookKeyCodes()).boxed().collect(Collectors.toList());
      setSize(450, 300);
      ScreenPositioner.centerOnScreen(this);
      initGui();
      initNativeHook();
   }

   private void initGui() {
      JLabel instructionsLabel = new JLabel(MESSAGES.get("record_shortcut"));
      instructionsLabel.setPreferredSize(new Dimension(100, 100));
      instructionsLabel.setHorizontalAlignment(SwingConstants.CENTER);

      getContentPane().add(instructionsLabel, BorderLayout.NORTH);

      JPanel actionPanel = new JPanel();
      getContentPane().add(actionPanel, BorderLayout.SOUTH);
      actionPanel.setLayout(new FormLayout(
            new ColumnSpec[] { ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC,
                  ColumnSpec.decode("max(40dlu;default)"), FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
                  FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(40dlu;default)"),
                  FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(5dlu;default)"), },
            new RowSpec[] { FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
                  FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("max(5dlu;default)"), }));

      JButton btnCancel = new JButton(MESSAGES.get("cancel"));
      btnCancel.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            okButtonPressed = false;
            setVisible(false);
         }
      });
      actionPanel.add(btnCancel, "3, 3, 2, 1");

      JButton btnOk = new JButton(MESSAGES.get("ok"));
      btnOk.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            okButtonPressed = true;
            setVisible(false);
         }
      });
      actionPanel.add(btnOk, "7, 3, 2, 1");

      nativeKeyCodesLabel = new JLabel(
            nativeHookKeyCodes.stream().map(kc -> NativeKeyEvent.getKeyText(kc)).collect(Collectors.joining(", ")));
      nativeKeyCodesLabel.setHorizontalAlignment(SwingConstants.CENTER);
      add(nativeKeyCodesLabel, BorderLayout.CENTER);
   }

   private void initNativeHook() {
      GlobalScreen.addNativeKeyListener(new NativeKeyListener() {

         @Override
         public void nativeKeyTyped(NativeKeyEvent e) {
            // Nothing todo
         }

         @Override
         public void nativeKeyReleased(NativeKeyEvent e) {
            recording = false;
         }

         @Override
         public void nativeKeyPressed(NativeKeyEvent e) {
            if (!recording) {
               recording = true;
               nativeHookKeyCodes.clear();
            }

            String keyCodesString = "";
            if (e.getKeyCode() == 14 || e.getKeyCode() == 3667) {
               nativeHookKeyCodes.clear();
            } else {
               nativeHookKeyCodes.add(e.getKeyCode());
               keyCodesString = nativeHookKeyCodes.stream().map(kc -> NativeKeyEvent.getKeyText(kc))
                     .collect(Collectors.joining(", "));
            }
            nativeKeyCodesLabel.setText(keyCodesString);
         }
      });
   }

   public boolean isOkButtonPressed() {
      return okButtonPressed;
   }

   public int[] getNativeHookKeyCodes() {
      return nativeHookKeyCodes.stream().mapToInt(i -> i).toArray();
   }
}
