/* File: JsonItem.java
 * Created: Mar 18, 2013
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
package org.dharts.dia.tesseract.model.json;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.dharts.dia.BoundingBox;
import org.dharts.dia.LevelProvider;
import org.dharts.dia.PageItem;
import org.dharts.dia.PageModelException;
import org.dharts.dia.json.JsonItemProxy;
import org.dharts.dia.props.PropertyException;
import org.dharts.dia.props.SimpleTypedMap;
import org.dharts.dia.props.TypedKey;
import org.dharts.dia.tesseract.BlockOrientation;
import org.dharts.dia.tesseract.TesseractException;
import org.dharts.dia.tesseract.model.Block;
import org.dharts.dia.tesseract.model.TessPageItem;
import org.dharts.dia.tesseract.model.TesseractLevelCatalog;

public class JsonItem extends JsonItemProxy {

    public JsonItem() {
        
    }
    
    public JsonItem(Block block) {
        super(block, TessItemStrategy.BLOCK);
    }
    
    public JsonItem(TessPageItem item) {
        super(item, TessItemStrategy.PAGE_ITEM);
    }

    @Override
    public String getType() {
        return type.toString();
    }

    @Override
    public void setType(String type) {
        this.type = TessItemStrategy.valueOf(type);
        if (type == null)
            throw new IllegalArgumentException("Invalid item type [" + type + "]");
    }
    
    private static LevelProvider catalog = TesseractLevelCatalog.getInstance();
    
    static <T extends Enum<T>> T getEnum(JsonItemProxy item, TypedKey<T> key) 
    {
        Map<String, Object> params = item.getParameters();
        Object value = params.get(key.getName());
        String errmsg = "Invalid value for'" + key.getName() + "': ";
        if (!String.class.isInstance(value))
            throw new IllegalStateException(errmsg + "Not a string - [" + value + "]");
        
        if (value == null)
            return null;
        
        return Enum.valueOf(key.getType(), (String)value);
    }
    
    static <T extends Number> Number getNumber(JsonItemProxy item, TypedKey<T> key)
    {
        Map<String, Object> params = item.getParameters();
        Object value = params.get(key.getName());
        String errmsg = "Invalid value for'" + key.getName() + "': ";
        if (!Number.class.isInstance(value))
            throw new IllegalStateException(errmsg + "Not a number - [" + value + "]");
        
        if (value == null)
            return Double.valueOf(0.0);
        
        return Number.class.cast(value);
    }
    
    private enum TessItemStrategy implements JsonItemStrategy
    {
        BLOCK {
            @Override
            public Map<String, Object> getParameters(PageItem item)
            {
                if (!Block.class.isInstance(item))
                    throw new IllegalArgumentException("Invalid page item. Expected a Block");
                
                Block block = (Block)item;
                BlockOrientation orientation = block.getOrientation();
                
                Map<String, Object> params = new HashMap<>();
                params.put(Block.BLOCK_TYPE.getName(), block.getType().toString());
                params.put(Block.ORIENTATION.getName(), orientation.orientation.toString());
                params.put(Block.TEXTLINE_ORDER.getName(), orientation.textlineOrder.toString());
                params.put(Block.WRITING_DIRECTION.getName(), orientation.writingDirection.toString());
                params.put(Block.DESKEW_ANGLE.getName(), Float.valueOf(orientation.deskewAnge));
                
                return Collections.unmodifiableMap(params);
            }
            
            @Override
            public PageItem toPageItem(JsonItemProxy item) {
                SimpleTypedMap params = new SimpleTypedMap(Block.DEFINED_PROPERTIES);
                
                try {
                    params.set(Block.BLOCK_TYPE, getEnum(item, Block.BLOCK_TYPE));
                    params.set(Block.ORIENTATION, getEnum(item, Block.ORIENTATION));
                    params.set(Block.TEXTLINE_ORDER, getEnum(item, Block.TEXTLINE_ORDER));
                    params.set(Block.WRITING_DIRECTION, getEnum(item, Block.WRITING_DIRECTION));
                    params.set(Block.DESKEW_ANGLE, getNumber(item, Block.DESKEW_ANGLE).floatValue());
                    
                    return Block.create(item.getBoundingBox(), params);
                } 
                catch (PropertyException | TesseractException ex) 
                {
                    throw new IllegalStateException("Could not restore instance for [" + item + "]", ex);
                }
            }
        },
        
        PAGE_ITEM {
            @Override
            public Map<String, Object> getParameters(PageItem item) {
                if (!TessPageItem.class.isInstance(item))
                    throw new IllegalArgumentException("Invalid page item. Expected a TessPageItem");
                
                String level = TessPageItem.class.cast(item).getLevel().getName();
                Map<String, Object> params = new HashMap<>();
                params.put(TessPageItem.LEVEL.getName(), level);

                return params;
            }
            
            @Override
            public PageItem toPageItem(JsonItemProxy item)  {
                Object obj = item.getParameters().get(TessPageItem.LEVEL.getName());
                if (!String.class.isInstance(obj))
                    throw new IllegalStateException("Invalid item level [" + obj +"]");
                
                String level = String.class.cast(obj);
                BoundingBox boundingBox = item.getBoundingBox();
                try {
                    return new TessPageItem(boundingBox, catalog.getLevel(level));
                } catch (PageModelException e) {
                    throw new IllegalStateException("Cannot restore item level [" + level + "]", e);
                }
            }
        };
    }
}