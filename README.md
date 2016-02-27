# TypeTalk
MaryTTS frontend with batteries included.

#1. Build
Download marytts sources from [https://github.com/marytts/marytts/archive/v5.1.2.zip](https://github.com/marytts/marytts/archive/v5.1.2.zip) and run mvn install

Download swingutils from [https://github.com/raginggoblin/swingutils/archive/master.zip](https://github.com/raginggoblin/swingutils/archive/master.zip) and run mvn install

Install voices to local maven repo:
* mvn install:install-file -DgroupId=de.dfki.mary -DartifactId=voice-dfki-obadiah-hsmm -Dversion=5.1 -Dpackaging=jar -Dfile=lib/voice-dfki-obadiah-hsmm-5.1.jar
* mvn install:install-file -DgroupId=de.dfki.mary -DartifactId=voice-dfki-spike-hsmm -Dversion=5.1 -Dpackaging=jar -Dfile=lib/voice-dfki-spike-hsmm-5.1.jar
* mvn install:install-file -DgroupId=de.dfki.mary -DartifactId=voice-cmu-slt-hsmm -Dversion=5.1.2 -Dpackaging=jar -Dfile=lib/voice-cmu-slt-hsmm-5.1.2.jar
* mvn install:install-file -DgroupId=de.dfki.mary -DartifactId=voice-dfki-prudence-hsmm -Dversion=5.1 -Dpackaging=jar -Dfile=lib/voice-dfki-prudence-hsmm-5.1.jar
* mvn install:install-file -DgroupId=de.dfki.mary -DartifactId=voice-cmu-bdl-hsmm -Dversion=5.1 -Dpackaging=jar -Dfile=lib/voice-cmu-bdl-hsmm-5.1.jar
* mvn install:install-file -DgroupId=de.dfki.mary -DartifactId=voice-dfki-poppy-hsmm -Dversion=5.1 -Dpackaging=jar -Dfile=lib/voice-dfki-poppy-hsmm-5.1.jar
* mvn install:install-file -DgroupId=de.dfki.mary -DartifactId=voice-cmu-rms-hsmm -Dversion=5.1 -Dpackaging=jar -Dfile=lib/voice-cmu-rms-hsmm-5.1.jar

Follow instructions from https://projectlombok.org/ to setup IDE.

#2. Usage
Download the latest release from [https://github.com/TypeTalk/TypeTalk](https://github.com/TypeTalk/TypeTalk) and run it with: `java -jar TypeTalk.one-jar.jar` 


