@echo off

if exist rhyme.jar ( del rhyme.jar )

javac -encoding utf-8 *.java
if errorlevel 1 ( goto end )

jar cvfm rhyme.jar manifest.mf splash.jpg *.class dic/*.dic

:end
del *.class
