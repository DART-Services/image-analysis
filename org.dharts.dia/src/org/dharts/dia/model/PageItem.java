/* File: PageItem.java
 * Created: Sep 22, 2012
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
package org.dharts.dia.model;

import org.dharts.dia.BoundingBox;
import org.dharts.dia.props.PropertyException;
import org.dharts.dia.props.TypedKey;
import org.dharts.dia.props.TypedMap;

/**
 * Immutable representation of an identified feature on a document page image. This could, 
 * among other things, include features recognized through a document layout analysis engine 
 * (e.g., a block of text or a single character), manually identified features, or a region 
 * of interest that a user wishes to attach notes to.
 *  
 * @author Neal Audenaert
 */
public interface PageItem extends Comparable<PageItem> {
    
    /**
     * @return The bounding box of this item. Will not be {@code null}.
     */
    BoundingBox getBox();
    
    // FIXME replace this. reading order is a property of the model, not of the page item
    @Deprecated
    int getSeqNumber();
    
    TypedMap getProperties();
    
    <X> X getProperty(TypedKey<X> key) throws PropertyException;
}