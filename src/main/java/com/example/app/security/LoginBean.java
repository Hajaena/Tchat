package com.example.app.security;

import com.example.app.entity.User;
import com.example.app.service.UserService;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import javax.faces.context.FacesContext;

@Named("auth")
@SessionScoped
public class LoginBean implements Serializable {
    private String username;
    private String password;
    private User user;

    @Inject
    // private UserService userService;

    // public String login() {
    // User u = userService.findByUsername(username);
    // if (u != null && userService.verifyPassword(u, password)) {
    // this.user = u;
    // return "/app/tchat.xhtml?faces-redirect=true";
    // }
    // // stay on login with message
    // FacesContext.getCurrentInstance().getExternalContext().getRequestMap()
    // .put("loginError", "Identifiants invalides");
    // return null;
    // }

    public String login() {
        // Bypass complet du login
        this.user = new User(); // marque l’utilisateur comme connecté
        FacesContext.getCurrentInstance().getExternalContext()
                .getSessionMap().put("auth", this); // alimente le filtre
        return "/app/tchat.xhtml?faces-redirect=true";
    }

    public String logout() throws IOException {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/login.xhtml?faces-redirect=true";
    }

    
    public boolean isLoggedIn() {
        return user != null;
    }

    public User getUser() {
        return user;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
