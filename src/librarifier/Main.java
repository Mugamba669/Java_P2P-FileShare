/**
 * 
 */
package librarifier;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import player.Player;

/**
 * @author walid
 *
 */
public class Main {
	public static void main(String[] args) throws IOException {
		/* Split stuff into books */
		List<File> books = new ArrayList<File>();
		System.out.println("Please select the stuff");
		String stuff = Player.selectStuff("Select the stuff");
		System.out.println("Now select where to store the books");
		String path_books = Player.selectDirectory("Select where to store the books");
        books = FileSplit.splitFile(new File(stuff), path_books);
        
        /* Merge books into file */
        /* Get extension of original file */
        String extension = "";
        String file = new File(stuff).getName();
        int lastIndexOf = file.lastIndexOf(".");
        if (lastIndexOf != -1) {
            extension = file.substring(lastIndexOf);
        }
        System.out.println("Please select where to store the final file");
        String directory = Player.selectDirectory("Select where to store the final file") + "\\final_file" + extension;
        FileSplit.mergeFiles(books, new File(directory));
        
        /* Generate .libr file */
        System.out.println("Please select where to store the library file");
        String path = Player.selectDirectory("Select where to store the library file");
        LibraryFile.libraryFile(books, stuff, path);
    }
}
