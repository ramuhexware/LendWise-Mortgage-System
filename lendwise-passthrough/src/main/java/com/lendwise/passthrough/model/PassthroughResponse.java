package com.lendwise.passthrough.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic response wrapper for pass-through operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PassthroughResponse {

    private String correlationId;
    private String status;
    private Object data;
    private String errorCode;
    private String errorMessage;
    private long durationMs;
}
