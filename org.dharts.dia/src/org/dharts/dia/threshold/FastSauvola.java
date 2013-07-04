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
public class FastSauvola implements Thresholder {
    
    private static final int N_THREADS = 10;
	// -----------------------------------------------------------------------
    // PROPERTIES
    // -----------------------------------------------------------------------
    private boolean initialized = false;
    private boolean processed   = false;
    
    private int m_width  = 0;
    private int m_height = 0;
    
    // TODO make these configuration parameters.
    private int    ts = 48;     // tile size
    private int    whalf = ts / 2;	// half the window size
    private double k  = 0.3;    //
    private int    r  = 128;    // control for dynamic range

    private BufferedImage sourceImage  = null;
    private BufferedImage outputImage = null;
    
    private long[][] m_iImg;
    private long[][] m_iImgSq;
	private final ExecutorService ex;
	private File sourceFile;
    
    // -----------------------------------------------------------------------
    // CONSTRUCTOR
    // -----------------------------------------------------------------------
    /** Default constructor. */
    public FastSauvola() {  
    	ex = Executors.newFixedThreadPool(N_THREADS);
    }
    
    public FastSauvola(int nThreads) {  
    	ex = Executors.newFixedThreadPool(nThreads);
    }

    // -----------------------------------------------------------------------
    // INITIALIZATION METHODS
    // -----------------------------------------------------------------------
    @Override
	public void initialize(File file) throws IOException {
		if (!file.exists() || !file.isFile() || !file.canRead()) {
            throw new IOException("Filename does not refer to a readable image file");
        }
        
		this.sourceFile = file;
    }

    @Override
    public void initialize(BufferedImage image) {
        m_width  = image.getWidth();
        m_height = image.getHeight();
        
        sourceImage = ImageUtils.grayscale(image); 

        // Makes for a reasonable assumption, but this parameter really needs
        // to be configured for good results
        ts = m_width / 15;

        // initialize the output image
        initialized = true;
        processed   = false;
    }
    
    // -----------------------------------------------------------------------
    // SAUVOLA ALGORITHM IMPLEMENTATION
    // -----------------------------------------------------------------------
    
    @Override
	public BufferedImage call() throws InterruptedException, IOException
    {
    	long start = System.currentTimeMillis();
    	if (sourceImage == null && sourceFile != null)
    		initialize(ImageIO.read(sourceFile));
    	
        if (!this.isReady()) 
            throw new IllegalStateException("The thresholding algorithm has not been properly initialized");
        
        Raster original = sourceImage.getData();
        
        computeIntegral(original);
        BufferedImage result = performThresholding(original);
        long end = System.currentTimeMillis();
        
        System.out.println("Processing Time: " + (end - start));
        return result;
    }

	// -----------------------------------------------------------------------
	// SAUVOLA ALGORITHM IMPLEMENTATION
	// -----------------------------------------------------------------------
	
	private final void computeIntegral(Raster data) {
	    // compute the integral of the image
	    int[][] rowsumImage      = new int[m_width][m_height];
	    int[][] rowsumSqImage    = new int[m_width][m_height];
	    long[][] integralImage   = new long[m_width][m_height];
	    long[][] integralSqImage = new long[m_width][m_height];
	
	    for (int j = 0; j < m_height; j++) {
	        int s = data.getSample(0, j, 0); // m_image.get.getSample(0, j, 0);
	        rowsumImage[0][j]   = s;
	        rowsumSqImage[0][j] = s * s;
	    }
	
	    for (int i = 1; i < m_width; i++) {
	        for(int j = 0; j < m_height; j++) {
	            int s = data.getSample(i, j, 0);
	
	            rowsumImage[i][j]   = rowsumImage[i - 1][j] + s;
	            rowsumSqImage[i][j] = rowsumSqImage[i - 1][j] + s * s;
	        }
	    }
	
	    for (int i = 0; i < m_width; i++) {
	        integralImage[i][0]   = rowsumImage[i][0];
	        integralSqImage[i][0] = rowsumSqImage[i][0];
	    }
	
	    for (int i = 0; i < m_width; i++) {
	        for (int j = 1; j < m_height; j++) {
	            integralImage[i][j] = integralImage[i][j - 1] + rowsumImage[i][j];
	            integralSqImage[i][j] = integralSqImage[i][j - 1] + rowsumSqImage[i][j];
	
	            assert rowsumImage[i][j] >= 0;
	            assert rowsumSqImage[i][j] >= 0;
	            assert integralImage[i][j] >= 0;
	            assert integralSqImage[i][j] >= 0;
	        }
	    }
	
	    m_iImg   = integralImage;
	    m_iImgSq = integralSqImage;
	}

	private BufferedImage performThresholding(Raster original)
			throws InterruptedException {
		WritableRaster output = sourceImage.getData().createCompatibleWritableRaster();
        // threshold the image
        for (int i = 0; i < m_width; i++) {
    		ex.execute(new ColumnProcessor(output, original, i));
        }
        
        ex.shutdown();
        try {
			ex.awaitTermination(5, TimeUnit.SECONDS);
			outputImage = new BufferedImage(sourceImage.getColorModel(), output, true, new Hashtable<>());
			return outputImage;
		} catch (InterruptedException e) {
			throw e;
			
		} finally {
			processed = true;
		}
	}

	public final RegionDistribution computeDistribution(int xmin, int ymin, int xmax, int ymax) {
		double diagsum, idiagsum, diff, sqdiagsum, sqidiagsum, sqdiff, area;
       
		area = (xmax - xmin + 1) * (ymax - ymin + 1);
		
		assert area > 0 : "Area should be positive.";
		
		if ((xmin == 0) && (ymin == 0)) {               // Point at origin
		    diff   = m_iImg[xmax][ymax];
		    sqdiff = m_iImgSq[xmax][ymax];
		    
		} else if ((xmin == 0) && (ymin != 0)) {        // first column
		    diff   = m_iImg[xmax][ymax] - m_iImg[xmax][ymin - 1];
		    sqdiff = m_iImgSq[xmax][ymax] - m_iImgSq[xmax][ymin - 1];
		    
		} else if ((xmin != 0) && (ymin == 0)) {        // first row
		    diff   = m_iImg[xmax][ymax] - m_iImg[xmin - 1][ymax];
		    sqdiff = m_iImgSq[xmax][ymax] - m_iImgSq[xmin - 1][ymax];
		    
		} else {                                        // rest of the image
		    diagsum    = m_iImg[xmax][ymax] + m_iImg[xmin - 1][ymin - 1];
		    idiagsum   = m_iImg[xmax][ymin - 1] + m_iImg[xmin - 1][ymax];
		    diff       = diagsum - idiagsum;
		    
		    sqdiagsum  = m_iImgSq[xmax][ymax] + m_iImgSq[xmin - 1][ymin - 1];
		    sqidiagsum = m_iImgSq[xmax][ymin - 1] + m_iImgSq[xmin - 1][ymax];
		    sqdiff     = sqdiagsum - sqidiagsum;
		}

		RegionDistribution result = new RegionDistribution();
		result.mean = diff / area;
		result.stddev = Math.sqrt((sqdiff - (diff * diff) / area) / (area - 1));
		
		return result;
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
        if (this.isDone()) 
        	return outputImage;
        else
        	throw new IllegalStateException("Execution is not complete");
    }
    
    public int getWidth() 
	{
		return m_width;
	}


	public int getHeight() 
	{
		return m_height;
	}


	@Override
	public boolean isReady() {
        return initialized;
    }

    @Override
	public boolean isDone() {
        return processed;
    }

	private final class ColumnProcessor implements Runnable {
		private final WritableRaster output;
		private final Raster original;
		private int i;

		private ColumnProcessor(WritableRaster output, Raster original, int colIx) {
			this.i = colIx;
			this.output = output;
			this.original = original;
		}

		@Override
		public void run() {
			int xmin, ymin, xmax, ymax;
			
			double threshold;
			
			for (int j = 0; j < m_height; j++) {
				xmin = Math.max(0, i - whalf);
				ymin = Math.max(0, j - whalf);
				xmax = Math.min(m_width - 1, i + whalf);
				ymax = Math.min(m_height - 1, j + whalf);
				
				RegionDistribution dist = computeDistribution(xmin, ymin, xmax, ymax);
				
				threshold = dist.mean * (1 + k * ((dist.stddev / r) - 1));
				
				if (original.getSample(i, j, 0) > threshold) 
					output.setSample(i, j, 0, 255);
				else 
					output.setSample(i, j, 0, 0);
			}
			
		}
	}

	public static final class RegionDistribution
	{
		private double mean;
		private double stddev;
		
		public double getMean() {
			return mean;
		}
		
		public double getStddev() {
			return stddev;
		}
	}
}
