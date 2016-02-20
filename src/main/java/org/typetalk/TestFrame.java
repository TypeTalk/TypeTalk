/*
 * Copyright 2016, SwingUtils <https://github.com/raginggoblin/swingutils>
 * 
 * This file is part of SwingUtils.
 *
 *  SwingUtils is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  SwingUtils is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with TypeTalk.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.typetalk;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.UIManager;

import raging.goblin.swingutils.DoubleSlider;

public class TestFrame {

   public static void main(String[] args) {
      UIManager.put("Slider.paintValue", false);
      initAntiAliasing();
      loadLaf("gtk");
      JFrame frame = new JFrame("Test frame");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setSize(600, 600);
      frame.getContentPane().add(createContent());
      frame.setVisible(true);
   }

   private static Component createContent() {
      JPanel contentPanel = new JPanel(new BorderLayout());
      DoubleSlider doubleSlider = new DoubleSlider(1.4, 0.24, 5.0, 0.2);
      doubleSlider.addChangeListener(cl -> {
         System.out.printf("Slider double Value: %4.3f%n", doubleSlider.getDoubleValue());
         System.out.printf("Slider int Value: %d%n", doubleSlider.getValue());
      });
      contentPanel.add(doubleSlider);

      JSlider intSlider = new JSlider(0, 100, 50);
      intSlider.setMinorTickSpacing(5);
      intSlider.addChangeListener(cl -> System.out.printf("Slider Value: %d%n", intSlider.getValue()));
      contentPanel.add(intSlider, BorderLayout.SOUTH);
      return contentPanel;

   }

   private static void loadLaf(String laf) {
      try {
         switch (laf) {
         case "metal":
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
            return;
         case "nimbus":
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            return;
         case "motif":
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
            return;
         case "gtk":
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
            return;
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   private static void initAntiAliasing() {
      System.setProperty("awt.useSystemAAFontSettings", "lcd");
      System.setProperty("swing.aatext", "true");
   }
}
