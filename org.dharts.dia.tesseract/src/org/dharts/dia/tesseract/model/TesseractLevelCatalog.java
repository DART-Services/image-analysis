/* File: TesseractLevelCatalog.java
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
package org.dharts.dia.tesseract.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;
import org.dharts.dia.model.Level;
import org.dharts.dia.model.LevelProvider;
import org.dharts.dia.model.PageModelException;
import org.dharts.dia.tesseract.tess4j.TessAPI;

/**
 * @author Neal Audenaert
 */
public class TesseractLevelCatalog implements LevelProvider 
{
    private HashMap<String, TesseractLevel> levels = new HashMap<>();
    
    TesseractLevelCatalog(HashMap<String, TesseractLevel> tLevels) {
        this.levels = tLevels;
    }
    
    @Override
    public Collection<String> getLevels() {
        return Collections.unmodifiableCollection(new HashSet<>(levels.keySet()));
    }
    
    @Override
    public boolean hasLevel(String name)
    {
        return levels.containsKey(name);
    }
    
    @Override
    public TesseractLevel getLevel(String name) throws PageModelException 
    {
        if (!hasLevel(name))
            throw new PageModelException("No such level [" + name + "]");
        
        return levels.get(name);
    }
    
    //======================================================================================
    // JSON MARSHALLING
    //======================================================================================
    private static final ObjectMapper mapper = new ObjectMapper();
    
    // TODO seems like this should be an enum
    public static final String PAGE = "Page";
    public static final String BLOCK = "Block";
    public static final String PARAGRAPH = "Paragraph";
    public static final String TEXTLINE = "Textline";
    public static final String WORD = "Word";
    public static final String SYMBOL = "Symbol";
    
    static {
        mapper.configure(Feature.INDENT_OUTPUT, true);
    }
    
    private static TesseractLevelCatalog instance;
    
    public synchronized static TesseractLevelCatalog getInstance() 
    {
        // HACK: we should probably move this from being a singleton to being something that
        //       that we can serialize and restore.
        if (instance != null)
            return instance;
        
        JsonCatalog catalog = new JsonCatalog();
        
        LevelDefinition page = catalog.defineLevel(PAGE, TessAPI.TessPageIteratorLevel.RIL_BLOCK);
        LevelDefinition block = catalog.defineLevel(BLOCK, TessAPI.TessPageIteratorLevel.RIL_BLOCK);
        LevelDefinition paragraph = catalog.defineLevel(PARAGRAPH, TessAPI.TessPageIteratorLevel.RIL_PARA);
        LevelDefinition textline = catalog.defineLevel(TEXTLINE, TessAPI.TessPageIteratorLevel.RIL_TEXTLINE);
        LevelDefinition word = catalog.defineLevel(WORD, TessAPI.TessPageIteratorLevel.RIL_WORD);
        LevelDefinition symbol = catalog.defineLevel(SYMBOL, TessAPI.TessPageIteratorLevel.RIL_SYMBOL);
        
        catalog.defineChildren(page, block);
        catalog.defineChildren(block, paragraph);
        catalog.defineChildren(paragraph, textline);
        catalog.defineChildren(textline, word);
        catalog.defineChildren(word, symbol);
        
        try {
            instance = catalog.build();
        } catch (PageModelException e) {
            throw new IllegalStateException("Failed to initialize TesseractLevelCatalog", e);
        }
        
        return instance;
    }
    
    /**
     * Restores a {@link TesseractLevelCatalog} from its JSON-serialized form.
     * 
     * @param json A serialized representation of the {@link TesseractLevelCatalog} to restore
     * @return The restored {@link TesseractLevelCatalog}
     * @throws PageModelException If the supplied JSON is invalid
     */
    public static TesseractLevelCatalog restore(String json) throws PageModelException 
    {
        try (InputStream is = new ByteArrayInputStream(json.getBytes("UTF-8"))) {
            synchronized (mapper) 
            {
                JsonCatalog jsonCatalog = mapper.readValue(is, JsonCatalog.class);
                return jsonCatalog.build();
            }
        } catch (IOException e) {
            throw new PageModelException("Could not restore catalog from supplied JSON value", e);
        } 
    }
    
    public static String toJSON(TesseractLevelCatalog catalog) 
    {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(4096))  {
            synchronized (mapper) 
            {
                mapper.writeValue(baos, new JsonCatalog(catalog));
            }
            
            return new String(baos.toByteArray(), "UTF-8");
        } catch (IOException | PageModelException e) {
            throw new IllegalStateException("Could not serialize catalog [" + catalog + "]", e);
        } 
    }

    static class JsonCatalog
    {
        private Map<String, LevelDefinition> levels = new HashMap<>();

        JsonCatalog()
        {
            
        }
        
        JsonCatalog(TesseractLevelCatalog tlc) throws PageModelException
        {
            for (String name : tlc.getLevels())
            {
                TesseractLevel level = tlc.getLevel(name);
                LevelDefinition defn = new LevelDefinition(level.getName(), level.getTessCode());
                
                ArrayList<String> children = new ArrayList<>();
                for (Level child : level.getAcceptibleChildren())
                {
                    children.add(child.getName());
                }
                
                defn.setChildren(children);
                levels.put(name, defn);
            }
        }
        
        /** 
         * Accessor to provide the levels that are defined for this catalog. Called during 
         * JSON serialization. 
         */
        public List<LevelDefinition> getDefinedLevels() 
        {
            TreeSet<LevelDefinition> defns = new TreeSet<>(new Comparator<LevelDefinition>() {
                @Override
                public int compare(LevelDefinition l1, LevelDefinition l2) {
                    int result = Integer.compare(l1.tessCode, l2.tessCode);
                    return result != 0 ? result : l1.getName().compareTo(l2.name);
                }
            });
            
            defns.addAll(levels.values());
            return Collections.unmodifiableList(new ArrayList<>(defns));
        }

        /** 
         * Mutator to set the levels that are defined for this catalog. Called during JSON
         * serialization. 
         */
        public void setDefinedLevels(List<LevelDefinition> levels) {
            for (LevelDefinition defn : levels) 
            {
                this.levels.put(defn.getName(), defn);
            }
        }

        LevelDefinition defineLevel(String name, int code)
        {
            if (levels.containsKey(name))
                throw new IllegalArgumentException("This level [" + name + "] has already been defined.");
            
            LevelDefinition defn = new LevelDefinition(name, code);
            levels.put(name, defn);
            return defn;
        }
        
        void defineChildren(LevelDefinition defn, LevelDefinition...children)
        {
            for (LevelDefinition child : children)
            {
                defn.children.add(child.getName());
            }
        }
    
        
        TesseractLevelCatalog build() throws PageModelException 
        {
            HashMap<String, TesseractLevel> tLevels = new HashMap<>();
            for (LevelDefinition defn : levels.values()) {
                tLevels.put(defn.name, new TesseractLevel(defn.name, defn.tessCode));
            }
            
            for (LevelDefinition defn : levels.values()) {
                restoreChildren(defn, tLevels);
            }
            
            return new TesseractLevelCatalog(tLevels);
        }
    
        /**
         * Initializes the collection of child nodes associated with a level definition.
         * 
         * @param defn
         * @param tLevels
         * @throws PageModelException
         */
        private void restoreChildren(LevelDefinition defn, HashMap<String, TesseractLevel> tLevels)
                throws PageModelException {
            TesseractLevel tLevel = tLevels.get(defn.name);
            
            Set<TesseractLevel> children = new HashSet<>();
            for (String c : defn.children)
            {
                TesseractLevel child = tLevels.get(c);
                if (child == null)
                    throw new PageModelException("Configuration error: Unrecognized page layout level [" + c + "]");
                
                children.add(child);
            }
            
            tLevel.addChildren(children);
        }
    }
    
    /**
     * A JSON-serializable representation of a {@link Level}, along with metadata specific to 
     * Tesseract. 
     * 
     * @author Neal Audenaert
     */
    static class LevelDefinition
    {
        private String name;
        private int tessCode;
        private List<String> children = new ArrayList<>();
        
        @SuppressWarnings("unused")  // invoked by Jackson
        private LevelDefinition()
        {
            
        }
        
        /**
         */
        LevelDefinition(String name, int tessCode) {
            this.name = name;
            this.tessCode = tessCode;
        }
        
        public String getName() 
        {
            return name;
        }
        
        public void setName(String n)
        {
            name = n;
        }
        
        public int getTessCode()
        {
            return tessCode;
        }
        
        public void setTessCode(int c)
        {
            tessCode = c;
        }

        public List<String> getChildren() {
            return children;
        }

        public void setChildren(List<String> children) {
            this.children = children;
        }
    }
    
    public static void main(String[] args) throws PageModelException {
        TesseractLevelCatalog catalog = TesseractLevelCatalog.getInstance();
        String json = TesseractLevelCatalog.toJSON(catalog);
        System.out.println(json);
        
        catalog = TesseractLevelCatalog.restore(json);
        System.out.println(TesseractLevelCatalog.toJSON(catalog));
    }
}
