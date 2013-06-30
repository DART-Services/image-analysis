/* File: PageItemWrapper.java
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
package org.dharts.dia.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.dharts.dia.BoundingBox;
import org.dharts.dia.BoxUtils;


/**
 * Basic implementation of a {@link PageModelNode}. For simple applications, this may be 
 * suitable for use as-is. For more complex requirements, this is intended to serve as a base
 * implementation of the core {@link PageModelNode} API that can be extended as needed. 
 *    
 * @author Neal Audenaert
 */
public class BasicModelNode<I extends PageItem> implements PageModelNode<I> {
    
    public static <X extends PageItem> BasicModelNode<X> create(X item, Class<X> type, Level level) {
        return new BasicModelNode<>(item, type, level, 0);
    }
    
    public static <X extends PageItem> BasicModelNode<X> create(X item, Class<X> type, Level level, int seqNo) {
        return new BasicModelNode<>(item, type, level, seqNo);
    }
    
    private final I item;
    private final Class<I> type;
    private final Level level;
    
    private final int seqNo;            // FIXME still need to figure out how to initialize the sequence number
    
    private CopyOnWriteArrayList<BasicModelNode<?>> children = new CopyOnWriteArrayList<>();
    
    private BasicModelNode(I item, Class<I> type, Level level, int seqNo) {
        this.item = item;
        this.level = level;
        this.type = type;
        this.seqNo = seqNo;
    }
    
    @Override
    public BoundingBox getBox() {
        return item.getBox();
    }

    @Override
    public Level getLevel() {
        return level;
    }

    @Override
    public I getItem() 
    {
        return item;
    }
    
    @Override
	public Class<I> getItemType() {
        return type;
    }

    @Override 
    public int getSeqNumber() {
        // HACK: should be assigned by the builder when the model is built.
        return seqNo;
    }

    @Override
    public List<PageModelNode<?>> getChildren() 
    {
        return Collections.unmodifiableList(
                new ArrayList<PageModelNode<?>>(children));
    }

    public <X extends PageItem> boolean add(X child, Class<X> childType, Level childLevel)
    {
        // Assume that children are added in reading order. 
        
        // NOTE: A node can only be a child if it is contained within the bounding box of 
        //       its parent. This is a highly restrictive assumption that may not hold in 
        //       the general case, but that is left to a more robust page model implementation.
        if (!BoxUtils.contains(item.getBox(), child.getBox()))
            return false;
        
        if (level.isAcceptableChild(childLevel)) {
            children.add(BasicModelNode.create(child, childType, childLevel));
            return true;
        } 
        
        if (level.isAcceptableDescendant(childLevel)) {
            // Chain of responsibility: process children in page order and add to the first descendant that accepts it.
            for (BasicModelNode<?> childNode : children) {
                if (childNode.add(child, childType, childLevel))
                    return true;
            }
        }
        
        return false;
    }
}
