#!/bin/bash
echo "Starting Blood Bank Frontend..."
cd frontend
mvn compile exec:java -Dexec.mainClass="com.bloodbank.ui.BloodBankApp"
