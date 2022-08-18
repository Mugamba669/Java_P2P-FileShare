/**
 * 
 */
package player;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Walid
 *
 */
public class PlayerServer {
	Player player;
	List<PlayerServerHandler> connections = new ArrayList<PlayerServerHandler>();
	
	public PlayerServer (Player player) {
		this.player = player;
		try {
			player.serverSocket = new ServerSocket(0);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void startPlayerAsServer () {
		final Thread playerAsServer = new Thread () {
			public void run () {
				try {
					player.serverSocket = new ServerSocket(0, -1, InetAddress.getLocalHost());
					
					player.setPortAsServer(player.serverSocket.getLocalPort());
					System.out.println(InetAddress.getLocalHost().getHostAddress() + " " + player.serverSocket.getLocalPort());
					
					player.setIp(player.serverSocket.getInetAddress().getHostAddress());
					
					while (true) {
						player.socketClient = player.serverSocket.accept();
						
						PlayerServerHandler psh = new PlayerServerHandler(player, player.socketClient);
						psh.sendBooks();
						connections.add(psh);
					}
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		playerAsServer.start();
	}
}
