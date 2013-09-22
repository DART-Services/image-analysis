package org.dharts.dia.seg;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * Use row-by-row labeling algorithm to label connected components
 * The algorithm makes two passes over the image: one pass to record
 * equivalences and assign temporary labels and the second to replace each
 * temporary label by the label of its equivalence class.
 * [Reference]
 * Linda G. Shapiro, Computer Vision: Theory and Applications.  (3.4 Connected
 * Components Labeling)
 * Rosenfeld and Pfaltz (1966)
 *
 * From https://www.cs.washington.edu/education/courses/576/02au/homework/hw3/ConnectComponent.java
 */
public class ConnectedComponents
{
    final static int MAX_LABELS = 100_000;
//    int nextLabel = 1;
//    private final UnionFind uf = new UnionFind(MAX_LABELS);

    /**
     * label and re-arrange the labels to make the numbers of label continuous
     * @param zeroAsBg Leaving label 0 untouched
     */
//    public int[] compactLabeling(int[] image, int w, int h, boolean zeroAsBg)
//    {
//        //label first
//        int[] label = labeling(image, w, h, zeroAsBg);
//        int[] stat = new int[nextLabel + 1];
//        for (int i=0;i<image.length;i++) {
//            if (label[i]>nextLabel)
//                System.err.println("bigger label than next_label found!");
//            stat[label[i]]++;
//        }
//
//        stat[0]=0;              // label 0 will be mapped to 0
//                                // whether 0 is background or not
//        int j = 1;
//        for (int i=1; i<stat.length; i++) {
//            if (stat[i]!=0) stat[i]=j++;
//        }
//
//        System.out.println("From "+nextLabel+" to "+(j-1)+" regions");
//        nextLabel= j-1;
//        for (int i=0;i<image.length;i++) label[i]= stat[label[i]];
//        return label;
//    }

    /**
     * return the max label in the labeling process.
     * the range of labels is [0..max_label]
     */
//    public int getMaxLabel() {return nextLabel;}


    /**
     * Label the connect components
     * If label 0 is background, then label 0 is untouched;
     * If not, label 0 may be reassigned
     * [Requires]
     *   0 is treated as background
     * @param image data
     * @param d dimension of the data
     * @param zeroAsBg label 0 is treated as background, so be ignored
     */
    public int[] labeling(int[] image, int w, int h)
    {
    	Finder finder = new Finder(image, w, h);
    	return finder.process();
    }

    public Collection<ConnectedComponent> findCCs(int[] image, int w, int h)
    {
    	Map<Integer, ConnectedComponent> components = new HashMap<>();
    	int offset = 0;
    	int[] labels = labeling(image, w, h);
    	for (int r = 0; r < h; r++)
    	{
    		for (int c = 0; c < w; c++)
    		{
    			int label = labels[offset + c];
    			if (label == 0)
    				continue;

    			Integer l = Integer.valueOf(label);
				if (!components.containsKey(l))
				{
					components.put(l, new ConnectedComponent());
				}

				components.get(l).add(c, r);
    		}

    		offset += w;
    	}

    	return components.values();
    }

	private static class Finder
	{
		private final int[] image;
		private final int w;
		private final int h;

		private int[] result;

		private final UnionFind uf;

		boolean zeroAsBg = true;		// HACK: is this needed?
		public Finder(int[] image, int w, int h) {
			this.image = image;
			this.w = w;
			this.h = h;

			result = new int[w * h];
			uf = new UnionFind(MAX_LABELS);
		}

		private int[] process() {
			firstPass();
			secondPass();

			return result;
		}

		private void firstPass() {
	        int yOffset = -w;
	        for (int y = 0; y < h; ++y)
	        {
	        	yOffset += w;		// first element of the current row.
	            for (int x = 0; x < w; ++x)
	            {
	            	int ix = yOffset + x;		// index for x,y
	                int px = image[ix];			// the value of the pixel at x,y
					if (px == 0 && zeroAsBg)
	                	continue;		// don't label background pixels

	                int k = 0;
	                boolean connected = false;
	                // if connected to the left
	                if (x > 0 && image[ix - 1] == px) {
	                   k = result[ix - 1];
	                   connected = true;
	                }

	                // if connected to the up
	                int prexIx = (y - 1) * w + x;
					if (y > 0 && image[prexIx] == px &&
	                    (connected == false || result[prexIx] < k )) {
	                    k = result[prexIx];
	                    connected = true;
	                }

	                if (!connected)
	                	k = uf.increment();

	                if (k >= MAX_LABELS)
	                	throw new IllegalStateException("maximum number of labels reached. increase MAX_LABELS and recompile.");

	                result[ix] = k;
	                // if connected, but with different label, then do union
	                if (x > 0 && image[ix - 1]== px && result[ix - 1] != k)
	                	uf.union(k, result[ix - 1]);
	                if (y > 0 && image[prexIx] == px && result[prexIx] != k)
	                	uf.union(k, result[prexIx]);
	            }
	        }
		}

		private void secondPass() {
			int nextLabel = 1;
		    for (int i = 0; i < w * h; i++ ) {
		        if (image[i] !=0 || !zeroAsBg) {
		            result[i] = uf.find(result[i]);
		            // The labels are from 1, if label 0 should be considered, then
		            // all the label should minus 1
		            if (!zeroAsBg)
		            	result[i]--;
		        }
		    }

		    nextLabel--;   // next_label records the max label
		    if (!zeroAsBg)
		    	nextLabel--;
		}
	}

    /***************************************************************************
    *  Compilation:  javac WeightedQuickUnionUF.java
    *  Execution:  java WeightedQuickUnionUF < input.txt
    *  Dependencies: StdIn.java StdOut.java
    *
    *  Weighted quick-union (without path compression).
    *
    ****************************************************************************/

   public static class UnionFind {
       private final int[] id;    // id[i] = parent of i
       private final int[] sz;    // sz[i] = number of objects in subtree rooted at i
       private int count = 0;     // number of components
       private int label = 0;

       // Create an empty union find data structure with N isolated sets.
       public UnionFind(int maxSize) {
           id = new int[maxSize];
           sz = new int[maxSize];
       }

       int increment()
       {
    	   label++;
    	   count++;
    	   id[label] = label;
    	   sz[label] = label;

    	   return label;
       }

       // Return the number of disjoint sets.
       public int count() {
           return count;
       }

       // Return component identifier for component containing p
       public int find(int p) {
           while (p != id[p])
               p = id[p];
           return p;
       }

      // Are objects p and q in the same set?
       public boolean connected(int p, int q) {
           return find(p) == find(q);
       }


       // Replace sets containing p and q with their union.
       public void union(int p, int q) {
           int i = find(p);
           int j = find(q);
           if (i == j) return;

           // make smaller root point to larger one
           if   (sz[i] < sz[j]) {
        	   id[i] = j;
        	   sz[j] += sz[i];
    	   } else {
    		   id[j] = i;
    		   sz[i] += sz[j];
    	   }

           count--;
       }
   }
}