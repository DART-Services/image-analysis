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

/**
 * Given a binary image, this filter performs binary dilation, setting all added
 * pixels to the given 'new' color.
 */
public class DilateFilter extends BinaryFilter {

	private int threshold = 2;

	public DilateFilter() {
	}

	/**
	 * Set the threshold - the number of neighbouring pixels for dilation to occur.
	 * @param threshold the new threshold
     * @see #getThreshold
	 */
	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}

	/**
	 * Return the threshold - the number of neighbouring pixels for dilation to occur.
	 * @return the current threshold
     * @see #setThreshold
	 */
	public int getThreshold() {
		return threshold;
	}

	@Override
	protected int[] filterPixels(int width, int height, int[] inPixels, Rectangle transformedSpace) {
		return filterPixels(width, height, inPixels);
	}

	@Override
	protected int[] filterPixels(int width, int height, int[] inPixels) {
		int[] outPixels = new int[width * height];

		for (int i = 0; i < iterations; i++) {
			if (i > 0) {
				int[] t = inPixels;
				inPixels = outPixels;
				outPixels = t;
			}

			float colorIx = (float)i/iterations;
			for (int y = 0; y < height; y++)
			{
				boolean isTop = y == 0;
				boolean isBottom = y == height - 1;

				int yOffset = y * width;
				for (int x = 0; x < width; x++) {
					boolean isLeft = x == 0;
					boolean isRight = x == width - 1;

					int ix = yOffset + x;
					int pixel = inPixels[ix];
					if (!isBlack(pixel)) {
						int ct = countNeighbors(x, y, isTop, isBottom, isLeft, isRight, width, i, inPixels);
						if (ct >= threshold) {
							if (colormap != null) {
								pixel = colormap.getColor(colorIx);
							} else
								pixel = newColor;
						}
					}
					outPixels[ix] = pixel;
				}
			}
		}

		return outPixels;
	}

	public final boolean isBlack(int rgb) {
		return rgb == 0xff000000;
	}

	private int countNeighbors(int x, int y, boolean isTop, boolean isBottom, boolean isLeft, boolean isRight, int width, int iteration, int[] inPixels) {
		int neighbours = 0;

		int x1 = x - 1;
		int x3 = x + 1;

		// compute y - 1
		int ioffset = width * (y - 1);
		if (!isTop)
		{
			if (!isLeft && isBlack(inPixels[ioffset + x1]))
				neighbours++;
			if (isBlack(inPixels[ioffset + x]))
				neighbours++;
			if (!isRight && isBlack(inPixels[ioffset + x3]))
				neighbours++;
		}

		ioffset += width;
		if (!isLeft && isBlack(inPixels[ioffset + x1]))
			neighbours++;
		if (!isRight && isBlack(inPixels[ioffset + x3]))
			neighbours++;

		if (!isBottom)
		{
			ioffset += width;
			if (!isLeft && isBlack(inPixels[ioffset + x1]))
				neighbours++;
			if (isBlack(inPixels[ioffset + x]))
				neighbours++;
			if (!isRight && isBlack(inPixels[ioffset + x3]))
				neighbours++;
		}

		return neighbours;
	}

	@Override
	public String toString() {
		return "Binary/Dilate...";
	}

}

