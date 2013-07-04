/* Created on  Aug 16, 2007
 * Last Modified on $Date: 2008-07-17 19:08:32 $
 * $Revision: 1.1 $
 * $Log: ImageUtils.java,v $
 * Revision 1.1  2008-07-17 19:08:32  neal
 * Reattached NADL Project to a CVS Repository. This time the HTML, JS, and other webcomponents are being backed up as well.
 *
 * Revision 1.2  2007-12-19 23:36:22  neal
 * Added methods to save image to output stream
 *
 * Revision 1.1  2007-11-08 15:39:20  neal
 * Creating a general project to provide a consistent codebase for NADL. This is being expanded to include most of the components from the old CSDLCommon and CSDLWeb packages, as I reorganize the package structure and improve those components.
 *
 * 
 * Copyright TEES Center for the Study of Digital Libraries (CSDL),
 *           Neal Audenaert
 *
 * ALL RIGHTS RESERVED. PERMISSION TO USE THIS SOFTWARE MAY BE GRANTED 
 * TO INDIVIDUALS OR ORGANIZATIONS ON A CASE BY CASE BASIS. FOR MORE 
 * INFORMATION PLEASE CONTACT THE DIRECTOR OF THE CSDL. IN THE EVENT 
 * THAT SUCH PERMISSION IS GIVEN IT SHOULD BE UNDERSTOOD THAT THIS 
 * SOFTWARE IS PROVIDED ON AN AS IS BASIS. THIS CODE HAS BEEN DEVELOPED 
 * FOR USE WITHIN A PARTICULAR RESEARCH PROJECT AND NO CLAIM IS MADE AS 
 * TO IS CORRECTNESS, PERFORMANCE, OR SUITABILITY FOR ANY USE.
 */
package org.dharts.dia.threshold;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.Kernel;
import java.awt.image.RenderedImage;
import java.util.HashMap;
import java.util.Map;

public class ImageUtils {
    private static final String logger = ImageUtils.class.getName();
    
    // Supported image formats (USING JAI)
    public static final String TIF  = "tif";
    public static final String TIFF = "tiff";
    public static final String BMP  = "bmp";
    public static final String FPX  = "fpx";
    public static final String GIF  = "gif";
    public static final String JPEG = "jpeg";
    public static final String JPG  = "jpg";
    public static final String PNG  = "png";
    public static final String PNM  = "pnm";
    
    public static final String supportedFormats[] =
        { TIF, TIFF, JPEG, JPG, GIF, BMP, FPX, PNG, PNM };
    
    public static final Map<String, String> fileStoreOpts =
        new HashMap<String, String>();
         
    static {
        fileStoreOpts.put(TIF,  "TIFF");
        fileStoreOpts.put(TIFF, "TIFF");
        fileStoreOpts.put(JPEG, "JPEG");
        fileStoreOpts.put(JPG,  "JPEG");
        fileStoreOpts.put(GIF,  "GIF");
        fileStoreOpts.put(BMP,  "BMP");
        fileStoreOpts.put(FPX,  "FPX");
        fileStoreOpts.put(PNG,  "PNG");
        fileStoreOpts.put(PNM,  "PNM");
    }
    
    /**
     * Attempts to load an image from the specified file. If the image cannot
     * be loaded, this will return null. Note that this supresses
     * (and logs) any possible <tt>FileNotFoundException</tt> or 
     * <tt>IOException</tt>.
     * 
     * @param file An <tt>File</tt> containing the image data.
     * 
     * @return a <tt>PlanarImage</tt> object containing the image data from 
     *      the provided <tt>File</tt> or <tt>null</tt> if the image 
     *      could not be loaded.
     */
//    public static PlanarImage loadImage(File file) {
//        PlanarImage image = null;
//        try {
//            InputStream is = new FileInputStream(file);
//            image = loadImage(is);
//            if (image == null) {
//                String msg = "Could not load image: " + file.getAbsolutePath();
//                LogService.logWarn(msg, logger);
//            }
//        } catch (FileNotFoundException fnfe) {
//            String msg = "Could not load image. File not found: " +
//                file.getAbsolutePath();
//            LogService.logError(msg, logger, fnfe);
//            image = null;
//        }
//        
//        return image;
//    }
    
    /** 
     * Attempts to load an image from the provided <tt>InputStream</tt>. If the 
     * image cannot be loaded, this will return null. Note that this supresses
     * (and logs) any possible <tt>IOException</tt>.
     *  
     * @param is An <tt>InputStream</tt> containing the image data.
     * 
     * @return a <tt>PlanarImage</tt> object containing the image data from 
     *      the provided <tt>InputStream</tt> or <tt>null</tt> if the image 
     *      could not be loaded.
     */
//    public static PlanarImage loadImage(InputStream is) {
//        PlanarImage image = null;
//        try {
//            SeekableStream imgStream = new FileCacheSeekableStream(is);
//            image = JAI.create("Stream", imgStream);
//        } catch (IOException ioe) {
//            String msg = "Could not load image. IO error. ";
//            LogService.logError(msg, logger, ioe);
//            image = null;
//        }
//        
//        return image;
//    }
    
    /**
     * Saves the provided image.
     * 
     * @param name The name of the image (without the file extension).
     * @param dir The directory in which the image is to be saved.
     * @param image The image to be saved.
     * @param format The format in which the image is to be saved. This must
     *      be one of the formats specified by 
     *      <tt>ImageUtils.supportedFormats</tt>.
     *      
     * @return The name of the saved file (with the extension).
     */
//    public static String saveImage(String name, File dir, RenderedImage image, 
//            String format) throws IOException {
//        
//        // save the image
//        String opt       = fileStoreOpts.get(format);
//        String filename  = name + "." + format;
//        File   imagefile = new File(dir, filename);
//        
//        // handle error conditions
//        String errmsg = null;
//        if (opt == null) {
//            errmsg = "Unsupported File Format: " + format;
//        } else if (!dir.exists()) {
//            errmsg = "Directory does not exist: " + dir.getAbsolutePath();
//        } else if (imagefile.exists()) {
//            errmsg = "Image already exists: " + imagefile.getAbsolutePath();
//        }
//        
//        if (errmsg != null) throw new IOException(errmsg);
//        
//        JAI.create("filestore", image, imagefile.getAbsolutePath(), opt, null);
//        LogService.logInfo("Archived Image <" + name + ">: " + 
//                imagefile.getAbsolutePath(), logger);
//        
//        return filename;
//    }
    
    /**
     * Writes the provided image to an output stream.
     * 
     * @param image The image to be written.
     * @param os The output stream to which the image will be written.
     * @param format The format in which the image is to be saved. This must
     *     a format specified by <tt>ImageUtils.supportedFormats</tt>. 
     */
//    public static void saveImage(RenderedImage image, OutputStream os,  
//            String format) throws IOException {
//        if (StringUtils.isEmpty(format)) format = JPEG;
//        String opt = fileStoreOpts.get(format);
//        if (opt == null) {
//            String msg = "Unsupported File Format: " + format;
//            throw new IOException(msg);
//        }
//        
//        JAI.create("encode", image, os, opt, null);
//    }
    
    /**
     * Writes the provided image to an output stream as a JPEG image.
     * 
     * @param image The image to be written.
     * @param os The output stream to which the image will be written.
     */
//    public static void saveImage(RenderedImage image, OutputStream os) 
//        throws IOException {
//        saveImage(image, os, JPEG);
//    }
    
    /**
     * Scales an image by the specified factor optionally using the 
     * provided interpolation algorithm. If the interpolation algorithm is
     * null, a bicubic interpolation will be used.
     *  
     * @param image The image to be scaled. 
     * @param factor The factor by which to scale the image.
     * @param interp (optional) The interpolation algorithm to be used. If 
     *      this is null, a default interpolation algorithm will be applied.
     *      
     * @return The scaled image.
     */
//    public static PlanarImage scaleImage(
//            RenderedImage image, float factor, Interpolation interp) {
//        // by default, use bicubic interpolation
//        if (interp == null) 
//            interp = new InterpolationBicubic(8);
//        
//        ParameterBlock pb = new ParameterBlock();     //scales image
//        pb.addSource(image); // The source image
//        pb.add(factor);      // % scale x
//        pb.add(factor);      // % scale y
//        pb.add(0.0f);        // don't know - leave 0 
//        pb.add(0.0f);        // don't know - leave 0
//        pb.add(interp);
//
//        return JAI.create("scale", pb, null);
//    }
    
    /**
     * Scales the provided image to fit within the maximum x, y coordinates 
     * specified. 
     * 
     * @param image The image to be scaled.
     * @param maxX The maximum number of pixels in the x direction.
     * @param maxY The maximum number of pixels in the y direction.
     * @param enlarge flag indicating whether to increase the image size if it
     *      is already smaller than the the provided x, y coordinates. 
     * @param interp (optional) The interpolation algorithm to be used. If 
     *      this is null, a default interpolation algorithm will be applied.
     *      
     * @return The scaled image.
     */
//    public static PlanarImage scaleImage(RenderedImage image, 
//            int maxX, int maxY, boolean enlarge, Interpolation interp) {
//        // by default, use bicubic interpolation
//        if (interp == null) 
//            interp = new InterpolationBicubic(8);
//        
//        // scale the image
//        float factor  = 1.0F; 
//        int   width   = image.getWidth(); 
//        int   height  = image.getHeight();
//        
//        // find scale factor
//        if (width > height) factor = (float)maxX / width;
//        else                factor = (float)maxY / height;
//        
//        if ((factor > 1) && !enlarge) factor = 1;
//        
//        return ImageUtils.scaleImage(image, factor, interp);
//    }
    
    /** 
     * Converts an image to a grayscale image.
     * 
     * @param image The image to be converted.
     * @return The grayscale version of the image.
     */
    public static BufferedImage grayscale(BufferedImage image) {
		ColorConvertOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
    	return op.filter(image, null);
        
        // TODO This provides a better perceptual balance in creating the grayscale image than equal 
    	// weighting of each band. this assumes and RGB band order
        // Current:   0.212671 * R + 0.715160 * G + 0.072169 * B;
        // Alternate: 0.3 * R + 0.59 * G + 0.11 * B
//        double[][] matrix = {{ 0.212671F, 0.715160F, 0.072169F, 0 }};  
    }
    
    /**
     * Applies a Frei-Chen edge detection algorithm.
     * 
     * @param image The image to convert.
     * @return The resulting image.
     * @author Grant Sherrick
     */
//    public static PlanarImage detectFCEdges(RenderedImage image) {
//        // edge detection (frei-chen)
//        float data_h[] = new float[] {             // horizontal kernel
//                1.0F,   0.0F,   -1.0F,
//                1.414F, 0.0F,   -1.414F,
//                1.0F,   0.0F,   -1.0F};
//        float data_v[] = new float[] {             // vertical kernel
//                -1.0F,  -1.414F, -1.0F,
//                0.0F,   0.0F,    0.0F,
//                1.0F,   1.414F,  1.0F};
//        
//        Kernel kern_h = new Kernel(3, 3, data_h);
//        Kernel kern_v = new Kernel(3, 3, data_v);
//        return JAI.create("gradientmagnitude", image ,kern_h, kern_v);  
//    }
    
//    public static PlanarImage detectEdges(RenderedImage image, int method) {
//        float data_h[] = null;
//        float data_v[] = null;
//        
//        switch (method) {
//        case EDGES_SOBEL:    data_h = SOBEL_H;    data_v = SOBEL_V;    break;
//        case EDGES_ROBERTS:  data_h = ROBERTS_H;  data_v = ROBERTS_V;  break;
//        case EDGES_PREWITT:  data_h = PREWITT_H;  data_v = PREWITT_V;  break;
//        case EDGES_FREICHEN: data_h = FREICHEN_H; data_v = FREICHEN_V; break;
//        default:             data_h = SOBEL_H;    data_v = SOBEL_V;    break;
//        }
//        
//        KernelJAI kern_h = new KernelJAI(3,3,data_h);
//        KernelJAI kern_v = new KernelJAI(3,3,data_v);
//        return JAI.create("gradientmagnitude", image ,kern_h, kern_v);  
//    }
    
    public static final int EDGES_SOBEL    = 0;
    public static final int EDGES_ROBERTS  = 1;
    public static final int EDGES_PREWITT  = 2;
    public static final int EDGES_FREICHEN = 3;
    
    
    public static final float[] SOBEL_H = new float[] {
        1.0F,  0.0F,  -1.0F,
        2.0F,  0.0F,  -2.0F,
        1.0F,  0.0F,  -1.0F};
    
    public static final float[] SOBEL_V = new float[] {
       -1.0F, -2.0F, -1.0F,
        0.0F,  0.0F,  0.0F,
        1.0F,  2.0F,  1.0F};
    
    public static final float[] ROBERTS_H = new float[] {
        0.0F,  0.0F, -1.0F,
        0.0F,  1.0F,  0.0F,
        0.0F,  0.0F,  0.0F};
    
    public static final float[] ROBERTS_V = new float[] {
        -1.0F,  0.0F,  0.0F,
        0.0F,  1.0F,  0.0F,
        0.0F,  0.0F,  0.0F};
    
    public static final float[] PREWITT_H = new float[] {
        1.0F,  0.0F, -1.0F,
        1.0F,  0.0F, -1.0F,
        1.0F,  0.0F, -1.0F};
    
    public static final float[] PREWITT_V = new float[] {
        -1.0F, -1.0F, -1.0F,
        0.0F,  0.0F,  0.0F,
        1.0F,  1.0F,  1.0F};
    
    
    public static final float[] FREICHEN_H = new float[] {
        1.0F,   0.0F,   -1.0F,
        1.414F, 0.0F,   -1.414F,
        1.0F,   0.0F,   -1.0F};
    
    public static final float[] FREICHEN_V = new float[] {
        -1.0F,  -1.414F, -1.0F,
        0.0F,   0.0F,    0.0F,
        1.0F,   1.414F,  1.0F};
    
    public static final float[] SQUARE_3 = new float[] {
        1, 1, 1, 
        1, 1, 1,
        1, 1, 1
    };
    
    public static final float[] CIRCLE_3 = new float[] {
        0, 1, 0, 
        1, 1, 1,
        0, 1, 0
    };
    
    public static final float[] RECTANGLE_9x3 = new float[] {
        1, 1, 1, 1, 1, 1, 1, 1, 1, 
        1, 1, 1, 1, 1, 1, 1, 1, 1, 
        1, 1, 1, 1, 1, 1, 1, 1, 1 
    };
    
    public static final float[] RECTANGLE_3x9 = new float[] {
        1, 1, 1, 
        1, 1, 1,
        1, 1, 1,
        1, 1, 1, 
        1, 1, 1,
        1, 1, 1,
        1, 1, 1, 
        1, 1, 1,
        1, 1, 1
    };
    
//    public static BufferedImage dilate(RenderedImage image, float[] data, int width, int height) {
//        Kernel kern = new Kernel(width, height, data);
//        return JAI.create("dilate", image, kern); 
//    }
//    
//    public static PlanarImage erode(RenderedImage image, float[] data, int width, int height) {
//        KernelJAI kern = new KernelJAI(width, height, data);
//        return JAI.create("erode", image, kern); 
//    }
    
    /** 
     * Implements the morphological open operation. An open operation consists 
     * of an erosion followed by a dilation.
     *  
     * @param image
     * @param data
     * @param width
     * @param height
     * @return
     */
//    public static PlanarImage open(RenderedImage image,
//            float[] data, int width, int height) {
//        PlanarImage result = null;
//        
//        KernelJAI kern = new KernelJAI(width, height, data);
//        result = JAI.create("erode", image, kern);
//        result = JAI.create("dilate", result, kern);
//        
//        return result;
//        
//    }
//    
//    public static PlanarImage close(RenderedImage image,
//            float[] data, int width, int height) {
//        PlanarImage result = null;
//        
//        KernelJAI kern = new KernelJAI(width, height, data);
//        result = JAI.create("dilate", image, kern);
//        result = JAI.create("erode", result, kern);
//        
//        return result;
//    }
}
