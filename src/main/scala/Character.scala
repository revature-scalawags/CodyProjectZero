import java.io.FileNotFoundException
// BELOW CODE IS NOT CURRENTLY BEING USED
    
    //Class used for the function that follows
    class Character(val name: String, val realm: String, val playable_class: String, val race: String, val current_spec: String) {

        def getCharacterInfo(charInfo: Array[Player], token: String): Array[Character] = {
        val results = new Array[Character](charInfo.size)
        var index = 0
        for (player <- charInfo) {
                val url = "https://us.api.blizzard.com/profile/wow/character/" + player.realm + "/" + player.name + "/appearance?namespace=profile-us&locale=en_US&access_token=" + token
                try {
                    val info = scala.io.Source.fromURL(url).mkString
                    var sub = info.substring(info.indexOf("\"name\"") + 7)
                    for (i <- 1 to 2) {
                        sub = sub.substring(sub.indexOf("\"name\"") + 7)
                    }
                    val race = sub.substring(1, sub.indexOf(",") - 1)
                    sub = sub.substring(sub.indexOf("\"name\"") + 7)
                    val playable_class = sub.substring(1, sub.indexOf(",") - 1)
                    sub = sub.substring(sub.indexOf("\"name\"") + 7)
                    val current_spec = sub.substring(1, sub.indexOf(",") - 1)
                    results(index) = new Character(player.name, player.realm, playable_class, race, current_spec)
                } catch {
                    case e: FileNotFoundException => {
                        println("Could not retrieve info for character \"" + player.name + "\"")
                        results(index) = new Character(player.name, player.realm, "unknown", "unknown", "unknown")
                    }
                }
                index += 1
        }
        for (i <- results) println(i.name)
        results
    }
    }

    /** This method retrieves additional data about individual characters that showed up in the original API call
      * However, it currently takes too long to execute this function when the number of characters grows beyond 10 or so
      * As such, it will not be used until it can be optimized
      * @param charInfo base character information (name and realm) used to retrieve other data
      * @param token token generated from original API call to authorize additional calls
      * @return returns an array of more detailed character objects
      */