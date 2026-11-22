package com.uscs.vigia.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SmsService {

    public void enviar(String telefone, String mensagem) {
        log.info("[SMS DEV] Para {}: {}", telefone, mensagem);
    }
}
