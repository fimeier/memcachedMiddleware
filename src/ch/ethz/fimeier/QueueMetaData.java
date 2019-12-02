package ch.ethz.fimeier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Contains all informations (sockets, streams) about a memtier_benchmark instance OR a memcached server
 * There exist one instance per client.
 * In addition each worker thread holds an instance of this class for each memcached server
 * Contains important methods to read/write from/to clients and memcached servers
 * @author fimeier
 *
 */
public class QueueMetaData {
	public Socket clientSocket;

	private PrintWriter outPrintWriter;
	private BufferedReader inBufferedReader;
	
	
	public int prefferedMemcachedServer = 0;



	/*
	 * Statistics per client
	 */
	
	public void clientThinkTime() {
		//implies 1 request
		if (tClientResponseSent==0)
			return;
		
		// tClientAddedToQueue is the "current" time
		// tClientResponseSent is the time the last responmse has been sent
		tClientThinkTimeCum += (tClientAddedToQueue - tClientResponseSent);
		tClientRequestsCum++;
	}

	/**
	 * use this to define the window the request will be processed in
	 */
	public int windowNumber = 0;
	public boolean getStatistics = false;
	public long tClientAddedToQueue = 0; //now in nano seconds //wird durch main.run() gesetzt und durch MyMiddleware.startPerThreadStatistics ausgelesen wenn cmd process beginnt
	public long tClientResponseSent = 0; //wird durch workerthread gesetzt nachdem Response an Client gesendet wurde
	public long tClientThinkTimeCum = 0;
	public long tClientRequestsCum = 0;

	
	public long nBytesSent = 0; //mefi84 could be used for data throughput

	public QueueMetaData(Socket _clientSocket) {
		try {
			clientSocket = _clientSocket;
			outPrintWriter = new PrintWriter(clientSocket.getOutputStream(), false);
			inBufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()), 5000);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void closeClientSocket() {
		MyMiddleware.logger.severe("closing connection to client"+clientSocket.getInetAddress()+":"+clientSocket.getPort());
		System.out.println("closing connection to client"+clientSocket.getInetAddress()+":"+clientSocket.getPort());
		try {
			clientSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	/**
	 * Tells whether this stream is ready to be read.
	 * Needed for non-blocking behavior
	 * (Wrapper Function: I experimented with different streams)
	 * @return
	 */
	public boolean readyLikeFunction() {
		try {
			return inBufferedReader.ready();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * reads in the data until the next newline and appends it to the given String Argument
	 * Remark: Used to read in commands, they are always terminated by a new line
	 * Emulates perfectly the needed behavior (worker thread must wait for a complete command)
	 * (Wrapper Function: I experimented with different streams)
	 * 
	 * @param in appends the read line to the in string
	 */
	public void readLineLikeFunction(StringBuilder in) {
		try {
			in.append(inBufferedReader.readLine());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			in.append(""); //?
		}
	}


	/**
	 * prints the given ArgumentString to the output
	 * (Wrapper Function: I experimented with different streams)
	 * @param outputLine
	 * @param flushIt
	 */
	public void printLikeFunction(StringBuilder outputLine, boolean flushIt) {
		outPrintWriter.print(outputLine);
		if (flushIt)
			outPrintWriter.flush();
		return;
	}


	/**
	 * prints the given Argument (as stream) to the output stream and probably flushes the stream
	 * (Wrapper Function: I experimented with different streams)
	 * @param activeClient
	 * @param flushIt
	 */
	/*public void printReadLineFromStreamFunction(QueueMetaData activeClient, boolean flushIt) {
		try {
			outPrintWriter.print(activeClient.inBufferedReader.readLine());
			outPrintWriter.print("\r\n");
			if (flushIt)
				outPrintWriter.flush();
			return;			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}*/

	/**
	 * Used to directly read memcached responses (data part) and write output to the client stream
	 * 
	 * @param activeClient
	 * @param nBytes
	 * @param flushIt
	 */
	public void printReadNBytesFromStreamFunction(QueueMetaData activeClient, int nBytes, boolean flushIt) {
		try {
			char[] cbuf = new char[nBytes+2];
			int nread = activeClient.inBufferedReader.read(cbuf, 0, nBytes+2);
			while (nread < nBytes+2) {
				nread += activeClient.inBufferedReader.read(cbuf, nread, nBytes+2-nread);
			}
			outPrintWriter.print(cbuf);

			if (flushIt)
				outPrintWriter.flush();
			return;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Used to forward the data part (set cmd) directly to all memcached servers
	 * 
	 * @param command the parsed command
	 * @param memCachedServers
	 * @param nBytes
	 * @param flushIt
	 * @param threadStatistic Instrumentation startAvgMemServiceTimeSet
	 */
	public void sendSetToAllMemchachedServers(StringBuilder command, QueueMetaData memCachedServers[], int nBytes, boolean flushIt, MyMiddlewareStatistics threadStatistic) {
		try {

			char[] cbuf = new char[nBytes+2];
			int nread = inBufferedReader.read(cbuf, 0, nBytes+2);
			while (nread < nBytes+2) {
				nread += inBufferedReader.read(cbuf, nread, nBytes+2-nread);
			}

			/*
			 * Instrumentation startAvgMemServiceTimeSet
			 */
			if(getStatistics) {
				threadStatistic.startAvgMemServiceTimeSet();
			}


			//send to all memcached Servers
			for (QueueMetaData memcachedServer: memCachedServers) {
				memcachedServer.outPrintWriter.print(command);
				memcachedServer.outPrintWriter.print(cbuf);

				if (flushIt)
					memcachedServer.outPrintWriter.flush();

			}
			return;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	/**
	 * helper function
	 */
	void printStats() {
		System.out.println(""
				+ "activeClient.in.ready()="+readyLikeFunction()
				//+ "activeClient.clientIn.available()="+clientIn.available()
				+ " clientSocket.isBound()="+clientSocket.isBound()
				+ " clientSocket.isClosed()="+clientSocket.isClosed()
				+ " clientSocket.isConnected()="+clientSocket.isConnected()
				+ " clientSocket.isInputShutdown()="+clientSocket.isInputShutdown()
				);
	}

	/**
	 * helper function
	 */
	String printNetworkInfos() {
		return new String("Client="
				+ clientSocket.getInetAddress()
				+ ":"
				+clientSocket.getPort()
				);
	}




}
