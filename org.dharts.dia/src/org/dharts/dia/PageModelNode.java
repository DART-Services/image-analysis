/* File: PageModelNode.java
 * Created: Feb 10, 2013
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

import java.util.List;



/**
 * Wraps a low-level observed page structure, represented by a {@link PageItem}, into node in 
 * an ordered, acyclic graph that represents a model of the page's layout.  
 * 
 * @author Neal Audenaert
 * 
 * @see PageModel
 * @see PageItem
 */
public interface PageModelNode<I extends PageItem> {
    // FIXME this should be parameterized based on the item type, not the page level

    /**
     * @return an list of the child nodes this node ordered by page reading order. 
     */
    List<? extends PageModelNode<?>> getChildren();
    
    /**
     * @return The bounding box that represents the extend of this {@link PageItem} on the 
     *      page image.
     */
    BoundingBox getBox();
    
    /**
     * @return The analytical level of the associated {@link PageItem}. Indicates, for example,
     *      if this is a block, paragraph, or word level feature.
     */
    Level getLevel();

    /**
     * @return The {@link PageItem} modeled by this {@link PageModelNode}.
     */
    I getItem();
    
    /**
     * @return The type of item wrapped by this node.
     */
    Class<I> getItemType();
    
    /** 
     * @return a sequence number that defines the relative order among items based on the
     *      reading order inferred by the model.
     */
    int getSeqNumber();

}