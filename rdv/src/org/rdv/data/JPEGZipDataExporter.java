
package org.rdv.data;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

public class JPEGZipDataExporter implements DataExporter {

  private static DateFormat EXPORT_DATE_FORMAT = 
      new SimpleDateFormat("yyyy-MM-dd--HH-mm-ss-SSS");
  
  private JPEGConverter jpegConverter_;
  
  private ZipOutputStream zipOut_;
  
  /**
   * Time of last exported frame.
   */
  private Double exportTime;
  
  
  public JPEGZipDataExporter(JPEGConverter converter){
    jpegConverter_=converter;
  }
  
  @Override
  public void abort() {
    // probably need to do other cleanup things here...
    
    // Complete the ZIP file
    try {
      zipOut_.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  
  }

  @Override
  public void close() throws IOException {

    // Complete the ZIP file
    zipOut_.close();
  
  }

  @Override
  public String getStatus() {
    if(exportTime==null){
      return "Exporting JPEG frames...";
    }else{
      return "Exported "+
        EXPORT_DATE_FORMAT.format(new Date(((long)(exportTime*1000))))+
        ".jpg";
    }
    
  }

  @Override
  public void init(File file, List<Channel> channels, 
      double startTime, double endTime)
      throws IOException {
    //exportZip_=file;
    zipOut_=new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
    
    exportTime=null;
    //exportDir_ = System.getProperty("java.io.tmpdir");//FilenameUtils.getFullPath(file.getAbsolutePath());
    //FileUtils.forceMkdir(new File(exportDir_));
  }

  @Override
  public void writeSample(NumericDataSample sample) throws IOException {

    // Add ZIP entry to output stream.
    zipOut_.putNextEntry(new ZipEntry(EXPORT_DATE_FORMAT.format(
                new Date(((long)(sample.getTimestamp()*1000))))+".jpg"));
    
    jpegConverter_.writeJPEG(sample,ImageIO.createImageOutputStream(zipOut_));
    
    // Complete the entry
    zipOut_.closeEntry();
    
    exportTime=sample.getTimestamp();
    
// uses a temp file to write image to first
//    File file = new File(FilenameUtils.concat(exportDir_, 
//        EXPORT_DATE_FORMAT.format(new Date(((long)(sample.getTimestamp()*1000))))+".jpg"));
//
//    FileImageOutputStream output = new FileImageOutputStream(file);
//    jpegConverter_.writeJPEG(sample,output);
//    output.close();
//    
//    FileInputStream in = new FileInputStream(file);
//  
//    // Add ZIP entry to output stream.
//    zipOut_.putNextEntry(new ZipEntry(file.getName()));
//  
//    // Create a buffer for reading the files
//    byte[] buf = new byte[1024];
//
//    // Transfer bytes from the file to the ZIP file
//    int len;
//    while ((len = in.read(buf)) > 0) {
//        zipOut_.write(buf, 0, len);
//    }
//  
//    // Complete the entry
//    zipOut_.closeEntry();
//    in.close();
//       
//    file.delete();
//    exportTime=sample.getTimestamp();
    
  }

  @Override
  public String getDescription() {
    return "JPEG Zip Archive";
  }

  @Override
  public String getDefaultFileExtension() {
    return "zip";
  }

}
