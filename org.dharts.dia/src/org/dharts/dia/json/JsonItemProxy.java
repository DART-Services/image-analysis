/* File: JsonItemProxy.java
 * Created: Mar 23, 2013
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
package org.dharts.dia.json;

import java.util.Map;

import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.dharts.dia.BoundingBox;
import org.dharts.dia.PageItem;
import org.dharts.dia.PageModel;

/**
 * @author Neal Audenaert
 *
 */
public abstract class JsonItemProxy {
    // HACK: this should be extracted from the main dia bundle as it adds external 
    //       dependencies (to Jackson)
    protected BoundingBox box;
    private Map<String, Object> params;
    
    protected JsonItemStrategy type;

    // called by Jackson
    protected JsonItemProxy()
    {
    }
    
    /**
     * @param block
     * @param box
     * @param level
     * @param params
     */
    public JsonItemProxy(PageItem item, JsonItemStrategy type) {
        this.type = type;
        this.box = item.getBox();
        this.params = type.getParameters(item);
    }

    /**
     * @return An identifier for the concrete implementation of {@link PageItem} represented 
     *      by this serialized form. Note that we use a normal string-based type rather than a 
     *      fully qualified Java class name.
     */
    public abstract String getType(); 

    public abstract void setType(String type);
    
    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
    public BoundingBox getBoundingBox() {
        return box;
    }
    
    public void setBoundingBox(BoundingBox box) {
        this.box = box;
    }
    
    public Map<String, Object> getParameters() {
        return params;
    }
    
    public void setParameters(Map<String, Object> params) {
        this.params = params;
    }
    
    public <X extends PageItem> X toItem(Class<X> clazz) {
        return clazz.cast(type.toPageItem(this));
    }

    /**
     * Defines a strategy for converting between {@link PageItem} instances and their 
     * corresponding {@link JsonItemProxy} representatoin. This consists chiefly of two 
     * functions. First, instantiating the appropriate implementation class and second, 
     * translating any custom state defined by the implementation into (or from) the map of 
     * JSON-serializable parameters defined by {@link JsonItemProxy#getParameters()}.
     */
    public static interface JsonItemStrategy {
        
        /**
         * Converts a {@link JsonItemProxy} into the corresponding {@link PageItem} 
         * defined by a particular {@link PageModel} implementation. 
         * 
         * @param proxy The proxy object to be converted into a {@link PageItem}
         * @return
         */
        PageItem toPageItem(JsonItemProxy proxy);
        
        /**
         * Converts any persistent internal state of the supplied page item into a 
         * JSON-serializable map.
         * 
         * @param item The item whose properties should be retrieved.
         * @return A JSON-serializable map representing the internal state of the supplied 
         *      page item. 
         * @throws IllegalArgumentException If the supplied item is not of the type that can
         *      be handled by this strategy.
         */
        Map<String, Object> getParameters(PageItem item) throws IllegalArgumentException;
    }

}