package glossary.jiffy.com.glossary;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
	public static String getFirstLineStrinFromSD(String filePath) {
		String result = "";
		if (!new File(filePath).exists())
			return result;
		try {
			BufferedReader bufferReader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath),
					"utf-8"));
			result = bufferReader.readLine();
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
	public static void writeToSD(String path, InputStream input) {

		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			try {
				File file = new File(path);
				File parent = file.getParentFile();
				if (!parent.exists()) {
					parent.mkdirs();
				}
				FileOutputStream fos = new FileOutputStream(file);
				byte[] b = new byte[2048];
				int j = 0;
				while ((j = input.read(b)) != -1) {
					fos.write(b, 0, j);
				}
				fos.flush();
				fos.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			Log.i("Utils", "NO SDCard available.");
		}
	}
}
