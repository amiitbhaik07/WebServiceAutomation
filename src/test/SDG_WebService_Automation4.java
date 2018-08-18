package test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.swing.JOptionPane;
import org.apache.http.client.methods.HttpPost;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

public class SDG_WebService_Automation4
{
	public static void main(String args[]) throws Exception
	{
		
		sendGetRequest("http://restapi.demoqa.com/utilities/weather/city/Nagpur");
		System.exit(0);
		
		try
		{
			String current = System.getProperty("user.dir");
			long start = System.currentTimeMillis();
			final String excelFileName = "Normal_WebServices";
			final String payloadResponses = current + "\\"+excelFileName+".xlsx";	
			String uname="",pwd="", requestUrl="";
			XSSFWorkbook workbook;
			XSSFSheet sheet, credsSheet;
			int counterPass=0, counterFail=0;
			int excelMethodColumn=3, excelRequestCol=4, excelExpResponseCol=5, excelActualResponseCol=6, excelResultCol=7, excelReasonCol=8;
			String str="";
			
			try
			{
				FileInputStream fis = new FileInputStream(payloadResponses);
				workbook = new XSSFWorkbook(fis);
				fis.close();
			}
			catch(Exception e)
			{
				throw new Exception("Unable to Read file " + payloadResponses);
			}
			
			try
			{
				sheet = workbook.getSheet("WebRequests");
				sheet.getRow(0);
			}
			catch(Exception e)
			{
				workbook.close();
				throw new Exception("Unable to get 'WebRequests' in " + excelFileName);
			}
			
			try
			{
				credsSheet = workbook.getSheet("Credentials");
			}
			catch(Exception e)
			{
				workbook.close();
				throw new Exception("Unable to get 'Credentials' in " + excelFileName);
			}
			
			
			for(int i=0; i<sheet.getRow(0).getPhysicalNumberOfCells(); i++)
			{
				try
				{
					str = sheet.getRow(0).getCell(i).getStringCellValue();
				}
				catch(Exception e)
				{
					str = sheet.getRow(0).getCell(i).getRawValue();
				}
				
				if(str.contains("Method"))
				{
					excelMethodColumn = i;
				}
				else if(str.contains("Request"))
				{
					excelRequestCol = i;
				}
				else if(str.contains("Expected Response"))
				{
					excelExpResponseCol = i;
				}
				else if(str.contains("Actual Response"))
				{
					excelActualResponseCol = i;
				}
				else if(str.contains("Result"))
				{
					excelResultCol = i;
				}
				else if(str.contains("Reason"))
				{
					excelReasonCol = i;
				}
			}			
			
			try
			{
				requestUrl = credsSheet.getRow(0).getCell(1).getStringCellValue();
			}
			catch(Exception e)
			{
				requestUrl = credsSheet.getRow(0).getCell(1).getRawValue();
			}
			
			try
			{
				uname = credsSheet.getRow(1).getCell(1).getStringCellValue();
			}
			catch(Exception e)
			{
				uname = credsSheet.getRow(1).getCell(1).getRawValue();
			}
			
			try
			{
				pwd = credsSheet.getRow(2).getCell(1).getStringCellValue();
			}
			catch(Exception e)
			{
				pwd = credsSheet.getRow(2).getCell(1).getRawValue();
			}
			
			if(requestUrl.trim().equals("") || uname.trim().equals("") || pwd.trim().equals(""))
			{
				workbook.close();
				throw new Exception("Please define URL, username and password in 'Credentials' sheet in " + excelFileName);
			}
			
			
			final String username = uname;
			final String password = pwd;	
			
			String strEncoded = null;
			try
			{

				//Authentication for getting response
				Authenticator.setDefault(new Authenticator() 
				{
				    @Override
				    protected PasswordAuthentication getPasswordAuthentication() 
				    {          
				        return new PasswordAuthentication(username, password.toCharArray());
				    }
				});		
				final String s = username+":"+password;
		        final byte[] authBytes = s.getBytes(StandardCharsets.UTF_8);
		        strEncoded = Base64.getEncoder().encodeToString(authBytes);
			}
			catch(Exception e)
			{}
			
			final String encoded = strEncoded;
			
			XSSFCellStyle passStyle = workbook.createCellStyle();
			Font headerFont = workbook.createFont();
			headerFont.setColor(IndexedColors.WHITE.getIndex());
			passStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
			passStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			passStyle.setFont(headerFont);
			
			XSSFCellStyle failStyle = workbook.createCellStyle();
			failStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
			failStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			failStyle.setFont(headerFont);

			XSSFCellStyle normalStyle = workbook.createCellStyle();
			Font headerFont1 = workbook.createFont();
			headerFont1.setColor(IndexedColors.BLACK.getIndex());
			normalStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
			normalStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			normalStyle.setFont(headerFont1);
			
			String jsonString="", method="", request="", expectedResponse="";
			XSSFCell cellActualResponse,cellReason,cellResult;
						
			System.out.println(sheet.getPhysicalNumberOfRows());
						
			for(int i=1; i<sheet.getPhysicalNumberOfRows(); i++)
			{		
				System.out.println(i);
				method="";
				try
				{
					method = sheet.getRow(i).getCell(excelMethodColumn).getStringCellValue();
				}
				catch(Exception e)
				{
					try
					{
						method = sheet.getRow(i).getCell(excelMethodColumn).getRawValue();
						if(request.equals(""))
							continue;
					}
					catch(Exception e1){
						continue;
					}
				}
				
				request="";
				try
				{
					request = sheet.getRow(i).getCell(excelRequestCol).getStringCellValue().replace("\\", "");
				}
				catch(Exception e)
				{
					try
					{
						request = sheet.getRow(i).getCell(excelRequestCol).getRawValue();
						if(request.equals(""))
							continue;
					}
					catch(Exception e1){
						continue;
					}
				}
				
				expectedResponse="";
				try
				{
					expectedResponse = sheet.getRow(i).getCell(excelExpResponseCol).getStringCellValue();
				}
				catch(Exception e)
				{
					try
					{
						expectedResponse = sheet.getRow(i).getCell(excelExpResponseCol).getRawValue();
					}
					catch(Exception e4){
						expectedResponse = "";
					}
				}
				
				expectedResponse = expectedResponse.trim().replace("\\", "").replace("sGreen1", "Green").replace("sRed1", "Red").replace("sRed2", "Red").replace("sYellow1", "Yellow").replace("sAmber", "Amber").replace("sGreen2", "Green");
				
				try
				{
					cellActualResponse = sheet.getRow(i).getCell(excelActualResponseCol);
					cellActualResponse.setCellValue("");
				}
				catch(Exception e)
				{
					cellActualResponse = sheet.getRow(i).createCell(excelActualResponseCol);
				}
				
				try
				{
					cellResult = sheet.getRow(i).getCell(excelResultCol);
					cellResult.setCellValue("");
				}
				catch(Exception e)
				{
					cellResult = sheet.getRow(i).createCell(excelResultCol);
				}
				
				try
				{
					cellReason = sheet.getRow(i).getCell(excelReasonCol);
					cellReason.setCellValue("");
				}
				catch(Exception e)
				{
					cellReason = sheet.getRow(i).createCell(excelReasonCol);
				}
				
				try
				{
					request = request.toString().trim();
					if(method.trim().equalsIgnoreCase("POST"))
					{
						jsonString = sendPostRequest(requestUrl, request, encoded);
					}
					else if(method.trim().equalsIgnoreCase("GET"))
					{
						jsonString = sendGetRequest(requestUrl, encoded);
					}
					
					try
					{
						JSONAssert.assertEquals(expectedResponse, jsonString, JSONCompareMode.LENIENT);
					}
					catch(AssertionError e1)
					{
						throw new Exception(e1);
					}
					
					cellActualResponse.setCellStyle(normalStyle);
					cellActualResponse.setCellValue(jsonString);
					cellResult.setCellStyle(passStyle);
					cellResult.setCellValue("Passed");
					
					counterPass++;
				}
				catch(Exception e)
				{		
					cellActualResponse.setCellStyle(normalStyle);
					cellActualResponse.setCellValue(jsonString);
					
					cellResult.setCellStyle(failStyle);
					cellResult.setCellValue("Failed");
					
					cellReason.setCellStyle(normalStyle);
					cellReason.setCellValue(e.getMessage());
					
					counterFail++;
				}
			}
			
			
			
			//Write this new Excel File in hard disk
			try
			{
				FileOutputStream out = new FileOutputStream(new File(payloadResponses));
				workbook.write(out);
				out.close();
				workbook.close();
				System.out.println("Success");	
			}
			catch(Exception e)
			{
				System.err.println("File already present");
				BufferedWriter out = new BufferedWriter(new FileWriter("filename"));
				out.write("aString1\n");
				out.close();
				boolean success = (new File("filename")).delete();
				if (success) {
					System.out.println("The file has been successfully deleted"); 
				}
			}
			
			
			//Close Workbook and print success message
			workbook.close();
			long end = System.currentTimeMillis();
			float diff = ((float)(end-start)/(float)1000.0);
			System.out.println("Showing Message");
			JOptionPane.showMessageDialog(null, "Completed Successfully!\nTotal Web-services Tested : " + (sheet.getPhysicalNumberOfRows()-1) + "\nTotal Success = '"+counterPass+"'\nTotal Failures = '"+counterFail+"'\nTotal Time elapsed : " + String.format("%.1f", diff) + " seconds", "Web-service Test Application", JOptionPane.INFORMATION_MESSAGE);
			System.out.println("Shown Message");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Error!\n"+e.getMessage(), "Web-service Test Application", JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	
	
	public static String sendPostRequest(String requestUrl, String payload, String encoder) 
	{	
		StringBuffer jsonString = null;
		try 
		{
	        URL url = new URL(requestUrl);
	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	        connection.setDoInput(true);
	        connection.setDoOutput(true);
	        
	        connection.setRequestMethod("POST");
	        connection.setRequestProperty("Authorization", "Basic " + encoder);
	        connection.setRequestProperty("Accept", "application/json");
	        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
	        
	        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
	        writer.write(payload);
	        writer.close();
	        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	        jsonString = new StringBuffer();
	        String line;
	        while ((line = br.readLine()) != null) 
	        {
	                jsonString.append(line);
	        }
	        br.close();
	        connection.disconnect();
	    } 
		catch (Exception e) 
		{
	        throw new RuntimeException(e.getMessage());
	    }
	    return jsonString.toString();
	}
	
	public static String sendGetRequest(String requestUrl, String encoder) 
	{	
		StringBuffer jsonString = null;
		try 
		{
	        URL url = new URL(requestUrl);
	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	        connection.setDoInput(true);
	        connection.setDoOutput(true);
	        
	        connection.setRequestMethod("GET");
	        connection.setRequestProperty("Authorization", "Basic " + encoder);
	        connection.setRequestProperty("Accept", "application/json");
	        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
	        
	        int responseCode = connection.getResponseCode();
			System.out.println("GET Response Code :: " + responseCode);
	        
	        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	        jsonString = new StringBuffer();
	        String line;
	        while ((line = br.readLine()) != null) 
	        {
	                jsonString.append(line);
	        }
	        br.close();
	        connection.disconnect();
	    } 
		catch (Exception e) 
		{
	        throw new RuntimeException(e.getMessage());
	    }
	    return jsonString.toString();
	}
	
	public static String sendGetRequest(String requestUrl) 
	{	
		StringBuffer jsonString = null;
		try 
		{
	        URL url = new URL(requestUrl);
	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	        connection.setDoInput(true);
	        connection.setDoOutput(true);	        
	        connection.setRequestMethod("GET");
	        connection.setRequestProperty("Accept", "application/json");
	        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
	        
	        int responseCode = connection.getResponseCode();
			System.out.println("GET Response Code :: " + responseCode);
	        
	        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	        jsonString = new StringBuffer();
	        String line;
	        while ((line = br.readLine()) != null) 
	        {
	                jsonString.append(line);
	        }
	        br.close();
	        connection.disconnect();
	        
	        System.out.println("$$$$$$$$$$$$$$\n\n\n\n"+jsonString.toString());
	    } 
		catch (Exception e) 
		{
	        throw new RuntimeException(e.getMessage());
	    }
	    return jsonString.toString();
	}
	
	public static void getResponse(String request, String authEncoded)
	{
		HttpPost post = new HttpPost("http://example.com/auth");
		post.addHeader("Authorization", "Basic " + authEncoded);
	}
}
