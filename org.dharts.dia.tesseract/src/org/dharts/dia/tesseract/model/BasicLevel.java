/* File: TesseractLevel.java
 * Created: Feb 23, 2013
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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.dharts.dia.model.Level;

/**
 * @author Neal Audenaert
 *
 */
public class BasicLevel implements Level 
{
    private final String name;
    private final Collection<BasicLevel> children;
    
    /**
     */
    protected BasicLevel(String name) {
        this.name = name;
        this.children = new CopyOnWriteArrayList<>();
    }
    
    @Override
    public String getName() 
    {
        return name;
    }
    
    /**
     * Called during the construction process in order to specify acceptable child levels 
     * of this {@link Level}. This method should not be called after the level has been 
     * properly constructed and made available to clients for use as doing so will change the
     * immutability guarantee of the {@link Level} interface.  
     * 
     * @param levels the child levels to add.
     */
    protected void addChildren(Collection<? extends BasicLevel> levels) {
        children.addAll(levels);
    }

    @Override
    public Collection<Level> getAcceptibleChildren() {
        return Collections.<Level>unmodifiableCollection(children);
    }

    @Override
    public boolean isAcceptableChild(Level level) {
        return children.contains(level);
    }
    
    @Override
    public boolean isAcceptableDescendant(Level level) {
        return hasDescendantLevel(level.getName(), new HashSet<String>());
    }

    /**
     * Recursively search sub-levels while visiting each level only once.
     */
    private boolean hasDescendantLevel(String levelName, Set<String> visited) {
        if (visited.contains(name))
            return false;
        visited.add(name);
        
        if (name.equals(levelName))
            return true;
        
        for (BasicLevel l : children) {
            if (l.hasDescendantLevel(levelName, visited))
                return true;
        }
         
        return false;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BasicLevel)
        {
            BasicLevel defn = (BasicLevel)obj;
            return name.equals(defn.name);
        }

        return false;
    }
    
    @Override
    public int hashCode() {
        int result = 17;
        
        result = result * 37 + name.hashCode();
        
        return result;
    }
    
    @Override
    public String toString() {
        return "Level: " + name;
    }
}
