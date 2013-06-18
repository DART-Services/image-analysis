/* File: SimpleBoxComparator.java
 * Created: Feb 9, 2013
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

import java.util.Comparator;


/**
 * Simple comparator for {@link BoundingBox}s that evaluates their position on a page (top to 
 * bottom, left to right). Note that, in general this is a poor proxy for reading order. Where
 * information about reading order is available, a more suitable implementation should be 
 * provided.
 *  
 * @author Neal Audenaert
 */
public class SimpleBoxComparator implements Comparator<BoundingBox>
{
    private static int cmp(int a, int b) {
        return (a == b) ? 0 : (a < b) ? -1 : 1; 
    }
    
    @Override
    public int compare(BoundingBox box1, BoundingBox box2) {
        if ((box1 == null) || (box2 == null))
            return (box1 == box2) ? 0 : (box2 == null) ? -1 : 1;
            
        if (box1.equals(box2))
            return 0;
        
        int cmp = cmp(box1.getTop(), box2.getTop());
        if (cmp != 0)
            return cmp;
        
        cmp = cmp(box1.getLeft(), box2.getLeft());
        if (cmp != 0)
            return cmp;
        
        cmp = cmp(box1.getRight(), box2.getRight());
        if (cmp != 0)
            return cmp;
        
        cmp = cmp(box1.getBottom(), box2.getBottom());
        if (cmp != 0)
            return cmp;
        
        return 0;
    }
}