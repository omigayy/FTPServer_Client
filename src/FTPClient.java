// programmer:Yingying Wu
// program description: The user interface and FTP command generation.
// program status: all code tested and work fine

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class FTPClient {
	
	static int item;//used to read char from file
	static int tmpFirstChar = -1;//indicator of whether need to add a char back to the beginning of a command
	static ArrayList<Character> command = new ArrayList<Character>();//store command
	static ArrayList<String> cmdList = new ArrayList<String>();// a list of command name
	
	private static void initCmd(){
	// initial command list
		cmdList.add("CONNECT");
		cmdList.add("GET");
		cmdList.add("QUIT\n");
	}
	
	private static String delEol(String str){
	// delete EOL at the end of a str
	// input: a str: xxxxxxx\r\n | xxxxxx\n |xxxxx
	// output: a str w/o EOL : xxxxx
		if (str.length()>=2){
			int tmp = str.charAt(str.length()-2);
			if (tmp == 13 || tmp ==10){
				return str.substring(0,str.length()-2);
			}
			else{
				tmp = str.charAt(str.length()-1);
				if (tmp == 13 || tmp ==10){
					return str.substring(0,str.length()-1);
				}
				else
					return str;
			}
		}
		else if (str.length()==1){
			int tmp = str.charAt(str.length()-1);
			if (tmp == 13 || tmp ==10){
				return str.substring(0,str.length()-1);
			}
			else
				return str;
		}
		else
			return str;
	}
	
	private static boolean validElt(String elt){
	// determine whether an elt is valid
		elt.toLowerCase();
		int code;
		if (elt.length()<2){
			return false;
		}
		code = elt.charAt(0);
		if (code > 122 || code < 97){
			return false;
		}
		for (int i = 1;i<elt.length();i++){
			code = elt.charAt(i);
			if (code<48 || code >122){
				return false;
			}
			else if(code >57 && code <97){
				return false;
			}
		}
		return true;
	}
	
	private static boolean validServerHost(String sh){
	//determine whether a server-host is valid
		StringTokenizer elts = new StringTokenizer(sh,".");
		String elt;
		if (!elts.hasMoreTokens()){
			return false;
		}
		while(elts.hasMoreTokens()){
			elt = (String) elts.nextToken();
			if (!validElt(elt)){
				return false;
			}
		}
		return true;
	}
	
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

	private static boolean validPathname(String pathname){
		String tmp = pathname;
		if (!isAllASCII(tmp))
			return false;
		tmp = delEol(tmp);
		if (tmp.isEmpty())
			return false;
		return true;
	}
	
	private static boolean validServerPort(String sp){
	//determine whether a server-post is valid
		for (int i=0;i<sp.length();i++){
			int tmp = sp.charAt(i);
			if (tmp <48 || tmp >57)
				return false;
		}
		int value = Integer.parseInt(sp);
		if (value>=0 && value <=65535){
			return true;
		}
		else{
			return false;
		}
	}
	
	private static String generateHostPort(int portN) throws UnknownHostException{
	// calculate proper host-post and concaternate them
		String myIP;
		InetAddress myInet;
		myInet = InetAddress.getLocalHost();
		myIP = myInet.getHostAddress();
		myIP = myIP.replace('.', ',');
		int port1,port2;
		port2 = portN % 256;
		port1 = (portN - port2)/256;
		String port = Integer.toString(port1) + ","+Integer.toString(port2);
		String hp = myIP + "," + port;
		return hp;
	}
	
	private static boolean validReplyCode(String replyC){
	// determine whether a reply code is valid
		for (int i=0;i<replyC.length();i++){
			int tmp = replyC.charAt(i);
			if (tmp<48 || tmp>57){
				return false;
			}
		}
		int code = Integer.parseInt(replyC);
		if (code>=100 && code <=599){
			return true;
		}
		else{
			return false;
		}
	}
	
	private static boolean validReplyText(String replyT){
	//determine whether a reply-text is valid
		boolean valid = true;
	    for (int i = 0; i < replyT.length(); i++) {
	        int c = replyT.charAt(i);
	        if (c > 127) {
	            valid = false;
	            break;
	        }
	        if (c == 10 || c==13){
	        	valid = false;
	        	break;
	        }
	    }
	    return valid;
	}

	private static boolean containsOnlyCRLF(String replyText){
		boolean result = true;
		int tmp;
		for (int i =0;i<replyText.length();i++){
			tmp = replyText.charAt(i);
			if (tmp !=13 && tmp !=10)
				result = false;
		}
		return result;
	}
	
	private static boolean indicateError(int code){
		if (code>=400 && code <= 599){
			return true;
		}
		return false;
	}
	
	private static boolean processReply(String serverReply){
		// input: reply from the server
		// output: false if unexpected reply or reply indicating ERROR condition
		// deal with it and print to std O
		StringTokenizer st = new StringTokenizer(serverReply," ");
		String replyCode = null;
		String replyText = "";
		
		if(st.hasMoreTokens()){
			replyCode = (String) st.nextToken();
			replyCode.toUpperCase();
		}
		else{
			System.out.println("ERROR -- reply-code");
			return false;
		}
		if (!validReplyCode(replyCode)){
			System.out.println("ERROR -- reply-code");
			return false;
		}
		
		if (!st.hasMoreTokens()){
			System.out.println("ERROR -- reply-text");
			return false;
		}
		while(st.hasMoreTokens()){
			replyText += (String) st.nextToken() + " ";
			replyText.toUpperCase();
		}
		replyText = replyText.substring(0,replyText.length()-1);

		if (containsOnlyCRLF(replyText)){
			System.out.println("ERROR -- reply-text");
			return false;
		}
//
//		if (replyText.length()>=2){
//			int last = replyText.charAt(replyText.length()-1);
//			int lastBO = replyText.charAt(replyText.length()-2);
//			if (last!=10 || lastBO!=13){
//				System.out.println("ERROR -- <CRLF>");
//				return false;
//			}
//		}
//		else{
//			System.out.println("ERROR -- <CRLF>");
//			return false;
//		}

		String text = replyText.substring(0,replyText.length()-2);
		if (!validReplyText(text)){
			System.out.println("ERROR -- reply-text");
			return false;
		}
		
		replyText = delEol(replyText);
		
		int code = Integer.parseInt(replyCode);
		if(indicateError(code)){
			System.out.print("FTP reply " + replyCode+ " accepted. Text is: " + replyText + "\n");
			return false;
		}
		
		System.out.print("FTP reply " + replyCode+ " accepted. Text is: " + replyText + "\n");
		return true;
	}
	
	public static void main(String[] args) throws IOException {
		int portNum;
	    try {
	    	portNum = Integer.parseInt(args[0]);
	    } catch (IndexOutOfBoundsException e) {
	        System.err.println("Please input a valid host for the connection or hardcode the host");
	        throw e;
	    } 
		
	//=========== variable declaration =============
		initCmd();
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in)); // read file
		StringBuilder commandStrB = new StringBuilder();//store command
		String commandStr = null;//store command
		String token = null;//store cmd tokens
		String serverHost=null;
		String serverPort="";
		String pathname = null;
		boolean connected = false;//indicator of whether connected
		String hostPort;
		String serverReply = null;
		int retrTime = 1; // for use in file name
		
		Socket clientSocket = null;
		DataOutputStream outToServer=null;//  Create output stream attached to socket
		BufferedReader inFromServer=null;//  Create (buffered) input stream attached to socket
		BufferedReader dataFromServer = null;
		
		do{
			
			//=============== read in command ================
			command.clear();
			commandStrB.delete(0, commandStrB.length());
			commandStr = null;
			serverPort="";
			if (tmpFirstChar != -1){
				command.add((char)tmpFirstChar);
				commandStrB.append((char)tmpFirstChar);
				tmpFirstChar = -1;
			}
			item = reader.read();
			while(item!=13 &&item != 10){
				command.add((char)item);
				commandStrB.append((char)item);
				item = reader.read();
			};
			
			commandStr = commandStrB.toString();
			
			System.out.println(commandStr);
			
			if (item == 13){
				command.add((char)13);
				commandStrB.append((char)13);
				item = reader.read();
				if (item==10){
					command.add((char)item);
					commandStrB.append((char)item);
				}
				else{
					tmpFirstChar = item;
				}
			}
			else if (item == 10){
				command.add((char)10);
				commandStrB.append((char)10);
			}
			
			commandStr = commandStrB.toString();

			// ===============================
			
			// parse command str w/ space
			StringTokenizer st = new StringTokenizer(commandStr," ");
			
			
			if(st.hasMoreTokens()){
				token = (String) st.nextToken();
				token.toUpperCase();
			}
			else{
				System.out.println("ERROR -- request");
				continue;
			}
			
			if (!cmdList.contains(token)){
				System.out.println("ERROR -- request");
				continue;
			}// valid request
			
			//process connect request
			if (token.equalsIgnoreCase(cmdList.get(0))){
				if(st.hasMoreTokens()){
					serverHost = (String) st.nextToken();
				}
				else{
					System.out.println("ERROR -- server-host");
					continue;
				}

				if (!validServerHost(serverHost)){
					System.out.println("ERROR -- server-host");
					continue;
				}
				while(st.hasMoreTokens()){
					serverPort += (String) st.nextToken();
				}
				
				if(serverPort == ""){
					System.out.println("ERROR -- server-port");
					continue;
				}
				serverPort = delEol(serverPort);
				if (!validServerPort(serverPort)){
					System.out.println("ERROR -- server-port");
					continue;
				}
				
				// CONNECT valid, execute
				
				int sp = Integer.parseInt(serverPort);
				
				//  Create client socket with connection to server
				if (clientSocket == null){
					try{
						clientSocket = new Socket(serverHost,sp);
					}catch(Exception ex){
						System.out.println("CONNECT failed");
						continue;
					}
					if(clientSocket.isConnected()){
						System.out.print("CONNECT accepted for FTP server at host "+ serverHost +" and port "
								+ serverPort +"\n"); // not btwn server & client \n
						connected = true;
					}
					else{
						System.out.println("CONNECT failed");
						continue;
					}
					outToServer = new DataOutputStream(clientSocket.getOutputStream());
					inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
					serverReply = inFromServer.readLine();
					if(!processReply(serverReply)){
						continue;
					}
				}
				else{
					clientSocket.close();
					connected = false;
					try{
						clientSocket = new Socket(serverHost,sp);
					}catch(Exception ex){
						System.out.println("CONNECT failed");
						continue;
					}
					if(clientSocket.isConnected()){
						System.out.print("CONNECT accepted for FTP server at host "+ serverHost +" and port "
								+ serverPort +"\n"); // not btwn server & client \n
						connected = true;
					}
					else{
						System.out.println("CONNECT failed");
						continue;
					}
					outToServer = new DataOutputStream(clientSocket.getOutputStream());
					inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
					serverReply = inFromServer.readLine();
					if(!processReply(serverReply)){
						continue;
					}
				}
				
				System.out.print("USER anonymous\r\n");
				outToServer.writeBytes("USER anonymous\r\n");
				serverReply = inFromServer.readLine();
				if(!processReply(serverReply)){
					continue;
				}
				
				System.out.print("PASS guest@\r\n");
				outToServer.writeBytes("PASS guest@\r\n");
				serverReply = inFromServer.readLine();
				if(!processReply(serverReply)){
					continue;
				}
				
				System.out.print("SYST\r\n");
				outToServer.writeBytes("SYST\r\n");
				serverReply = inFromServer.readLine();
				if(!processReply(serverReply)){
					continue;
				}
				
				System.out.print("TYPE I\r\n");
				outToServer.writeBytes("TYPE I\r\n");
				serverReply = inFromServer.readLine();
				if(!processReply(serverReply)){
					continue;
				}
				
			}
			
			//process GET request
			else if(token.equalsIgnoreCase(cmdList.get(1))){
				if (!connected){
					System.out.println("ERROR -- expecting CONNECT");
					continue;
				}
				if(!st.hasMoreTokens()){
					System.out.println("ERROR -- pathname");
					continue;
				}
				
				token = (String)st.nextToken();
				int index = commandStr.indexOf(token.charAt(0));
				pathname = commandStr.substring(index, commandStr.length());
				
				if(!validPathname(pathname)){
					System.out.println("ERROR -- pathname");
					continue;
				}
				
				
				// ====== valid, execute ======
				hostPort = generateHostPort(portNum);
				pathname = delEol(pathname);
				
				ServerSocket welcomeSocket = null;
						
				try{
					welcomeSocket = new ServerSocket(portNum);
					System.out.print("GET accepted for "+pathname+"\n");
				}catch (IOException e) {
			        System.out.println("GET failed, FTP-data port not allocated.");
			        continue;
			    }
				
				System.out.print("PORT "+hostPort+"\r\n");
				outToServer.writeBytes("PORT "+hostPort+"\r\n");
				portNum++;
				serverReply = inFromServer.readLine();
				if(!processReply(serverReply)){
					welcomeSocket.close();
					continue;
				}
				
				System.out.print("RETR " + pathname+ "\r\n");
				outToServer.writeBytes("RETR " + pathname+ "\r\n");
				serverReply = inFromServer.readLine();
				if(!processReply(serverReply)){
					welcomeSocket.close();
					continue;
				}
				Socket connectionSocket;
				try{
					connectionSocket = welcomeSocket.accept();
				}catch(Exception ex){
					continue;
				}
				dataFromServer = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
				
				FileOutputStream fop = null;
//				String fopName = "retr_files/file" + retrTime;
				String fopName = pathname;
				File fileOut = new File(fopName);
				try {
					fop = new FileOutputStream(fileOut);
					if (!fileOut.exists()){
						fileOut.createNewFile();
					}
					int content;
					while((content = dataFromServer.read())!=-1){
						fop.write((char)content);
					}
					fop.close();
					retrTime++;
					connectionSocket.close();
					welcomeSocket.close();
					serverReply = inFromServer.readLine();
					if(!processReply(serverReply)){
						continue;
					}
					continue;
				}catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			// process QUIT command
			else if(token.equalsIgnoreCase(cmdList.get(2))){
				if (!connected){
					System.out.println("ERROR -- expecting CONNECT");
					continue;
				}
				// valid, execute
				System.out.print("QUIT accepted, terminating FTP client\n");
				System.out.print("QUIT\r\n");
				if(connected == true){
					outToServer.writeBytes("QUIT\r\n");
				}
				serverReply = inFromServer.readLine();
				if(!processReply(serverReply)){
					continue;
				}
				clientSocket.close();
				connected = false;
				System.exit(0);
			}
			
		} while (true);
	}
}
