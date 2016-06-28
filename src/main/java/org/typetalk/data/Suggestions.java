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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.typetalk.TypeTalkProperties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class Suggestions {

   private static final int SIZE_LIMIT = 1000;
   private static final TypeTalkProperties PROPERTIES = TypeTalkProperties.getInstance();
   private static Suggestions instance;

   private ObjectMapper mapper = new ObjectMapper();
   private Path suggestionsFile;
   private Executor writeFileExecutor = Executors.newSingleThreadExecutor();
   private Map<String, Suggestion> suggestions = new HashMap<>();
   private List<String> suggestionValues = new ArrayList<>();
   private SuggestionComparator suggestionComparator = new SuggestionComparator();

   private Suggestions() {
      suggestionsFile = Paths.get(PROPERTIES.getSettingsDirectory() + File.separator + "suggestions.json");
      initSuggestionsFile();
      readSuggestions();
   }

   public static Suggestions getInstance() {
      if (instance == null) {
         instance = new Suggestions();
      }
      return instance;
   }

   public void addSuggestion(String suggestionValue) {
      synchronized (suggestions) {
         Suggestion suggestion = suggestions.get(suggestionValue);
         if (suggestion == null) {
            suggestion = new Suggestion(suggestionValue, 0);
            suggestions.put(suggestionValue, suggestion);
            suggestionValues.add(suggestionValue);
         }
         suggestion.setCount(suggestion.getCount() + 1);
         suggestionValues.sort(suggestionComparator);
         if (suggestions.size() > SIZE_LIMIT) {
            String lastSuggestionValue = suggestionValues.get(suggestionValues.size() - 1);
            suggestions.remove(lastSuggestionValue);
            suggestionValues.remove(lastSuggestionValue);
         }
         writeSuggestionsToFile();
      }
   }

   public List<String> getSuggestions() {
      return suggestionValues;
   }

   private void writeSuggestionsToFile() {
      writeFileExecutor.execute(() -> {
         try {
            mapper.writeValue(suggestionsFile.toFile(), suggestions.values());
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
         List<Suggestion> suggestionsFromFile = mapper.readValue(suggestionsFile.toFile(),
               new TypeReference<List<Suggestion>>() {
               });
         for (Suggestion suggestion : suggestionsFromFile) {
            suggestionValues.add(suggestion.getValue());
            suggestions.put(suggestion.getValue(), suggestion);
         }
         suggestionValues.sort(suggestionComparator);
      } catch (IOException e) {
         log.error("Unable to read suggestions file", e);
      }
   }

   private class SuggestionComparator implements Comparator<String> {

      @Override
      public int compare(String s1, String s2) {
         if (suggestions.get(s1) != null && suggestions.get(s2) != null) {
            if (suggestions.get(s1).getCount() == suggestions.get(s2).getCount()) {
               return s1.compareTo(s2);
            }
            return Integer.compare(suggestions.get(s2).getCount(), suggestions.get(s1).getCount());
         }
         return 0;
      }
   }
}
