package org.dharts.dia.seg.lines;

import java.awt.image.BufferedImage;

import org.dharts.dia.BoundingBox;

public interface TextLine {

	BoundingBox getBoundingBox();
	
	BufferedImage renderLine();
}
