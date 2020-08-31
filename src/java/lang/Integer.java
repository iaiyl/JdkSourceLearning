/*
 * Copyright (c) 1994, 2018, Oracle and/or its affiliates. All rights reserved.
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

import java.lang.annotation.Native;
import java.util.Objects;
import jdk.internal.HotSpotIntrinsicCandidate;
import jdk.internal.misc.VM;

import static java.lang.String.COMPACT_STRINGS;
import static java.lang.String.LATIN1;
import static java.lang.String.UTF16;

/**
 * {@code Integer}类在对象中包装了基本类型{@code int}的值。类型为{@code Integer}的对象
 * 包含一个类型为{@code int}的字段。
 * <p>此外，这个类提供了一些方法，用于把{@code int} 转换为{@code String}，{@code String}
 * 转换为{@code int}，以及其他一些在处理{@code int}时有用的常量和方法。
 *
 * <p>实施说明："bit twiddling"方法(例如{@link #highestOneBit(int) highestOneBit} 和
 * {@link #numberOfTrailingZeros(int) numberOfTrailingZeros})的实现基于
 * Henry S. Warren, Jr.'s <i>Hacker's Delight</i>, (Addison Wesley, 2002)
 * 的材料。
 *
 * @author  Lee Boynton
 * @author  Arthur van Hoff
 * @author  Josh Bloch
 * @author  Joseph D. Darcy
 * @since 1.0
 */
public final class Integer extends Number implements Comparable<Integer> {
    /**
     * 保持最小值的常数{@code int}，可以具有-2<sup>31</sup>。
     */
    @Native public static final int   MIN_VALUE = 0x80000000;

    /**
     * 保持最大值的常数{@code int}，可以具有2<sup>31</sup>-1。
     */
    @Native public static final int   MAX_VALUE = 0x7fffffff;

    /**
     * 代表基本类型{@code int}的{@code Class}实例。
     *
     * @since   1.1
     */
    @SuppressWarnings("unchecked")
    public static final Class<Integer>  TYPE = (Class<Integer>) Class.getPrimitiveClass("int");

    /**
     * 用于将数字表示为字符串的所有可能字符。
     * 进制
     */
    static final char[] digits = {
        '0' , '1' , '2' , '3' , '4' , '5' ,
        '6' , '7' , '8' , '9' , 'a' , 'b' ,
        'c' , 'd' , 'e' , 'f' , 'g' , 'h' ,
        'i' , 'j' , 'k' , 'l' , 'm' , 'n' ,
        'o' , 'p' , 'q' , 'r' , 's' , 't' ,
        'u' , 'v' , 'w' , 'x' , 'y' , 'z'
    };

    /**
     * 返回以第二个参数为指定基数的第一个参数的字符串表示。
     *
     * <p> 如果基数小于{@code Character.MIN_RADIX}或者大于
     * {@code Character.MAX_RADIX}，将会使用{@code 10}替代。
     *
     * <p>如果第一个参数是负数，则结果的第一个元素是ASCII减去字符{@code '-'}
     * ({@code '\u005Cu002D'})。如果第一个参数不是负数，则结果中不会
     * 出现符号字符。
     *
     * <p>结果的剩余字符表示第一个参数的大小。如果大小为0，则用单个字符{@code '0'}
     * ({@code '\u005Cu0030'})表示；否则，表示大小的第一个字符将不是零字符。
     * 使用以下ASCII字符作为数字：
     * <blockquote>
     *     {@code 0123456789abcdefghijklmnopqrstuvwxyz}
     * </blockquote>
     *
     * 这些是{@code '\u005Cu0030'}到{@code '\u005Cu0039'}和{@code '\u005Cu0061'}
     * 到{@code '\u005Cu007A'}。如果{@code radix}是<var>N</var>，那么这些字符的前
     * <var>N</var>个按照所示顺序用作基数-<var>N</var>数字。因此，十六进制（基数 16）
     * 的数字是0123456789abcdef。如果需要大写字母，可以在结果上调用
     * {@link java.lang.String#toUpperCase()}方法：
     *
     * <blockquote>
     *  {@code Integer.toString(n, 16).toUpperCase()}
     * </blockquote>
     *
     * @param   i       要转换为字符串的整数。
     * @param   radix   字符串表示使用的基数。
     * @return  指定基数中参数的字符串表示形式。
     * @see     java.lang.Character#MAX_RADIX
     * @see     java.lang.Character#MIN_RADIX
     */
    public static String toString(int i, int radix) {
        //如果基数大于36或小于2，使用10
        if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX)
            radix = 10;

        /* 使用更快的版本 */
        if (radix == 10) {
            return toString(i);
        }

        if (COMPACT_STRINGS) {
            //如果开启压缩字符串，使用33位byte存储，
            byte[] buf = new byte[33];
            boolean negative = (i < 0);
            int charPos = 32;
            //转为负数
            if (!negative) {
                i = -i;
            }

            //当前数（负数）小于 radix
            while (i <= -radix) {
                //取余赋值，取模保存
                buf[charPos--] = (byte)digits[-(i % radix)];
                i = i / radix;
            }
            //保存最后一位
            buf[charPos] = (byte)digits[-i];
            //保存负号
            if (negative) {
                buf[--charPos] = '-';
            }
            //LATIN1编码的字符串，从charPos开始到32，长度为33-charPos
            return StringLatin1.newString(buf, charPos, (33 - charPos));
        }
        //使用UTF16编码
        return toStringUTF16(i, radix);
    }

    /**
     * 指定数字的UTF16编码的字符串，i为指定数字，radix为基底
     */
    private static String toStringUTF16(int i, int radix) {
        byte[] buf = new byte[33 * 2];
        boolean negative = (i < 0);
        int charPos = 32;
        if (!negative) {
            i = -i;
        }
        while (i <= -radix) {
            StringUTF16.putChar(buf, charPos--, digits[-(i % radix)]);
            i = i / radix;
        }
        StringUTF16.putChar(buf, charPos, digits[-i]);

        if (negative) {
            StringUTF16.putChar(buf, --charPos, '-');
        }
        return StringUTF16.newString(buf, charPos, (33 - charPos));
    }

    /**
     * 返回以第二个参数指定的基数的第一个参数的无符号整数值的字符串表示形式。
     *
     * <p>如果基数小于{@code Character.MIN_RADIX}或者大于
     *  {@code Character.MAX_RADIX}，将会使用{@code 10}替代。
     *
     * <p>注意，由于第一个参数被视为无符号值，因此不会打印任何前导符号字符。
     *
     * <p>如果大小为零，则用单个零字符{@code '0'}({@code '\u005Cu0030'})表示；
     * 否则，表示大小的第一个字符将不是零字符。
     *
     * <p>基数的行为和用作数字的字符与{@link #toString(int, int) toString}相同。
     *
     * @param   i       要转换为无符号字符串的整数。
     * @param   radix   字符串表示使用的基数。
     * @return  指定基数中参数的无符号字符串表示形式。
     * @see     #toString(int, int)
     * @since 1.8
     */
    public static String toUnsignedString(int i, int radix) {
        //先转换为无符号的long，之后调用Long的toUnsignedString方法
        return Long.toUnsignedString(toUnsignedLong(i), radix);
    }

    /**
     * 返回以&nbsp;16为指定的基数的第一个参数的无符号整数值的字符串表示形式。
     *
     * <p>如果参数为负数，则无符号数值是参数加 2<sup>32</sup>；否则，他等于参数。
     * 此值将转换为十六进制（基数为&nbsp;16）的ASCII数字字符串，没有额外的前导{@code 0}s。
     *
     * <p>可以通过调用{@link Integer#parseUnsignedInt(String, int) Integer.parseUnsignedInt(s, 16)}
     * 从返回的字符串s恢复参数的值。
     *
     * <p>如果无符号大小为0，则由单个字符零{@code '0'}({@code '\u005Cu0030'})表示；
     * 否则，无符号大小表示的第一个字符将不是零字符。使用以下ASCII字符作为数字：
     *
     * <blockquote>
     *  {@code 0123456789abcdef}
     * </blockquote>
     *
     * 这些是{@code '\u005Cu0030'}到{@code '\u005Cu0039'}和{@code '\u005Cu0061'}
     * 到{@code '\u005Cu007A'}。如果需要大写字母，可以在结果上调用
     * {@link java.lang.String#toUpperCase()}方法：
     *
     * <blockquote>
     *  {@code Integer.toHexString(n).toUpperCase()}
     * </blockquote>
     *
     * @param   i   要转换为字符串的整数。
     * @return  以十六进制（基数&nbsp;16）表示的参数的无符号整数的字符串表示形式。
     *
     * @see #parseUnsignedInt(String, int)
     * @see #toUnsignedString(int, int)
     * @since   1.0.2
     */
    public static String toHexString(int i) {
        return toUnsignedString0(i, 4);
    }

    /**
     * 返回以&nbsp;8为指定的基数的第一个参数的无符号整数值的字符串表示形式。
     *
     * <p>如果参数为负数，则无符号数值是参数加 2<sup>32</sup>；否则，他等于参数。
     * 此值将转换为八进制（基数为&nbsp;8）的ASCII数字字符串，没有额外的前导{@code 0}s。
     *
     * <p>可以通过调用{@link Integer#parseUnsignedInt(String, int) Integer.parseUnsignedInt(s, 8)}
     * 从返回的字符串s恢复参数的值。
     *
     * <p>如果无符号大小为0，则由单个字符零{@code '0'}({@code '\u005Cu0030'})表示；
     * 否则，无符号大小表示的第一个字符将不是零字符。使用以下ASCII字符作为数字：
     *
     * <blockquote>
     * {@code 01234567}
     * </blockquote>
     *
     * 这些是{@code '\u005Cu0030'}到{@code '\u005Cu0037'}
     *
     * @param   i   要转换为字符串的整数。
     * @return  以八进制（基数&nbsp;8）表示的参数的无符号整数的字符串表示形式。
     *
     * @see #parseUnsignedInt(String, int)
     * @see #toUnsignedString(int, int)
     * @since   1.0.2
     */
    public static String toOctalString(int i) {
        return toUnsignedString0(i, 3);
    }

    /**
     * 返回以&nbsp;2为指定的基数的第一个参数的无符号整数值的字符串表示形式。
     *
     * <p>如果参数为负数，则无符号数值是参数加 2<sup>32</sup>；否则，他等于参数。
     * 此值将转换为二进制（基数为&nbsp;2）的ASCII数字字符串，没有额外的前导{@code 0}s。
     *
     * <p>可以通过调用{@link Integer#parseUnsignedInt(String, int) Integer.parseUnsignedInt(s, 2)}
     * 从返回的字符串s恢复参数的值。
     *
     * <p>如果无符号大小为0，则由单个字符零{@code '0'}({@code '\u005Cu0030'})表示；
     * 否则，无符号大小表示的第一个字符将不是零字符。使用二进制数字{@code '0'}({@code '\u005Cu0030'})字符
     * 和{@code '1'}({@code '\u005Cu0031'})字符。
     *
     * @param   i   要转换为字符串的整数。
     * @return  以二进制（基数&nbsp;2）表示的参数的无符号整数的字符串表示形式。
     *
     * @see #parseUnsignedInt(String, int)
     * @see #toUnsignedString(int, int)
     * @since   1.0.2
     */
    public static String toBinaryString(int i) {
        return toUnsignedString0(i, 1);
    }

    /**
     * 将整数转换为无符号数字。
     */
    private static String toUnsignedString0(int val, int shift) {
        // assert shift > 0 && shift <=5 : "Illegal shift value";
        // numberOfLeadingZeros高位0的个数，包括符号位在内，mag为二进制有效位数
        int mag = Integer.SIZE - Integer.numberOfLeadingZeros(val);
        /** chars表示有效的字符个数，即String中char数组的长度，次数申请的长度应为
         * mag / shift 向上取整， 如何向上取整呢， ------>mag/shift + (shift - 1 ) / shitf
         * 如果是二进制 ---> shift = 1 ---> (shift - 1)/ shift = 0  ---> mag / shift得到的肯定为整数
         * 如果是八进制（需要三个二进制表示 ， shift为3） ---> shift = 3 ---> (shift - 1)/ shift = 2/3 ---> mag / shift = mag/3，此时为加上2/3再向下取整，即为该数的向上取整
         * 如果是十六进制（需要四个二进制表示 ， shift为4） ---> shift = 4 ---> (shift - 1)/ shift = 3/4 ---> mag / shift = mag/4，此时为加上3/4再向下取整，即为该数的向上取整
         */
        int chars = Math.max(((mag + (shift - 1)) / shift), 1);

        if (COMPACT_STRINGS) {
            // 压缩字符串，申请byte长度为chars，使用Latin1编码，每个字符占用1个字节
            byte[] buf = new byte[chars];
            // 将对应的无符号int进行进制转换放入缓冲区
            formatUnsignedInt(val, shift, buf, 0, chars);
            return new String(buf, LATIN1);
        } else {
            // 非压缩字符串，申请byte长度为chars * 2，使用UTF16编码，每个字符占用2个字节
            byte[] buf = new byte[chars * 2];
            // 将对应的无符号int进行进制转换后放入缓冲区
            formatUnsignedIntUTF16(val, shift, buf, 0, chars);
            return new String(buf, UTF16);
        }
    }

    /**
     * 将{@code int}(视为无符号数)格式化放入字符缓冲区。如果{@code len}超出了{@code val}
     * 格式化ASCII表示形式，则{@code}将以前导零填充。
     *
     * @param val 要格式化的无符号整数
     * @param shift 要格式化的基数的log2 (十六进制为4, 八进制为3, 二进制为1)
     * @param buf 要写入的字符缓冲区
     * @param offset 目标缓冲区中起始的偏移量
     * @param len 要写的字符数
     */
    static void formatUnsignedInt(int val, int shift, char[] buf, int offset, int len) {
        // assert shift > 0 && shift <=5 : "Illegal shift value";
        // assert offset >= 0 && offset < buf.length : "illegal offset";
        // assert len > 0 && (offset + len) <= buf.length : "illegal length";
        // 缓冲区长度
        int charPos = offset + len;
        // 基数 radix (1 << shift) 计算出当前偏移量下的进制基数
        int radix = 1 << shift;
        // 掩码，进制减一，和val做 & 运算，可算得对应的该进制的对应的数字
        int mask = radix - 1;
        do {
            // 从后往前，将算得的对应基数下的数字放入缓冲区
            buf[--charPos] = Integer.digits[val & mask];
            // 无符号右移对应的偏移量，因为这部分已经算过了
            val >>>= shift;
        } while (charPos > offset);
    }

    /**
     * byte[]/LATIN1 版本
     *
     * @param val 要格式化的无符号整数
     * @param shift 要格式化的基数的log2 (十六进制为4, 八进制为3, 二进制为1)
     * @param buf 要写入的字符缓冲区
     * @param offset 目标缓冲区中起始的偏移量
     * @param len 要写的字符数
     */
    static void formatUnsignedInt(int val, int shift, byte[] buf, int offset, int len) {
        // 缓冲区长度
        int charPos = offset + len;
        // 基数 radix (1 << shift) 计算出当前偏移量下的进制基数
        int radix = 1 << shift;
        // 掩码，进制减一，和val做 & 运算，可算得对应的该进制的对应的数字
        int mask = radix - 1;
        do {
            // 从后往前，将算得的对应基数下的数字放入缓冲区
            buf[--charPos] = (byte)Integer.digits[val & mask];
            // 无符号右移对应的偏移量，因为这部分已经算过了
            val >>>= shift;
        } while (charPos > offset);
    }

    /** byte[]/UTF16 版本
     *
     * @param val 要格式化的无符号整数
     * @param shift 要格式化的基数的log2 (十六进制为4, 八进制为3, 二进制为1)
     * @param buf 要写入的字符缓冲区
     * @param offset 目标缓冲区中起始的偏移量
     * @param len 要写的字符数
     */
    private static void formatUnsignedIntUTF16(int val, int shift, byte[] buf, int offset, int len) {
        // 缓冲区 byte数组长度
        int charPos = offset + len;
        // 基数 radix (1 << shift) 计算出当前偏移量下的进制基数
        int radix = 1 << shift;
        // 掩码，进制减一，和val做 & 运算，可算得对应的该进制的对应的数字
        int mask = radix - 1;
        do {
            // 将进制转换后的数值放入缓冲区，每次使用两个字节
            StringUTF16.putChar(buf, --charPos, Integer.digits[val & mask]);
            // 无符号右移对应的偏移量，因为这部分已经算过了
            val >>>= shift;
        } while (charPos > offset);
    }

    //十位
    static final byte[] DigitTens = {
        '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
        '1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
        '2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
        '3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
        '4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
        '5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
        '6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
        '7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
        '8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
        '9', '9', '9', '9', '9', '9', '9', '9', '9', '9',
        } ;

    //个位
    static final byte[] DigitOnes = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        } ;


    /**
     * 返回表示指定整数的{@code String}对象。参数转换为带符号的十进制表示形式
     * 并作为字符串返回，就像参数和基数为10的{@link #toString(int, int)}
     * 方法的参数一样。
     *
     * @param   i   要转换的整数。
     * @return  基数为10的参数的字符串表示。
     */
    @HotSpotIntrinsicCandidate
    public static String toString(int i) {
        //获得整数的size，即对应字符串的长度
        int size = stringSize(i);
        if (COMPACT_STRINGS) {
            //如果是压缩字符串，使用LATIN1，初始化大小为size
            byte[] buf = new byte[size];
            //将处理后的数字放入buf
            getChars(i, size, buf);
            //LATIN1编码，占8位，即一字节，使用byte数组存储，返回LATIN1编码的字符串。
            return new String(buf, LATIN1);
        } else {
            //非压缩字符串，占用两倍的size
            byte[] buf = new byte[size * 2];
            //使用UTF16编码字符
            StringUTF16.getChars(i, size, buf);
            //UTF16编码，占16位，2字节，使用byte数组（大小为LATIN1编码的两倍）存储，返回UTF16编码的字符串。
            return new String(buf, UTF16);
        }
    }

    /**
     * 返回参数参数的字符串表示形式，为十进制无符号数值。
     *
     * 该参数将转换为无符号的十进制表示形式，并作为字符串返回，就像参数和基数为 10
     * 作为 {@link #toUnsignedString(int, int)}方法的参数一样。
     *
     * @param   i  要转换为无符号字符串的的整数
     * @return  此参数的无符号字符串表示形式
     * @see     #toUnsignedString(int, int)
     * @since 1.8
     */
    public static String toUnsignedString(int i) {
        return Long.toString(toUnsignedLong(i));
    }

    /**
     * 将代表整数i的字符放入字符数组buf中。字符从指定 index（不包含）处的最低有效数字开始
     * 向后放置到缓冲区中，然后从那里向后工作。
     *
     * @implNote 此方法将输入的正值转为负值，涵盖 Integer.MIN_VALUE的情况。以其他方式
     * 转换(从负数到正数)，在显示Integer.MIN_VALUE时，该正数将会溢出。
     *
     * @param i     转换的值
     * @param index 最低有效数字后的下一个索引
     * @param buf   目标缓冲区, Latin1编码
     * @return 最高有效数字或负号（如果存在）的索引
     */
    static int getChars(int i, int index, byte[] buf) {
        //每次循环处理两个字符
        int q, r;
        //字符开始的位置为index，从这个位置开始向后(即从index到0)工作
        int charPos = index;

        //是否为负数，如果是正数，则置为负数
        boolean negative = i < 0;
        if (!negative) {
            i = -i;
        }

        // 每次迭代产生两位数
        while (i <= -100) {
            //得到后两位数
            q = i / 100;
            r = (q * 100) - i;
            //后两位数已经处理，将剩余字符赋值给i
            i = q;
            //从数组中取得（个位）
            buf[--charPos] = DigitOnes[r];
            //从数组中取得（十位）
            buf[--charPos] = DigitTens[r];
        }

        // 我们知道此时最多剩下两位数，先处理个位
        q = i / 10;
        r = (q * 10) - i;
        buf[--charPos] = (byte)('0' + r);

        // 剩下的就是最后一个数字, '0' - 负数得到正数
        if (q < 0) {
            buf[--charPos] = (byte)('0' - q);
        }

        //如果是负数，则将'-'放入buf
        if (negative) {
            buf[--charPos] = (byte)'-';
        }
        //返回当前索引，即最后一个处理的字符的位置
        return charPos;
    }

    // 处于兼容性原因留在这里, see JDK-8143900.
    static final int [] sizeTable = { 9, 99, 999, 9999, 99999, 999999, 9999999,
                                      99999999, 999999999, Integer.MAX_VALUE };

    /**
     * 返回给定int值的字符串表示size。
     *
     * @param x int值
     * @return 字符串大小
     *
     * @implNote 还有其他计算方式：比如，binary search，但是值如果是偏向零，因此 linear search
     * 获胜。在循环展开后，迭代结果通常会内联到生成的代码中。
     *
     */
    static int stringSize(int x) {
        //当前参数的位数，如果是负数，先把符号计算算进去
        int d = 1;
        //如果是正数，取反，d置为o
        if (x >= 0) {
            d = 0;
            x = -x;
        }
        //基数，
        int p = -10;
        // 循环9次，因为int的最大值为2147483647，10位数，
        // 而-10循环扩大10次会超出int，因此只能循环9次
        for (int i = 1; i < 10; i++) {
            if (x > p)
                return i + d;
            p = 10 * p;
        }
        //如果循环9次依旧没有返回，证明是10位数 + 符号。
        return 10 + d;
    }

    /**
     * 将字符串参数解析为以第二个参数指定的基数的有符号整数。字符串中的字符必须全部
     * 为指定基数的数字（由{@link java.lang.Character#digit(char, int)}是否
     * 返回非负值确定），除了第一个字符可能是一个ASCII减号{@code '-'}({@code '\u005Cu002D'})
     * 以表示一个负值，或一个ASCII加号{@code '+'}({@code '\u005Cu002B'})
     * 表示正值。返回结果是整数值。
     * ，
     * <p>如果发生以下任何一种情况，将引发类型{@code NumberFormatException}的异常：
     * <ul>
     *     <li>
     *         第一个参数为{@code null}或长度为零的字符串。
     *     </li>
     *     <li>
     *         基数小于{@link java.lang.Character#MIN_RADIX}或大于
     *         {@link java.lang.Character#MAX_RADIX}。
     *     </li>
     *     <li>
     *         字符串的任何字符都不是指定基底的数字，除了第一个字符可以是减号
     *         {@code '-'}({@code '\u005Cu002D'})或加号
     *         {@code '+'}({@code '\u005Cu002B'})，前提是字符串长度超过1。
     *     </li>
     *     <li>
     *         字符串表示的值不是{@code int}类型的值(超过长度)。
     *     </li>
     * </ul>
     *
     * <p>例如:
     * <blockquote><pre>
     * parseInt("0", 10) returns 0
     * parseInt("473", 10) returns 473
     * parseInt("+42", 10) returns 42
     * parseInt("-0", 10) returns 0
     * parseInt("-FF", 16) returns -255
     * parseInt("1100110", 2) returns 102
     * parseInt("2147483647", 10) returns 2147483647
     * parseInt("-2147483648", 10) returns -2147483648
     * parseInt("2147483648", 10) throws a NumberFormatException
     * parseInt("99", 8) throws a NumberFormatException
     * parseInt("Kona", 10) throws a NumberFormatException
     * parseInt("Kona", 27) returns 411787
     * </pre></blockquote>
     *
     * @param      s   包含要解析的整数表示的{@code String}。
     * @param      radix   解析{@code s}使用的基数。
     * @return     指定基数的字符串参数表示的整数。
     * @exception  NumberFormatException 如果 {@code String}
     *             不包含可解析的{@code int}。
     */
    public static int parseInt(String s, int radix)
                throws NumberFormatException
    {
        /*
         * 警告：在 VM 初始化期间，此方法可能会在 IntegerCache初始化之前被调用。必须注意
         * 不要使用valueOf方法。
         */

        if (s == null) {
            throw new NumberFormatException("null");
        }

        if (radix < Character.MIN_RADIX) {
            throw new NumberFormatException("radix " + radix +
                                            " less than Character.MIN_RADIX");
        }

        if (radix > Character.MAX_RADIX) {
            throw new NumberFormatException("radix " + radix +
                                            " greater than Character.MAX_RADIX");
        }

        // 正负数，在程序处理过程中，所有的数按照负数处理，最后根据此标识，转换数字对应的正负。
        boolean negative = false;
        // i记录处理的s中的第i字符，len记录字符串长度
        int i = 0, len = s.length();
        // limit所能处理的最大的数的范围
        int limit = -Integer.MAX_VALUE;

        if (len > 0) {
            /**
             * 判断第一位是否为符号位，即首位是否 < '0'
             * '-' -----> negative = true，并设置limit为Integer.MIN_VALUE
             * 不是'+' -------> throw NumberFormatException
             * len是否为 1, 数字不能仅有一个字符组成， -----> throw NumberFormatException
             */
            char firstChar = s.charAt(0);
            if (firstChar < '0') { // Possible leading "+" or "-"
                if (firstChar == '-') {
                    negative = true;
                    limit = Integer.MIN_VALUE;
                } else if (firstChar != '+') {
                    throw NumberFormatException.forInputString(s);
                }

                if (len == 1) { // Cannot have lone "+" or "-"
                    throw NumberFormatException.forInputString(s);
                }
                i++;
            }

            /**
             * 当前radix下，去除最后一位之后的最大值，如Integer的最大值，limit为-2147483647，
             * 在radix为10的情况下，去除之后为-214748364。
             * 当解析2147483657时，假设radix为10， 在解析最后一位时，此时result为-214748365，
             * 判断会抛出异常。就可以避免在下面的运算result *= radix中出现精度丢失。
             *
             * 当解析2147483648时，假设radix为10， 在解析最后一位时，此时result为-214748364，
             * 此时result的值和multmin相等，result < multmin为false，因此并不会抛出异常，
             * 在result * radix时，得到-2147483640也不会出现精度丢失风险，在下面的判断中，
             * result < digit + limit ,此时result为-2147483640, digit为8, limit为-2147483647，
             * 如果直接使用result - digit计算最大值，可能会导致溢出，limit + digit得到的值并不会溢出。
             * 此时digit + limit 为 -2147483639，大于result（-2147483640），因此抛出异常。
             */
            int multmin = limit / radix;
            int result = 0;
            while (i < len) {
                // 负数做累加可以避免在MAX_VALUE附近出现意外
                //获得该位置对应的数字
                int digit = Character.digit(s.charAt(i++), radix);
                // 判断当前是否越界
                if (digit < 0 || result < multmin) {
                    throw NumberFormatException.forInputString(s);
                }
                result *= radix;
                // 判断增加了当前值之后, 会不会越界
                if (result < limit + digit) {
                    throw NumberFormatException.forInputString(s);
                }
                //结果
                result -= digit;
            }
            return negative ? result : -result;
        } else {
            throw NumberFormatException.forInputString(s);
        }
    }

    /**
     * 将{@link CharSequence}参数解析为指定{@code radix}的有符号{@code int}，
     * 从指定的{@code beginindex}开始，并扩展到{@code endIndex - 1}。
     *
     * <p>此方法没有采取措施来防止{@code CharSequence}在解析时突然发生变化。
     *
     *
     * @param      s   包含要解析的{@code int}表示形式的{@code CharSequence}。
     * @param      beginIndex   开始索引，包含。
     * @param      endIndex     结束索引，不包含。
     * @param      radix   解析{@code s}所使用的基数。
     * @return     由指定基数的子序列表示的有符号的 {@code int}。
     *
     * @throws     NullPointerException  如果{@code s}是null.
     * @throws     IndexOutOfBoundsException  如果 {@code beginIndex} 是负数，
     *             或者 {@code beginIndex} 大于 {@code endIndex} ，
     *             或者 {@code endIndex} 大于 {@code s.length()}。
     * @throws     NumberFormatException  如果 {@code CharSequence} 在指定的
     *             {@code radix}中不包含可解析的{@code int}，或者{@code radix}
     *             小于{@link java.lang.Character#MIN_RADIX} 或者
     *             大于{@link java.lang.Character#MAX_RADIX}。
     * @since  9
     */
    public static int parseInt(CharSequence s, int beginIndex, int endIndex, int radix)
                throws NumberFormatException {
        // 检验字符序列是否为空。
        s = Objects.requireNonNull(s);

        /**
         * 边界检查，抛出IndexOutOfBoundsException
         * 开始索引 < 0
         * 开始索引 > 结束索引
         * 结束索引 > 字符串长度
         */
        if (beginIndex < 0 || beginIndex > endIndex || endIndex > s.length()) {
            throw new IndexOutOfBoundsException();
        }
        /**
         * radix检验，抛出 NumberFormatException
         * radix < 2
         * radix > 36
         */
        if (radix < Character.MIN_RADIX) {
            throw new NumberFormatException("radix " + radix +
                                            " less than Character.MIN_RADIX");
        }
        if (radix > Character.MAX_RADIX) {
            throw new NumberFormatException("radix " + radix +
                                            " greater than Character.MAX_RADIX");
        }

        // 是否为负数
        boolean negative = false;
        // 开始索引，i当前处理的sequence的索引
        int i = beginIndex;
        // limit所能处理的最大的数的范围
        int limit = -Integer.MAX_VALUE;

        if (i < endIndex) {
            // 处理第一个字符，如果这个字符是符号，则需要做对应的处理
            char firstChar = s.charAt(i);
            if (firstChar < '0') { // Possible leading "+" or "-"
                if (firstChar == '-') {
                    negative = true;
                    limit = Integer.MIN_VALUE;
                } else if (firstChar != '+') {
                    throw NumberFormatException.forCharSequence(s, beginIndex,
                            endIndex, i);
                }
                i++;
                if (i == endIndex) { // Cannot have lone "+" or "-"
                    throw NumberFormatException.forCharSequence(s, beginIndex,
                            endIndex, i);
                }
            }

            // 除了最后一位，所能处理的最大数，防止精度丢失
            int multmin = limit / radix;
            int result = 0;
            while (i < endIndex) {
                // Accumulating negatively avoids surprises near MAX_VALUE
                int digit = Character.digit(s.charAt(i), radix);
                if (digit < 0 || result < multmin) {
                    throw NumberFormatException.forCharSequence(s, beginIndex,
                            endIndex, i);
                }
                result *= radix;
                if (result < limit + digit) {
                    throw NumberFormatException.forCharSequence(s, beginIndex,
                            endIndex, i);
                }
                i++;
                result -= digit;
            }
            return negative ? result : -result;
        } else {
            throw NumberFormatException.forInputString("");
        }
    }

    /**
     * 将字符串参数解析为带符号的十进制整数。字符串中的字符必须都是十进制数字，
     * 除了第一个字符可能是一个ASCII 减号{@code '-'}({@code '\u005Cu002D'})
     * 表明是一个负数，或者是ASCII 加号{@code '+'}({@code '\u005Cu002B'})
     * 表明是一个正数。返回结果整数值，就像参数和基数 10 作为
     * {@link #parseInt(java.lang.String, int)}方法的参数一样。
     *
     * @param s    一个{@code String}，包含要解析的{@code int}表示形式
     * @return     十进制参数表示的整数值。
     * @exception  NumberFormatException  如果字符串不包含可解析的整数。
     */
    public static int parseInt(String s) throws NumberFormatException {
        return parseInt(s,10);
    }

    /**
     * 将字符串参数解析为第二个参数指定基数的无符号整数。无符号整数通常与负数相关的
     * 值映射为大于{@code MAX_VALUE}的正数。
     *
     * 字符串中的字符必须全部为指定基数的数字（由{@link java.lang.Character#digit(char, int)}
     * 是否返回非负值确定），除了第一个字符可能是一个ASCII加号{@code '+'}
     * ({@code '\u005Cu002B'})表示正值。返回结果是整数值。
     *
     * <p>如果发生以下任何一种情况，将引发类型{@code NumberFormatException}的异常：
     * <ul>
     *     <li>第一个参数为{@code null}或字符串长度为0。</li>
     *     <li>
     *         基数radix小于{@link java.lang.Character#MIN_RADIX}
     *         或大于{@link java.lang.Character#MAX_RADIX}。
     *     </li>
     *     <li>
     *         字符串中的任意字符不是指定基数的数字，除了第一个字符可以是加号
     *         {@code '+'}({@code '\u005Cu002B'})，前提是字符串长度超过1。
     *     </li>
     *     <li>
     *         字符串表示的值大于最大的无符号数{@code int}, 2<sup>32</sup>-1。
     *     </li>
     * </ul>
     *
     * @param      s   一个{@code String}，包含要解析的无符号整数表示形式
     * @param      radix   解析{@code s}使用的基数。
     * @return     指定基数的字符串参数的整数表示。
     * @throws     NumberFormatException 如果 {@code String}不包含可解析的{@code int}。
     * @since 1.8
     */
    public static int parseUnsignedInt(String s, int radix)
                throws NumberFormatException {
        if (s == null)  {
            throw new NumberFormatException("null");
        }

        int len = s.length();
        if (len > 0) {
            //收个字符如果是符号，不能为'-'
            char firstChar = s.charAt(0);
            if (firstChar == '-') {
                throw new
                    NumberFormatException(String.format("Illegal leading minus sign " +
                                                       "on unsigned string %s.", s));
            } else {
                /**
                 * 如果len <= 5，则解析为int的时候肯定不会出现精度丢失，因为在Character.MAX_RADIX为基底
                 * 的情况下， Integer.MAX_VALUE 为 6 个数字。
                 * 如果radix为10进制，且 len <= 9，Integer.MAX_VALUE 以 10 为基数是 10 个数字，因此肯定
                 * 不会出现精度丢失。(因为{@link #parseUnsignedInt(String)}的基数为10，加上这个判断就可
                 * 直接让该方法实际调用{@link #parseInt(String, int)}方法，而不是调用{@link Long#parseLong(String, int)}
                 * 方法)
                 */
                if (len <= 5 || // Integer.MAX_VALUE 在 Character.MAX_RADIX 为 6 个数字
                    (radix == 10 && len <= 9) ) { // Integer.MAX_VALUE 以 10 为基数是 10 个数字
                    return parseInt(s, radix);
                } else {
                    long ell = Long.parseLong(s, radix);
                    //判断高32位是否存在非零值，如果存在说明解析的值大于Integer.MAX_VALUE，则会抛出异常
                    if ((ell & 0xffff_ffff_0000_0000L) == 0) {
                        return (int) ell;
                    } else {
                        throw new
                            NumberFormatException(String.format("String value %s exceeds " +
                                                                "range of unsigned int.", s));
                    }
                }
            }
        } else {
            throw NumberFormatException.forInputString(s);
        }
    }

    /**
     * 将{@link CharSequence}参数解析为指定{@code radix}的无符号{@code int}，
     * 从指定的{@code beginindex}开始，并扩展到{@code endIndex - 1}。
     *
     * <p>此方法没有采取措施来防止{@code CharSequence}在解析时突然发生变化。
     *
     * @param      s   包含要解析的无符号{@code int}表示形式的{@code CharSequence}。
     * @param      beginIndex   开始索引，包含。
     * @param      endIndex     结束索引，不包含。
     * @param      radix   解析{@code s}所使用的基数。
     * @return     由指定基数的子序列表示的无符号的 {@code int}。
     *
     * @throws     NullPointerException  如果 {@code s} 为null.
     * @throws     IndexOutOfBoundsException  如果 {@code beginIndex} 是负数，
     *             或者 {@code beginIndex} 大于 {@code endIndex} ，
     *             或者 {@code endIndex} 大于 {@code s.length()}。
     * @throws     NumberFormatException  如果 {@code CharSequence} 在指定的
     *             {@code radix}中不包含可解析的{@code int}，或者{@code radix}
     *             小于{@link java.lang.Character#MIN_RADIX} 或者
     *             大于{@link java.lang.Character#MAX_RADIX}。
     * @since  9
     */
    public static int parseUnsignedInt(CharSequence s, int beginIndex, int endIndex, int radix)
                throws NumberFormatException {
        // 检验字符序列是否为空。
        s = Objects.requireNonNull(s);
        /**
         * 边界检查，抛出IndexOutOfBoundsException
         * 开始索引 < 0
         * 开始索引 > 结束索引
         * 结束索引 > 字符串长度
         */
        if (beginIndex < 0 || beginIndex > endIndex || endIndex > s.length()) {
            throw new IndexOutOfBoundsException();
        }
        int start = beginIndex, len = endIndex - beginIndex;

        if (len > 0) {
            char firstChar = s.charAt(start);
            if (firstChar == '-') {
                throw new
                    NumberFormatException(String.format("Illegal leading minus sign " +
                                                       "on unsigned string %s.", s));
            } else {
                if (len <= 5 || // Integer.MAX_VALUE in Character.MAX_RADIX is 6 digits
                        (radix == 10 && len <= 9)) { // Integer.MAX_VALUE in base 10 is 10 digits
                    return parseInt(s, start, start + len, radix);
                } else {
                    long ell = Long.parseLong(s, start, start + len, radix);
                    if ((ell & 0xffff_ffff_0000_0000L) == 0) {
                        return (int) ell;
                    } else {
                        throw new
                            NumberFormatException(String.format("String value %s exceeds " +
                                                                "range of unsigned int.", s));
                    }
                }
            }
        } else {
            throw new NumberFormatException("");
        }
    }

    /**
     * 将字符串参数解析为无符号十进制整数。字符串中的所有字符必须都是十进制数字，除了
     * 第一个字符可能是一个ASCII加号{@code '+'}({@code '\u005Cu002B'})。
     * 返回结果是整数值，就像是参数和基数10为给定参数的{@link #parseInt(java.lang.String, int)}方法一样。
     *
     * @param s   一个{@code String}，包含要解析的无符号整数表示形式
     * @return    参数的十进制无符号整数值。
     * @throws    NumberFormatException  如果字符串中不包含可解析的整数。
     * @since 1.8
     */
    public static int parseUnsignedInt(String s) throws NumberFormatException {
        return parseUnsignedInt(s, 10);
    }

    /**
     * 返回一个{@code Integer}对象, 该对象包含使用第二个参数为指定基数解析
     * {@code String}中的值。第一个参数被解释为由第二个参数为指定基数的有
     * 符号整数表示，就像参数作为{@link #parseInt(java.lang.String, int)}
     * 方法一样。返回结果是指定字符串的整数类型表示的{@code Integer}对象。
     *
     * <p>换句话说，此方法返回一个{@code Integer}对象，其值等于:
     *
     * <blockquote>
     *  {@code new Integer(Integer.parseInt(s, radix))}
     * </blockquote>
     *
     * @param      s   被解析的字符串。
     * @param      radix 用于解释{@code s}的基数。
     * @return     {@code Integer}对象，保存指定由指定的基数中的字符串参数表示。
     *
     * @exception NumberFormatException 如果 {@code String} 不包含可解析的{@code int}。
     */
    public static Integer valueOf(String s, int radix) throws NumberFormatException {
        return Integer.valueOf(parseInt(s,radix));
    }

    /**
     * 返回一个包含指定{@code String}值的{@code Integer}对象。该参数被解释为
     * 以有符号十进制整数，就像参数作为{@link #parseInt(java.lang.String)}方法
     * 一样。返回结果是指定字符串的整数类型表示的{@code Integer}对象。
     *
     * <p>换句话说，此方法返回一个{@code Integer}对象，其值等于:
     *
     * <blockquote>
     *  {@code new Integer(Integer.parseInt(s))}
     * </blockquote>
     *
     * @param      s   被解析的字符串。
     * @return     {@code Integer}对象，保存字符串参数表示的值。
     * @exception  NumberFormatException  如果字符串不能解析为整数。
     */
    public static Integer valueOf(String s) throws NumberFormatException {
        return Integer.valueOf(parseInt(s, 10));
    }

    /**
     * 缓存以支持 JLS 要求的 -128 到 127 (包括)之间值自动装箱的对象表示语义。
     *
     * 首次使用时会初始化缓存。缓存的大小可以由{@code -XX:AutoBoxCacheMax=<size>}
     * 选项控制。在 VM 初始化期间，java.lang.Integer.IntegerCache.high 属性可以
     * 设置并保存在 java.internal.misc.VM 类的私有系统属性中。
     */
    private static class IntegerCache {
        static final int low = -128;
        static final int high;
        static final Integer cache[];

        static {
            // high 值可能由属性配置
            int h = 127;
            String integerCacheHighPropValue =
                VM.getSavedProperty("java.lang.Integer.IntegerCache.high");
            if (integerCacheHighPropValue != null) {
                try {
                    int i = parseInt(integerCacheHighPropValue);
                    i = Math.max(i, 127);
                    // 数组大小的最大值是 Integer.MAX_VALUE
                    h = Math.min(i, Integer.MAX_VALUE - (-low) -1);
                } catch( NumberFormatException nfe) {
                    // If the property cannot be parsed into an int, ignore it.
                }
            }
            high = h;

            // 初始化cache缓存
            cache = new Integer[(high - low) + 1];
            int j = low;
            for(int k = 0; k < cache.length; k++)
                cache[k] = new Integer(j++);

            // 设置范围[-128 , 127]必须实习(JLS7 5.1.7)
            assert IntegerCache.high >= 127;
        }

        private IntegerCache() {}
    }

    /**
     * 返回指定{@code int}值代表的{@code Integer}实例。如果一个新的{@code Integer}
     * 实例不是必须的，此方法通常会优先使用构造器{@link #Integer(int)},因为此方法
     * 可能通过缓存经常请求的值产生明显更好的空间和时间性能。
     *
     * 此方法将始终缓存 -128 到 127 (包括)范围内的值，并且可能缓存该范围之外的其他值。
     *
     * @param  i {@code int} 值.
     * @return {@code i}代表的{@code Integer} 实例。
     * @since  1.5
     */
    @HotSpotIntrinsicCandidate
    public static Integer valueOf(int i) {
        if (i >= IntegerCache.low && i <= IntegerCache.high)
            return IntegerCache.cache[i + (-IntegerCache.low)];
        return new Integer(i);
    }

    /**
     *  {@code Integer} 的值.
     *
     * @serial
     */
    private final int value;

    /**
     * 构造一个新的已分配的{@code Integer}对象，该对象表示指定的{@code int}值。
     *
     *
     * @param   value   {@code Integer}对象表示的值。
     *
     * @deprecated
     * 很少适合使用这个构造函数。静态工厂{@link #valueOf(int)}通常是更好的选择，
     * 因为它可能产生更好的时间和空间性能。
     */
    @Deprecated(since="9")
    public Integer(int value) {
        this.value = value;
    }

    /**
     * 构造一个新的已分配的{@code Integer}对象，该对象由指定{@code String}
     * 参数表示的{@code int}值。字符串转换为{@code int}值，就像是使用以10为
     * 基数的{@code parseInt}方法。
     *
     * @param   s   {@code String} 被转化为 {@code Integer}
     * @throws      NumberFormatException 如果 {@code String} 不包含可转换的整数。
     *
     * @deprecated
     * 很少适合使用这个构造函数。使用{@link #parseInt(String)}把字符串转换
     * 为基本类型{@code int}，或者使用{@link #valueOf(String)}把字符串
     * 转换为一个{@code Integer}对象。
     */
    @Deprecated(since="9")
    public Integer(String s) throws NumberFormatException {
        this.value = parseInt(s, 10);
    }

    /**
     * 返回 此{@code Integer}的值在进行收缩基本类型转换之后的{@code byte}。
     *
     * @jls 5.1.3 Narrowing Primitive Conversions
     */
    public byte byteValue() {
        return (byte)value;
    }

    /**
     * 返回 此{@code Integer}的值在进行收缩基本类型转换之后的{@code short}。
     * @jls 5.1.3 Narrowing Primitive Conversions
     */
    public short shortValue() {
        return (short)value;
    }

    /**
     * 返回此 {@code Integer} 的值为{@code int}。
     */
    @HotSpotIntrinsicCandidate
    public int intValue() {
        return value;
    }

    /**
     * 返回 此{@code Integer}的值在进行加宽基本类型转换之后的{@code long}。
     * @jls 5.1.2 Widening Primitive Conversions
     * @see Integer#toUnsignedLong(int)
     */
    public long longValue() {
        return (long)value;
    }

    /**
     * 返回 此{@code Integer}的值在进行加宽基本类型转换之后的{@code float}。
     * @jls 5.1.2 Widening Primitive Conversions
     */
    public float floatValue() {
        return (float)value;
    }

    /**
     * 返回 此{@code Integer}的值在进行加宽基本类型转换之后的{@code double}。
     * @jls 5.1.2 Widening Primitive Conversions
     */
    public double doubleValue() {
        return (double)value;
    }

    /**
     * 返回此{@code Integer}的值代表的{@code String}对象。此值被转换为有符号十进制
     * 表示形式并作为字符串返回，就像将整数值提供给{@link Integer#toString(int)}方法
     * 的参数一样。
     *
     * @return  基数为 &nbsp;10 的该对象的值的字符串表示。
     */
    public String toString() {
        return toString(value);
    }

    /**
     * 返回{@code Integer}的哈希码。
     *
     * @return  此对象的哈希码值, 等于此{@code Integer}对象表示的基本类型{@code int}值。
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(value);
    }

    /**
     * 返回{@code int}值的哈希码; 与{@code Integer.hashCode()}兼容。
     *
     * @param value 用于哈希的值
     * @since 1.8
     *
     * @return  {@code int} 值的哈希码值。
     */
    public static int hashCode(int value) {
        return value;
    }

    /**
     * 比较此对象与指定对象。当且仅当参数不是{@code null}并且包含与此对象相同
     * 的{@code int}值的{@code Integer}对象时，结果为{@code true}。
     *
     * @param   obj   要与之比较的对象。
     * @return  如果对象相同 {@code true} ;
     *          其他情况 {@code false} 。
     */
    public boolean equals(Object obj) {
        if (obj instanceof Integer) {
            return value == ((Integer)obj).intValue();
        }
        return false;
    }

    /**
     * 确定具有指定名称的系统属性的整数值。
     *
     * <p>第一个参数被视为系统属性的名称。系统属性可以通过访问
     * {@link java.lang.System#getProperty(String)}得到。
     * 使用{@link Integer#decode(String) decode}支持的语法将此属性
     * 的字符串值解释为整数值，并返回表示此值的{@code Integer}对象。
     *
     * <p>如果没有具体指定名称的属性，如果指定的名称为empty或{@code null}，
     * 或者属性没有正确的数字格式，则返回{@code null}。
     *
     * <p>换句话说，此方法返回一个相等值的{@code Integer}对象：
     *
     * <blockquote>
     *  {@code getInteger(nm, null)}
     * </blockquote>
     *
     * @param   nm   属性名称。
     * @return  属性的 {@code Integer}值。
     * @throws  SecurityException 原因与
     *          {@link System#getProperty(String) System.getProperty} 相同
     * @see     java.lang.System#getProperty(java.lang.String)
     * @see     java.lang.System#getProperty(java.lang.String, java.lang.String)
     */
    public static Integer getInteger(String nm) {
        return getInteger(nm, null);
    }

    /**
     * 确定具有指定名称的系统属性的整数值。
     *
     * <p>第一个参数被视为系统属性的名称。系统属性可以通过访问
     * {@link java.lang.System#getProperty(String)}得到。
     * 使用{@link Integer#decode(String) decode}支持的语法将此属性
     * 的字符串值解释为整数值，并返回表示此值的{@code Integer}对象。
     *
     * <p>第二个参数为默认值。如果没有指定属性名称，或者属性没有正确的数字格式，
     * 或者指定的名称为empty或{@code null}，则返回第二个参数值表示的{@code Integer}对象。
     *
     * <p>换句话说，此方法返回一个值相等的{@code Integer}对象：
     *
     * <blockquote>
     *  {@code getInteger(nm, new Integer(val))}
     * </blockquote>
     *
     * 但是在实践中，它可以通过以下方法实现：
     *
     * <blockquote><pre>
     * Integer result = getInteger(nm, null);
     * return (result == null) ? new Integer(val) : result;
     * </pre></blockquote>
     *
     * 避免在不需要默认值时，分配不必要的{@code Integer}对象。
     *
     * @param   nm   属性名称.
     * @param   val   default value.
     * @return  属性的 {@code Integer}值。
     * @throws  SecurityException 原因与
     *          {@link System#getProperty(String) System.getProperty}相同
     * @see     java.lang.System#getProperty(java.lang.String)
     * @see     java.lang.System#getProperty(java.lang.String, java.lang.String)
     */
    public static Integer getInteger(String nm, int val) {
        Integer result = getInteger(nm, null);
        return (result == null) ? Integer.valueOf(val) : result;
    }

    /**
     * 返回具有指定名称的系统属性的整数值。第一个参数被视为系统属性的名称。
     * 系统属性可以通过访问 {@link java.lang.System#getProperty(String)}得到。
     * 使用{@link Integer#decode(String) decode}支持的语法将此属性
     * 的字符串值解释为整数值，并返回表示此值的{@code Integer}对象；综上所述：
     * <ul>
     *     <li>
     *         如果此属性值以两个 ASCII 字符 {@code 0x} 或 ASCII 字符 {@code #}开始，
     *         后面没有减号，则其余部分将会被解析为十六进制整数，与以16为基底的方法
     *         {@link #valueOf(java.lang.String, int)}一样。
     *     </li>
     *     <li>
     *         如果属性值以 ASCII 字符{@code 0}开头，后面跟另外一个字符，
     *         则将其解析为八进制整数，与以8为基底的方法
     *         {@link #valueOf(java.lang.String, int)}一样。
     *     </li>
     *     <li>
     *         否则，属性值将会被解析为十进制整数，与以10为基底的方法
     *        {@link #valueOf(java.lang.String, int)}一样。
     *     </li>
     * </ul>
     *
     * <p>第二个参数为默认值。如果没有指定属性名称，或者属性没有正确的数字格式，
     * 或者指定的名称为empty或{@code null}，则返回默认值。
     *
     * @param   nm   属性名称。
     * @param   val   默认值。
     * @return  该属性的 {@code Integer} 值。
     * @throws  SecurityException 原因与
     *          {@link System#getProperty(String) System.getProperty}相同。
     * @see     System#getProperty(java.lang.String)
     * @see     System#getProperty(java.lang.String, java.lang.String)
     */
    public static Integer getInteger(String nm, Integer val) {
        String v = null;
        try {
            v = System.getProperty(nm);
        } catch (IllegalArgumentException | NullPointerException e) {
        }
        if (v != null) {
            try {
                return Integer.decode(v);
            } catch (NumberFormatException e) {
            }
        }
        return val;
    }

    /**
     * 将{@code String}解码为{@code Integer}。
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
     * "{@code #}", 或前导零) 由 {@code Integer.parseInt}方法解析，其基数为指定
     * 基数(10, 16, 或 8)。此字符序列必须表示为正值，或抛出{@link NumberFormatException}
     * 异常。如果指定的{@code String}第一个字符为减号，则结果为负。
     * {@code String}不允许使用空格字符。
     *
     *
     * @param     nm 用于解析的{@code String}。
     * @return    {@code nm}表示的{@code Integer}对象，拥有{@code int}值，
     * @exception NumberFormatException  如果{@code String} 不包含可解析整数。
     * @see java.lang.Integer#parseInt(java.lang.String, int)
     */
    public static Integer decode(String nm) throws NumberFormatException {
        int radix = 10;
        int index = 0;
        boolean negative = false;
        Integer result;

        if (nm.length() == 0)
            throw new NumberFormatException("Zero length string");
        char firstChar = nm.charAt(0);
        // 处理符号, 如果有的话
        if (firstChar == '-') {
            negative = true;
            index++;
        } else if (firstChar == '+')
            index++;

        // 处理基数说明符, 如果有的话
        if (nm.startsWith("0x", index) || nm.startsWith("0X", index)) {
            index += 2;
            radix = 16;
        }
        else if (nm.startsWith("#", index)) {
            index ++;
            radix = 16;
        }
        else if (nm.startsWith("0", index) && nm.length() > 1 + index) {
            index ++;
            radix = 8;
        }

        if (nm.startsWith("-", index) || nm.startsWith("+", index))
            throw new NumberFormatException("Sign character in wrong position");

        try {
            //内部调用了pasreInt方法
            result = Integer.valueOf(nm.substring(index), radix);
            result = negative ? Integer.valueOf(-result.intValue()) : result;
        } catch (NumberFormatException e) {
            // 如果数值为 Integer.MIN_VALUE, 我们将在这里结束。 下一行处理这种情况，并导致任何真正的格式错误被重新抛出。
            // 在try中如果是Integer.MIN_VALUE的情况，没有加符号是不能处理的，会抛出NumberFormatException
            String constant = negative ? ("-" + nm.substring(index))
                                       : nm.substring(index);
            result = Integer.valueOf(constant, radix);
        }
        return result;
    }

    /**
     * 以数字方式比较两个{@code Integer}对象。
     *
     * @param   anotherInteger   要比较的 {@code Integer}。
     * @return  如果此 {@code Integer}与参数 {@code Integer}相等，值为{@code 0}；
     *          如果此 {@code Integer}在数值上小于该参数{@code Integer}，值小于{@code 0};
     *          如果此 {@code Integer}在数值上大于该参数{@code Integer}，值大于{@code 0};
     *          (有符号比较)。
     * @since   1.2
     */
    public int compareTo(Integer anotherInteger) {
        return compare(this.value, anotherInteger.value);
    }

    /**
     * 以数字方式比较两个{@code int}值。返回的值与以下方法返回值相同：
     * <pre>
     *    Integer.valueOf(x).compareTo(Integer.valueOf(y))
     * </pre>
     *
     * @param  x 第一个比较的 {@code int}
     * @param  y 第二个比较的 {@code int}
     * @return   如果 {@code x == y}，值为 {@code 0};
     *           如果 {@code x < y}，值小于 {@code 0};
     *           如果 {@code x > y}，值大于 {@code 0}。
     * @since 1.7
     */
    public static int compare(int x, int y) {
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }

    /**
     * 以无符号数值方式比较两个{@code int}值
     *
     * @param  x 第一个比较的 {@code int}
     * @param  y 第二个比较的 {@code int}
     * @return   如果 {@code x == y}，值为 {@code 0};
     *           以无符号值，如果 {@code x < y}，值小于 {@code 0};
     *           以无符号值，如果 {@code x > y}，值大于 {@code 0}。
     * @since 1.8
     */
    public static int compareUnsigned(int x, int y) {
        return compare(x + MIN_VALUE, y + MIN_VALUE);
    }

    /**
     * 通过无符号转换将参数转换为{@code long}。在无符号转换为{@code long}的过程中，
     * {@code long}的高阶32位为零，而低阶32位和整数参数的位相等。
     *
     * 所以，零和正的{@code int}值映射为数值相等的{@code long}值，而负的{@code int}
     * 值映射为 输入值加上2<sup>32</sup>的相等值。
     *
     * @param  x 转换为无符号{@code long}的值
     * @return 将参数转换为无符号的 {@code long}值
     * @since 1.8
     */
    public static long toUnsignedLong(int x) {
        return ((long) x) & 0xffffffffL;
    }

    /**
     * 返回将第一个参数除以第二个参数的无符号商，其中每个参数和结果都被解释为无符号值。
     *
     * <p>注意，在二进制补码运算中，如果两个操作数被认为都是有符号的或无符号的，那么
     * 另外三种基本的加，减 和 乘的算术运算在位上是相同的。因此，不单独提供{@code addUnsigned}等方法。
     *
     * @param dividend 被除数
     * @param divisor 除数
     * @return 第一个参数除以第二个参数的无符号商
     * @see #remainderUnsigned
     * @since 1.8
     */
    public static int divideUnsigned(int dividend, int divisor) {
        // 代替棘手的代码，现在仅使用long算法。
        return (int)(toUnsignedLong(dividend) / toUnsignedLong(divisor));
    }

    /**
     * 返回第一个参数除以第二个参数的无符号余数，其中每个参数和结果都被解释为无符号值。
     *
     * @param dividend 被除数
     * @param divisor 除数
     * @return 第一个参数除以第二个参数的无符号余数
     * @see #divideUnsigned
     * @since 1.8
     */
    public static int remainderUnsigned(int dividend, int divisor) {
        // 代替棘手的代码，现在仅使用long算法。
        return (int)(toUnsignedLong(dividend) % toUnsignedLong(divisor));
    }


    // Bit twiddling

    /**
     * 用于以二进制补码形式表示{@code int}值的位数。
     *
     * @since 1.5
     */
    @Native public static final int SIZE = 32;

    /**
     * 用于以二进制补码形式表示的{@code int}值的字节数。
     *
     * @since 1.8
     */
    public static final int BYTES = SIZE / Byte.SIZE;

    /**
     * 返回{@code int}值，最多只有一位，位于指定{@code int}值中最高位("最左侧")的一位。
     * 如果指定的值在其二进制补码表示中没有 1 位，即如果它等于零，则返回零。
     *
     * @param i 要计算其最高位的值
     * @return 具有一个1位的 {@code int}值，位于指定值中最高阶 1 位的位置；如果指定值
     *         本身等于0，则为零。
     * @since 1.5
     */
    public static int highestOneBit(int i) {
        /**
         * 符号移动：
         * 正数，三码（源码、反码、补码）相同，所以无论左移还是右移都是补0。
         * 负数的补码就需要注意，左移在右边补0，右移需要在左边补1。
         *
         * 1.有符号右移>>（若正数,高位补0,负数,高位补1）
         * 正数:例如4>>2
         * 首先写出4的二进制数，因为是正数所以最高位为0，也就是第一个
         *  0000 0100
         * 右移两位得到（高位补0）
         *  0000  0001
         * 结果为：1，右移n位也就是4/（2^n）
         *
         * 负数：例如-4>>2（高位补1）
         * 首先写出-4的二进制数源码,因为是负数所以最高位为1   (负数的源码是负数的绝对值)
         * 1000 0100
         * 然后写出-4反码：保证符号位不变，其余位置取反
         * 1111 1011（反码）
         * 最后写出-4的补码：在反码的基础上加1
         * 1111 1100（补码）
         * 右移2位： 在高位补1
         * 1111 1111
         * 根据补码写出原码才是我们所求的结果， 保留符号位，然后减1取反       （或按位取反再加上1）
         * 1111 1110（减1的结果）
         * 1000 0001（取反的结果）
         * 结果为：-1
         *
         * 2：无符号右移>>>(不论正负,高位均补0)  注意：无符号，所以都是当正数操作的
         */

        /** 《Java编程思想》：只有数值右端的低5位才有用。这样可防止我们移位超过int类型值所具有的位数。
         * （译注：因为2的5次方为32，而int类型值只有32位。）若对于一个long类型的数值进行处理，最后得到的
         * 结果也是long，此时只会用到数值右端的低6位，以防止位移超过long型数值具有的位数。
         *  对于移位操作：左操作数 移位操作符 右操作数。在Java中int类型大小为32bits，
         *  long类型大小为64bits。Java不允许一次位移操作移动左操作数的所有位，也就是说int类型的左操作数
         *  最多只能移动31位，long类型的左操作数只能移动63位。31对应的二进制为11111共5位，
         *  63对应的二进制为111111共6位。左操作数位int型，如果右操作数为32（二进制100000共6位），
         *  那么只取低5位，也就是00000，结果就是没变化。
         *
         * 因此对于0之外的数， MIN_VALUE >>> numberOfLeadingZeros(i)即可算得到结果，相当于把
         * 1000 0000 0000 0000 0000 0000 0000 0000 无符号右移i的最左侧0的个数
         * 对于0，numberOfLeadingZeros(i)得到的结果为 32， 移动32 位得到的结果依旧为MIN_VALUE
         * 因此，为了处理这种情况，需要进行 i & 操作。
         */
        return i & (MIN_VALUE >>> numberOfLeadingZeros(i));
    }

    /**
     * 返回{@code int}值，最多只有一位，位于指定{@code int}值中最低位("最右侧")的一位。
     * 如果指定的值在其二进制补码表示中没有 1 位，即如果它等于零，则返回零。
     *
     * @param i  要计算其最高位的值
     * @return 具有一个1位的 {@code int}值，位于指定值中最低阶 1 位的位置；如果指定值
     *         本身等于0，则为零。
     * @since 1.5
     */
    public static int lowestOneBit(int i) {
        // HD, Section 2-1
        /**
         * 负数在计算机底层的存储为补码，即除符号位，各位取反 +1，如 -1，原码为：
         * 1000 0000 0000 0000 0000 0000 0000 0001
         * 反码为:
         * 1111 1111 1111 1111 1111 1111 1111 1110
         * 补码为反码+1：
         * 1111 1111 1111 1111 1111 1111 1111 1111
         * 在进行 & 操作后，得到的即为最低位。
         */

        return i & -i;
    }

    /**
     * 返回指定{@code int}值的二进制补码表示中最高位（"最左侧"）一位之前的零位个数。
     * 如果指定的值在其2的补码表示中没有一位，换句话说，如果它等于0，则返回32。
     *
     * <p>请注意，此方法与以2为底的对数紧密相关。
     * 对于所有正的{@code int}值x:
     * <ul>
     *     <li>
     *         floor( log<sub>2</sub>(x) ) = {@code 31 - numberOfLeadingZeros(x)}
     *     </li>
     *     <li>
     *         ceil( log<sub>2</sub>(x) ) = {@code 32 - numberOfLeadingZeros(x - 1)}
     *     </li>
     * </ul>
     *
     * @param i 要计算其前导零个数的值。
     * @return 指定的 {@code int}值的二进制补码表示中 最高位（"最左侧"）一位前的零位数，如果
     *         该值等于零，则为32。
     * @since 1.5
     */
    @HotSpotIntrinsicCandidate
    public static int numberOfLeadingZeros(int i) {
        // HD, Count leading 0's
        if (i <= 0)
            return i == 0 ? 32 : 0;
        int n = 31;
        if (i >= 1 << 16) { n -= 16; i >>>= 16; }
        if (i >= 1 <<  8) { n -=  8; i >>>=  8; }
        if (i >= 1 <<  4) { n -=  4; i >>>=  4; }
        if (i >= 1 <<  2) { n -=  2; i >>>=  2; }
        return n - (i >>> 1);
    }

    /**
     * 返回指定{@code int}值的二进制补码表示中最低位（"最右侧"）一位之前的零位个数。
     * 如果指定的值在其2的补码表示中没有一位，换句话说，如果它等于0，则返回32。
     *
     * @param i 要计算其尾随零个数的值。
     * @return 指定的 {@code int}值的二进制补码表示中最低位（"最右侧"）一位前的零位数，如果
     *         该值等于零，则为32。
     * @since 1.5
     */
    @HotSpotIntrinsicCandidate
    public static int numberOfTrailingZeros(int i) {
        // HD, Figure 5-14
        int y;
        if (i == 0) return 32;
        int n = 31;
        // 将 i 的低 16 位移动到高 16 位的位置，如果不为零，说明低 16 位存在 1 ，则结果需要减去16
        y = i <<16; if (y != 0) { n = n -16; i = y; }
        // 将 i 的低 8 位移动到高 8 位的位置，如果不为零，说明低 8 位存在 1，则结果减去 8
        y = i << 8; if (y != 0) { n = n - 8; i = y; }
        // 将 i 的低 4 位移动到高 4 位的位置，如果不为零，说明低 4 位存在 1，则结果减去 4
        y = i << 4; if (y != 0) { n = n - 4; i = y; }
        // 将 i 的低 2 位移动到高 2 位的位置，如果不为零，说明低 2 位存在 1，则结果减去 2
        y = i << 2; if (y != 0) { n = n - 2; i = y; }
        // 此时只有高 2 位可能存在 1，需要判断这个 1 在什么位置， i << 1，如果为 0 ，说明为 10， 如果为 1，说明为 01
        // 如果为 0，左移 31 位依然为 0，则 n 即为结果， 如果为 1， 左移 31 位为 1，n - 1即为结果。
        return n - ((i << 1) >>> 31);
    }

    /**
     * 返回指定的{@code int}值的二进制补码表示中的 1 位的个数。此方法有时
     * 称为<i>人口数量</i>。
     *
     * @param i 要对其进行位统计的值
     * @return 指定 {@code int}值的二进制补码表示中的位数。
     * @since 1.5
     */
    @HotSpotIntrinsicCandidate
    public static int bitCount(int i) {
        // HD, Figure 5-2
        /**
         * 算法思路：
         *      1. 求每两位二进制数中1的个数
         *      2. 求每四位二进制数中1的个数
         *      3. 依次类推，求到三十二位二进制中1的个数，即需要的结果。
         *
         * 一、两位二进制中 1 的个数：
         *     首先来分析求两位怎么求，两位二进制数有四种可能，它们所对应的1的个数（在这里个数也使用二进制来表示）如下所示，
         *     二进制数             二进制位为1的个数
         *       00                      00
         *       01                      01
         *       10                      01
         *       11                      10
         *     也就是说我们要将这个int值每两位左边所示的二进制位，变成右边所示的二进制位。
         *     将左列标记为l，右列标记为r，则r = l - x，观察上述表格可以发现x = l >>> 1，即 r = l - (l >>> 1)。
         *       l          r          x
         *      00         00         00
         *      01         01         00
         *      10         01         01
         *      11         10         01
         * 上述我们讨论了只有两个二进制位的情况，如果对于整个int32位来说，转化关系是怎样的呢？
         *     在这之前先来讨论4位的情况，举个例子说吧：
         *          现在有0111这个数，我们要求每两位的1的个数，目标值是0110，如果直接代入上述公式，我们得到的是0100，过程：
         *              0111 >>> 1 = 0011   ①
         *              0111 - 0011 = 0100  ②
         *          而理想的运算过程为:
         *              0111 - 0001 = 0110  ③
         *     对比可知道上面第一步求得的0011是不对的，也就是说我们不能直接右移一位得到减数，那么应该怎么做？
         *     仔细分析可以发现，减数之间的区别在于右移时从高位传到低位的那个1，根据表格我们知道x（x代表减数）的高位不可能是1，
         *     而1又会因为右移从高位传过来，所以在右移之后，我们需要与上0101来消除这个高位影响。将这个扩展到32位二进制数上，
         *     我们就得到公式i = i - ((i >>> 1) & 0x55555555);。
         */
        i = i - ((i >>> 1) & 0x55555555);
        /**
         * 接着看四位的求法。
         *    这里我们需要注意一点，此时的二进制位已经不再仅仅代表二进制位，我们应该把两位二进制位看成一个整体，当成个数来看。
         *    也就是说，我们需要对这四位数进行拆分，拆成两组，每组分别代表这两位包含的1二进制位的个数。那么这4个二进制位上面
         *    所包含的所有为1的二进制位的个数为两组的和，也就是高位所代表的二进制数与低位所代表的二进制数的和。比如有这样一个
         *    4位的二进制数，????，它所包含的1位的个数为，00?? + 00??，注意前面的两个0，高2位一组， 低2位一组，都按照低2
         *    位方式计算，根据这个原理我们可以得到下表，可以对照着这个表格加深理解。
         *            l                    r
         *          0000                 0000
         *          0001                 0001
         *          0010                 0010
         *          0100                 0001
         *          1000                 0010
         *          0101                 0010
         *          0110                 0011
         *          1000                 0010
         *          1001                 0011
         *          1010                 0100
         *
         *    我们最终的到的是个四位数，但是我们只需要两位数进行求和运算，所以为了消除高位的影响，我们需要与上0011，那么就得到
         *    第二个公式(i & 0x33333333) + ((i >>> 2) & 0x33333333); 这里要讲一下，为什么0x33333333要&两次，而不是放
         *    在外面一起&。这是因为00?? & 00?? 可能会产生进位，这样的话，如果放在外面与就会消除这个进位，使结果不正确。
         */
        i = (i & 0x33333333) + ((i >>> 2) & 0x33333333);
        /**
         * 这几个位数与四位计算的原理是一样的。
         * 八位：向右移四位，与原数相加，因为这里四位中最大的可能数为0100，产生进位也只是1000，不会影响到前面的4位数，所以八位的
         *      时候可以在外面&，得到公式(i + (i >>> 4)) & 0x0f0f0f0f。
         */
        i = (i + (i >>> 4)) & 0x0f0f0f0f;
        /**
         * 十六位：i = (i + (i >>> 8)) & 0x00ff00ff;
         */
        i = i + (i >>> 8);
        /**
         * 三十二位：i = (i + (i >>> 16)) & 0x0000ffff;
         */
        i = i + (i >>> 16);
        /**
         * 我这里表达形式稍微与源码有点不一样，源码是把消除部分放到了最后，即i = i & 0x3f; ,而省略了十六位和三十二位那里的与操作，
         * 因为最后面二进制位为1的个数不可能超过100000这个二进制数所以这两步与操作可以直接放到最后用一步来代替，最终我们就得到所有
         * 的公式：
         */
        return i & 0x3f;
    }

    /**
     * 返回通过向左旋转指定的{@code int}值的二进制补码表示形式获得的值。
     * (位移超过最左或最高的，从最右或最低重新进入)。
     *
     * <p>注意，具有负距离的左旋转相当于右旋转：
     * {@code rotateLeft(val, -distance) == rotateRight(val, distance)}。
     * 还要注意，32 的任意倍数的旋转是不变的，所以除了距离的最后五位之外都可以忽略，
     * 即便距离为负值：
     * {@code rotateLeft(val, distance) == rotateLeft(val, distance & 0x1F)}
     *
     * @param i 要左旋转的值。
     * @param distance 左旋转的距离。
     * @return 将指定 {@code int}值的二进制补码表示形式向左旋转指定位数得到的int值。
     * @since 1.5
     */
    public static int rotateLeft(int i, int distance) {
        /**
         * 循环移位就是把数值变成二进制，然后循环移动的过程；换句话说，循环移位就是将移出的低位
         * 放到该数的高位（循环右移）或把移出的高位放到该数的低位（循环左移），左移，和右移动都
         * 是对整数进行的操作，在Win32控制台应用程序中，整形占4Byte节32bit。
         *
         * 循环左移的过程可以分为3步： 
         *      1.将x左端的n位先移动到y的低n位中，x>>(32-n); 
         *      2.将x左移n位，其右面低位补0，x<<n; 
         *      3.进行按位或运算(x >> (32 - n) | (x << n));
         *
         * 所有的右边操作数为负数的情况都等同于取该操作数补码的后面5位于0b11111逻辑与
         * -5的补码：11111011 后面五位即 11011&0b11111  后得到11011 
         * 也就是27  相当于无符号右移-5位就相当于无符号右移27位
         */
        return (i << distance) | (i >>> -distance);
    }

    /**
     * 返回通过向右旋转指定的{@code int}值的二进制补码表示形式获得的值。
     * ( 移位超过最右或最低的，从最左或最高重新进入 )。
     *
     * <p>注意，具有负距离的右旋转相当于左旋转：
     * {@code rotateRight(val, -distance) == rotateLeft(val, distance)}。
     * 还要注意，32 的任意倍数的旋转是不变的，所以除了距离的最后五位之外都可以忽略。
     * 即便距离为负值：
     * {@code rotateLeft(val, distance) == rotateLeft(val, distance & 0x1F)}。
     *
     * @param i 要右旋转的值。
     * @param distance 右旋转的距离。
     * @return 将指定 {@code int}值的二进制补码表示形式向右旋转指定位数得到的int值。
     * @since 1.5
     */
    public static int rotateRight(int i, int distance) {
        return (i >>> distance) | (i << -distance);
    }

    /**
     * 返回将指定的{@code int}值的二进制补码表示进行位反转所得到的int值。
     *
     * @param i 要反转的值
     * @return 将指定 {@code int}值的二进制补码表示进行位反转得到的值。
     * @since 1.5
     */
    public static int reverse(int i) {
        // HD, Figure 7-1
        /**
         * 16进制的5为0101，或操作前半部分首先取出i的所有奇数位，然后整体左移一位，这样实现i的奇数位
         * 左移一位变成偶数位；或操作后半部分先右移，即将偶数位右移变成奇数位，然后再取出奇数位。这样
         * 就完成了32位中奇数位与偶数位的交换。
         * 举例：
         *      0101 0101 0101 0101 0101 0101 0101 0101
         * 1. 进行( i & 0x55555555)得到 : 0101 0101 0101 0101 0101 0101 0101 0101
         *    左移 1 位得到 : 1010 1010 1010 1010 1010 1010 1010 1010 (把两位中的右侧放到左侧)
         * 2. 进行 (i >>> 1)得到 : 0010 1010 1010 1010 1010 1010 1010 1010
         *    & 0x55555555得到 : 0010 1010 1010 1010 1010 1010 1010 1010 (把两位中的左侧放到右侧)
         *    | 合并上面两步操作 : 1010 1010 1010 1010 1010 1010 1010 1010 , 等价于左右两位交换位置。
         */
        i = (i & 0x55555555) << 1 | (i >>> 1) & 0x55555555;
        /**
         * 这句同样是实现交换，只不过3对应的16进制为0011，
         * 此时 i = 1010 1010 1010 1010 1010 1010 1010 1010,
         * 1. 进行 (i & 0x33333333)得到 : 0010 0010 0010 0010 0010 0010 0010 0010
         *    左移 2 位得到 : 1000 1000 1000 1000 1000 1000 1000 1000 (把四位中的低两位放到高两位)
         * 2.进行 (i >>> 2) 得到 : 0010 1010 1010 1010 1010 1010 1010 1010,
         *    & 0x33333333得到 : 0010 0010 0010 0010 0010 0010 0010 0010 (把四位中的高两位放到低两位)
         *    | 合并 : 1010 1010 1010 1010 1010 1010 1010 1010 , 等价于把 4 位中的高两位和低两位交换。
         */
        i = (i & 0x33333333) << 2 | (i >>> 2) & 0x33333333;
        /**
         * f对应的16进制为1111,
         * 此时 i = 1010 1010 1010 1010 1010 1010 1010 1010
         * 1. 进行 (i & 0x0f0f0f0f)得到 : 0000 1010 0000 1010 0000 1010 0000 1010
         *    左移 4 位得到 : 1010 0000 1010 0000 1010 0000 1010 0000 (把八位中的高 4 位放到低 4 位)
         * 2. 进行 (i >>> 4)得到 : 0000 1010 1010 1010 1010 1010 1010 1010
         *    & 0x0f0f0f0f得到 : 0000 1010 0000 1010 0000 1010 0000 1010 (把八位中的低 4 位放到高 4 位)
         *    | 合并  1010 1010 1010 1010 1010 1010 1010 1010 等价于把 8 位中的高 4 位和低 4 位交换。
         *
         * 此时交换过之后，需要交换 16 位(2字节)中的高 8 位(高字节) 和低 8 位(低字节)。即可以以字节为单位进行交换。
         */
        i = (i & 0x0f0f0f0f) << 4 | (i >>> 4) & 0x0f0f0f0f;

        /**
         * 以字节为单位进行交换。
         */
        return reverseBytes(i);
    }

    /**
     * 返回指定{@code int}值的 signum 函数。(如果指定的值为负，返回值为-1；如果指定的
     * 值为零，返回值为0；如果指定的值为正，返回值为1)。
     *
     * @param i 要计算正负号的值
     * @return 指定 {@code int}值的 signum 函数。
     * @since 1.5
     */
    public static int signum(int i) {
        // HD, Section 2-7
        /**
         * i >> 31 ，得到符号位，如果是负数即得到 -1，正数得到0，0得到0
         * -i >>> 31，得到符号位，如果是负数得到 0, 正数得到1, 0得到0
         *         进行 | 操作      负数 -> -1, 正数 -> 1, 0 -> 0
         */
        return (i >> 31) | (-i >>> 31);
    }

    /**
     * 返回通过反转指定的 {@code int} 值的二进制补码表示中的字节顺序获得的值。
     *
     * @param i 要反转其字节的值
     * @return 通过反转指定的 {@code int}值中的字节获得的值。
     * @since 1.5
     */
    @HotSpotIntrinsicCandidate
    public static int reverseBytes(int i) {
        /**
         * i << 24 : 左移 24 位，把最后一个字节放在第一个字节上
         * i & 0xff00 : 得到倒数第二个字节
         *       （此时保留了 i 第三个字节, 也就是倒数第二个字节）
         * ((i & 0xff00) << 8) : 把倒数第二个字节放在倒数第三个字节上
         * (i >>> 8) : 右移 8 位，把倒数第三个字节放在倒数第二个字节上
         *        （此时保留了 i 的第一个、第二个、第三个字节, 而我们只需要第二个字节，也就是倒数第三个字节）
         *  ((i >>> 8) & 0xff00) : 只保留移动后的第二个字节，也就是移动前的倒数第三个字节。
         *  (i >>> 24) : 把 第一个字节 放在最后一个字节的位置。
         */
        return (i << 24)            |
               ((i & 0xff00) << 8)  |
               ((i >>> 8) & 0xff00) |
               (i >>> 24);
    }

    /**
     * 像+运算符那样将两个整数相加。
     *
     * @param a 第一个操作数
     * @param b 第二个操作数
     * @return {@code a} 和 {@code b} 的和
     * @see java.util.function.BinaryOperator
     * @since 1.8
     */
    public static int sum(int a, int b) {
        return a + b;
    }

    /**
     * 返回两个{@code int}值中较大的那个，就像调用了 {@link Math#max(int, int) Math,max}。
     *
     * @param a 第一个操作数
     * @param b 第二个操作数
     * @return {@code a} 和 {@code b} 中较大的
     * @see java.util.function.BinaryOperator
     * @since 1.8
     */
    public static int max(int a, int b) {
        return Math.max(a, b);
    }

    /**
     * 返回两个{@code int}值中较小的那个，就像调用了{@link Math#min(int, int) Math.min}。
     *
     * @param a 第一个操作数
     * @param b 第二个操作数
     * @return {@code a} 和 {@code b} 中较小的
     * @see java.util.function.BinaryOperator
     * @since 1.8
     */
    public static int min(int a, int b) {
        return Math.min(a, b);
    }

    /** use serialVersionUID from JDK 1.0.2 for interoperability */
    @Native private static final long serialVersionUID = 1360826667806852920L;
}
