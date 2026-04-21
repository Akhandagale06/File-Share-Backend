package com.aditya.File.share.spring.controller;

import com.aditya.File.share.spring.documents.UserCredits;
import com.aditya.File.share.spring.dto.FileMetaDataDTO;
import com.aditya.File.share.spring.service.FileMetaDataService;
import com.aditya.File.share.spring.service.UserCreditsService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("/files")
public class FileController {

    private  final FileMetaDataService fileMetaDataService;
    private final UserCreditsService userCreditsService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFiles(@RequestParam("files") MultipartFile [] files) throws IOException {
        Map<String, Object> response = new HashMap<>();

       List<FileMetaDataDTO> list= fileMetaDataService.uploadFiles(files);

        UserCredits finalCredits= userCreditsService.getUserCredits();
        response.put("files", list);
        response.put("remainingCredits",finalCredits.getCredits());
        return ResponseEntity.ok(response);
    }
    @GetMapping("/my")
    public ResponseEntity<?> getFilesForCurrentUser(){
        List<FileMetaDataDTO> files= fileMetaDataService.getFiles();
        return ResponseEntity.ok(files);
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<?> getPublicFile(@PathVariable String id){
        FileMetaDataDTO file =fileMetaDataService.getPublicFile(id);
        return ResponseEntity.ok(file);
    }
   @GetMapping("/download/{id}")
    public ResponseEntity<?> download(@PathVariable String id) {
        FileMetaDataDTO file = fileMetaDataService.getDownloadFiles(id);
        
        Map<String, String> response = new HashMap<>();
        response.put("url", file.getFileLocation());
        response.put("name", file.getName());
        
        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?>  deleteFile(@PathVariable String id){
        fileMetaDataService.deleteFile(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle-public")
    public ResponseEntity<?> togglePublic (@PathVariable String id){
     FileMetaDataDTO file= fileMetaDataService.togglePublic(id);
        return ResponseEntity.ok(file);
    }
}
