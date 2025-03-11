package com.mobiauto.backend.service;

import com.mobiauto.backend.dto.RevendaRequestDTO;
import com.mobiauto.backend.dto.RevendaResponseDTO;
import com.mobiauto.backend.mapper.RevendaMapper;
import com.mobiauto.backend.model.Cargo;
import com.mobiauto.backend.model.Revenda;
import com.mobiauto.backend.model.Usuario;
import com.mobiauto.backend.repository.RevendaRepository;
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
class RevendaServiceTest {

    @Mock
    private RevendaRepository revendaRepository;

    @Mock
    private RevendaMapper revendaMapper;

    @InjectMocks
    private RevendaService revendaService;

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
    void findAll_DeveRetornarTodasRevendas_QuandoUsuarioAdmin() {
        mockSecurityContext(usuarioAdmin);
        Revenda revenda = new Revenda();
        when(revendaRepository.findAll()).thenReturn(List.of(revenda));
        RevendaResponseDTO dto = new RevendaResponseDTO(1L, "35882339000143", "Revenda A");
        when(revendaMapper.toResponseDTO(revenda)).thenReturn(dto);

        List<RevendaResponseDTO> result = revendaService.findAll();

        assertEquals(1, result.size());
        assertEquals(dto, result.get(0));
        verify(revendaRepository).findAll();
    }

    @Test
    void findAll_DeveRetornarRevendaDoUsuario_QuandoUsuarioNaoAdmin() {
        mockSecurityContext(usuarioGerente);
        Revenda revenda = new Revenda();
        when(revendaRepository.findById(1L)).thenReturn(Optional.of(revenda));
        RevendaResponseDTO dto = new RevendaResponseDTO(1L, "35882339000143", "Revenda A");
        when(revendaMapper.toResponseDTO(revenda)).thenReturn(dto);

        List<RevendaResponseDTO> result = revendaService.findAll();

        assertEquals(1, result.size());
        assertEquals(dto, result.get(0));
        verify(revendaRepository).findById(1L);
    }

    @Test
    void findById_DeveRetornarRevenda_QuandoUsuarioTemAcesso() {
        mockSecurityContext(usuarioGerente);
        Revenda revenda = new Revenda();
        revenda.setId(1L);
        when(revendaRepository.findById(1L)).thenReturn(Optional.of(revenda));
        RevendaResponseDTO dto = new RevendaResponseDTO(1L, "35882339000143", "Revenda A");
        when(revendaMapper.toResponseDTO(revenda)).thenReturn(dto);

        RevendaResponseDTO result = revendaService.findById(1L);

        assertEquals(dto, result);
        verify(revendaRepository).findById(1L);
    }

    @Test
    void findById_DeveLancarForbidden_QuandoUsuarioSemAcesso() {
        mockSecurityContext(usuarioGerente);
        Revenda revenda = new Revenda();
        revenda.setId(2L);
        when(revendaRepository.findById(2L)).thenReturn(Optional.of(revenda));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> revendaService.findById(2L));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void save_DeveSalvarRevenda_QuandoUsuarioAdmin() {
        mockSecurityContext(usuarioAdmin);
        RevendaRequestDTO dto = new RevendaRequestDTO("35882339000143", "Revenda A");
        when(revendaRepository.existsByCnpj(dto.getCnpj())).thenReturn(false);
        Revenda revenda = new Revenda();
        when(revendaRepository.save(any(Revenda.class))).thenReturn(revenda);
        RevendaResponseDTO responseDTO = new RevendaResponseDTO(1L, "35882339000143", "Revenda A");
        when(revendaMapper.toResponseDTO(revenda)).thenReturn(responseDTO);

        RevendaResponseDTO result = revendaService.save(dto);

        assertEquals(responseDTO, result);
        verify(revendaRepository).save(any(Revenda.class));
    }

    @Test
    void save_DeveLancarForbidden_QuandoUsuarioNaoAdmin() {
        mockSecurityContext(usuarioGerente);
        RevendaRequestDTO dto = new RevendaRequestDTO("35882339000143", "Revenda A");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> revendaService.save(dto));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void save_DeveLancarBadRequest_QuandoCnpjDuplicado() {
        mockSecurityContext(usuarioAdmin);
        RevendaRequestDTO dto = new RevendaRequestDTO("35882339000143", "Revenda A");
        when(revendaRepository.existsByCnpj(dto.getCnpj())).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> revendaService.save(dto));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void update_DeveAtualizarRevenda_QuandoUsuarioAdmin() {
        mockSecurityContext(usuarioAdmin);
        RevendaRequestDTO dto = new RevendaRequestDTO("35882339000143", "Revenda Atualizada");
        Revenda revenda = new Revenda();
        revenda.setCnpj("63410664000149");
        when(revendaRepository.findById(1L)).thenReturn(Optional.of(revenda));
        when(revendaRepository.existsByCnpj(dto.getCnpj())).thenReturn(false);
        when(revendaRepository.save(any(Revenda.class))).thenReturn(revenda);
        RevendaResponseDTO responseDTO = new RevendaResponseDTO(1L, "35882339000143", "Revenda Atualizada");
        when(revendaMapper.toResponseDTO(revenda)).thenReturn(responseDTO);

        RevendaResponseDTO result = revendaService.update(1L, dto);

        assertEquals(responseDTO, result);
        verify(revendaRepository).save(any(Revenda.class));
    }

    @Test
    void update_DeveLancarForbidden_QuandoUsuarioNaoAdmin() {
        mockSecurityContext(usuarioGerente);
        RevendaRequestDTO dto = new RevendaRequestDTO("35882339000143", "Revenda Atualizada");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> revendaService.update(1L, dto));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void update_DeveLancarBadRequest_QuandoCnpjDuplicado() {
        mockSecurityContext(usuarioAdmin);
        RevendaRequestDTO dto = new RevendaRequestDTO("35882339000143", "Revenda Atualizada");
        Revenda revenda = new Revenda();
        revenda.setCnpj("63410664000149");
        when(revendaRepository.findById(1L)).thenReturn(Optional.of(revenda));
        when(revendaRepository.existsByCnpj(dto.getCnpj())).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> revendaService.update(1L, dto));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void delete_DeveDeletarRevenda_QuandoUsuarioAdmin() {
        mockSecurityContext(usuarioAdmin);
        Revenda revenda = new Revenda();
        when(revendaRepository.findById(1L)).thenReturn(Optional.of(revenda));

        revendaService.delete(1L);

        verify(revendaRepository).deleteById(1L);
    }

    @Test
    void delete_DeveLancarForbidden_QuandoUsuarioNaoAdmin() {
        mockSecurityContext(usuarioGerente);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> revendaService.delete(1L));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }
}