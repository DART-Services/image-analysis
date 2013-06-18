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
package org.dharts.dia.tesseract.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.dharts.dia.BoundingBox;
import org.dharts.dia.Level;
import org.dharts.dia.PageItem;
import org.dharts.dia.SimpleBoxComparator;
import org.dharts.dia.props.PropertyException;
import org.dharts.dia.props.SimpleTypedKey;
import org.dharts.dia.props.SimpleTypedMap;
import org.dharts.dia.props.TypedKey;
import org.dharts.dia.props.TypedMap;


public class TessPageItem implements PageItem {
    final static SimpleBoxComparator CMP = new SimpleBoxComparator();
    
    public static final TypedKey<String> LEVEL = SimpleTypedKey.create("level", String.class);
    
    public final static Collection<TypedKey<?>> DEFINED_PROPERTIES = 
            Collections.unmodifiableCollection(Arrays.<TypedKey<?>>asList(LEVEL));
    
    private final Level level;                      
    private final BoundingBox box;

    private final SimpleTypedMap params;

    public TessPageItem(BoundingBox box, Level level) {
        this.box = box;
        this.level = level;
        
        params = new SimpleTypedMap(DEFINED_PROPERTIES);
    }

    @Override
    public BoundingBox getBox() {
        return box;
    }
    
    @Override
    public TypedMap getProperties() {
        return params;
    }

    @Override
    public <X> X getProperty(TypedKey<X> key) throws PropertyException {
        return params.get(key);
    }

    @Override
    public int getSeqNumber() {
        // TODO Auto-generated method stub
        return 0;
    }

    // FIXME almost certainly not needed
    public Level getLevel() {
        return level;
    }
    
    @Override
    public String toString() {
        return level.getName() + ": " + box;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TessPageItem)
        {
            TessPageItem item = (TessPageItem)obj;
            return level.equals(item.level) && box.equals(item.box);
        }
        
        return false;
    }
    
    @Override
    public int hashCode() {
        int result = 17;
        
        result = result * 37 + level.hashCode();
        result = result * 37 + box.hashCode();
        
        return result; 
    }

    @Override
    public int compareTo(PageItem o) {
        return CMP.compare(box, o.getBox());
    }
}