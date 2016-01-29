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
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;

import org.typetalk.Messages;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class HelpBrowser extends JFrame {

   private static final Messages MESSAGES = Messages.getInstance();
   private static HelpBrowser instance;

   private HelpBrowser() {
      super(MESSAGES.get("client_window_title") + " - " + MESSAGES.get("help"));
      setDefaultCloseOperation(HIDE_ON_CLOSE);
      setSize(600, 600);
      ScreenPositioner.centerOnScreen(this);
      initGui();
   }

   public static HelpBrowser getInstance() {
      if (instance == null) {
         instance = new HelpBrowser();
      }
      return instance;
   }

   private void initGui() {
      JTextPane helpPane = new JTextPane();
      helpPane.setContentType("text/html");
      EditorKit kit = helpPane.getEditorKit();
      Document document = kit.createDefaultDocument();
      try {
         kit.read(getClass().getResourceAsStream("/help.html"), document, 0);
         helpPane.setDocument(document);
      } catch (IOException | BadLocationException e) {
         JOptionPane.showMessageDialog(HelpBrowser.this, MESSAGES.get("load_help_error"), MESSAGES.get("error"),
               JOptionPane.ERROR_MESSAGE);
         log.error("Unable to load help text", e);
      }
      helpPane.setEditable(false);
      helpPane.addHyperlinkListener(l -> {
         if (HyperlinkEvent.EventType.ACTIVATED == l.getEventType()) {
            String description = l.getDescription();
            if (description != null && description.startsWith("#")) {
               helpPane.scrollToReference(description.substring(1));
            }
         }
      });
      JScrollPane scrollPane = new JScrollPane(helpPane);
      getContentPane().add(scrollPane);

      JButton homeButton = new JButton(Icon.getIcon("/icons/house.png"));
      homeButton.addActionListener(a -> {
         helpPane.setCaretPosition(0);
         scrollPane.getVerticalScrollBar().setValue(0);
      });
      getContentPane().add(homeButton, BorderLayout.SOUTH);
   }

   public static void main(String[] args) {
      try {
         UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
            | UnsupportedLookAndFeelException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      new HelpBrowser().setVisible(true);
   }
}
