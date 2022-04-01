/**
 * Copyright 2015-2016 Emmanuel Keller / QWAZR
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class AnnotationsUtils {

  public static <A extends Annotation> A getFirstAnnotation(Class<?> clazz, Class<A> annotationClass,
          Set<Class<?>> checked) {
    if (clazz == null)
      return null;
    if (checked.contains(clazz))
      return null;
    checked.add(clazz);
    A annotation = clazz.getAnnotation(annotationClass);
    if (annotation != null)
      return annotation;
    annotation = getFirstAnnotation(clazz.getInterfaces(), annotationClass, checked);
    if (annotation != null)
      return annotation;
    return getFirstAnnotation(clazz.getSuperclass(), annotationClass, checked);
  }

  public static <A extends Annotation> A getFirstAnnotation(Class<?> clazz, Class<A> annotationClass) {
    return getFirstAnnotation(clazz, annotationClass, new HashSet<>());
  }

  public static <A extends Annotation> A getFirstAnnotation(Class<?>[] classes, Class<A> annotationClass,
          Set<Class<?>> checked) {
    if (classes == null)
      return null;
    for (Class<?> cl : classes) {
      A annotation = getFirstAnnotation(cl, annotationClass, checked);
      if (annotation != null)
        return annotation;
    }
    return null;
  }

  public static void browseFieldsRecursive(final Class<?> clazz, final Consumer<Field> consumer) {
    if (clazz == null || clazz.isPrimitive())
      return;
    browseFields(clazz.getDeclaredFields(), consumer);
    Class<?> nextClazz = clazz.getSuperclass();
    if (nextClazz == clazz)
      return;
    browseFieldsRecursive(nextClazz, consumer);
  }

  public static void browseFields(final Field[] fields, final Consumer<Field> consumer) {
    if (fields == null || consumer == null)
      return;
    for (Field field : fields)
      consumer.accept(field);
  }

}
