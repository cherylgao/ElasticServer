package com.cheryl.server;

import java.lang.management.ManagementFactory;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.glassfish.jersey.servlet.ServletContainer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;

import com.cheryl.util.Latch;

public class ElasticSearchServer {
    private static final String JERSEY_RESOURCES = "com.cheryl.resources";
    private Server jettyServer;

    public static void main(String[] args) {
        ElasticSearchServer server = new ElasticSearchServer(8080);
        server.startOnThread();
        server.join();
        System.exit(0);
    }

    public ElasticSearchServer(int port) {
        WebApplicationContext appContext = getContext();
        HandlerCollection handlers = new HandlerCollection();
        ServletContextHandler scHandler = getServletContextHandler(handlers, createJerseyServletHolder(), appContext);
        Server server = createServer(port);
        server.setHandler(handlers);
        server.setStopAtShutdown(true);
        jettyServer = server;
    }
    
    private static ServletContextHandler getServletContextHandler(HandlerCollection handlers, ServletHolder servletHolder, WebApplicationContext appContext) {
        ServletContextHandler servletContextHandler;
        servletContextHandler = new ServletContextHandler(handlers, "/", ServletContextHandler.NO_SESSIONS);
        servletContextHandler.addServlet(servletHolder, "/*");
        servletContextHandler.addEventListener(new ContextLoaderListener(appContext));
        return servletContextHandler;
    }

    private static WebApplicationContext getContext() {
        XmlWebApplicationContext ctx = new XmlWebApplicationContext();
        ctx.setConfigLocation("classpath:/application-context.xml");
        ctx.refresh();
        return ctx;
    }

    private static ServletHolder createJerseyServletHolder() {
        ServletHolder jerseyServletHolder = new ServletHolder(ServletContainer.class);
        jerseyServletHolder.setInitParameter("jersey.config.server.provider.packages", JERSEY_RESOURCES);
        jerseyServletHolder.setInitOrder(1);
        jerseyServletHolder.setInitParameter("jersey.config.server.tracing", "ALL");
        return jerseyServletHolder;
    }

    private Server createServer(int port) {
        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(50);
        QueuedThreadPool threadPool = new QueuedThreadPool(200, 10, 60000, queue);
        Server server = new Server(threadPool);
        ServerConnector connector = new ServerConnector(server, 20, 50);
        connector.setHost("0.0.0.0");
        connector.setPort(port);
        connector.setAcceptQueueSize(20);
        server.addConnector(connector);
        addMBContainer(server);
        return server;
    }
    
    private static void addMBContainer(Server server) {
        MBeanContainer mbContainer = new MBeanContainer(ManagementFactory.getPlatformMBeanServer());
        server.addEventListener(mbContainer);
        server.addBean(mbContainer);
    }
    public Thread startOnThread() {
        Latch initialized = new Latch();
        Thread thread = new Thread(() -> {
            try {
                System.out.println("Server is starting");
                jettyServer.start();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                initialized.done();
            }
        });
        thread.start();
        initialized.await();
        return thread;
    }

    public void join() {
        try {
            System.out.println("Server waiting for jetty to finish.");
            jettyServer.join();
        }
        catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
