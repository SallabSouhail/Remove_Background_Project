package com.removebg.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface RemoveBgClient {
    byte[] removeBackground(MultipartFile image) throws IOException;
}