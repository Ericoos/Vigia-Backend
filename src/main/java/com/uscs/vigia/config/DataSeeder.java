package com.uscs.vigia.config;

import com.uscs.vigia.model.*;
import com.uscs.vigia.repository.BairroRepository;
import com.uscs.vigia.repository.CategoriaRepository;
import com.uscs.vigia.repository.SubcategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CategoriaRepository categoriaRepository;
    private final SubcategoriaRepository subcategoriaRepository;

    private final BairroRepository bairroRepository;

    @Override
    public void run(String... args) {
        seedCategorias();
        seedBairros();
    }

    private void seedCategorias() {
        if (categoriaRepository.count() > 0 || subcategoriaRepository.count() > 0) {
            return;
        }

        Categoria seguranca = new Categoria(TipoCategoria.SEGURANCA);
        Categoria inseguranca = new Categoria(TipoCategoria.INSEGURANCA);

        categoriaRepository.saveAll(List.of(seguranca, inseguranca));

        Subcategoria iluminacaoBoa = new Subcategoria(TipoSubcategoria.ILUMINACAO_BOA, seguranca);
        Subcategoria areaMovimentada = new Subcategoria(TipoSubcategoria.AREA_MOVIMENTADA, seguranca);
        Subcategoria presencaPolicial = new Subcategoria(TipoSubcategoria.PRESENCA_POLICIAL, seguranca);
        Subcategoria comercioAtivo = new Subcategoria(TipoSubcategoria.COMERCIO_ATIVO, seguranca);
        Subcategoria cameras = new Subcategoria(TipoSubcategoria.CAMERAS, seguranca);

        Subcategoria iluminacaoRuim = new Subcategoria(TipoSubcategoria.ILUMINACAO_RUIM, inseguranca);
        Subcategoria roubo = new Subcategoria(TipoSubcategoria.ROUBO, inseguranca);
        Subcategoria furto = new Subcategoria(TipoSubcategoria.FURTO, inseguranca);
        Subcategoria vandalismo = new Subcategoria(TipoSubcategoria.VANDALISMO, inseguranca);
        Subcategoria acidente = new Subcategoria(TipoSubcategoria.ACIDENTE, inseguranca);

        subcategoriaRepository.saveAll(List.of(iluminacaoBoa, areaMovimentada, presencaPolicial, comercioAtivo, cameras, iluminacaoRuim, roubo, furto, vandalismo, acidente));
    }

    private void seedBairros() {
        if (bairroRepository.count() == 0) {
            bairroRepository.saveAll(Arrays.stream(TipoBairro.values()).map(Bairro::new).toList());
        }
    }
}
