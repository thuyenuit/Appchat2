package com.example.thuyenbu.uitchat.model;

import java.util.LinkedList;
import java.util.List;

public class MessageTest {
    public String _name;
    public String _listMember;
    public List<ContentMessageTest> _listConten;

    public MessageTest(){
        _name = "";
        _listMember = "";
        _listConten = new LinkedList<ContentMessageTest>();
    }
}
