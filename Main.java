package wsChatClient;

import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

import javax.xml.ws.WebServiceRef;

import wschatserver.ChatServerService;
import wschatserver.ChatServer;
        

public class Main {    
    private JFrame frame;

    private JTextArea myText;
    private static JTextArea otherText;
    private JScrollPane myTextScroll;
    private JScrollPane otherTextScroll;
    private static TextThread otherTextThread;
    private String textString = "";
    
    private static final int HOR_SIZE = 400;
    private static final int VER_SIZE = 150;
    private static final int TYP_SIZE = 20;
    
    private ChatServerService service;
    private ChatServer port;
    private String userName;
    private boolean test = true;

    private void initComponents(String host) {
    	frame = new JFrame("Chat Client");
        myText = new JTextArea();

        myTextScroll = new JScrollPane(myText);			
        myTextScroll.setHorizontalScrollBarPolicy(
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                
		myTextScroll.setVerticalScrollBarPolicy(
			JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		myTextScroll.setMaximumSize(
		    new java.awt.Dimension(HOR_SIZE, TYP_SIZE));
		myTextScroll.setMinimumSize(new java.awt.Dimension(HOR_SIZE, TYP_SIZE));
		myTextScroll.setPreferredSize(new java.awt.Dimension(
		    HOR_SIZE, TYP_SIZE));
                

        myText.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                textTyped(evt);
            }
        });
        frame.getContentPane().add(myTextScroll, java.awt.BorderLayout.SOUTH);
        
        otherText = new JTextArea();
        
        otherTextScroll = new JScrollPane(otherText);
        otherText.setBackground(new java.awt.Color(200, 200, 200));
        otherTextScroll.setHorizontalScrollBarPolicy(
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        otherTextScroll.setVerticalScrollBarPolicy(
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        otherTextScroll.setMaximumSize(
            new java.awt.Dimension(HOR_SIZE, VER_SIZE));
        otherTextScroll.setMinimumSize(
            new java.awt.Dimension(HOR_SIZE, VER_SIZE));
        otherTextScroll.setPreferredSize(new java.awt.Dimension(
		    HOR_SIZE, VER_SIZE));
        otherText.setEditable(false);
               
        frame.getContentPane().add(otherTextScroll,
            java.awt.BorderLayout.CENTER);
            
        //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        /*Set up a JOptionPane which would ask the user for a user name
        If the user name is blank or the cancel button is pressed which 
        would automatically set the value of user name to null it will ask
        for a user name until it gets one*/
        while(test){
            userName = JOptionPane.showInputDialog("Please enter a user name!");
            if (userName == null || userName.equals("")){
                System.out.print("Please enter a valid user name");
                test = true;
            }else{
                test = false;  
            }
            
        }
        try {
            // open up the chat service and set it to a variable called port 
            service = new wschatserver.ChatServerService();
            port = service.getChatServerPort();
            //set the test variable to true
            test = true;
            /*Set up a JOptionPane which would ask the user for a user name
            If the user name is already taken it will ask for a user name 
            that isnt taken until it gets one*/
            while(test){
                if(port.checks(userName)){
                    System.out.println("User name already exists try again!");
                    userName = JOptionPane.showInputDialog("Please enter a user name!");
                    test = true;
                }else{
                    port.join(userName);
                    port.talk( "has joined the chat\n", userName);
                    test = false;  
                }
            }
            //Sets up the otherTextThread which is the one that will display the 
            //messages.
            otherTextThread = new TextThread(otherText, userName, port);
            //start the other text thread
            otherTextThread.start();
            //Sets up the frames window listener for when it is closed
            frame.addWindowListener(new WindowAdapter() {
              public void windowClosing(WindowEvent e) {
                    /*When the window is closed try to leave the server*/
                    try {
                        //Use the talk method to tell the other clients that 
                        //a certain user has left.
                        port.talk( "has left the server!\n", userName);
                        //call the leave method sending in the user name 
                        //of the client that is leaving.
                        port.leave(userName);
                        
                    }
                    catch (Exception ex) {
                        //Display that the exit has failed if the error is caught
                        otherText.append("Exit failed.\n");
                    }
                    //Exit the program.
                    System.exit(0);
              }
            });
          
        }
        
        catch (Exception ex) {
            //if exceptions are caught before the client has joined
            //Display error message
            otherText.append("Failed to connect to server.\n");
        }
    }

    private void textTyped(java.awt.event.KeyEvent evt) {
        //get the input from the keyboard
        char c = evt.getKeyChar();
        //id = port.join(userName);
        //if the input is new line or enter
        if (c == '\n'){
            try {
                //if statement for the private messages
               if(textString.substring(0,2).equals("/w")){
                   //add the input to the text string 
                    textString = textString + c;
                    //variables for grabbing the substring for the recipient 
                    //name which is after the first space and before the second
                    //space.
                    int firstSpace = textString.indexOf(" ");
                    int secondSpace = textString.indexOf(" ", firstSpace+1);
                    //string that holds the recipients name
                    String recipSend = textString.substring(3, secondSpace);
                    //String that is getting sent without the /w and recipient
                    //name.
                    String sendString = textString.substring(secondSpace);
                    //set the text to empty
                    myText.setText("");
                    //send the string username and recipname through to 
                    //the whisper method in the server.
                    port.whisper(sendString,userName,recipSend);
               }else{
                   //else just send a notmal message that all clients can see.
                    textString = textString + c;
                    myText.setText("");
                    port.talk(textString,userName);
               }
            }
            catch (Exception ie) {
                //catch exception and display that the message cannot be sent
                    otherText.append("Failed to send message.\n");
            }
            //set text string to empty string
            textString = "";
            //if the backspace key is pressed
        } else if(c=='\b'){
            //if the length of the string is not 0 
            if(textString.length() != 0){
                //make text string a substring of textstring that has 1 less 
                //character
              textString = textString.substring(0,textString.length()-1);  
            }
        } else {
            //add the new character to the text string
            textString = textString + c;
        }
        
    }
    
    
    public static void main(String[] args) {
    	/*if (args.length < 1) {
    		System.out.println("Usage: AppChatClient host");
    		return;
    	}*/
        //TODO put CLI for host back in
        //set up the frame and all its components
    	final String host = "localhost";
    	javax.swing.SwingUtilities.invokeLater(new Runnable() {
    		Main client = new Main();
                @Override
    		public void run() {
    			client.initComponents(host);
    		}
    	});
    	
    }
}
//set up the textthread that is used to write messages
class TextThread extends Thread {

    ObjectInputStream in;
    JTextArea otherText;
    int id;
    String user;
    ChatServer port;
    
    
    TextThread(JTextArea other, String userName, ChatServer port) throws IOException
    {
        otherText = other;
        user = userName;
        this.port = port;
        
    }
    @Override
    public void run() {
        while (true) {
            try {
                String newText = port.listen(user);
                if (!newText.equals("")) {
                    
                    otherText.append(newText);
                }
                Thread.sleep(1000);
            }
            catch (Exception e) {
                    otherText.append("Error reading from server.\n");
                    //return;
            }  
        }
    }
}