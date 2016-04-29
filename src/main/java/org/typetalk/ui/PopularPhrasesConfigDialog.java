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
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.typetalk.Messages;
import org.typetalk.TypeTalkProperties;
import org.typetalk.speech.Speeker;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import marytts.exceptions.MaryConfigurationException;
import raging.goblin.swingutils.Icon;
import raging.goblin.swingutils.ScreenPositioner;

@Slf4j
public class PopularPhrasesConfigDialog extends JDialog {

   private static final Messages MESSAGES = Messages.getInstance();
   private static final TypeTalkProperties PROPERTIES = TypeTalkProperties.getInstance();

   @Getter
   private boolean okPressed;
   private List<JTextField> textFields = new ArrayList<>();
   private Speeker speeker;

   public PopularPhrasesConfigDialog(JFrame parent) {
      super(parent, MESSAGES.get("popular_phrases_config_title"), true);
      setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      initSpeeker();
      initActionsPanel();
      initConfigPanel(parent);
      setSize(800, 450);
      ScreenPositioner.centerOnScreen(this);
   }

   private void initSpeeker() {
      try {
         speeker = new Speeker();
      } catch (MaryConfigurationException e) {
         log.error("Unable to start configure popular phrases dialog", e);
         JOptionPane.showMessageDialog(PopularPhrasesConfigDialog.this, MESSAGES.get("init_marytts_error"),
               MESSAGES.get("error"), JOptionPane.ERROR_MESSAGE);
      }
   }

   private void initConfigPanel(JFrame parent) {
      JPanel configPanel = new JPanel(new GridLayout(0, 1));
      configPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
      getContentPane().add(configPanel, BorderLayout.CENTER);
      for (int i = 0; i < PROPERTIES.getPopularPhrases().length; i++) {
         configPanel.add(createPopularPhrasePanel(i));
      }
   }

   private JPanel createPopularPhrasePanel(int index) {
      JPanel popularPhrasePanel = new JPanel();
      popularPhrasePanel.setLayout(new FormLayout(
            new ColumnSpec[] { ColumnSpec.decode("default"), FormFactory.RELATED_GAP_COLSPEC,
                  ColumnSpec.decode("default:grow"), FormFactory.DEFAULT_COLSPEC, ColumnSpec.decode("default") },
            new RowSpec[] { FormFactory.DEFAULT_ROWSPEC }));

      popularPhrasePanel.add(new JLabel("" + (index + 1)), "1, 1");

      textFields.add(new JTextField(PROPERTIES.getPopularPhrase(index)));
      popularPhrasePanel.add(textFields.get(index), "3, 1");

      JButton playButton = new JButton(Icon.getIcon("/icons/control_play.png"));
      playButton.addActionListener(a -> speeker.speek(Arrays.asList(textFields.get(index).getText())));
      popularPhrasePanel.add(playButton, "5, 1");
      return popularPhrasePanel;
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
         saveConfiguration();
         okPressed = true;
         setVisible(false);
      });
      actionPanel.add(btnOk, "11, 1, 2, 1");
   }

   private void saveConfiguration() {
      for (int i = 0; i < PROPERTIES.getPopularPhrases().length; i++) {
         PROPERTIES.setPopularPhrase(textFields.get(i).getText().trim(), i);
      }
   }
}
