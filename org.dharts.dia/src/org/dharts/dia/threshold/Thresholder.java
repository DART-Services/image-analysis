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
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;

import org.dharts.dia.BadParameterException;

/**
 * Defines a general interface for binarization algorithms.
 *
 * @author Neal Audenaert
 */
public interface Thresholder extends Callable<int[]> {

    /**
     * Sets the image to be processed with this <code>Thresholder</code> and
     * initializes the internal state of the thresholder. Following a call to
     * this method, the object must be ready to process the specified image,
     * using default values for any required configuration parameters. Client
     * specified parameter values may be provided via the
     * <code>setParameters</code> method prior to executing the algorithm.
     *
     * @param file A file containing the image to be binarized. This file must
     *      exist and be a readable image file. The file itself will not be
     *      modified.
     * @throws IOException If the file cannot be read or if there is an error
     *      initialzing the <code>Thresholder</code> based on this image.
     */
    public void initialize(File file) throws IOException;

    /**
     * Sets the image to be processed with this <code>Thresholder</code> and
     * initializes the internal state of the thresholder. Following a call to
     * this method, the object must be ready to process the specified image,
     * using default values for any required configuration parameters. Client
     * specified parameter values may be provided via the
     * <code>setParameter</code> method prior to executing the algorithm.
     *
     * @param image The image to be binarized. The supplied image will not be
     *      modified.
     * @throws IOException If the file cannot be read or if there is an error
     *      initialzing the <code>Thresholder</code> based on this image.
     */
    public void initialize(BufferedImage image) throws IOException;

    /**
     * Executes the binarization algorithm.
     *
     * @return The thresholded image.
     * @throws IllegalStateException If the binarization algorithm has not been
     *      <code>initialize</code>d.
     * @throws IOException If there is an error reading and/or processing
     *      the image data.
     * @throws InterruptedException
     */
    @Override
    public int[] call() throws IllegalStateException, IOException, InterruptedException;

    /**
     * List the configuration parameters for this algorithm. The parameters
     * are returned as a <code>Map</code> whose keys are the parameter names
     * and whose values are human readable definitions of the parameter.
     * Subclasses should implement these definitions to describe the appropriate
     * range and typically useful values for the parameters.
     *
     * @return A <code>Map</code> whose keys are the parameter names and whose
     *      values are the human readable definitions of the parameter.
     */
    public Map<String, String> listParamters();

    /**
     * Sets the value of a specified parameter. See <code>listParamters</code>
     * to determine what parameters are defined for a given algorithm.
     *
     * @param param The parameter to be set.
     * @param value The value to assign to this parameter.
     * @throws BadParameterException If the supplied parameter is not
     *      recognized or if the provided value is not valid.
     */
    public void setParameter(String param, double value) throws BadParameterException;

    /**
     * Returns the current value for the specified parameter.
     *
     * @param param The paramater to query.
     *
     * @return The value of the specified parameter or <code>Double.NaN</code>
     *      if the indicated parameter is not in use by this algorithm.
     *
     * @throws BadParameterException If the supplied parameter is not defined
     *      for this algorithm.
     */
    public double getParameter(String param) throws BadParameterException;

    /**
     * Returns the resulting thresholded image following the exectution of
     * this algorithm. If this algorithm has not yet completed, this will
     * throw a <code>NotReadyException</code>.
     *
     * @return The thresholded image. This image will have values pixel values
     *      of either 0 or 255.
     *
     * @throws IllegalStateException If the algorithm has not yet finished execution.
     */
    public BufferedImage getResult() throws IllegalStateException;

    /**
     * Returns the resulting thresholded image following the exectution of
     * this algorithm. If this algorithm has not yet completed, this will
     * throw a <code>NotReadyException</code>.
     *
     * @return The thresholded image. This image will have values pixel values
     *      of either 0 or 255.
     *
     * @throws NotReadyException If the algorithm has not yet finished
     *      execution.
     */
    // public WritableRaster getWritableRaster() throws NotReadyException;

    /**
     * Returns the resulting thresholded image following the exectution of
     * this algorithm. If this algorithm has not yet completed, this will
     * throw a <code>NotReadyException</code>.
     *
     * @return The thresholded image. This image will have values pixel values
     *      of either 0 or 255.
     *
     * @throws NotReadyException If the algorithm has not yet finished
     *      execution.
     */
    // public Raster getRaster() throws NotReadyException;

    /**
     * Returns the resulting thresholded image following the exectution of
     * this algorithm. If this algorithm has not yet completed, this will
     * throw a <code>NotReadyException</code>.
     *
     * @return The thresholded image as a raster-order byte array. Not that
     *      to access any given point <x, y> in the original image, you should
     *      used the array index
     *      <code>
     *        i = y * width + x;
     *     </code>
     *
     *     Where widht is the width of the input image in pixels.
     *
     * @throws NotReadyException If the algorithm has not yet finished
     *      execution.
     */
    // public byte[] getPixels() throws NotReadyException;

    /**
     * Indicates whether the <code>Thresholder</code> is ready to be
     * initialized with a new image. If this is false, any calls to
     * <code>initialize</code> will result in an exception.
     *
     * @return <code>true</code> if the <code>Thresholder</code> is ready to be
     *      initialized, false if it is not.
     */
    public boolean isReady();

    /**
     * Indicates whether the <code>Thresholder</code> has finished processing
     * the supplied image. If this returns <code>false</code>, any attempt to
     * get the resulting binarized image will result in an exception.
     *
     * @return <code>true</code> if the <code>Thresholder</code> has finished
     *      processing the image, false if it has not.
     */
//    public boolean isDone();


}
