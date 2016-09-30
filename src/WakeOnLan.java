import java.io.*;
import java.net.*;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class WakeOnLan extends Application{
	
	public static final int PORT = 24;
	private Label label_ip,label_mac;
	private TextField tf_ip, tf_mac;
	private GridPane gp_wake;
	private Button btn;

	public static void main(String[] args) {
		launch(args);
	}
	public void init(){
		btn = new Button("wake pc");

		label_ip = new Label("ip");
		label_mac= new Label("mac");
		tf_ip = new TextField();
		tf_mac = new TextField();
		gp_wake = new GridPane();

		gp_wake.add(label_ip,0,0);
		gp_wake.add(label_mac,0,1);
		gp_wake.add(tf_ip,1,0);
		gp_wake.add(tf_mac,1,1);
		GridPane.setHgrow(tf_ip,Priority.ALWAYS);
		GridPane.setHgrow(tf_mac,Priority.ALWAYS);

		String[] temp = readfile();
		tf_ip.setText(temp[0]);
		tf_mac.setText(temp[1]);
		
		btn.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event){
				String ip = tf_ip.getText();
				String mac = tf_mac.getText();
				run(ip,mac);
			}
		});
		
	}
	public void start(Stage primaryStage){
		primaryStage.setTitle("Wake-on-LAN");
		VBox vb = new VBox();
		vb.setFillWidth(true);
		primaryStage.setScene(new Scene(vb,300,150));
		primaryStage.show();
		vb.getChildren().addAll(gp_wake,btn);
	}

	public void stop(){}

	
	private static byte[] getMacBytes(String macStr) throws IllegalArgumentException {
		byte[] bytes = new byte[6];
		String[] hex = macStr.split("(\\:|\\-)");
		if (hex.length != 6) {
			throw new IllegalArgumentException("Invalid MAC address.");
		}
		try {
			for (int i = 0; i < 6; i++) {
				bytes[i] = (byte) Integer.parseInt(hex[i], 16);
			}
		}
		catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid hex digit in MAC address.");
		}
		return bytes;
	}

	public static void run(String ip, String mac){
		
		String ipStr = ip;
		String macStr = mac;
		
		try {
			byte[] macBytes = getMacBytes(macStr);
			byte[] bytes = new byte[6 + 16 * macBytes.length];
			for (int i = 0; i < 6; i++) {
				bytes[i] = (byte) 0xff;
			}
			for (int i = 6; i < bytes.length; i += macBytes.length) {
				System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
			}
			
			InetAddress address = InetAddress.getByName(ipStr);
			DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, PORT);
			DatagramSocket socket = new DatagramSocket();
			socket.send(packet);
			socket.close();
			
			System.out.println("Wake-on-LAN packet sent.");
		}
		catch (Exception e) {
			System.out.println("Failed to send Wake-on-LAN packet: + e");
			System.exit(1);
		}
	}

	public static String[] readfile(){

		File check = new File("wol.ini");
		Boolean exi = check.exists();
		String[] temp = new String[2];

		if(exi){

			try(BufferedReader br = new BufferedReader(new FileReader("wol.ini")))
			{
				for(int i = 0; i < 2; i ++){
					temp[i] = br.readLine();
				}
			}
			catch(IOException e){
				e.printStackTrace();
			}
		}
		else{
			temp[0] = "ip not found please fill in";
			temp[1] = "mac not found please fill in";
		}	
		return temp;
	}
	
}