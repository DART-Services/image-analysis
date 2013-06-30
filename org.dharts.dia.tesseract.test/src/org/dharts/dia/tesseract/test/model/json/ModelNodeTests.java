/* File: ModelNodeTests.java
 * Created: Mar 27, 2013
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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.dharts.dia.BoundingBox;
import org.dharts.dia.BoxUtils;
import org.dharts.dia.SimpleBoundingBox;
import org.dharts.dia.model.BasicModelNode;
import org.dharts.dia.model.Level;
import org.dharts.dia.model.LevelProvider;
import org.dharts.dia.model.PageModelException;
import org.dharts.dia.model.PageModelNode;
import org.dharts.dia.tesseract.model.TessPageItem;
import org.dharts.dia.tesseract.model.TesseractLevelCatalog;

/**
 *
 */
public class ModelNodeTests {

    LevelProvider levelProvider = TesseractLevelCatalog.getInstance();
    Random random = new Random(System.currentTimeMillis());
    int seqNo = 1;
    
    public ModelNodeTests() {
        // TODO Auto-generated constructor stub
    }
    
    PageModelNode<TessPageItem> generateCharacter(int x, int y, int lineHeight) 
    {
        int widthBase = 20;
        int widthVar = 8;
        long width = Math.round(widthBase + random.nextDouble() * widthVar);
        
        long height = lineHeight;
        Double t = Double.valueOf(Math.floor(Math.random() * 3));
        switch (t.intValue())
        {
        case 0:
            height = Math.round(height * .5);
            break;
        case 1:
            height = Math.round(height * .7);
            break;
        case 2: 
            height = Math.round(height * .85);
            break;
        default:
            throw new IllegalStateException();
        }
        
        int left = x;
        int top = (int)(y + (lineHeight - height));
        int right = (int)(x + width);
        int bottom = (y + lineHeight);
        
        BoundingBox box = new SimpleBoundingBox(left, top, right, bottom);
        
        try {
            Level level = levelProvider.getLevel(TesseractLevelCatalog.SYMBOL);
            TessPageItem item = new TessPageItem(box, level);
            
            return BasicModelNode.create(item, TessPageItem.class, level, seqNo++);
        } catch (PageModelException e) {
            throw new IllegalStateException(e);
        }
    }
    
    double norm(double mean, double stdev)
    {
        return stdev * random.nextGaussian() + mean; 
        
    }
    
    BasicModelNode<TessPageItem> generateWord(int x, int y, int lineHeight) 
    {
        int chCount = (int)Math.round(norm(4, 5));
        chCount = Math.min(15, Math.max(1, chCount));
        
        BoundingBox box = new SimpleBoundingBox(x, y, x + 20, y + lineHeight);
        List<PageModelNode<TessPageItem>> items = new ArrayList<>();
        for (int i = 0; i < chCount; i++) {
            PageModelNode<TessPageItem> node = generateCharacter(x, y, lineHeight);
            items.add(node);
            
            box = BoxUtils.union(box, node.getBox()); 
        }
        
        try {
            Level level = levelProvider.getLevel(TesseractLevelCatalog.WORD);
            TessPageItem item = new TessPageItem(box, level);
            
            BasicModelNode<TessPageItem> result = 
                    BasicModelNode.create(item, TessPageItem.class, level, seqNo++);
            for (PageModelNode<TessPageItem> node: items)
            {
                result.add(node.getItem(), TessPageItem.class, node.getLevel());
            }
            
            return result;
        } catch (PageModelException e) {
            throw new IllegalStateException(e);
        }
        
    }
}
