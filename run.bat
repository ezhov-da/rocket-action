@echo off
cd /d %~dp0
start "run" "%JAVA_HOME%\bin\javaw" -jar -Xmx256m rocket-action.jar "path-to-configuration"