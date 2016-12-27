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
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Language {

   private static final List<Language> LANGUAGES = new ArrayList<>();
   private static final Preferences PREFERENCES = Preferences.userNodeForPackage(Language.class);
   private static final TypeTalkProperties PROPERTIES = TypeTalkProperties.getInstance();
   private static final String DEFAULT_LANGUAGE = "en-GB";

   private String name;
   private String description;
   private Locale locale;
   private String language;
   private String country;
   private String languageJarFile;

   public static List<Language> getAllLanguages() {
      if (LANGUAGES.isEmpty()) {
         loadLanguages();
      }
      return LANGUAGES;
   }

   public static void loadLanguages() {
      LANGUAGES.addAll(readAllLanguages());
   }

   public static Language getSelectedLanguage() {
      String name = PREFERENCES.get("language", DEFAULT_LANGUAGE);
      return getLanguage(name);
   }

   public static void setSelectedLanguage(String language) {
      PREFERENCES.put("language", language);
   }

   public static Language getDefaultLanguage() {
      return getLanguage(DEFAULT_LANGUAGE);
   }

   @Override
   public String toString() {
      return language + " - " + country;
   }

   private static List<Language> readAllLanguages() {
      File installationDir = new File(
            PROPERTIES.getSettingsDirectory() + File.separator + Application.INSTALLATION_DIR);
      List<File> languagesFiles = Arrays.asList(installationDir.listFiles()).stream()
            .filter(f -> f.getName().startsWith("marytts-lang")).collect(Collectors.toList());
      return languagesFiles.stream().map(f -> readFromXml(f)).filter(v -> v != null).collect(Collectors.toList());
   }

   private static Language readFromXml(File xmlFile) {
      try {
         InputStream xmlStream = new FileInputStream(xmlFile);
         Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlStream);
         document.getDocumentElement().normalize();

         Node languageNode = document.getElementsByTagName("language").item(0);
         Node descriptionNode = document.getElementsByTagName("description").item(0);
         Node filesNode = document.getElementsByTagName("files").item(0);

         if (languageNode != null && languageNode.getNodeType() == Node.ELEMENT_NODE && descriptionNode != null
               && descriptionNode.getNodeType() == Node.ELEMENT_NODE && filesNode != null
               && filesNode.getNodeType() == Node.ELEMENT_NODE) {

            String name = ((Element) languageNode).getAttribute("name");
            String description = ((Element) descriptionNode).getTextContent();
            String localeText = ((Element) languageNode).getAttribute("locale");
            Locale locale = new Locale(localeText.split("-")[0], localeText.split("-")[1]);
            String language = locale.getDisplayLanguage();
            String country = locale.getDisplayCountry();
            String languageJarFile = ((Element) filesNode).getTextContent();

            return new Language(name, description, locale, language, country, languageJarFile);

         } else {
            log.error("No <language>, <description> or <files> element in xml file: " + xmlFile.getName());
         }

      } catch (SAXException | IOException | ParserConfigurationException e) {
         log.error("Not able to parse xml to language, file: " + xmlFile.getName(), e);
      }

      return null;
   }

   private static Language getLanguage(String name) {
      try {

         return getAllLanguages().stream().filter(v -> v.getName().equals(name)).collect(Collectors.toList()).get(0);

      } catch (IndexOutOfBoundsException e) {
         log.error("No voices found");
      }

      return null;
   }
}