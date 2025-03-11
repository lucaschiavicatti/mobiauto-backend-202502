package com.mobiauto.backend.service;

import com.mobiauto.backend.dto.OportunidadeRequestDTO;
import com.mobiauto.backend.dto.OportunidadeResponseDTO;
import com.mobiauto.backend.dto.UsuarioResponseDTO;
import com.mobiauto.backend.mapper.OportunidadeMapper;
import com.mobiauto.backend.model.*;
import com.mobiauto.backend.repository.ClienteRepository;
import com.mobiauto.backend.repository.OportunidadeRepository;
import com.mobiauto.backend.repository.RevendaRepository;
import com.mobiauto.backend.repository.UsuarioRepository;
import com.mobiauto.backend.repository.VeiculoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.mobiauto.backend.model.Cargo.ASSISTENTE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OportunidadeServiceTest {

    @Mock
    private OportunidadeRepository oportunidadeRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private VeiculoRepository veiculoRepository;

    @Mock
    private RevendaRepository revendaRepository;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private OportunidadeMapper oportunidadeMapper;

    @InjectMocks
    private OportunidadeService oportunidadeService;

    private Usuario usuarioAdmin;
    private Usuario usuarioGerente;
    private Revenda revenda;

    @BeforeEach
    void setUp() {
        usuarioAdmin = new Usuario(1L, "Admin", "admin@example.com", "senha", Cargo.ADMINISTRADOR, new Revenda(1L));
        revenda = new Revenda(1L);
        usuarioGerente = new Usuario(2L, "Gerente", "gerente@example.com", "senha", Cargo.GERENTE, revenda);
    }

    private void mockSecurityContext(Usuario usuario) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(usuario);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void findAll_DeveRetornarTodasOportunidades_QuandoUsuarioAdmin() {
        mockSecurityContext(usuarioAdmin);
        Oportunidade oportunidade = new Oportunidade();
        when(oportunidadeRepository.findAll()).thenReturn(List.of(oportunidade));
        OportunidadeResponseDTO dto = new OportunidadeResponseDTO(
                1L,
                new OportunidadeResponseDTO.ClienteDTO(1L, "Cliente", "cliente@example.com", "123"),
                new OportunidadeResponseDTO.VeiculoDTO(1L, "VW", "Gol", "1.0", 2020),
                2L,
                1L,
                "EM_ATENDIMENTO",
                null,
                LocalDateTime.now(),
                null
        );
        when(oportunidadeMapper.toResponseDTO(oportunidade)).thenReturn(dto);

        List<OportunidadeResponseDTO> result = oportunidadeService.findAll();

        assertEquals(1, result.size());
        assertEquals(dto, result.get(0));
        verify(oportunidadeRepository).findAll();
    }

    @Test
    void findAll_DeveRetornarOportunidadesDaRevenda_QuandoUsuarioNaoAdmin() {
        mockSecurityContext(usuarioGerente);
        Oportunidade oportunidade = new Oportunidade();
        when(oportunidadeRepository.findAllByRevenda_Id(1L)).thenReturn(List.of(oportunidade));
        OportunidadeResponseDTO dto = new OportunidadeResponseDTO(
                1L,
                new OportunidadeResponseDTO.ClienteDTO(1L, "Cliente", "cliente@example.com", "123"),
                new OportunidadeResponseDTO.VeiculoDTO(1L, "VW", "Gol", "1.0", 2020),
                2L,
                1L,
                "EM_ATENDIMENTO",
                null,
                LocalDateTime.now(),
                null
        );
        when(oportunidadeMapper.toResponseDTO(oportunidade)).thenReturn(dto);

        List<OportunidadeResponseDTO> result = oportunidadeService.findAll();

        assertEquals(1, result.size());
        assertEquals(dto, result.get(0));
        verify(oportunidadeRepository).findAllByRevenda_Id(1L);
    }

    @Test
    void findById_DeveRetornarOportunidade_QuandoUsuarioTemAcesso() {
        mockSecurityContext(usuarioGerente);
        Oportunidade oportunidade = new Oportunidade();
        oportunidade.setRevenda(revenda);
        when(oportunidadeRepository.findById(1L)).thenReturn(Optional.of(oportunidade));
        OportunidadeResponseDTO dto = new OportunidadeResponseDTO(
                1L,
                new OportunidadeResponseDTO.ClienteDTO(1L, "Cliente", "cliente@example.com", "123"),
                new OportunidadeResponseDTO.VeiculoDTO(1L, "VW", "Gol", "1.0", 2020),
                2L,
                1L,
                "EM_ATENDIMENTO",
                null,
                LocalDateTime.now(),
                null
        );
        when(oportunidadeMapper.toResponseDTO(oportunidade)).thenReturn(dto);

        OportunidadeResponseDTO result = oportunidadeService.findById(1L);

        assertEquals(dto, result);
        verify(oportunidadeRepository).findById(1L);
    }

    @Test
    void findById_DeveLancarNotFound_QuandoOportunidadeNaoExiste() {
        mockSecurityContext(usuarioGerente);
        when(oportunidadeRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> oportunidadeService.findById(1L));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void save_DeveSalvarOportunidade_QuandoDadosValidosComUsuario() {
        mockSecurityContext(usuarioGerente);
        OportunidadeRequestDTO dto = new OportunidadeRequestDTO(1L, 1L, 2L, 1L, "EM_ATENDIMENTO", null);
        Cliente cliente = new Cliente();
        Veiculo veiculo = new Veiculo();
        Usuario usuario = new Usuario(2L, "Assistente", "assistente@example.com", "senha", ASSISTENTE, revenda);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculo));
        when(revendaRepository.findById(1L)).thenReturn(Optional.of(revenda));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario));
        Oportunidade oportunidade = new Oportunidade();
        when(oportunidadeRepository.save(any(Oportunidade.class))).thenReturn(oportunidade);
        OportunidadeResponseDTO responseDTO = new OportunidadeResponseDTO(
                1L,
                new OportunidadeResponseDTO.ClienteDTO(1L, "Cliente", "cliente@example.com", "123"),
                new OportunidadeResponseDTO.VeiculoDTO(1L, "VW", "Gol", "1.0", 2020),
                2L,
                1L,
                "EM_ATENDIMENTO",
                null,
                LocalDateTime.now(),
                null
        );
        when(oportunidadeMapper.toResponseDTO(oportunidade)).thenReturn(responseDTO);

        OportunidadeResponseDTO result = oportunidadeService.save(dto);

        assertEquals(responseDTO, result);
        verify(oportunidadeRepository).save(any(Oportunidade.class));
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void save_DeveSalvarOportunidade_QuandoDistribuicaoAutomatica() {
        mockSecurityContext(usuarioGerente);
        OportunidadeRequestDTO dto = new OportunidadeRequestDTO(1L, 1L, null, 1L, "EM_ATENDIMENTO", null);
        Cliente cliente = new Cliente();
        Veiculo veiculo = new Veiculo();
        Usuario assistente = new Usuario(3L, "Assistente", "assistente@example.com", "senha", ASSISTENTE, revenda);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculo));
        when(revendaRepository.findById(1L)).thenReturn(Optional.of(revenda));
        when(usuarioService.findAll()).thenReturn(List.of(new UsuarioResponseDTO(3L, "Assistente", "assistente@example.com", ASSISTENTE, 1L, null)));
        when(usuarioService.toUsuario(any(UsuarioResponseDTO.class))).thenReturn(assistente);
        Oportunidade oportunidade = new Oportunidade();
        when(oportunidadeRepository.save(any(Oportunidade.class))).thenReturn(oportunidade);
        OportunidadeResponseDTO responseDTO = new OportunidadeResponseDTO(
                1L,
                new OportunidadeResponseDTO.ClienteDTO(1L, "Cliente", "cliente@example.com", "123"),
                new OportunidadeResponseDTO.VeiculoDTO(1L, "VW", "Gol", "1.0", 2020),
                3L,
                1L,
                "EM_ATENDIMENTO",
                null,
                LocalDateTime.now(),
                null
        );
        when(oportunidadeMapper.toResponseDTO(oportunidade)).thenReturn(responseDTO);

        OportunidadeResponseDTO result = oportunidadeService.save(dto);

        assertEquals(responseDTO, result);
        verify(oportunidadeRepository).save(any(Oportunidade.class));
        verify(usuarioRepository).save(assistente);
    }

    @Test
    void save_DeveLancarBadRequest_QuandoConcluidoSemMotivo() {
        mockSecurityContext(usuarioGerente);
        OportunidadeRequestDTO dto = new OportunidadeRequestDTO(1L, 1L, 2L, 1L, "CONCLUIDO", null);
        Cliente cliente = new Cliente();
        Veiculo veiculo = new Veiculo();
        Usuario usuario = new Usuario(2L, "Assistente", "assistente@example.com", "senha", ASSISTENTE, revenda);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculo));
        when(revendaRepository.findById(1L)).thenReturn(Optional.of(revenda));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> oportunidadeService.save(dto));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void update_DeveAtualizarOportunidade_QuandoUsuarioTemPermissao() {
        mockSecurityContext(usuarioGerente);
        OportunidadeRequestDTO dto = new OportunidadeRequestDTO(1L, 1L, 2L, 1L, "CONCLUIDO", "Venda realizada");
        Oportunidade oportunidade = new Oportunidade();
        oportunidade.setRevenda(revenda);
        oportunidade.setUsuario(usuarioGerente);
        when(oportunidadeRepository.findById(1L)).thenReturn(Optional.of(oportunidade));
        Cliente cliente = new Cliente();
        Veiculo veiculo = new Veiculo();
        Usuario usuario = new Usuario(2L, "Assistente", "assistente@example.com", "senha", ASSISTENTE, revenda);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculo));
        when(revendaRepository.findById(1L)).thenReturn(Optional.of(revenda));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario));
        when(oportunidadeRepository.save(any(Oportunidade.class))).thenReturn(oportunidade);
        OportunidadeResponseDTO responseDTO = new OportunidadeResponseDTO(
                1L,
                new OportunidadeResponseDTO.ClienteDTO(1L, "Cliente", "cliente@example.com", "123"),
                new OportunidadeResponseDTO.VeiculoDTO(1L, "VW", "Gol", "1.0", 2020),
                2L,
                1L,
                "CONCLUIDO",
                "Venda realizada",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        when(oportunidadeMapper.toResponseDTO(oportunidade)).thenReturn(responseDTO);

        OportunidadeResponseDTO result = oportunidadeService.update(1L, dto);

        assertEquals(responseDTO, result);
        verify(oportunidadeRepository).save(any(Oportunidade.class));
    }

    @Test
    void delete_DeveDeletarOportunidade_QuandoUsuarioTemPermissao() {
        mockSecurityContext(usuarioGerente);
        Oportunidade oportunidade = new Oportunidade();
        oportunidade.setRevenda(revenda);
        when(oportunidadeRepository.findById(1L)).thenReturn(Optional.of(oportunidade));

        oportunidadeService.delete(1L);

        verify(oportunidadeRepository).deleteById(1L);
    }

    @Test
    void delete_DeveLancarForbidden_QuandoUsuarioSemPermissao() {
        mockSecurityContext(usuarioGerente);
        Oportunidade oportunidade = new Oportunidade();
        oportunidade.setRevenda(new Revenda(2L));
        when(oportunidadeRepository.findById(1L)).thenReturn(Optional.of(oportunidade));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> oportunidadeService.delete(1L));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }
}