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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
import org.typetalk.UIProperties;
import org.typetalk.speech.Speeker;
import org.typetalk.speech.Speeker.EndOfSpeechListener;

import lombok.extern.slf4j.Slf4j;
import marytts.exceptions.MaryConfigurationException;

@Slf4j
public class ClientWindow extends JFrame implements EndOfSpeechListener {

   private static final Messages MESSAGES = Messages.getInstance();
   private static final UIProperties PROPERTIES = UIProperties.getInstance();

   private Speeker speeker;

   private JTextField typingField;
   private JTextArea speakingArea;
   private JButton saveButton;
   private JButton playButton;
   private JMenuItem playMenuItem;
   private JButton stopButton;
   private JMenuItem stopMenuItem;
   private JPanel parseTextButtonPanel;

   private boolean shiftPressed = false;
   private boolean ctrlPressed = false;

   private ActionListener playListener = a -> {
      setParsing(true);
      List<String> speeches = Arrays.asList(speakingArea.getText().trim().split("\n"));
      if (speeches.size() < 1) {
         setParsing(false);
         typingField.grabFocus();
      } else {
         speeker.speek(speeches);
      }
   };

   private ActionListener stopListener = a -> {
      speeker.stopSpeeking();
      setParsing(false);
   };

   private ActionListener saveListener = a -> {
      JFileChooser chooser = new JFileChooser();
      chooser.setDialogTitle(MESSAGES.get("save_to_wav"));
      chooser.setFileFilter(new WaveFilter());
      int returnValue = chooser.showOpenDialog(ClientWindow.this);
      if (returnValue == JFileChooser.APPROVE_OPTION) {
         File file = chooser.getSelectedFile();
         if (!file.getName().endsWith("wav") || !file.getName().endsWith("WAV")) {
            file = new File(file.getAbsolutePath() + ".wav");
         }
         speeker.save(speakingArea.getText(), file);
      }
   };

   public ClientWindow(Speeker speeker) throws MaryConfigurationException {
      super(MESSAGES.get("client_window_title"));
      this.speeker = speeker;
      speeker.addEndOfSpeechListener(this);
      initGui();
      if (PROPERTIES.isNativeHookEnabled()) {
         initNativeHook();
      }
      setDefaultCloseOperation(EXIT_ON_CLOSE);
   }

   @Override
   public void endOfSpeech() {
      SwingUtilities.invokeLater(() -> {
         setParsing(false);
         typingField.grabFocus();
      });
   }

   @Override
   public void setVisible(boolean visible) {
      super.setVisible(visible);
      typingField.grabFocus();
   }

   private void initGui() {
      setSize(600, 400);
      ScreenPositioner.centerOnScreen(this);
      BorderLayout layout = new BorderLayout(10, 10);
      getContentPane().setLayout(layout);
      Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
      ((JComponent) getContentPane()).setBorder(padding);

      speakingArea = new JTextArea();
      speakingArea.setWrapStyleWord(true);
      speakingArea.setLineWrap(true);
      JScrollPane speakingPane = new JScrollPane(speakingArea);
      speakingPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      speakingArea.addKeyListener(new KeyAdapter() {

         @Override
         public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_TAB) {
               if (shiftPressed) {
                  typingField.grabFocus();
               } else {
                  saveButton.grabFocus();
               }
               e.consume();
            }
         }
      });
      getContentPane().add(speakingPane, BorderLayout.CENTER);
      new EditAdapter(speakingArea);

      typingField = new JTextField();
      getContentPane().add(typingField, BorderLayout.SOUTH);
      typingField.addKeyListener(new KeyAdapter() {

         @Override
         public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
               speeker.speek(Arrays.asList(typingField.getText()));
               speakingArea.setText(speakingArea.getText() + "\n" + typingField.getText());
               speakingArea.setCaretPosition(speakingArea.getText().length());
               typingField.setText("");
               typingField.requestFocusInWindow();
            }
         }
      });
      new EditAdapter(typingField);

      GridLayout buttonLayout = new GridLayout(2, 0);
      buttonLayout.setVgap(20);
      parseTextButtonPanel = new JPanel(buttonLayout);
      parseTextButtonPanel.setPreferredSize(new Dimension(50, 50));
      getContentPane().add(parseTextButtonPanel, BorderLayout.EAST);

      saveButton = new JButton(Icon.getIcon("/icons/save.png"));
      saveButton.setToolTipText(MESSAGES.get("save_tooltip"));
      saveButton.addActionListener(saveListener);
      parseTextButtonPanel.add(saveButton, BorderLayout.EAST);

      playButton = new JButton(Icon.getIcon("/icons/control_play.png"));
      playButton.setToolTipText(MESSAGES.get("play_tooltip"));
      playButton.addActionListener(playListener);
      parseTextButtonPanel.add(playButton, BorderLayout.EAST);

      stopButton = new JButton(Icon.getIcon("/icons/control_stop.png"));
      stopButton.setToolTipText(MESSAGES.get("stop_tooltip"));
      stopButton.addActionListener(stopListener);

      addGlobalKeyAdapters(typingField, speakingArea, saveButton, playButton);

      setJMenuBar(createMenu());
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
         int returnValue = chooser.showOpenDialog(ClientWindow.this);
         if (returnValue == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
               List<String> allLines = Files.readAllLines(file.toPath());
               speakingArea.setText(allLines.stream().collect(Collectors.joining("\n")));
            } catch (Exception e) {
               JOptionPane.showMessageDialog(ClientWindow.this, MESSAGES.get("open_file_error"), MESSAGES.get("error"),
                     JOptionPane.ERROR_MESSAGE);
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
         int returnValue = chooser.showOpenDialog(ClientWindow.this);
         if (returnValue == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().endsWith("txt") || !file.getName().endsWith("TXT")) {
               file = new File(file.getAbsolutePath() + ".txt");
            }
            try {
               Files.write(file.toPath(), speakingArea.getText().getBytes());
            } catch (Exception e) {
               JOptionPane.showMessageDialog(ClientWindow.this, MESSAGES.get("save_file_error"), MESSAGES.get("error"),
                     JOptionPane.ERROR_MESSAGE);
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
         VoiceConfigurationDialog dialog = new VoiceConfigurationDialog(ClientWindow.this);
         dialog.setVisible(true);
         if (dialog.isOkPressed()) {
            try {
               speeker.initMaryTTS();
            } catch (Exception e) {
               JOptionPane.showMessageDialog(ClientWindow.this, MESSAGES.get("init_marytts_error"),
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
         GuiConfigDialog dialog = new GuiConfigDialog(ClientWindow.this);
         dialog.setVisible(true);
         if (dialog.isOkPressed() && dialog.isSettingsChanged()) {
            JOptionPane.showMessageDialog(ClientWindow.this, MESSAGES.get("restart_message"),
                  MESSAGES.get("restart_title"), JOptionPane.INFORMATION_MESSAGE);
         }
      });
      configureMenu.add(configureGuiItem);

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
      playMenu.add(stopMenuItem);

      JMenu helpMenu = new JMenu(MESSAGES.get("help"));
      helpMenu.setMnemonic(KeyEvent.VK_H);
      menuBar.add(helpMenu);

      JMenuItem helpItem = new JMenuItem(MESSAGES.get("help"), Icon.getIcon("/icons/help.png"));
      helpItem.setMnemonic(KeyEvent.VK_F1);
      helpItem.addActionListener(a -> HelpBrowser.getInstance().setVisible(true));
      helpMenu.add(helpItem);

      JMenuItem aboutItem = new JMenuItem(MESSAGES.get("about"), Icon.getIcon("/icons/star.png"));
      aboutItem.setMnemonic(KeyEvent.VK_A);
      aboutItem.addActionListener(a -> new AboutDialog(ClientWindow.this).setVisible(true));
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
      } catch (

      NativeHookException ex)

      {
         log.warn("Unable to use native hook, disabling it");
         PROPERTIES.setNativeHookEnabled(false);
      }

   }

   private void setParsing(boolean parsing) {
      typingField.setEnabled(!parsing);
      speakingArea.setEnabled(!parsing);
      saveButton.setEnabled(!parsing);
      playButton.setVisible(!parsing);
      playMenuItem.setEnabled(!parsing);
      stopButton.setVisible(parsing);
      stopMenuItem.setEnabled(parsing);
      if (parsing) {
         parseTextButtonPanel.remove(playButton);
         parseTextButtonPanel.add(stopButton);
      } else {
         parseTextButtonPanel.add(playButton);
         parseTextButtonPanel.remove(stopButton);
      }
   }

   private void addGlobalKeyAdapters(Component... components) {
      Arrays.stream(components).forEach(c -> {
         c.addKeyListener(new SpecialKeyAdapter());
         c.addKeyListener(new ShortCutsAdapter());
      });
   }

   private class SpecialKeyAdapter extends KeyAdapter {

      @Override
      public void keyPressed(KeyEvent e) {
         if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
            shiftPressed = true;
         } else if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
            ctrlPressed = true;
         }
      }

      @Override
      public void keyReleased(KeyEvent e) {
         if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
            shiftPressed = false;
         } else if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
            ctrlPressed = false;
         }
      }
   }

   private class ShortCutsAdapter extends KeyAdapter {

      @Override
      public void keyPressed(KeyEvent e) {
         if (ctrlPressed && e.getKeyCode() == KeyEvent.VK_P) {
            playListener.actionPerformed(null);
         } else if (ctrlPressed && e.getKeyCode() == KeyEvent.VK_S) {
            saveListener.actionPerformed(null);
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
