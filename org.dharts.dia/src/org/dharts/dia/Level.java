/* File: Level.java
 * Created: Feb 10, 2013
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
package org.dharts.dia;

import java.util.Collection;

/**
 * 
 * @author Neal Audenaert
 */
public interface Level {
    
    /**
     * @return The name of this level. 
     */
    String getName();
    
    /**
     * 
     * @return An unmodifiable collection of {@link Level}s that can be accepted as children 
     *      of this {@link Level}. Note that this is intended as a hint for performance 
     *      purposes. Just because a {@link Level} is defined as an acceptable child of this
     *      {@link Level} does not mean that a specific {@link PageModelNode} will be 
     *      accepted simply because it is of the appropriate {@link Level}. For instance, the
     *      parent {@link PageModelNode} may permit only one child-node of a particular type.
     */
    Collection<Level> getAcceptibleChildren();
    
    /**
     * Indicates whether the supplied level is an acceptable immediate child type of this 
     * {@link Level}
     *  
     * @param pageItem The level to test.
     * @return {@code true} if the supplied level is an acceptable child.
     */
    boolean isAcceptableChild(Level level);
    
    /**
     * Indicates whether the supplied level is an acceptable child type of this {@link Level}
     * or of any of the allowable sub-levels
     *  
     * @param pageItem The level to test.
     * @return {@code true} if the supplied level is an acceptable descendant.
     */
    boolean isAcceptableDescendant(Level level);  
}