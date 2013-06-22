/* File: PageBlock.java
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

import org.dharts.dia.BoundingBox;
import org.dharts.dia.PageItem;
import org.dharts.dia.props.PropertyException;
import org.dharts.dia.props.SimpleTypedKey;
import org.dharts.dia.props.SimpleTypedMap;
import org.dharts.dia.props.TypedKey;
import org.dharts.dia.props.TypedMap;
import org.dharts.dia.tesseract.BlockOrientation;
import org.dharts.dia.tesseract.PublicTypes.Orientation;
import org.dharts.dia.tesseract.PublicTypes.PolyBlockType;
import org.dharts.dia.tesseract.PublicTypes.TextlineOrder;
import org.dharts.dia.tesseract.PublicTypes.WritingDirection;
import org.dharts.dia.tesseract.TesseractException;

/**
 * Represents the largest scale feature of a page, for example, a column or other major 
 * division of text. 
 *  
 * @author Neal Audenaert
 */
public class Block implements PageItem {
    // FIXME need to make this structure fully generic - a PageItem should have a 
    //       defined set of typed parameters and be able to instantiate and return
    //       those parameters
    public final static TypedKey<PolyBlockType> BLOCK_TYPE = SimpleTypedKey.create("block type", PolyBlockType.class);
    public final static TypedKey<Orientation> ORIENTATION = SimpleTypedKey.create("orientation", Orientation.class);
    public final static TypedKey<WritingDirection> WRITING_DIRECTION = SimpleTypedKey.create("writing direction", WritingDirection.class);
    public final static TypedKey<TextlineOrder> TEXTLINE_ORDER = SimpleTypedKey.create("textline order", TextlineOrder.class);
    public final static TypedKey<Float> DESKEW_ANGLE = SimpleTypedKey.create("deskew angle", Float.class);
    
    public final static  Collection<TypedKey<?>> DEFINED_PROPERTIES = Arrays.<TypedKey<?>>asList(
            BLOCK_TYPE, ORIENTATION, WRITING_DIRECTION, TEXTLINE_ORDER, DESKEW_ANGLE);
    
    public static Block create(BoundingBox box, TypedMap params) throws TesseractException
    {
        try 
        {
            PolyBlockType type = params.get(BLOCK_TYPE);
            Orientation orientation = params.get(ORIENTATION);
            WritingDirection direction = params.get(WRITING_DIRECTION);
            TextlineOrder lineOrder = params.get(TEXTLINE_ORDER);
            float angle = params.contains(DESKEW_ANGLE) ? params.get(DESKEW_ANGLE).floatValue() : 0.0F;
            
            BlockOrientation blockOrientation = 
                    new BlockOrientation(orientation, direction, lineOrder, angle);
            
            return new Block(type, blockOrientation, box);
        } 
        catch (PropertyException pe) 
        {
            throw new TesseractException("Could not restore block instance from parameters", pe);
        }
    }

    /**
     * @param type
     * @param orientation
     * @return
     */
    private static SimpleTypedMap buildProperties(PolyBlockType type, BlockOrientation orientation) {
        SimpleTypedMap params = new SimpleTypedMap(DEFINED_PROPERTIES);
        try 
        {
            params.set(BLOCK_TYPE, type);
            params.set(ORIENTATION, orientation.orientation);
            params.set(WRITING_DIRECTION, orientation.writingDirection);
            params.set(TEXTLINE_ORDER, orientation.textlineOrder);
            params.set(DESKEW_ANGLE, Float.valueOf(orientation.deskewAnge));
        } 
        catch (Exception ex)
        {
            throw new IllegalStateException("Failed to construct block", ex);
        }
        
        return params;
    }

    private final SimpleTypedMap params;

    private final int seqNo = 0;        // HACK place holder for now. Need to implement
    private final PolyBlockType type;
    private final BlockOrientation orientation;
    private final BoundingBox box;
    
    public Block(PolyBlockType type, BlockOrientation orientation, BoundingBox box) {
        this.box = box;
        this.type = type;
        this.orientation = orientation;

        this.params = buildProperties(type, orientation);
    }

    @Override
    public BoundingBox getBox() {
        return box;
    }

    @Override
    public TypedMap getProperties() {
        return this.params;
    }
    
    @Override
    public <X> X getProperty(TypedKey<X> key)  
    {
        try {
            return params.get(key);
        } catch (PropertyException e) {
            throw new IllegalArgumentException("Could not get value [" + key + "]", e);
        }
    }
    
    @Override
    public int getSeqNumber() {
        return seqNo;
    }
    
    /**
     * @return the type
     */
    public PolyBlockType getType() {
        return type;
    }

    /**
     * @return the orientation
     */
    public BlockOrientation getOrientation() {
        return orientation;
    }
    
    @Override
    public String toString() {
        return "Block [" + type.toString() + "]: " + box;
    }

    @Override
    public boolean equals(Object obj) {
        // TODO need to track the page this belongs to?
        if (obj == null)
            return false;
        if (obj.getClass() != Block.class)
            return super.equals(obj);
        
        Block b = Block.class.cast(obj);
        return b.type.equals(type) 
            && b.orientation.equals(orientation)
            && b.box.equals(box);
    }
    
    @Override
    public int hashCode() {
        int result = 17;
        
        result = 37 * result + type.value;
        result = 37 * result + orientation.hashCode();
        result = 37 * result + box.hashCode();
        
        return result;
    }

    /**
     * 
     * <p>Note: this class has a natural order that is inconsistent with equals. Ordering 
     * takes into account only the page reading order and does not consider any other 
     * features of this block.
     */
    @Override
    public int compareTo(PageItem o) {
        return TessPageItem.CMP.compare(box, o.getBox());
    }
}