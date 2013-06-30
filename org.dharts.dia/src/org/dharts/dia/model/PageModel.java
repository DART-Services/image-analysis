/* File: PageModel.java
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
package org.dharts.dia.model;

import java.util.List;


/**
 * Defines a directed, acyclic graph that models of the structure of a document page. A 
 * {@link PageModel} implementation will typically be developed in parallel with a 
 * {@link PageModelBuilder}. The builder is responsible for constructing the model based on 
 * low-level {@link PageItem} observed by the document analysis system along with high-level 
 * semantic analysis of the page based on prior knowledge and observations from adjacent pages.
 * 
 * @author Neal Audenaert
 */
public interface PageModel {

    /**
     * @return The top-level nodes of this page model.
     */
    List<PageModelNode<?>> getRoots();

}
