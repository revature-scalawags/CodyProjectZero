# **Cody P's Project Zero**
# World of Warcraft PvP Leaderboards Basic Analysis
Grabs data from the WoW API to analyze data about the current US Player Versus Player season.

User will be able to input the number of top players they wish to analyze as well as the specific bracket. 

Analysis includes:
> Alliance player percent vs Horde player percent.

> Average win percent, loss percent, and total matches played.

> Total counts of players in each realm (realms with zero players excluded).

## Requirements
> JDK version 8 or 11 (https://adoptopenjdk.net/).

> Scala and SBT (https://www.scala-lang.org/download/2.12.8.html).

> "dotenv" sbt plugin (https://github.com/mefellows/sbt-dotenv).

> 10 MBs of available storage space to store the generated JSON files

>Access credentials to Blizzard's battle.net APIs (https://develop.battle.net/).

>Access credentials to a MongoDB Atlas cluster

>> These access credentials should be saved in a .env file located in the root directory of this project.
This .env file should contain the user's Blizzard client ID, Blizzard client secret, Mongo Atlas default database name, MongoDB Atlas username, and MongoDB Atlas password in the following format:

    ID=<Blizzard Client ID>
    SECRET=<Blizzard Client Secret>
    DB_NAME=<Database Name>
    DB_USER=<Database User>
    DB_PASS=<Database Password>

## Usage
1. Ensure all above requirements are met
2. Download the project files in this repository
3. Open your desired shell of choice, navigate to the downloaded "CodyProjectZero" folder, and run the following command:
    
    sbt --error run

4. Follow the instructions provided to navigate to your desired analysis.
    > User may also choose to run a "Quick Analysis" at any time when requested for input, which will allow the user to specify their desired bracket, player count, and analysis type all in one line.

    >> Format: [bracket] [player count] [analysis type]

    >> Example: 2v2 500 f

## Known Errors
Occasionally, the program will declare that your input is invalid, when it is in fact falid. Simply try again when this happens and the program should continue as normal.
