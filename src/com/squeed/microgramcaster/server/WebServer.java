package com.squeed.microgramcaster.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.apache.http.HttpException;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;

import com.squeed.microgramcaster.MediaStoreAdapter;

import android.content.Context;

/**
 * Derived from http://www.integratingstuff.com/2011/10/24/adding-a-webserver-to-an-android-app/
 * 
 * @author Erik
 *
 */
public class WebServer {

	public static boolean RUNNING = false;
	public static int serverPort = 8181;

	private static final String ALL_PATTERN = "*.mp4";

	private Context context = null;

	private BasicHttpProcessor httpproc = null;
	private BasicHttpContext httpContext = null;
	private HttpService httpService = null;
	private HttpRequestHandlerRegistry registry = null;

	public WebServer(Context context, MediaStoreAdapter mediaStoreAdapter) {
		this.setContext(context);

		httpproc = new BasicHttpProcessor();
		httpContext = new BasicHttpContext();

		httpproc.addInterceptor(new ResponseDate());
		httpproc.addInterceptor(new ResponseServer());
		httpproc.addInterceptor(new ResponseContent());
		httpproc.addInterceptor(new ResponseConnControl());

		httpService = new HttpService(httpproc,
		    new DefaultConnectionReuseStrategy(), new DefaultHttpResponseFactory());

		registry = new HttpRequestHandlerRegistry();

		registry.register(ALL_PATTERN, new MediaRequestHandler(context, mediaStoreAdapter));

		httpService.setHandlerResolver(registry);
	}

	private ServerSocket serverSocket;

	public void runServer() {
		Thread thread = new Thread() {
            @Override
            public void run() {
            	try {
        			serverSocket = new ServerSocket(serverPort);

        			serverSocket.setReuseAddress(true);

        			while (RUNNING) {
        				try {
        					final Socket socket = serverSocket.accept();

        					DefaultHttpServerConnection serverConnection = new DefaultHttpServerConnection();

        					serverConnection.bind(socket, new BasicHttpParams());

        					httpService.handleRequest(serverConnection, httpContext);

        					serverConnection.shutdown();
        				} catch (IOException e) {
        					e.printStackTrace();
        				} catch (HttpException e) {
        					e.printStackTrace();
        				}
        			}

        			serverSocket.close();
        		} catch (SocketException e) {
        			e.printStackTrace();
        		} catch (IOException e) {
        			e.printStackTrace();
        		}

        		RUNNING = false;
            }
        };
        thread.start();
		
	}

	public synchronized void startServer() {
		RUNNING = true;
		runServer();
	}

	public synchronized void stopServer() {
		RUNNING = false;
		if (serverSocket != null) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public Context getContext() {
		return context;
	}
}
