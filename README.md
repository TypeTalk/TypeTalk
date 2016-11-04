# TypeTalk
MaryTTS frontend with batteries included.

#1. Usage
If you are using a Linux system the easiest way to install TypeTalk is by using your package management system. We provide .deb and .rpm packages. Download the appropriate package from [https://github.com/TypeTalk/TypeTalk/releases/latest](https://github.com/TypeTalk/TypeTalk/releases/latest), install it through your package manager and you are ready to go. If you are on a system that is not capable of handling .deb or .rpm files, download the platform independent jar file from [https://github.com/TypeTalk/TypeTalk/releases/latest](https://github.com/TypeTalk/TypeTalk/releases/latest) and run it using the command: `java -jar TypeTalk.one-jar.jar`

#2. Build

##2.1 MaryTTS
1. Download marytts sources from [https://github.com/marytts/marytts/archive/v5.2.zip](https://github.com/marytts/marytts/archive/v5.2.zip)
2. unzip sources to a temporary directory
3. run 'mvn install' inside this directory

##2.2 Swingutils
1. Download swingutils from [https://github.com/raginggoblin/swingutils/archive/master.zip](https://github.com/raginggoblin/swingutils/archive/master.zip)
2. unzip sources to a temporary directory
3. run 'mvn install' inside this directory

##2.3 TypeTalk
Clone repo: 'git clone https://github.com/TypeTalk/TypeTalk.git'

##2.4 MaryTTS voices
Move to source directory and install voices to local maven repo:
* mvn install:install-file -DgroupId=de.dfki.mary -DartifactId=voice-dfki-obadiah-hsmm -Dversion=5.2 -Dpackaging=jar -Dfile=lib/voice-dfki-obadiah-hsmm-5.2.jar
* mvn install:install-file -DgroupId=de.dfki.mary -DartifactId=voice-dfki-spike-hsmm -Dversion=5.2 -Dpackaging=jar -Dfile=lib/voice-dfki-spike-hsmm-5.2.jar
* mvn install:install-file -DgroupId=de.dfki.mary -DartifactId=voice-cmu-slt-hsmm -Dversion=5.2 -Dpackaging=jar -Dfile=lib/voice-cmu-slt-hsmm-5.2.jar
* mvn install:install-file -DgroupId=de.dfki.mary -DartifactId=voice-dfki-prudence-hsmm -Dversion=5.2 -Dpackaging=jar -Dfile=lib/voice-dfki-prudence-hsmm-5.2.jar
* mvn install:install-file -DgroupId=de.dfki.mary -DartifactId=voice-cmu-bdl-hsmm -Dversion=5.2 -Dpackaging=jar -Dfile=lib/voice-cmu-bdl-hsmm-5.2.jar
* mvn install:install-file -DgroupId=de.dfki.mary -DartifactId=voice-dfki-poppy-hsmm -Dversion=5.2 -Dpackaging=jar -Dfile=lib/voice-dfki-poppy-hsmm-5.2.jar
* mvn install:install-file -DgroupId=de.dfki.mary -DartifactId=voice-cmu-rms-hsmm -Dversion=5.2 -Dpackaging=jar -Dfile=lib/voice-cmu-rms-hsmm-5.2.jar

##2.5 Lombok
Follow instructions from https://projectlombok.org/ to setup IDE.




