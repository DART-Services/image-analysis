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

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 */
public class IntegralImage
{
    private int width  = 0;
    private int height = 0;
    private int imArea = 0;
    
    private long[][] iImg;			// integral image
    private long[][] iImgSq;		// integral image squared

    /** Default constructor. */
    public IntegralImage() {  
    }
    
	public void initialize(File file) throws IOException 
	{
		if (!file.exists() || !file.isFile() || !file.canRead()) 
            throw new IOException("Filename does not refer to a readable image file");
        
		initialize(ImageIO.read(file));
    }

    public void initialize(BufferedImage image) 
    {
        width  = image.getWidth();
        height = image.getHeight();
        imArea = width * height;
        
        ColorConvertOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
    	image = op.filter(image, null);
    	
    	computeIntegral(image.getData());
    }

	private final void computeIntegral(Raster data) 
	{
	    // compute the integral of the image
	    int[][] rowsumImage = new int[width][height];
	    int[][] rowsumSqImage = new int[width][height];
	    long[][] integralImage = new long[width][height];
	    long[][] integralSqImage = new long[width][height];
	
	    for (int y = 0; y < height; y++) {
	        int s = data.getSample(0, y, 0);
	        rowsumImage[0][y] = s;
	        rowsumSqImage[0][y] = s * s;
	    }
	
	    for (int x = 1; x < width; x++) {
	        for (int y = 0; y < height; y++) {
	            int s = data.getSample(x, y, 0);
	
	            rowsumImage[x][y] = rowsumImage[x - 1][y] + s;
	            rowsumSqImage[x][y] = rowsumSqImage[x - 1][y] + s * s;
	        }
	    }
	
	    for (int x = 0; x < width; x++) {
	    	integralImage[x][0] = rowsumImage[x][0];
	    	integralSqImage[x][0] = rowsumSqImage[x][0];
	    	
	        for (int y = 1; y < height; y++) {
	            integralImage[x][y] = integralImage[x][y - 1] + rowsumImage[x][y];
	            integralSqImage[x][y] = integralSqImage[x][y - 1] + rowsumSqImage[x][y];
	
	            // rough check for overflow errors
	            assert rowsumImage[x][y] >= 0;
	            assert rowsumSqImage[x][y] >= 0;
	            assert integralImage[x][y] >= 0;
	            assert integralSqImage[x][y] >= 0;
	        }
	    }
	
	    iImg   = integralImage;
	    iImgSq = integralSqImage;
	}

    public final int getWidth() 
	{
		return width;
	}

	public final int getHeight() 
	{
		return height;
	}
	
	public final int getArea()
	{
		return imArea;
	}
	
	public long[] getVerticalProjection()
	{
		long[] result = new long[width];
		
		int w = 10;				// size of soothing window;
		int mid = w / 10; 		// midpoint of smoothing window
		int y = height = 1;

		int x2 = width - w - 1;
		// NOTE, I'm really interested in the derivative, not in the raw projection
		for (int x = 0; x < mid; x++)
		{
			result[x] = (iImg[w][y] - iImg[0][y]) / w;  // NOTE: this truncates rather than rounds
			result[x2] = (iImg[x2][y] - iImg[width - 1][y]) / w;  
		}
		
		result[0] = iImg[0][y];
		for (int i = 0; i < width - w; i++)
		{
			int x = i + mid;
			result[x] = (iImg[i + w][y] - iImg[i][y]) / w;
		}
		
		return result;
	}
	
	public long[] getHorizontalProjection(int window)
	{
		// TODO need to apply a smoothing function
		long[] result = new long[height];
		int x = width - 1;
		long[] temp = iImg[x];
		result[0] = iImg[x][0];
		for (int y = 1; x < width; x++)
		{
			result[y] = temp[y] - temp[y - 1];
		}
		
		return result;
	}
	
	/**
	 * 
	 * @param y
	 * @param window The size of the smoothing window to use (in pixels). Must be greater
	 * 		than zero and smaller than the height of the image.
	 * @return The 
	 */
	public final long getHorizontalProjection(int y, int window)
	{
		int x = width - 1;
		int miny = y - window;
		miny = miny > 0 ? miny : 0;
		
		int maxy = miny + window;
		if (maxy >= height) 
		{
			maxy = height - 1;
			miny = maxy - window;
		}
		
		// TODO could move the scale factor outside of the implicit loop
		return (iImg[x][maxy] - iImg[x][miny]) / (window * width);  
	}
	
	/**
	 * Returns the mean and variance of the selected image region.
	 * 
	 * @param xmin the min x value of the image region
	 * @param ymin the min y value of the image region
	 * @param xmax the max x value of the image region
	 * @param ymax the max y value of the image region
	 * 
	 * @return A two-element array where the first element is the mean value of the pixels in this region
	 * 		and the second is the variance of the pixel values in this region 
	 */
	public final double[] getGausModel(int xmin, int ymin, int xmax, int ymax) {
		double diff;
		double sqdiff;
		double mean, var;

		if ((xmin == 0) && (ymin == 0)) {               // Point at origin
		    diff = iImg[xmax][ymax];
		    sqdiff = iImgSq[xmax][ymax];
		    
		} else if ((xmin == 0) && (ymin != 0)) {        // first column
		    diff = iImg[xmax][ymax] - iImg[xmax][ymin - 1];
		    sqdiff = iImgSq[xmax][ymax] - iImgSq[xmax][ymin - 1];
		    
		} else if ((xmin != 0) && (ymin == 0)) {        // first row
		    diff = iImg[xmax][ymax] - iImg[xmin - 1][ymax];
		    sqdiff = iImgSq[xmax][ymax] - iImgSq[xmin - 1][ymax];
		    
		} else {                                        // rest of the image
		    double diagsum = iImg[xmax][ymax] + iImg[xmin - 1][ymin - 1];
		    double idiagsum = iImg[xmax][ymin - 1] + iImg[xmin - 1][ymax];
		    diff = diagsum - idiagsum;
		    
		    double sqdiagsum  = iImgSq[xmax][ymax] + iImgSq[xmin - 1][ymin - 1];
		    double sqidiagsum = iImgSq[xmax][ymin - 1] + iImgSq[xmin - 1][ymax];
		    sqdiff = sqdiagsum - sqidiagsum;
		}

		double area = (xmax - xmin + 1) * (ymax - ymin + 1);
		mean = diff / area;
		var = (sqdiff - (diff * diff) / area) / (area - 1);
		
		return new double[] { mean, var };
	}
}
