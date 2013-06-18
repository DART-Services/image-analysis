/* File: BoundingBox.java
 * Created: Nov 3, 2012
 * Author: Neal Audenaert
 *
 * Copyright 2012 Digital Archives, Research & Technology Services
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
 * Immutable definition of a rectangular region on a page. 
 *  
 * @author Neal Audenaert
 */
public interface BoundingBox {
    // TODO may implement a more generic RegionOfInterest and allow for circular, elliptical,
    //      polygonal and non-contiguous regions
    // TODO provide explicit support for scaling?
    
    /** 
     * @return The position of the left side of the bounding box. 
     */
    int getLeft();
    
    /** 
     * @return The position of the top of the bounding box. 
     */
    int getTop();
    
    /** 
     * @return The position of the right side of the bounding box. 
     */
    int getRight();
    
    /** 
     * @return The position of the bottom of the bounding box. 
     */
    int getBottom();
    
    /**
     * @return The width of the bounding box. 
     */
    int getWidth();
    
    /**
     * @return The height of the bounding box.
     */
    int getHeight();
    
    /**
     * @return The area of the bounding box.
     */
    public int getArea();
}