/**
 * 
 */
package player;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import librarifier.BooksToSHA1;

/**
 * @author Walid
 *
 */
public class Player {
	private String ip;
	private int port;
	private int portAsServer;
	private String library;
	private String finalFile;
	private String directory;
	private List<File> books;
	/* This list contains the last list of available players returned by the hub */
	public List<Player> currentPlayers;
	PlayerClient playerClient;
	ServerSocket serverSocket;
	Socket socket;
	Socket socketClient;
	InputStream is;
	OutputStream os;
	
	public Player () {
		
	}
	
	public Player (String ip, int port, String library, List<File> books) {
		this.ip = ip;
		this.port = port;
		this.library = library;
		this.books = books;
	}
	
	public String getIp () {
		return ip;
	}
	
	public void setIp (String ip) {
		this.ip = ip;
	}
	
	public int getPort () {
		return port;
	}
	
	public void setPort (int port) {
		this.port = port;
	}
	
	public int getPortAsServer () {
		return portAsServer;
	}
	
	public void setPortAsServer (int portAsServer) {
		this.portAsServer = portAsServer;
	}
	
	public String getLibrary () {
		return library;
	}
	
	public void setLibrary (String library) {
		this.library = library;
	}
	
	public String getFinalFile () {
		return finalFile;
	}
	
	public void setFinalFile (String finalFile) {
		this.finalFile = finalFile;
	}
	
	public String getDirectory () {
		return directory;
	}
	
	public void setDirectory (String directory) {
		this.directory = directory;
	}
	
	public List<File> getBooks () {
		return books;
	}
	
	public void setBooks (List<File> books) {
		this.books = books;
	}
	
	public static String selectDirectory (String title) {
		String directory;
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new java.io.File("."));
		chooser.setDialogTitle(title);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			directory = chooser.getSelectedFile().toString();
			return directory;
		} 
		return null;
	}

	public static String selectLibraryFile (String title) {
		String file;
		JFileChooser chooser = new JFileChooser();
		chooser.addChoosableFileFilter(new FileNameExtensionFilter("Library files", "libr"));
		chooser.setCurrentDirectory(new java.io.File("."));
		chooser.setDialogTitle(title);
		chooser.setAcceptAllFileFilterUsed(false);
		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			file = chooser.getSelectedFile().toString();
			return file;
		} 
		return null;
	}

	public static String selectStuff (String title) {
		String file;
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new java.io.File("."));
		chooser.setDialogTitle(title);
		chooser.setAcceptAllFileFilterUsed(false);
		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			file = chooser.getSelectedFile().toString();
			return file;
		} 
		return null;
	}
	
	/* This method checks the player's directory and returns the books that he has */
	public int[] missingBooks () throws IOException {
		List<String> sha1_libr = new ArrayList<String>();
		List<File> books = new ArrayList<File>();
		int numberOfBooks = 0;

		/* Getting the number of books and SHA1 from the library file */
		BufferedReader br = new BufferedReader (new FileReader(this.library));
		String line = "";
		/* SHA1 info starts from line 5 on the library file */
		for (int i = 0; i < 4; i++) {
			line = br.readLine();
		}
		while ((line = br.readLine()) != null) {
		    sha1_libr.add(line);
		    numberOfBooks++;
		}
		br.close();
		
		/* Getting the books that are in the directory */
		for (int i = 0; i < numberOfBooks; i++) {
			File book = new File(directory + "\\Book" + i);
			if(book.exists() && !book.isDirectory()) { 
			    books.add(i, book);
			}
			else {
				books.add(i, null);
			}
		}
		
		/* Get SHA1 of books from player's directory */
		List<String> sha1_player = new ArrayList<String>();
		sha1_player = BooksToSHA1.booksToSHA1(books);
		
		/* Compare the player's SHA1 data with the library file's */
		List<Integer> invalid_books = new ArrayList<Integer>();
		for (int i = 0; i < numberOfBooks; i++) {
			if (books.get(i) == null) {
				invalid_books.add(i);
				continue;
			}
			else {
				if (!sha1_libr.get(i).equals(sha1_player.get(i))) {
					invalid_books.add(i);
				}
			}
		}
		
		int[] missing_books = new int[invalid_books.size()];
		if (invalid_books != null) {
			for (int i = 0; i < missing_books.length; i++) {
				missing_books[i] = invalid_books.get(i);
			}
		}
		
		return missing_books;
	}
	
	public String[] getHubAddress () throws IOException {
		BufferedReader br = new BufferedReader (new FileReader(library));
		String line = "";
		line = br.readLine();
		br.close();
		String[] address = line.split(":");
		return address;
	}
	
	public void connectToHub () throws IOException {
		/* Get hub address and port from library file */
		String[] address = getHubAddress();
		String host = address[0];
		int port = Integer.parseInt(address[1]);
		InetAddress addr = InetAddress.getByName(host);
		socket = new Socket (addr, port);

		playerClient = new PlayerClient(socket, this);
		playerClient.playerDirectory();
	}
}
