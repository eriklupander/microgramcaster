package com.squeed.microgramcaster.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Method;

public class NonBufferedResponse extends NanoHTTPD.Response {

   
	private Long available;
   
    /**
     * Basic constructor.
     */
    public NonBufferedResponse(Status status, String mimeType, InputStream data, Long available) {
        super(status, mimeType, data);
		this.available = available;
    }


    @Override
    protected void sendAsFixedLength(OutputStream outputStream, PrintWriter pw) throws IOException {
        int pending = (int) (data != null ? available : 0); // This is to support partial sends, see serveFile()
        pw.print("Content-Length: "+pending+"\r\n");

        pw.print("\r\n");
        pw.flush();

        if (requestMethod != Method.HEAD && data != null) {
            int BUFFER_SIZE = 16 * 1024;
            byte[] buff = new byte[BUFFER_SIZE];
            while (pending > 0) {
                int read = data.read(buff, 0, ((pending > BUFFER_SIZE) ? BUFFER_SIZE : pending));
                if (read <= 0) {
                    break;
                }
                outputStream.write(buff, 0, read);

                pending -= read;
            }
        }
    }

}
