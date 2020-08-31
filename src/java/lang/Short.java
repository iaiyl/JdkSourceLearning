/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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

import jdk.internal.HotSpotIntrinsicCandidate;

/**
 * {@code Short}类在对象中包装了基本类型{@code short}的值。{@code Short}类型的对象
 * 包含一个类型为{@code byte}的字段。
 *
 * <p>此外，这个类提供了一些方法，用于把{@code short} 转换为{@code String}，{@code String}
 * 转换为{@code short},以及其他一些在处理{@code short}时有用的常量和方法。
 *
 * @author  Nakul Saraiya
 * @author  Joseph D. Darcy
 * @see     java.lang.Number
 * @since   1.1
 */
public final class Short extends Number implements Comparable<Short> {

    /**
     * 保持最小值的常数{@code short}，可以具有-2<sup>15</sup>。
     */
    public static final short   MIN_VALUE = -32768;

    /**
     * 保持最大值的常数{@code short}，可以具有2<sup>15</sup>-1。
     */
    public static final short   MAX_VALUE = 32767;

    /**
     * 代表基本类型{@code short}的{@code Class}实例。
     */
    @SuppressWarnings("unchecked")
    public static final Class<Short>    TYPE = (Class<Short>) Class.getPrimitiveClass("short");

    /**
     * 返回表示指定{@code short}的新的{@code String}对象。假定基数为10。
     *
     * @param s 被转化的 {@code short}
     * @return 代表指定 {@code short}的字符串
     * @see java.lang.Integer#toString(int)
     */
    public static String toString(short s) {
        return Integer.toString((int)s, 10);
    }

    /**
     * 将字符串参数解析为第二个参数指定的基数的有符号{@code short}。
     * 字符串中的字符必须是指定基数的数字，（由{@link java.lang.Character#digit(char, int)}是否
     * 返回非负值确定），除了第一个字符可能是一个ASCII减号{@code '-'}({@code '\u005Cu002D'})
     * 以表示一个负值，或一个ASCII加号{@code '+'}({@code '\u005Cu002B'})表示正值。
     * 返回结果是{@code short}值。
     *
     * <p>如果发生以下任何一种情况，将引发类型{@code NumberFormatException}的异常：
     * <ul>
     *     <li>第一个参数为{@code null}或长度为零的字符串。</li>
     *     <li>
     *         基数小于{@link java.lang.Character#MIN_RADIX}或
     *         大于{@link java.lang.Character#MAX_RADIX}。
     *     </li>
     *     <li>
     *         字符串的任何字符都不是指定基底的数字，除了第一个字符可以是减号
     *         {@code '-'}({@code '\u005Cu002D'})或加号
     *         {@code '+'}({@code '\u005Cu002B'})，前提是字符串长度超过1。
     *     </li>
     *     <li>字符串表示的值不是{@code short}类型的值(超过长度)。</li>
     * </ul>
     *
     * @param s         包含要解析的{@code short}表示的{@code String}。
     * @param radix     解析{@code s}使用的基数。
     * @return          指定基数的字符串参数表示的 {@code short}。
     * @throws          NumberFormatException 如果 {@code String}不包含可以解析的{@code short}。
     */
    public static short parseShort(String s, int radix)
        throws NumberFormatException {
        int i = Integer.parseInt(s, radix);
        if (i < MIN_VALUE || i > MAX_VALUE)
            throw new NumberFormatException(
                "Value out of range. Value:\"" + s + "\" Radix:" + radix);
        return (short)i;
    }

    /**
     * 将字符串参数解析为有符号十进制{@code short}。字符串中的字符必须是十进制数字，
     * 除了第一个字符可能是一个ASCII减号{@code '-'}({@code '\u005Cu002D'})
     * 以表示一个负值，或一个ASCII加号{@code '+'}({@code '\u005Cu002B'})表示正值。
     * 返回的{@code short}值，就像是参数和基数为10作为
     * {@link #parseShort(java.lang.String, int)}方法一样。
     *
     * @param s 包含要解析的{@code short}表示的{@code String}。
     * @return  字符串参数的十进制表示的 {@code short}。
     * @throws  NumberFormatException 如果 {@code String} 不包含可解析的 {@code short}。
     */
    public static short parseShort(String s) throws NumberFormatException {
        return parseShort(s, 10);
    }

    /**
     * 返回一个{@code Short}对象, 该对象包含使用第二个参数为指定基数解析
     * {@code String}中的值。第一个参数被解释为由第二个参数为指定基数的有
     * 符号{@code short}表示，就像参数作为{@link #parseShort(java.lang.String, int)}
     * 方法一样。返回结果是指定字符串的{@code short}值表示的{@code Short}对象。
     *
     * <p> In 换句话说，此方法返回一个{@code Byte}对象，其值等于:
     *
     * <blockquote>
     *  {@code new Short(Short.parseShort(s, radix))}
     * </blockquote>
     *
     * @param s         被解析的字符串
     * @param radix     用于解释{@code s}使用的基数。
     * @return          {@code Short}对象，保存指定由指定的基数中的字符串参数表示。
     * @throws          NumberFormatException 如果 {@code String} 不包含可解析的{@code short}。
     */
    public static Short valueOf(String s, int radix)
        throws NumberFormatException {
        return valueOf(parseShort(s, radix));
    }

    /**
     * 返回一个包含指定{@code String}值的{@code Short}对象。
     * 该参数被解释为十进制有符号{@code short}, 就像参数作为
     * {@link #parseShort(java.lang.String)}方法一样。
     * 返回结果是指定字符串的{@code short}值表示的{@code Short}对象。
     *
     * <p>换句话说，此方法返回一个{@code Short}对象，其值等于：
     *
     * <blockquote>
     *  {@code new Short(Short.parseShort(s))}
     * </blockquote>
     *
     * @param s 被解析的字符串
     * @return  {@code Short}对象，保存字符串参数表示的值。
     * @throws  NumberFormatException 如果 {@code String} 不包含可解析的{@code short}。
     */
    public static Short valueOf(String s) throws NumberFormatException {
        return valueOf(s, 10);
    }

    private static class ShortCache {
        private ShortCache(){}

        static final Short cache[] = new Short[-(-128) + 127 + 1];

        static {
            for(int i = 0; i < cache.length; i++)
                cache[i] = new Short((short)(i - 128));
        }
    }

    /**
     * 返回代表指定{@code short}值的{@code Short}实例。
     *
     * 如果不需要新的{@code Short}实例，则通常应该优先使用此方法，而不是构造方法
     * {@link #Short(short)}, 因为此方法通过缓存经常请求的值可能会明显产生更好
     * 的空间和时间性能。
     *
     * 此方法将始终缓存 -128 到 127 范围内的值，包含端点，并可能缓存此范围之外的其他值。
     *
     * @param  s short值
     * @return 代表 {@code s}的{@code Short}实例。
     * @since  1.5
     */
    @HotSpotIntrinsicCandidate
    public static Short valueOf(short s) {
        final int offset = 128;
        int sAsInt = s;
        if (sAsInt >= -128 && sAsInt <= 127) { // must cache
            return ShortCache.cache[sAsInt + offset];
        }
        return new Short(s);
    }

    /**
     * 将{@code String}解码为{@code Short}。
     * 接受以下语法给出的十进制，十六进制和八进制：
     *
     * <blockquote>
     * <dl>
     * <dt><i>DecodableString:</i>
     * <dd><i>Sign<sub>opt</sub> DecimalNumeral</i>
     * <dd><i>Sign<sub>opt</sub></i> {@code 0x} <i>HexDigits</i>
     * <dd><i>Sign<sub>opt</sub></i> {@code 0X} <i>HexDigits</i>
     * <dd><i>Sign<sub>opt</sub></i> {@code #} <i>HexDigits</i>
     * <dd><i>Sign<sub>opt</sub></i> {@code 0} <i>OctalDigits</i>
     *
     * <dt><i>Sign:</i>
     * <dd>{@code -}
     * <dd>{@code +}
     * </dl>
     * </blockquote>
     *
     * <i>DecimalNumeral</i>, <i>HexDigits</i>, and <i>OctalDigits</i>
     * 在<cite>The Java&trade; Language Specification</cite>的 3.10.1 节
     * 定义，但数字之间不接受下划线。
     *
     * <p>可选符号和/或基数说明符之后的字符串序列("{@code 0x}", "{@code 0X}",
     * "{@code #}", 或前导零) 由 {@code Short.parseShort}方法解析，其基数为指定
     * 基数(10, 16, 或 8)。此字符序列必须表示为正值，或抛出{@link NumberFormatException}
     * 异常。如果指定的{@code String}第一个字符为减号，则结果为负。
     * {@code String}不允许使用空格字符。
     *
     * @param     nm 用于解析的{@code String}。
     * @return    {@code nm}表示的{@code Short}对象，拥有{@code short}值。
     * @throws    NumberFormatException  如果{@code String} 不包含可解析{@code short}。
     * @see java.lang.Short#parseShort(java.lang.String, int)
     */
    public static Short decode(String nm) throws NumberFormatException {
        int i = Integer.decode(nm);
        if (i < MIN_VALUE || i > MAX_VALUE)
            throw new NumberFormatException(
                    "Value " + i + " out of range from input " + nm);
        return valueOf((short)i);
    }

    /**
     * {@code Short}的值。
     *
     * @serial
     */
    private final short value;

    /**
     * 构造一个新的已分配的{@code Short}对象，该对象表示指定的{@code short}值。
     *
     * @param value     {@code Short}对象表示的值。
     *
     * @deprecated
     * 很少适合使用这个构造函数。静态工厂{@link #valueOf(short)}通常是更好的选择，
     * 因为它可能产生更好的时间和空间性能。
     */
    @Deprecated(since="9")
    public Short(short value) {
        this.value = value;
    }

    /**
     * 构造一个新的已分配的{@code Short}对象，该对象由指定{@code String}
     * 参数表示的{@code short}值。字符串转换为{@code short}值，就像是使用以10为
     * 基数的{@code parseShort}方法。
     *
     * @param s  被转化为 {@code Byte} 的 {@code String}
     * @throws  NumberFormatException 如果 {@code String} 不包含可解析的{@code short}。
     *
     * @deprecated
     * 很少适合使用这个构造函数。使用{@link #parseShort(String)}把字符串转换
     * 为基本类型{@code short}，或者使用{@link #valueOf(String)}把字符串
     * 转换为一个{@code Short}对象。
     */
    @Deprecated(since="9")
    public Short(String s) throws NumberFormatException {
        this.value = parseShort(s, 10);
    }

    /**
     * 返回 此{@code Short}的值在进行收缩基本类型转换之后的{@code byte}。
     * @jls 5.1.3 Narrowing Primitive Conversions
     */
    public byte byteValue() {
        return (byte)value;
    }

    /**
     * 返回此{@code Short}的{@code short}值。
     */
    @HotSpotIntrinsicCandidate
    public short shortValue() {
        return value;
    }

    /**
     * 返回 此{@code Short}的值在进行加宽基本类型转换之后的{@code int }。
     * @jls 5.1.2 Widening Primitive Conversions
     */
    public int intValue() {
        return (int)value;
    }

    /**
     * 返回 此{@code Short}的值在进行加宽基本类型转换之后的{@code long }。
     * @jls 5.1.2 Widening Primitive Conversions
     */
    public long longValue() {
        return (long)value;
    }

    /**
     * 返回 此{@code Short}的值在进行加宽基本类型转换之后的{@code float }。
     * @jls 5.1.2 Widening Primitive Conversions
     */
    public float floatValue() {
        return (float)value;
    }

    /**
     * 返回 此{@code Short}的值在进行加宽基本类型转换之后的{@code double }。
     * @jls 5.1.2 Widening Primitive Conversions
     */
    public double doubleValue() {
        return (double)value;
    }

    /**
     * 返回此{@code Short}的值表示的{@code String}对象。此值被转换为有符号十进制
     * 表示形式并作为字符串返回，就像将{@code short}值提供给{@link java.lang.Short#toString(short)}
     * 方法一样。
     *
     * @return  基数为 &nbsp;10 的该对象的值的字符串表示。
     */
    public String toString() {
        return Integer.toString((int)value);
    }

    /**
     * 返回此{@code Short}的哈希码；结果和调用了{@code intValue()}一样。
     *
     * @return 此 {@code Short}的哈希码。
     */
    @Override
    public int hashCode() {
        return Short.hashCode(value);
    }

    /**
     * 返回{@code short}值的哈希码；与{@code Short.hashCode()}兼容。
     *
     * @param value 用于哈希的值
     * @return {@code short}值的哈希码值。
     * @since 1.8
     */
    public static int hashCode(short value) {
        return (int)value;
    }

    /**
     * 将此对象与指定的对象比较。当且仅当参数不是{@code null}并且包含与此对象相同
     * 的{@code short}值的{@code Short}对象时，结果为{@code true}。
     *
     * @param obj       要与之比较的对象。
     * @return          如果对象相同 {@code true} ;
     *                  其他情况 {@code false}.
     */
    public boolean equals(Object obj) {
        if (obj instanceof Short) {
            return value == ((Short)obj).shortValue();
        }
        return false;
    }

    /**
     * 以数字方式比较两个{@code Short}对象。
     *
     * @param   anotherShort   被比较的 {@code Short}。
     * @return 如果此 {@code Short}与参数 {@code Short}相等，值为{@code 0}；
     *         如果此 {@code Short}在数值上小于该参数{@code Short}，值小于{@code 0};
     *         如果此 {@code Short}在数值上大于该参数{@code Short}，值大于{@code 0};
     *         (有符号比较)。
     * @since   1.2
     */
    public int compareTo(Short anotherShort) {
        return compare(this.value, anotherShort.value);
    }

    /**
     * 以数字方式比较两个{@code short}值。返回的值与以下方法返回值相同 ：
     * <pre>
     *    Short.valueOf(x).compareTo(Short.valueOf(y))
     * </pre>
     *
     * @param  x  第一个比较的 {@code short}
     * @param  y  第二个比较的 {@code short}
     * @return   如果 {@code x == y}，值为 {@code 0};
     *           如果 {@code x < y}，值小于 {@code 0};
     *           如果 {@code x > y}，值大于 {@code 0}。
     * @since 1.7
     */
    public static int compare(short x, short y) {
        return x - y;
    }

    /**
     * 以无符号数值方式比较两个{@code short}值。
     *
     * @param  x 第一个比较的 {@code byte}
     * @param  y 第一个比较的 {@code byte}
     * @return 如果 {@code x == y}，值为 {@code 0};
     *         以无符号值，如果 {@code x < y}，值小于 {@code 0};
     *         以无符号值，如果 {@code x > y}，值大于 {@code 0}。
     * @since 9
     */
    public static int compareUnsigned(short x, short y) {
        return Short.toUnsignedInt(x) - Short.toUnsignedInt(y);
    }

    /**
     * 用于以二进制补码形式表示{@code short}值的位数。
     * @since 1.5
     */
    public static final int SIZE = 16;

    /**
     * 用于以二进制补码形式表示的{@code short}值的字节数。
     *
     * @since 1.8
     */
    public static final int BYTES = SIZE / Byte.SIZE;

    /**
     * 返回通过反转指定的 {@code short} 值的二进制补码表示中的字节顺序获得的值。
     *
     * @param i 要反转其字节的值
     * @return 通过反转(或等效的交换)指定的 {@code short}值中的字节获得的值。
     * @since 1.5
     */
    @HotSpotIntrinsicCandidate
    public static short reverseBytes(short i) {
        return (short) (((i & 0xFF00) >> 8) | (i << 8));
    }


    /**
     * 将参数转换为无符号{@code int}。在转换为无符号{@code int}中，
     * {@code int}的高阶 16 为零，低阶 16 位和{@code short}参数的位相等。
     *
     * 因此，零和正的{@code short}值映射为数值相等的{@code int}值，而
     * 负的{@code short}值映射为 {@code int}值加上2<sup>16</sup>的相等值。
     *
     * @param  x 要转换为无符号{@code int}的值
     * @return 将参数转换为无符号的 {@code int}值
     * @since 1.8
     */
    public static int toUnsignedInt(short x) {
        return ((int) x) & 0xffff;
    }

    /**
     * 通过无符号转换将参数转换为{@code long}。在转换为无符号{@code long}中，
     * {@code long}的高阶 48 位为零，低阶 16 位和{@code byte}参数的位相等。
     *
     * 因此，零和正的{@code short}值映射为数值相等的{@code long}值，而
     * 负的{@code short}值映射为 {@code long}值加上2<sup>16</sup>的相等值。
     *
     * @param  x 要转换为无符号{@code long}的值
     * @return 将参数转换为无符号的 {@code long}值
     * @since 1.8
     */
    public static long toUnsignedLong(short x) {
        return ((long) x) & 0xffffL;
    }

    /** use serialVersionUID from JDK 1.1. for interoperability */
    private static final long serialVersionUID = 7515723908773894738L;
}
