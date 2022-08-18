/**
 * 
 */
package player;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;

import librarifier.BookToBytes;
import librarifier.FileSplit;

/**
 * @author Walid
 *
 */
public class PlayerClient extends Thread {
	Player player;
	Socket socket;
	//	BlockingQueue<String> blockingQueue;
	BlockingQueue<Object> blockingQueue = new ArrayBlockingQueue<Object>(2000);
	BlockingQueue<Object> blockingQueue1 = new ArrayBlockingQueue<Object>(2000);

	public PlayerClient (Socket socket, Player player) {
		super("Player Connection");
		this.player = player;
		this.socket = socket;
	}

	public void playerDirectory () {
		final Thread myDirectory = new Thread () {
			public void run () {
				try {
					System.out.println("Hub : " + socket.getPort() + " " + socket.getInetAddress());

					InputStream is = socket.getInputStream();
					InputStreamReader isr = new InputStreamReader(is);
					BufferedReader br = new BufferedReader(isr);

					OutputStream os = socket.getOutputStream();
					OutputStreamWriter osw = new OutputStreamWriter(os);
					BufferedWriter bw = new BufferedWriter(osw);

					/* Sending player directory and server port to the hub */
					String my_info = player.getDirectory() + "," + player.getPortAsServer();

					bw.write(my_info.toString() + "\n");
					bw.flush();
					System.out.println("Message sent to the hub is : " + my_info.toString() + "\n");

					/* We must receive acknowledgment from hub connecting */
					String message = br.readLine();

					/* Parse the JSON message */
					JSONObject obj1 = new JSONObject(message);
					if (obj1.has("ack_connection")) {
						/* Check if the player is missing some books */
						if (player.missingBooks().length > 0) {
							playerInOut(player);
						}
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		myDirectory.start();
	}

	public void playerInOut (Player player) {
		final Thread inOutThread = new Thread () {
			public void run () {
				try {
					InputStream is = socket.getInputStream();
					InputStreamReader isr = new InputStreamReader(is);
					BufferedReader br = new BufferedReader(isr);

					OutputStream os = socket.getOutputStream();
					OutputStreamWriter osw = new OutputStreamWriter(os);
					BufferedWriter bw = new BufferedWriter(osw);

					/* Requesting list from the hub */
					JSONObject obj = new JSONObject();
					obj.put("req_list", "");

					bw.write(obj.toString() + "\n");
					bw.flush();
					System.out.println("Message sent to the hub is : " + obj.toString() + "\n");

					/* Read message received from the hub */
					String message = br.readLine();

					/* Parse the JSON message */
					JSONObject obj1 = new JSONObject(message);
					if (obj1.has("res_list")) {
						String[] keys = JSONObject.getNames(obj1);
						Object value = obj1.get(keys[0]);

						JSONObject obj2 = new JSONObject(value.toString());
						JSONArray players = obj2.getJSONArray("players");
						/* If the list of players returned by the hub is empty */
						if (players.isEmpty()) {
							/* Wait for a moment and re-request the list from the hub */
							System.out.println("Message received from the hub : " + message);
							TimeUnit.SECONDS.sleep(10);
							playerInOut(player);
						}
						else {
							String library_file = new File(player.getLibrary()).getAbsolutePath().substring(new File(player.getLibrary()).getAbsolutePath().lastIndexOf("\\")+1);
							List<Player> players_returned = new ArrayList<Player>();
							for (int i = 0; i < players.length(); i++) {
								JSONObject player = players.getJSONObject(i);
								Player p = new Player();
								p.setIp(player.getString("ip"));
								p.setPortAsServer(player.getInt("port"));
								p.setDirectory(player.getString("directory"));
								/* Getting the library file name */
								String library = player.getString("directory") + "\\" + library_file;
								p.setLibrary(library);
								players_returned.add(p);
							}
							player.currentPlayers = players_returned;

							System.out.println("Message received from the hub : " + message);


							/* Connect the player to the other players in the returned list */
							connectToPlayers(player);
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		inOutThread.start();
	}

	//	public void connectToPlayers (Player player) {
	//		try {
	//			while (true) {
	//				System.out.println("je suis ici 2");
	//				Socket socket_player_server = player.serverSocket.accept();
	////				requestBooks(socket, player);
	//				
	//				PlayerToPlayer playerToPlayer = new PlayerToPlayer(socket, player, this);
	//				requestBooks(socket_player_server, player);
	//			}
	//		} catch (IOException e) {
	//			// TODO Auto-generated catch block
	//			e.printStackTrace();
	//		}
	//	}

	public boolean checkIfAllBookReceived (Player player) {
		/* Testing if the player is missing any books before requesting */
		try {
			int[] missingBooks = player.missingBooks();

			if (missingBooks.length == 0) {
				/* Player has all the books */
				System.out.println("Player has all the needed books");

				/* Generating the final file as the player has all needed books */
				List<File> books = new ArrayList<File>();
				int numberOfBooks = 0;
				String final_file_name = "";

				/* Getting the number of books from the library file */
				BufferedReader brr = new BufferedReader (new FileReader(player.getLibrary()));
				String line = "";
				/* SHA1 info starts from line 5 on the library file */
				for (int i = 0; i < 4; i++) {
					line = brr.readLine();
					if (i == 1) {
						final_file_name = line;
					}
				}
				while ((line = brr.readLine()) != null) {
					numberOfBooks++;
				}
				brr.close();

				/* Checking if final file already exists */
				File final_file = new File(player.getDirectory() + "\\" + final_file_name);
				if (!(final_file.exists() && !final_file.isDirectory())) {
					/* Getting the books that are in the directory */
					for (int i = 0; i < numberOfBooks; i++) {
						File book = new File(player.getDirectory() + "\\Book" + i);
						if(book.exists() && !book.isDirectory()) { 
							books.add(i, book);
						}
					}

					/* Creating final file */
					FileSplit.mergeFiles(books, final_file);

					/* Disconnecting */
					//					player.serverSocket.close();
					return true;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public void connectToPlayers (Player player) throws IOException {
		Socket[] sockets = new Socket[player.currentPlayers.size()];
		for (int i = 0; i < player.currentPlayers.size(); i++) {
			String host = player.currentPlayers.get(i).getIp();
			int port = player.currentPlayers.get(i).getPortAsServer();

			System.out.println(host + " " + port);
			System.out.println(host.substring(1));
			sockets[i] = new Socket (host.substring(1), port);
		}

		requestBooks(sockets, player);
	}

	public void requestBooks (Socket[] sockets, Player player) {
		final Thread requestBooksThread = new Thread () {
			public void run () {
				try {

					int answers = 0;

					for (int i = 0; i < sockets.length; i++) {

						ObjectInputStream ois = new ObjectInputStream(sockets[i].getInputStream());

						OutputStream os = sockets[i].getOutputStream();
						OutputStreamWriter osw = new OutputStreamWriter(os);
						BufferedWriter bw = new BufferedWriter(osw);

						while (true) {
							/* Getting the player's missing and put it them in a JSON file */
							JSONArray obj1 = new JSONArray();
							JSONObject obj = new JSONObject();
							int[] missingBooks = player.missingBooks();
							//obj.put("req_books", Arrays.toString(missingBooks));
							//obj1.put(Arrays.toString(missingBooks));
							for (int j = 0; j < missingBooks.length; j++) {
								obj1.put(missingBooks[j]);
							}
							obj.put("req_books", obj1);

							/* Requesting the missing books from other players */
							bw.write(obj.toString() + "\n");
							System.out.println("Do you have these books : " + obj.toString() + " ? \n");
							bw.flush();

							/* Reading the response */

							Object o = ois.readObject();

							blockingQueue.put(o);


							Object message = blockingQueue.take();
							System.out.println(message.toString());

							if (message.getClass() == String.class) {
								System.out.println("Message received from player is : " + message.toString());

								/* Parse the JSON message */
								JSONObject obj2 = new JSONObject(message.toString());

								/* Getting response from players about the books that they have */
								if (obj2.has("res_player")) {
									String[] keys = JSONObject.getNames(obj2);

									/* The player has some of (or all) the requested books */
									if (obj2.get(keys[0]).toString().equals("{\"id\":1}")) {
										System.out.println("id = 1");
									}

									/* The player has none of the requested books */
									else if (obj2.get(keys[0]).toString().equals("{\"id\":2}")) {
										System.out.println("id = 2");
										answers++;
									}
								}
							}
							/* If blockingQueue has books in it */
							else if (message.getClass() == ArrayList.class) {
								System.out.println("books arrived");
								/* Getting the books received */
								List<byte[]> books_received = new ArrayList<byte[]>();
								books_received = (List<byte[]>) message;

								/* Checking if the books received are valid */
								for (int  j= 0; j < books_received.size(); j++) {
									BookToBytes.bytesToBook(books_received.get(j), player);
								}

								if (checkIfAllBookReceived(player) == true) {
									break; 
								}
								answers++;
							}

							/* If the player got responses from all players but still needs other books */
							if (answers == player.currentPlayers.size()) {
								/* Ask the hub for a new list */
								//						player.serverSocket.close();
								//						playerInOut(player);
								System.out.println("JE SOOOOOOOOOOOOOOOORS");
								break;
							}
						}
						/* If player still needs books request a new list from hub */
						if (answers == player.currentPlayers.size() && checkIfAllBookReceived(player) == false) {
							playerInOut(player);
						}
						/* If the player got all the needed books disconnect */
						else if (checkIfAllBookReceived(player) == true) {
							ois.close();
							os.close();
							for (int j = 0; j < sockets.length; j++) {
								sockets[i].close();
							}
						}
						
					}

				} catch (IOException | ClassNotFoundException | InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		requestBooksThread.start();
	}




}
