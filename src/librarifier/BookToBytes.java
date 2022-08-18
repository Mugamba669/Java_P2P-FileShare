/**
 * 
 */
package librarifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import player.Player;

/**
 * @author Walid
 *
 */
public class BookToBytes {
	public static byte[] bookToBytes (String book) {
		try {
			File file = new File(book);
			FileInputStream fin = new FileInputStream(file);

			byte fileContent[] = new byte[(int)file.length()];
			fin.read(fileContent);
			fin.close();
			return fileContent;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static File bytesToBook (byte[] bytes, Player player) {
		try {
			/* Verify SHA1 of the received book */
			File f = new File(player.getDirectory() + "\\Book");
//			File f = new File("C:\\Users\\Walid\\Desktop\\Book");
			FileOutputStream fos = new FileOutputStream(f);
			fos.write(bytes);
			fos.close();

			List<File> book = new ArrayList<File>();
			book.add(f);

			/* Generate SHA1 from received file */
			String sha1 = BooksToSHA1.booksToSHA1(book).get(0);

			/* Compare SHA1 of received file with sha1 from library file */
			List<String> sha1_libr = new ArrayList<String>();

			/* Getting the number of books and SHA1 from the library file */
			BufferedReader br = new BufferedReader (new FileReader(player.getLibrary()));
//			BufferedReader br = new BufferedReader (new FileReader("C:\\Users\\Walid\\Desktop\\Backlog.libr"));
			String line = "";
			/* SHA1 info starts from line 5 on the library file */
			for (int i = 0; i < 4; i++) {
				line = br.readLine();
			}
			while ((line = br.readLine()) != null) {
				sha1_libr.add(line);
			}
			br.close();

			int bookReceived = -1;
			for (int i = 0; i < sha1_libr.size(); i++) {
				if (sha1_libr.get(i).equals(sha1)) {
					bookReceived = i;
				}
			}

			/* If book received is not valid */
			if (bookReceived == -1) {
				f.delete();
				return null;
			}
			/* If book received is valid */
			else {
				f.delete();
				f = new File(player.getDirectory() + "\\Book" + bookReceived);
//				f = new File("C:\\Users\\Walid\\Desktop\\new\\Book" + bookReceived);
				fos = new FileOutputStream(f);
				fos.write(bytes);
				fos.close();
				return f;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
}
