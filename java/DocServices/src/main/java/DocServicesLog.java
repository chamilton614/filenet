package com.hp.docsolutions.filenet.p8.base;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;

public class DocServicesLog {
	
	private FileOutputStream file;
	private String logFileName;
	public final String ERROR = "ERROR";
	public final String INFO = "INFO";
	public final String WARN = "WARN";
	private String lineSeparator = System.getProperty("line.separator");
	private String logFileDirectory;
	public final String appBasePath = ResourceBundle.getBundle(ConstantsUtil.GLOBAL_CONFIG).getString(
			ConstantsUtil.GLOBAL_LOG_FILE_DIRECTORY);
	
	public void setLogFileDirectory(String logFileDirectory) {
		this.logFileDirectory = logFileDirectory;
	}
	
	public void setLogFileName(String logFileName) {
		this.logFileName = logFileName;
	}

	public void log(String code, String msg)
	{
		try
		{
			if (file == null)
			{
				Calendar cl = Calendar.getInstance();
				Date date = new Date();
				Format monthFormatter = new SimpleDateFormat("MMM");
				Format yearFormatter = new SimpleDateFormat("yyyy");
				Format dayFormatter = new SimpleDateFormat("dd");
				
				String yearDirectory = yearFormatter.format(date);
				String monthDirectory = monthFormatter.format(date);
				String dayDirectory = dayFormatter.format(date);
				
				String localLogDirectory = appBasePath + "/" + logFileDirectory + "/"
					+ yearDirectory + "/" + monthDirectory + "/"
					+ dayDirectory + "/";
				
				File logFileDir = new File(localLogDirectory);
				
				if (!logFileDir.exists())
				{
					boolean logFile = logFileDir.mkdirs();
					if (!logFile)
					{
						localLogDirectory = appBasePath + "/" + logFileDirectory + "/";
					}
				}
				String fileName = localLogDirectory + "/" + this.logFileName
					+ cl.getTimeInMillis() + ".txt";
				file = new FileOutputStream(new File(fileName), true);
			}
			
			msg = new Date() + " " + code + " : " + msg + lineSeparator;
			file.write(msg.getBytes());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void reset()
	{
		try
		{
			this.file.close();
			this.file = null;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public String getStackTrace(Throwable throwable)
	{
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter, true);
		throwable.printStackTrace(printWriter);
		printWriter.flush();
		stringWriter.flush();
		return stringWriter.toString();
	}
	
}
