
import java.io.Serializable;
import java.util.ArrayList;

public class Datapacket implements Serializable{

    private static final long serialVersionUID = 1L;
    private int command=0;
    private String username;
    private String filename;
    private long filesize;
    private ArrayList<String> clients;
    private ArrayList<String> filelist;

    public Datapacket(int com){
        this.command=com;
    }

    public Datapacket(int com,String name){
        this.command=com;
        this.username=name;
    }

    public Datapacket(int com,String name,String file,long fs){
        this.command=com;
        this.username=name;
        this.filename=file;
        this.filesize=fs;
    }

    public Datapacket(ArrayList<String> t){
        this.clients=t;
    }


    public Datapacket(int com,ArrayList<String> t){
        this.command=com;
        this.clients=t;
        this.filelist=new ArrayList<String>();
        this.filename="";
        this.username="";
        this.filesize=0;
    }

    public Datapacket(int com,ArrayList<String> t,ArrayList<String> f){
        this.command=com;
        this.clients=t;
        this.filelist=f;
    }

    public Datapacket(int com,String username,ArrayList<String> f){
        this.command=com;
        this.username=username;
        this.filelist=f;
    }

    public String getUserName(){
        return this.username;
    }

    public String getFileName(){
        return this.filename;
    }

    public int getCommand(){
        return this.command;
    }

    public long getFileSize(){
        return this.filesize;
    }

    public ArrayList<String> getUserList(){
        return this.clients;
    }

    public ArrayList<String> getFileList(){
        return this.filelist;
    }
    
}