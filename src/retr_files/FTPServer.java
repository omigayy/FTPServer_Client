// Programmer: Yingying Wu
// Description: File Transfer Protocol (FTP), Client and Server – Step 2
// date last modified: 02/05/2016 2:45
// status: all codes tested and work fine
// TODO: horrible user command check.. too tired to optimize

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class FTPServer {

static int retrTime = 1; // for use in file name
static boolean hasPort = false; // is port called(before retr
static String outputHP = "";
static String host = "";
static int port;
	
//a function to determine if input str is all ASCII
private static boolean isAllASCII(String input) {
    boolean isASCII = true;
    for (int i = 0; i < input.length(); i++) {
        int c = input.charAt(i);
        if (c > 0x7F) {
            isASCII = false;
            break;
        }
    }
    return isASCII;
}

//a function to determine if input str has only Crlf
private static boolean hasOnlyCrlf(String input) {
    boolean hasOnlyCrlf = true;
    for (int i = 0; i < input.length(); i++) {
        int c = input.charAt(i);
        if (c != 13 && c!=10) {
        	hasOnlyCrlf = false;
            break;
        }
    }
    return hasOnlyCrlf;
}

//a function to print out all ASCII code of a string (for test use
//private static void printAllChar(String input) {
//    for (int i = 0; i < input.length(); i++) {
//        int c = input.charAt(i);
//        System.out.println(c);
//    }
//}

//check if port command is correct
//execute if correct
public static boolean hostportCheck(String hostport){
	int lastChar, lastButOneChar;
	if (isAllASCII(hostport)){
		lastChar = hostport.charAt(hostport.length() - 1);
		lastButOneChar = hostport.charAt(hostport.length()-2);
		if(hasOnlyCrlf(hostport)){
			return false;
		}
		
		// CRLF and ASCII correct, start processing
		else{
			StringTokenizer hpToken = new StringTokenizer(hostport,".//,");
			ArrayList<Integer> hp = new ArrayList<Integer>();
			while (hpToken.hasMoreTokens()){
				hp.add(Integer.parseInt(hpToken.nextToken()));
			}
			if (hp.size()!=6){
				return false;
			}
			if(!inRange(hp)){
				return false;
			}
			else{
				outputHP = "";
				for (int i =0;i<4;i++){
					outputHP= outputHP + Integer.toString(hp.get(i)) + ".";
				}
				outputHP = outputHP.substring(0,outputHP.length()-1);
				host = outputHP;
				port = convertPort(hp.get(4),hp.get(5));
				outputHP = outputHP + "," + Integer.toString(port);
				hasPort = true;
				return true;
			}
		}
	}
	else {
		return false;
	}
}

//a function to check if the integers in an arraylist are in range 0-255
private static boolean inRange(ArrayList<Integer> a){
	for (int i=0;i<a.size();i++){
		if (a.get(i)<0 || a.get(i)>255)
			return false;
	}
	return true;
}

//a function to delete any "\" or "/" at the beginning of a string
private static String deleteBegin(String path){
	int first;
	first = path.charAt(0);
	if (first == 47 || first == 92){
		path = path.substring(1,path.length());
	}
	return path;
}

//a function to convert port-number
private static int convertPort(int a, int b){
	return (256*a+b);
}

//check if quit command is correct
//execute if correct
//private static void checkQuit(String token){
//	int lastC = token.charAt(token.length()-1);
//	int lastButOneC = token.charAt(token.length()-2);
//	int fifthChar=0;
//	if (token.length()>4){
//		fifthChar = token.charAt(4);
//	}
//	if (fifthChar!=13 && fifthChar!=10 && fifthChar!=0){
//		outToClient.writeBytes("500 Syntax error, command unrecognized.\n");
//	}
//	else if(lastButOneC==13 && lastC==10){
//		outToClient.writeBytes("200 Command OK.\n");
//		System.exit(0);
//	}
//	else{
//		outToClient.writeBytes("501 Syntax error in parameter.\n");
//	}
//}

//check if syst command is correct
//give reply if correct
//private static void checkSyst(String token){
//	int lastC = token.charAt(token.length()-1);
//	int lastButOneC = token.charAt(token.length()-2);
//	int fifthChar=0;
//	if (token.length()>4){
//		fifthChar = token.charAt(4);
//	}
//	if (fifthChar!=13 && fifthChar!=10 && fifthChar!=0){
//		outToClient.writeBytes("500 Syntax error, command unrecognized.\n");
//	}
//	else if(lastButOneC==13 && lastC==10){
//		outToClient.writeBytes("215 UNIX Type: L8.\n");
//	}
//	else{
//		outToClient.writeBytes("501 Syntax error in parameter.\n");
//	}
//}

//check if noop command is correct
//give reply if correct
//private static void checkNoop(String token){
//	int lastC = token.charAt(token.length()-1);
//	int lastButOneC = token.charAt(token.length()-2);
//	int fifthChar=0;
//	if (token.length()>4){
//		fifthChar = token.charAt(4);
//	}
//	if (fifthChar!=13 && fifthChar!=10 && fifthChar!=0){
//		outToClient.writeBytes("500 Syntax error, command unrecognized.\n");
//	}
//	else if(lastButOneC==13 && lastC==10){
//		outToClient.writeBytes("215 UNIX Type: L8.\n");
//	}
//	else{
//		outToClient.writeBytes("501 Syntax error in parameter.\n");
//	}
//}

//execute a retr command
//copy file of givin path to a file in retr_files dir
private static void executeRetr(String para, DataOutputStream outToClient){
	para = deleteBegin(para);
	File fileIn = new File(para);
	FileInputStream fis = null;
	try {
		if (fileIn.exists()){
			System.out.println("150 File status okay.\n");
			outToClient.writeBytes("150 File status okay.\n");
			outToClient.flush();
			fis = new FileInputStream(fileIn);
			
			Socket dataSocket;
			try{
				dataSocket = new Socket(host,port);
			}catch(Exception e){
				System.out.println("425 Can not open data connection.\n");
				outToClient.writeBytes("425 Can not open data connection.\n");
				outToClient.flush();
				return;
			}
			
			if(!dataSocket.isConnected()){
				System.out.println("425 Can not open data connection.\n");
				outToClient.writeBytes("425 Can not open data connection.\n");
				outToClient.flush();
				dataSocket.close();
				return;
			}
			
			DataOutputStream outToServer = new DataOutputStream(dataSocket.getOutputStream());
			
			int content;
			while((content =fis.read())!=-1){
				outToServer.writeByte(content);
			}
			dataSocket.close();
			
			System.out.println("250 Requested file action completed.\n");
			outToClient.writeBytes("250 Requested file action completed.\n");
			outToClient.flush();
			
			hasPort = false;
		}
		else{
			System.out.println("550 File not found or access denied.\n");
			outToClient.writeBytes("550 File not found or access denied.\n");
			outToClient.flush();
		}
	} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}
	
public static void main(String[] args) throws IOException {
	int portNumber;
    try {
    	portNumber = Integer.parseInt(args[0]);
    } catch (IndexOutOfBoundsException e) {
        System.err.println("Please input a valid host for the connection or hardcode the host");
        throw e;
    } 
	
	
//=========== variable declaration =============
//		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in)); // 0130 revised
//		int item;//used to read char from file
//		int tmpFirstChar = -1;//indicator of whether need to add a char back to the beginning of a command
	
		int lastChar, lastButOneChar;//used to check CRLF
		boolean loggedIn = false,needPass = false;//indicators of whether expecting a "user" or a "pass" command
		String token = null;//store command
		String para = "";// store command
		
//		ArrayList<Character> command = new ArrayList<Character>();//store command
//		StringBuilder commandStrB = new StringBuilder();//store command
		
		String commandStr = null;//store command
		String clientSentence = null;//sentence from client
		Socket connectionSocket = null;
		BufferedReader inFromClient = null;
		DataOutputStream outToClient = null;
		ServerSocket welcomeSocket = new ServerSocket(portNumber);
		
// ================= read file until EOF =============
		do{
			para = "";
			
			if (connectionSocket == null){
				connectionSocket = welcomeSocket.accept();
				
				inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
	
				outToClient = new DataOutputStream(connectionSocket.getOutputStream());
				
				System.out.println("220 COMP 431 FTP server ready.\n");
				outToClient.writeBytes("220 COMP 431 FTP server ready.\n");
				outToClient.flush();
			}
			
			clientSentence = null;
			clientSentence = inFromClient.readLine();
			if(clientSentence != null){
				System.out.println(clientSentence);
			}
			else{
				connectionSocket = welcomeSocket.accept();
				
				inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
	
				outToClient = new DataOutputStream(connectionSocket.getOutputStream());
				
				System.out.println("220 COMP 431 FTP server ready.\n");
				outToClient.writeBytes("220 COMP 431 FTP server ready.\n");
				outToClient.flush();
				continue;
			}
			
			
			
			commandStr = clientSentence;
			
			// parse command str w/ space
			StringTokenizer st;
			
			st = new StringTokenizer(commandStr," ");
			
	// ====================== command check ====================================
			if (commandStr.charAt(0)== ' '){
				System.out.println("500 Syntax error, command unrecognized.\n");
				outToClient.writeBytes("500 Syntax error, command unrecognized.\n");
				outToClient.flush();
			}// command starts w/ space
			
			else if (st.hasMoreTokens()) {
				token = (String) st.nextToken();
				token.toLowerCase();
				
				String shortCmd = "";
				if(token.length()>=4){
					shortCmd = token.substring(0,4);
				}
				else{
					System.out.println("500 Syntax error, command unrecognized.\n");
					outToClient.writeBytes("500 Syntax error, command unrecognized.\n");
					outToClient.flush();
					continue;
				}
				
				// =================== quit valid at any point ===================
				if(shortCmd.equalsIgnoreCase("quit")){
					int lastC = token.charAt(token.length()-1);
					int lastButOneC = token.charAt(token.length()-2);
					int fifthChar=0;
					if (token.length()>4){
						fifthChar = token.charAt(4);
					}
//					if (fifthChar!=13 && fifthChar!=10 && fifthChar!=0){
//						System.out.println("500 Syntax error, command unrecognized.\n");
//						outToClient.writeBytes("500 Syntax error, command unrecognized.\n");
//						continue;
//					}
					
					System.out.println("221 Goodbye.\n");
					outToClient.writeBytes("221 Goodbye.\n");
					outToClient.flush();
					connectionSocket.close();
					connectionSocket = null;
					loggedIn = false;
					needPass = false;
					inFromClient = null;
					outToClient = null;
					continue;
//					
//					else{
//						System.out.println("501 Syntax error in parameter.\n");
//						outToClient.writeBytes("501 Syntax error in parameter.\n");
//						continue;
//					}
				}// ================================================================
				
				// =================== check user command if haven't logged in ===================
				if (loggedIn == false){
					if (token.equalsIgnoreCase("user")){
						while(st.hasMoreTokens()){
							para = para + (String) st.nextToken();
						}
						if (para == ""){
							System.out.println("501 Syntax error in parameter.\n");
							outToClient.writeBytes("501 Syntax error in parameter.\n");
							outToClient.flush();
							continue;
						}
						else if (isAllASCII(para)){
							lastChar = para.charAt(para.length() - 1);
							lastButOneChar = para.charAt(para.length()-2);
							if(hasOnlyCrlf(para)){
								System.out.println("501 Syntax error in parameter.\n");
								outToClient.writeBytes("501 Syntax error in parameter.\n");
								outToClient.flush();
								continue;
							}//"user CRLF
//							else if(lastButOneChar!=13 || lastChar!=10){
//								System.out.println("501 Syntax error in parameter.\n");
//								outToClient.writeBytes("501 Syntax error in parameter.\n");
//								continue;
//							}//CRLF error
							else{
								System.out.println("331 Guest access OK, send password.\n");
								outToClient.writeBytes("331 Guest access OK, send password.\n");
								outToClient.flush();
								loggedIn = true;
								needPass = true;
								continue;
							}
						}
						else {
							System.out.println("501 Syntax error in parameter.\n");
							outToClient.writeBytes("501 Syntax error in parameter.\n");//has nonASCII char
							outToClient.flush();
							continue;
						}
					}//user
					else{
						System.out.println("530 Not logged in.\n");
						outToClient.writeBytes("530 Not logged in.\n");
						outToClient.flush();
						continue;
					}
				}
				
			// ================ check pass command immediately after a valid user command ==============
				if (needPass == true){
					if(token.equalsIgnoreCase("pass")){
						while(st.hasMoreTokens()){
							para = para + (String) st.nextToken();
						}
						if (para == ""){
							System.out.println("501 Syntax error in parameter.\n");
							outToClient.writeBytes("501 Syntax error in parameter.\n");
							outToClient.flush();
							continue;
						}
						else if (isAllASCII(para)){
							lastChar = para.charAt(para.length() - 1);
							lastButOneChar = para.charAt(para.length()-2);
							if(hasOnlyCrlf(para)){
								System.out.println("501 Syntax error in parameter.\n");
								outToClient.writeBytes("501 Syntax error in parameter.\n");
								outToClient.flush();
								continue;
							}
//							else if(lastButOneChar!=13 || lastChar!=10){
//								System.out.println("501 Syntax error in parameter.\n");
//								outToClient.writeBytes("501 Syntax error in parameter.\n");
//								continue;
//							}
							else{
								System.out.println("230 Guest login OK.\n");
								outToClient.writeBytes("230 Guest login OK.\n");
								outToClient.flush();
								needPass = false;
								continue;
							}
						}
						else {
							System.out.println("501 Syntax error in parameter.\n");
							outToClient.writeBytes("501 Syntax error in parameter.\n");
							outToClient.flush();
							continue;
						}
					}//password
					else {
						System.out.println("503 Bad sequence of commands.\n");
						outToClient.writeBytes("503 Bad sequence of commands.\n");
						outToClient.flush();
						continue;
					}
				}
				
				// // =================== check user command after logged in ===================
				if (token.equalsIgnoreCase("user")){
					while(st.hasMoreTokens()){
						para = para + (String) st.nextToken();
					}
					if (para == ""){
						System.out.println("501 Syntax error in parameter.\n");
						outToClient.writeBytes("501 Syntax error in parameter.\n");
						outToClient.flush();
						continue;
					}
					else if (isAllASCII(para)){
						lastChar = para.charAt(para.length() - 1);
						lastButOneChar = para.charAt(para.length()-2);
						if(hasOnlyCrlf(para)){
							System.out.println("501 Syntax error in parameter.\n");
							outToClient.writeBytes("501 Syntax error in parameter.\n");
							outToClient.flush();
							continue;
						}//"user CRLF
//						else if(lastButOneChar!=13 || lastChar!=10){
//							System.out.println("501 Syntax error in parameter.\n");
//							outToClient.writeBytes("501 Syntax error in parameter.F\n");
//							continue;
//						}//CRLF error
						else{
							System.out.println("331 Guest access OK, send password.\n");
							outToClient.writeBytes("331 Guest access OK, send password.\n");
							outToClient.flush();
							needPass = true;
							continue;
						}
					}
					else {
						System.out.println("501 Syntax error in parameter.\n");
						outToClient.writeBytes("501 Syntax error in parameter.\n");//has nonASCII char
						outToClient.flush();
						continue;
					}
				}//user
				
				// =================== check retr command ===================
				if(token.equalsIgnoreCase("retr")){
					while(st.hasMoreTokens()){
						para = para + (String) st.nextToken();
					}
					if (para == ""){
						System.out.println("501 Syntax error in parameter.\n");
						outToClient.writeBytes("501 Syntax error in parameter.\n");
						outToClient.flush();
						continue;
					}
					else if (isAllASCII(para)){
						lastChar = para.charAt(para.length() - 1);
						lastButOneChar = para.charAt(para.length()-2);
						if(hasOnlyCrlf(para)){
							System.out.println("501 Syntax error in parameter.\n");
							outToClient.writeBytes("501 Syntax error in parameter.\n");
							outToClient.flush();
							continue;
						}
//						else if(lastButOneChar!=13 || lastChar!=10){
//							System.out.println("501 Syntax error in parameter.\n");
//							outToClient.writeBytes("501 Syntax error in parameter.\n");
//							continue;
//						}
						else{
							if(hasPort){
								executeRetr(para,outToClient);
								continue;
							}// check if there is a port command called before retr is called
							else{
								System.out.println("503 Bad sequence of commands.\n");
								outToClient.writeBytes("503 Bad sequence of commands.\n");
								outToClient.flush();
								continue;
							}
						}
					}
					else {
						System.out.println("501 Syntax error in parameter.\n");
						outToClient.writeBytes("501 Syntax error in parameter.\n");
						outToClient.flush();
						continue;
					}
				}
				
				// =================== check port command ===================
				else if(token.equalsIgnoreCase("port")){
					while(st.hasMoreTokens()){
						para = para + (String) st.nextToken();
					}
					if (para == ""){
						System.out.println("501 Syntax error in parameter.\n");
						outToClient.writeBytes("501 Syntax error in parameter.\n");
						outToClient.flush();
						continue;
					}
					else{
						if(hostportCheck(para)){
							System.out.println("200 Port command successful ("+ outputHP + ").\n");
							outToClient.writeBytes("200 Port command successful ("+ outputHP + ").\n");
							outToClient.flush();
							continue;
						}
						else{
							System.out.println("501 Syntax error in parameter.\n");
							outToClient.writeBytes("501 Syntax error in parameter.\n");
							outToClient.flush();
							continue;
						}
					}
				}
				
				// =================== check type command ===================
				else if(token.equalsIgnoreCase("type")){
					if(st.hasMoreTokens()){
						token =  (String) st.nextToken();
						
						if (token.startsWith("A")){
							System.out.println("200 Type set to A.\n");
							outToClient.writeBytes("200 Type set to A.\n");
							outToClient.flush();
							continue;
						}
						else if (token.startsWith("I")){
							System.out.println("200 Type set to I.\n");
							outToClient.writeBytes("200 Type set to I.\n");
							outToClient.flush();
							continue;
						}
						else {
							System.out.println("501 Syntax error in parameter.\n");
							outToClient.writeBytes("501 Syntax error in parameter.\n");
							outToClient.flush();
							continue;
						}
					}
					else {
						System.out.println("500 Syntax error, command unrecognized.\n");
						outToClient.writeBytes("500 Syntax error, command unrecognized.\n");
						outToClient.flush();
						continue;
					}
				}
				
				// =================== check noop/syst/quit and invalid command ===================
				else{
					if(token.length()>=4){
						para = token.substring(0,4);
					}
					else{
						System.out.println("500 Syntax error, command unrecognized.\n");
						outToClient.writeBytes("500 Syntax error, command unrecognized.\n");
						outToClient.flush();
						continue;
					}
					if(para.equalsIgnoreCase("noop")){ 
						int lastC = token.charAt(token.length()-1);
						int lastButOneC = token.charAt(token.length()-2);
						int fifthChar=0;
						if (token.length()>4){
							fifthChar = token.charAt(4);
						}
//						if (fifthChar!=13 && fifthChar!=10 && fifthChar!=0){
//							System.out.println("500 Syntax error, command unrecognized.\n");
//							outToClient.writeBytes("500 Syntax error, command unrecognized.\n");
//							continue;
//						}
		
						System.out.println("200 Command OK.\n");
						outToClient.writeBytes("200 Command OK.\n");
						outToClient.flush();
						continue;
					
//						else{
//							System.out.println("501 Syntax error in parameter.\n");
//							outToClient.writeBytes("501 Syntax error in parameter.\n");
//							continue;
//						}
					}//noop
					else if(para.equalsIgnoreCase("syst")){
						int lastC = token.charAt(token.length()-1);
						int lastButOneC = token.charAt(token.length()-2);
						int fifthChar=0;
						if (token.length()>4){
							fifthChar = token.charAt(4);
						}
//						if (fifthChar!=13 && fifthChar!=10 && fifthChar!=0){
//							System.out.println("500 Syntax error, command unrecognized.\n");
//							outToClient.writeBytes("500 Syntax error, command unrecognized.\n");
//							continue;
//						}
						
						System.out.println("215 UNIX Type: L8.\n");
						outToClient.writeBytes("215 UNIX Type: L8.\n");
						outToClient.flush();
						continue;
						
//						else{
//							System.out.println("501 Syntax error in parameter.\n");
//							outToClient.writeBytes("501 Syntax error in parameter.\n");
//							continue;
//						}
					}
					else{
						System.out.println("500 Syntax error, command unrecognized.\n");
						outToClient.writeBytes("500 Syntax error, command unrecognized.\n");
						outToClient.flush();
						continue;
					}// invalid command here
				}
			}
		} while(true);	
		
	}
}