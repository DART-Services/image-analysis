package org.dharts.dia.seg;

import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;

import org.dharts.dia.BoundingBox;
import org.dharts.dia.SimpleBoundingBox;

public class ConnectedComponent {

	public static void write(ConnectedComponent cc, WritableRaster raster, int[] color)
	{
		int bands = color.length;
		for (int[] point : cc.points)
		{
			int x = point[0];
			int y = point[1];
			for (int b = 0; b < bands; b++) {
				raster.setSample(x, y, b, color[b]);
			}
		}
	}

	// TODO make this immutable
	private final List<int[]> points = new ArrayList<>();
	private volatile int xMin = -1;
	private volatile int xMax = -1;
	private volatile int yMin = -1;
	private volatile int yMax = -1;

	public ConnectedComponent() {
		// TODO Auto-generated constructor stub
	}

	public BoundingBox getBounds()
	{
		return new SimpleBoundingBox(xMin, yMin, xMax, yMax);
	}

	public void add(int x, int y)
	{
		if (xMin < 0 || xMin > x)
			xMin = x;
		if (xMax < 0 || xMax < x)
			xMax = x;
		if (yMin < 0 || yMin > y)
			yMin = y;
		if (yMax < 0 || yMax < y)
			yMax = y;

		points.add(new int[] {x, y});
	}

	public int getNumberOfPixels()
	{
		return points.size();
	}
}
