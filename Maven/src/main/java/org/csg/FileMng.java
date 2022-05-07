package org.csg;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;


public class FileMng {
	  public static void copyDir(File source, File target)
	  {
	    if (source.isDirectory()){
	      if (!target.exists()) {
	        target.mkdirs();
	      }
	      if(!source.getName().equals("playerdata") && !source.getName().equals("stats")){
	    	  for (String el : source.list()){
	  	        if (!el.equals("uid.dat") && !el.equals("session.lock")) {
	  	          copyDir(new File(source, el), new File(target, el));
	  	        }
	  	      }
	      }
	      
	    }else{
	      try{
	        if (!target.getParentFile().exists())
	        {
	          new File(target.getParentFile().getAbsolutePath()).mkdirs();
	          target.createNewFile();
	        }
	        else if (!target.exists())
	        {
	          target.createNewFile();
	        }
	        InputStream in = new FileInputStream(source);
	        Object out = new FileOutputStream(target);
	        
	        byte[] buf = new byte[1024];
	        int len;
	        while ((len = in.read(buf)) > 0) {
	          ((OutputStream)out).write(buf, 0, len);
	        }
	        in.close();
	        ((OutputStream)out).close();
	      }
	      catch (Exception exception)
	      {
	      }
	    }
	  }
	  
	  public static boolean deleteDir(File dir)
	  {
	    if (dir.isDirectory()) {
	      for (File f : dir.listFiles()) {
	        if (!deleteDir(f)) {
	          return false;
	        }
	      }
	    }
	    return dir.delete();
	  }
	  

	/* 替换文件中的字符串，并覆盖原文件
	 * @param filePath
	 * @param oldstr
	 * @param newStr
	 * @throws IOException
	 */
	public static void autoReplaceStr(String filePath, String oldstr, String newStr) throws IOException {
		File file = new File(filePath);
		Long fileLength = file.length();
		byte[] fileContext = new byte[fileLength.intValue()];
		FileInputStream in = null;
		PrintWriter out = null;
		in = new FileInputStream(filePath);
		in.read(fileContext);
// 避免出现中文乱码
		String str = new String(fileContext, "utf-8");//字节转换成字符
		str = str.replace(oldstr, newStr);
		out = new PrintWriter(filePath, "utf-8");//写入文件时的charset
		out.write(str);
		out.flush();
		out.close();
		in.close();
	}
}
