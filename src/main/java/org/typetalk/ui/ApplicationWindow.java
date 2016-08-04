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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.JTextComponent;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.typetalk.Messages;
import org.typetalk.TypeTalkProperties;
import org.typetalk.data.Suggestions;
import org.typetalk.speech.Speeker;
import org.typetalk.speech.Speeker.EndOfSpeechListener;
import org.typetalk.speech.Speeker.SpeechListener;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import fr.pud.client.view.jsuggestfield.JSuggestField;
import lombok.extern.slf4j.Slf4j;
import marytts.exceptions.MaryConfigurationException;
import raging.goblin.swingutils.HelpBrowser;
import raging.goblin.swingutils.Icon;
import raging.goblin.swingutils.ScreenPositioner;

@Slf4j
public class ApplicationWindow extends JFrame implements EndOfSpeechListener, SpeechListener {

   private static final Messages MESSAGES = Messages.getInstance();
   private static final TypeTalkProperties PROPERTIES = TypeTalkProperties.getInstance();

   private Dimension expandedSize = new Dimension(600, 440);
   private Dimension collapsedSize = new Dimension(600, 80);
   private int expandedContentPaneHeight = 415;
   private int collapsedContentPaneHeight = 55;
   private boolean firstTimeVisible = true;
   private boolean speeking = false;

   private Speeker speeker;

   private JPanel expandedPanel;
   private JPanel collapsedPanel;
   private JTextField typingField;
   private JLayeredPane speakingPane;
   private JPanel glassPanel;
   private JTextArea speakingArea;
   private JButton saveButton;
   private JButton playButton;
   private JMenuItem playMenuItem;
   private JButton stopButton;
   private JMenuItem stopMenuItem;
   private JPanel parseTextButtonPanel;
   private JButton collapseExpandButton;
   private JMenuItem expandItem;
   private JMenuItem collapseItem;
   private List<JButton> popularPhrasesButtons;
   private List<ActionListener> popularPhrasesListeners;
   private JPanel popularPhrasesPanel;

   private ActionListener collapseExpandListener = al -> {
      if (PROPERTIES.isScreenCollapsed()) {
         expand();
      } else {
         collapse();
      }
      PROPERTIES.setScreenCollapsed(!PROPERTIES.isScreenCollapsed());
   };

   private ActionListener playListener = al -> {
      speeking = true;
      updateGuiSpeekingState();
      List<String> speeches = Arrays.asList(speakingArea.getText().split("\\."));
      if (speeches.size() < 1) {
         speeking = false;
         updateGuiSpeekingState();
         typingField.grabFocus();
      } else {
         speakingArea.setCaretPosition(0);
         speeker.speek(speeches);
      }
   };

   private ActionListener stopListener = al -> {
      speeker.stopSpeeking();
      speeking = false;
      updateGuiSpeekingState();
   };

   private ActionListener saveListener = al -> {
      JFileChooser chooser = new JFileChooser();
      chooser.setDialogTitle(MESSAGES.get("save_to_wav"));
      chooser.setFileFilter(new WaveFilter());
      int returnValue = chooser.showOpenDialog(ApplicationWindow.this);
      if (returnValue == JFileChooser.APPROVE_OPTION) {
         File file = chooser.getSelectedFile();
         if (!file.getName().endsWith("wav") || !file.getName().endsWith("WAV")) {
            file = new File(file.getAbsolutePath() + ".wav");
         }
         speeker.save(speakingArea.getText(), file);
      }
   };

   public ApplicationWindow(Speeker speeker) throws MaryConfigurationException {
      super(MESSAGES.get("client_window_title"));
      this.speeker = speeker;
      speeker.addEndOfSpeechListener(this);
      speeker.addSpeechListener(this);
      setDefaultCloseOperation(EXIT_ON_CLOSE);
      if (PROPERTIES.isNativeHookEnabled()) {
         initNativeHook();
      }
      initGui();
   }

   @Override
   public void endOfSpeech() {
      SwingUtilities.invokeLater(() -> {
         speeking = false;
         updateGuiSpeekingState();
         typingField.grabFocus();
      });
   }

   @Override
   public void currentlySpeeking(String speech) {
      SwingUtilities.invokeLater(() -> {
         int startIndex = speakingArea.getText().indexOf(speech);
         speakingArea.setSelectionStart(startIndex);
         speakingArea.setSelectionEnd(startIndex + speech.length());
      });
   }

   @Override
   public void setVisible(boolean visible) {
      super.setVisible(visible);
      calculateScreenSize();
      typingField.grabFocus();
   }

   private void initGui() {
      initSpeakingArea();
      initTypingField();
      initRightButtonPanel();
      initPopularPhrasesListeners();
      initPopularPhrasesButtonPanel();
      initCollapseExpandButton();

      setIconImage(Icon.getIcon("/icons/sound.png").getImage());
      setJMenuBar(createMenu());
      Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
      ((JComponent) getContentPane()).setBorder(padding);

      expandedPanel = new JPanel();
      expandedPanel.setLayout(new FormLayout(
            new ColumnSpec[] { ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC,
                  ColumnSpec.decode("50px") },
            new RowSpec[] { RowSpec.decode("fill:default:grow"), FormFactory.RELATED_GAP_ROWSPEC,
                  FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC }));
      expandedPanel.add(parseTextButtonPanel, "3, 1");
      expandedPanel.add(speakingPane, "1, 1");

      collapsedPanel = new JPanel();
      collapsedPanel.setLayout(
            new FormLayout(new ColumnSpec[] { ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC,
                  ColumnSpec.decode("50px") }, new RowSpec[] { FormFactory.DEFAULT_ROWSPEC }));

      if (PROPERTIES.isScreenCollapsed()) {
         collapse();
      } else {
         expand();
      }

      ScreenPositioner.centerOnScreen(this);
      addGlobalKeyAdapters(typingField, speakingArea, saveButton, playButton, stopButton, collapseExpandButton);
      popularPhrasesButtons.forEach(b -> addGlobalKeyAdapters(b));
   }

   private void initSpeakingArea() {
      speakingArea = new JTextArea();

      speakingArea.setWrapStyleWord(true);
      speakingArea.setLineWrap(true);
      JScrollPane scrollPane = new JScrollPane(speakingArea);
      scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      speakingArea.addKeyListener(new KeyAdapter() {

         @Override
         public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_TAB) {
               if (e.isShiftDown()) {
                  typingField.grabFocus();
               } else {
                  saveButton.grabFocus();
               }
               e.consume();
            }
         }
      });
      new EditAdapter(speakingArea);

      speakingPane = new JLayeredPane();
      speakingPane.add(scrollPane, new Integer(1));

      glassPanel = new JPanel();
      glassPanel.setOpaque(false);
      glassPanel.addMouseListener(new MouseAdapter() {
        
         @Override
         public void mousePressed(MouseEvent e) {
            e.consume();
         }

         @Override
         public void mouseClicked(MouseEvent e) {
            e.consume();
         }

         @Override
         public void mouseReleased(MouseEvent e) {
            e.consume();
         }

         @Override
         public void mouseDragged(MouseEvent e) {
            e.consume();
         }
      });
      speakingPane.add(glassPanel, new Integer(2));
      glassPanel.setVisible(false);

      speakingPane.addComponentListener(new ComponentAdapter() {

         @Override
         public void componentResized(ComponentEvent e) {
            scrollPane.setBounds(0, 0, speakingPane.getWidth(), speakingPane.getHeight());
            glassPanel.setBounds(0, 0, speakingPane.getWidth(), speakingPane.getHeight());
         }
      });
   }

   private void initRightButtonPanel() {
      GridLayout buttonLayout = new GridLayout(2, 0);
      buttonLayout.setVgap(20);
      parseTextButtonPanel = new JPanel(buttonLayout);
      parseTextButtonPanel.setPreferredSize(new Dimension(50, 50));

      saveButton = new JButton(Icon.getIcon("/icons/save.png"));
      saveButton.setToolTipText(MESSAGES.get("save_tooltip"));
      saveButton.addActionListener(saveListener);
      parseTextButtonPanel.add(saveButton);

      playButton = new JButton(Icon.getIcon("/icons/control_play.png"));
      playButton.setToolTipText(MESSAGES.get("play_tooltip"));
      playButton.addActionListener(playListener);
      parseTextButtonPanel.add(playButton);

      stopButton = new JButton(Icon.getIcon("/icons/control_stop.png"));
      stopButton.setToolTipText(MESSAGES.get("stop_tooltip"));
      stopButton.addActionListener(stopListener);
      stopButton.setVisible(false);
   }

   private void initPopularPhrasesListeners() {
      popularPhrasesListeners = new ArrayList<>();
      for (int i = 0; i < PROPERTIES.getPopularPhrases().length; i++) {
         final int index = i;
         popularPhrasesListeners.add(a -> {
            String popularPhrase = PROPERTIES.getPopularPhrase(index);
            typingField.setText(popularPhrase);
            speekTypingFieldContent(false);
         });
      }
   }

   private void initPopularPhrasesButtonPanel() {
      popularPhrasesPanel = new JPanel();
      popularPhrasesPanel.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("default:grow"),
            FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC,
            ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
            FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC,
            ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
            FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC,
            ColumnSpec.decode("default:grow") }, new RowSpec[] { FormFactory.DEFAULT_ROWSPEC }));
      popularPhrasesButtons = new ArrayList<>();
      for (int i = 0; i < PROPERTIES.getPopularPhrases().length; i++) {
         JButton popularPhraseButton = new JButton("" + (i + 1));
         popularPhrasesButtons.add(popularPhraseButton);
         popularPhrasesPanel.add(popularPhraseButton, String.format("%d, 1", (1 + i * 2)));
         popularPhraseButton.addActionListener(popularPhrasesListeners.get(i));
      }
      reinitPopularPhrasesButtonsTooltip();
   }

   private void reinitPopularPhrasesButtonsTooltip() {
      for (int i = 0; i < PROPERTIES.getPopularPhrases().length; i++) {
         if (!PROPERTIES.getPopularPhrase(i).isEmpty()) {
            popularPhrasesButtons.get(i).setToolTipText(PROPERTIES.getPopularPhrase(i) + " (Ctrl " + (i + 1) + ")");
         } else {
            popularPhrasesButtons.get(i).setToolTipText(null);
         }
      }
   }

   private void initTypingField() {
      typingField = PROPERTIES.isSuggestionsEnabled() ? createSuggestField() : createTextField();
   }

   private JTextField createTextField() {
      JTextField typingField = new JTextField();
      typingField.addKeyListener(new KeyAdapter() {

         @Override
         public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
               speekTypingFieldContent(false);
            }
         }
      });
      new EditAdapter(typingField);
      return typingField;
   }

   private JTextField createSuggestField() {
      JSuggestField typingField = new JSuggestField(this, Suggestions.getInstance().getSuggestions());
      typingField.addKeyListener(new KeyAdapter() {

         @Override
         public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
               if (typingField.isSuggestionAccepted()) {
                  typingField.setSuggestionAccepted(false);
               } else {
                  speekTypingFieldContent(true);
                  typingField.hideSuggest();
               }
            }
         }
      });
      new EditAdapter(typingField);
      return typingField;
   }

   private void speekTypingFieldContent(boolean saveSuggestions) {
      if (!typingField.getText().isEmpty()) {
         speeker.speek(Arrays.asList(typingField.getText()));
         if (saveSuggestions) {
            Suggestions.getInstance().addSuggestion(typingField.getText());
         }
      }
      speakingArea.setText(speakingArea.getText() + "\n" + typingField.getText());
      speakingArea.setCaretPosition(speakingArea.getText().length());
      typingField.setText("");
      typingField.requestFocusInWindow();
   }

   private void initCollapseExpandButton() {
      collapseExpandButton = new JButton(Icon.getIcon("/icons/application_top_contract.png"));
      collapseExpandButton.addActionListener(collapseExpandListener);
   }

   private void calculateScreenSize() {
      if (firstTimeVisible) {
         try {
            Thread.sleep(500);
         } catch (InterruptedException e1) {
            log.error("Calculate screen interrupted");
         }
         double conentPaneHeight = getSize().getHeight() - getContentPane().getSize().getHeight();
         collapsedSize = new Dimension((int) collapsedSize.getWidth(),
               collapsedContentPaneHeight + (int) conentPaneHeight);
         expandedSize = new Dimension((int) expandedSize.getWidth(),
               expandedContentPaneHeight + (int) conentPaneHeight);
         pack();
         if (PROPERTIES.isScreenCollapsed()) {
            collapse();
         } else {
            expand();
         }
         firstTimeVisible = false;
      }
   }

   private void collapse() {
      collapseItem.setEnabled(false);
      expandItem.setEnabled(true);
      collapseExpandButton.setIcon(Icon.getIcon("/icons/application_top_expand.png"));
      collapseExpandButton.setToolTipText(MESSAGES.get("expand_tooltip"));
      getContentPane().remove(expandedPanel);
      collapsedPanel.add(typingField, "1, 1");
      collapsedPanel.add(collapseExpandButton, "3, 1");
      getContentPane().add(collapsedPanel);
      setSize(collapsedSize);
      typingField.grabFocus();
   }

   private void expand() {
      collapseItem.setEnabled(true);
      expandItem.setEnabled(false);
      collapseExpandButton.setIcon(Icon.getIcon("/icons/application_top_contract.png"));
      collapseExpandButton.setToolTipText(MESSAGES.get("collapse_tooltip"));
      getContentPane().remove(collapsedPanel);
      expandedPanel.add(typingField, "1, 5");
      expandedPanel.add(popularPhrasesPanel, "1, 3");
      expandedPanel.add(collapseExpandButton, "3, 5");
      getContentPane().add(expandedPanel);
      setSize(expandedSize);
      typingField.grabFocus();
   }

   private JMenuBar createMenu() {
      JMenuBar menuBar = new JMenuBar();

      JMenu fileMenu = new JMenu(MESSAGES.get("file"));
      fileMenu.setMnemonic(KeyEvent.VK_F);
      menuBar.add(fileMenu);

      JMenuItem openFileItem = new JMenuItem(MESSAGES.get("open_file_menu"), Icon.getIcon("/icons/folder_page.png"));
      openFileItem.setMnemonic(KeyEvent.VK_O);
      openFileItem.addActionListener(a -> {
         JFileChooser chooser = new JFileChooser();
         chooser.setDialogTitle(MESSAGES.get("open_file"));
         chooser.setFileFilter(new TxtFilter());
         int returnValue = chooser.showOpenDialog(ApplicationWindow.this);
         if (returnValue == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
               List<String> allLines = Files.readAllLines(file.toPath());
               speakingArea.setText(allLines.stream().collect(Collectors.joining("\n")));
            } catch (Exception e) {
               JOptionPane.showMessageDialog(ApplicationWindow.this, MESSAGES.get("open_file_error"),
                     MESSAGES.get("error"), JOptionPane.ERROR_MESSAGE);
               log.error("Unable to open text file", e);
            }
         }
      });
      fileMenu.add(openFileItem);

      JMenuItem saveFileItem = new JMenuItem(MESSAGES.get("save_file_menu"), Icon.getIcon("/icons/page_save.png"));
      saveFileItem.setMnemonic(KeyEvent.VK_S);
      saveFileItem.addActionListener(a -> {
         JFileChooser chooser = new JFileChooser();
         chooser.setDialogTitle(MESSAGES.get("save_file"));
         chooser.setFileFilter(new TxtFilter());
         int returnValue = chooser.showOpenDialog(ApplicationWindow.this);
         if (returnValue == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().endsWith("txt") || !file.getName().endsWith("TXT")) {
               file = new File(file.getAbsolutePath() + ".txt");
            }
            try {
               Files.write(file.toPath(), speakingArea.getText().getBytes());
            } catch (Exception e) {
               JOptionPane.showMessageDialog(ApplicationWindow.this, MESSAGES.get("save_file_error"),
                     MESSAGES.get("error"), JOptionPane.ERROR_MESSAGE);
               log.error("Unable to save text file", e);
            }
         }
      });
      fileMenu.add(saveFileItem);

      fileMenu.addSeparator();

      JMenuItem exportItem = new JMenuItem(MESSAGES.get("export_menu"), Icon.getIcon("/icons/save.png"));
      exportItem.setMnemonic(KeyEvent.VK_E);
      exportItem.addActionListener(saveListener);
      fileMenu.add(exportItem);

      fileMenu.addSeparator();

      JMenuItem quitItem = new JMenuItem(MESSAGES.get("quit"), Icon.getIcon("/icons/cross.png"));
      quitItem.setMnemonic(KeyEvent.VK_Q);
      quitItem.addActionListener(a -> {
         System.exit(0);
      });
      fileMenu.add(quitItem);

      JMenu configureMenu = new JMenu(MESSAGES.get("configure"));
      configureMenu.setMnemonic(KeyEvent.VK_C);
      menuBar.add(configureMenu);

      JMenuItem configureVoiceItem = new JMenuItem(MESSAGES.get("configure_voice"),
            Icon.getIcon("/icons/comment_edit.png"));
      configureVoiceItem.setMnemonic(KeyEvent.VK_V);
      configureVoiceItem.addActionListener(a -> {
         VoiceConfigurationDialog dialog = new VoiceConfigurationDialog(ApplicationWindow.this);
         dialog.setVisible(true);
         if (dialog.isOkPressed()) {
            try {
               speeker.initMaryTTS();
            } catch (Exception e) {
               JOptionPane.showMessageDialog(ApplicationWindow.this, MESSAGES.get("init_marytts_error"),
                     MESSAGES.get("error"), JOptionPane.ERROR_MESSAGE);
               log.error("Unable to reinitialize marytts", e);
            }
         }
      });
      configureMenu.add(configureVoiceItem);

      JMenuItem configureGuiItem = new JMenuItem(MESSAGES.get("configure_gui"),
            Icon.getIcon("/icons/application_form_edit.png"));
      configureGuiItem.setMnemonic(KeyEvent.VK_G);
      configureGuiItem.addActionListener(a -> {
         GuiConfigDialog dialog = new GuiConfigDialog(ApplicationWindow.this);
         dialog.setVisible(true);
         if (dialog.isOkPressed() && dialog.isSettingsChanged()) {
            JOptionPane.showMessageDialog(ApplicationWindow.this, MESSAGES.get("restart_message"),
                  MESSAGES.get("restart_title"), JOptionPane.INFORMATION_MESSAGE);
         }
      });
      configureMenu.add(configureGuiItem);

      JMenuItem configurePopularPhrasesItem = new JMenuItem(MESSAGES.get("popular_phrases_config_title"),
            Icon.getIcon("/icons/tag_blue_edit.png"));
      configurePopularPhrasesItem.setMnemonic(KeyEvent.VK_P);
      configurePopularPhrasesItem.addActionListener(a -> {
         PopularPhrasesConfigDialog dialog = new PopularPhrasesConfigDialog(ApplicationWindow.this);
         dialog.setVisible(true);
         if (dialog.isOkPressed()) {
            reinitPopularPhrasesButtonsTooltip();
         }
      });
      configureMenu.add(configurePopularPhrasesItem);

      JMenu playMenu = new JMenu(MESSAGES.get("play"));
      playMenu.setMnemonic(KeyEvent.VK_P);
      menuBar.add(playMenu);

      playMenuItem = new JMenuItem(MESSAGES.get("play"), Icon.getIcon("/icons/control_play.png"));
      playMenuItem.setMnemonic(KeyEvent.VK_L);
      playMenuItem.addActionListener(playListener);
      playMenu.add(playMenuItem);

      stopMenuItem = new JMenuItem(MESSAGES.get("stop"), Icon.getIcon("/icons/control_stop.png"));
      stopMenuItem.setMnemonic(KeyEvent.VK_T);
      stopMenuItem.addActionListener(stopListener);
      stopMenuItem.setEnabled(false);
      playMenu.add(stopMenuItem);

      JMenu viewMenu = new JMenu(MESSAGES.get("view"));
      viewMenu.setMnemonic(KeyEvent.VK_V);
      menuBar.add(viewMenu);

      expandItem = new JMenuItem(MESSAGES.get("expand"), Icon.getIcon("/icons/application_top_expand.png"));
      expandItem.setMnemonic(KeyEvent.VK_X);
      expandItem.addActionListener(collapseExpandListener);
      viewMenu.add(expandItem);

      collapseItem = new JMenuItem(MESSAGES.get("collapse"), Icon.getIcon("/icons/application_top_contract.png"));
      collapseItem.setMnemonic(KeyEvent.VK_C);
      collapseItem.addActionListener(collapseExpandListener);
      viewMenu.add(collapseItem);

      JMenu helpMenu = new JMenu(MESSAGES.get("help"));
      helpMenu.setMnemonic(KeyEvent.VK_H);
      menuBar.add(helpMenu);

      JMenuItem helpItem = new JMenuItem(MESSAGES.get("help"), Icon.getIcon("/icons/help.png"));
      helpItem.setMnemonic(KeyEvent.VK_F1);
      helpItem.addActionListener(a -> HelpBrowser
            .getInstance("/help.html", MESSAGES.get("client_window_title") + " - " + MESSAGES.get("help"),
                  MESSAGES.get("load_help_error"), MESSAGES.get("error"))
            .setVisible(true));
      helpMenu.add(helpItem);

      JMenuItem aboutItem = new JMenuItem(MESSAGES.get("about"), Icon.getIcon("/icons/star.png"));
      aboutItem.setMnemonic(KeyEvent.VK_A);
      aboutItem.addActionListener(a -> new AboutDialog(ApplicationWindow.this).setVisible(true));
      helpMenu.add(aboutItem);

      return menuBar;
   }

   private void initNativeHook() {
      try {
         java.util.logging.Logger logger = java.util.logging.Logger
               .getLogger(GlobalScreen.class.getPackage().getName());
         logger.setLevel(Level.OFF);
         GlobalScreen.registerNativeHook();
         GlobalScreen.addNativeKeyListener(new NativeKeyListener() {

            private Set<Integer> pressedKeyCodes = new HashSet<>();

            @Override
            public void nativeKeyTyped(NativeKeyEvent e) {
               // Nothing todo
            }

            @Override
            public void nativeKeyReleased(NativeKeyEvent e) {
               pressedKeyCodes.clear();
            }

            @Override
            public void nativeKeyPressed(NativeKeyEvent e) {
               if (Arrays.stream(PROPERTIES.getNativeHookKeyCodes()).anyMatch(new Integer(e.getKeyCode())::equals)) {
                  pressedKeyCodes.add(e.getKeyCode());
               }
               if (pressedKeyCodes.size() == 3) {
                  pressedKeyCodes.clear();
                  SwingUtilities.invokeLater(new Runnable() {

                     @Override
                     public void run() {
                        if (getExtendedState() == Frame.ICONIFIED) {
                           setExtendedState(Frame.NORMAL);
                        } else {
                           setExtendedState(Frame.ICONIFIED);
                        }
                     }
                  });
               }
            }
         });
      } catch (NativeHookException ex) {
         log.warn("Unable to use native hook, disabling it");
         PROPERTIES.setNativeHookEnabled(false);
      }
   }

   private void updateGuiSpeekingState() {
      glassPanel.setVisible(speeking);
      typingField.setEnabled(!speeking);
      speakingArea.setEditable(!speeking);
      saveButton.setEnabled(!speeking);
      playButton.setVisible(!speeking);
      playMenuItem.setEnabled(!speeking);
      stopButton.setVisible(speeking);
      stopMenuItem.setEnabled(speeking);
      if (speeking) {
         parseTextButtonPanel.remove(playButton);
         parseTextButtonPanel.add(stopButton);
      } else {
         parseTextButtonPanel.add(playButton);
         parseTextButtonPanel.remove(stopButton);
      }
      popularPhrasesButtons.forEach(b -> b.setEnabled(!speeking));
   }

   private void addGlobalKeyAdapters(Component... components) {
      Arrays.stream(components).forEach(c -> c.addKeyListener(new ShortCutsAdapter()));
   }

   private class ShortCutsAdapter extends KeyAdapter {

      @Override
      public void keyPressed(KeyEvent e) {
         if (e.isControlDown()) {
            if (e.getKeyCode() == KeyEvent.VK_P && speeking) {
               stopListener.actionPerformed(null);
            } else if (e.getKeyCode() == KeyEvent.VK_P && !speeking) {
               playListener.actionPerformed(null);
            } else if (e.getKeyCode() == KeyEvent.VK_S) {
               saveListener.actionPerformed(null);
            } else if (e.getKeyCode() == KeyEvent.VK_E) {
               collapseExpandListener.actionPerformed(null);
            } else if (e.getKeyCode() >= KeyEvent.VK_1 && e.getKeyCode() <= KeyEvent.VK_9) {
               int index = e.getKeyCode() - KeyEvent.VK_1;
               popularPhrasesListeners.get(index).actionPerformed(null);
            }
         }
      }
   }

   private class EditAdapter extends MouseAdapter implements FocusListener {

      private JTextComponent parent;
      private JPopupMenu menu;

      public EditAdapter(JTextComponent parent) {
         this.parent = parent;
         parent.addMouseListener(this);
         parent.addFocusListener(this);
         menu = createEditMenu();
      }

      @Override
      public void mouseClicked(MouseEvent e) {
         parent.requestFocus();

         // Left button
         if (e.getButton() == MouseEvent.BUTTON3) {
            menu.setLocation(e.getXOnScreen() + 1, e.getYOnScreen() + 1);
            menu.setVisible(true);
         } else {
            menu.setVisible(false);
         }
      }

      private JPopupMenu createEditMenu() {
         final JPopupMenu menu = new JPopupMenu(MESSAGES.get("edit"));

         final JMenuItem cutItem = new JMenuItem(MESSAGES.get("cut"), Icon.getIcon("/icons/cut.png"));
         cutItem.addActionListener(a -> {
            StringSelection selection = new StringSelection(parent.getSelectedText());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
            parent.replaceSelection("");
            menu.setVisible(false);
         });
         menu.add(cutItem);

         JMenuItem copyItem = new JMenuItem(MESSAGES.get("copy"), Icon.getIcon("/icons/page_copy.png"));
         copyItem.addActionListener(a -> {
            StringSelection selection = new StringSelection(parent.getSelectedText());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
            menu.setVisible(false);
         });
         menu.add(copyItem);

         JMenuItem pasteItem = new JMenuItem(MESSAGES.get("paste"), Icon.getIcon("/icons/paste_plain.png"));
         pasteItem.addActionListener(a -> {
            Transferable clipboardContents = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
            if (clipboardContents != null && clipboardContents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
               try {
                  String pasted = (String) clipboardContents.getTransferData(DataFlavor.stringFlavor);
                  parent.replaceSelection(pasted);
               } catch (UnsupportedFlavorException | IOException ex) {
                  log.error("Unable to paste content", ex);
               }
            }

            menu.setVisible(false);
         });
         menu.add(pasteItem);

         JMenuItem deleteItem = new JMenuItem(MESSAGES.get("delete"), Icon.getIcon("/icons/page_white_delete.png"));
         deleteItem.addActionListener(a -> {
            parent.replaceSelection("");
            menu.setVisible(false);
         });
         menu.add(deleteItem);

         menu.addSeparator();

         JMenuItem selectAllItem = new JMenuItem(MESSAGES.get("select_all"), Icon.getIcon("/icons/accept.png"));
         selectAllItem.addActionListener(a -> {
            parent.setSelectionStart(0);
            parent.setSelectionEnd(parent.getText().length());
            menu.setVisible(false);
         });
         menu.add(selectAllItem);

         return menu;
      }

      @Override
      public void focusGained(FocusEvent e) {
         // nothing todo
      }

      @Override
      public void focusLost(FocusEvent e) {
         menu.setVisible(false);
      }
   }

   private class WaveFilter extends FileFilter {

      @Override
      public boolean accept(File file) {
         return file.isDirectory() || file.getName().endsWith("wav") || file.getName().endsWith("WAV");
      }

      @Override
      public String getDescription() {
         return MESSAGES.get("wav");
      }
   }

   private class TxtFilter extends FileFilter {

      @Override
      public boolean accept(File file) {
         return file.isDirectory() || file.getName().endsWith("txt") || file.getName().endsWith("TXT");
      }

      @Override
      public String getDescription() {
         return MESSAGES.get("txt");
      }
   }
}
