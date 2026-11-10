package com.bank.MaverickBank.dto.response;

public record UploadStatusDto(
        boolean pictureUploaded,
        boolean documentUploaded
) {
}
