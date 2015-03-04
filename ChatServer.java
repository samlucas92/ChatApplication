package wschatserver;

import javax.jws.WebService;
import javax.jws.WebMethod;
import java.util.*;

@WebService
public class ChatServer {
    //creates a hashmap which will hold all the usernames and messages
    private static HashMap<String,String> serverList = new HashMap<>();

    /*Method which will check to see if the username that is given by the client
    is already in the chat and if it is then it will return true*/
    public boolean checks(String userName){
        synchronized (serverList) {
            for(String user : serverList.keySet()){
                if(user.equals(userName)){
                    return true;  
                }  
            }

            return false;
        }
    }
    /*Whisper method which will take in the message, userName and recipient name 
    This will then change the message that is sent to pm*/
    public void whisper(String message, String userName, String recip){
         synchronized(serverList){
            message = "PM from " + userName + " to " + recip + ": " + message;
            //if the serverlist contains the recipient value
            if(serverList.containsKey(recip)){
                //put the message and the both clients into the list ready to get
                //printed to the screen
                String val = serverList.get(recip);
                val = val + message;
                serverList.put(recip,val);
                serverList.put(userName,val);
            }else{
                //other wise the message that gets put into the list is to
                //notify that the user doesnt exist.
                String val = serverList.get(recip);
                val = "User does not exist";
                serverList.put(recip, val);
                serverList.put(userName, val);
            }
        }
    }
    @WebMethod
    //method for joining the server which will take in the username
    public void join(String userName){
        synchronized (serverList) {
            //puts the user name with an empty string into the list
            serverList.put(userName,"");
        }
    }
    
    @WebMethod
    //Method where the message will get printed out 
    //this take in the message and username as strings
    public void talk(String message, String userName){
        synchronized(serverList){
            //layout for the massage to be printed
            message = userName + ": " + message;
            for(String name : serverList.keySet()){
                String val = serverList.get(name);
                val = val + message;
                serverList.put(name,val);          
            }
        }
    }
    
    @WebMethod
    public String listen(String userName) {
        synchronized(serverList){
          String temp = serverList.get(userName);
          serverList.put(userName,"");
          return temp;
        } 
    }
    
    @WebMethod
    //leave method takes in the user name
    public void leave(String userName){
        //removes the username from the list
        serverList.remove(userName);
    }
}