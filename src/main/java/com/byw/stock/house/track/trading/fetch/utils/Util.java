package com.byw.stock.house.track.trading.fetch.utils;

import io.netty.util.CharsetUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


/**
 * 
 * This is the platform general utility class.
 * 
 * This is the platform general utility class.
 * 
 * @title Utils
 * @package com.topsec.tss.platform.core.utils
 * @author baiyanwei
 * @version
 * @date 2014-8-8
 * 
 */
public class Util {

    //
    // Logging Object
    //
    final private static int BUFFER_SIZE = 1024;
    final private static int EOF = -1;

    /**
     * This will turn an input stream into a String Buffer. The InputStream will always be closed before return.
     * 
     * @param inputStream
     * @return
     */
    public static StringBuffer getInputStream2StringBuffer(InputStream inputStream) {

        if (inputStream == null) {
            return null;
        }

        InputStreamReader utf8 = null;
        //
        try {
            utf8 = new InputStreamReader(inputStream, CharsetUtil.UTF_8);
            //
            StringBuffer stringBuffer = new StringBuffer();
            //
            char[] buffer = new char[BUFFER_SIZE];
            int n = 0;
            //
            while ((n = utf8.read(buffer)) != EOF) {
                stringBuffer.append(buffer, 0, n);
            }
            //
            return stringBuffer;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (utf8 != null) {
                try {
                    utf8.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }

    /**
     * 
     * 将字节写入到File中.
     * 
     * @param bytes
     * @param file
     */
    public static void writeByteContentToFile(byte[] bytes, File file) {

        if (bytes == null || file.exists() == false) {
            return;
        }
        if (bytes == null || bytes.length <= 0) {
            return;
        }
        ByteArrayInputStream byteInputStream = null;

        OutputStream sOutput = null;
        try {
            byteInputStream = new ByteArrayInputStream(bytes);
            sOutput = new FileOutputStream(file);

            byte[] buffer = new byte[BUFFER_SIZE];
            int size;
            while ((size = byteInputStream.read(buffer, 0, buffer.length)) != -1) {
                sOutput.write(buffer, 0, size);
            }
            sOutput.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (byteInputStream != null) {
                try {
                    byteInputStream.close();
                } catch (IOException e) {
                }
            }
            if (sOutput != null) {
                try {
                    sOutput.close();
                } catch (IOException e) {
                }
            }
        }

    }

    public static byte[] readFileContent2ByteArray(File dataFile) throws FileNotFoundException {

        if (dataFile == null || dataFile.exists() == false) {
            return null;
        }
        return readInputStream2ByteArray(new FileInputStream(dataFile));
    }

    /**
     * This will turn an input stream into a byte[]. The InputStream will always be closed before return.
     * 
     * @param inputStream
     * @return
     */
    public static byte[] readInputStream2ByteArray(InputStream inputStream) {

        if (inputStream == null) {
            return null;
        }
        //
        ByteArrayOutputStream byteArrayOutput = null;

        try {
            byteArrayOutput = new ByteArrayOutputStream();
            //
            byte[] buffer = new byte[BUFFER_SIZE];
            int n = 0;
            //
            while ((n = inputStream.read(buffer)) != EOF) {
                byteArrayOutput.write(buffer, 0, n);
            }
            //
            byte[] dataArray = byteArrayOutput.toByteArray();
            //
            return dataArray;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
            if (byteArrayOutput != null) {
                try {
                    byteArrayOutput.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }

    /**
     * Converts the input bytes to hex.
     */
    public static String convertToHex(byte[] data) {

        if (data == null) {
            return "";
        }

        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9))
                    buf.append((char) ('0' + halfbyte));
                else
                    buf.append((char) ('a' + (halfbyte - 10)));
                halfbyte = data[i] & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    /**
     * Use the standard java GZip functionality.
     * 
     * returns a byte array of the compress data.
     * 
     * @param inputBytes
     * @return
     */
    public static byte[] compress(byte[] inputBytes) {

        if (inputBytes == null || inputBytes.length == 0) {
            return null;
        }

        ByteArrayOutputStream bytesOutputStream = null;
        GZIPOutputStream gZIPOutputStream = null;
        //
        try {
            bytesOutputStream = new ByteArrayOutputStream();
            gZIPOutputStream = new GZIPOutputStream(bytesOutputStream);
            //compress the data
            gZIPOutputStream.write(inputBytes, 0, inputBytes.length);
            //
            gZIPOutputStream.finish();
            //
            byte[] dataArray = bytesOutputStream.toByteArray();
            //
            return dataArray;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bytesOutputStream != null) {
                try {
                    bytesOutputStream.close();
                } catch (IOException e) {
                }
            }
            if (gZIPOutputStream != null) {
                try {
                    gZIPOutputStream.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }

    /**
     * Uncompress a byte array of data
     * 
     * @param inputBytes
     * @return an uncompressed byte array.
     */
    public static byte[] decompress(byte[] inputBytes) {

        if (inputBytes == null || inputBytes.length == 0) {
            return null;
        }
        GZIPInputStream gzipInputStream = null;
        ByteArrayOutputStream bytesOutputStream = null;
        try {
            gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(inputBytes));
            bytesOutputStream = new ByteArrayOutputStream();

            byte[] buffer = new byte[BUFFER_SIZE];
            int n = 0;

            while ((n = gzipInputStream.read(buffer)) != EOF) {
                bytesOutputStream.write(buffer, 0, n);
            }
            //
            byte[] dataArray = bytesOutputStream.toByteArray();
            //
            return dataArray;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (gzipInputStream != null) {
                try {
                    gzipInputStream.close();
                } catch (IOException e) {
                }
            }
            if (bytesOutputStream != null) {
                try {
                    bytesOutputStream.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }

    /**
     * get MD5 for content.
     * 
     * @param content
     * @return
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    public static String md5String(String content) throws NoSuchAlgorithmException, UnsupportedEncodingException {

        if (content == null || content.length() == 0) {
            return null;
        }
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] md5hash = new byte[32];
        md.update(content.getBytes("iso-8859-1"), 0, content.length());
        md5hash = md.digest();
        return convertToHex(md5hash);
    }

    /**
     * traverse each extension and handle it.
     * 
     * @param extPnt
     *            extension point identifier
     * @param handler
     * @throws PlatformException
     * 
     *             CR - I like this lets move it to core
     */

    /**
     * Tests whether or not the url has a http or https prefix, if it does returns true else returns false.
     * 
     * @param url
     * @return
     */
    public static boolean testHttpPrefix(String url) {

        if (url == null || url.length() == 0) {
            return false;
        }

        return url.toLowerCase().startsWith("http://") == true || url.toLowerCase().startsWith("https://") == true;
    }

    /**
     * subtract the pat from the source
     * 
     * @param source
     * @param pat
     * @return
     */
    public static String subtract(String source, String pat) {

        if (source == null || source.length() == 0) {
            return null;
        }

        if (pat == null || pat.length() == 0) {
            return null;
        }

        int index = source.indexOf(pat);
        if (index == -1) {
            // not found
            return source;
        }

        StringBuffer sb = new StringBuffer();
        if (index != 0) {
            sb.append(source.substring(0, index));
        }

        if (index + pat.length() != source.length()) {
            sb.append(source.substring(index + pat.length()));
        }

        return sb.toString();
    }

    /**
     * 
     * @param clazz
     * @return
     */
    public static String getShortClassName(Class<?> clazz) {

        if (clazz == null) {
            return null;
        }

        return getShortClassName(clazz.getName());
    }

    /**
     * Get the Short class name.
     * 
     * @param className
     * @return
     */
    public static String getShortClassName(String className) {

        if (className == null || className.length() == 0) {
            return null;
        }
        int lastDotIndex = className.lastIndexOf(".");
        int nameEndIndex = className.length();
        String shortName = className.substring(lastDotIndex + 1, nameEndIndex);
        //
        return shortName;
    }

    /**
     * get 32 bit UUID String
     * 
     * @return
     */
    public static String getUUID32() {

        String sn = UUID.randomUUID().toString();
        // UUID(32);
        return sn.substring(0, 8) + sn.substring(9, 13) + sn.substring(14, 18) + sn.substring(19, 23) + sn.substring(24);
    }

    /**
     * load the properties collection by path.
     * 
     * @param path
     * @return
     * @throws IOException
     */
    public static Properties loadProperties(String path) throws IOException {

        if (path == null || path.length() == 0) {
            return null;
        }

        InputStream fileInput = null;
        try {
            // parser the class name to path.
            fileInput = Util.class.getClassLoader().getResourceAsStream(path);
            if (fileInput == null) {
                // Doesn't define the RES.properties file
                fileInput = new FileInputStream(path);
            }
            Properties properties = new Properties();
            // loading into cache.
            properties.load(fileInput);
            return properties;
        } catch (IOException e) {
            throw e;
        } finally {
            if (fileInput != null) {
                try {
                    fileInput.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
