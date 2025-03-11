package com.mobiauto.backend.service;

import com.mobiauto.backend.dto.VeiculoRequestDTO;
import com.mobiauto.backend.dto.VeiculoResponseDTO;
import com.mobiauto.backend.mapper.VeiculoMapper;
import com.mobiauto.backend.model.Cargo;
import com.mobiauto.backend.model.Revenda;
import com.mobiauto.backend.model.Usuario;
import com.mobiauto.backend.model.Veiculo;
import com.mobiauto.backend.repository.RevendaRepository;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VeiculoServiceTest {

    @Mock
    private VeiculoRepository veiculoRepository;

    @Mock
    private RevendaRepository revendaRepository;

    @Mock
    private VeiculoMapper veiculoMapper;

    @InjectMocks
    private VeiculoService veiculoService;

    private Usuario usuarioAdmin;
    private Usuario usuarioGerente;
    private Revenda revenda;

    @BeforeEach
    void setUp() {
        revenda = new Revenda(1L);
        usuarioAdmin = new Usuario(1L, "Admin", "admin@example.com", "senha", Cargo.ADMINISTRADOR, revenda);
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
    void findAll_DeveRetornarTodosVeiculos_QuandoUsuarioAdmin() {
        mockSecurityContext(usuarioAdmin);
        Veiculo veiculo = new Veiculo();
        when(veiculoRepository.findAll()).thenReturn(List.of(veiculo));
        VeiculoResponseDTO dto = new VeiculoResponseDTO(1L, "teste", "teste", "teste", 2025, 1L);
        when(veiculoMapper.toResponseDTO(veiculo)).thenReturn(dto);

        List<VeiculoResponseDTO> result = veiculoService.findAll();

        assertEquals(1, result.size());
        assertEquals(dto, result.get(0));
        verify(veiculoRepository).findAll();
    }

    @Test
    void findAll_DeveRetornarVeiculosDaRevenda_QuandoUsuarioNaoAdmin() {
        mockSecurityContext(usuarioGerente);
        Veiculo veiculo = new Veiculo();
        when(veiculoRepository.findAllByRevenda_Id(1L)).thenReturn(List.of(veiculo));
        VeiculoResponseDTO dto = new VeiculoResponseDTO(1L, "teste", "teste", "teste", 2025, 1L);
        when(veiculoMapper.toResponseDTO(veiculo)).thenReturn(dto);

        List<VeiculoResponseDTO> result = veiculoService.findAll();

        assertEquals(1, result.size());
        assertEquals(dto, result.get(0));
        verify(veiculoRepository).findAllByRevenda_Id(1L);
    }

    @Test
    void findById_DeveRetornarVeiculo_QuandoUsuarioTemAcesso() {
        mockSecurityContext(usuarioGerente);
        Veiculo veiculo = new Veiculo();
        veiculo.setRevenda(revenda);
        when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculo));
        VeiculoResponseDTO dto = new VeiculoResponseDTO(1L, "teste", "teste", "teste", 2025, 1L);
        when(veiculoMapper.toResponseDTO(veiculo)).thenReturn(dto);

        VeiculoResponseDTO result = veiculoService.findById(1L);

        assertEquals(dto, result);
        verify(veiculoRepository).findById(1L);
    }

    @Test
    void findById_DeveLancarForbidden_QuandoUsuarioSemAcesso() {
        mockSecurityContext(usuarioGerente);
        Veiculo veiculo = new Veiculo();
        veiculo.setRevenda(new Revenda(2L));
        when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculo));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> veiculoService.findById(1L));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void findById_DeveLancarNotFound_QuandoVeiculoNaoExiste() {
        mockSecurityContext(usuarioAdmin);
        when(veiculoRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> veiculoService.findById(1L));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void save_DeveSalvarVeiculo_QuandoGerenteNaPropriaRevenda() {
        Revenda revendaMock = mock(Revenda.class);
        when(revendaMock.getId()).thenReturn(1L);
        usuarioGerente.setRevenda(revendaMock);
        mockSecurityContext(usuarioGerente);
        VeiculoRequestDTO dto = new VeiculoRequestDTO("teste", "teste", "teste", 2025, 1L);
        when(revendaRepository.findById(1L)).thenReturn(Optional.of(revenda));
        Veiculo veiculo = new Veiculo();
        when(veiculoRepository.save(any(Veiculo.class))).thenReturn(veiculo);
        VeiculoResponseDTO responseDTO = new VeiculoResponseDTO(1L, "teste", "teste", "teste", 2025, 1L);
        when(veiculoMapper.toResponseDTO(veiculo)).thenReturn(responseDTO);

        VeiculoResponseDTO result = veiculoService.save(dto);

        assertEquals(responseDTO, result);
        verify(veiculoRepository).save(any(Veiculo.class));
    }

    @Test
    void save_DeveLancarForbidden_QuandoGerenteForaDaRevenda() {
        mockSecurityContext(usuarioGerente);
        VeiculoRequestDTO dto = new VeiculoRequestDTO("teste", "teste", "teste", 2025, 2L);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> veiculoService.save(dto));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void save_DeveLancarNotFound_QuandoRevendaNaoExiste() {
        mockSecurityContext(usuarioAdmin);
        VeiculoRequestDTO dto = new VeiculoRequestDTO("teste", "teste", "teste", 2025, 1L);
        when(revendaRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> veiculoService.save(dto));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void update_DeveAtualizarVeiculo_QuandoGerenteNaPropriaRevenda() {
        Revenda revendaMock = mock(Revenda.class);
        when(revendaMock.getId()).thenReturn(1L);
        usuarioGerente.setRevenda(revendaMock);
        mockSecurityContext(usuarioGerente);
        VeiculoRequestDTO dto = new VeiculoRequestDTO("teste", "teste", "teste", 2021, 1L);
        Veiculo veiculo = new Veiculo();
        veiculo.setRevenda(revenda);
        when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculo));
        when(revendaRepository.findById(1L)).thenReturn(Optional.of(revenda));
        when(veiculoRepository.save(any(Veiculo.class))).thenReturn(veiculo);
        VeiculoResponseDTO responseDTO = new VeiculoResponseDTO(1L, "teste", "teste", "teste", 2021, 1L);
        when(veiculoMapper.toResponseDTO(veiculo)).thenReturn(responseDTO);

        VeiculoResponseDTO result = veiculoService.update(1L, dto);

        assertEquals(responseDTO, result);
        verify(veiculoRepository).save(any(Veiculo.class));
    }

    @Test
    void update_DeveLancarForbidden_QuandoGerenteForaDaRevenda() {
        mockSecurityContext(usuarioGerente);
        Veiculo veiculo = new Veiculo();
        veiculo.setRevenda(new Revenda(2L));
        when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculo));
        VeiculoRequestDTO dto = new VeiculoRequestDTO("teste", "teste", "teste", 2021, 2L);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> veiculoService.update(1L, dto));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void update_DeveLancarNotFound_QuandoVeiculoNaoExiste() {
        mockSecurityContext(usuarioAdmin);
        VeiculoRequestDTO dto = new VeiculoRequestDTO("teste", "teste", "teste", 2021, 1L);
        when(veiculoRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> veiculoService.update(1L, dto));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void delete_DeveDeletarVeiculo_QuandoAdmin() {
        mockSecurityContext(usuarioAdmin);
        Veiculo veiculo = new Veiculo();
        when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculo));

        veiculoService.delete(1L);

        verify(veiculoRepository).deleteById(1L);
    }

    @Test
    void delete_DeveLancarForbidden_QuandoGerenteForaDaRevenda() {
        mockSecurityContext(usuarioGerente);
        Veiculo veiculo = new Veiculo();
        veiculo.setRevenda(new Revenda(2L));
        when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculo));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> veiculoService.delete(1L));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void delete_DeveLancarNotFound_QuandoVeiculoNaoExiste() {
        mockSecurityContext(usuarioAdmin);
        when(veiculoRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> veiculoService.delete(1L));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }
}