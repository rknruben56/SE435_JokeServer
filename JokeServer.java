/*--------------------------------------------------------
1. Ruben Rodriguez / Date: 4/19/2015

2. Java build 1.8.0_40

3.To compile:

> javac JokeServer.java

4. Precise examples / instructions to run this program:

In shell window, navigate to where the compiled file is and enter:

> java JokeServer

All acceptable commands are displayed on the various consoles.

5. List of files needed for running the server and client.

 a. JokeServer.java
 b. JokeClient.java

6. Notes: This code is mostly taken from InetServer with all the joke logic implemented. JokeClientAdmin is used as well to interact with the JokeServer.

----------------------------------------------------------*/

import java.io.*; // Get the Input/Output libraries
import java.net.*; // Get the Java networking libraries
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

/**
 * Worker thread that does the specialized work per connection
 * 
 * @author rodri_000
 *
 */
class Worker extends Thread {
	Socket _sock;
	boolean _jokeMode;
	boolean _serverMode;
	private static final int JOKE_STACK_INDEX = 0;
	private static final int PROVERB_STACK_INDEX = 1;

	// Constructor
	Worker(Socket s, boolean jokeMode, boolean serverMode) {
		_sock = s;
		_jokeMode = jokeMode;
		_serverMode = serverMode;
	}

	/**
	 * Run method inherited by the Thread class
	 */
	public void run() {
		// Get I/O streams in/out from the socket:
		PrintStream out = null;
		BufferedReader in = null;

		// try/catch block that does the exception handling
		try {
			// Establishes the input and output
			in = new BufferedReader(new InputStreamReader(
					_sock.getInputStream()));
			out = new PrintStream(_sock.getOutputStream());
			try {

				// If server is in maintenance mode
				if (_serverMode == false) {
					out.println("The server is temporarily unavailable -- check-back shortly.");
				}
				// else return proper joke or proverb
				else {
					String name = in.readLine();
					System.out.println("Sending joke/proverb to " + name);
					updateClientState(name, out);
				}
			} catch (IOException x) {
				System.out.println("Server read error");
				x.printStackTrace();
			}
			// Closes this single connection while the server is still open
			_sock.close();
		} catch (IOException ioe) {
			System.out.println(ioe);
		}
	}

	/**
	 * Updates the client state. If its a new client, it adds it to the
	 * JokeServer HashMap. The key is the client name with a unique identifier
	 * and the value is an ArrayList of stacks. Each client has 2 stacks: 1 for
	 * jokes and 1 for proverbs so that the conversation is maintained between
	 * modes. If the client goes through all 5 jokes/proverbs, a new stack is
	 * populated with the joke/proverb list randomized.
	 * 
	 * @param name
	 * @param out
	 */
	private void updateClientState(String name, PrintStream out) {
		// If the HashMap doesn't contain the client, add it to the server
		if (!JokeServer._clientState.containsKey(name)) {
			// Create stacks
			Stack<String> jokeStack = new Stack<String>();
			Stack<String> proverbStack = new Stack<String>();

			// Populate stacks with random jokes/proverbs
			populateStack(jokeStack, JokeServer._jokeList, name);
			populateStack(proverbStack, JokeServer._proverbList, name);

			// Add stacks to their key in the HashMap
			ArrayList<Stack<String>> clientStacks = new ArrayList<Stack<String>>();
			clientStacks.add(jokeStack);
			clientStacks.add(proverbStack);
			JokeServer._clientState.put(name, clientStacks);

			// else its a returning client
		} else {

			// get the client's joke and proverb stacks
			ArrayList<Stack<String>> userStack = JokeServer._clientState
					.get(name);

			// If joke mode, get the joke stack
			if (_jokeMode) {
				Stack<String> jokeStack = userStack.get(JOKE_STACK_INDEX);

				// if all the jokes have been seen, regenerate stack with random
				// jokes
				if (jokeStack.isEmpty()) {
					populateStack(jokeStack, JokeServer._jokeList, name);
				}

				// get random joke from stack
				String joke = jokeStack.pop();
				out.println(joke);

				// else get the proverb stack
			} else {
				Stack<String> proverbStack = userStack.get(PROVERB_STACK_INDEX);

				// If the stack is empty, populate with random proverbs
				if (proverbStack.isEmpty()) {
					populateStack(proverbStack, JokeServer._proverbList, name);
				}

				// get random proverb from stack
				String proverb = proverbStack.pop();
				out.println(proverb);
			}
		}
	}

	/**
	 * Populates the specified stack by randomizing the joke/proverb lists
	 * 
	 * @param jokeStack
	 * @param jokeList
	 * @param name
	 */
	private static void populateStack(Stack<String> jokeStack,
			List<String> jokeList, String name) {
		List<String> randomJokeList = subName(jokeList, name);
		Collections.shuffle(randomJokeList);
		for (int i = 0; i < randomJokeList.size(); i++) {
			jokeStack.push(randomJokeList.get(i));
		}

	}

	/**
	 * Substitutes the name into the joke or proverb
	 * 
	 * @param jokeList
	 * @param name
	 * @return
	 */
	private static List<String> subName(List<String> jokeList, String name) {
		String clientName = name.substring(0, name.indexOf(":"));
		Iterator<String> itr = jokeList.iterator();
		List<String> listWithName = new ArrayList<String>();
		while (itr.hasNext()) {
			String jokeWithName = itr.next().replace("XName", clientName);
			listWithName.add(jokeWithName);
		}
		return listWithName;
	}
}

/**
 * Server class that handles the server socket and spawns worker threads for
 * each client connection
 * 
 * @author rodri_000
 *
 */
public class JokeServer {

	// proverb list
	public static List<String> _proverbList = Arrays.asList(
			"A: A friend in need is a friend indeed XName",
			"B: Don't count your chickens before they are hatched XName",
			"C: Laughter is the best medicine XName",
			"D: XName, patience is a virtue",
			"E: The early bird catches the worm XName.");

	// joke list
	public static List<String> _jokeList = Arrays
			.asList("A: Hey XName, what did the mommy bullet say to the daddy bullet? We're gonna have a BB!",
					"B: How do you make a tissue dance XName? You put a little boogie in it.",
					"C: Why can't you hear a pterodactyl in the bathroom XName? Because it has a silent pee.",
					"D: Hey XName, what did the little fish say when he swam into a wall? DAM!",
					"E: Hey XName, why did the policeman smell bad? He was on duty.");

	// HashMap that contains the clients and their joke/proverb state
	public static HashMap<String, ArrayList<Stack<String>>> _clientState = new HashMap<String, ArrayList<Stack<String>>>();

	public static void main(String a[]) throws IOException {
		int qLen = 6; // Number of requests for the operating system to queue
		int port = 4569; // Port to connect to

		// Create new server socket
		Socket sock;
		ServerSocket servsock = new ServerSocket(port, qLen);

		System.out
				.println("Ruben Rodriguez's Joke Server 1.8 starting up, listening at port 4569.\n");

		// Start admin looper to get state
		AdminLooper al = new AdminLooper();
		Thread t = new Thread(al);
		t.start();

		while (true) {
			sock = servsock.accept();// wait for the client connection
			// Spawn new worker thread that handles the work
			new Worker(sock, al.getJokeMode(), al.getServerMode()).start();
		}
	}
}

/**
 * Looper thread that listens for joke client admin connections and changes the
 * server modes
 * 
 * @author rodri_000
 *
 */
class AdminLooper implements Runnable {
	public static boolean adminControlSwitch = true;
	public static boolean _jokeMode = true;
	public static boolean _serverMode = true;

	/**
	 * Returns whether joke mode is true or false
	 * 
	 * @return
	 */
	public boolean getJokeMode() {
		return _jokeMode;
	}

	/**
	 * Returns whether server is running or in maintenance
	 * 
	 * @return
	 */
	public boolean getServerMode() {
		return _serverMode;
	}

	public void run() {
		System.out.println("Admin looper thread is running");

		int q_len = 6;
		int port = 4570;
		Socket sock;

		try {
			// establish connection with joke admin client if requested
			ServerSocket servsock = new ServerSocket(port, q_len);
			while (adminControlSwitch) {
				sock = servsock.accept();
				new AdminWorker(sock).start();
			}
		} catch (IOException ioe) {
			System.out.println(ioe);
		}
	}
}

/**
 * Worker thread for the Admin server that changes the server modes between
 * joke, proverb, or maintenance
 * 
 * @author rodri_000
 *
 */
class AdminWorker extends Thread {
	Socket _sock;

	AdminWorker(Socket s) {
		_sock = s;
	}

	public void run() {
		PrintStream out = null;
		BufferedReader in = null;

		try {
			// establish I/O
			in = new BufferedReader(new InputStreamReader(
					_sock.getInputStream()));
			out = new PrintStream(_sock.getOutputStream());
			try {
				// get server mode from input and update JokeServer
				String mode = in.readLine();
				updateJokeServerState(mode, out);

			} catch (IOException x) {
				System.out.println("Server read error");
				x.printStackTrace();
			}
			// close connection
			_sock.close();
		} catch (IOException ioe) {
			System.out.println(ioe);
		}
	}

	/**
	 * Updates the JokeServer state depending on the specified command from the
	 * admin client
	 * 
	 * @param mode
	 * @param out
	 */
	private void updateJokeServerState(String mode, PrintStream out) {

		switch (mode.toLowerCase()) {
		case "joke-mode":
			AdminLooper._jokeMode = true;
			AdminLooper._serverMode = true;
			out.println("Changing mode to joke");
			break;
		case "proverb-mode":
			AdminLooper._jokeMode = false;
			AdminLooper._serverMode = true;
			out.println("Changing mode to proverb");
			break;
		case "maintenance-mode":
			AdminLooper._serverMode = false;
			out.println("Changing mode to maintenance");
			break;
		default:
			out.println("Oops! Don't understand the server mode you inputted. Please try again.");
			break;
		}
	}
}
