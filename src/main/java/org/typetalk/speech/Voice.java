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

package org.typetalk.speech;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.typetalk.Application;
import org.typetalk.Messages;
import org.typetalk.TypeTalkProperties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Voice {

   @AllArgsConstructor
   public enum Gender {

      MALE(Messages.getInstance().get("male")), FEMALE(Messages.getInstance().get("female"));

      private String displayName;

      @Override
      public String toString() {
         return displayName;
      }
   };

   private static final List<Voice> VOICES = new ArrayList<>();
   private static final Preferences PREFERENCES = Preferences.userNodeForPackage(Voice.class);
   private static final TypeTalkProperties PROPERTIES = TypeTalkProperties.getInstance();
   private static final String DEFAULT_VOICE = "dfki-obadiah-hsmm";

   @Getter
   private String name;
   @Getter
   private String description;
   private Gender gender;
   private String language;
   private String country;

   public static List<Voice> getAllVoices() {
      if (VOICES.isEmpty()) {
         VOICES.addAll(readAllVoices());
      }
      return VOICES;
   }

   public static Voice getSelectedVoice() {
      String name = PREFERENCES.get("voice", DEFAULT_VOICE);
      return getVoice(name);
   }

   public static void setSelectedVoice(String voice) {
      PREFERENCES.put("voice", voice);
   }

   public static Voice getDefaultVoice() {
      return getVoice(DEFAULT_VOICE);
   }

   @Override
   public String toString() {
      return Character.toUpperCase(name.charAt(0)) + name.substring(1) + " (" + gender + ", " + language + " - "
            + country + ")";
   }

   private static List<Voice> readAllVoices() {
      File installationDir = new File(PROPERTIES.getSettingsDirectory() + File.separator + Application.INSTALLATION_DIR);
      List<File> voiceFiles = Arrays.asList(installationDir.listFiles())
            .stream()
            .filter(f -> f.getName().startsWith("voice"))
            .collect(Collectors.toList());
      return voiceFiles.stream()
            .map(f -> readFromXml(f))
            .filter(v -> v != null)
            .collect(Collectors.toList());
   }

   private static Voice readFromXml(File xmlFile) {
      try {
         InputStream xmlStream = new FileInputStream(xmlFile);
         Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlStream);
         document.getDocumentElement().normalize();
         Node voice = document.getElementsByTagName("voice").item(0);
         Node description = document.getElementsByTagName("description").item(0);

         if (voice != null && voice.getNodeType() == Node.ELEMENT_NODE && description != null
               && description.getNodeType() == Node.ELEMENT_NODE) {

            String name = ((Element) voice).getAttribute("name");
            String descriptionText = ((Element) description).getTextContent();
            String genderText = ((Element) voice).getAttribute("gender");
            String localeText = ((Element) voice).getAttribute("locale");
            Locale locale = new Locale(localeText.split("-")[0], localeText.split("-")[1]);
            return new Voice(name, descriptionText, Gender.valueOf(genderText.toUpperCase()),
                  locale.getDisplayLanguage(), locale.getDisplayCountry());

         } else {
            log.error("No <voice> or <description> element in xml file: " + xmlFile.getName());
         }

      } catch (SAXException | IOException | ParserConfigurationException e) {
         log.error("Not able to parse xml to voice, file: " + xmlFile.getName(), e);
      }

      return null;
   }

   private static Voice getVoice(String name) {
      try {

         return getAllVoices()
               .stream()
               .filter(v -> v.getName().equals(name))
               .collect(Collectors.toList())
               .get(0);

      } catch (IndexOutOfBoundsException e) {
         log.error("No voices found");
      }
      
      return null;
   }
}