/**
 * 
 */
package librarifier;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * @author walid
 *
 */
public class FileSplit {
	/* Code inspired from : https://stackoverflow.com/questions/10864317/how-to-break-a-file-into-pieces-using-java */
	public static List<File> splitFile (File f, String path) throws IOException {
		int bookCounter = 0;
		List<File> books = new ArrayList<File>();

		int sizeOfBook = 2048; // 2KB
		byte[] buffer = new byte[sizeOfBook];

		/* try-with-resources to ensure closing stream */
		try (FileInputStream fis = new FileInputStream(f); BufferedInputStream bis = new BufferedInputStream(fis)) {

			int bytesAmount = 0;
			while ((bytesAmount = bis.read(buffer)) > 0) {
				/* write each book into separate files with different number in name */
				String filePartName = String.format("%s%d", "Book", bookCounter++);
				File newFile = new File(path, filePartName);
				try (FileOutputStream out = new FileOutputStream(newFile)) {
					out.write(buffer, 0, bytesAmount);
				}
				books.add(newFile);
			}
			return books;
		}
	}

	public static void mergeFiles (List<File> books, File resultFile) throws IOException {
		try (FileOutputStream fos = new FileOutputStream(resultFile);
			BufferedOutputStream mergingStream = new BufferedOutputStream(fos)) {
			for (File f : books) {
				Files.copy(f.toPath(), mergingStream);
			}
		}
	}
}
