import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Path;

public class Client{
	
	/** Read the PDF file. */
	public static void main(String[] args) throws IOException {
		Path p1 = Paths.get("README.txt");
		Path p2 = Paths.get("WRITEME.txt");
		Reader r = new Reader(p1, p2);
		
		r.one();
		r.combo23();
		r.one();
		
		while(true) {
			int i = r.four();
			
			if(i == 1) {
			}
			else if (i == 2) {
				r.combo23();
			}
			else {
				r.three();
			}
			r.one();
		}
	}
}
