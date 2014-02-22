package com.squeed.microgramcaster.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.net.Uri;

import com.squeed.microgramcaster.MediaItem;
import com.squeed.microgramcaster.MediaStoreAdapter;

import fi.iki.elonen.NanoHTTPD;

/**
 * Subclass of the NanoHTTPD (https://github.com/NanoHttpd/nanohttpd/) lightweight
 * HTTP Server.
 * 
 * Derived from https://github.com/NanoHttpd/nanohttpd/blob/master/webserver/src/main/java/fi/iki/elonen/SimpleWebServer.java
 * 
 * My modification is stripping out unnecessary functionality and changing from File serving to MediaStore serving.
 * 
 * This server expects the displayname of a Mediastore as path argument. E.g. to serve 'sintel_trailer-720p.mp4',
 * the request should be to http://host:port/sintel_trailer-720p.mp4 
 * 
 * @author Erik (+ Paul S. Hawke, Jarno Elonen and Konstantinos Togias for https://github.com/NanoHttpd/nanohttpd)
 *
 */
public class MyHTTPD extends NanoHTTPD {
	
	public static final String WEB_SERVER_PROTOCOL = "http";
	public static final Integer WEB_SERVER_PORT = 8282;
	
	private Context ctx;
	private MediaStoreAdapter mediaStoreAdapter;

	
    /**
     * Common mime type for dynamic content: binary
     */
    public static final String MIME_DEFAULT_BINARY = "application/octet-stream";
   
    /**
     * Hashtable mapping (String)FILENAME_EXTENSION -> (String)MIME_TYPE
     */
    private static final Map<String, String> MIME_TYPES = new HashMap<String, String>() {{
//        put("css", "text/css");
//        put("htm", "text/html");
//        put("html", "text/html");
//        put("xml", "text/xml");
//        put("java", "text/x-java-source, text/java");
//        put("md", "text/plain");
//        put("txt", "text/plain");
//        put("asc", "text/plain");
        put("gif", "image/gif");
        put("jpg", "image/jpeg");
        put("jpeg", "image/jpeg");
        put("png", "image/png");
        put("mp3", "audio/mpeg");
        put("m3u", "audio/mpeg-url");
        put("mp4", "video/mp4");
        put("ogv", "video/ogg");
//        put("flv", "video/x-flv");
//        put("mov", "video/quicktime");
//        put("swf", "application/x-shockwave-flash");
//        put("js", "application/javascript");
//        put("pdf", "application/pdf");
//        put("doc", "application/msword");
//        put("ogg", "application/x-ogg");
//        put("zip", "application/octet-stream");
//        put("exe", "application/octet-stream");
//        put("class", "application/octet-stream");
    }};
	
    
    public MyHTTPD(String hostName, int port, Context ctx, MediaStoreAdapter mediaStoreAdapter) {
		super(port);
		this.ctx = ctx;
		this.mediaStoreAdapter = mediaStoreAdapter;
	}
	
	
    public Response serve(IHTTPSession session) {
        Map<String, String> header = session.getHeaders();
        String uri = session.getUri();

        try {
			return respond(Collections.unmodifiableMap(header), uri);
		} catch (IOException e) {
			return createResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT,
                  "INTERNAL ERRROR: " + e.getMessage());
		}
    }

    private Response respond(Map<String, String> headers, String uri) throws IOException {
        // Remove URL arguments
        uri = uri.trim().replace(File.separatorChar, '/');
        if (uri.indexOf('?') >= 0) {
            uri = uri.substring(0, uri.indexOf('?'));
        }
        
        String requestedFile = uri;

        // Prohibit getting out of current directory
        if (uri.startsWith("src/main") || uri.endsWith("src/main") || uri.contains("../")) {
            return createResponse(Response.Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT, "FORBIDDEN: Won't serve ../ for security reasons.");
        }

        if(requestedFile.startsWith("/")) {
        	requestedFile = requestedFile.substring(1);
        }
        final MediaItem mediaItem = mediaStoreAdapter.findFile(ctx, requestedFile);		
        if(mediaItem == null) {
        	return createResponse(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "NOT_FOUND: " + requestedFile);
        }
		final InputStream is = ctx.getContentResolver().openInputStream(Uri.parse("file:" + mediaItem.getData()));
       

        String mimeTypeForFile = getMimeTypeForFile(uri);
      
        Response response = serveFile(uri, headers, mediaItem, is, mimeTypeForFile);
     
        return response != null ? response :
            createResponse(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "Error 404, file not found.");
    }

    
    // Get MIME type from file name extension, if possible
    private String getMimeTypeForFile(String uri) {
        int dot = uri.lastIndexOf('.');
        String mime = null;
        if (dot >= 0) {
            mime = MIME_TYPES.get(uri.substring(dot + 1).toLowerCase());
        }
        return mime == null ? MIME_DEFAULT_BINARY : mime;
    }

    /**
     * Serves file from homeDir and its' subdirectories (only). Uses only URI, ignores all headers and HTTP parameters.
     */
    Response serveFile(String uri, Map<String, String> header, MediaItem mediaItem, InputStream is, String mime) {
        Response res;
        try {
            // Calculate etag
            String etag = Integer.toHexString((mediaItem.getName() + mediaItem.getLastModified() + "" + mediaItem.getSize()).hashCode());

            // Support (simple) skipping:
            long startFrom = 0;
            long endAt = -1;
            String range = header.get("range");
            if (range != null) {
                if (range.startsWith("bytes=")) {
                    range = range.substring("bytes=".length());
                    int minus = range.indexOf('-');
                    try {
                        if (minus > 0) {
                            startFrom = Long.parseLong(range.substring(0, minus));
                            endAt = Long.parseLong(range.substring(minus + 1));
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            // Change return code and add Content-Range header when skipping is requested
            long fileLen = mediaItem.getSize(); //.length();
            if (range != null && startFrom >= 0) {
                if (startFrom >= fileLen) {
                    res = createResponse(Response.Status.RANGE_NOT_SATISFIABLE, NanoHTTPD.MIME_PLAINTEXT, "");
                    res.addHeader("Content-Range", "bytes 0-0/" + fileLen);
                    res.addHeader("ETag", etag);
                } else {
                    if (endAt < 0) {
                        endAt = fileLen - 1;
                    }
                    long newLen = endAt - startFrom + 1;
                    if (newLen < 0) {
                        newLen = 0;
                    }

                    final long dataLen = newLen;//                  
                    is.skip(startFrom);

                    res = createResponse(Response.Status.PARTIAL_CONTENT, mime, is);
                    //res.addHeader("Content-Length", "" + dataLen); // Note: The Content-Length is also set inside NanoHTTPD code which results in duplicate headers, which breaks Chrome playback.
                    res.addHeader("Content-Range", "bytes " + startFrom + "-" + endAt + "/" + fileLen);
                    res.addHeader("ETag", etag);
                }
            } else {
                if (etag.equals(header.get("if-none-match")))
                    res = createResponse(Response.Status.NOT_MODIFIED, mime, "");
                else {
                    res = createResponse(Response.Status.OK, mime, is);
                   // res.addHeader("Content-Length", "" + fileLen);
                    res.addHeader("ETag", etag);
                }
            }
        } catch (IOException ioe) {
            res = createResponse(Response.Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT, "FORBIDDEN: Reading file failed.");
        }

        return res;
    }
	
    // Announce that the file server accepts partial content requests
    private Response createResponse(Response.Status status, String mimeType, InputStream message) {
        Response res = new Response(status, mimeType, message);
        res.addHeader("Accept-Ranges", "bytes");
        return res;
    }

    // Announce that the file server accepts partial content requests
    private Response createResponse(Response.Status status, String mimeType, String message) {
        Response res = new Response(status, mimeType, message);
        res.addHeader("Accept-Ranges", "bytes");
        return res;
    }
}
