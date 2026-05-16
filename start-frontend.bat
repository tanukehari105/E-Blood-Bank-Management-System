@echo off
echo ============================================
echo  Blood Bank - Smart Healthcare Platform
echo  Starting Frontend (Java Swing)...
echo ============================================
cd /d "%~dp0frontend"
mvn compile exec:java
pause
