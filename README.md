# CSCI201Project_Connect4
Team: Anh Nguyen, Maia Cho, Saleem Bekkali, Matthew Rosenthal, Dylan Drain

## Connect4 main working code (src):
- Servermain: creates GameBoard, threads for each Player
  - saving login data (user, pass): SQL database
  - Player findPlayer(String name)
    - Player randomPlayer(String name) for random matchmaking
- Clientmain: each Player, implements log in, register, lobby search (by Player name)
  - Prompt login, pulls data or makes new one
  - Ensures all entries from client are valid
- GameBoard: checks for winner, add() to insert tokens, print()
- Player: handles random matchmaking, outgoing and incoming synchronized invites (only for registered users)
  - takeTurn() implements lock-condition signaling so players take turn one at a time
  - void run() sends lobby and game commands to Clientmain
    - checks for winner and ends games, going back to lobby
