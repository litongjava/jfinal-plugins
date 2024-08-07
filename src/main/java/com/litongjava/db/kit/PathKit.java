package com.litongjava.db.kit;

import java.io.File;
import java.io.UnsupportedEncodingException;

/**
 * new File("..\path\abc.txt") 中的三个方法获取路径的方法
 * 1： getPath() 获取相对路径，例如   ..\path\abc.txt
 * 2： getAbsolutePath() 获取绝对路径，但可能包含 ".." 或 "." 字符，例如  D:\otherPath\..\path\abc.txt
 * 3： getCanonicalPath() 获取绝对路径，但不包含 ".." 或 "." 字符，例如  D:\path\abc.txt
 * 
 * 2018-05-12 新测试：
 * 1：PathKit.class.getResource("/") 将获取 class path 根目录，例如：/Users/james/workspace/jfinal/webapp/WEB-INF/classes
 * 2：PathKit.class.getResource("") 将获取 PathKit 这个 class 所在的目录，即：rootClassPath + "/com/jfinal/kit"
 * 
 * 3：ClassLoader.getResource("/") 将获取到 null 值，该用法无意义
 * 4：ClassLoader.getResource("") 将获取 class path 根目录，与 PathKit.class.getResource("/") 一样
 */
public class PathKit {
	
	private static String webRootPath;
	private static String rootClassPath;
	
	@SuppressWarnings("rawtypes")
	public static String getPath(Class clazz) {
		String path = clazz.getResource("").getPath();
		return new File(path).getAbsolutePath();
	}
	
	public static String getPath(Object object) {
		String path = object.getClass().getResource("").getPath();
		return new File(path).getAbsolutePath();
	}
	
	// 注意：命令行返回的是命令行所在的当前路径
	public static String getRootClassPath() {
		if (rootClassPath == null) {
			try {
				// String path = PathKit.class.getClassLoader().getResource("").toURI().getPath();
				String path = getClassLoader().getResource("").toURI().getPath();
				rootClassPath = new File(path).getAbsolutePath();
			}
			catch (Exception e) {
				// String path = PathKit.class.getClassLoader().getResource("").getPath();
				// String path = getClassLoader().getResource("").getPath();
				// rootClassPath = new File(path).getAbsolutePath();
				
				try {
					String path = PathKit.class.getProtectionDomain().getCodeSource().getLocation().getPath();
					path = java.net.URLDecoder.decode(path, "UTF-8");
					if (path.endsWith(File.separator)) {
						path = path.substring(0, path.length() - 1);
					}
					rootClassPath = path;
				} catch (UnsupportedEncodingException e1) {
					throw new RuntimeException(e1);
				}
			}
		}
		return rootClassPath;
	}
	
	/**
	 * 优先使用 current thread 所使用的 ClassLoader 去获取路径
	 * 否则在某些情况下会获取到 tomcat 的 ClassLoader，那么路径值将是
	 * TOMCAT_HOME/lib
	 * 
	 * issue: https://gitee.com/jfinal/jfinal/issues/ID428#note_699360
	 */
	private static ClassLoader getClassLoader() {
		ClassLoader ret = Thread.currentThread().getContextClassLoader();
		return ret != null ? ret : PathKit.class.getClassLoader();
	}
	
	public static void setRootClassPath(String rootClassPath) {
		PathKit.rootClassPath = rootClassPath;
	}
	
	public static String getPackagePath(Object object) {
		Package p = object.getClass().getPackage();
		return p != null ? p.getName().replaceAll("\\.", "/") : "";
	}
	
	public static File getFileFromJar(String file) {
		throw new RuntimeException("Not finish. Do not use this method.");
	}
	
	public static String getWebRootPath() {
		if (webRootPath == null) {
			webRootPath = detectWebRootPath();
		}
		return webRootPath;
	}
	
	public static void setWebRootPath(String webRootPath) {
		if (webRootPath == null) {
			return ;
		}
		
		if (webRootPath.endsWith(File.separator)) {
			webRootPath = webRootPath.substring(0, webRootPath.length() - 1);
		}
		PathKit.webRootPath = webRootPath;
	}
	
	// 注意：命令行返回的是命令行所在路径的上层的上层路径
	private static String detectWebRootPath() {
		try {
			String path = PathKit.class.getResource("/").toURI().getPath();
			String ret = new File(path).getParentFile().getParentFile().getCanonicalPath();
			// 支持 maven 项目在开发环境下探测 webRootPath
			if (path.endsWith("/target/classes/")) {
				return ret + "/src/main/webapp";
			} else if (path.endsWith("\\target\\classes\\")) {
				return ret + "\\src\\main\\webapp";
			} else {
				return ret;
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static boolean isAbsolutePath(String path) {
		return path.startsWith("/") || path.indexOf(':') == 1;
	}
	
	/*
	private static String detectWebRootPath() {
		try {
			String path = PathKit.class.getResource("/").getFile();
			return new File(path).getParentFile().getParentFile().getCanonicalPath();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	*/
}


