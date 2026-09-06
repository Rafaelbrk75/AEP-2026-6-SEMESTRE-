package br.com.unicesumar.aep.imunizamais.web;

import br.com.unicesumar.aep.imunizamais.domain.Campanha;
import br.com.unicesumar.aep.imunizamais.service.CampanhaService;
import br.com.unicesumar.aep.imunizamais.web.dto.CoberturaCampanhaResponse;
import br.com.unicesumar.aep.imunizamais.web.dto.NovaCampanhaRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/campanhas")
public class CampanhaController {

    private final CampanhaService campanhaService;

    public CampanhaController(CampanhaService campanhaService) {
        this.campanhaService = campanhaService;
    }

    @PostMapping
    public ResponseEntity<Campanha> cadastrar(@Valid @RequestBody NovaCampanhaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(campanhaService.cadastrar(request));
    }

    @GetMapping
    public ResponseEntity<List<Campanha>> listar(
            @RequestParam(name = "apenasAtivas", defaultValue = "false") boolean apenasAtivas) {
        return ResponseEntity.ok(apenasAtivas ? campanhaService.listarAtivas() : campanhaService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Campanha> buscar(@PathVariable String id) {
        return ResponseEntity.ok(campanhaService.buscarPorId(id));
    }

    @GetMapping("/{id}/cobertura")
    public ResponseEntity<CoberturaCampanhaResponse> cobertura(@PathVariable String id) {
        return ResponseEntity.ok(campanhaService.cobertura(id));
    }

    @PatchMapping("/{id}/encerramento")
    public ResponseEntity<Campanha> encerrar(@PathVariable String id) {
        return ResponseEntity.ok(campanhaService.encerrar(id));
    }
}
