/**
 * 
 */
package player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.json.JSONArray;
import org.json.JSONObject;

import librarifier.BookToBytes;

/**
 * @author Walid
 *
 */
public class PlayerServerHandler {
	Player player;
	Socket socket;
	BlockingQueue<Object> blockingQueue = new ArrayBlockingQueue<Object>(2000);

	public PlayerServerHandler (Player player, Socket socket) {
		this.player = player;
		this.socket =socket;
	}

	public void sendBooks () {
		final Thread playerServer = new Thread () {
			public void run () {
				try {

					ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

					InputStream is = socket.getInputStream();
					InputStreamReader isr = new InputStreamReader(is);
					BufferedReader br = new BufferedReader(isr);

					while (is.available() == 0) {
						Thread.sleep(1);
					}

					/* Read message received from the hub */
					String message_received = br.readLine();

					blockingQueue.put(message_received);

					String message = (String) blockingQueue.take();

					System.out.println("Books that the player " + player.getIp() + " " + player.getPortAsServer() + " is requesting : " + message);

					/* Getting the requested player's missing books */
					int[] missing_books = player.missingBooks();
					System.out.print("The books i'm missing : [");
					for (int j = 0; j < missing_books.length; j++) {
						System.out.print(missing_books[j] + ", ");
					}
					System.out.println("]");

					/* Parse the JSON message */
					JSONObject obj1 = new JSONObject(message);
					JSONArray requested_books = null;
					if (obj1.has("req_books")) {
						requested_books = obj1.getJSONArray("req_books");
					}

					System.out.println("requested books");
					for (int j = 0; j < requested_books.length(); j++) {
						System.out.println(requested_books.get(j).toString());
					}

					/* Testing if the player has any of the requested books */
					List<Integer> to_send = new ArrayList<Integer>();
					for (int j = 0; j < requested_books.length(); j++) {
						int test = 0;
						for (int k = 0; k < missing_books.length; k++) {
							if (requested_books.getInt(j) != missing_books[k]) {
								test++;	
							}
						}
						if (test == missing_books.length) {
							to_send.add(requested_books.getInt(j));
						}
					}						

					/* Transforming list to array */
					System.out.println("books i have to send : " + to_send.size());
					int[] books_to_send = new int[to_send.size()];
					for (int j = 0; j < to_send.size(); j++) {
						books_to_send[j] = to_send.get(j);
						System.out.println(books_to_send[j]);
					}
					System.out.println();
					System.out.println("number of books i have to send : " + books_to_send.length);

					/* If there's available books to send */
					if (books_to_send.length > 0) {
						JSONObject books_sending = new JSONObject();
						JSONObject obj2 = new JSONObject();
						obj2.put("books", Arrays.toString(books_to_send));
						obj2.put("id", 1);
						books_sending.put("res_player", obj2);

						System.out.println("Message sent to player is : " + books_sending.toString());
						
						oos.writeObject(books_sending.toString());

						/* Get the books to send */
						List<byte[]> books_bytes = new ArrayList<byte[]>();
						for (int j = 0; j < books_to_send.length; j++) {
							books_bytes.add(BookToBytes.bookToBytes(player.getDirectory() + "\\Book" + books_to_send[j]));
						}
						
						oos.writeObject(books_bytes);
					}
					/* If there's no available books to send */
					else {
						JSONObject books_sending = new JSONObject();
						JSONObject obj2 = new JSONObject();
						obj2.put("id", 2);
						books_sending.put("res_player", obj2);

						oos.writeObject(books_sending.toString());
					}
					
				} catch (IOException | InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		playerServer.start();
	}
}
