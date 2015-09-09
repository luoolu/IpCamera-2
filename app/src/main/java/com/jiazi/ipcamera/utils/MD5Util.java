package com.jiazi.ipcamera.utils;

import java.security.MessageDigest;

/**
 * MD5加密
 */
public class MD5Util {
	public static String getMD5(String content, int bit_num) {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("MD5");
			digest.reset();
			digest.update(content.getBytes("utf-8"));
			byte[] a = digest.digest();

			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < a.length; i++) {
				sb.append(Character.forDigit((a[i] & 0xf0) >> 4, 16));
				sb.append(Character.forDigit(a[i] & 0x0f, 16));
			}

			String temp = sb.toString();
			if (bit_num == 16) {
				// 16位加密，从第9位到25位
				return temp.substring(8, 24);
			} else if (bit_num == 32) {
				return temp;
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}