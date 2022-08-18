/**
 * 
 */
package librarifier;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * @author walid
 *
 */
public class LibraryFile {
	public static String libraryFile (List<File> books, String stuff, String path) {
		try {
			List<String> sha1 = new ArrayList<String>();
	        sha1 = BooksToSHA1.booksToSHA1(books);
	        /* File content */
			List<String> lines = new ArrayList<String>();
			lines.add(InetAddress.getLocalHost().getHostAddress() + ":1234");
			String stuff_name = new File(stuff).getAbsolutePath().substring(new File(stuff).getAbsolutePath().lastIndexOf("\\")+1);
			lines.add(stuff_name);
			lines.add(Long.toString(new File(stuff).length()));
			lines.add(Integer.toString(Constants.SIZE_OF_BOOK));

	        stuff_name = stuff_name.substring(0, stuff_name.lastIndexOf('.'));
	        File file = new File(path + "\\" + stuff_name + ".libr");
	
	        /* If file doesn't exist then create it */
	        if (!file.exists()) {
	            file.createNewFile();
	        }
	
	        FileWriter fw = new FileWriter(file.getAbsoluteFile());
	        BufferedWriter bw = new BufferedWriter(fw);
	
	        /* Write in file */
	        for (String str: lines) {
	        	bw.write(str);
	        	bw.newLine();
	        }
	        for (int i = 0; i < sha1.size(); i++) {
	        	bw.write(sha1.get(i));
	        	/* Test to not have an empty line at the end */
	        	if (i != sha1.size() - 1) {
	        		bw.newLine();
	        	}
	        }
	
	        /* Close file */
	        bw.close();

	        return (path + "\\" + stuff_name + ".libr");
	    }
	    catch(Exception e){
	        System.out.println(e);
	    }
		return null;
	}
}
