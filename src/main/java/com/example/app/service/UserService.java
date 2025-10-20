package com.example.app.service;

import com.example.app.entity.User;
import org.mindrot.jbcrypt.BCrypt;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

@ApplicationScoped
public class UserService {

    @PersistenceContext(unitName = "appPU")
    private EntityManager em;

    public User findByUsername(String username) {
        return em.createQuery("SELECT u FROM User u WHERE u.username = :u", User.class)
                .setParameter("u", username)
                .getResultStream().findFirst().orElse(null);
    }

    @Transactional
    public void createUser(String username, String rawPassword, String fullName) {
        User u = new User();
        u.setUsername(username);
        u.setPasswordHash(BCrypt.hashpw(rawPassword, BCrypt.gensalt()));
        u.setFullName(fullName);
        em.persist(u);
    }

    public boolean verifyPassword(User u, String rawPassword) {
        return u != null && BCrypt.checkpw(rawPassword, u.getPasswordHash());
    }
}
