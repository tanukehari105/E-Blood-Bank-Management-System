@echo off
echo ============================================
echo  Blood Bank - Smart Healthcare Platform
echo  Starting Backend (Spring Boot)...
echo ============================================
cd /d "%~dp0backend"
mvn clean spring-boot:run
pause
