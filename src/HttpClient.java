import java.util.*;
import java.io.*;
import java.net.*;

public class HttpClient {
	protected URL url;
	protected HttpURLConnection server;
	/**
	* @param szUrl: String object for the URL
	*/
	public HttpClient(String szUrl) throws Exception {
		try {
			url=new URL(szUrl);
		}
		catch(Exception e) {
			throw new Exception("Adresse invalide.");
		}
	}
	
	/**
	* @param method: String object for client method (POST, GET,...)
	*/
	public void connect(String method) throws Exception {
		try {
			server=(HttpURLConnection)url.openConnection();
			server.setDoInput(true);
			server.setDoOutput(true);
			server.setRequestMethod(method);
			server.setRequestProperty("Content-type","application/x-www-form-urlencoded");
			server.connect();
		}
		catch(Exception e) {
			throw new Exception("Erreur de connexion.");
		}
	}
	public void disconnect() {
		server.disconnect();
	}
	public String displayResponse(boolean affiche) throws Exception {
		String line,total="";
		try {
			BufferedReader s=new BufferedReader(
			new InputStreamReader(
			server.getInputStream()));
			line=s.readLine();
			while (line != null) {
				if (affiche) System.out.println(line);
				total=total + line;
				line=s.readLine();
			}
			s.close();
			return total;
		}
		catch(Exception e) {
			throw new Exception("Erreur de lecture de flux entrant.");
		}
	}
	public void post(String s) throws Exception {
		try {
			BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(server.getOutputStream()));
			bw.write(s,0,s.length());
			bw.flush();
			bw.close();
		}
		catch(Exception e) {
			throw new Exception("Erreur d'écriture du flux sortant.");
		}
	}
}