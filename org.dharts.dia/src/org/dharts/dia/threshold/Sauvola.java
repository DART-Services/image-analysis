/* Created on       May 28, 2010
 * Last Modified on $Date: $
 * $Revision: $
 * $Log: $
 *
 * Copyright Institute for Digital Christian Heritage (IDCH),
 *           Neal Audenaert
 *
 * ALL RIGHTS RESERVED. 
 */
package org.dharts.dia.threshold;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.imageio.ImageIO;

import org.dharts.dia.BadParameterException;

/**
 * An adaptive thresholding algorithm based on the technique described by J. Sauvola in: 
 * 
 * Sauvola, J. and M. Pietikäinen, Adaptive document image binarization. In
 *      Pattern Recognition 33 (2000) pp 255-236.
 * 
 * @author Neal Audenaert
 */
public class Sauvola implements Thresholder {
    
    // -----------------------------------------------------------------------
    // PROPERTIES
    // -----------------------------------------------------------------------
    private boolean m_initialized = false;
    private boolean m_processed   = false;
    
    private int m_width  = 0;
    private int m_height = 0;
    
    // TODO make these configuration parameters.
    private int    ts = 48;     // tile size
    private double k  = 0.5;    //
    private int    r  = 128;    // control for dynamic range

    private BufferedImage m_image  = null;
    private BufferedImage m_output = null;
    
    // -----------------------------------------------------------------------
    // CONSTRUCTOR
    // -----------------------------------------------------------------------
    /** Default constructor. */
    public Sauvola() {  }
    

    // -----------------------------------------------------------------------
    // INITIALIZATION METHODS
    // -----------------------------------------------------------------------
    @Override
    public void initialize(File file) throws IOException {
        if (!file.exists() || !file.isFile() || !file.canRead()) {
            throw new IOException("Filename does not refer to a readable " +
            		"image file");
        }
        
        initialize(ImageIO.read(file));
    }

    @Override
    public void initialize(BufferedImage image) throws IOException {
        m_width  = image.getWidth();
        m_height = image.getHeight();
        
        m_image = ImageUtils.grayscale(image);
        
        // Makes for a reasonable assumption, but this parameter really needs
        // to be configured for good results
        ts = m_width / 15;
        
        m_initialized = true;
        m_processed   = false;

//        BufferedImage im = new BufferedImage(m_width, m_height, BufferedImage.TYPE_BYTE_BINARY);
//        
//        return im;
    }

    
    // -----------------------------------------------------------------------
    // SAUVOLA ALGORITHM IMPLEMENTATION
    // -----------------------------------------------------------------------
    
    // NOTE: This partially implements the complete Sauvola binarization
    //     algorithm. The original algorithm applies two different thresholding 
    //     criteria, one for text based tiles (the text based method, or tbm) 
    //     and a different one for background or graphical tiles (the standard 
    //     discriminant method, or sdm). A separate method is used to determine
    //     which category an individual tile belongs to. 
    //
    //     We have implemented fully the tbm algorithm and have implemented
    //     stubs for tile classification and for the sdm method. We have yet
    //     to see an implementation that incorporates the full algorithm and 
    //     the details in the original paper are unclear to us at this time. 
    //            
    
    @Override
	public BufferedImage call() throws IOException {
        // TODO Sauvola's algorithm interpolates between the nearest anchor
        //      points to get thresholds on a pixel by pixel basis. We should
        //      adopt this approach. Now, for simplicy, we're just using the 
        //      anchor point to compute the threshold for all pixels in a tile.
        
        if (!this.isReady()) {
            throw new IllegalStateException("The thresholding algorithm has not " +
            		"been properly initialized");
        }
        
        WritableRaster output = m_image.getData().createCompatibleWritableRaster();
        TileIterator tiles = new TileIterator(m_image, ts);
        while (tiles.hasNext()) {
            tiles.next();
            
            // deterine the threshold
            int t = isText(tiles) ? tbm(tiles) : sdm(tiles);
            
            // Write the tile to the output image
            Raster data = tiles.getRaster();
            int w    = data.getMinX() + tiles.getWidth();
            int h    = data.getMinY() + tiles.getHeight();

            for (int y = data.getMinY(); y < h; y++) {
                for (int x = data.getMinX(); x < w; x++) {
                    int s = data.getSample(x, y, 0);
                    if (s > t) 
                    	output.setSample(x, y, 0, 255);
                    else 
                    	output.setSample(x, y, 0, 0);
                }
            }
        }
        
        m_output = new BufferedImage(m_image.getColorModel(), output, true, new Hashtable<>());
        m_processed = true;
        
        return m_output;
    }
    
    /**
     * Determines whether a given image tile is text. 
     * 
     * @param tiles The <code>TileIterator</code> whose state represents the 
     *      tile to evaluate. 
     * @return
     */
    private boolean isText(TileIterator tiles) {
        // TODO For now, this is hardwired to return true. We need to implement 
        //     this discriminant function as described in Sauvola's paper or 
        //     remove this method. This produces poor results for non-text 
        //     image regions.
        return true;
    }
    
    /**
     * Compute the threshold for a tile under the assumption that the tile
     * contains text. This implements the following equation:
     *   
     * <code>
     *    T(x,y) = mu(x,y) * (1 + k * (std(x,y)/R - 1)))
     * </code>
     * @param tile
     * @return
     */
    private int tbm(TileIterator tile) {
        double mu = tile.mean()[0];
        double std = tile.stdev()[0];
        
        return (int) (mu * (1 + k * (std/r - 1)));
    }
    
    /**
     * Compute the threshold for a tile under the assumption that the tile
     * contains does not contain text.
     *  
     * @param tile The tile from the original image for which to determine 
     *      the appropriate threshold. This should be a tile for which 
     *      <code>isText</code> returns false. 
     * @return The threshold value for the specified tile.
     */
    private int sdm(TileIterator tile) {
        // This is currently fixed to return 0 in all cases, as we have not yet
        // implemented this portion of the algorithm.
        return 255;
    }
    
    // -----------------------------------------------------------------------
    // ACCESSOR METHODS
    // -----------------------------------------------------------------------
    @Override
    public Map<String, String> listParamters() {
        // NOTE this isn't particularly good form, but I'm working on figuring
        //      out how I want to handle parameters.
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("k", "Takes positive numbers. This is a weighting factor " +
        		"that adjusts how sensitve the algorithm is to the standard " +
        		"deviation. Lower valus will result in more pixels being " +
        		"identified as foreground, raising this value will result in " +
        		"more pixels being identified as background. The default " +
        		"value 0.5 following the recommendation of the original " +
        		"Sauvola paper.");
        
        params.put("R", "R is the dynamic of the standard deviation. By " +
        		"default, this value is 128 (assuming that the image ranges " +
        		"through all 256 gray levels). For images with less " +
        		"variation, this number may need to be adjusted. Reducing " +
        		"this value will make the algorithm more sensitive to the " +
        		"local variation.");
        
        params.put("ts", "Tile Size. This is the size of the tiles the Sauvola" +
        		"algorithm will use to evaluate local features. This should " +
        		"be set to a value that is approximately three characters " +
        		"wide. That is, it should be large enough to capture the " +
        		"variation between the text and background, but small enough" +
        		"that the background (including any damage to the text) is " +
        		"relatively uniform. By default, this is set to w / 15, " +
        		"where 'w' is the image width.");
        
        return params;
    }

    @Override
    public double getParameter(String param) throws BadParameterException {
        if (param.equals("k")) { 
            return k;
        } else if (param.equals("R")) {
            return r;
        } else if (param.equals("ts")) {
            return ts;
        } else {
            throw new BadParameterException("Unrecognized parameter: " + param);
        }
    }
    
    @Override
    public void setParameter(String param, double value) 
            throws BadParameterException  {
        
        if (param.equals("k")) { 
            if (value < 0) {
                throw new BadParameterException(
                        "Invalid value for 'k' (" + value + "). " +
                        "Must be a positive number.");
            }
            
            k = value;
        } else if (param.equals("R")) {
            if ((value < 0) || (value > 128)) {
                throw new BadParameterException(
                        "Invalid value for 'R' (" + value + "). " +
                        "Must be between 0 and 128 (.");
            }
            
            r = (int) Math.round(value);
        } else if (param.equals("ts")) {
            if (value < 0) {
                throw new BadParameterException(
                        "Invalid value for 'ts' (" + value + "). " +
                        "Must be a positive number.");
            }
            
            ts = (int) Math.round(value);
        } else {
            throw new BadParameterException("Unrecognized parameter: " + param);
        }
    }
    
    @Override
    public BufferedImage getResult() {
        if (m_processed)
        	return m_output;
        else
        	throw new IllegalStateException("Execution has not completed");
    }
    
    @Override
    public boolean isReady() {
        return m_initialized;
    }
}
