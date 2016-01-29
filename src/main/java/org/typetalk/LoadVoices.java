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

package org.typetalk;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is a simple utility to generate some code. It does not have any practical meaning within the program.
 */
public class LoadVoices {

   public static void main(String[] args) throws IOException {
      File voicesDir = new File("lib");
      String[] voices = voicesDir.list();
      StringBuilder mvnCommands = new StringBuilder();
      StringBuilder xmlFileNames = new StringBuilder();
      StringBuilder dependencies = new StringBuilder();

      for (String voiceFileName : voices) {
         String artifactId = voiceFileName.replaceAll("-5.1.2.jar", "").replaceAll("-5.1.jar", "");

         Pattern versionPattern = Pattern.compile("\\d.*.jar");
         Matcher versionMatcher = versionPattern.matcher(voiceFileName);
         versionMatcher.find();
         String version = voiceFileName.substring(versionMatcher.start()).replaceAll(".jar", "");

         String installCommand = String.format(
               "mvn install:install-file -DgroupId=de.dfki.mary -DartifactId=%s -Dversion=%s -Dpackaging=jar -Dfile=lib/%s",
               artifactId, version, voiceFileName);
         Runtime.getRuntime().exec(installCommand);

         mvnCommands.append(installCommand + "\n");
         xmlFileNames.append("\"" + artifactId + "-" + version + "-component.xml\", ");

         dependencies.append("<dependency><groupId>de.dfki.mary</groupId><artifactId>" + artifactId
               + "</artifactId><version>" + version + "</version></dependency>\n");
      }

      System.out.println(mvnCommands);
      System.out.println();
      System.out.println(xmlFileNames);
      System.out.println();
      System.out.println(dependencies);
   }
}
