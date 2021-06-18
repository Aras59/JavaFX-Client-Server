
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.*;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class ClientFX extends Application {
    private static Client clientfx=null;
    private ArrayList<String> clients = new ArrayList<String>();

    /*private void badlogin(Stage primaryStage) {
        GridPane grid = new GridPane();
        Scene scene = new Scene(grid, 400, 20);
        Label info = new Label("Serwer niedostepny lub nazwa zajeta!");
        grid.addRow(0, info);
        primaryStage.setTitle("Blad");
        primaryStage.setScene(scene);
        primaryStage.show();
    }*/

    private void clientLoginWin(Stage primaryStage) {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        Label user = new Label("Nazwa uzytkownika:");
        Label path = new Label("Scieżka do lokalnego folderu:");
        TextField tf1 = new TextField();
        TextField tf2 = new TextField();
        Button login = new Button("Login");
        Scene scene = new Scene(grid, 400, 400);
        grid.addRow(0, user, tf1);
        grid.addRow(1, path, tf2);
        grid.addRow(2, login);
        primaryStage.setTitle("Klient");
        primaryStage.setScene(scene);
        primaryStage.show();
        login.setOnAction((event) -> {
            try {
                clientfx = new Client(new Socket(InetAddress.getLocalHost(), 9999), 9999, tf1.getText(), tf2.getText());
                clientfx.clientConnetct();
                clientfx.start();
                clientWorkWin(primaryStage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        
    }

    private void clientWorkWin(Stage primaryStage) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        Scene scene = new Scene(grid, 530, 400);
        ListView<String> listaplikow = FileScan(clientfx.getpath());
        ListView<String> listaklientow = getClientsList();
        Label filelabel = new Label("Pliki użytkownika: "+clientfx.getClientname());
        Label status = statusUpdate();
        
        Button bt = new Button("Wyślij!");
        bt.setOnAction((event)->{
           String user=listaklientow.getSelectionModel().getSelectedItem();
           System.out.println(user);
           String file= listaplikow.getSelectionModel().getSelectedItem();
           System.out.println(file);
            if(user!=null&&file!=null){
              clientfx.put(user,file);
            }

        });
        
        grid.add(bt, 0, 0, 1, 1);
        grid.add(status,0,3,1,1);
        grid.add(filelabel, 0, 1, 1, 1);
        grid.add(listaklientow, 1, 2, 1, 1);
        grid.add(listaplikow, 0, 2, 1, 1);
        primaryStage.setTitle("Klient" + " " + clientfx.getClientname());
        primaryStage.setScene(scene);
    }

    public Label statusUpdate(){
        Label temp = new Label();
        Thread th = new Thread(()->{
            while(true){
                try{
                    
                    Thread.sleep(100);
                    final String t=clientfx.getstatus();
                    Platform.runLater(()->{
                        temp.setText(t);
                    });
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
                
                
            }
        });
        th.start();
        return temp;
    }


    private ListView<String> FileScan(String path) {
        ListView<String> filelist = new ListView<String>();
        ObservableList<String> n = FXCollections.observableArrayList("");
        filelist.setItems(n);
        Thread th = new Thread(() -> {
            try{
                while (true) {
                    ArrayList<String> temp = new ArrayList<String>();
                    File actual = new File(path);
                    Thread.sleep(1000);
                    for(File f:actual.listFiles()){
                        temp.add(f.getName());
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


    public ListView<String> getClientsList() {
        ListView<String> t= new ListView<String>();
        ObservableList<String> n = FXCollections.observableArrayList("");
        t.setItems(n);
        Thread th = new Thread(()->{
            try{
                while(true){
                    clients=clientfx.getUserList();
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

    @Override
    public void start(Stage primaryStage) {
        clientLoginWin(primaryStage);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}