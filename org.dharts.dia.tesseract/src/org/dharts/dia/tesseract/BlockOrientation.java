/* File: BlockOrientation.java
 * Created: Mar 17, 2013
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
package org.dharts.dia.tesseract;

import org.dharts.dia.tesseract.PublicTypes.Orientation;
import org.dharts.dia.tesseract.PublicTypes.TextlineOrder;
import org.dharts.dia.tesseract.PublicTypes.WritingDirection;

/**
 * Defines the block orientation of a particular feature on the page, including the 
 * orientation of the block, the writing direction within the block, the order in which
 * lines of text are read and the skew angle within the block in radians.
 * 
 * @author Neal Audenaert
 */
public class BlockOrientation {
    // TODO evaluate and, if appropriate, move enums from PublicTypes into this class.
    //      possibly move to the main DIA package, making only the mapping between these 
    //          enums and the Tesseract API part of the Tesseract package
    // NOTE Only if these features (orientation, writing direction, textline order and deskew 
    //      angle) make sense across different OCR/Layout Analysis implementations.
    
    public final Orientation orientation;
    public final WritingDirection writingDirection;
    public final TextlineOrder textlineOrder;
    
    /** The number of radians required to rotate the block anti-clockwise for it to be 
     *  level. <code>-PI / 4 <= deskewAngl <= PI / 4</code>. This is calculated after 
     *  the block is rotated so that the text orientation is upright. */
    public final float deskewAnge;
    
    public BlockOrientation(Orientation orientation, 
                            WritingDirection writingDirection, 
                            TextlineOrder order, 
                            float angle) {
        this.orientation = orientation;
        this.writingDirection = writingDirection;
        this.textlineOrder = order;
        this.deskewAnge = angle;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj.getClass() != BlockOrientation.class)
            return false;
        
        BlockOrientation bo = BlockOrientation.class.cast(obj);
        return orientation == bo.orientation
            && writingDirection == bo.writingDirection
            && textlineOrder == bo.textlineOrder
            && deskewAnge == bo.deskewAnge;
    }
    
    @Override
    public int hashCode() {
        int result = 17;

        result = 37 * result + orientation.value;
        result = 37 * result + writingDirection.value;
        result = 37 * result + textlineOrder.value;
        result = 37 * result + Float.floatToIntBits(deskewAnge);
        
        return result;
    }
}