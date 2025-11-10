package com.springcloud.msvc_items.infrastructure.integration.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springcloud.msvc_items.shared.exception.custom.ErrorResponse;
import com.springcloud.msvc_items.shared.exception.custom.InsufficientStockException;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import java.io.IOException;
import java.nio.charset.Charset;

public class CustomErrorDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper;

    public CustomErrorDecoder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() == 409) {
            try {
                String body = Util.toString(response.body().asReader(Charset.defaultCharset()));
                ErrorResponse error = objectMapper.readValue(body, ErrorResponse.class);

                if ("INSUFFICIENT_STOCK".equals(error.code())) {
                    return new InsufficientStockException(error.message());
                }
            } catch (IOException e) {
                System.err.println("Error de deserialización para status 409. Usando Default Decoder. Mensaje: " + e.getMessage());
            }
        }
        return new ErrorDecoder.Default().decode(methodKey, response);
    }
}