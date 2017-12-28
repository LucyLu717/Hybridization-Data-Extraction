import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

public class Reader {
	
	private BufferedReader br;
	private BufferedWriter bw;
	private String[] list = new String[2]; // store the lines that have been read
	private String line = ""; // the line read by BufferedReader br
	private int index = 0; // keep track of the size of the list and position of the line that is being processed
	private boolean flag = false; // true iff there is a *
	
	/** Initialize the reader. */
	public Reader(Path p, Path p1) throws IOException {
		InputStream is = Files.newInputStream(p);
		InputStreamReader ir = new InputStreamReader(is);
		br = new BufferedReader(ir);
		bw = Files.newBufferedWriter(p1);
		
	}
	
	/** Write the name of the species under study,
	 *  and the name of the first species that
	 *  hybridizes with this species listed. */
	protected void combo23() throws IOException{
		stepTwo();
		stepOne();
		stepThree();
	}
	
	/** Read in a line. */
	protected void one() throws IOException{
		stepOne();
	}
	
	/** Continue to write the name of the species that
	 *  hybridizes with this species listed. */
	protected void three() throws IOException{
		stepThree();
	}
	
	/** Return an integer that indicates the step next. */
	protected int four() throws IOException{
		return stepFour();
	}

	/** Step 1: Read in a line. */
	private void stepOne() throws IOException {
		line = readNextLine();
		
		// Skip when the line is empty 
		// or the content after the star is not a species name
		if(line.length()<8) {
		}
		
		// Store lines in two slots, for such a situation:
		// Species name
		// [...]
		else if(index==0) {
			list[0] = line;
			index = 1;
		}	
		else {
			list[1] = line;
			index = 0;
		}  
	}
	
	/** Read the next line and return the line that is read. */
	private String readNextLine() throws IOException {
		String line1 = br.readLine();
		
		//Exit the program when reaching the end of the text
		if(line1 == null) {
			br.close();
			bw.flush();
			bw.close();
			System.exit(0);
		}
		
		// If line contains *
		if(line1.contains("*")) {
			flag= true;
			line1 = br.readLine();
		}
		
		return line1;
	}
	
	/** Step 2: Write the name of the species under study. */
	private void stepTwo() throws IOException{
		System.out.println("I'm 2");	
		
		int i= 0; // the index of [
		int j= 0; // the indes of (
		
		while(line.length() >= 0) {
			i = lookForLeft();
			j = lookForPar();
			
			if(j != -1 && j < i) {
				writeName(j, 0);	  // For situation: name () []
				break;
			}
			
			if (i != -1) {
				writeName(i, 0);
				break;
			}
			
			stepOne(); // If there is neither ( nor [, read a new line
		}
	}
	
	/** Return the index of [ in the line.
	 *  Return -1 if no [ is found. */
	private int lookForLeft() {
		return line.indexOf('[');
	}
	
	/** Return the index of ( in the line.
	 *  Return -1 if no ( is found. */
	private int lookForPar() {
		return line.indexOf('(');
	}
	
	/** Write in the output file the name of the species .
	 *  ind is the index of [ or : in the line.
	 *  x represents different situations:
	 *  		0 - a new species under study
	 *  		1 - a hybrid species
	 *  		2 - "See also" is in the line. */
	private void writeName(int ind, int x) throws IOException{
		if (ind > 2 && ind < 6 && line.indexOf(')') == -1) // Skip if there's irrelevant []
			return;
		
		if(x == 0)
			bw.newLine(); // Write the name of a new species
		else
			bw.append(","); // Write the name of a hybridizing species
		
		String newline= "";
		
		// [ is the beginning of a new line 
		// OR 
		// (...
		// ...) []
		if(ind < 2 || ind < 6 && line.indexOf(')') != -1) { 
			newline = list[index];
			int j= newline.indexOf('('); // name()[]
			if(j != -1)
				newline= newline.substring(0, j);
		}
		
		// "See:"
		else if(x == 2) { 
			String linez = line;	
			while (line.indexOf('.') == -1 
					|| line.lastIndexOf('.') != line.length() -1 
						||line.charAt(line.lastIndexOf('.')+1) != ' '){
						stepOne();
						linez += line;
					}
			line = linez;
			
			// name; name
			int punc = lookForSemi(); 
				
			if(line.contains("*")) {
				int star = line.indexOf('*');
				
				//See: xxx * xxx.
				if (punc == -1) {
					return;
					}
				
				//See: ...; xxx * xxx; ...
				else if (punc < star){
					String linex = line.substring(0, star);
					linex = linex.substring(0, linex.lastIndexOf(';')+1);
					
					String liney = line.substring(star);
					liney = liney.substring(liney.indexOf(';')+1);
						
					line = linex + liney;
				}
				
				//See: xxx * xxx; ...
				else {
					line = line.substring(punc);
				}
			}
				
			if(punc != -1) {
				newline = line.substring(ind+1, punc);
				bw.append(newline.trim());
				bw.append(",");
				line = line.substring(punc+1);
				
				punc = lookForSemi();
				if(punc == -1) {
					int j= line.lastIndexOf('.'); // name; name.
					newline= line.substring(0, j);
					bw.append(newline.trim());
					return;
				}
					
				while(line.length()>0 && punc != -1) {
					newline = line.substring(0, punc);
					bw.append(newline.trim());
					bw.append(",");
					line = line.substring(punc+1);
					punc = lookForSemi();
				}
				return;
			}
		
			else {
				punc = lookForPeriod();
				newline = line.substring(ind+1, punc); // name.
			}
		}	

		else
			newline = line.substring(0, ind); // name []
		
		bw.append(newline.trim());
		
	}
	
	/** Step 3: Write in the output file the name of the species 
	 *  that hybridizes with the species under study. */
	public void stepThree() throws IOException{
		System.out.println("I'm 3");
		
		if (containsSee() == true) {
			int colon= lookForColon();
			if(colon == -1) // Contains "See" but there's no colon
				colon = 7;
			writeName(colon, 2);
			return;
		}
		
		if(flag == false)
			return;

		int left = lookForLeft();
		
		while(left == -1) {
			stepOne();
			left= lookForLeft();
		}
		
		flag= false;
		
		int j= lookForPar(); // name () []
		if(j != -1 && j < left)
			left= j;
		
		writeName(left, 1);
		
	}
	
	/** Return the index of the parenthesis ( between x and [.
	 *  Return -1 if none is found. */
	private int lookForColon() {
		return line.indexOf(':');
	}
	
	/** Return an integer that indicates the step next. */
	private int stepFour() {
		System.out.println("I'm 4");
		
		int left = lookForLeft();
		
		if(flag == true && left == -1) // There's no [
			return 1;
		
		if(flag == true || containsSee() == true) // Contains "See also"
			return 3;
		
		if(left != -1) // name () []
			return 2;
		
		return 1;
	}
	
	/** Return true iff the line starts with "See".
	 *  Return false otherwise.
	 *  
	 *  Because the text is converted from PDF format, 
	 *  there are spaces between the letters, which is why 
	 *  the code is testing the letters; in some cases, 
	 *  the program fails to recognize "See" even if 
	 *  it is at the beginning of the line. */
	private boolean containsSee() {
		if(line.length()<6)
			return false;
		
		if(line.charAt(1) == 'S' &&
				line.charAt(3) == 'e' &&
					line.charAt(5) == 'e') {
			return true;
		}
		return false;
	}
	
	/** Return the index of .
	 *  Return -1 if none is found. */
	private int lookForPeriod() {
		return line.indexOf(".");
	}
	
	/** Return the index of ;
	 *  Return -1 if none is found. */
	private int lookForSemi() {
		return line.indexOf(";");
	}
}
