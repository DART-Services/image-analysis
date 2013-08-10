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
    
    private IntegralImage iImage;
    
    // -----------------------------------------------------------------------
    // PROPERTIES
    // -----------------------------------------------------------------------
    // TODO make these configuration parameters.
    private int    ts = 48;     	// tile size
    private int    whalf = ts / 2;	// half the window size
    private double k  = 0.3;    	//
    private int    r  = 128;    	// control for dynamic range

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

    private static final ColorConvertOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
    @Override
    public void initialize(BufferedImage image) {
     	sourceImage = op.filter(image, null);
        iImage = new IntegralImage();
        iImage.initialize(sourceImage);
        
        initProps();
    }
    
    public void initialize(IntegralImage iIm) {
    	this.iImage = iIm;
    	this.sourceImage = iIm.getSourceImage();
    	
    	initProps();
    }

	private void initProps() {
		width = iImage.getWidth();
    	height = iImage.getHeight();
    	imArea = iImage.getArea();
    	
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
        outputImage = performThresholding(original);
        
        return outputImage;
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
	
	public final int getForegroundPixelCount() 
	{
		return ct.get();
	}
	
	public final int getBackgroundPixelCount() 
	{
		return imArea - ct.get();
	}
	

	@Override
	public final boolean isReady() {
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
			double mean, stddev;
			int xmin, ymin, xmax, ymax;
			
			double threshold;
			boolean isBackground;
			
			for (int j = 0; j < height; j++) {
				xmin = Math.max(0, i - whalf);
				ymin = Math.max(0, j - whalf);
				xmax = Math.min(width - 1, i + whalf);
				ymax = Math.min(height - 1, j + whalf);
			       
				double[] model = iImage.getGausModel(xmin, ymin, xmax, ymax);
				mean = model[0];
				stddev = Math.sqrt(model[1]);

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
