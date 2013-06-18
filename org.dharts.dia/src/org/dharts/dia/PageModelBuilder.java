/* File: PageModelBuilder.java
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
package org.dharts.dia;


/**
 * Constructs a new {@link PageModel} instance. This is meant to collect the results of a  
 * low-level document image analysis system (such as that provided by Tesseract) and create 
 * a {@link PageModel} based on that data that can be serialized and restored independently 
 * the image recognition system. 
 * 
 * <p>Note that implementations may provide addition document image understanding support to 
 * evaluate and re-structure the raw representation provided to the builder in order to improve 
 * the accuracy of the {@link PageModel} or to provide additional semantics not supported by 
 * the basic document image analysis system. Consequently, the {@link PageModel} returned by
 * {@link #build()} may have a different structure than the inputs supplied to this builder. 
 * Implementations are expected to document any additional analysis that is performed. 
 * 
 * @author Neal Audenaert
 */
public interface PageModelBuilder {

    /**
     * Indicates whether the supplied {@link PageItem} can be added to this {@link PageModel}
     * 
     * @param item The item to test.
     * @return {@code true} if the supplied item can be added to the {@link PageModel}, 
     *      {@code false} otherwise.
     */
    <X extends PageItem> boolean isSupported(X item);
    
    /**
     * Adds a recognized block to the {@link PageModel}.
     *  
     * @param item The item to be added to this page model.
     * @throws IllegalArgumentException if the supplied item is not supported by this 
     *      {@link PageModelBuilder}. 
     *      
     * @see {@link #isSupported(PageItem)}
     */
    <X extends PageItem> void add(X item);

    /**
     * Finalizes any analytical tasks and returns the built {@link PageModel}. Following 
     * a call to this method, the PageModelBuilder cannot be reused and any subsequent 
     * method invocations may result in an {@link IllegalStateException}.
     *  
     * @return The built {@link PageModel}. Will not be {@code null}
     */
    PageModel build() throws PageModelException;
}