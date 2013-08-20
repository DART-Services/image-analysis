package org.dharts.dia.seg.lines;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import org.dharts.dia.util.ImageWrapper;
import org.dharts.dia.util.IntegralImage;

// HACK: this is a hackish implementation of a line segmentation algorithm
public class ProjectionProfiler implements Callable<List<Integer>>
{
	public static ProjectionProfiler create(BufferedImage image)
	{
		ImageWrapper wrapper = new ImageWrapper(image);
		return new ProjectionProfiler(wrapper.getIntegralImage());
	}

	public static ProjectionProfiler create(IntegralImage iImage)
	{
		return new ProjectionProfiler(iImage);
	}

	private final IntegralImage iImage;

	public ProjectionProfiler(IntegralImage iImage)
	{
		this.iImage = iImage;
	}

	@Override
	public List<Integer> call() throws Exception {
		return findLines();
	}

	public List<Integer> findLines()
	{
		int h = iImage.getHeight();

		long[] proj = new long[h];
		long[] dProj = new long[h];

		dProj[0] = 0;
		int window = 30;
		boolean increasing = false;
		proj[0] = iImage.getHorizontalProjection(0, window);
		List<Integer> lines = new ArrayList<>();
		for (int y = 1; y < h; y++)
		{
			proj[y] = iImage.getHorizontalProjection(y, window);
			dProj[y] = proj[y] - proj[y - 1];

			if ((dProj[y] > 0 && !increasing) || (dProj[y] < 0 && increasing))
			{
				increasing = !increasing;

				if (dProj[y] > 0)
					lines.add(Integer.valueOf(y));
			}
		}

		prune(proj, lines);
		return lines;
	}

	private static void prune(long[] proj, List<Integer> lines) {
		double mean = 0;
		double var = 0;
		int sz = 0;
		double M2 = 0;
		double delta;
		for (Integer ix : lines)
		{
			sz++;
			long v = proj[ix.intValue()];
			delta = v - mean;
			mean = mean + delta / sz;
			M2 = M2 + delta * (v - mean);
			var = M2 / sz;
		}

		double s = Math.sqrt(var);
		double threshold = mean + s;
		int prev = 0;
		for (Iterator<Integer> i = lines.iterator(); i.hasNext(); )
		{
			int lineIx = i.next().intValue();
			long v = proj[lineIx];
			if (v > threshold || v > 240 || (lineIx - prev) < 20)
				i.remove();

			prev = lineIx;
		}
	}
}