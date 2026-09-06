package br.com.unicesumar.aep.imunizamais.web;

import br.com.unicesumar.aep.imunizamais.domain.Paciente;
import br.com.unicesumar.aep.imunizamais.domain.SituacaoPaciente;
import br.com.unicesumar.aep.imunizamais.service.PacienteService;
import br.com.unicesumar.aep.imunizamais.service.VacinacaoService;
import br.com.unicesumar.aep.imunizamais.web.dto.AplicacaoDoseRequest;
import br.com.unicesumar.aep.imunizamais.web.dto.NovoPacienteRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {

    private final PacienteService pacienteService;
    private final VacinacaoService vacinacaoService;

    public PacienteController(PacienteService pacienteService, VacinacaoService vacinacaoService) {
        this.pacienteService = pacienteService;
        this.vacinacaoService = vacinacaoService;
    }

    @PostMapping
    public ResponseEntity<Paciente> cadastrar(@Valid @RequestBody NovoPacienteRequest request) {
        Paciente paciente = pacienteService.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(paciente);
    }

    @GetMapping
    public ResponseEntity<List<Paciente>> listar(@RequestParam(required = false) String cidade) {
        List<Paciente> pacientes = (cidade == null || cidade.isBlank())
                ? pacienteService.listar()
                : pacienteService.listarPorCidade(cidade);
        return ResponseEntity.ok(pacientes);
    }

    @GetMapping("/{cpf}")
    public ResponseEntity<Paciente> buscar(@PathVariable String cpf) {
        return ResponseEntity.ok(pacienteService.buscarPorCpf(cpf));
    }

    @PostMapping("/{cpf}/doses")
    public ResponseEntity<Paciente> aplicarDose(@PathVariable String cpf,
                                                @Valid @RequestBody AplicacaoDoseRequest request) {
        Paciente atualizado = vacinacaoService.registrarAplicacao(cpf, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(atualizado);
    }

    @GetMapping("/{cpf}/situacao")
    public ResponseEntity<List<SituacaoPaciente>> situacao(@PathVariable String cpf) {
        return ResponseEntity.ok(vacinacaoService.consultarSituacao(cpf));
    }
}
