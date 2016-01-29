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

import java.awt.Panel;

import javax.swing.JLabel;
import javax.swing.JSeparator;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * This component consists of a label followed by a line (JSeparator).
 */
public class StringSeparator extends Panel {

   public StringSeparator(String text) {
      setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("left:default"),
            FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), }, new RowSpec[] {
            FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

      add(new JLabel(text), "2, 2");
      add(new JSeparator(), "4, 2");
   }
}
