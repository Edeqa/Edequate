package com.edeqa.edequate;

import com.edeqa.helpers.Misc;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public class RequestFilter implements Filter {

    private final static Logger LOGGER = Logger.getLogger(RequestFilter.class.getSimpleName());

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        Misc.log("Request", "initialized");
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response,
                         final FilterChain chain) throws IOException, ServletException {

        String path = ((HttpServletRequest) request).getRequestURI();
        String query = ((HttpServletRequest) request).getQueryString();
        String ipRemote = request.getRemoteAddr();

        Misc.log("RequestFilter", "[" + ipRemote + "]", path, query != null ? query : "");

//        if (path.toLowerCase().startsWith("/rest")) {
//            request.getRequestDispatcher(path).forward(request, response);
//        } else {
            chain.doFilter(request, response);
//        }
    }

    @Override
    public void destroy() {
        Misc.log("Request", "destroyed");
    }


}