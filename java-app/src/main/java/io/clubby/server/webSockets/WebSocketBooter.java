package io.clubby.server.webSockets;


import io.stallion.http.ServeCommandOptions;
import io.stallion.settings.Settings;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;

import javax.websocket.server.ServerContainer;


public class WebSocketBooter {

    private ServerContainer serverContainer;

    public Server boot(Server server, HandlerCollection handlerCollection, ServeCommandOptions options) {
        if (server == null) {
            server = new Server();
        }
        //ServerConnector connector = new ServerConnector(server);
        //connector.setPort(8091);
        //server.addConnector(connector);

        // Setup the basic application "context" for this application at "/"
        // This is also known as the handler tree (in jetty speak)
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/st-wsroot");
        //context.setVirtualHosts(new String[]{"@Websocket"});

        /*
        ServerConnector webSocketConnector = new ServerConnector(server);
        webSocketConnector.setPort(Settings.instance().getPort() + 1);
        webSocketConnector.setName("Websocket");
        server.addConnector(webSocketConnector);
        */


        //server.setHandler(context);
        handlerCollection.addHandler(context);
        //context.setVirtualHosts();
        context.setServer(server);
        try
        {
            // Initialize javax.websocket layer
            ServerContainer wscontainer = WebSocketServerContainerInitializer.configureContext(context);
            wscontainer.setDefaultMaxSessionIdleTimeout(60 * 60 * 1000);

            // Add WebSocket endpoint to javax.websocket layer
            wscontainer.addEndpoint(WebSocketEventHandler.class);

            //server.start();
            server.dump(System.err);
            return server;
        }
        catch (Throwable t)
        {
            throw new RuntimeException(t);
        }
    }
}
