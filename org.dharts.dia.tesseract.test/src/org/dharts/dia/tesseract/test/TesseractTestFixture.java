/* File: TesseractTestFixture.java
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
package org.dharts.dia.tesseract.test;

import static org.junit.Assert.assertFalse;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.dharts.dia.tesseract.ImageAnalyzer;
import org.dharts.dia.tesseract.ImageAnalyzerFactory;
import org.dharts.dia.tesseract.TesseractException;
import org.dharts.dia.tesseract.model.TesseractLevelCatalog;
import org.junit.After;
import org.junit.Before;

/**
 * @author Neal Audenaert
 *
 */
public class TesseractTestFixture {

    protected final static File tessDataPath = new File(".");
    protected final static File simpleImageFile = new File("res/testing/simple.png");
    protected final static File poetryImageFile = new File("res/testing/simple_poetry.png");
    
    protected TesseractLevelCatalog levels = TesseractLevelCatalog.getInstance();
    protected ImageAnalyzerFactory factory;

    @Before
    public void setup() {
        try {
            factory = ImageAnalyzerFactory.createFactory(tessDataPath);
        } catch (IOException | TesseractException e) {
            throw new IllegalStateException("Could not ", e);
        }
    }
    
    @After
    public void tearDown() {
        if ((factory != null) && !factory.isClosed()) {
            try {
                factory.close();
            } catch (Exception ex) {
                assertFalse("Failed to close non-null factory: " + ex.getMessage(), true);
            }
        }
    }

    protected ImageAnalyzer getImageAnalyzer(File f) throws TesseractException, IOException {
        BufferedImage image = ImageIO.read(f);
        return factory.createImageAnalyzer(image);
    }
}