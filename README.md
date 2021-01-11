# WORLD OF WARCRAFT PVP LEADERBOARDS ANALYSIS
## Project Description
Grabs data from the WoW API and analyzes the data to produce statistics about about the current US Player Versus Player season.

User is able to input the number of top players they wish to analyze as well as the specific bracket. 

## Technologies Used
- Scala
- Scalatest (for Unit Testing)
- MongoDB (for data persistence)
- Logback (Logging)
- Spray-json (For json formatting)
- .env plugin (For utilizing environment variables)

## Features

### Current:

- Allows user to specify a pvp bracket and count of players between 1 and 4500. Then, a user can choose to see the following: 
    - Current Horde and Alliance player percentage
    - Total counts of players in each realm (realms with zero players excluded)
    - Average win percent, loss percent, and total matches played.
- User may also choose to run a "Quick Analysis" at any time when requested for input, which will allow the user to specify their desired bracket, player count, and analysis type all in one line.
- Analysis results are saved to a MongoDB Atlas cluster, so users can go back and look at results of previous analyses.
### To-Do:
- Add support for player class and player race analysis

## Requirements
- [JDK version 8 or 11](https://adoptopenjdk.net/).

- [Scala and SBT](https://www.scala-lang.org/download/2.12.8.html).

- ["dotenv" sbt plugin](https://github.com/mefellows/sbt-dotenv).

- 10 MBs of available storage space to store the generated JSON files

- Access credentials to [Blizzard's battle.net APIs](https://develop.battle.net/).

- Access credentials to a [MongoDB Atlas Cluster](https://www.mongodb.com/cloud/atlas/register)

    - These access credentials should be saved in a .env file located in the root directory of this project. This .env file should contain the user's Blizzard client ID, Blizzard client secret, Mongo Atlas default database name, MongoDB Atlas username, and MongoDB Atlas password in the following format:

    ID=<Blizzard Client ID>
    SECRET=<Blizzard Client Secret>
    DB_NAME=<Database Name>
    DB_USER=<Database User>
    DB_PASS=<Database Password>

## Usage
1. Ensure all above requirements are met
2. Navigate to your desired work folder and run the shell command:
```bash
git clone https://github.com/revature-scalawags/CodyProjectZero.git
```
3. Open your desired shell of choice, navigate to the downloaded "CodyProjectZero" folder, and run the following command:
```bash  
sbt --error run
```
4. Follow the instructions provided to navigate to your desired analysis.
