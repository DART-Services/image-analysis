/* File: TesseractPageAnalyzer.java
 * Created: Feb 9, 2013
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

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

import org.dharts.dia.BoundingBox;
import org.dharts.dia.PageItem;
import org.dharts.dia.PageItemIterator;
import org.dharts.dia.PageModel;
import org.dharts.dia.PageModelException;
import org.dharts.dia.SimpleBoundingBox;
import org.dharts.dia.tesseract.BlockOrientation;
import org.dharts.dia.tesseract.ImageAnalyzer;
import org.dharts.dia.tesseract.ImageAnalyzerFactory;
import org.dharts.dia.tesseract.LayoutIterator;
import org.dharts.dia.tesseract.PublicTypes.PolyBlockType;
import org.dharts.dia.tesseract.TesseractException;
import org.dharts.dia.tesseract.model.TessPageItemIterator.PageItemFactory;

/**
 * @author Neal Audenaert
 */
public class TesseractPageAnalyzer {
    // NOTE: is intended to be confined to a single thread, used and disposed
    private static final TesseractLevelCatalog levelProvider = TesseractLevelCatalog.getInstance();
    
    private final ImageAnalyzerFactory factory;

    /**
     * 
     * @param factory The {@link ImageAnalyzerFactory} to be used to process document images.
     *      This class assumes that it has exclusive access of the supplied factory until the 
     *      class is disposed. Note that it will not close the factory on dispose.
     */
    public TesseractPageAnalyzer(ImageAnalyzerFactory factory) {
        this.factory = factory;
        
        // TODO supply a ModelBuilderFactory
    }
    
    public PageModel analyze(BufferedImage image) throws PageModelException 
    {
        BoundingBox box = new SimpleBoundingBox(0, 0, image.getWidth(), image.getHeight());
        Page model = new Page(box);
        
        ImageAnalyzer analyzer = null;
        LayoutIterator layout = null;
        try {
            analyzer = factory.createImageAnalyzer(image);
            layout = analyzer.analyzeLayout();
                        
            for (PageItemFactory<?> f : getFactories())
            {
                process(layout, model, f);
            }
            
            return model;
        } catch (TesseractException te) {
            throw new PageModelException("Failed to parse document image", te);
        } finally {
            try {
                if (layout != null)
                    layout.close();
                if (analyzer != null)
                    analyzer.close();
            } catch (TesseractException te) {
                throw new IllegalStateException("Could not close image analyzer", te);
            }
        }
    }

    private <X extends PageItem> void process(LayoutIterator layout, Page pageModel, 
                                              PageItemFactory<X> factory) 
    {
        PageItemIterator<X> iterator = new TessPageItemIterator<>(layout, factory);
        while (iterator.hasNext()) {
            pageModel.add(iterator.next(), factory.getType(), factory.getLevel());
        }
    }

    /**
     * @return
     */
    private List<PageItemFactory<?>> getFactories() {
        // TODO rename method
        List<PageItemFactory<?>> factories = Arrays.asList(
                new BlockFactory(), new ParagraphFactory(), new TextlineFactory(), new WordFactory(), new SymbolFactory());
        return factories;
    }
    
    private static TesseractLevel getTessearctLevel(String name) {
        try {
            return levelProvider.getLevel(name);
        } catch (PageModelException e) {
            throw new IllegalStateException("Could not retrieve level [" + name + "]", e);
        }
    }
    
    private static class BlockFactory implements PageItemFactory<Block>
    {
        @Override
        public TesseractLevel getLevel() {
            return getTessearctLevel(TesseractLevelCatalog.BLOCK);
        }

        @Override
        public Class<Block> getType() {
            return Block.class;
        }
        
        @Override
        public Block getItem(LayoutIterator iterator) {
            PolyBlockType blockType = iterator.getBlockType();
            BlockOrientation orientation = iterator.getOrientation();
            BoundingBox box = iterator.getBoundingBox(getLevel());
            
            return new Block(blockType, orientation, box);
        }
    }
    
    private static class ParagraphFactory implements PageItemFactory<TessPageItem>
    {
        @Override
        public TesseractLevel getLevel() {
            return getTessearctLevel(TesseractLevelCatalog.PARAGRAPH);
        }
        
        @Override
        public Class<TessPageItem> getType() {
            return TessPageItem.class;
        }
        
        @Override
        public TessPageItem getItem(LayoutIterator iterator) {
            BoundingBox box = iterator.getBoundingBox(getLevel());
            return new TessPageItem(box, getLevel());
        }
    }
    
    private static class TextlineFactory implements PageItemFactory<TessPageItem>
    {
        @Override
        public TesseractLevel getLevel() {
            return getTessearctLevel(TesseractLevelCatalog.TEXTLINE);
        }
        
        @Override
        public Class<TessPageItem> getType() {
            return TessPageItem.class;
        }
        
        @Override
        public TessPageItem getItem(LayoutIterator iterator) {
            BoundingBox box = iterator.getBoundingBox(getLevel());
            return new TessPageItem(box, getLevel());
        }
    }
    
    private static class WordFactory implements PageItemFactory<TessPageItem>
    {
        @Override
        public TesseractLevel getLevel() {
            return getTessearctLevel(TesseractLevelCatalog.WORD);
        }
        
        @Override
        public Class<TessPageItem> getType() {
            return TessPageItem.class;
        }
        
        @Override
        public TessPageItem getItem(LayoutIterator iterator) {
            BoundingBox box = iterator.getBoundingBox(getLevel());
            return new TessPageItem(box, getLevel());
        }
    }
    
    private static class SymbolFactory implements PageItemFactory<TessPageItem>
    {
        @Override
        public TesseractLevel getLevel() {
            return getTessearctLevel(TesseractLevelCatalog.WORD);
        }
        
        @Override
        public Class<TessPageItem> getType() {
            return TessPageItem.class;
        }
        
        @Override
        public TessPageItem getItem(LayoutIterator iterator) {
            BoundingBox box = iterator.getBoundingBox(getLevel());
            return new TessPageItem(box, getLevel());
        }
    }
}
