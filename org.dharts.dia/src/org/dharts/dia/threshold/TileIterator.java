/* Created on       May 28, 2010
 * Last Modified on $Date: $
 * $Revision: $
 * $Log: $
 *
 * Copyright Institute for Digital Christian Heritage,
 *           Neal Audenaert
 *
 * ALL RIGHTS RESERVED. 
 */
package org.dharts.dia.threshold;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Iterates over an image through a set of non-overlapping rectangular tiles.
 * 
 * @author Neal Audenaert
 */
public class TileIterator implements Iterator<Raster> {
//    private static final Logger logger = Logger.getLogger("org.dharts.dia.loggers");
    
    /** The source image from which this tile is taken. */
    private final BufferedImage m_source;
    
    /** The width of this tile. */
    private int m_width;
    
    /** The height of this tile (in pixels). */
    private int m_height; 
    
    private final int m_imgWidth;  /** The width of the source image. */
    private final int m_imgHeight; /** The height of the source image. */
    private final int m_ctXTiles;  /** Number of tiles horizontally. */
    private final int m_ctYTiles;  /** Number of tiles vertically. */
    private final int m_ctTiles;   /** Number of tiles in the image. */
    private final int m_ctBands;
    
    private int ix = -1;           /** Index of the current tile. */
    private Raster m_tile = null;  /** The current tile data. */
    
    private final double[] m_mean;
    private final double[] m_std;
    
    /**
     * Creates a new <code>TileIterator</code> with square tiles.
     * 
     * @param source The image to be iterated over.
     * @param w The width and height of the tiles to use.
     */
    public TileIterator(BufferedImage source, int w) 
        throws IOException {
        this(source, w, w);
    }
    
    /**
     * Creates a new <code>TileIterator</code>.
     * 
     * @param source The image to be iterated over.
     * @param w The width of the tiles to use.
     * @param h The height of the tiles to use.
     */
    public TileIterator(BufferedImage source, int w, int h) 
        throws IOException {
        
        // error messages
        String errInit  = "Could not initialize the TileIterator from " +
        		"the provided source image: ";
        try {
            m_source = source;
        } catch (Exception ex) {
            throw new IOException(errInit + ex.getLocalizedMessage());
        }
        
        m_ctBands = m_source.getColorModel().getNumComponents();
        
        try {
            m_width  = w;
            m_height = h;
            
            m_imgWidth  = source.getWidth();
            m_imgHeight = source.getHeight();
            
            m_ctXTiles = m_source.getNumXTiles();
            m_ctYTiles = m_source.getNumYTiles();
            m_ctTiles  = m_ctXTiles * m_ctYTiles;
            
            m_mean = new double[m_ctBands];
            m_std = new double[m_ctBands];
        } catch (Exception ex) {
            throw new IOException(errInit + ex.getLocalizedMessage());
        }
    }
    
    /** 
     * Returns the source image this tile belongs to. 
     * 
     * @return the source image this tile belongs to.
     */
    public RenderedImage getSourceImage() { 
        return m_source;
    }
    
    /** 
     * Returns the current tile as a <code>Raster</code>. This is the same 
     * <code>Raster</code> that was returned by the <code>next</code> method.
     * 
     * @return
     */
    public Raster getRaster() {
        return m_tile;
    }
    
    /** 
     * Returns the current tile as a <code>RenderedImage</code>. 
     *  
     * @return
     */
    public RenderedImage getImage() {
        int x = (ix % m_ctXTiles) * this.m_width;
        int y = ix / m_ctXTiles * this.m_height;
        
        return m_source.getSubimage(x, y, getWidth(), getHeight());
    }
    
    /** 
     * Returns the x coordinate of the current tile relative to the 
     * source image.
     * 
     * @return the x coordinate of the current tile relative to the 
     *      source image.
     */
    public int getX() {
        return ix % m_ctXTiles;
    }
    
    /** 
     * Returns the y coordinate of the current tile relative to the 
     * source image.
     * 
     * @return the y coordinate of the current tile relative to the 
     *      source image.
     */
    public int getY() {
        return ix / m_ctXTiles;
    }
    
    /**
     * Returns the number of tiles in the horizonal direction.
     * 
     * @return the number of tiles in the horizonal direction
     */
    public int getNumXTiles() { 
        return m_ctXTiles;
    }

    /**
     * Returns the number of tiles in the vertical direction.
     * 
     * @return the number of tiles in the vertical direction
     */
    public int getNumYTiles() { 
        return m_ctYTiles;
    }
    
    /**
     * Returns the width of this tile in pixels. This adjusts for the fact that
     * tiles at the rightmost edge of the image may be more narrow that other 
     * tiles.
     * 
     * @return The width of this tile in pixels.
     */
    public int getWidth() {
        if (ix < 0) 
            throw new NoSuchElementException();
        
        return ((m_tile.getMinX() + m_width) <= m_imgWidth) 
               ? m_width 
               : (m_imgWidth - m_tile.getMinX());
    }
    
    /**
     * Returns the height of this tile in pixels. This adjusts for the fact that
     * tiles at the bottom edge of the image may be shorter that other tiles. 
     * 
     * @return The height of this tile in pixels.
     */
    public int getHeight() {
        if (ix < 0) 
            throw new NoSuchElementException();
        
        return ((m_tile.getMinY() + m_height) <= m_imgHeight) 
               ? m_height
               : (m_imgHeight - m_tile.getMinY());
    }
    
    /** 
     * Returns the size of this tile in terms of the total number of pixels.
     * 
     * @return The size of this tile in terms of the total number of pixels.
     */
    public int getSize() {
        return this.getWidth() * this.getHeight();
    }
    
    /** 
     * Computes the mean value for this tile. Note that this assumes a single
     *      banded (typically grayscale) image. 
     * 
     * @return The mean value for this tile.
     */
    public double[] mean() {
        if (!Double.isNaN(m_mean[0])) return m_mean; 
        if (ix < 0) 
            throw new NoSuchElementException();
        
        // compute actual width and height of this raster in the original image
        int w = m_tile.getMinX() + getWidth();
        int h = m_tile.getMinY() + getHeight();
        
        // compute mean
        int[] acc = new int[m_ctBands];
        for (int x = m_tile.getMinX(); x < w; x++) {
            for (int y = m_tile.getMinY(); y < h; y++) {
                for (int b = 0; b < m_ctBands; b++) {
                    acc[b] += m_tile.getSample(x, y, b);
                }
            }
        }
        
        double sz = this.getSize();
        for (int b = 0; b < m_ctBands; b++) {
            m_mean[b] = acc[b] / sz;
        }
        
        return m_mean;
    }
    
    /** 
     * Computes the standard deviation of the pixels in this tile. Note that 
     * this assumes a single banded (typically grayscale) image. 
     * 
     * @return The mean value for this tile.
     */
    public double[] stdev() {
        if (!Double.isNaN(m_std[0])) return m_std;
        if (ix < 0)
            throw new NoSuchElementException();
        
        if (Double.isNaN(m_mean[0])) { 
            this.mean();
        }
        
        int w = m_tile.getMinX() + m_width;
        if (w > m_imgWidth) w = m_imgWidth;
        
        int h = m_tile.getMinY() + m_height;
        if (h > m_imgHeight) h = m_imgHeight;
        
        // compute mean
        int sample = 0;
        int[] var = new int[m_ctBands];
        for (int x = m_tile.getMinX(); x < w; x++) {
            for (int y = m_tile.getMinY(); y < h; y++) {
                for (int b = 0; b < m_ctBands; b++) {
                    sample = m_tile.getSample(x, y, b);
                    var[b] += (m_mean[b] - sample) * (m_mean[b] - sample); 
                }
            }
        }
        
        double sz = this.getSize();
        for (int b = 0; b < m_ctBands; b++) {
            m_std[b] = Math.sqrt(var[b] / sz);
        }
        
        return m_std;
    }
    
    /** @inheritDoc */
    @Override
	public boolean hasNext() {
        return ix < (m_ctTiles - 1);
    }

    /**
     * Returns the next tile in the underlying image. 
     */
    @Override
	public Raster next() {
        if (ix >= m_ctTiles) 
            throw new NoSuchElementException();
        
        ix++;
        int x = ix % m_ctXTiles;
        int y = ix / m_ctXTiles;
        
        assert x < m_ctXTiles : "Constraint failed. Bad x value.";
        assert y < m_ctYTiles : "Constraint failed. Bad y value.";
        
        // reset the mean and std
        for (int b = 0; b < m_ctBands; b++) {
            m_mean[b] = Double.NaN;
            m_std[b]  = Double.NaN;
        }
        
        m_tile = m_source.getTile(x, y);
        
        
        return m_tile;
    }
    
    /**
     * Unsupported operation.
     */
    @Override
	public void remove() {
        throw new UnsupportedOperationException(
                "Cannot remove a tile from an image.");
    }
    
    // -----------------------------------------------------------------------
    // DELETE ME - MAIN PROGRAM TO ALLOW ME TO TEST THE ITERATOR
    // -----------------------------------------------------------------------
//    /** 
//     * Loads an image file from the commandline inputs. 
//     * @param args
//     * @return
//     */
//    private static File loadImageFile(String[] args) throws IOException {
//        // TODO need to configure this to use real CLI parsing. For now its 
//        //      just nice stub code that does one thing poorly and in a very 
//        //      brittle manner.
//        
//        File f = null;
//        String fname = null;
//        if (args.length > 0) {
//            fname = args[0];
////            LogService.info("Reading file: " + fname, logger);
//            f = new File(fname);
//        } else { 
//            System.err.println("No filename provided");
//            System.err.println("Usage: java org.idch.dia.thresholding.TileIterator <filename>");
//            throw new IOException("No filename provided.");
//        }
//        
//        if (!f.exists()) {
//            System.err.println("The specified file (" + f.getAbsolutePath() + ") does not exist.");
//            throw new IOException("The specified file (" + f.getAbsolutePath() + ") does not exist.");
//        } else if (!f.isFile() || !f.canRead()) {
//            System.err.println("The supplied pathname must be to a readable file: " + f.getAbsolutePath());
//            throw new IOException("The supplied pathname must be to a readable file: " + f.getAbsolutePath());
//        }
//     
//        return f;
//    }
    
//    public static void main(String[] args) {
//        try {
//            long start = System.currentTimeMillis();
//
//            File f = loadImageFile(args);
//            BufferedImage img = ImageIO.read(f);
//            TileIterator itt = new TileIterator(img, 256, 256);
//            if (itt.hasNext()) {
//                itt.next();
//                
//                RenderedImage im = itt.getImage();
//                String fmt = ImageUtils.JPEG;
//                ImageIO.write(im, fmt, new File(f.getParentFile(), "tile." + fmt));
//            }
//            System.out.println("Done.");
//            
//            long end = System.currentTimeMillis();
//            System.out.println("done. Time Elapsed: " + (end - start) + " ms");
//        } catch (Exception ex) {
//            System.err.println(ex.getMessage());
//            ex.printStackTrace();
//        }
//    }
}