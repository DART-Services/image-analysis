/* File: TypedMap.java
 * Created: Mar 17, 2013
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
package org.dharts.dia.props;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class SimpleTypedMap implements TypedMap 
{
    // NOTE: thread safe. keys is read only, values is thread safe
    private final Collection<TypedKey<?>> keys;
    private final ConcurrentHashMap<TypedKey<?>, Object> values;
    
    /**
     * @param props
     */
    public SimpleTypedMap(Collection<TypedKey<?>> props) {
        this.keys = new HashSet<>(props);
        this.values = new ConcurrentHashMap<>();
    }
    
    /**
     * @param props
     */
    public SimpleTypedMap(Collection<TypedKey<?>> props, Map<TypedKey<?>, Object> values) {
        this.keys = new HashSet<>(props);
        this.values = new ConcurrentHashMap<>(values);
    }

    private void checkKey(TypedKey<?> key) throws PropertyException
    {
        if (!keys.contains(key))
            throw new PropertyException("The supplied key is not defined for this map [" + key +"]");
    }
    
    /* (non-Javadoc)
     * @see org.dharts.dia.props.TypedMap#getDefinedKeys()
     */
    @Override
    public Collection<TypedKey<?>> getDefinedKeys() {
        return Collections.unmodifiableCollection(keys);
    }
    
    /* (non-Javadoc)
     * @see org.dharts.dia.props.TypedMap#isDefined(org.dharts.dia.props.TypedKey)
     */
    @Override
    public boolean isDefined(TypedKey<?> key) {
        return keys.contains(key);
    }
    
    /* (non-Javadoc)
     * @see org.dharts.dia.props.TypedMap#contains(org.dharts.dia.props.TypedKey)
     */
    @Override
    public <X> boolean contains(TypedKey<?> key) {
        return keys.contains(key) && values.get(key) != null;
    }
    
    public <X> void set(TypedKey<X> key, X value) throws PropertyException
    {
        checkKey(key);
        values.put(key, value);
    }
    
    /* (non-Javadoc)
     * @see org.dharts.dia.props.TypedMap#get(org.dharts.dia.props.TypedKey)
     */
    @Override
    public <X> X get(TypedKey<X> key) throws PropertyException {
        checkKey(key);
        
        Object v = values.get(key);
        if (v == null)
            throw new PropertyException("Value not defined [" + key + "]");
        
        return key.getType().cast(v);
    }
}