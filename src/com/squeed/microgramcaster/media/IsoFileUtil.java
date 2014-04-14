package com.squeed.microgramcaster.media;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.List;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.FileTypeBox;
import com.googlecode.mp4parser.FileDataSourceImpl;

public class IsoFileUtil {

	public static String getInfo(FileChannel fc) {
		IsoFile isoFile = null;
		try {
			isoFile = new IsoFile(new FileDataSourceImpl(fc));
			List<Box> boxes = isoFile.getBoxes();
			StringBuilder buf = new StringBuilder();

			buf.append(isoFile.toString());
			for(Box box : boxes) {
				
				buf.append("Class: " + box.getClass().getSimpleName() + "\n");
				buf.append("Type: " + box.getType() + "\n");
				buf.append("Size: " + box.getSize() + "\n");
				
				if(box instanceof FileTypeBox) {
					FileTypeBox fb = (FileTypeBox) box;
					buf.append("Major Brand: " +fb.getMajorBrand() + "\n");
					for(String cbrand : fb.getCompatibleBrands()) {
						buf.append("Compatible Brand: " +cbrand+ "\n");
					}
				}
			}
			return buf.toString();
		} catch (IOException e) {
			return e.getMessage();
		} finally {
			if(isoFile != null)
				try { isoFile.close(); } catch (IOException e) { }
		}
		
	}
}
