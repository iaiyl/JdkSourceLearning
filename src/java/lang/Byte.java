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
 * {@code Byte}类在对象中包装了基本类型{@code byte}的值。{@code Byte}类型的对象
 * 包含一个类型为{@code byte}的字段。
 *
 * <p>此外，这个类提供了一些方法，用于把{@code byte} 转换为{@code String}，{@code String}
 * 转换为{@code byte},以及其他一些在处理{@code byte}时有用的常量和方法。
 *
 * @author  Nakul Saraiya
 * @author  Joseph D. Darcy
 * @see     java.lang.Number
 * @since   1.1
 */
public final class Byte extends Number implements Comparable<Byte> {

    /**
     * 保持最小值的常数{@code byte}，可以具有-2<sup>7</sup>。
     */
    public static final byte   MIN_VALUE = -128;

    /**
     * 保持最大值的常数{@code byte}，可以具有2<sup>7</sup>-1。
     */
    public static final byte   MAX_VALUE = 127;

    /**
     *  代表基本类型{@code byte}的{@code Class}实例。
     */
    @SuppressWarnings("unchecked")
    public static final Class<Byte>     TYPE = (Class<Byte>) Class.getPrimitiveClass("byte");

    /**
     * 返回表示指定{@code byte}的新的{@code String}对象。假定基数为10。
     *
     * @param b 被转换的 {@code byte}
     * @return 代表指定 {@code byte}的字符串
     * @see java.lang.Integer#toString(int)
     */
    public static String toString(byte b) {
        return Integer.toString((int)b, 10);
    }

    /**
     * ByteCache，缓存从 127 ~ -128。
     */
    private static class ByteCache {
        private ByteCache(){}

        static final Byte cache[] = new Byte[-(-128) + 127 + 1];

        static {
            for(int i = 0; i < cache.length; i++)
                cache[i] = new Byte((byte)(i - 128));
        }
    }

    /**
     * 返回代表指定{@code byte}值的{@code Byte}实例。
     *
     * 如果不需要新的{@code Byte}实例，则通常应该优先使用此方法，而不是构造方法
     * {@link #Byte(byte)}，因为此方法可能会明显产生更好的空间和时间性能，
     * 因为所有的字节值都被缓存了。
     *
     * @param  b 字节值。
     * @return 代表 {@code b}的{@code Byte}实例
     * @since  1.5
     */
    @HotSpotIntrinsicCandidate
    public static Byte valueOf(byte b) {
        final int offset = 128;
        return ByteCache.cache[(int)b + offset];
    }

    /**
     * 将字符串参数解析为第二个参数指定的基数的有符号{@code byte}。
     * 字符串中的字符必须是指定基数的数字，（由{@link java.lang.Character#digit(char, int)}是否
     * 返回非负值确定），除了第一个字符可能是一个ASCII减号{@code '-'}({@code '\u005Cu002D'})
     * 以表示一个负值，或一个ASCII加号{@code '+'}({@code '\u005Cu002B'})表示正值。返回结果是{@code byte}值。
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
     *     <li>字符串表示的值不是{@code byte}类型的值(超过长度)。</li>
     * </ul>
     *
     * @param s         包含要解析的{@code byte}表示的{@code String}。
     * @param radix     解析{@code s}使用的基数。
     * @return          指定基数的字符串参数表示的 {@code byte}。
     * @throws          NumberFormatException 如果 {@code String}
     *                  不包含可解析的 {@code byte}.
     */
    public static byte parseByte(String s, int radix)
        throws NumberFormatException {
        int i = Integer.parseInt(s, radix);
        if (i < MIN_VALUE || i > MAX_VALUE)
            throw new NumberFormatException(
                "Value out of range. Value:\"" + s + "\" Radix:" + radix);
        return (byte)i;
    }

    /**
     * 将字符串参数解析为有符号十进制{@code byte}。字符串中的字符必须是十进制数字，
     * 除了第一个字符可能是一个ASCII减号{@code '-'}({@code '\u005Cu002D'})
     * 以表示一个负值，或一个ASCII加号{@code '+'}({@code '\u005Cu002B'})表示正值。
     * 返回的{@code byte}值，就像是参数和基数为10作为{@link #parseByte(String, int)}方法一样。
     *
     * @param s         包含要解析的{@code byte}表示的{@code String}。
     * @return          字符串参数的十进制表示的 {@code byte}。
     * @throws          NumberFormatException 如果 {@code String} 不包含可解析的 {@code byte}。
     */
    public static byte parseByte(String s) throws NumberFormatException {
        return parseByte(s, 10);
    }

    /**
     * 返回一个{@code Byte}对象, 该对象包含使用第二个参数为指定基数解析
     * {@code String}中的值。第一个参数被解释为由第二个参数为指定基数的有
     * 符号{@code byte}表示，就像参数作为{@link #parseByte(java.lang.String, int)}
     * 方法一样。返回结果是指定字符串的{@code byte}值表示的{@code Byte}对象。
     *
     * <p> In 换句话说，此方法返回一个{@code Byte}对象，其值等于:
     *
     * <blockquote>
     * {@code new Byte(Byte.parseByte(s, radix))}
     * </blockquote>
     *
     * @param s         被解析的字符串
     * @param radix     用于解释{@code s}使用的基数。
     * @return          {@code Byte}对象，保存指定由指定的基数中的字符串参数表示。
     * @throws          NumberFormatException 如果 {@code String} 不包含可解析的{@code byte}。
     */
    public static Byte valueOf(String s, int radix)
        throws NumberFormatException {
        return valueOf(parseByte(s, radix));
    }

    /**
     * 返回一个包含指定{@code String}值的{@code Byte}对象。该参数被解释为
     * 十进制有符号{@code byte}，就像参数作为{@link #parseByte(java.lang.String)}方法一样。
     * 返回结果是指定字符串的{@code byte}值表示的{@code Byte}对象。
     *
     * <p> 换句话说，此方法返回一个{@code Byte}对象，其值等于：
     *
     * <blockquote>
     * {@code new Byte(Byte.parseByte(s))}
     * </blockquote>
     *
     * @param s         被解析的字符串
     * @return          {@code Byte}对象，保存字符串参数表示的值。
     * @throws          NumberFormatException 如果 {@code String} 不包含可解析的{@code byte}。
     */
    public static Byte valueOf(String s) throws NumberFormatException {
        return valueOf(s, 10);
    }

    /**
     * 将{@code String}解码为{@code Byte}。
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
     * "{@code #}", 或前导零) 由 {@code Byte.parseByte}方法解析，其基数为指定
     * 基数(10, 16, 或 8)。此字符序列必须表示为正值，或抛出{@link NumberFormatException}
     * 异常。如果指定的{@code String}第一个字符为减号，则结果为负。
     * {@code String}不允许使用空格字符。
     *
     * @param     nm 用于解析的{@code String}。
     * @return   {@code nm}表示的{@code Byte}对象，拥有{@code byte}值。
     * @throws  NumberFormatException  如果{@code String} 不包含可解析{@code byte}。
     * @see java.lang.Byte#parseByte(java.lang.String, int)
     */
    public static Byte decode(String nm) throws NumberFormatException {
        int i = Integer.decode(nm);
        if (i < MIN_VALUE || i > MAX_VALUE)
            throw new NumberFormatException(
                    "Value " + i + " out of range from input " + nm);
        return valueOf((byte)i);
    }

    /**
     * {@code Byte} 的值。
     *
     * @serial
     */
    private final byte value;

    /**
     * 构造一个新的已分配的{@code Byte}对象，该对象表示指定的{@code byte}值。
     *
     * @param value     {@code byte}对象表示的值。
     *
     * @deprecated
     * 很少适合使用这个构造函数。静态工厂{@link #valueOf(byte)}通常是更好的选择，
     * 因为它可能产生更好的时间和空间性能。
     */
    @Deprecated(since="9")
    public Byte(byte value) {
        this.value = value;
    }

    /**
     * 构造一个新的已分配的{@code Byte}对象，该对象由指定{@code String}
     * 参数表示的{@code byte}值。字符串转换为{@code byte}值，就像是使用以10为
     * 基数的{@code parseByte}方法。
     *
     * @param s         {@code String} 被转化为 {@code Byte}
     * @throws          NumberFormatException 如果 {@code String} 不包含可解析的{@code byte}。
     *
     * @deprecated
     * 很少适合使用这个构造函数。使用{@link #parseByte(String)}把字符串转换
     * 为基本类型{@code byte}，或者使用{@link #valueOf(String)}把字符串
     * 转换为一个{@code Byte}对象。
     */
    @Deprecated(since="9")
    public Byte(String s) throws NumberFormatException {
        this.value = parseByte(s, 10);
    }

    /**
     * 返回此{@code Byte}的{@code byte}值。
     */
    @HotSpotIntrinsicCandidate
    public byte byteValue() {
        return value;
    }

    /**
     * 返回 此{@code Byte}的值在进行加宽基本类型转换之后的{@code short}。
     * @jls 5.1.2 Widening Primitive Conversions
     */
    public short shortValue() {
        return (short)value;
    }

    /**
     * 返回 此{@code Byte}的值在进行加宽基本类型转换之后的{@code int}。
     * @jls 5.1.2 Widening Primitive Conversions
     */
    public int intValue() {
        return (int)value;
    }

    /**
     * 返回 此{@code Byte}的值在进行加宽基本类型转换之后的{@code long}。
     * Returns the value of this {@code Byte} as a {@code long} after
     * a widening primitive conversion.
     * @jls 5.1.2 Widening Primitive Conversions
     */
    public long longValue() {
        return (long)value;
    }

    /**
     * 返回 此{@code Byte}的值在进行加宽基本类型转换之后的{@code float}。
     * @jls 5.1.2 Widening Primitive Conversions
     */
    public float floatValue() {
        return (float)value;
    }

    /**
     * 返回 此{@code Byte}的值在进行加宽基本类型转换之后的{@code double}。
     * Returns the value of this {@code Byte} as a {@code double}
     * after a widening primitive conversion.
     * @jls 5.1.2 Widening Primitive Conversions
     */
    public double doubleValue() {
        return (double)value;
    }

    /**
     * 返回此{@code Byte}的值表示的{@code String}对象。此值被转换为有符号十进制
     * 表示形式并作为字符串返回，就像将{@code byte}值提供给{@link java.lang.Byte#toString(byte)}
     * 方法一样。
     *
     * @return  基数为 &nbsp;10 的该对象的值的字符串表示。
     */
    public String toString() {
        return Integer.toString((int)value);
    }

    /**
     * 返回此{@code Byte}的哈希码；结果和调用了{@code intValue()}一样。
     *
     * @return 此 {@code Byte}的哈希码。
     */
    @Override
    public int hashCode() {
        return Byte.hashCode(value);
    }

    /**
     * 返回{@code byte}值的哈希码；与{@code Byte.hashCode()}兼容。
     *
     * @param value 用于哈希的值
     * @return {@code byte}值的哈希码值。
     * @since 1.8
     */
    public static int hashCode(byte value) {
        return (int)value;
    }

    /**
     * 将此对象与指定的对象比较。当且仅当参数不是{@code null}并且包含与此对象相同
     * 的{@code byte}值的{@code Byte}对象时，结果为{@code true}。
     *
     * @param obj       要与之比较的对象。
     * @return          如果对象相同 {@code true} ;
     *                  其他情况 {@code false}.
     */
    public boolean equals(Object obj) {
        if (obj instanceof Byte) {
            return value == ((Byte)obj).byteValue();
        }
        return false;
    }

    /**
     * 以数字方式比较两个{@code Byte}对象。
     *
     * @param   anotherByte   被比较的 {@code Byte}。
     * @return  如果此 {@code Byte}与参数 {@code Byte}相等，值为{@code 0}；
     *          如果此 {@code Byte}在数值上小于该参数{@code Byte}，值小于{@code 0};
     *          如果此 {@code Byte}在数值上大于该参数{@code Byte}，值大于{@code 0};
     *          (有符号比较)。
     * @since   1.2
     */
    public int compareTo(Byte anotherByte) {
        return compare(this.value, anotherByte.value);
    }

    /**
     * 以数字方式比较两个{@code byte}值。返回的值与以下方法返回值相同 ：
     * <pre>
     *    Byte.valueOf(x).compareTo(Byte.valueOf(y))
     * </pre>
     *
     * @param  x 第一个比较的 {@code byte}
     * @param  y 第二个比较的 {@code byte}
     * @return   如果 {@code x == y}，值为 {@code 0};
     *           如果 {@code x < y}，值小于 {@code 0};
     *           如果 {@code x > y}，值大于 {@code 0}。
     * @since 1.7
     */
    public static int compare(byte x, byte y) {
        return x - y;
    }

    /**
     * 以无符号数值方式比较两个{@code byte}值。
     *
     * @param  x 第一个比较的 {@code byte}
     * @param  y 第一个比较的 {@code byte}
     * @return 如果 {@code x == y}，值为 {@code 0};
     *         以无符号值，如果 {@code x < y}，值小于 {@code 0};
     *         以无符号值，如果 {@code x > y}，值大于 {@code 0}。
     * @since 9
     */
    public static int compareUnsigned(byte x, byte y) {
        return Byte.toUnsignedInt(x) - Byte.toUnsignedInt(y);
    }

    /**
     * 将参数转换为无符号{@code int}。在转换为无符号{@code int}中，
     * {@code int}的高阶 24 为零，低阶 8 位和{@code byte}参数的位相等。
     *
     * 因此，零和正的{@code byte}值映射为数值相等的{@code int}值，而
     * 负的{@code byte}值映射为 {@code int}值加上2<sup>8</sup>的相等值。
     *
     * @param  x 转换为无符号{@code int}的值
     * @return 将参数转换为无符号的 {@code int}值
     * @since 1.8
     */
    public static int toUnsignedInt(byte x) {
        return ((int) x) & 0xff;
    }

    /**
     * 通过无符号转换将参数转换为{@code long}。在转换为无符号{@code long}中，
     * {@code long}的高阶 56 位为零，低阶 8 位和{@code byte}参数的位相等。
     *
     * 因此，零和正的{@code byte}值映射为数值相等的{@code long}值，而
     * 负的{@code byte}值映射为 {@code long}值加上2<sup>8</sup>的相等值。
     *
     * @param  x 转换为无符号{@code long}的值
     * @return 将参数转换为无符号的 {@code long}值
     * @since 1.8
     */
    public static long toUnsignedLong(byte x) {
        return ((long) x) & 0xffL;
    }


    /**
     * 用于以二进制补码形式表示{@code byte}值的位数。
     *
     * @since 1.5
     */
    public static final int SIZE = 8;

    /**
     * 用于以二进制补码形式表示的{@code byte}值的字节数。
     *
     * @since 1.8
     */
    public static final int BYTES = SIZE / Byte.SIZE;

    /** use serialVersionUID from JDK 1.1. for interoperability */
    private static final long serialVersionUID = -7183698231559129828L;
}
