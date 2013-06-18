/* File: BlockIterator.java
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

import java.util.NoSuchElementException;

import org.apache.log4j.Logger;
import org.dharts.dia.BoundingBox;
import org.dharts.dia.PageItem;
import org.dharts.dia.PageItemIterator;
import org.dharts.dia.tesseract.LayoutIterator;

/**
 * @author Neal Audenaert
 */
public class TessPageItemIterator<I extends PageItem> implements PageItemIterator<I> {
    private static final Logger LOGGER = Logger.getLogger(TessPageItemIterator.class);
    
    private final LayoutIterator iterator;
    
    private I nextBlock = null;
    private boolean endOfPage = false;

    private final PageItemFactory<I> itemFactory;
    private final TesseractLevel level;

    
    public TessPageItemIterator(LayoutIterator iterator, PageItemFactory<I> itemFactory) {
        this.itemFactory = itemFactory;
        this.level = itemFactory.getLevel();
        this.iterator = iterator.copy();
        initNextItem(false);
    }
     
    private void initNextItem(boolean advance) {
        if (this.endOfPage) {
            nextBlock = null;
            return;
        }
        
        if (advance) {
            if (!iterator.next(level)) {
                this.endOfPage = true;
                iterator.close();
                return;
            }
        }

        try {
            BoundingBox box = iterator.getBoundingBox(level);
            if (box == null) {
                initNextItem(true);
            }
            
            nextBlock = itemFactory.getItem(iterator); // new Block(iterator.getBlockType(), iterator.getOrientation(), box);
        } catch (Exception te) {
            LOGGER.error("Could not initialize iterator.", te);
            this.nextBlock = null;
        }
    }
    
    @Override
    public boolean hasNext() {
        return nextBlock != null;
    }

    @Override
    public I next() {
        if (nextBlock == null) 
            throw new NoSuchElementException();
        
        I next = this.nextBlock;
        initNextItem(true);
        return next;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Cannot remove blocks from the page model");
    }
    
    public static interface PageItemFactory<I extends PageItem> {
        
        TesseractLevel getLevel();
        
        Class<I> getType();
        
        I getItem(LayoutIterator iterator);
    }
}
