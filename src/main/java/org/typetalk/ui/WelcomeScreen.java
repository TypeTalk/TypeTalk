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
import java.awt.Dimension;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.jnativehook.keyboard.NativeKeyEvent;
import org.typetalk.Messages;
import org.typetalk.UIProperties;

public class WelcomeScreen extends JFrame {

   private static final Messages MESSAGES = Messages.getInstance();
   private static final UIProperties PROPERTIES = UIProperties.getInstance();
   private int[] nativeHookKeyCodes = PROPERTIES.getNativeHookKeyCodes();

   public WelcomeScreen() {
      setTitle(MESSAGES.get("client_window_title"));
      BorderLayout borderLayout = (BorderLayout) getContentPane().getLayout();
      borderLayout.setVgap(10);
      borderLayout.setHgap(30);

      setResizable(false);
      setAlwaysOnTop(true);
      setUndecorated(true);
      setSize(650, 500);

      getContentPane().add(new JLabel(), BorderLayout.EAST);
      getContentPane().add(new JLabel(), BorderLayout.WEST);

      JPanel centerPanel = new JPanel(new BorderLayout());
      getContentPane().add(centerPanel, BorderLayout.CENTER);

      String nativeHookKeysString = Arrays.stream(nativeHookKeyCodes).mapToObj(kc -> NativeKeyEvent.getKeyText(kc))
            .collect(Collectors.joining(", "));
      String welcomeMessage = String.format(MESSAGES.get("welcome"), nativeHookKeysString);
      JLabel welcomeLabel = new JLabel(welcomeMessage, SwingConstants.CENTER);
      centerPanel.add(welcomeLabel);

      JPanel checkBoxPanel = new JPanel();
      centerPanel.add(checkBoxPanel, BorderLayout.SOUTH);

      JCheckBox chckbxDisableWelcome = new JCheckBox(MESSAGES.get("disable_welcome"));
      checkBoxPanel.add(chckbxDisableWelcome);

      JPanel closePanel = new JPanel();
      getContentPane().add(closePanel, BorderLayout.SOUTH);

      JButton btnCloseButton = new JButton(MESSAGES.get("ok"));
      btnCloseButton.setPreferredSize(new Dimension(100, (int) btnCloseButton.getPreferredSize().getHeight()));
      btnCloseButton.addActionListener(ae -> {
         PROPERTIES.setWelcomeScreenEnabled(!chckbxDisableWelcome.isSelected());
         setVisible(false);
         dispose();
      });
      closePanel.add(btnCloseButton);
   }
}
