# Peer-to-peer (P2P) system for file sharing system

The goal of this project is to create a Java App for file sharing based on the BitTorrent protocol.

## Description of the architecture 

* The hub is a peer that serves the others players with the list of all connected players. the hub doesn't know anything about what the other players have.

* When running the Player the first time, it gets the ip address and the port of the Hub from the .libr file, connects to the Hub and request the list of connected players. 

* After getting the list, the player get the number of books from the .libr file and initialise a hashmap with the value 0 for each book number (as key).

* The Player connect to the other players getting their ip addresses and ports from the list of Players received previously.

* The Player fill a table with its missing books.
* The Player requests one random book from each connected player.
* If the other players have the requested book, they send it as binary file as described in the Protocol.md, the Player updates its missing books table and request another list of players from the *Hub*.

* If the other players don't have the requested book, the player request another list of Players from the Hub.

* The Player can play both roles in the same time, the server and client (it can get missing books from others and provide requested books that he has to others)
* once the Player has all the books, it triggers a merge function to merge all the books in order to get the complete stuff.

All the interactions between the peers (Hub and Players) happen through TCP sockets, the data type that we send through the Stream is in the form of binary data.

## How to run the code
- 1st step : Launch the Hub throught the class "Hub.java"
- 2nd step :
    - If on a single machine :
        - Run the player throught the class "PlayerMain.java"
        - Hit the button "Upload" 
        - Create a directory for this player, this directory will contain the library file that will be generated in the next step and also the books and the final file.
        - Choose a stuff to generate the library file (this is done throught the GUI file chooser)
        - Create as many directories for as many other players that you would want to have on your program, and copy the library file on all the created directories.
        - Launch as many instances of player as you want throught "PlayerMain.java" and hit the button "Download" and perceive with indications as shown in the GUI (same steps as before : Choose the created directory in the previous step and select the corresponding library file)
        - The players will receive the books.
    
    - If on multiple machines :
        - Connect to the same network
        - Launch a player instance throught "PlayerMain.java"
        - Hit "Upload" and repeat same steps as before
        - Pass the library file to the other players throught a flash drive
        - The other players will create a directory and put the library file in it and launch a player instance throught "PlayerMain.java" and repeat the same steps as explained before
        - The players will receive the books
