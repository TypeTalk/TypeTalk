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
import java.awt.Frame;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.typetalk.Messages;

import raging.goblin.swingutils.Icon;
import raging.goblin.swingutils.ScreenPositioner;

public class AboutDialog extends JDialog {

   private static final Messages MESSAGES = Messages.getInstance();

   public AboutDialog(Frame parent) {
      super(parent, true);
      setDefaultCloseOperation(DISPOSE_ON_CLOSE);
      setTitle(MESSAGES.get("client_window_title"));

      setSize(610, 510);
      ScreenPositioner.centerOnScreen(this);

      JPanel topPanel = new JPanel();
      getContentPane().add(topPanel, BorderLayout.NORTH);
      JLabel iconLabel = new JLabel(Icon.getIcon("/icons/sound.png"));
      topPanel.add(iconLabel);
      topPanel.add(new JLabel(MESSAGES.get("about_title")));

      JPanel centerPanel = new JPanel();
      getContentPane().add(centerPanel, BorderLayout.CENTER);
      centerPanel.add(new JLabel(MESSAGES.get("about_text")));
   }
}
