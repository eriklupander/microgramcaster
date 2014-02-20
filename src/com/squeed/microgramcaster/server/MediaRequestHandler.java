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
			
			//response.setHeader("Content-Length", ""+mediaItem.getSize());
			response.setEntity(entity);
		} else {
			response.setStatusCode(400);
			response.setHeader("Content-Type", "text/html");
		}
		
		
		
		
	}

	public Context getContext() {
		return context;
	}
}
