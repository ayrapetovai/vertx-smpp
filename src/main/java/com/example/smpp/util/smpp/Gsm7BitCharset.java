package com.example.smpp.util.smpp;

import java.nio.CharBuffer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.util.HashMap;

import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * A Charset implementation for Gsm 7-bit default and extended character set
 * See GSM 03.38
 *
 * @author Sverker Abrahamsson
 * @version $Id: Gsm7BitCharset.java 90 2011-04-19 22:07:52Z sverkera $
 */
public class Gsm7BitCharset extends Charset {

  private boolean debug = false;

  // HashMap's used for encoding and decoding
  protected static HashMap<String, Byte> defaultEncodeMap = new HashMap<String, Byte>();
  protected static HashMap<Byte, String> defaultDecodeMap = new HashMap<Byte, String>();
  protected static HashMap<String, Byte> extEncodeMap = new HashMap<String, Byte>();
  protected static HashMap<Byte, String> extDecodeMap = new HashMap<Byte, String>();

  protected static int[] defaultEncodeArray;
  protected static int[] extEncodeArray;

  protected static int[] defaultDecodeArray;
  protected static int[] extDecodeArray;


  // Data to populate the hashmaps with
  private static final Object[][] gsmCharacters = {
      { "@",      0x00 },
      { "£",      0x01 },
      { "$",      0x02 },
      { "¥",      0x03 },
      { "è",      0x04 },
      { "é",      0x05 },
      { "ù",      0x06 },
      { "ì",      0x07 },
      { "ò",      0x08 },
      { "Ç",      0x09 },
      { "\n",     0x0a },
      { "Ø",      0x0b },
      { "ø",      0x0c },
      { "\r",     0x0d },
      { "Å",      0x0e },
      { "å",      0x0f },
      { "\u0394", 0x10 },
      { "_",      0x11 },
      { "\u03A6", 0x12 },
      { "\u0393", 0x13 },
      { "\u039B", 0x14 },
      { "\u03A9", 0x15 },
      { "\u03A0", 0x16 },
      { "\u03A8", 0x17 },
      { "\u03A3", 0x18 },
      { "\u0398", 0x19 },
      { "\u039E", 0x1a },
      { "\u001B", 0x1b }, // 27 is Escape character
      { "Æ",      0x1c },
      { "æ",      0x1d },
      { "ß",      0x1e },
      { "É",      0x1f },
      { "\u0020", 0x20 },
      { "!",      0x21 },
      { "\"",     0x22 },
      { "#",      0x23 },
      { "¤",      0x24 },
      { "%",      0x25 },
      { "&",      0x26 },
      { "'",      0x27 },
      { "(",      0x28 },
      { ")",      0x29 },
      { "*",      0x2a },
      { "+",      0x2b },
      { ",",      0x2c },
      { "-",      0x2d },
      { ".",      0x2e },
      { "/",      0x2f },
      { "0",      0x30 },
      { "1",      0x31 },
      { "2",      0x32 },
      { "3",      0x33 },
      { "4",      0x34 },
      { "5",      0x35 },
      { "6",      0x36 },
      { "7",      0x37 },
      { "8",      0x38 },
      { "9",      0x39 },
      { ":",      0x3a },
      { ";",      0x3b },
      { "<",      0x3c },
      { "=",      0x3d },
      { ">",      0x3e },
      { "?",      0x3f },
      { "¡",      0x40 },
      { "A",      0x41 },
      { "B",      0x42 },
      { "C",      0x43 },
      { "D",      0x44 },
      { "E",      0x45 },
      { "F",      0x46 },
      { "G",      0x47 },
      { "H",      0x48 },
      { "I",      0x49 },
      { "J",      0x4a },
      { "K",      0x4b },
      { "L",      0x4c },
      { "M",      0x4d },
      { "N",      0x4e },
      { "O",      0x4f },
      { "P",      0x50 },
      { "Q",      0x51 },
      { "R",      0x52 },
      { "S",      0x53 },
      { "T",      0x54 },
      { "U",      0x55 },
      { "V",      0x56 },
      { "W",      0x57 },
      { "X",      0x58 },
      { "Y",      0x59 },
      { "Z",      0x5a },
      { "Ä",      0x5b },
      { "Ö",      0x5c },
      { "Ñ",      0x5d },
      { "Ü",      0x5e },
      { "§",      0x5f },
      { "¿",      0x60 },
      { "a",      0x61 },
      { "b",      0x62 },
      { "c",      0x63 },
      { "d",      0x64 },
      { "e",      0x65 },
      { "f",      0x66 },
      { "g",      0x67 },
      { "h",      0x68 },
      { "i",      0x69 },
      { "j",      0x6a },
      { "k",      0x6b },
      { "l",      0x6c },
      { "m",      0x6d },
      { "n",      0x6e },
      { "o",      0x6f },
      { "p",      0x70 },
      { "q",      0x71 },
      { "r",      0x72 },
      { "s",      0x73 },
      { "t",      0x74 },
      { "u",      0x75 },
      { "v",      0x76 },
      { "w",      0x77 },
      { "x",      0x78 },
      { "y",      0x79 },
      { "z",      0x7a },
      { "ä",      0x7b },
      { "ö",      0x7c },
      { "ñ",      0x7d },
      { "ü",      0x7e },
      { "à",      0x7f }
  };

  static {
    int maxCode = Stream.of(gsmCharacters).mapToInt(a -> intValueOfBytes((String)a[0])).max().getAsInt() + 1;
    defaultEncodeArray = new int[maxCode];
    for (Object[] strAndCode: gsmCharacters) {
      defaultEncodeArray[intValueOfBytes((String)strAndCode[0])] = (Integer) strAndCode[1];
    }
  }
  static {
    int maxCode = Stream.of(gsmCharacters).mapToInt(a -> (Integer)a[1]).max().getAsInt() + 1;
    defaultDecodeArray = new int[maxCode];
    for (Object[] strAndCode: gsmCharacters) {
      defaultEncodeArray[(Integer) strAndCode[1]] = intValueOfBytes((String)strAndCode[0]);
    }
  }

  private static final Object[][] gsmExtensionCharacters = {
      { "\n", 0x0a },
      { "^",  0x14 },
      { " ",  0x1b }, // reserved for future extensions
      { "{",  0x28 },
      { "}",  0x29 },
      { "\\", 0x2f },
      { "[",  0x3c },
      { "~",  0x3d },
      { "]",  0x3e },
      { "|",  0x40 },
      { "€",  0x65 }
  };

  static {
    int maxCode = Stream.of(gsmExtensionCharacters).mapToInt(a -> intValueOfBytes((String)a[0])).max().getAsInt() + 1;
    extEncodeArray = new int[maxCode];
    for (Object[] strAndCode: gsmExtensionCharacters) {
      extEncodeArray[intValueOfBytes((String)strAndCode[0])] = (Integer) strAndCode[1];
    }
  }
  static {
    int maxCode = Stream.of(gsmExtensionCharacters).mapToInt(a -> (Integer)a[1]).max().getAsInt() + 1;
    extDecodeArray = new int[maxCode];
    for (Object[] strAndCode: gsmExtensionCharacters) {
      extEncodeArray[(Integer) strAndCode[1]] = intValueOfBytes((String)strAndCode[0]);
    }
  }
  private static Logger logger = Logger.getLogger(Gsm7BitCharset.class.getName());

  // static section that populates the encode and decode HashMap objects
//  static {
//    // default alphabet
//    int len = gsmCharacters.length;
//    for (int i = 0; i < len; i++) {
//      Object[] map = gsmCharacters[i];
//      defaultEncodeMap.put((String) map[0], (Byte) map[1]);
//      defaultDecodeMap.put((Integer) map[1], (String) map[0]);
//    }
//
//    // extended alphabet
//    len = gsmExtensionCharacters.length;
//    for (int i = 0; i < len; i++) {
//      Object[] map = gsmExtensionCharacters[i];
//      extEncodeMap.put((String) map[0], (Byte) map[1]);
//      extDecodeMap.put((Integer) map[1], (String) map[0]);
//    }
//  }

  /**
   * Constructor for the Gsm7Bit charset.  Call the superclass
   * constructor to pass along the name(s) we'll be known by.
   * Then save a reference to the delegate Charset.
   */
  public Gsm7BitCharset(String canonical, String[] aliases) {
    super(canonical, aliases);
  }

  // ----------------------------------------------------------

  /**
   * Called by users of this Charset to obtain an encoder.
   * This implementation instantiates an instance of a private class
   * (defined below) and passes it an encoder from the base Charset.
   */
  public CharsetEncoder newEncoder() {
    return new Gsm7BitEncoder(this);
  }

  /**
   * Called by users of this Charset to obtain a decoder.
   * This implementation instantiates an instance of a private class
   * (defined below) and passes it a decoder from the base Charset.
   */
  public CharsetDecoder newDecoder() {
    return new Gsm7BitDecoder(this);
  }

  /**
   * This method must be implemented by concrete Charsets.  We always
   * say no, which is safe.
   */
  public boolean contains(Charset cs) {
    return (false);
  }

  /**
   * The encoder implementation for the Gsm7Bit Charset.
   * This class, and the matching decoder class below, should also
   * override the "impl" methods, such as implOnMalformedInput() and
   * make passthrough calls to the baseEncoder object.  That is left
   * as an exercise for the hacker.
   */
  private class Gsm7BitEncoder extends CharsetEncoder {

    /**
     * Constructor, call the superclass constructor with the
     * Charset object and the encodings sizes from the
     * delegate encoder.
     */
    Gsm7BitEncoder(Charset cs) {
      super(cs, 1, 2);
    }

    /**
     * Implementation of the encoding loop.
     */
    @Override
    protected CoderResult encodeLoop(CharBuffer cb, ByteBuffer bb) {
      CoderResult cr = CoderResult.UNDERFLOW;

      while (cb.hasRemaining()) {
        if (!bb.hasRemaining()) {
          cr = CoderResult.OVERFLOW;
          break;
        }
        char ch = cb.get();

        // first check the default alphabet
        int b = 0;
        if (ch < defaultEncodeArray.length) {
          b = defaultEncodeArray[ch];
        }
        if(debug)
          logger.finest("Encoding ch " + ch + " to byte " + b);
        if (b != 0) {
          bb.put((byte) b);
        } else {
          // check extended alphabet
          if (ch < extEncodeArray.length) {
            b = extEncodeArray[ch];
          }
          if(debug)
            logger.finest("Trying extended map to encode ch " + ch + " to byte " + b);
          if (b != 0) {
            // since the extended character set takes two bytes
            // we have to check that there is enough space left
            if (bb.remaining() < 2) {
              // go back one step
              cb.position(cb.position() - 1);
              cr = CoderResult.OVERFLOW;
              break;
            }
            // all ok, add it to the buffer
            bb.put((byte) 0x1b);
            bb.put((byte) b);
          } else {
            // no match found, send a ?
            b = 0x3F;
            bb.put((byte) b);
          }
        }
      }
      return cr;
    }
  }

  // --------------------------------------------------------

  /**
   * The decoder implementation for the Gsm 7Bit Charset.
   */
  private class Gsm7BitDecoder extends CharsetDecoder {

    /**
     * Constructor, call the superclass constructor with the
     * Charset object and pass alon the chars/byte values
     * from the delegate decoder.
     */
    Gsm7BitDecoder(Charset cs) {
      super(cs, 1, 1);
    }

    /**
     * Implementation of the decoding loop.
     */
    protected CoderResult decodeLoop(ByteBuffer bb, CharBuffer cb) {
      CoderResult cr = CoderResult.UNDERFLOW;

      while (bb.hasRemaining()) {
        if (!cb.hasRemaining()) {
          cr = CoderResult.OVERFLOW;
          break;
        }
        byte b = bb.get();

        // first check the default alphabet
        if(debug)
          logger.finest("Looking up byte " + b);
        int s = 0;
        if (b < defaultDecodeArray.length) {
          s = defaultDecodeArray[b];
        }
        if (s != 0) {
          char ch = (char) s;
          if (ch != '\u001B') {
            if(debug)
              logger.finest("Found string " + s);
            cb.put(ch);
          } else {
            if(debug)
              logger.finest("Found escape character");
            // check the extended alphabet
            if (bb.hasRemaining()) {
              b = bb.get();
              s = 0;
              if (b < extDecodeArray.length) {
                s = extDecodeArray[b];
              }
              if (s != 0) {
                if(debug)
                  logger.finest("Found extended string " + s);
                ch = (char) s;
                cb.put(ch);
              } else {
                cb.put('?');
              }
            }
          }
        } else {
          cb.put('?');
        }
      }
      return cr;
    }
  }

  private static int intValueOfBytes(String s) {
    var data = 0;
    var strBytes = s.getBytes();
    data |= ((int) strBytes[0]) & 0xF;
    if (strBytes.length > 1) {
      data |= (((int) strBytes[1]) & 0xF) << 8;
    }
    if (strBytes.length > 2) {
      data |= (((int) strBytes[2]) & 0xF) << 16;
    }
    if (strBytes.length > 3) {
      data |= (((int) strBytes[3]) & 0xF) << 24;
    }
    if (data < 0) {
      // never be here
      throw  new IllegalStateException(
          strBytes.length + " bytes are bad '" + s + "' [" +
              Integer.toHexString(strBytes[0]) + "," +
              (strBytes.length > 1? Integer.toHexString(strBytes[1]): "") + "," +
              (strBytes.length > 2? Integer.toHexString(strBytes[2]): "") + "," +
              (strBytes.length > 3? Integer.toHexString(strBytes[3]): "") + "," +
              "] -> " + Integer.toHexString(data)
      );
    }
    return data;
  }
}

/*
 * $Log: not supported by cvs2svn $
 * Revision 1.2  2006/03/09 16:24:14  sverkera
 * Removed compiler and javadoc warnings
 *
 * Revision 1.1  2003/09/30 09:02:09  sverkera
 * Added implementation for GSM 7Bit charset
 *
 */