package com.matheusoliveira04.s3flow.exceptions;

import java.time.LocalDateTime;
import java.util.List;

public record StandardError (LocalDateTime timestamp, Integer statusCode, String uri, List<String> messages) {}
