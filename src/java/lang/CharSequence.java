/*
 * Copyright (c) 2000, 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package java.lang;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

/**
 * {@code CharSequence} 是 {@code char} 值的可读序列。该接口对许多不同种类的{@code char}
 * 序列提供了统一，只读访问。
 *
 * 一个{@code char}值表示<i>基本多文种平面(Basic Multilingual Plane , BMP)</i>中的一个字符或一个替代项。
 * 请参阅<a href="Character.html#unicode">Unicode Character Representation</a>。
 *
 * <p> 此接口不会细化 {@link java.lang.Object#equals(java.lang.Object) equals}和
 * {@link Object#hashCode() hashCode}方法的常规协定。因此，测试实现{@code CharSequence}两个对象
 * 是否相等的结果通常是未定义的。每个对象可以由不同的类实现，并且不能保证每个测试类都能够测试其实例与另外一个
 * 类的实例是否相等。因此，将任意的{@code CharSequence}实例用作集合中的元素或映射中的key是不合适的。
 *
 * @author Mike McCloskey
 * @since 1.4
 * @spec JSR-51
 */

public interface CharSequence {

    /**
     * 返回此字符序列的长度。此长度是序列中 16 位{@code char}的数量。
     *
     * @return  此序列中 {@code char}的数量
     */
    int length();

    /**
     * 返回指定索引处的{@code char}值。索引的范围是从 0 到 {@code length() - 1}。序列的第一个 {@code char}
     * 值在索引 0 处，下一个在索引 1 处，依次类推，就像数组索引一样。
     *
     * <p>如果索引指定的 {@code char} 值为
     * <a href="{@docRoot}/java.base/java/lang/Character.html#unicode">surrogate</a>, 则返回代理值。
     *
     * @param   index   要返回的 {@code char} 值的索引
     *
     * @return  指定的 {@code char} 值
     *
     * @throws  IndexOutOfBoundsException
     *          如果 {@code index} 参数为负数或不小于{@code length()}
     */
    char charAt(int index);

    /**
     * 返回{@code CharSequence}，它是次序列的子序列。子序列从指定索引处的{@code char}值
     * 开始，到索引 {@code end - 1}处的 {@code char}值结束。返回序列的长度 (在 {@code char}中)
     * 是 {@code end - start}, 所以如果 {@code start == end}，则返回一个空序列。
     *
     * @param   start   开始的索引，包含
     * @param   end     结束的索引，不包含
     *
     * @return  指定的子序列
     *
     * @throws  IndexOutOfBoundsException
     *          如果 {@code start} 或 {@code end} 索引为负数,
     *          如果 {@code end} 大于 {@code length()},
     *          或者 {@code start} 大于 {@code end}
     */
    CharSequence subSequence(int start, int end);

    /**
     * 返回一个字符串，该字符串包含次序列中的字符，其顺序与次序列相同。字符串的长度即是序列的长度。
     *
     * @return  次序列组成的字符串
     */
    public String toString();

    /**
     * 返回 {@code int} 的流，对此序列中的{@code char}值进行零扩展。映射到
     * <a href="{@docRoot}/java.base/java/lang/Character.html#unicode">surrogate code point</a>
     * 的任何字符都是未解释的。
     *
     * <p>当终结流开始操作时，流绑定到该序列（具体地，对于可变序列，流的分割器是
     * <a href="../util/Spliterator.html#binding"><em>late-binding</em></a>）。
     * 如果在该操作期间修改了序列，则结果是未定义的。
     *
     *
     * @return 此序列char值的IntStream
     * @since 1.8
     */
    public default IntStream chars() {
        class CharIterator implements PrimitiveIterator.OfInt {
            //当前位置
            int cur = 0;

            /**
             * 是否还有下一个
             * @return
             */
            public boolean hasNext() {
                return cur < length();
            }

            /**
             * 获取下一个int值
             * @return
             */
            public int nextInt() {
                if (hasNext()) {
                    return charAt(cur++);
                } else {
                    throw new NoSuchElementException();
                }
            }

            @Override
            public void forEachRemaining(IntConsumer block) {
                for (; cur < length(); cur++) {
                    block.accept(charAt(cur));
                }
            }
        }

        return StreamSupport.intStream(() ->
                Spliterators.spliterator(
                        new CharIterator(),
                        length(),
                        Spliterator.ORDERED),
                Spliterator.SUBSIZED | Spliterator.SIZED | Spliterator.ORDERED,
                false);
    }

    /**
     * Returns a stream of code point values from this sequence.  Any surrogate
     * pairs encountered in the sequence are combined as if by {@linkplain
     * Character#toCodePoint Character.toCodePoint} and the result is passed
     * to the stream. Any other code units, including ordinary BMP characters,
     * unpaired surrogates, and undefined code units, are zero-extended to
     * {@code int} values which are then passed to the stream.
     *
     * <p>The stream binds to this sequence when the terminal stream operation
     * commences (specifically, for mutable sequences the spliterator for the
     * stream is <a href="../util/Spliterator.html#binding"><em>late-binding</em></a>).
     * If the sequence is modified during that operation then the result is
     * undefined.
     *
     * @return an IntStream of Unicode code points from this sequence
     * @since 1.8
     */
    public default IntStream codePoints() {
        class CodePointIterator implements PrimitiveIterator.OfInt {
            int cur = 0;

            @Override
            public void forEachRemaining(IntConsumer block) {
                final int length = length();
                int i = cur;
                try {
                    while (i < length) {
                        char c1 = charAt(i++);
                        if (!Character.isHighSurrogate(c1) || i >= length) {
                            block.accept(c1);
                        } else {
                            char c2 = charAt(i);
                            if (Character.isLowSurrogate(c2)) {
                                i++;
                                block.accept(Character.toCodePoint(c1, c2));
                            } else {
                                block.accept(c1);
                            }
                        }
                    }
                } finally {
                    cur = i;
                }
            }

            public boolean hasNext() {
                return cur < length();
            }

            public int nextInt() {
                final int length = length();

                if (cur >= length) {
                    throw new NoSuchElementException();
                }
                char c1 = charAt(cur++);
                if (Character.isHighSurrogate(c1) && cur < length) {
                    char c2 = charAt(cur);
                    if (Character.isLowSurrogate(c2)) {
                        cur++;
                        return Character.toCodePoint(c1, c2);
                    }
                }
                return c1;
            }
        }

        return StreamSupport.intStream(() ->
                Spliterators.spliteratorUnknownSize(
                        new CodePointIterator(),
                        Spliterator.ORDERED),
                Spliterator.ORDERED,
                false);
    }

    /**
     * Compares two {@code CharSequence} instances lexicographically. Returns a
     * negative value, zero, or a positive value if the first sequence is lexicographically
     * less than, equal to, or greater than the second, respectively.
     *
     * <p>
     * The lexicographical ordering of {@code CharSequence} is defined as follows.
     * Consider a {@code CharSequence} <i>cs</i> of length <i>len</i> to be a
     * sequence of char values, <i>cs[0]</i> to <i>cs[len-1]</i>. Suppose <i>k</i>
     * is the lowest index at which the corresponding char values from each sequence
     * differ. The lexicographic ordering of the sequences is determined by a numeric
     * comparison of the char values <i>cs1[k]</i> with <i>cs2[k]</i>. If there is
     * no such index <i>k</i>, the shorter sequence is considered lexicographically
     * less than the other. If the sequences have the same length, the sequences are
     * considered lexicographically equal.
     *
     *
     * @param cs1 the first {@code CharSequence}
     * @param cs2 the second {@code CharSequence}
     *
     * @return  the value {@code 0} if the two {@code CharSequence} are equal;
     *          a negative integer if the first {@code CharSequence}
     *          is lexicographically less than the second; or a
     *          positive integer if the first {@code CharSequence} is
     *          lexicographically greater than the second.
     *
     * @since 11
     */
    @SuppressWarnings("unchecked")
    public static int compare(CharSequence cs1, CharSequence cs2) {
        if (Objects.requireNonNull(cs1) == Objects.requireNonNull(cs2)) {
            return 0;
        }

        if (cs1.getClass() == cs2.getClass() && cs1 instanceof Comparable) {
            return ((Comparable<Object>) cs1).compareTo(cs2);
        }

        for (int i = 0, len = Math.min(cs1.length(), cs2.length()); i < len; i++) {
            char a = cs1.charAt(i);
            char b = cs2.charAt(i);
            if (a != b) {
                return a - b;
            }
        }

        return cs1.length() - cs2.length();
    }

}
