package org.dharts.dia.seg.lines;

import java.awt.image.BufferedImage;

import org.dharts.config.params.ParameterValueSet;

public interface LineSegmenter {

	ParameterValueSet getConfiguration();
	
	void setConfiguration(ParameterValueSet config);
	
	/**
	 * Performs line segmentation on the supplied image. Note that implementations should 
	 * take care to ensure that this segmentation is performed in a thread-safe manner. 
	 * 
	 * @param image The image to be segmented.
	 * @return An {@link Iterable} of recognized {@link TextLine}s.
	 */
	Iterable<TextLine> segment(BufferedImage image);
}
