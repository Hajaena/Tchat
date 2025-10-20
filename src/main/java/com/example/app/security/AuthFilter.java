package com.example.app.security;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter("/app/*")
public class AuthFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        LoginBean auth = (LoginBean) request.getSession().getAttribute("auth");
        boolean logged = auth != null && auth.isLoggedIn();
        if (!logged) {
            response.sendRedirect(request.getContextPath() + "/login.xhtml");
            return;
        }
        chain.doFilter(req, res);
    }
}
