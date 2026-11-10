package com.bank.MaverickBank.dto.response;

public record PaginationDto(
        int totalPages,
        Long totalRecords,
        Integer numRecordsInPage
) {
}
