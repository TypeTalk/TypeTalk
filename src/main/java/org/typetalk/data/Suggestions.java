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

package org.typetalk.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.commons.lang.StringUtils;
import org.typetalk.UIProperties;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class Suggestions {

   private static final int SIZE_LIMIT = 1000;
   private static final UIProperties PROPERTIES = UIProperties.getInstance();
   private static Suggestions instance;

   private Path suggestionsFile;
   private Executor writeFileExecutor = Executors.newSingleThreadExecutor();

   @Getter
   private List<String> suggestions = new ArrayList<>();

   private Suggestions() {
      suggestionsFile = Paths.get(PROPERTIES.getSettingsDirectory() + File.separator + "suggestions.txt");
      initSuggestionsFile();
      readSuggestions();
   }

   public static Suggestions getInstance() {
      if (instance == null) {
         instance = new Suggestions();
      }
      return instance;
   }

   public void addSuggestion(String suggestion) {
      synchronized (suggestions) {
         suggestions.remove(suggestion);
         suggestions.add(suggestion);
         if (suggestions.size() > SIZE_LIMIT) {
            suggestions.remove(0);
         }
      }
      writeSuggestionsToFile();
   }

   private void writeSuggestionsToFile() {
      writeFileExecutor.execute(() -> {
         try {
            Files.write(suggestionsFile, StringUtils.join(suggestions, System.lineSeparator()).getBytes());
         } catch (Exception e) {
            log.error("Unable to write suggestions file", e);
         }
      });
   }

   private void initSuggestionsFile() {
      if (!Files.exists(suggestionsFile)) {
         try {
            Files.createFile(suggestionsFile);
         } catch (IOException e) {
            log.error("Unable to create suggestions file", e);
         }
      }
   }

   private void readSuggestions() {
      try {
         suggestions = Files.readAllLines(suggestionsFile);
      } catch (IOException e) {
         log.error("Unable to read suggestions file", e);
      }
   }
}
