
import java.net.*;
import java.io.*;
import java.util.*;

public class Server {
    private ServerSocket server = null;
    private String serverpath = "/home/micha/Pulpit/Projekt/Projekt/src/SerwerFolder";
    private ArrayList<String> clients = new ArrayList<String>();
    private ArrayList<ServerClient> serverclients = new ArrayList<ServerClient>();


    public Server(ServerSocket socket) {
        server = socket;
    }

    public String getServerPath() {
        return this.serverpath;
    }

    public ArrayList<String> getClienList() {
        return this.clients;
    }

    public ArrayList<ServerClient> getServerClientList() {
        return this.serverclients;
    }

    public void addNewClient(ServerClient client){
        serverclients.add(client);
    }


    public static void serverFolderScan(File file) {
        for (File f : file.listFiles()) {
            System.out.println(f.getName());
        }
    }

    public Socket accept() throws IOException{
        return server.accept();
    }

}