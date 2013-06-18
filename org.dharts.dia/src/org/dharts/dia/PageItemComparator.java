/* File: PageItemComparator.java
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
 * Default {@link Comparator} implementation for {@link PageItem}s. This compares items
 * based on their associated {@link BoundingBox}. While this may be useful for simple 
 * comparisons, it is not appropriate for reconstructing the reading order of a page in 
 * the general case. {@link PageModel} implementations will typically need to supply their own 
 * {@link Comparator} implementation in order to more appropriately handle page layout 
 * and cultural reading conventions.  
 * 
 * @author Neal Audenaert
 * @see SimpleBoxComparator
 */
public class PageItemComparator implements Comparator<PageItem>
{
    SimpleBoxComparator cmp = new SimpleBoxComparator();
    
    @Override
    public int compare(PageItem b1, PageItem b2) {
        return cmp.compare(b1.getBox(), b2.getBox());
    }
}