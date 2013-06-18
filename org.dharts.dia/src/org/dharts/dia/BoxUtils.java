/* File: BoxUtils.java
 * Created: Feb 23, 2013
 * Author: Neal Audenaert
 *
 * Copyright 2013 Digital Archives, Research & Technology Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dharts.dia;

/**
 * Defines methods to assist in comparing {@link BoundingBox}s and in calculating intersections
 * and unions between boxes.
 *  
 * @author Neal Audenaert
 */
public class BoxUtils {

    /**
     * @param parent The {@link BoundingBox} supposed to contain the child.
     * @param child The {@link BoundingBox} supposed to be contained by the parent.
     * @return {@code true} if the child's region lies within or is co-extensive 
     *      with the parent's. 
     */
    public static boolean contains(BoundingBox parent, BoundingBox child) {
        return parent.getTop() <= child.getTop() && parent.getBottom() >= child.getBottom() 
            && parent.getLeft() <= child.getLeft() && parent.getRight() >= child.getRight();
    }
    
    /**
     * Indicates whether two bounding boxes intersect.
     * 
     * @param b1 The first box to test
     * @param b2 The second box to test
     * @return {@code true} if {@code b1} and {@code b2} intersect.
     */
    public static boolean intersects(BoundingBox b1, BoundingBox b2) {
        int l = b1.getLeft() > b2.getLeft() ? b1.getLeft() : b2.getLeft(); 
        int t = b1.getTop() > b2.getTop() ? b1.getTop() : b2.getTop(); 
        int r = b1.getRight() < b2.getRight() ? b1.getRight() : b2.getRight(); 
        int b = b1.getBottom() < b2.getBottom() ? b1.getBottom() : b2.getBottom(); 
        
        return !((l > r) || (t > b)); 
    }

    /**
     * Computes the intersection of two {@link BoundingBox}s. If the boxes do not intersect, 
     * this will return a {@link BoundingBox} anchored at the origin {@code (0, 0)} with a 
     * negative height and width.
     * 
     * @param b1 The first box
     * @param b2 The second box
     * @return A {@link BoundingBox} representing the intersection of the two supplied boxes.
     *      Will not return {@code null}. If the supplied boxes do not intersect, this will 
     *      return a {@link BoundingBox} anchored at the origin {@code (0, 0)} with a 
     *      negative height and width.
     *      
     * @see #intersects(BoundingBox, BoundingBox)
     */
    public static BoundingBox intersection(BoundingBox b1, BoundingBox b2) {
        int l = b1.getLeft() > b2.getLeft() ? b1.getLeft() : b2.getLeft(); 
        int t = b1.getTop() > b2.getTop() ? b1.getTop() : b2.getTop(); 
        int r = b1.getRight() < b2.getRight() ? b1.getRight() : b2.getRight(); 
        int b = b1.getBottom() < b2.getBottom() ? b1.getBottom() : b2.getBottom(); 
        
        return ((l > r) || (t > b)) 
                ? new SimpleBoundingBox(0, 0, -1, -1) 
                : new SimpleBoundingBox(l, t, r, b);
    }
    
    /**
     * Computes the union of two boxes. This is applicable regardless of whether the 
     * bounding boxes intersect and will return a contiguous {@link BoundingBox} that spans
     * all content covered by the two boxes. Note that, unless one box contains the other, 
     * this will include regions that are not in either box.
     * 
     * @param b1 The first box
     * @param b2 The second box
     * @return The smallest possible {@link BoundingBox} that contains both of the 
     *      supplied boxes. 
     */
    public static BoundingBox union(BoundingBox b1, BoundingBox b2) {
        int l = b1.getLeft() < b2.getLeft() ? b1.getLeft() : b2.getLeft(); 
        int t = b1.getTop() < b2.getTop() ? b1.getTop() : b2.getTop(); 
        int r = b1.getRight() > b2.getRight() ? b1.getRight() : b2.getRight(); 
        int b = b1.getBottom() > b2.getBottom() ? b1.getBottom() : b2.getBottom(); 
        
        return new SimpleBoundingBox(l, t, r, b);
    }
    
    // Static methods only. Should not be instantiated.
    private BoxUtils() {
        
    }
}
