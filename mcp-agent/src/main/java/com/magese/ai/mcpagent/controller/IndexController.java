package com.magese.ai.mcpagent.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * Index
 *
 * @author Magese
 * @since 2025/10/10 15:18
 */
@Controller
public class IndexController {

    @GetMapping(value = "")
    public Mono<ResponseEntity<Void>> index() {
        return Mono.just(ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create("/index.html"))
                .build()
        );
    }
}
