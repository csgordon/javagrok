/*
 * Copyright (C) 2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * This class contains static utility methods that operate on or return objects
 * of type {@code Iterator}. Also see the parallel implementations in {@link
 * Iterables}.
 *
 * @author Kevin Bourrillion
 * @author Scott Bonneau
 */
@GwtCompatible
public final class Iterators {
  private Iterators() {}

  static final UnmodifiableIterator<Object> EMPTY_ITERATOR
      = new UnmodifiableIterator<Object>() {
        public boolean hasNext() {
          return false;
        }
        public Object next() {
          throw new NoSuchElementException();
        }
      };


  /** Returns the empty {@code Iterator}. */
  // Casting to any type is safe since there are no actual elements.
  @SuppressWarnings("unchecked")
  public static <T> UnmodifiableIterator<T> emptyIterator() {
    return (UnmodifiableIterator<T>) EMPTY_ITERATOR;
  }

  private static final Iterator<Object> EMPTY_MODIFIABLE_ITERATOR =
      new Iterator<Object>() {
        /*@Override*/ public boolean hasNext() {
          return false;
        }

        /*@Override*/ public Object next() {
          throw new NoSuchElementException();
        }

        /*@Override*/ public void remove() {
          throw new IllegalStateException();
        }
      };

  /**
   * Returns the empty {@code Iterator} that throws
   * {@link IllegalStateException} instead of
   * {@link UnsupportedOperationException} on a call to
   * {@link Iterator#remove()}.
   */
  // Casting to any type is safe since there are no actual elements.
  @SuppressWarnings("unchecked")
  static <T> Iterator<T> emptyModifiableIterator() {
    return (Iterator<T>) EMPTY_MODIFIABLE_ITERATOR;
  }

  /** Returns an unmodifiable view of {@code iterator}. */
  public static <T> UnmodifiableIterator<T> unmodifiableIterator(
      final Iterator<T> iterator) {
    checkNotNull(iterator);
    return new UnmodifiableIterator<T>() {
      public boolean hasNext() {
        return iterator.hasNext();
      }
      public T next() {
        return iterator.next();
      }
    };
  }

  /**
   * Returns the number of elements remaining in {@code iterator}. The iterator
   * will be left exhausted: its {@code hasNext()} method will return
   * {@code false}.
   */
  public static int size(Iterator<?> iterator) {
    int count = 0;
    while (iterator.hasNext()) {
      iterator.next();
      count++;
    }
    return count;
  }

  /**
   * Returns {@code true} if {@code iterator} contains {@code element}.
   */
  public static boolean contains(Iterator<?> iterator, Object element)
  {
    if (element == null) {
      while (iterator.hasNext()) {
        if (iterator.next() == null) {
          return true;
        }
      }
    } else {
      while (iterator.hasNext()) {
        if (element.equals(iterator.next())) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Traverses an iterator and removes every element that belongs to the
   * provided collection. The iterator will be left exhausted: its
   * {@code hasNext()} method will return {@code false}.
   *
   * @param iterator the iterator to (potentially) remove elements from
   * @param c the elements to remove
   * @return {@code true} if any elements are removed from {@code iterator}
   */
  public static boolean removeAll(Iterator<?> iterator, Collection<?> c) {
    checkNotNull(c);
    boolean modified = false;
    while (iterator.hasNext()) {
      if (c.contains(iterator.next())) {
        iterator.remove();
        modified = true;
      }
    }
    return modified;
  }

  /**
   * Traverses an iterator and removes every element that does not belong to the
   * provided collection. The iterator will be left exhausted: its
   * {@code hasNext()} method will return {@code false}.
   *
   * @param iterator the iterator to (potentially) remove elements from
   * @param c the elements to retain
   * @return {@code true} if any elements are removed from {@code iterator}
   */
  public static boolean retainAll(Iterator<?> iterator, Collection<?> c) {
    checkNotNull(c);
    boolean modified = false;
    while (iterator.hasNext()) {
      if (!c.contains(iterator.next())) {
        iterator.remove();
        modified = true;
      }
    }
    return modified;
  }

  /**
   * Determines whether two iterators contain equal elements in the same order.
   * More specifically, this method returns {@code true} if {@code iterator1}
   * and {@code iterator2} contain the same number of elements and every element
   * of {@code iterator1} is equal to the corresponding element of
   * {@code iterator2}.
   *
   * <p>Note that this will modify the supplied iterators, since they will have
   * been advanced some number of elements forward.
   */
  public static boolean elementsEqual(
      Iterator<?> iterator1, Iterator<?> iterator2) {
    while (iterator1.hasNext()) {
      if (!iterator2.hasNext()) {
        return false;
      }
      Object o1 = iterator1.next();
      Object o2 = iterator2.next();
      if (!Objects.equal(o1, o2)) {
        return false;
      }
    }
    return !iterator2.hasNext();
  }

  /**
   * Returns a string representation of {@code iterator}, with the format
   * {@code [e1, e2, ..., en]}. The iterator will be left exhausted: its
   * {@code hasNext()} method will return {@code false}.
   */
  public static String toString(Iterator<?> iterator) {
    if (!iterator.hasNext()) {
      return "[]";
    }
    StringBuilder builder = new StringBuilder();
    builder.append('[').append(iterator.next());
    while (iterator.hasNext()) {
      builder.append(", ").append(iterator.next());
    }
    return builder.append(']').toString();
  }

  /**
   * Returns the single element contained in {@code iterator}.
   *
   * @throws NoSuchElementException if the iterator is empty
   * @throws IllegalArgumentException if the iterator contains multiple
   *     elements.  The state of the iterator is unspecified.
   */
  public static <T> T getOnlyElement(Iterator<T> iterator) {
    T first = iterator.next();
    if (!iterator.hasNext()) {
      return first;
    }

    StringBuilder sb = new StringBuilder();
    sb.append("expected one element but was: <" + first);
    for (int i = 0; i < 4 && iterator.hasNext(); i++) {
      sb.append(", " + iterator.next());
    }
    if (iterator.hasNext()) {
      sb.append(", ...");
    }
    sb.append(">");

    throw new IllegalArgumentException(sb.toString());
  }

  /**
   * Returns the single element contained in {@code iterator}, or {@code
   * defaultValue} if the iterator is empty.
   *
   * @throws IllegalArgumentException if the iterator contains multiple
   *     elements.  The state of the iterator is unspecified.
   */
  public static <T> T getOnlyElement(
      Iterator<T> iterator, T defaultValue) {
    return iterator.hasNext() ? getOnlyElement(iterator) : defaultValue;
  }

  /**
   * Adds all elements in {@code iterator} to {@code collection}. The iterator
   * will be left exhausted: its {@code hasNext()} method will return
   * {@code false}.
   *
   * @return {@code true} if {@code collection} was modified as a result of this
   *         operation
   */
  public static <T> boolean addAll(
      Collection<T> collection, Iterator<? extends T> iterator) {
    checkNotNull(collection);
    boolean wasModified = false;
    while (iterator.hasNext()) {
      wasModified |= collection.add(iterator.next());
    }
    return wasModified;
  }

  /**
   * Returns the number of elements in the specified iterator that equal the
   * specified object. The iterator will be left exhausted: its
   * {@code hasNext()} method will return {@code false}.
   *
   * @see Collections#frequency
   */
  public static int frequency(Iterator<?> iterator, Object element) {
    int result = 0;
    if (element == null) {
      while (iterator.hasNext()) {
        if (iterator.next() == null) {
          result++;
        }
      }
    } else {
      while (iterator.hasNext()) {
        if (element.equals(iterator.next())) {
          result++;
        }
      }
    }
    return result;
  }

  /**
   * Returns an iterator that cycles indefinitely over the elements of {@code
   * iterable}.
   *
   * <p>The returned iterator supports {@code remove()} if the provided iterator
   * does. After {@code remove()} is called, subsequent cycles omit the removed
   * element, which is no longer in {@code iterable}. The iterator's
   * {@code hasNext()} method returns {@code true} until {@code iterable} is
   * empty.
   *
   * <p><b>Warning:</b> Typical uses of the resulting iterator may produce an
   * infinite loop. You should use an explicit {@code break} or be certain that
   * you will eventually remove all the elements.
   */
  public static <T> Iterator<T> cycle(final Iterable<T> iterable) {
    checkNotNull(iterable);
    return new Iterator<T>() {
      Iterator<T> iterator = emptyIterator();
      Iterator<T> removeFrom;

      public boolean hasNext() {
        if (!iterator.hasNext()) {
          iterator = iterable.iterator();
        }
        return iterator.hasNext();
      }
      public T next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        removeFrom = iterator;
        return iterator.next();
      }
      public void remove() {
        checkState(removeFrom != null,
            "no calls to next() since last call to remove()");
        removeFrom.remove();
        removeFrom = null;
      }
    };
  }

  /**
   * Returns an iterator that cycles indefinitely over the provided elements.
   *
   * <p>The returned iterator supports {@code remove()} if the provided iterator
   * does. After {@code remove()} is called, subsequent cycles omit the removed
   * element, but {@code elements} does not change. The iterator's
   * {@code hasNext()} method returns {@code true} until all of the original
   * elements have been removed.
   *
   * <p><b>Warning:</b> Typical uses of the resulting iterator may produce an
   * infinite loop. You should use an explicit {@code break} or be certain that
   * you will eventually remove all the elements.
   */
  public static <T> Iterator<T> cycle(T... elements) {
    return cycle(Lists.newArrayList(elements));
  }

  /**
   * Combines two iterators into a single iterator. The returned iterator
   * iterates across the elements in {@code a}, followed by the elements in
   * {@code b}. The source iterators are not polled until necessary.
   *
   * <p>The returned iterator supports {@code remove()} when the corresponding
   * input iterator supports it.
   */
  @SuppressWarnings("unchecked")
  public static <T> Iterator<T> concat(Iterator<? extends T> a,
      Iterator<? extends T> b) {
    checkNotNull(a);
    checkNotNull(b);
    return concat(Arrays.asList(a, b).iterator());
  }

  /**
   * Combines three iterators into a single iterator. The returned iterator
   * iterates across the elements in {@code a}, followed by the elements in
   * {@code b}, followed by the elements in {@code c}. The source iterators
   * are not polled until necessary.
   *
   * <p>The returned iterator supports {@code remove()} when the corresponding
   * input iterator supports it.
   */
  @SuppressWarnings("unchecked")
  public static <T> Iterator<T> concat(Iterator<? extends T> a,
      Iterator<? extends T> b, Iterator<? extends T> c) {
    checkNotNull(a);
    checkNotNull(b);
    checkNotNull(c);
    return concat(Arrays.asList(a, b, c).iterator());
  }

  /**
   * Combines four iterators into a single iterator. The returned iterator
   * iterates across the elements in {@code a}, followed by the elements in
   * {@code b}, followed by the elements in {@code c}, followed by the elements
   * in {@code d}. The source iterators are not polled until necessary.
   *
   * <p>The returned iterator supports {@code remove()} when the corresponding
   * input iterator supports it.
   */
  @SuppressWarnings("unchecked")
  public static <T> Iterator<T> concat(Iterator<? extends T> a,
      Iterator<? extends T> b, Iterator<? extends T> c,
      Iterator<? extends T> d) {
    checkNotNull(a);
    checkNotNull(b);
    checkNotNull(c);
    checkNotNull(d);
    return concat(Arrays.asList(a, b, c, d).iterator());
  }

  /**
   * Combines multiple iterators into a single iterator. The returned iterator
   * iterates across the elements of each iterator in {@code inputs}. The input
   * iterators are not polled until necessary.
   *
   * <p>The returned iterator supports {@code remove()} when the corresponding
   * input iterator supports it.
   *
   * @throws NullPointerException if any of the provided iterators is null
   */
  public static <T> Iterator<T> concat(Iterator<? extends T>... inputs) {
    return concat(Arrays.asList(inputs).iterator());
  }

  /**
   * Combines multiple iterators into a single iterator. The returned iterator
   * iterates across the elements of each iterator in {@code inputs}. The input
   * iterators are not polled until necessary.
   *
   * <p>The returned iterator supports {@code remove()} when the corresponding
   * input iterator supports it. The methods of the returned iterator may throw
   * {@code NullPointerException} if any of the input iterators are null.
   */
  public static <T> Iterator<T> concat(
      final Iterator<? extends Iterator<? extends T>> inputs) {
    checkNotNull(inputs);
    return new Iterator<T>() {
      Iterator<? extends T> current = emptyIterator();
      Iterator<? extends T> removeFrom;

      public boolean hasNext() {
        // http://code.google.com/p/google-collections/issues/detail?id=151
        // current.hasNext() might be relatively expensive, worth minimizing.
        boolean currentHasNext;
        while (!(currentHasNext = current.hasNext()) && inputs.hasNext()) {
          current = inputs.next();
        }
        return currentHasNext;
      }
      public T next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        removeFrom = current;
        return current.next();
      }
      public void remove() {
        checkState(removeFrom != null,
            "no calls to next() since last call to remove()");
        removeFrom.remove();
        removeFrom = null;
      }
    };
  }
}
