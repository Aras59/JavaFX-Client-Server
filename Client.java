
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class Client extends Thread {

    private Socket socket = null;
    private int port = 9999;
    private String path = null;
    private String clientname = null;
    private ArrayList<String> clients = new ArrayList<String>();
    private ObjectOutputStream out = null;
    private ObjectInputStream in = null;
    private String user = "";
    private String file = "";
    private String status="";
    // private ArrayList<Pair<String,String>> essa =null;

    public Client(Socket sock, int p, String cliname, String pa) {
        socket = sock;
        port = p;
        path = pa;
        clientname = cliname;
        try {
            in = new ObjectInputStream(socket.getInputStream());
            out = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Socket getSocket() {
        return this.socket;
    }

    public ObjectOutputStream getOut() {
        return this.out;
    }

    public ObjectInputStream getIn() {
        return this.in;
    }

    public int getport() {
        return this.port;
    }

    public String getpath() {
        return this.path;
    }

    public String getstatus() {
        return this.status;
    }

    public String getClientname() {
        return this.clientname;
    }

    public ArrayList<String> getUserList() {
        return this.clients;
    }

    public ArrayList<String> filescan(String sciezka) {
        ArrayList<String> temp = new ArrayList<String>();
        File actual = new File(sciezka);
        actual.mkdir();
        if (actual.listFiles() != null) {
            for (File f : actual.listFiles()) {
                temp.add(f.getName());
            }
        }
        return temp;
    }

    public void put(String use, String fil) {
        this.user = use;
        this.file = fil;
    }

    public void del() {
        this.user = "";
        this.file = "";
    }

    public void listRequest() throws IOException, ClassNotFoundException {
        this.status="Pobieram";
        out.writeObject(new Datapacket(2));
        Datapacket paket=(Datapacket)in.readObject();
        if(paket.getCommand()==2){
            ArrayList<String> siema = paket.getUserList();
            clients=siema;
        } 
    }

    

    public void clientConnetct(){
         try {
                out.writeObject(new Datapacket(1,clientname));
                this.status="Połączony";
        } catch (IOException  e) {
            e.printStackTrace();
        }
    }
    //wysyłańsko plików
    //odbierańsko plików
    //lista essaa
    //wysyłańsko do innych klientow

    public void run(){
        try {
            while(true){

               Thread.sleep(1000);
                
               listRequest();

/////////////////////////////////Synchronizacja z serverem od strony clienta////////////////////////////////////////////////////////////////////
                
                out.writeObject(new Datapacket(3));
                out.flush();
                ArrayList<String>lista = filescan(path);
                for(String temp:lista){
                    this.status="Sprawdzam";
                    File f=new File(path+"/"+temp);
                    out.writeObject(new Datapacket(4,clientname,temp,f.length()));
                    out.flush();
                    Datapacket pake=(Datapacket)in.readObject();
                    if(pake.getCommand()==14) continue;
                    if(pake.getCommand()==15){
                        this.status="Wysyłam";
                        DataOutputStream outdata = new DataOutputStream(socket.getOutputStream());
                        FileInputStream findata = new FileInputStream(f);
                        int count;
                        byte[] buffer = new byte[8192]; // or 4096, or more
                        while ((count = findata.read(buffer)) > 0)
                        {
                            outdata.write(buffer, 0, count);
                        }
                        findata.close();
                    }        
                }
                out.writeObject(new Datapacket(5));
                out.flush();
//////////////////////////////////Synchronizacja z Serverem od strony Servera//////////////////////////////////////////////////////////////////                
                Datapacket packet;
                while((packet=(Datapacket)in.readObject()).getCommand()!=7){
                        this.status="Sprawdzam";
                        if(packet.getCommand()==7) break;
                        if(packet.getCommand()==4) {
                        ArrayList<String>list = filescan(path);  
                        if(list.contains(packet.getFileName())){
                            this.status="Wysyłam";
                            out.writeObject(new Datapacket(5));
                            out.flush();
                        }else{
                            this.status="Wysyłam";
                            out.writeObject(new Datapacket(6));
                            out.flush();
                            DataInputStream indata = new DataInputStream(socket.getInputStream());   
                            FileOutputStream filout= new FileOutputStream(new File(path+"/"+packet.getFileName())); 
                            byte[] buffer = new byte[8192]; // or 4096, or more
                            int count = 0;
                            long filesize=packet.getFileSize();
                            while(filesize>0&& (count=indata.read(buffer,0,(int)packet.getFileSize()))>0){
                                filout.write(buffer,0,count); 
                                filesize-=count;
                            }
                            filout.close();
                        }
                    } 
                }
                System.out.println(clientname+" "+"Synchronizacja zakonczona");
                this.status="Sprawdzam";
////////////////////////////////////////////////////////////////////////////////////////////////////
               if(file!=""&&user!=""){
                    
                    File f= new File(path+"/"+file);
                    out.writeObject(new Datapacket(8,user,file,f.length()));
                    Datapacket pake=(Datapacket)in.readObject();
                    if(pake.getCommand()==16) continue;
                    if(pake.getCommand()==17){
                        this.status="Wysyłam";
                        DataOutputStream outdata = new DataOutputStream(socket.getOutputStream());
                        FileInputStream findata = new FileInputStream(f);
                        int count;
                        byte[] buffer = new byte[8192]; // or 4096, or more
                        while ((count = findata.read(buffer)) > 0)
                        {
                            outdata.write(buffer, 0, count);
                        }
                        findata.close();   
                    }
                    Datapacket pakiet=(Datapacket)in.readObject();
                    this.status="Sprawdzam";
                    if(pakiet.getCommand()==9){
                        System.out.println("Plik "+" "+file+" "+ "wysłany do: "+user);
                    }
                    del();
                }              
            }
        } catch(IOException | ClassNotFoundException | InterruptedException e){
        e.printStackTrace();
        }
    
    }
}