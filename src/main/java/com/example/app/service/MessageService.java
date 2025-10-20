package com.example.app.service;

import com.example.app.entity.ChatMessage;
import com.example.app.entity.Student;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

@ApplicationScoped
public class MessageService {

    @PersistenceContext(unitName = "appPU")
    private EntityManager em;

    @Transactional
    public ChatMessage saveMessage(String studentName, String content) {
        Student s = em.createQuery("SELECT s FROM Student s WHERE s.name = :n", Student.class)
                .setParameter("n", studentName)
                .getResultStream().findFirst().orElse(null);
        if (s == null) {
            s = new Student();
            s.setName(studentName);
            em.persist(s);
        }
        ChatMessage m = new ChatMessage();
        m.setStudent(s);
        m.setContent(content);
        m.setSentAt(new Date());
        em.persist(m);
        return m;
    }

    public List<ChatMessage> last(int n) {
        return em.createQuery("SELECT m FROM ChatMessage m ORDER BY m.sentAt DESC", ChatMessage.class)
                .setMaxResults(n)
                .getResultList();
    }
}
