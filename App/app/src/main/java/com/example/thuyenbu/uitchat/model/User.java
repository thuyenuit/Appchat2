package com.example.thuyenbu.uitchat.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    public String name;
    public String email;
    public String avata;
    public String gender;
    public Status status;
    public Message message;

    public List<String> listFriend;
    public List<String> listGroupChat;
    public Request listRequest;

    public User(){
        status = new Status();
        message = new Message();

        status.isOnline = false;
        status.timestamp = 0;
        message.idReceiver = "0";
        message.idSender = "0";
        message.text = "";
        message.timestamp = 0;

        listFriend = new ArrayList<>();
        listFriend.add("0");

        listGroupChat = new ArrayList<>();
        listGroupChat.add("0");

        listRequest = new Request();
        listRequest.Id = "0";
    }
}
