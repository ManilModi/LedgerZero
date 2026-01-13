package org.example.dto.common;

import lombok.*;

import java.util.Map;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Response {
    private String message;
    private int statusCode;
    private String error;
    private Map<String, Object> data;
}
