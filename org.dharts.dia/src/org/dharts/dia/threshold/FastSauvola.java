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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;

/**
 * An adaptive thresholding algorithm based on the technique 
 * described by J. Sauvola in: 
 * 
 * Sauvola, J. and M. Pietikäinen, Adaptive document image binarization. In
 *      Pattern Recognition 33 (2000) pp 255-236.
 * 
 * @author Neal Audenaert
 */
public class FastSauvola implements Thresholder 
{
	// TODO need to factor out the integral image concepts and tools from the thresholder 
	private static final int N_THREADS = 10;		// default number of threads to use internally
    
    private final ExecutorService ex;
    private int width  = 0;
    private int height = 0;
    private int imArea = 0;
    private final AtomicInteger ct = new AtomicInteger(0);
    
    private boolean enableOutput = true;
    private BufferedImage sourceImage = null;
    private BufferedImage outputImage = null;
    
    // TODO should evaluate using single dimensional arrays
    private long[][] iImg;			// integral image
    private long[][] iImgSq;		// integral image squared
    
    // -----------------------------------------------------------------------
    // PROPERTIES
    // -----------------------------------------------------------------------
    // TODO make these configuration parameters.
    private int    ts = 48;     // tile size
    private int    whalf = ts / 2;	// half the window size
    private double k  = 0.3;    //
    private int    r  = 128;    // control for dynamic range

    /** Default constructor. */
    public FastSauvola() {  
    	ex = Executors.newFixedThreadPool(N_THREADS);
    }
    
    public FastSauvola(int nThreads) {  
    	ex = Executors.newFixedThreadPool(nThreads);
    }

    @Override
	public void initialize(File file) throws IOException {
		if (!file.exists() || !file.isFile() || !file.canRead()) {
            throw new IOException("Filename does not refer to a readable image file");
        }
        
		initialize(ImageIO.read(file));
    }

    @Override
    public void initialize(BufferedImage image) {
        width  = image.getWidth();
        height = image.getHeight();
        imArea = width * height;
        
        sourceImage = ImageUtils.grayscale(image); 

        // Makes for a reasonable assumption, but this parameter really needs
        // to be configured for good results
        ts = width / 15;
    }
    
    public void setGenerateImage(boolean flag)
    {
    	this.enableOutput = flag;
    }
    
    @Override
	public BufferedImage call() throws InterruptedException, IOException
    {
    	if (!this.isReady()) 
            throw new IllegalStateException("The thresholding algorithm has not been properly initialized");
        
        Raster original = sourceImage.getData();
        computeIntegral(original);
        outputImage = performThresholding(original);
        
        return outputImage;
    }

	private final void computeIntegral(Raster data) {
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
	
	            assert rowsumImage[x][y] >= 0;
	            assert rowsumSqImage[x][y] >= 0;
	            assert integralImage[x][y] >= 0;
	            assert integralSqImage[x][y] >= 0;
	        }
	    }
	
	    iImg   = integralImage;
	    iImgSq = integralSqImage;
	}

	private BufferedImage performThresholding(Raster original) throws InterruptedException {
		WritableRaster output = null;
		if (enableOutput)
			output = sourceImage.getData().createCompatibleWritableRaster();
		
        for (int col = 0; col < width; col++) {
    		ex.execute(new ColumnProcessor(col, original, output));
        }
        
        ex.shutdown();
        ex.awaitTermination(5, TimeUnit.SECONDS);		// HACK: arbitrary delay
        if (enableOutput)
        	return new BufferedImage(sourceImage.getColorModel(), output, true, new Hashtable<>());
        else 
        	return null;
	}

	public double getMean() 
	{
		int xmax = width - 1;
		int ymax = height - 1;
		double area = width * height;
		
		return iImg[xmax][ymax] / area;
	}
	
	public final double getStandardDeviation() 
	{
		return Math.sqrt(getVariance());
	}
	
	public final double getVariance() 
	{
		int xmax = width - 1;
		int ymax = height - 1;
		
		double diff   = iImg[xmax][ymax];
		double sqdiff = iImgSq[xmax][ymax];
		
		return (sqdiff - (diff * diff) / imArea) / (imArea - 1);
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
	public double getParameter(String param) {
        if (param.equals("k")) { 
            return k;
        } else if (param.equals("ts")) {
            return ts;
        } else {
            throw new IllegalArgumentException("Unrecognized parameter: " + param);
        }
    }
    
    @Override
	public void setParameter(String param, double value) {
        
        if (param.equals("k")) { 
            if (value < 0) 
                throw new IllegalArgumentException("Invalid value for 'k' (" + value + "). Must be a positive number.");
            
            
            k = value;
            
        } else if (param.equals("ts")) {
            if (value < 0) 
                throw new IllegalArgumentException("Invalid value for 'ts' (" + value + "). Must be a positive number.");
            
            
            ts = (int) Math.round(value);
            whalf = ts / 2;
        } else {
            throw new IllegalArgumentException("Unrecognized parameter: " + param);
        }
    }
    
    @Override
	public BufferedImage getResult() {
        if (outputImage != null) 
        	return outputImage;
        else
        	throw new IllegalStateException("Execution is not complete");
    }
    
    public int getWidth() 
	{
		return width;
	}


	public int getHeight() 
	{
		return height;
	}
	
	public int getArea()
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
	
	public long[] getHorizontalProjection()
	{
		// TODO need to apply a smoothing function
		long[] result = new long[height];
		int x = width = 1;
		long[] temp = iImg[x];
		result[0] = iImg[x][0];
		for (int y = 1; x < width; x++)
		{
			result[y] = temp[y] - temp[y - 1];
		}
		
		return result;
	}
	
	public int getForegroundPixelCount() 
	{
		return ct.get();
	}
	
	public int getBackgroundPixelCount() 
	{
		return imArea - ct.get();
	}
	

	@Override
	public boolean isReady() {
        return sourceImage != null;
    }

	private final class ColumnProcessor implements Runnable {
		private final WritableRaster output;
		private final Raster original;
		private int i;

		private ColumnProcessor(int colIx, Raster original, WritableRaster output) {
			this.i = colIx;
			this.output = output;
			this.original = original;
		}

		@Override
		public void run() {
			double diagsum, idiagsum, diff, sqdiagsum, sqidiagsum, sqdiff, area;
			double mean, stddev;
			int xmin, ymin, xmax, ymax;
			
			double threshold;
			boolean isBackground;
			
			for (int j = 0; j < height; j++) {
				xmin = Math.max(0, i - whalf);
				ymin = Math.max(0, j - whalf);
				xmax = Math.min(width - 1, i + whalf);
				ymax = Math.min(height - 1, j + whalf);
			       
				area = (xmax - xmin + 1) * (ymax - ymin + 1);
				
				if ((xmin == 0) && (ymin == 0)) {               // Point at origin
				    diff   = iImg[xmax][ymax];
				    sqdiff = iImgSq[xmax][ymax];
				    
				} else if ((xmin == 0) && (ymin != 0)) {        // first column
				    diff   = iImg[xmax][ymax] - iImg[xmax][ymin - 1];
				    sqdiff = iImgSq[xmax][ymax] - iImgSq[xmax][ymin - 1];
				    
				} else if ((xmin != 0) && (ymin == 0)) {        // first row
				    diff   = iImg[xmax][ymax] - iImg[xmin - 1][ymax];
				    sqdiff = iImgSq[xmax][ymax] - iImgSq[xmin - 1][ymax];
				    
				} else {                                        // rest of the image
				    diagsum    = iImg[xmax][ymax] + iImg[xmin - 1][ymin - 1];
				    idiagsum   = iImg[xmax][ymin - 1] + iImg[xmin - 1][ymax];
				    diff       = diagsum - idiagsum;
				    
				    sqdiagsum  = iImgSq[xmax][ymax] + iImgSq[xmin - 1][ymin - 1];
				    sqidiagsum = iImgSq[xmax][ymin - 1] + iImgSq[xmin - 1][ymax];
				    sqdiff     = sqdiagsum - sqidiagsum;
				}

				mean = diff / area;
				stddev = Math.sqrt((sqdiff - (diff * diff) / area) / (area - 1));
				
				threshold = mean * (1 + k * ((stddev / r) - 1));
				isBackground = original.getSample(i, j, 0) > threshold;
				if (output != null)
					output.setSample(i, j, 0, isBackground ? 255 : 0);
				
				if (!isBackground)
					ct.incrementAndGet();
			}
		}
	}
}
