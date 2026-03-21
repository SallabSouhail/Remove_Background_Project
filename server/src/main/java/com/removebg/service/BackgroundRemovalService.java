package com.removebg.service;

import com.removebg.exception.UnsupportedFormatException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface BackgroundRemovalService {
    public byte[] processAndRemoveBackground(MultipartFile image, String clerkId) throws IOException;
    public String getProcessedImageName(MultipartFile image) throws UnsupportedFormatException;


}
