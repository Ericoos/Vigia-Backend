package com.uscs.vigia.service;

import com.uscs.vigia.enums.FinalidadeCodigo;
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

    public void gerar(Usuario usuario, FinalidadeCodigo finalidade) {
        usuario.setCodigoVerificacao(String.format(Locale.ROOT, "%06d", geradorAleatorio.nextInt(1_000_000)));
        usuario.setCodigoExpiraEm(LocalDateTime.now(FUSO).plusMinutes(VALIDADE_MINUTOS));
        usuario.setTentativasCodigo(0);
        usuario.setFinalidadeCodigo(finalidade);
    }

    public boolean validar(Usuario usuario, String codigo, FinalidadeCodigo finalidade) {
        if (usuario.getCodigoVerificacao() == null || usuario.getCodigoExpiraEm() == null) {
            return false;
        }
        // Código gerado para outra finalidade não vale aqui (e não é consumido).
        if (usuario.getFinalidadeCodigo() != finalidade) {
            return false;
        }
        if (!LocalDateTime.now(FUSO).isBefore(usuario.getCodigoExpiraEm())) {
            limpar(usuario);
            return false;
        }

        int tentativas = usuario.getTentativasCodigo() != null ? usuario.getTentativasCodigo() : 0;
        if (tentativas >= LIMITE_TENTATIVAS) {
            limpar(usuario);
            return false;
        }
        if (usuario.getCodigoVerificacao().equals(codigo)) {
            limpar(usuario);
            return true;
        }

        tentativas++;
        usuario.setTentativasCodigo(tentativas);
        if (tentativas >= LIMITE_TENTATIVAS) {
            limpar(usuario);
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

    private void limpar(Usuario usuario) {
        usuario.setCodigoVerificacao(null);
        usuario.setFinalidadeCodigo(null);
    }
}
