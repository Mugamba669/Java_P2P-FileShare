/**
 * 
 */
package player;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import librarifier.FileSplit;
import librarifier.LibraryFile;

/**
 * @author Walid
 *
 */
public class PlayerMain {
	static String action;
	
	public static void main(String[] args) throws IOException {
		Player player = new Player();
		action = "a";
		
		JFrame frame = new JFrame();
		frame.setTitle("Download or Upload stuff ?");
		frame.setSize(400, 200);
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
		int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
		frame.setLocation(x, y);
		frame.getContentPane().setLayout(null);
		frame.setVisible(true);
		
		while (!action.equals("d") && !action.equals("u")) {
			JButton download_btn = new JButton("Download");
			download_btn.setBounds(50, 60, 100, 30); /* Distance from left, Distance from top,length of button, height of button */
			frame.getContentPane().add(download_btn);
			download_btn.addActionListener(new ActionListener() {
	
				@Override
				public void actionPerformed(ActionEvent arg0) {
					// TODO Auto-generated method stub
					action = "d";
					frame.dispose();
				}
			});
	
			JButton upload_btn = new JButton("Upload");
			upload_btn.setBounds(230, 60, 100, 30); /* Distance from left, Distance from top,length of button, height of button */
			frame.getContentPane().add(upload_btn);
			upload_btn.addActionListener(new ActionListener() {
	
				@Override
				public void actionPerformed(ActionEvent arg0) {
					// TODO Auto-generated method stub
					action = "u";
					frame.dispose();
				}
			});
		}
		
		String directory = null;
		while(directory == null) {
			JOptionPane.showMessageDialog(null, "Please choose a default directory to store files", "Info Box" + "", JOptionPane.INFORMATION_MESSAGE);
			System.out.println("Please choose a default directory to store files");
			directory = Player.selectDirectory("Select directory");
			player.setDirectory(directory);
		}
		
		/* Download file */
		if (action.equals("d")) {
			String library = null;
			String library_name = null;
			while (library == null) {
				JOptionPane.showMessageDialog(null, "Please select the library file of the stuff to download", "Info Box" + "", JOptionPane.INFORMATION_MESSAGE);
				System.out.println("Please select the library file");
				library = Player.selectLibraryFile("Select the library file");
				File original_file = new File (library);
				library_name = original_file.getAbsolutePath().substring(original_file.getAbsolutePath().lastIndexOf("\\")+1);
			}
			
			try {
				Files.move(Paths.get(library), Paths.get(directory + "\\" + library_name), StandardCopyOption.REPLACE_EXISTING);
				/* Setting library file for player and connecting to the hub */
				player.setLibrary(directory + "\\" + library_name);
				
				player.setPortAsServer(-1);
				
				/* Starting player as server */
				PlayerServer playerAsServer = new PlayerServer(player);
				playerAsServer.startPlayerAsServer();
				
				do {
					
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				while(player.getPortAsServer() == -1);
				
				/* Connect with hub */
				player.connectToHub();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				JOptionPane.showMessageDialog(null, "Error while moving the library file to the chosen directory", "Info Box" + "", JOptionPane.INFORMATION_MESSAGE);
				System.out.println("Error while moving the library file to the chosen directory");
				e.printStackTrace();
			}
		}
		
		/* Upload file */
		if (action.equals("u")) {
			String stuff = null;
			String file_name = null;
			while (stuff == null) {
				JOptionPane.showMessageDialog(null, "Please select the stuff to upload", "Info Box" + "", JOptionPane.INFORMATION_MESSAGE);
				System.out.println("Please select the stuff to upload");
				stuff = Player.selectStuff("Select the stuff");
				File original_file = new File (stuff);
				file_name = original_file.getAbsolutePath().substring(original_file.getAbsolutePath().lastIndexOf("\\")+1);
			}
			
			/* Generate the library file & split the stuff into books*/
			/* Split stuff into books */
			List<File> books = new ArrayList<File>();
			/* Books will be generated in the default directory */
	        books = FileSplit.splitFile(new File(stuff), directory);
	        player.setBooks(books);
	        /* Generate the library file in the default directory */
	        player.setLibrary(LibraryFile.libraryFile(books, stuff, directory));
			
			player.setPortAsServer(-1);
			
			/* Starting player as server */
			PlayerServer playerAsServer = new PlayerServer(player);
			playerAsServer.startPlayerAsServer();
			
			do {
				
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			while(player.getPortAsServer() == -1);
			
	        /* Connecting to the hub */
	        player.connectToHub();
	        
			try {
				Files.move(Paths.get(stuff), Paths.get(directory + "\\" + file_name), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				JOptionPane.showMessageDialog(null, "Error while moving the stuff to the chosen directory", "Info Box" + "", JOptionPane.INFORMATION_MESSAGE);
				System.out.println("Error while moving the stuff to the chosen directory");
				e.printStackTrace();
			}
		}
	}
}
