package com.lendwise.passthrough.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Generic request wrapper for pass-through operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PassthroughRequest {

    private String correlationId;
    private String sourceSystem;
    private String targetService;
    private String operation;
    private Map<String, String> headers;
    private Object payload;
}
