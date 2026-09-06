package br.com.unicesumar.aep.imunizamais.web;

import br.com.unicesumar.aep.imunizamais.domain.Vacina;
import br.com.unicesumar.aep.imunizamais.service.VacinaService;
import br.com.unicesumar.aep.imunizamais.web.dto.NovaVacinaRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vacinas")
public class VacinaController {

    private final VacinaService vacinaService;

    public VacinaController(VacinaService vacinaService) {
        this.vacinaService = vacinaService;
    }

    @PostMapping
    public ResponseEntity<Vacina> cadastrar(@Valid @RequestBody NovaVacinaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vacinaService.cadastrar(request));
    }

    @GetMapping
    public ResponseEntity<List<Vacina>> listar() {
        return ResponseEntity.ok(vacinaService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vacina> buscar(@PathVariable String id) {
        return ResponseEntity.ok(vacinaService.buscarPorId(id));
    }
}
