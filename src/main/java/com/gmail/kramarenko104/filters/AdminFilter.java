package com.gmail.kramarenko104.filters;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class AdminFilter implements Filter {

    public AdminFilter() {
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest hreq = (HttpServletRequest) req;
        HttpSession session = hreq.getSession();
        if (session != null) {
            Object isAdminAttr = session.getAttribute("isAdmin");
            if (isAdminAttr == null || (isAdminAttr != null && !(boolean) isAdminAttr)) {
                req.getRequestDispatcher("/forbidden").forward(req, resp);
            }
        }
        chain.doFilter(req, resp);
    }

    public void destroy() {
    }
}
