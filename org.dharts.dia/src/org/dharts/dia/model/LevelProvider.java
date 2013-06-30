/* File: LevelProvider.java
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
package org.dharts.dia.model;

import java.util.Collection;


/**
 * Provides access to the {@link Level}s that are defined for a particular {@link PageModel}
 * implementation.
 * 
 * @author Neal Audenaert
 */
public interface LevelProvider {

    /**
     * @return The names of all defined levels.
     */
    public abstract Collection<String> getLevels();

    /**
     * @param name The name of the {@link Level} to check.
     * @return {@code true} if the named level is defined for this {@link PageModel} 
     *      implementation. 
     */
    public abstract boolean hasLevel(String name);

    /**
     * @param name The name of the {@link Level} to retrieve.
     * @return An instance of the named {@link Level}.
     * 
     * @throws PageModelException If the named level is not defined or cannot be instantiated. 
     *      This will be thrown if {@link #hasLevel(String)} returns {@code false}.  
     */
    public abstract Level getLevel(String name) throws PageModelException;

}