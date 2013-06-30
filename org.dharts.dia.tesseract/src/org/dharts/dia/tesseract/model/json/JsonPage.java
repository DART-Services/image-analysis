/* File: JsonPage.java
 * Created: Mar 16, 2013
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

import java.util.List;

import org.dharts.dia.BoundingBox;
import org.dharts.dia.model.PageModelNode;
import org.dharts.dia.tesseract.model.Page;

/**
 * @author Neal Audenaert
 */
public class JsonPage {
    @SuppressWarnings("unused")
	private List<PageModelNode<?>> roots;
    BoundingBox box;
    
    /** Invoked by JSON parser. */
    JsonPage() {
        // TODO Auto-generated constructor stub
    }
    
    
    public JsonPage(Page p) {
        this.box = p.getExtent();
        roots = p.getRoots();
    }
    
    public BoundingBox getBoudningBox() {
        return box;
    }
    
    public void getBoudningBox(BoundingBox b) {
        box = b;
    }
    
    public Page asPage() {
        Page p = new Page(box);
        
        return p;
    }
}
