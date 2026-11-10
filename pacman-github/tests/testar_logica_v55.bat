@echo off
setlocal
cd /d "%~dp0\.."
if not exist "build\v55-testes" mkdir "build\v55-testes"
javac -encoding UTF-8 -d "build\v55-testes" ^
 core\src\main\java\org\pacman\model\Direcao.java ^
 core\src\main\java\org\pacman\model\MapaJogo.java ^
 core\src\main\java\org\pacman\model\Pacman.java ^
 core\src\main\java\org\pacman\model\Fantasma.java ^
 core\src\main\java\org\pacman\model\RankingPartidas.java ^
 core\src\main\java\org\pacman\ai\EstrategiaPerseguicao.java ^
 core\src\main\java\org\pacman\util\CarregadorMapa.java ^
 tests\V55SmokeTests.java
if errorlevel 1 exit /b 1
java -cp "build\v55-testes" V55SmokeTests assets\maps\mapa_v5.txt
endlocal
