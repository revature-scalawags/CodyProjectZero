# **Cody P's Project Zero**
# World of Warcraft PvP Leaderboards Basic Analysis
Grabs data from the WoW API to analyze data about the current US Player Versus Player season.

User will be able to input the number of top players they wish to analyze as well as the specific bracket. 

Analysis includes:
> Alliance player percent vs Horde player percent

> Average win percent, loss percent, and total matches played

> Total counts of players in each realm (realms with zero players excluded)

## Requirements
> JDK version 8 or 11 (https://adoptopenjdk.net/)

> Scala and SBT (https://www.scala-lang.org/download/2.12.8.html)

> "dotenv" sbt plugin (https://github.com/mefellows/sbt-dotenv)

>Access credentials to Blizzard's battle.net APIs (https://develop.battle.net/)

>> These access credentials should be saved in a .env file located in the root directory of this project.
This .env file should contain the user's Blizzard client ID and Blizzard client secret in the following format:

    ID=<Blizzard Client ID here>
    SECRET=<Blizzard Client Secret here>

> 10 MBs of available storage space to store the generated csv files

## Usage
1. Download the project files in this repository
2. Ensure all above requirements are met
3. Navigate to the downloaded "CodyProjectZero" folder and run the following command:
    
    sbt --error run

4. Follow the instructions provided to navigate to your desired analysis.

## Known Errors
Occasionally, the program will declare that your input is invalid, when it is in fact falid. Simply try again when this happens and the program should continue as normal.
