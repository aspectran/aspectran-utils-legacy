/*
 * Copyright (c) 2008-present The Aspectran Project
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
package com.aspectran.utils;

import com.aspectran.utils.annotation.jsr305.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * Provides static utility methods for accessing JavaBeans style properties via reflection.
 * <p>This class supports simple, nested, and indexed property access.
 * It can operate on standard JavaBeans, {@link Map}s, and {@link Properties} objects.</p>
 *
 * <p>Created: 2008. 04. 22 PM 3:47:15</p>
 */
public abstract class BeanUtils {

    /** An empty immutable {@code Object} array. */
    static final Object[] NO_ARGUMENTS = new Object[0];

    /**
     * Returns the value of a property from a bean, supporting nested and indexed properties.
     * @param bean the bean whose property is to be extracted
     * @param name the name of the property (e.g., "name", "address.city", "friends[0]")
     * @return the property value
     * @throws InvocationTargetException if a property accessor method throws an exception
     */
    public static Object getProperty(Object bean, @NonNull String name) throws InvocationTargetException {
        Object value;
        if (bean instanceof Properties) {
            value = getProperty((Properties)bean, name);
            if (value != null) {
                return value;
            }
        }
        if (name.contains(".")) {
            StringTokenizer parser = new StringTokenizer(name, ".");
            value = bean;
            while (parser.hasMoreTokens()) {
                value = getSimpleProperty(value, parser.nextToken());
                if (value == null) {
                    break;
                }
            }
            return value;
        } else {
            return getSimpleProperty(bean, name);
        }
    }

    /**
     * Returns the value of a simple (non-nested) property from a bean.
     * @param bean the bean whose property is to be extracted
     * @param name the name of the property (e.g., "name", "friends[0]")
     * @return the property value
     * @throws InvocationTargetException if a property accessor method throws an exception
     */
    public static Object getSimpleProperty(Object bean, @NonNull String name) throws InvocationTargetException {
        try {
            Object value;
            if (name.contains("[")) {
                value = getIndexedProperty(bean, name);
            } else if (bean instanceof Properties) {
                value = getProperty((Properties)bean, name);
            } else if (bean instanceof Map<?, ?>) {
                value = ((Map<?, ?>)bean).get(name);
            } else {
                BeanDescriptor bd = BeanDescriptor.getInstance(bean.getClass());
                Method method = bd.getGetter(name);
                try {
                    value = method.invoke(bean, NO_ARGUMENTS);
                } catch (Throwable t) {
                    throw ExceptionUtils.unwrapThrowable(t);
                }
            }
            return value;
        } catch (InvocationTargetException e) {
            throw e;
        } catch (Throwable t) {
            if (bean == null) {
                throw new InvocationTargetException(t, "Could not get property '" + name +
                        "' from null reference. Cause: " + t);
            } else {
                throw new InvocationTargetException(t, "Could not get property '" + name +
                        "' from " + bean.getClass().getName() + ". Cause: " + t);
            }
        }
    }

    private static Object getProperty(@NonNull Properties props, @NonNull String name) {
        Object value = props.get(name);
        if (value == null) {
            // Allow for defaults fallback or potentially overridden accessor...
            value = props.getProperty(name);
        }
        return value;
    }

    /**
     * Sets the value of a property on a bean, supporting nested and indexed properties.
     * <p>If a nested property is null, this method will attempt to instantiate it.</p>
     * @param bean the bean whose property is to be modified
     * @param name the name of the property (e.g., "name", "address.city", "friends[0]")
     * @param value the value to set
     * @throws InvocationTargetException if a property accessor method throws an exception
     * @throws NoSuchMethodException if an accessor method for this property cannot be found
     */
    public static void setProperty(Object bean, @NonNull String name, Object value)
            throws InvocationTargetException, NoSuchMethodException {
        if (bean instanceof Properties) {
            ((Properties)bean).put(name, value);
        } else if (name.contains(".")) {
            StringTokenizer parser = new StringTokenizer(name, ".");
            String newName = parser.nextToken();
            Object child = bean;
            while (parser.hasMoreTokens()) {
                Class<?> type = BeanTypeUtils.getPropertyTypeForSetter(child, newName);
                Object parent = child;
                child = getSimpleProperty(parent, newName);
                if (child == null) {
                    if (value == null) {
                        return; // don't instantiate child path if value is null
                    } else {
                        try {
                            child = ClassUtils.createInstance(type);
                            setProperty(parent, newName, child);
                        } catch (Exception e) {
                            throw new InvocationTargetException(e, "Cannot set value of property '" + name
                                    + "' because '" + newName + "' is null and cannot be instantiated on instance of "
                                    + type.getName() + ". Cause: " + e);
                        }
                    }
                }
                newName = parser.nextToken();
            }
            setSimpleProperty(child, newName, value);
        } else {
            setSimpleProperty(bean, name, value);
        }
    }

    /**
     * Sets the value of a simple (non-nested) property on a bean.
     * @param bean the bean whose property is to be modified
     * @param name the name of the property (e.g., "name", "friends[0]")
     * @param value the value to set
     * @throws InvocationTargetException if a property accessor method throws an exception
     */
    public static void setSimpleProperty(Object bean, @NonNull String name, Object value) throws InvocationTargetException {
        if (bean instanceof Properties) {
            ((Properties)bean).put(name, value);
            return;
        }
        try {
            if (name.contains("[")) {
                setIndexedProperty(bean, name, value);
            } else {
                if (bean instanceof Map<?, ?>) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>)bean;
                    map.put(name, value);
                } else {
                    BeanDescriptor bd = BeanDescriptor.getInstance(bean.getClass());
                    Method method = bd.getSetter(name);
                    Object[] params = new Object[] { value };
                    try {
                        method.invoke(bean, params);
                    } catch (Throwable t) {
                        throw ExceptionUtils.unwrapThrowable(t);
                    }
                }
            }
        } catch (InvocationTargetException e) {
            throw e;
        } catch (Throwable t) {
            try {
                MethodUtils.invokeSetter(bean, name, value);
                return;
            } catch (Throwable tt) {
                //ignore
            }
            if (bean == null) {
                throw new InvocationTargetException(t, "Could not set property '" + name + "' to value '" +
                        value + "' for null reference. Cause: " + t);
            } else {
                throw new InvocationTargetException(t, "Could not set property '" + name + "' to value '" + value +
                        "' for " + bean.getClass().getName() + ". Cause: " + t);
            }
        }
    }

    /**
     * Returns the value of an indexed property (e.g., "names[2]") from a bean.
     * The property must be a {@link List} or an array.
     * @param bean the bean to query
     * @param indexedName the name of the indexed property
     * @return the value at the specified index
     * @throws InvocationTargetException if the property is not a list or array, or if the index is out of bounds
     */
    public static Object getIndexedProperty(Object bean, @NonNull String indexedName) throws InvocationTargetException {
        try {
            String name = indexedName.substring(0, indexedName.indexOf("["));
            int index = Integer.parseInt(indexedName.substring(indexedName.indexOf("[") + 1, indexedName.indexOf("]")));
            Object obj = (!name.isEmpty() ? getSimpleProperty(bean, name) : bean);
            Object value;
            if (obj instanceof List<?>) {
                value = ((List<?>)obj).get(index);
            } else if (obj instanceof Object[]) {
                value = ((Object[])obj)[index];
            } else if (obj instanceof char[]) {
                value = ((char[])obj)[index];
            } else if (obj instanceof boolean[]) {
                value = ((boolean[])obj)[index];
            } else if (obj instanceof byte[]) {
                value = ((byte[])obj)[index];
            } else if (obj instanceof double[]) {
                value = ((double[])obj)[index];
            } else if (obj instanceof float[]) {
                value = ((float[])obj)[index];
            } else if (obj instanceof int[]) {
                value = ((int[])obj)[index];
            } else if (obj instanceof long[]) {
                value = ((long[])obj)[index];
            } else if (obj instanceof short[]) {
                value = ((short[])obj)[index];
            } else {
                throw new IllegalArgumentException("The '" + name + "' property of the " +
                        bean.getClass().getName() + " class is not a List or Array");
            }
            return value;
        } catch (InvocationTargetException e) {
            throw e;
        } catch (Exception e) {
            throw new InvocationTargetException(e, "Error getting ordinal list from JavaBean. Cause: " + e);
        }
    }

    /**
     * Sets the value of an indexed property (e.g., "names[2]") on a bean.
     * The property must be a {@link List} or an array.
     * @param bean the bean to modify
     * @param indexedName the name of the indexed property
     * @param value the value to set at the specified index
     * @throws InvocationTargetException if the property is not a list or array, or if the index is out of bounds
     */
    public static void setIndexedProperty(Object bean, @NonNull String indexedName, Object value)
            throws InvocationTargetException {
        try {
            String name = indexedName.substring(0, indexedName.indexOf("["));
            int index = Integer.parseInt(indexedName.substring(indexedName.indexOf("[") + 1, indexedName.indexOf("]")));
            Object obj = getSimpleProperty(bean, name);
            if (obj instanceof List<?>) {
                @SuppressWarnings("unchecked")
                List<Object> list = (List<Object>)obj;
                list.set(index, value);
            } else if (obj instanceof Object[]) {
                ((Object[])obj)[index] = value;
            } else if (obj instanceof char[]) {
                ((char[])obj)[index] = (Character)value;
            } else if (obj instanceof boolean[]) {
                ((boolean[])obj)[index] = (Boolean)value;
            } else if (obj instanceof byte[]) {
                ((byte[])obj)[index] = (Byte)value;
            } else if (obj instanceof double[]) {
                ((double[])obj)[index] = (Double)value;
            } else if (obj instanceof float[]) {
                ((float[])obj)[index] = (Float)value;
            } else if (obj instanceof int[]) {
                ((int[])obj)[index] = (Integer)value;
            } else if (obj instanceof long[]) {
                ((long[])obj)[index] = (Long)value;
            } else if (obj instanceof short[]) {
                ((short[])obj)[index] = (Short)value;
            } else {
                throw new IllegalArgumentException("The '" + name + "' property of the " +
                        bean.getClass().getName() + " class is not a List or Array");
            }
        } catch (InvocationTargetException e) {
            throw e;
        } catch (Exception e) {
            throw new InvocationTargetException(e, "Error getting ordinal value from JavaBean. Cause: " + e);
        }
    }

    /**
     * Checks if a bean has a readable property with the given name.
     * @param bean the bean to check
     * @param name the property name to check for
     * @return {@code true} if the property exists and is readable, {@code false} otherwise
     * @throws NoSuchMethodException if an accessor method for a nested property cannot be found
     */
    public static boolean hasReadableProperty(Object bean, @NonNull String name) throws NoSuchMethodException {
        boolean exists = false;
        if (bean instanceof Map<?, ?>) {
            exists = true; // ((Map)bean).containsKey(propertyName);
        } else {
            if (name.contains(".")) {
                StringTokenizer parser = new StringTokenizer(name, ".");
                Class<?> type = bean.getClass();
                while (parser.hasMoreTokens()) {
                    name = parser.nextToken();
                    type = BeanDescriptor.getInstance(type).getGetterType(name);
                    exists = BeanDescriptor.getInstance(type).hasReadableProperty(name);
                }
            } else {
                exists = BeanDescriptor.getInstance(bean.getClass()).hasReadableProperty(name);
            }
        }
        return exists;
    }

    /**
     * Checks if a bean has a writable property with the given name.
     * @param bean the bean to check
     * @param name the property name to check for
     * @return {@code true} if the property exists and is writable, {@code false} otherwise
     * @throws NoSuchMethodException if an accessor method for a nested property cannot be found
     */
    public static boolean hasWritableProperty(Object bean, @NonNull String name) throws NoSuchMethodException {
        boolean exists = false;
        if (bean instanceof Map<?, ?>) {
            exists = true; // ((Map)bean).containsKey(propertyName);
        } else {
            if (name.contains(".")) {
                StringTokenizer parser = new StringTokenizer(name, ".");
                Class<?> type = bean.getClass();
                while (parser.hasMoreTokens()) {
                    name = parser.nextToken();
                    type = BeanDescriptor.getInstance(type).getGetterType(name);
                    exists = BeanDescriptor.getInstance(type).hasWritableProperty(name);
                }
            } else {
                exists = BeanDescriptor.getInstance(bean.getClass()).hasWritableProperty(name);
            }
        }
        return exists;
    }

    /**
     * Returns an array of the readable property names exposed by a bean.
     * @param bean the bean to inspect
     * @return an array of readable property names
     */
    public static String[] getReadablePropertyNames(@NonNull Object bean) {
        return BeanDescriptor.getInstance(bean.getClass()).getReadablePropertyNames();
    }

    /**
     * Returns an array of readable properties exposed by the bean,
     * excluding those marked as non-serializable.
     * @param bean the bean to inspect
     * @return an array of serializable, readable property names
     */
    public static String[] getReadablePropertyNamesWithoutNonSerializable(@NonNull Object bean) {
        return BeanDescriptor.getInstance(bean.getClass()).getReadablePropertyNamesWithoutNonSerializable();
    }

    /**
     * Returns an array of the writable property names exposed by a bean.
     * @param bean the bean to inspect
     * @return an array of writable property names
     */
    public static String[] getWritablePropertyNames(@NonNull Object bean) {
        return BeanDescriptor.getInstance(bean.getClass()).getWritablePropertyNames();
    }

}
