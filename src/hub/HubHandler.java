/**
 * 
 */
package hub;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.json.JSONArray;
import org.json.JSONObject;

import player.Player;

/**
 * @author Walid
 *
 */
public class HubHandler extends Thread {
	InputStream is;
	OutputStream os;
	Hub hub;
	Socket socket;
	BlockingQueue<Object> blockingQueue = new ArrayBlockingQueue<Object>(2000);
	
	public HubHandler (Socket socket, Hub hub) {
		super("Hub Connection");
		this.socket = socket;
		this.hub = hub;
	}
	
	public void startHub () {
		final Thread startHub = new Thread () {
			public void run () {
				try {
					is = socket.getInputStream();
					InputStreamReader isr = new InputStreamReader(is);
					BufferedReader br = new BufferedReader(isr);
					
					os = socket.getOutputStream();
					OutputStreamWriter osw = new OutputStreamWriter(os);
					BufferedWriter bw = new BufferedWriter(osw);
					
					while (is.available() == 0) {
						Thread.sleep(1);
					}
					
					String currentPlayer = socket.getInetAddress().toString() + " " + socket.getPort();
					
					/* Read message received from the player */
					String message = br.readLine();
					
					System.out.println("Message received from player " + currentPlayer + " is : " + message);
					
					/* Get player's default work directory and server port */
					String[] infos = message.split(",");
					String directory = infos[0];
					int playerAsServerPort = Integer.parseInt(infos[1]);
					
					System.out.println(directory +" "+ playerAsServerPort);
					
					/* Add the player to the list of available players */
					int test = 0;
					if (hub.listOfPlayers != null) {
						for (int i = 0; i < hub.listOfPlayers.size(); i++) {
							/* Checking if the player is already in the list */
							if (hub.listOfPlayers.get(i).getIp() == socket.getInetAddress().toString() && hub.listOfPlayers.get(0).getPort() == socket.getPort()) {
								test = 1;
							}
						}
						/* Add player to the list if he isn't already in it */
						if (test == 0) {
							Player player = new Player();
							player.setIp(socket.getInetAddress().toString());
							player.setPort(socket.getPort());
							player.setDirectory(directory);
							player.setPortAsServer(playerAsServerPort);
							hub.listOfPlayers.add(player);
						}
					}
					
					/* Send acknowledgement to the player about the connection */
					JSONObject ack = new JSONObject();
					ack.put("ack_connection", "");
							
					bw.write(ack.toString() + "\n");
					bw.flush();
					
					hubInOut(br, bw);
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		};
		startHub.start();
	}
	
	public void checkIfPlayerIsConnected (BufferedReader br, BufferedWriter bw) {
		final Thread checkAvailabilityThread = new Thread () {
			public void run () {
				try {
					JSONObject obj = new JSONObject();
					obj.put("req_availability", "");

					bw.write(obj.toString() + "\n");
					bw.flush();
					
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		checkAvailabilityThread.start();
	}
	
	public void hubInOut (BufferedReader br, BufferedWriter bw) {
		final Thread inOutThread = new Thread () {
			public void run () {
				try {
					/* If no message is received, wait to receive it */
					while (is.available() == 0) {
						Thread.sleep(1);
					}
					
					String currentPlayer = socket.getInetAddress().toString() + " " + socket.getPort();
					
					while (true) {
						/* Read message received from the player */
						String message = br.readLine();
						System.out.println("Message received from player " + currentPlayer + " is : " + message);
						
						/* Parse the JSON message */
						JSONObject obj = new JSONObject(message);
						if (obj.has("req_list")) {
							/* Get at most 10 random available players from the list */
							List<Player> players = new ArrayList<Player>();
							/* Get the already returned list to the players (null if it's the first request) */
							Player player = new Player();
							if (hub.listOfPlayers != null) {
								/* Get the concerned player */
								System.out.println("Address and port of p req the list : " + socket.getInetAddress().toString() + " " + socket.getPort());
								for (int i = 0; i < hub.listOfPlayers.size(); i++) {
									if (hub.listOfPlayers.get(i).getIp().equals(socket.getInetAddress().toString()) && hub.listOfPlayers.get(i).getPort() == socket.getPort()) {
										player = hub.listOfPlayers.get(i);
										break;
									}
								}
							}
							System.out.println("Player requesting the list : " + player.getIp() + " " + player.getPort());
							players = hub.selectRandomPlayers(player);
							
							/* Construct the JSON file (list of available players) */
							if (players != null) {
								/* If there's available players in the list */
								JSONObject obj3 = new JSONObject();
								JSONObject obj2 = new JSONObject();
								JSONArray array = new JSONArray();
								for (int i = 0; i < players.size(); i++) {
									JSONObject arrayElement = new JSONObject();
									arrayElement.put("port", players.get(i).getPortAsServer());
									arrayElement.put("ip", players.get(i).getIp());
									arrayElement.put("directory", players.get(i).getDirectory());
									array.put(arrayElement);
								}
								obj2.put("players", array);
								obj3.put("res_list", obj2);
								
								/* Send the list to the player */
								bw.write(obj3.toString() + "\n");
								System.out.println("Message sent to the player " + currentPlayer + " is : " + obj3.toString() + "\n");
								bw.flush();
							}
							else {
								/* if there's no available player in the list */
								JSONObject obj3 = new JSONObject();
								JSONObject obj2 = new JSONObject();
								JSONArray array = new JSONArray();
								JSONObject arrayElement = new JSONObject();
								array.put(arrayElement);
								obj2.put("players", array);
								obj3.put("res_list", obj2);
								
								/* Send the list to the player */
								bw.write(obj3.toString() + "\n");
								System.out.println("Message sent to the player " + currentPlayer + " is : " + obj3.toString() + "\n");
								bw.flush();
							}
						}
					}
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					try {
						is.close();
						os.close();
						socket.close();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
		inOutThread.start();
	}
}
