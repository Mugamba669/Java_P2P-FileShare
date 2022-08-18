/**
 * 
 */
package hub;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JOptionPane;

import librarifier.Constants;
import player.Player;

/**
 * @author Walid
 *
 */
public class Hub extends Thread {
	Socket socket;
	ServerSocket serverSocket;
	List<Player> listOfPlayers = new ArrayList<Player>();
	List<HubHandler> hubHandler = new ArrayList<HubHandler>();
	
	public Hub () {
		try {
			int port = Constants.HUB_PORT;
			serverSocket = new ServerSocket(port);
			JOptionPane.showMessageDialog(null, "Hub Started and listening on port : " + port, "Info Box" + "", JOptionPane.INFORMATION_MESSAGE);
			System.out.println("Hub Started and listening on port : " + serverSocket.getLocalPort());
			
			while (true) {
				socket = serverSocket.accept();
				
				HubHandler connection = new HubHandler(socket, this);
				connection.startHub();
				hubHandler.add(connection);
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	/* This method returns a list of at most 10 players to the selected player */
	/* The parameter player is the player that will receive the list */
	public List<Player> selectRandomPlayers (Player player) {
		if (this.listOfPlayers != null) {
			List<Player> players = new ArrayList<Player>();
			
			/* If there's more than 10 players available */
			if (this.listOfPlayers.size() - 1 > 10) {
				/* Get position of the player in the list to avoid returning it */
				int pos_player = -1;
				for (int i = 0; i < listOfPlayers.size(); i++) {
					if (listOfPlayers.get(i).getIp() == player.getIp() && listOfPlayers.get(i).getPort() == player.getPort()) {
						pos_player = i;
						break;
					}
				}
				
				if (pos_player != -1) {
					for (int i = 0; i < 10; i++) {
						Random rand = new Random();
						int position;
						/* To make sure we don't get the same player more than one time */
						if (i > 0) {
							int test;
							do {
								test = 0;
								/* In order not to get the player requesting the list */
								do {
									position = rand.nextInt(this.listOfPlayers.size());
								}
								while (position == pos_player);
								/* We Compare current randomly selected player with previous ones */
								for (int j = 0; j < players.size(); j++) {
									if (players.get(j).getIp() == this.listOfPlayers.get(position).getIp() && players.get(j).getPort() == this.listOfPlayers.get(position).getPort()) {
										test = 1;
										break;
									}
								}
							}
							while (test != 0);
						}
						else {
							do {
								position = rand.nextInt(this.listOfPlayers.size());
							}
							while (position == pos_player);
						}
						players.add(this.listOfPlayers.get(position));
					}
				}
			}
			else {
				for (int i = 0; i < this.listOfPlayers.size(); i++) {
					if (listOfPlayers.get(i).getIp() != player.getIp() && listOfPlayers.get(i).getPort() != player.getPort()) {
						System.out.println(listOfPlayers.get(i).getIp() + " " + player.getIp());
						System.out.println(listOfPlayers.get(i).getPort() + " " + player.getPort());
						players.add(this.listOfPlayers.get(i));
					}
				}
			}
			return players;
		}
		return null;
	}
	
	public static void main(String[] args) throws IOException {
		new Hub();
	}
}
