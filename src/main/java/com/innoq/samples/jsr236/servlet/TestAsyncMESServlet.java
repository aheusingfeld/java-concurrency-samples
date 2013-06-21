package com.innoq.samples.jsr236.servlet;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Sample servlet showing how to use JSR236's ManagedExecutorService with Servlet3.1 async support.
 */
@WebServlet(urlPatterns="/testAsync", asyncSupported=true)
public class TestAsyncMESServlet extends HttpServlet {

    @Resource
    private ManagedExecutorService managedExecutorService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        final AsyncContext asyncContext = req.startAsync();
        final PrintWriter writer = res.getWriter();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                writer.println("Done");
                asyncContext.complete();
            }
        };
        managedExecutorService.submit(runnable);
    }
}
