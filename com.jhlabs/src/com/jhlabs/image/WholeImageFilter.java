/*
 * Copyright 2006 Jerry Huxtable
 *
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
 *
 * With revisions by Neal Audenaert (neal@idch.org)
 * Copyright 2013 Digital Archives, Reserach & Technology Services
 */


package com.jhlabs.image;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

/**
 * A filter which acts as a superclass for filters which need to have the whole image in memory
 * to do their stuff.
 */
public abstract class WholeImageFilter extends AbstractBufferedImageOp {

	/**
     * The input image bounds.
     */
	protected Rectangle originalSpace;

	/**
	 * Construct a WholeImageFilter.
	 */
	public WholeImageFilter() {
	}

    @Override
	public BufferedImage filter(BufferedImage src, BufferedImage dst) {
        int width = src.getWidth();
        int height = src.getHeight();

		originalSpace = new Rectangle(0, 0, width, height);
		// this appears to be unneeded/unused
		Rectangle dstSpace = transformSpace(originalSpace);

        if (dst == null) {
            ColorModel dstCM = src.getColorModel();
			WritableRaster raster = dstCM.createCompatibleWritableRaster(dstSpace.width, dstSpace.height);
			dst = new BufferedImage(dstCM, raster, dstCM.isAlphaPremultiplied(), null);
		}

		int[] inPixels = getRGB(src, 0, 0, width, height, null);
		inPixels = filterPixels(width, height, inPixels, dstSpace);
		setRGB(dst, 0, 0, dstSpace.width, dstSpace.height, inPixels);

        return dst;
    }

	/**
     * Calculate output bounds for given input bounds.
     *
     * <p>WARNING: Implementations must not modify the supplied rectangle. The {@link Rectangle}
     * implementation should not be mutable. However, it exposes mutable member variables
     * publicly. This reduces thread safety and makes implementations harder to reason about.
     * As a workaround, implementations of this method MUST NOT modify the supplied rectangle.
     * Instead, they should create a new rectangle and return that instance. The returned
     * rectangle, in turn, MUST NOT be modified or exposed.
     *
     * @param rect input and output rectangle
     */
	protected Rectangle transformSpace(Rectangle rect) {
		return new Rectangle(rect.x, rect.y, rect.width, rect.height);
	}

	/**
     * Actually filter the pixels.
     *
     * @param width the image width
     * @param height the image height
     * @param inPixels the image pixels. Note that this may be modified by the filter.
     * @param transformedSpace the output bounds
     * @return the output pixels
     */
	@Deprecated
	protected abstract int[] filterPixels(int width, int height, int[] inPixels, Rectangle transformedSpace);

	/**
	 * Actually filter the pixels.
	 *
	 * <p><b>NOTE:</b> This method is intended as a replacement for {@link #filterPixels(int, int, int[], Rectangle)}.
	 * The {@code transformedSpace} parameter appears not to be used (or else, is always identical
	 * to the original space). Filter implementations that do not use this parameter will be migrated
	 * over time to overwrite this method and to delegate the original (now deprecated
	 * {@code filterPixels}) to the simplified implementation. Once this transition has been
	 * completed, the deprecated method will be removed.
	 *
     * @param width the image width
     * @param height the image height
     * @param inPixels the image pixels
     * @param transformedSpace the output bounds
     * @return the output pixels
	 */
	protected int[] filterPixels(int width, int height, int[] inPixels)
	{
		return filterPixels(width, height, inPixels, transformSpace(originalSpace));
	}
}

