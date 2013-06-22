/* File: PageItemTests.java
 * Created: Mar 24, 2013
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
package org.dharts.dia.tesseract.test.model.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.dharts.dia.BoundingBox;
import org.dharts.dia.Level;
import org.dharts.dia.LevelProvider;
import org.dharts.dia.PageModelException;
import org.dharts.dia.SimpleBoundingBox;
import org.dharts.dia.props.TypedKey;
import org.dharts.dia.tesseract.BlockOrientation;
import org.dharts.dia.tesseract.PublicTypes;
import org.dharts.dia.tesseract.PublicTypes.Orientation;
import org.dharts.dia.tesseract.PublicTypes.PolyBlockType;
import org.dharts.dia.tesseract.PublicTypes.TextlineOrder;
import org.dharts.dia.tesseract.PublicTypes.WritingDirection;
import org.dharts.dia.tesseract.model.Block;
import org.dharts.dia.tesseract.model.TessPageItem;
import org.dharts.dia.tesseract.model.TesseractLevelCatalog;
import org.dharts.dia.tesseract.model.json.JsonItem;
import org.junit.Test;

/**
 */
public class PageItemTests {

    /**
     * 
     */
    public PageItemTests() {
        // TODO Auto-generated constructor stub
    }
    
    private <X extends Enum<X>> X getEnum(Map<String, Object> params, TypedKey<X> key) {
        Object value = params.get(key.getName());
        if (value != null) 
        {
            Class<X> type = key.getType();
            if (!(value instanceof String))
                throw new IllegalArgumentException("The stored value [" + value + "] is not a string. ");

            return Enum.valueOf(type, (String)value);
        }
        
        return null;
    }
    
    BoundingBox box = new SimpleBoundingBox(0, 0, 50, 50);
    PolyBlockType blockType = PublicTypes.PolyBlockType.CAPTION_TEXT;
    Orientation orientation = PublicTypes.Orientation.UP;
    WritingDirection direction = PublicTypes.WritingDirection.LEFT_TO_RIGHT;
    TextlineOrder order = PublicTypes.TextlineOrder.TOP_TO_BOTTOM;
    float angle = 0.23F;
    
    /**
     * @param para
     * @param jsonPara
     */
    private void evaluate(TessPageItem para, JsonItem jsonPara) {
        assertEquals(para.getBox(), jsonPara.getBoundingBox());
        assertEquals(para, jsonPara.toItem(TessPageItem.class));
    }

    void evaluate(Block block, JsonItem jsonBlock) 
    {
        assertNotNull(jsonBlock);
        
        Map<String, Object> params = jsonBlock.getParameters();
        assertEquals(blockType, getEnum(params, Block.BLOCK_TYPE));
        assertEquals(orientation, getEnum(params, Block.ORIENTATION));
        assertEquals(order, getEnum(params, Block.TEXTLINE_ORDER));
        assertEquals(direction, getEnum(params, Block.WRITING_DIRECTION));
        
        Object obj = params.get(Block.DESKEW_ANGLE.getName());
        if (!Number.class.isInstance(obj))
            throw new IllegalStateException("Invalide deskew angle [" + obj +"]");
        
        Number deskewAngle = (Number)obj;
        assertEquals(Float.valueOf(angle), Float.valueOf(deskewAngle.floatValue()));
        
        assertEquals(block.getBox(), jsonBlock.getBoundingBox());
        
        assertEquals(block, jsonBlock.toItem(Block.class));
    }
    
    @Test
    public void testCreateBlock() {
        BlockOrientation o = new BlockOrientation(orientation, direction, order, angle);
        Block block = new Block(blockType, o, box);
        JsonItem jsonBlock = new JsonItem(block);
        
        evaluate(block, jsonBlock);
    }        
        
    @Test
    public void testCreatePageItem() throws PageModelException {
        LevelProvider levelProvider = TesseractLevelCatalog.getInstance();
        Level paraLevel = levelProvider.getLevel(TesseractLevelCatalog.PARAGRAPH);
        
        TessPageItem para = new TessPageItem(box, paraLevel);
        JsonItem jsonPara = new JsonItem(para);
        
        evaluate(para, jsonPara);
    }

    @Test
    public void testSerializeBlock() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        
        BlockOrientation o = new BlockOrientation(orientation, direction, order, angle);
        Block block = new Block(blockType, o, box);
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(4096))
        {
            mapper.writeValue(baos, new JsonItem(block));
            String json = baos.toString("UTF-8");
            
            evaluate(block, mapper.readValue(json, JsonItem.class));
        }
    }
    
    public void testSerializeItem() throws PageModelException, IOException 
    {
        ObjectMapper mapper = new ObjectMapper();
        
        LevelProvider levelProvider = TesseractLevelCatalog.getInstance();
        Level paraLevel = levelProvider.getLevel(TesseractLevelCatalog.PARAGRAPH);
        
        TessPageItem para = new TessPageItem(box, paraLevel);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(4096))
        {
            mapper.writeValue(baos, new JsonItem(para));
            String json = baos.toString("UTF-8");
            
            evaluate(para, mapper.readValue(json, JsonItem.class));
        }
    }
}

