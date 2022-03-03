package connect4;
import java.sql.*;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.util.Map;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;



/* Accesses DB
 * Holds list of player objects
 */
public class Servermain {
	
	//list of all active players, pull from that list
	private static CopyOnWriteArrayList<Player> players;
	//list of all usernames to check for uniqueness
	private static ConcurrentHashMap<String,Player> users;
	
	private static String dbUrl = "jdbc:mysql://localhost:3306/connectfour";
	private static String dbUser = "root";
	private static String pwd = "root";
	
	private static ServerSocket ss;
	
	private static Connection conn;
	
	private static PreparedStatement st;
	
	private static ExecutorService es;
	
	//where we store boards, call player function assign & pass board to there
	
	
	/* Creates the server socket that waits 
	 * for clientmain to connect & send player info.
	 */
	public static void main(String[] args) {
		try{
			Scanner scan = new Scanner(System.in);
			while(!loginDatabase(scan)) {} // keep trying to log in to database until success
			System.out.println("Successfully logged in to database.");
			players = new CopyOnWriteArrayList<Player>();
			users = new ConcurrentHashMap<String,Player>();
			Class.forName("com.mysql.cj.jdbc.Driver");
			ss = new ServerSocket(10000);
			Socket s;
			while(true) {
				try {
					s = ss.accept();
					
					/* Check if guest or registered user or login
					 * Run either createGuest or createPlayer, pass in socket
					 */ 
					
					BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
					PrintWriter pr = new PrintWriter(s.getOutputStream());
					
					String action = readInput(br);
					
					boolean b =  false;
					
					
					if(action.equals("login")) {
						while(b==false) {
							b = logPlayer(br,pr,s);
						}
							
					}
					else if(action.equals("register")) {
						while(b==false) {
							b = createPlayer(br,pr,s);
						}
						
					}
					else if(action.equals("guest")) {
						while(b==false) {
							b = createGuest(br,pr,s);
						}
						
					}
					else if(action.equals("quit")) {
						br.close();
						pr.close();
						s.close();
					}
				} catch (SocketException e) {
					System.out.println("Incoming client connection dropped.");
				} catch (IOException e) {
					e.printStackTrace();
					System.out.println("A problem occurred with incoming client connection.");
				}
			}
			
			
			
		}
		catch(Exception e) {
			e.printStackTrace();
			System.out.println("Connection error.");
		}
	}
	
	/*
	 *
	 */
	private static boolean loginDatabase(Scanner scan) {
		System.out.println("Database username:");
		dbUser = scan.nextLine().trim();
		System.out.println("Database password:");
		pwd = scan.nextLine().trim();
		try {
			conn = DriverManager.getConnection(dbUrl,dbUser,pwd);
		} catch (SQLException e) {
			System.out.println(e.toString());
			System.out.println("Warning: Please run the connectfour.sql script if you haven't already!");
			System.out.println("Try again");
			return false;
		}
		return true;
	}
	
	/* [COMPLETE] Create GUEST player object & add to database & player list.
	 * Write "success" to client, return true.
	 * If DB error, writer "error" to client, return false.
	 */
	public static boolean createGuest(BufferedReader br, PrintWriter pr, Socket s) throws IOException {
		
		try {
			
			Random rng = new Random(0);
			String user = "Guest#" + Integer.toString(rng.nextInt(Integer.MAX_VALUE)%10000);
			
			while(users.containsKey(user)) { //if we already have this username, re-randomize
				user = "Guest#" + Integer.toString(rng.nextInt(Integer.MAX_VALUE)%10000);
			}
			
			st = conn.prepareStatement("INSERT INTO c4players VALUES(0,?,null,0)");
			st.setString(1, user);
			int success = st.executeUpdate();
			
			if(success>=1) {
				Thread p = new Player(s,br,pr,user,false);
				p.start();
				players.add((Player)p);
				users.put(user,(Player)p);
				pr.println("success");
				pr.flush();
				return true;
			}
			else {
				pr.println("error");
				pr.flush();
				return false;
			}
			
		}
		catch(SQLException e) {
			pr.println("error");
			pr.flush();
			e.printStackTrace();
			return false;
		}
	}
	
	/* [COMPLETE] Create player object & add to database & player list.
	 * Increment player count.
	 * Verify that username is unique. If so, write "success" to client, return true.
	 * If not, write "error" to client, return false.
	 */
	public static boolean createPlayer(BufferedReader br, PrintWriter pr, Socket s) throws IOException {
		try {
			//check for username in DB
			String user = readInput(br);
			
			String pass = hashPasscode(readInput(br));
			
			if(!users.containsKey(user) && user.length()<=50 && pass.length()<=64) { 
				//if DB does not contain username & user/pass is valid, add to DB 
				st = conn.prepareStatement("INSERT INTO c4players VALUES(0,?,?,1);");
				st.setString(1, user);
				st.setString(2, pass);
				int success = st.executeUpdate();
				
				if(success >= 1) { //update successful
					//create player, add to list
					Thread p = new Player(s,br,pr,user,true);
					p.start();
					
					//TODO: p.setUser(user) //etc etc
					players.add((Player)p);
					users.put(user,(Player)p);
					pr.println(("success"));
					pr.flush();
					return true;
				}
			}
			else {
				pr.println("error");
				pr.flush();
				return false;
			}
			
			
//				st = conn.prepareStatement("SELECT * FROM c4players WHERE username = ?");
//				st.setString(1, user);
//				ResultSet result = st.executeQuery();
//				
//				if(result.next() || users.find(user)) { //if we get a match, invalid - 
//					//write response to socket
//					ostream.write(("Username taken - enter a unique username.").getBytes());
//					ostream.flush();
//					System.out.println("Enter a unique username.");
//					return false;
//				}
			
				
			
			
		}
		catch(SQLException e) {
			if(e.getSQLState().startsWith("23")) {
				pr.println("error");
				pr.flush();
				e.printStackTrace();
				return false;
			}
		}
		/**catch(Exception e) {
			pr.println("error");
			e.printStackTrace();
			return false;
		}**/
		
		return false;
		
	}
	
	public static String readInput(BufferedReader br) throws IOException {
		String input = "";
		//System.out.println(br.toString());
		while(true) {
			//System.out.println("Eeee");
			input = br.readLine().trim();
			if((input!=null) && !input.isEmpty()) {
				//System.out.println("read!");
				return input;
			}
		}
	}
	
	
	
	/* [COMPLETE] Logs player in.
	 * Checks username, hashed password in DB.
	 * If match, write "success" to client.
	 * If not, write "error" to client.
	 */
	public static boolean logPlayer(BufferedReader br, PrintWriter pr, Socket s) throws IOException {
		
		try{
			String user ="";
			String pass = "";
			user = readInput(br);
			
			pass = readInput(br);
			
			
			st = conn.prepareStatement("SELECT username,password FROM c4players WHERE username = ?");
			st.setString(1, user);
			ResultSet results = st.executeQuery();
			
			if(results.next()) {
				String hashPass = hashPasscode(pass);
				String resultPass = results.getString("password");
				
				if(hashPass.equals(resultPass)) {
					Thread p = new Player(s,br,pr,user,true);
					p.start();
					users.put(user,(Player)p);
					players.add((Player)p);
					pr.println("success");
					pr.flush();
					return true;
				}
			}
				
		}
		catch(SQLException e) {
			System.out.println("login exception!");
			e.printStackTrace();
			pr.println("error");
			pr.flush();
			return false;
		}
		
		pr.println("error");
		pr.flush();
		return false;
	}
	
	/*[COMPLETE] Hashes passed-in passcode.
	 * Returns a string in hexadecimal.
	 * If error, returns null.
	 */
	public static String hashPasscode(String pass) {
		
		try{
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] byteHash = md.digest(pass.getBytes(StandardCharsets.UTF_8));
			
			StringBuilder hashPass = new StringBuilder(byteHash.length * 2);
			for(int i=0; i<byteHash.length; i++) {
				String hexa = Integer.toHexString(0xff & byteHash[i]);
				if(hexa.length() == 1) {
					hashPass.append('0');
				}
				hashPass.append(hexa);
			}
			return hashPass.toString();
		}
		
		catch(NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	
	
	/* [COMPLETE] Called by a player seeking to play game.
	 * Search for player & check if they're available for a game.
	 * If they're available, return player2 object.
	 * If not available, return null.
	 */
	public static Player findPlayer(String p2) {
		
		Player playerTwo = users.get(p2);
		
		if(playerTwo != null && !playerTwo.isPlaying()) {
			return playerTwo;
		}
		
		return null;
	}
	
	/* [COMPLETE] Called by a player looking for a random
	 *  opponent, to find another player looking for a 
	 *  random opponent.
	 *  If they're looking for a random opponent, return player2 object.
	 *  If they aren't, return null.
	 */
	public static Player randomPlayer(String p1) {
		
		for(Map.Entry<String,Player> entry : users.entrySet()) {
			if(!((entry.getKey()).equals(p1)) && entry.getValue().isWaiting()) {
				return entry.getValue();
			}
		}
		
		return null;
	}
	
	
	
}
