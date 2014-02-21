package com.squeed.microgramcaster.server;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import android.content.Context;
import android.net.Uri;

import com.squeed.microgramcaster.MediaItem;
import com.squeed.microgramcaster.MediaStoreAdapter;

public class MediaRequestHandler implements HttpRequestHandler {
	
	private Context context = null;
	private MediaStoreAdapter mediaStoreAdapter;

	public MediaRequestHandler(Context context, MediaStoreAdapter mediaStoreAdapter) {
		this.context = context;
		this.mediaStoreAdapter = mediaStoreAdapter;
	}

	@Override
	public void handle(HttpRequest request, HttpResponse response,
	    HttpContext httpContext) throws HttpException, IOException {
		final String requestedFile = request.getRequestLine().getUri().substring(request.getRequestLine().getUri().lastIndexOf("/") + 1); 
		final MediaItem mediaItem = mediaStoreAdapter.findFile(context, requestedFile);
		if(mediaItem == null) {
			response.setStatusCode(500);
			response.setHeader("Content-Type", "text/html");
			return;
		}
		
		String r = request.getFirstHeader("Range").getValue();
		if(r != null && r.startsWith("bytes=")) {
			
//			response.setStatusCode(206);
//			
//					
//			String range = r.split("=")[1];
//			
//			final long rangeStart = Long.parseLong(range.split("-")[0]);
//			final long rangeEnd = Long.parseLong(range.split("-").length == 2 ? range.split("-")[1] : "-1");
//			response.setHeader("Accept-Ranges","bytes");
//			response.setHeader("Content-Length", ""+ ((rangeEnd != -1 ? rangeEnd : mediaItem.getSize()) - rangeStart));
//			response.setHeader("Content-Range","bytes "+rangeStart+"-"+(rangeEnd != -1 ? rangeEnd : mediaItem.getSize())+"/" + mediaItem.getSize());
			
			HttpEntity entity = new EntityTemplate(new ContentProducer() {
				public void writeTo(final OutputStream outstream) throws IOException {
					InputStream input = context.getContentResolver().openInputStream(Uri.parse("file:" + mediaItem.getData()));
					try {
						
						byte[] buffer = new byte[4096];
						int len;
						while ((len = input.read(buffer)) != -1) {
							outstream.write(buffer, 0, len);	
							outstream.flush();
						}
						outstream.flush();
						
					} catch (Exception e) {
						e.printStackTrace();
					} finally {

						if(input != null) input.close();
					}
				}
			});
			
			response.setHeader("Content-Type", "video/mp4");
			response.setHeader("Etag", MD5(mediaItem.getName() + mediaItem.getLastModified()));
			
			//response.setHeader("Content-Length", ""+mediaItem.getSize());
			response.setEntity(entity);
		} else {
			response.setStatusCode(400);
			response.setHeader("Content-Type", "text/html");
		}
	}
	
	
	private void handleEtag(HttpRequest req, HttpResponse resp, MediaItem item) {
		// If file not has been modified, send a 304
        String hash = request.getFirstHeader("If-None-Match").getValue();
        String etag = MD5(item.getName()+item.getLastModified());
        if (hash != null && etag.equals(hash)) {
            resp.setStatus(304); // Not modified
            resp.setHeader("Etag", etag);
            return;
        } else if (req.getDateHeader("If-Modified-Since") > 0) {
            // Fallback if If-None-Match is unsupported
            final Calendar modifiedSince = Calendar.getInstance();
            modifiedSince.setTimeInMillis(req.getDateHeader("If-Modified-Since"));
            final Calendar modified = Calendar.getInstance();
            modified.setTimeInMillis(file.lastModified());
            modified.set(Calendar.MILLISECOND, 0);
            if (!modified.after(modifiedSince)) {
                resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                resp.setHeader("Etag", etag);
                return;
            }
        }
	}
	
	public static String MD5(String md5) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
        }
        return null;
    }

	public Context getContext() {
		return context;
	}
	
	private void writeRangeToResponse(HttpRequest req, HttpResponse resp, MediaItem mediaItem, InputStream inputStrea, OutputStream os, Range range) throws IOException {
        int contentLength = (int) (range.end - range.start + 1);

        resp.setHeader("Content-Range", "bytes " + range.start + "-" + range.end + "/" + range.length);
        resp.setContentLength(contentLength);
        resp.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);

       

        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            byte[] buf = new byte[BUFFER_SIZE];
            raf.seek(range.start);

            for (long i = range.start; i <= range.end; i += BUFFER_SIZE) {
                int readBytes = is.read(buf);
                os.write(buf, 0, Math.min(readBytes, (int) (range.end - i + 1)));
            }
            os.flush();
        } catch (IOException e) {
            // This might happen if web client is shut down. Therefore, it is handled silently.
            if (logger.isDebugEnabled()) {
                logger.debug("IO Exception trying to serve file " + file.getPath() + ": " + e.getMessage());
            }
        }
        // Do not close the stream, it's managed by the web container
    }
	
	private Range parseContentRange(HttpRequest request, HttpResponse response, MediaItem mediaItem) throws IOException {

        // Retrieving the content-range header (if any is specified
        String rangeHeader = request.getFirstHeader("Range").getValue();

        if (rangeHeader == null) {
            // Try if-range
            String md5 = request.getHeader("If-Range");
            if (MD5(mediaItem.getName() + mediaItem.getLastModified()).equals(md5)) {
                rangeHeader = request.getHeader("Range");
            }
        }
        if (rangeHeader == null)
            return null;

        // bytes is the only range unit supported
        if (!rangeHeader.startsWith("bytes")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }

        rangeHeader = rangeHeader.substring(6).trim();

        int dashPos = rangeHeader.indexOf('-');
        int slashPos = rangeHeader.indexOf('/');

        if (dashPos == -1) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }

        Range range = new Range();

        try {
            range.start = Long.parseLong(rangeHeader.substring(0, dashPos));
            if (slashPos == -1) {
                try {
                    range.end = Long.parseLong(rangeHeader.substring(dashPos + 1));
                } catch (NumberFormatException e) {
                    range.end = file.length() - 1;
                }
                range.length = file.length();
            } else {
                range.end = Long.parseLong(rangeHeader.substring(dashPos + 1, slashPos));
                range.length = Long.parseLong(rangeHeader.substring(slashPos + 1, rangeHeader.length()));
            }
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }

        if (!range.validate()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }

        return range;

    }

    private class Range {

        public long start;
        public long end;
        public long length;

        /**
         * Validate range.
         */
        public boolean validate() {
            if (end >= length)
                end = length - 1;
            return ((start >= 0) && (end >= 0) && (start <= end) && (length > 0));
        }

        public void recycle() {
            start = 0;
            end = 0;
            length = 0;
        }

    }
}
