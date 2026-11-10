@echo off
setlocal
cd /d "%~dp0\.."
if not exist "build\v55-mock" mkdir "build\v55-mock"
set "ARQUIVOS=build\v55-mock\fontes.txt"
if exist "%ARQUIVOS%" del "%ARQUIVOS%"
for /r "tests\mock\stubs" %%F in (*.java) do @echo "%%F">>"%ARQUIVOS%"
for /r "core\src\main\java" %%F in (*.java) do @echo "%%F">>"%ARQUIVOS%"
echo "tests\mock\IntegrationMock.java">>"%ARQUIVOS%"
javac -encoding UTF-8 -d "build\v55-mock" @"%ARQUIVOS%"
if errorlevel 1 exit /b 1
java -cp "build\v55-mock" IntegrationMock
endlocal
