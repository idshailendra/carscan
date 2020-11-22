package com.technospan.carscan.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Collectors;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.technospan.carscan.config.FileStorageProperties;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;
    
    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
        	logger.error("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }
    

    public float getCompressionQuality(long size){
    	if(size>5100000l){
    		return -1f;
    	}else if(size>4100000l){
    		return .5f;
    	}else if(size>3100000l){
    		return .6f;
    	}else if(size>2100000l){
    		return .8f;
    	} else {
    		return 1f;
    	}
    	
    }
    

    public String storeFile(MultipartFile file,float quality) {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            if(fileName.contains("..")) {
                throw new IOException("Sorry! Filename contains invalid path sequence " + fileName);
            }
          BufferedImage image = ImageIO.read(file.getInputStream());            

          Path targetLocation = this.fileStorageLocation.resolve(fileName);
  	      File compressedImageFile = new File(targetLocation.toString());
  	      OutputStream os =new FileOutputStream(compressedImageFile);

  	      Iterator<ImageWriter>writers =  ImageIO.getImageWritersByFormatName("jpg");
  	      ImageWriter writer = (ImageWriter) writers.next();

  	      ImageOutputStream ios = ImageIO.createImageOutputStream(os);
  	      writer.setOutput(ios);

  	      ImageWriteParam param = writer.getDefaultWriteParam();
  	      
  	      param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
  	      param.setCompressionQuality(quality);
  	      writer.write(null, new IIOImage(image, null, null), param);
  	      
  	      os.close();
  	      ios.close();
  	      writer.dispose();
        } catch (IOException ex) {
        	logger.error("Could not create the directory where the uploaded files will be stored.", ex);
        }
        return fileName;
    }

    public Resource loadFileAsResource(String fileName) throws IOException {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()) {
                return resource;
            } else {
                throw new IOException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new IOException("File not found " + fileName, ex);
            
        }
    }
}