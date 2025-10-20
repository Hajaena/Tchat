package com.example.app.web;

import com.example.app.entity.ChatMessage;
import com.example.app.service.MessageService;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named("chat")
@RequestScoped
public class ChatBean implements Serializable {

    private String studentName;
    private String text;
    private List<ChatMessage> messages;

    @Inject
    private MessageService messageService;

    @PostConstruct
    public void init() {
        messages = messageService.last(50);
    }

    public List<ChatMessage> getMessages() { return messages; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}
