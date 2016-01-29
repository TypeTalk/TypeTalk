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

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;

public final class ScreenPositioner {

   private ScreenPositioner() {
      // Utilitiy class
   }

   public static void centerOnScreen(Window window) {
      Toolkit toolkit = Toolkit.getDefaultToolkit();
      Dimension resolution = toolkit.getScreenSize();

      int x = (int) (resolution.getWidth() / 2 - window.getWidth() / 2);
      int y = (int) (resolution.getHeight() / 2 - window.getHeight() / 2);

      window.setLocation(x, y);
   }

}