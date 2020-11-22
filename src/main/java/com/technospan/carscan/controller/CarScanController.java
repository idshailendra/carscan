package com.technospan.carscan.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.technospan.carscan.dto.UploadFileResponse;
import com.technospan.carscan.service.FileStorageService;

@RestController
public class CarScanController {

	private static final Logger logger = LoggerFactory.getLogger(CarScanController.class);
	
	@Autowired
    private FileStorageService fileStorageService;
    
    @PostMapping("/uploadFile")
    public UploadFileResponse uploadFile(@RequestParam("file") MultipartFile file)  {
    	float quality=fileStorageService.getCompressionQuality(file.getSize());
    	
    	if (quality<0){
    		return new UploadFileResponse(file.getName(), "Too large to upload", file.getSize());
    	}
    	String fileName = fileStorageService.storeFile(file,quality);
         String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                 .path("/downloadFile/")
                 .path(fileName)
                 .toUriString();

         return new UploadFileResponse(fileName, fileDownloadUri,
                 file.getContentType(), file.getSize());    }

    @PostMapping("/uploadMultipleFiles")
    public List<UploadFileResponse> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
        return Arrays.asList(files)
                .stream()
                .map(file -> uploadFile(file))
                .collect(Collectors.toList());
    }

    @GetMapping("/downloadFile/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) throws IOException {
    	 Resource resource = fileStorageService.loadFileAsResource(fileName);

         String contentType = null;
         try {
             contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
         } catch (IOException ex) {
             logger.info("Could not determine file type.");
         }

         if(contentType == null) {
             contentType = "application/octet-stream";
         }

         return ResponseEntity.ok()
                 .contentType(MediaType.parseMediaType(contentType))
                 .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                 .body(resource);
     }
}
