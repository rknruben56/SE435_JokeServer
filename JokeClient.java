/*--------------------------------------------------------
1. Ruben Rodriguez / Date: 4/19/2015

2. Java build 1.8.0_40

3.To compile:

> javac JokeClient.java

4. instructions to run this program:

In shell window, navigate to where the compiled file is and enter:

> java JokeClient

All acceptable commands are displayed on the various consoles.

This runs across machines, in which case you have to pass the IP address of
the server to the clients. For example, if the server is running at
140.192.1.22 then you would type:

> java JokeClient 140.192.1.22

5. List of files needed for running the server and client.

 a. JokeServer.java
 b. JokeClient.java

6. Notes: This code was mostly taken from InetClient. Please enter your name in the first prompt. Otherwise the jokes will sound weird. If you try to enter any input besides hitting enter after the initialization it will just ignore it and send another joke

----------------------------------------------------------*/

import java.io.*; //Get the IO libraries
import java.net.*; //Get the Java networking libraries
import java.util.Random; //Get the Random library used to make the IDs

/**
 * Client side of the Joke Server that calls the server and receives the joke or
 * proverb
 * 
 * @author rodri_000
 *
 */
public class JokeClient {

	private static final int RANDOM_LIMIT = 100;

	public static void main(String args[]) {
		String _serverName;
		int count = 0;
		Random rand = new Random();
		int clientID = 0;
		String name = "";

		// establishes the server name
		if (args.length < 1)
			_serverName = "localhost";
		else
			_serverName = args[0];

		System.out.println("Ruben Rodriguez's Joke Client, 1.8.\n");
		System.out.println("Using server: " + _serverName + ", Port: 4569");
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		// gets the input and gets the jokes/proverbs
		try {
			String input = "";
			do {
				// Get the Name from the User if it's the initial prompt and
				// establishes an
				// ID
				if (count == 0) {
					System.out
							.println("Enter your name for personalized jokes! or (quit) to end: ");
					System.out.flush();
					name = in.readLine();
					// If user doesn't enter quit for name
					if (name.indexOf("quit") < 0) {
						clientID = rand.nextInt(RANDOM_LIMIT);
						talkToServer(name, _serverName, clientID);
					} else {
						break;
					}
					count = 1;
				}
				// Else get the enter key stroke to get jokes
				System.out
						.println("Press Enter to get a joke or (quit) to end: ");
				System.out.flush();
				input = in.readLine();
				// regular call to get joke
				if (input.indexOf("quit") < 0)
					talkToServer(name, _serverName, clientID);
			}
			// continue client until user enters quit
			while (input.indexOf("quit") < 0);
			System.out.println("Cancelled by user request.");
		} catch (IOException x) {
			x.printStackTrace();
		}
	}

	/**
	 * Private method that establishes the communication with the server. Sends
	 * the established clientID to get the joke/proverb
	 * 
	 * @param name
	 * @param serverName
	 * @param clientID
	 */
	private static void talkToServer(String name, String serverName,
			int clientID) {
		Socket sock;
		BufferedReader fromServer;
		PrintStream toServer;
		String textFromServer;

		try {
			// Open the connection to the Joke server port. Port established is
			// 4569
			sock = new Socket(serverName, 4569);

			// Create filter I/O streams for the socket
			fromServer = new BufferedReader(new InputStreamReader(
					sock.getInputStream()));
			toServer = new PrintStream(sock.getOutputStream());

			// Send client name and ID
			toServer.println(name + ":" + clientID);
			toServer.flush();

			// Read the output response from the server and block while
			// synchronously waiting
			for (int i = 0; i <= 1; i++) {
				textFromServer = fromServer.readLine();
				if (textFromServer != null)
					System.out.println(textFromServer);
			}
			sock.close(); // closes connection
		} catch (IOException x) {
			System.out.println("Socket error.");
			x.printStackTrace();
		}
	}
}
