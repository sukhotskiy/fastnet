package org.fastnet.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;


import java.util.zip.CRC32;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class CryptUtilities {

	private static final String ALGORITHM = "PBEWithMD5AndDES";
	private static byte[] SALT = {
			(byte) 0xc7, (byte) 0x73, (byte) 0x21, (byte) 0x8c, (byte) 0x7e, (byte) 0xc8, (byte) 0xee, (byte) 0x99
	};

	public static String encodeObject(Object object) throws IOException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		GZIPOutputStream zout = new GZIPOutputStream(bout);
		ObjectOutputStream oout = new ObjectOutputStream(zout);
		try {
			oout.writeObject(object);
			zout.finish();
			return encode64(encrypt(bout.toByteArray(), computeArray()));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			zout.close();
			oout.close();
		}
		return null;
	}

	public static void encodeObjectToFile(Object object, String fileName) throws IOException {
		FileOutputStream bout = new FileOutputStream(fileName);
		GZIPOutputStream zout = new GZIPOutputStream(bout);
		ObjectOutputStream oout = new ObjectOutputStream(zout);
		try {
			oout.writeObject(object);
			zout.finish();
			bout.flush();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			zout.close();
			oout.close();
			bout.close();
		}
	}

	public static Object decodeObjectFromFile(String fileName) throws IOException, ClassNotFoundException {
		ObjectInputStream oin = null;
		try {
			oin = new ObjectInputStream(new GZIPInputStream(new FileInputStream(fileName)));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		try {
			return oin.readObject();
		} finally {
			if (oin != null)
				oin.close();
		}
	}

	public static String encodeBytes(byte[] array) throws IOException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		GZIPOutputStream zout = new GZIPOutputStream(bout);
		try {
			zout.write(array);
			zout.finish();
			return encode64(bout.toByteArray());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			zout.close();
		}
		return null;
	}

	public static byte[] decodeBytes(String line, Long length) throws IOException {
		GZIPInputStream stream = null;
		try {
			stream = new GZIPInputStream(new ByteArrayInputStream(CryptUtilities.decode64(line)));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		try {
			ByteBuffer result = ByteBuffer.allocate(length.intValue());
			byte[] buff = new byte[1024];
			int len = stream.read(buff);
			while (len > 0) {
				result.put(buff, 0, len);
				len = stream.read(buff);
			}
			return result.array();
		} finally {
			if (stream != null)
				stream.close();
		}
	}

	private static char[] computeArray() {
		char[] array = new char[9];
		array[0] = (char) DHCommon.skip1024ModulusBytes[11];
		array[1] = (char) DHCommon.skip1024ModulusBytes[1];
		array[2] = (char) DHCommon.skip1024ModulusBytes[5];
		array[3] = (char) DHCommon.skip1024ModulusBytes[16];
		array[4] = (char) DHCommon.skip1024ModulusBytes[23];
		array[5] = (char) DHCommon.skip1024ModulusBytes[2];
		array[6] = (char) DHCommon.skip1024ModulusBytes[46];
		array[7] = (char) DHCommon.skip1024ModulusBytes[28];
		array[8] = (char) DHCommon.skip1024ModulusBytes[3];
		return array;
	}

	public static Object decodeObject(String line) throws IOException, ClassNotFoundException {
		ObjectInputStream oin = null;
		try {
			oin = new ObjectInputStream(new GZIPInputStream(new ByteArrayInputStream(decrypt(CryptUtilities.decode64(line), computeArray()))));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		try {
			return oin.readObject();
		} finally {
			if (oin != null)
				oin.close();
		}
	}

	public static char[] readStrt(String line) throws IOException, ClassNotFoundException {
		ObjectInputStream oin = null;
		try {
			oin = new ObjectInputStream(new GZIPInputStream(new ByteArrayInputStream(decrypt(CryptUtilities.decode64(line), computeArray()))));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		try {
			return (char[]) oin.readObject();
		} finally {
			if (oin != null)
				oin.close();
		}
	}

	public static byte[] digest(byte[] bytes) {
		try {
			return (MessageDigest.getInstance("MD5")).digest(bytes);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	public static String digest(String str) {
		return SBase64.byteArrayToBase64(digest(str.getBytes()));
	}

	public static String encode64(byte[] bytes) {
		return SBase64.byteArrayToBase64(bytes);
	}

	public static byte[] decode64(String str) {
		return SBase64.base64ToByteArray(str);
	}

	private static byte[] crypt(byte[] srcArray, int mode, char[] passArray) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException, InvalidAlgorithmParameterException {
		int count = 20;
		PBEKeySpec pbeKeySpec = new PBEKeySpec(passArray);
		PBEParameterSpec pbeParamSpec = new PBEParameterSpec(SALT, count);
		SecretKeyFactory keyFac = SecretKeyFactory.getInstance(ALGORITHM);
		SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);
		Cipher pbeCipher = Cipher.getInstance(ALGORITHM);
		pbeCipher.init(mode, pbeKey, pbeParamSpec);
		return pbeCipher.doFinal(srcArray);
	}

	public static byte[] encrypt(byte[] src, char[] passArray) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException, InvalidAlgorithmParameterException {
		return crypt(src, Cipher.ENCRYPT_MODE, passArray);
	}

	public static byte[] decrypt(byte[] src, char[] passArray) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException, InvalidAlgorithmParameterException {
		return crypt(src, Cipher.DECRYPT_MODE, passArray);
	}

	static public long crc32(String srcFolder) throws IOException {
		CRC32 crc32 = new CRC32();
		digestFolder("", srcFolder, crc32);
		return crc32.getValue();
	}

	static private void digestFile(String path, String srcFile, CRC32 crc32) throws IOException {
		File folder = new File(srcFile);
		if (folder.isDirectory()) {
			digestFolder(path, srcFile, crc32);
		} else {
			byte[] buf = new byte[1024];
			int len;
			FileInputStream in = new FileInputStream(srcFile);
			try {
				while ((len = in.read(buf)) > 0) {
					crc32.update(buf, 0, len);
				}
			} catch (IOException e) {
				System.out.println(e.getMessage() + "------Err----" + srcFile);
			}
		}
	}

	static private void digestFolder(String path, String srcFolder, CRC32 crc32) throws IOException {
		if (path.contains("configuration") || path.contains("metadata")) {
			return;
		}
		if (srcFolder.contains("configuration") || srcFolder.contains("metadata")) {
			return;
		}
		File folder = new File(srcFolder);
		String[] list = folder.list();
		if (list != null) {
			for (String fileName : list) {
				if (path.equals("")) {
					digestFile(folder.getName(), srcFolder + "/" + fileName, crc32);
				} else {
					digestFile(path + "/" + folder.getName(), srcFolder + "/" + fileName, crc32);
				}
			}
		}
	}
}
