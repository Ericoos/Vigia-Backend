package com.uscs.vigia.service;

import com.uscs.vigia.model.Usuario;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Locale;

@Service
public class CodigoService {

    private static final int VALIDADE_MINUTOS = 10;
    private static final int LIMITE_TENTATIVAS = 5;
    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    private final SecureRandom geradorAleatorio = new SecureRandom();

    public void gerar(Usuario usuario) {
        usuario.setCodigoVerificacao(String.format(Locale.ROOT, "%06d", geradorAleatorio.nextInt(1_000_000)));
        usuario.setCodigoExpiraEm(LocalDateTime.now(FUSO).plusMinutes(VALIDADE_MINUTOS));
        usuario.setTentativasCodigo(0);
    }

    public boolean validar(Usuario usuario, String codigo) {
        if (usuario.getCodigoVerificacao() == null || usuario.getCodigoExpiraEm() == null) {
            return false;
        }
        if (!LocalDateTime.now(FUSO).isBefore(usuario.getCodigoExpiraEm())) {
            usuario.setCodigoVerificacao(null);
            return false;
        }

        int tentativas = usuario.getTentativasCodigo() != null ? usuario.getTentativasCodigo() : 0;
        if (tentativas >= LIMITE_TENTATIVAS) {
            usuario.setCodigoVerificacao(null);
            return false;
        }
        if (usuario.getCodigoVerificacao().equals(codigo)) {
            usuario.setCodigoVerificacao(null);
            return true;
        }

        tentativas++;
        usuario.setTentativasCodigo(tentativas);
        if (tentativas >= LIMITE_TENTATIVAS) {
            usuario.setCodigoVerificacao(null);
        }
        return false;
    }

    public boolean podeReenviar(Usuario usuario) {
        if (usuario.getCodigoExpiraEm() == null) {
            return true;
        }
        LocalDateTime ultimoCodigoEm = usuario.getCodigoExpiraEm().minusMinutes(VALIDADE_MINUTOS);
        return !LocalDateTime.now(FUSO).isBefore(ultimoCodigoEm.plusMinutes(1));
    }
}
