/* File: Page.java
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
package org.dharts.dia.tesseract.model;

import java.util.Collections;
import java.util.List;

import org.dharts.dia.BoundingBox;
import org.dharts.dia.model.BasicModelNode;
import org.dharts.dia.model.Level;
import org.dharts.dia.model.LevelProvider;
import org.dharts.dia.model.PageItem;
import org.dharts.dia.model.PageModel;
import org.dharts.dia.model.PageModelException;
import org.dharts.dia.model.PageModelNode;
import org.dharts.dia.props.PropertyException;
import org.dharts.dia.props.SimpleTypedMap;
import org.dharts.dia.props.TypedKey;
import org.dharts.dia.props.TypedMap;

/**
 * 
 * @author Neal Audenaert
 */
public class Page implements PageModel {
    private final BoundingBox extent;
    private final BasicModelNode<PageItem> root;      
    
    /**
     * 
     * @param extent
     */
    public Page(BoundingBox extent) {
        this.extent = extent;  
        Level level;
        try {
            LevelProvider catalog = TesseractLevelCatalog.getInstance();       // HACK: should restore from context
            level = catalog.getLevel(TesseractLevelCatalog.PAGE);
        } catch (PageModelException e) {
            throw new IllegalStateException("Failed to construct page model", e);
        }
        
        root = BasicModelNode.create(new PageItem() {
            private TypedMap params = new SimpleTypedMap(Collections.<TypedKey<?>>emptySet());

            @Override
            public int compareTo(PageItem o) {
                return TessPageItem.CMP.compare(Page.this.extent, o.getBox());
            }
            
            @Override
            public BoundingBox getBox() {
                return Page.this.extent;
            }

            @Override
            public int getSeqNumber() {
                return 0;
            }

            @Override
            public TypedMap getProperties() {
                return params;
            }

            @Override
            public <X> X getProperty(TypedKey<X> key) throws PropertyException {
                return params.get(key);
            }
            
        }, PageItem.class, level);
    }

    public BoundingBox getExtent() {
        return extent;
    }

    @Override
    public List<PageModelNode<?>> getRoots()
    {
        return root.getChildren();
    }
    
    boolean isRootItem(PageItem item, TesseractLevel level)
    {
        // NOTE: for now, any Block level items are assumed to be root items. Eventually, 
        //       we'll neet to add some more structure to the block level items
        return level.getName().equals(TesseractLevelCatalog.BLOCK);
    }
    
    public <X extends PageItem> boolean add(X item, Class<X> type, TesseractLevel level)
    {
        if (isRootItem(item, level))
            root.add(item, type, level);
            
        return root.add(item, type, level);
    }
}
