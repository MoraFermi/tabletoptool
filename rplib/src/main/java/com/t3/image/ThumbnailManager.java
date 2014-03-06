/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package com.t3.image;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.t3.FileUtil;
import com.t3.MD5Key;
import com.t3.swing.SwingUtil;

public class ThumbnailManager {

    private File thumbnailLocation;
    private Dimension thumbnailSize;
    
    public ThumbnailManager(File thumbnailLocation, Dimension thumbnailSize) {
        this.thumbnailLocation = thumbnailLocation;
        this.thumbnailSize = thumbnailSize;
    }
    
    public Image getThumbnail(File file) throws IOException {

        // Cache
        BufferedImage thumbnail = getCachedThumbnail(file);
        if (thumbnail != null) {
            return thumbnail;
        }
        
        // Create
        return createThumbnail(file);
    }

    private Image createThumbnail(File file) throws IOException {

        // Gather info
        File thumbnailFile = getThumbnailFile(file);
        if (thumbnailFile.exists()) {
        	return ImageUtil.getImage(thumbnailFile);
        }
        
        Image image = ImageUtil.getImage(file);

        // Should we bother making a thumbnail ?
        if (file.length() < 30 * 1024) {
            return image;
        }

        // Transform the image
        Dimension imgSize = new Dimension(image.getWidth(null), image.getHeight(null));
        SwingUtil.constrainTo(imgSize, thumbnailSize.width, thumbnailSize.height);
        
        BufferedImage thumbnailImage = new BufferedImage(imgSize.width, imgSize.height, ImageUtil.pickBestTransparency(image));

        Graphics2D g = thumbnailImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(image, 0, 0, imgSize.width, imgSize.height, null);
        g.dispose();
        
        // Use png to preserve transparency
        FileUtil.writeBytes(thumbnailFile, ImageUtil.imageToBytes(thumbnailImage, "png"));

        return thumbnailImage;
    }
    
    private BufferedImage getCachedThumbnail(File file) {
        
        File thumbnailFile = getThumbnailFile(file);
        
        if (!thumbnailFile.exists()) {
            return null;
        }
        
        try {
            // Check that it hasn't changed on disk
            if (file.lastModified() > thumbnailFile.lastModified()) {
                return null;
            }
            
            // Get the thumbnail
            BufferedImage thumbnail = ImageIO.read(thumbnailFile);

            // Check that we have the size we want
            if (thumbnail.getWidth() != thumbnailSize.width && thumbnail.getHeight() != thumbnailSize.height) {
                return null;
            }
            
            return thumbnail;
        } catch (IOException ioe) {
            return null;
        }
    }
    
    private File getThumbnailFile(File file) {
        
        MD5Key key = new MD5Key(file.getAbsolutePath().getBytes());
        
        return new File(thumbnailLocation.getPath() + "/" + key);
    }
}
