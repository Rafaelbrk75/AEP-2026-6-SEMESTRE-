package br.com.unicesumar.aep.imunizamais.web;

import br.com.unicesumar.aep.imunizamais.domain.PostoSaude;
import br.com.unicesumar.aep.imunizamais.service.PostoSaudeService;
import br.com.unicesumar.aep.imunizamais.web.dto.NovoPostoSaudeRequest;
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
@RequestMapping("/api/postos-saude")
public class PostoSaudeController {

    private final PostoSaudeService postoSaudeService;

    public PostoSaudeController(PostoSaudeService postoSaudeService) {
        this.postoSaudeService = postoSaudeService;
    }

    @PostMapping
    public ResponseEntity<PostoSaude> cadastrar(@Valid @RequestBody NovoPostoSaudeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postoSaudeService.cadastrar(request));
    }

    @GetMapping
    public ResponseEntity<List<PostoSaude>> listar(
            @RequestParam(name = "apenasAtivos", defaultValue = "false") boolean apenasAtivos) {
        return ResponseEntity.ok(apenasAtivos ? postoSaudeService.listarAtivos() : postoSaudeService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostoSaude> buscar(@PathVariable String id) {
        return ResponseEntity.ok(postoSaudeService.buscarPorId(id));
    }

    @PatchMapping("/{id}/desativacao")
    public ResponseEntity<PostoSaude> desativar(@PathVariable String id) {
        return ResponseEntity.ok(postoSaudeService.desativar(id));
    }
}
