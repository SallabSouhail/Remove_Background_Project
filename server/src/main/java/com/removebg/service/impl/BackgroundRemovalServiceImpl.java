package com.removebg.service.impl;

import com.removebg.exception.CreditInsufficientException;
import com.removebg.exception.OperationFailedException;
import com.removebg.exception.UnsupportedFormatException;
import com.removebg.exception.UserNotFoundException;
import com.removebg.service.BackgroundRemovalService;
import com.removebg.service.RemoveBgClient;
import com.removebg.service.UserCreditService;
import com.removebg.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class BackgroundRemovalServiceImpl implements BackgroundRemovalService {

    private static final int REMOVE_BG_CREDIT_COST = 1;

    private final RemoveBgClient removeBgClient;
    private final UserService userService;
    private final UserCreditService userCreditService;

    @Override
    public byte[] processAndRemoveBackground(MultipartFile image, String clerkId) throws IOException {
        if (userService.getUserByClerkId(clerkId).isEmpty()) {
            throw new UserNotFoundException("User not found");
        }

        if (!userCreditService.consumeCredits(clerkId, REMOVE_BG_CREDIT_COST)) {
            throw new CreditInsufficientException("Credit insufficient, please charge your credits");
        }

        byte[] removedBgImage;
        try {
            removedBgImage = removeBgClient.removeBackground(image);
        } catch (Exception ex) {
            userCreditService.refundCredits(clerkId, REMOVE_BG_CREDIT_COST);
            log.warn("Background removal failed for clerk user {}, refunded the consumed credit", clerkId, ex);
            throw new OperationFailedException("Removing background has been failed, please try again");
        }

        if (removedBgImage == null || removedBgImage.length == 0) {
            userCreditService.refundCredits(clerkId, REMOVE_BG_CREDIT_COST);
            log.warn("Background removal returned an empty response for clerk user {}, refunded the consumed credit", clerkId);
            throw new OperationFailedException("Removing background has been failed, please try again");
        }

        return removedBgImage;
    }

    public boolean isSupportedFormat(MultipartFile image) {
        String imageType = image.getContentType();
        if (imageType != null) {
            return imageType.equalsIgnoreCase(MediaType.IMAGE_JPEG_VALUE)
                    || imageType.equalsIgnoreCase(MediaType.IMAGE_PNG_VALUE);
        }
        return false;
    }

    public boolean isSupportedExtension(MultipartFile image) {
        String imageName = image.getOriginalFilename();
        if (isValidName(imageName)) {
            String extension = imageName.substring(imageName.lastIndexOf("."));
            return extension.equalsIgnoreCase(".jpg")
                    || extension.equalsIgnoreCase(".jpeg")
                    || extension.equalsIgnoreCase(".png");
        }
        return false;
    }

    public boolean isValidName(String imageName) {
        return imageName != null && imageName.contains(".");
    }

    @Override
    public String getProcessedImageName(MultipartFile image) throws UnsupportedFormatException {
        if (image == null) {
            throw new UnsupportedFormatException("The image format is not supported");
        }

        String imageName = image.getOriginalFilename();
        if (isValidName(imageName) && isSupportedFormat(image) && isSupportedExtension(image)) {
            String baseName = imageName.substring(0, imageName.lastIndexOf("."));
            String extension = imageName.substring(imageName.lastIndexOf("."));
            return baseName + "no-bg" + extension;
        }

            throw new UnsupportedFormatException("The image format is not supported");
    }
}
