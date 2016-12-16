package org.typetalk;

import java.io.File;
import java.io.FilenameFilter;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import marytts.tools.install.InstallFileParser;
import marytts.tools.install.InstallerGUI;

public class InstallerGuiRunner {
   
   public static void main(String args[]) throws Exception {
      String maryBase = System.getProperty("mary.base");
      if (maryBase == null || !new File(maryBase).isDirectory()) {
         JFrame window = new JFrame("This is the Frames's Title Bar!");
         JFileChooser fc = new JFileChooser();
         fc.setDialogTitle("Please indicate MARY TTS installation directory");
         fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
         int returnVal = fc.showOpenDialog(window);
         if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            if (file != null)
               maryBase = file.getAbsolutePath();
         }
      }
      if (maryBase == null || !new File(maryBase).isDirectory()) {
         System.out.println("No MARY base directory -- exiting.");
         System.exit(0);
      }
      System.setProperty("mary.base", maryBase);

      File archiveDir = new File(maryBase + "/download");
      if (!archiveDir.exists())
         archiveDir.mkdir();
      System.setProperty("mary.downloadDir", archiveDir.getPath());
      File infoDir = new File(maryBase + "/installed");
      if (!infoDir.exists())
         infoDir.mkdir();
      System.setProperty("mary.installedDir", infoDir.getPath());

      InstallerGUI g = new InstallerGUI();

      File[] componentDescriptionFiles = infoDir.listFiles(new FilenameFilter() {
         @Override
         public boolean accept(File dir, String name) {
            return name.endsWith(".xml");
         }
      });
      for (File cd : componentDescriptionFiles) {
         try {
            g.addLanguagesAndVoices(new InstallFileParser(cd.toURI().toURL()));
         } catch (Exception exc) {
            exc.printStackTrace();
         }
      }
      componentDescriptionFiles = archiveDir.listFiles(new FilenameFilter() {
         @Override
         public boolean accept(File dir, String name) {
            return name.endsWith(".xml");
         }
      });
      for (File cd : componentDescriptionFiles) {
         try {
            g.addLanguagesAndVoices(new InstallFileParser(cd.toURI().toURL()));
         } catch (Exception exc) {
            exc.printStackTrace();
         }
      }

      if (args.length > 0) {
         g.setAndUpdateFromMaryComponentURL(args[0]);
      }

      g.setVisible(true);

   }
}
