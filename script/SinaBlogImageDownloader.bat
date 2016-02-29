@echo off
Setlocal enabledelayedexpansion
::CODER BY xiaoyao9184
::TIME 2013-05-28
::FILE SinaBlogImageDownloader
::DESC run UTF8 swt/jar with UTF8 console in UTF8 JVM

:console encoding
chcp 65001

:java
java -Dfile.encoding=utf-8 -jar SinaBlogImageDownloader.jar

:end
exit