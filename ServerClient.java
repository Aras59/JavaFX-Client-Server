import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;


public class ServerClient extends Thread{
    private Socket socket;
    private String username;
    private String path;
    private String clientpath;
    private File file=null;
    private ObjectInputStream in = null;
    private ObjectOutputStream out=null;
    private ArrayList<String> clients;
  

    public ServerClient(Socket sock,ArrayList<String> cli,String p){
        socket=sock;
        clients=cli;
        path=p;
         try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    
    public Socket getSocket(){
        return socket;
    }

    public String getUsername(){
        return username;
    }

    public String getPath(){
        return path;
    }


    public ArrayList<String>filescan(String sciezka){
        ArrayList<String> temp = new ArrayList<String>();
        File actual = new File(sciezka);
        if(actual.listFiles()!=null){
            for(File f:actual.listFiles()){
                temp.add(f.getName());
            }
        }
        return temp;
    }

        
    @Override
    public void run() {
        try{  
            while(true){
                
                Datapacket pakt=(Datapacket)in.readObject();
                switch (pakt.getCommand()){
                    case 1:
                            this.clients.add(pakt.getUserName());
                            this.username=pakt.getUserName();
                            this.clientpath=path+"/"+username;
                            file=new File(path);
                            if(file.mkdir()) System.out.println("Create"+pakt.getUserName());
                            else System.out.println("No Crete, Already exist!");
                            break;
                    case 2:
                            ArrayList<String> Siema=new ArrayList<String>(clients);
                            out.writeObject(new Datapacket(2,Siema));
                            break;
                    case 3:
                            Datapacket packet;
                            while((packet=(Datapacket)in.readObject()).getCommand()!=5){
                                if(packet.getCommand()==5) break;
                                String temp=clientpath+"/"+packet.getFileName();
                                ArrayList<String>lista = filescan(clientpath);
                                if(lista.contains(packet.getFileName())){
                                    out.writeObject(new Datapacket(14));   
                                    out.flush();  //14 istnieje plik
                                }else{
                                    out.writeObject(new Datapacket(15));
                                    out.flush();
                                    DataInputStream indata = new DataInputStream(socket.getInputStream());   
                                    FileOutputStream filout= new FileOutputStream(new File(temp)); 
                                    byte[] buffer = new byte[8192]; 
                                    int count = 0;
                                    long filesize=packet.getFileSize();
                                    while(filesize>0&& (count=indata.read(buffer,0,(int)packet.getFileSize()))>0){
                                        filout.write(buffer,0,count); 
                                        filesize-=count;
                                    }
                                    filout.close();          //15 nie istnieje plik
                                }  
                            }
                                 
/////////////////////////////////////////////////////////////////////////////////////////////////////
                            ArrayList<String> list = filescan(clientpath);
                            for(String temporary:list){
                                File f=new File(clientpath+"/"+temporary);
                                out.writeObject(new Datapacket(4,"Server",temporary,f.length()));
                                Datapacket pake=(Datapacket)in.readObject();
                                if(pake.getCommand()==5){
                                    continue;
                                }
                                else if(pake.getCommand()==6){
                                    DataOutputStream outdata = new DataOutputStream(socket.getOutputStream());
                                    FileInputStream findata = new FileInputStream(f);
                                    int counter;
                                    byte[] buff = new byte[8192]; // or 4096, or more
                                    while ((counter = findata.read(buff)) > 0)
                                    {
                                        outdata.write(buff, 0, counter);
                                    }
                                    findata.close();
                                }
                            }
                            out.writeObject(new Datapacket(7));
                            out.flush();
                            break;
                    case 8:
                            String temporaryPath=path+"/"+pakt.getUserName()+"/"+pakt.getFileName();
                            ArrayList<String>array = filescan(path+"/"+pakt.getUserName());
                            if(array.contains(pakt.getFileName())){
                                out.writeObject(new Datapacket(16));   
                                out.flush();
                            }
                            else{
                                out.writeObject(new Datapacket(17));
                                out.flush();
                                DataInputStream indata = new DataInputStream(socket.getInputStream());   
                                FileOutputStream filout= new FileOutputStream(new File(temporaryPath)); 
                                byte[] buffer = new byte[8192]; // or 4096, or more
                                int count = 0;
                                long filesize=pakt.getFileSize();
                                while(filesize>0&& (count=indata.read(buffer,0,(int)pakt.getFileSize()))>0){
                                    filout.write(buffer,0,count); 
                                    filesize-=count;
                                }
                                filout.close();
                                out.writeObject(new Datapacket(9));  
                                out.flush(); 
                            } 
                            
                }   
            }
        }catch(IOException | ClassNotFoundException e) {
            clients.remove(username);
            e.printStackTrace();
        }
        
    }

}