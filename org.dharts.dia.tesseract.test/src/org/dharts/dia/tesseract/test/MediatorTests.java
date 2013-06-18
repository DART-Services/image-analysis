/* File: MediatorTests.java
 * Created: 26 August 2012
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
package org.dharts.dia.tesseract.test;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.dharts.dia.BoundingBox;
import org.dharts.dia.tesseract.ImageAnalyzer;
import org.dharts.dia.tesseract.LayoutIterator;
import org.dharts.dia.tesseract.TesseractException;
import org.dharts.dia.tesseract.model.TesseractLevel;
import org.dharts.dia.tesseract.model.TesseractLevelCatalog;
import org.junit.Test;

public class MediatorTests extends TesseractTestFixture {
    @Test
    public void testAnalyzer() throws IOException, TesseractException {

        ImageAnalyzer analyzer = getImageAnalyzer(simpleImageFile);
        assertNotNull("Failed to create analyzer", analyzer);
        analyzer.close();
    }
    
    @Test
    public void testPageIterator() throws Exception {
        ImageAnalyzer analyzer = getImageAnalyzer(poetryImageFile);
        
        LayoutIterator iterator = analyzer.analyzeLayout();
        assertNotNull("Failed to create iterator", iterator);

        TesseractLevel textline = levels.getLevel(TesseractLevelCatalog.TEXTLINE);
        do {
            @SuppressWarnings("unused")
            BoundingBox box = iterator.getBoundingBox(textline);
        } while (iterator.next(textline));
        
        iterator.close();
        analyzer.close();
    }
}
