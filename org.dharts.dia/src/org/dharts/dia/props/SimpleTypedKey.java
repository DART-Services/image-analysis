/* File: SimpleTypedKey.java
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

/**
 * @author Neal Audenaert
 */
public class SimpleTypedKey<T> implements TypedKey<T> {

    public static <X> SimpleTypedKey<X> create(String name, Class<X> type)
    {
        return new SimpleTypedKey<>(name, type);
    }

    private final String name;
    private final Class<T> type;
    
    private SimpleTypedKey(String name, Class<T> type) {
        this.name = name;
        this.type = type;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public Class<T> getType() {
        return type;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !TypedKey.class.isInstance(obj))
            return false;
        
        TypedKey<?> k = TypedKey.class.cast(obj);
        return k.getType() == type && k.getName().equals(name);
    }
    
    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * result + type.hashCode();
        result = 37 * result + name.hashCode();
        
        return result;
    }
    
    @Override
    public String toString() {
        return name + " (" + type.toString() + ")";
    }
    
    
}
