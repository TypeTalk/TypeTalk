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

import java.util.ArrayList;
import java.util.List;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class LookAndFeel {
   
   private String name;
   private String fullClassName;

   @Override
   public String toString() {
      return name;
   }
   
   public static LookAndFeel[] getAll() {
      List<LookAndFeel> allLafs = new ArrayList<>();
      for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
         allLafs.add(new LookAndFeel(info.getName(), info.getClassName()));
      }
      return allLafs.toArray(new LookAndFeel[allLafs.size()]);
   }
}
