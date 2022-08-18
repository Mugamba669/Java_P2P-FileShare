/**
 * 
 */
package librarifier;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author walid
 *
 */
public class BooksToSHA1 {
	/* Code inspired from : http://www.mkyong.com/java/how-to-generate-a-file-checksum-value-in-java/ */
    public static List<String> booksToSHA1 (List<File> books) throws IOException {
        try {
        	List<String> sha1 = new ArrayList<String>();
        	for (int j = 0; j < books.size(); j++) { 
        		if (books.get(j) != null) {
		        	byte[] bytes = Files.readAllBytes(books.get(j).toPath());
		            MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
		            byte[] digestedBytes = messageDigest.digest(bytes);
		        
		            /* Convert the digestedBytes to hexadecimal format to reduce the size of the output */
		            String result = "";
		            for (int i = 0; i < digestedBytes.length; i++) {
		                result += Integer.toString((digestedBytes[i] & 0xff) + 0x100, 16).substring(1);
		            }
		            sha1.add(result);
        		}
        		else {
        			sha1.add(null);
        		}
            }
        	return sha1;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
		return null;        
    }
}
