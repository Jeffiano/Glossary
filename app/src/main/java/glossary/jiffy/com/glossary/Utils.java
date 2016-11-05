package glossary.jiffy.com.glossary;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
	public static void log(Object o) {
		System.out.println(o);
	}

	public static void saveStringToFile(String infos, String filePath) {
		try {
			OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(filePath), "GBK");
			BufferedWriter writer = new BufferedWriter(write);
			writer.append(infos);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String getStringCacheFromSD(String filePath) {
		String result = "";
		String line = "";
		if (!new File(filePath).exists())
			return result;
		try {
			BufferedReader bufferReader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath),
					"utf-8"));
			while ((line = bufferReader.readLine()) != null) {
				result += line;
			}
			bufferReader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;

	}

	public static List<String> getStringArrayFromSD(String filePath) {
		List<String> list = new ArrayList<String>();
		String result = "";
		String line = "";
		if (!new File(filePath).exists())
			return list;
		try {
			BufferedReader bufferReader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath),
					"utf-8"));
			while ((line = bufferReader.readLine()) != null) {
				result += line;
				if (line != null && !line.equals(""))
					list.add(line);
			}
			bufferReader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;

	}

	public static boolean copy(String fileFrom, String fileTo) {
		try {
			FileInputStream in = new FileInputStream(fileFrom);
			FileOutputStream out = new FileOutputStream(fileTo);
			byte[] bt = new byte[1024];
			int count;
			while ((count = in.read(bt)) > 0) {
				out.write(bt, 0, count);
			}
			in.close();
			out.close();
			return true;
		} catch (IOException ex) {
			return false;
		}
	}

	public static boolean isChineseChar(String str) {
		boolean temp = false;
		Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
		Matcher m = p.matcher(str);
		if (m.find()) {
			temp = true;
		}
		return temp;
	}

}
