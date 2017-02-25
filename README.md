# TypeTalk
MaryTTS frontend with batteries included.

#1. Usage
If you are using a Linux system the easiest way to install TypeTalk is by using your package management system. We provide .deb and .rpm packages. Download the appropriate package from [https://github.com/TypeTalk/TypeTalk/releases/latest](https://github.com/TypeTalk/TypeTalk/releases/latest), install it through your package manager and you are ready to go. If you are on a system that is not capable of handling .deb or .rpm files, download the platform independent jar file from [https://github.com/TypeTalk/TypeTalk/releases/latest](https://github.com/TypeTalk/TypeTalk/releases/latest) and run it using the command: `java -jar TypeTalk-jar-with-dependencies.jar`

#2. Build

##2.1 Swingutils
1. Download swingutils from [https://github.com/raginggoblin/swingutils/archive/master.zip](https://github.com/raginggoblin/swingutils/archive/master.zip)
2. unzip sources to a temporary directory
3. run 'mvn install' inside this directory

##2.2 TypeTalk
1. Clone repo: 'git clone https://github.com/TypeTalk/TypeTalk.git'
2. run 'mvn package' inside the TypeTalk directory

##2.3 Lombok
If you want to build TypeTalk using an IDE, follow instructions from https://projectlombok.org/ to setup your IDE.




