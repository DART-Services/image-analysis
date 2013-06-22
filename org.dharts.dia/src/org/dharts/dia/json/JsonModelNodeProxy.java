/* File: JsonModelNode.java
 * Created: Mar 23, 2013
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
package org.dharts.dia.json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dharts.dia.BasicModelNode;
import org.dharts.dia.Level;
import org.dharts.dia.LevelProvider;
import org.dharts.dia.PageItem;
import org.dharts.dia.PageModelException;
import org.dharts.dia.PageModelNode;

/**
 * @author Neal Audenaert
 *
 */
public abstract class JsonModelNodeProxy<T extends JsonItemProxy> {

    private String levelName;
    protected T item;
    private List<JsonModelNodeProxy<T>> children;
    private Map<String, Object> params = new HashMap<>();
    private int seqNo = 0;

    public T getItem() {
        return item;
    }

    /**
     * @param item the item to set
     */
    public abstract void setItem(T itemProxy); 

    public String getLevel() {
        return levelName;
    }
    
    /**
     * @param level the level to set
     */
    public void setLevel(String level) {
        this.levelName = level;
    }
    
    /**
     * @return
     */
    public int getSeqNumber() {
        return seqNo;
    }
    
    public void setSeqNumber(int seqNo) {
        this.seqNo = seqNo;
    }

    public List<JsonModelNodeProxy<T>> getChildren()
    {
        return children;
    }
    
    /**
     * @param children the children to set
     */
    public void setChildren(List<JsonModelNodeProxy<T>> children) {
        this.children = Collections.unmodifiableList(new ArrayList<>(children));
    }
    
    public Map<String, Object> getParameters() {
        return params;
    }
    
    public void setParameters(Map<String, Object> params) {
        this.params = params;
    }
    
    public <X extends PageItem> PageModelNode<X> toModelNode(
            LevelProvider levelProvider, Class<X> itemType) {
        Level level;
        try {
            level = levelProvider.getLevel(levelName);
        } catch (PageModelException e) {
            throw new IllegalStateException("Cannot restore node level [" + levelName + "]", e);
        }
        
        X pageItem = getItem().toItem(itemType);
        return BasicModelNode.create(pageItem, itemType, level, seqNo);
    }
}
