
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;


public class ServerOkno extends Application {
    public static Server server=null;
    private ListView<String> listaklientow;
    private String selectItem="";
    private ListView<String> listaplikow;


    public void ServerWin(Stage primaryStage){
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        Label pliki = new Label("Pliki:");
        listaklientow = getClientsList(server.getClienList());
        listaklientow.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> ov, String old_val, String new_val) -> {
                String temp=listaklientow.getSelectionModel().getSelectedItem();
                if(temp!=null)
                 selectItem=temp;
        });
        listaplikow = updateFilelist();
        Label klienci= new Label("Klienci:");
        Scene scene = new Scene(grid, 530, 400);   

        
        grid.add(klienci,0,0,1,1);
        grid.add(pliki,1,0,1,1);
        grid.add(listaklientow,0,1,1,1);
        grid.add(listaplikow,1,1,1,1);
        primaryStage.setTitle("Server");
        primaryStage.setScene(scene);
        
    }

    public ListView<String> updateFilelist(){
        
        ListView<String> temp=new ListView<String>();
        ObservableList<String> n= FXCollections.observableArrayList("");
        temp.setItems(n);
        Thread th=new Thread(()->{
            try{
                while(true){
                    Thread.sleep(1000);
                    String sciezka=server.getServerPath()+"/"+selectItem;
                    ArrayList<String> t=new ArrayList<String>();
                    File actual = new File(sciezka);
                    if(actual.listFiles()!=null){
                        for(File f:actual.listFiles()){
                            t.add(f.getName());
                        }
                    }
                    final ObservableList<String> helper = FXCollections.observableArrayList(t);
                        Platform.runLater(() -> {
                            temp.setItems(helper);
                    });
                }       
            }catch(InterruptedException e){
                e.printStackTrace();
            }
        });
        th.start();
        return temp;
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

    public ListView<String> getClientsList(ArrayList<String> clients) {
        ListView<String> t= new ListView<String>();
        ObservableList<String> n = FXCollections.observableArrayList("");
        t.setItems(n);
        Thread th = new Thread(()->{
            try{
                while(true){
                    ArrayList<String> temp = new ArrayList<String>();
                    Thread.sleep(1000);
                    for(String s:clients){
                        temp.add(s);
                    }
                    final ObservableList<String> helper = FXCollections.observableArrayList(temp);
                    Platform.runLater(() -> {
                        t.setItems(helper);
                    });
                }
            }catch(InterruptedException e){
                e.printStackTrace();

            }
        });

        th.start();
        return t;
    }


    public ListView<String> getFileList(String path) {
        ListView<String> filelist = new ListView<String>();
        ObservableList<String> n = FXCollections.observableArrayList("");
        filelist.setItems(n);
        Thread th = new Thread(() -> {
            try{
                while (true) {
                    ArrayList<String> temp = new ArrayList<String>();
                    File actual = new File(path);
                    Thread.sleep(1000);
                    if(actual.listFiles()!=null){
                        for(File f:actual.listFiles()){
                            temp.add(f.getName());
                        }
                    }
                    
                    final ObservableList<String> helper = FXCollections.observableArrayList(temp);
                    Platform.runLater(() -> {
                        filelist.setItems(helper);
                    });
                }
            }catch(InterruptedException e){
                e.printStackTrace();
            }         
        });
        th.start();
        return filelist;
    }

    @Override
    public void start(Stage primaryStage) {
        
        primaryStage.setTitle("Server!");
        Button btn = new Button();
        btn.setText("Run Server");

        btn.setOnAction((event) -> {
            try {
                server=new Server(new ServerSocket(9999));
                Thread th = new Thread(()->{
                    try {
                       
                        while(true){
                            Socket client = server.accept();
                            System.out.println("Nowy klient!");
                            ServerClient t = new ServerClient(client, server.getClienList(), server.getServerPath());
                            server.addNewClient(t);
                            t.start();
                            
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                th.start();
                ServerWin(primaryStage);
                
            } catch (Exception e) {
                System.out.println("Exception Occurred, Server is down.");
                e.printStackTrace();
            }
        });

        StackPane root = new StackPane();
        root.getChildren().addAll(btn);
        primaryStage.setScene(new Scene(root, 100, 100));
        primaryStage.show();
        
       
    }

    public static void main(String[] args) {
        launch();
        
    }

}