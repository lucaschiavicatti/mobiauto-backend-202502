package com.mobiauto.backend.service;

import com.mobiauto.backend.dto.RevendaRequestDTO;
import com.mobiauto.backend.dto.RevendaResponseDTO;
import com.mobiauto.backend.mapper.RevendaMapper;
import com.mobiauto.backend.model.Cargo;
import com.mobiauto.backend.model.Revenda;
import com.mobiauto.backend.repository.RevendaRepository;
import com.mobiauto.backend.utils.JwtAuthUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static com.mobiauto.backend.model.Cargo.ADMINISTRADOR;
import static com.mobiauto.backend.model.Cargo.ASSISTENTE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RevendaServiceTest {

    @Mock
    private RevendaRepository revendaRepository;

    @Mock
    private RevendaMapper revendaMapper;

    @Mock
    private Jwt jwt;

    @InjectMocks
    private RevendaService revendaService;

    private Revenda revenda;
    private RevendaRequestDTO revendaRequestDTO;
    private RevendaResponseDTO revendaResponseDTO;
    private static final Long REVENDA_ID = 1L;
    private static final Long OUTRA_REVENDA_ID = 2L;
    private static final String CNPJ = "12345678000195";
    private static final String NOME_SOCIAL = "Revenda Teste";
    private static final String OUTRO_CNPJ = "94794851000105";
    private static final List<Cargo> CARGOS_ADMIN = List.of(ADMINISTRADOR);
    private static final List<Cargo> CARGOS_ASSISTENTE = List.of(ASSISTENTE);

    private MockedStatic<JwtAuthUtil> jwtAuthUtilMockedStatic;

    @BeforeEach
    void setUp() {
        revenda = new Revenda();
        revenda.setId(REVENDA_ID);
        revenda.setCnpj(CNPJ);
        revenda.setNomeSocial(NOME_SOCIAL);

        revendaRequestDTO = new RevendaRequestDTO(CNPJ, NOME_SOCIAL);

        revendaResponseDTO = new RevendaResponseDTO(REVENDA_ID, CNPJ, NOME_SOCIAL);

        jwtAuthUtilMockedStatic = mockStatic(JwtAuthUtil.class);
    }

    @AfterEach
    void tearDown() {
        jwtAuthUtilMockedStatic.close();
    }

    @Test
    void buscarTodos_Admin_RetornaTodasRevendas() {
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(revendaRepository.findAll()).thenReturn(List.of(revenda));
        when(revendaMapper.toResponseDTO(revenda)).thenReturn(revendaResponseDTO);

        List<RevendaResponseDTO> result = revendaService.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(revendaResponseDTO, result.get(0));
        verify(revendaRepository).findAll();
        verify(revendaMapper).toResponseDTO(revenda);
        verifyNoMoreInteractions(revendaRepository, revendaMapper);
    }

    @Test
    void buscarTodos_NaoAdmin_RetornaRevendaDoUsuario() {
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ASSISTENTE);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(revendaRepository.findById(REVENDA_ID)).thenReturn(Optional.of(revenda));
        when(revendaMapper.toResponseDTO(revenda)).thenReturn(revendaResponseDTO);

        List<RevendaResponseDTO> result = revendaService.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(revendaResponseDTO, result.get(0));
        verify(revendaRepository).findById(REVENDA_ID);
        verify(revendaMapper).toResponseDTO(revenda);
        verifyNoMoreInteractions(revendaRepository, revendaMapper);
    }

    @Test
    void buscarTodos_NaoAdmin_RevendaNaoEncontrada_LancaNotFound() {
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ASSISTENTE);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(revendaRepository.findById(REVENDA_ID)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            revendaService.findAll();
        });
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Revenda não encontrada", exception.getReason());
        verify(revendaRepository).findById(REVENDA_ID);
        verifyNoInteractions(revendaMapper);
    }

    @Test
    void buscarPorId_Admin_Sucesso() {
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(revendaRepository.findById(REVENDA_ID)).thenReturn(Optional.of(revenda));
        when(revendaMapper.toResponseDTO(revenda)).thenReturn(revendaResponseDTO);

        RevendaResponseDTO result = revendaService.findById(REVENDA_ID);

        assertNotNull(result);
        assertEquals(revendaResponseDTO, result);
        verify(revendaRepository).findById(REVENDA_ID);
        verify(revendaMapper).toResponseDTO(revenda);
        verifyNoMoreInteractions(revendaRepository, revendaMapper);
    }

    @Test
    void buscarPorId_NaoAdmin_MesmaRevenda_Sucesso() {
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ASSISTENTE);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(revendaRepository.findById(REVENDA_ID)).thenReturn(Optional.of(revenda));
        when(revendaMapper.toResponseDTO(revenda)).thenReturn(revendaResponseDTO);

        RevendaResponseDTO result = revendaService.findById(REVENDA_ID);

        assertNotNull(result);
        assertEquals(revendaResponseDTO, result);
        verify(revendaRepository).findById(REVENDA_ID);
        verify(revendaMapper).toResponseDTO(revenda);
        verifyNoMoreInteractions(revendaRepository, revendaMapper);
    }

    @Test
    void buscarPorId_NaoAdmin_RevendaDiferente_LancaForbidden() {
        Revenda outraRevenda = new Revenda();
        outraRevenda.setId(OUTRA_REVENDA_ID);
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ASSISTENTE);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(revendaRepository.findById(OUTRA_REVENDA_ID)).thenReturn(Optional.of(outraRevenda));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            revendaService.findById(OUTRA_REVENDA_ID);
        });
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("Você só pode acessar sua própria revenda", exception.getReason());
        verify(revendaRepository).findById(OUTRA_REVENDA_ID);
        verifyNoInteractions(revendaMapper);
    }

    @Test
    void buscarPorId_NaoEncontrado_LancaNotFound() {
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(revendaRepository.findById(REVENDA_ID)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            revendaService.findById(REVENDA_ID);
        });
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Revenda não encontrada", exception.getReason());
        verify(revendaRepository).findById(REVENDA_ID);
        verifyNoInteractions(revendaMapper);
    }

    @Test
    void salvar_Admin_Sucesso() {
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(revendaRepository.existsByCnpj(CNPJ)).thenReturn(false);
        when(revendaRepository.save(any(Revenda.class))).thenReturn(revenda);
        when(revendaMapper.toResponseDTO(revenda)).thenReturn(revendaResponseDTO);

        RevendaResponseDTO result = revendaService.save(revendaRequestDTO);

        assertNotNull(result);
        assertEquals(revendaResponseDTO, result);
        verify(revendaRepository).existsByCnpj(CNPJ);
        verify(revendaRepository).save(any(Revenda.class));
        verify(revendaMapper).toResponseDTO(revenda);
        verifyNoMoreInteractions(revendaRepository, revendaMapper);
    }

    @Test
    void salvar_NaoAdmin_LancaForbidden() {
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ASSISTENTE);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            revendaService.save(revendaRequestDTO);
        });
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("Apenas administradores podem criar revendas", exception.getReason());
        verifyNoInteractions(revendaRepository, revendaMapper);
    }

    @Test
    void salvar_CnpjJaCadastrado_LancaBadRequest() {
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(revendaRepository.existsByCnpj(CNPJ)).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            revendaService.save(revendaRequestDTO);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("CNPJ já cadastrado", exception.getReason());
        verify(revendaRepository).existsByCnpj(CNPJ);
        verifyNoMoreInteractions(revendaRepository);
        verifyNoInteractions(revendaMapper);
    }

    @Test
    void atualizar_Admin_Sucesso() {
        RevendaRequestDTO novoDto = new RevendaRequestDTO(OUTRO_CNPJ, "Novo Nome");
        RevendaResponseDTO novoResponseDTO = new RevendaResponseDTO(REVENDA_ID, OUTRO_CNPJ, "Novo Nome");
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(revendaRepository.findById(REVENDA_ID)).thenReturn(Optional.of(revenda));
        when(revendaRepository.existsByCnpj(OUTRO_CNPJ)).thenReturn(false);
        when(revendaRepository.save(revenda)).thenReturn(revenda);
        when(revendaMapper.toResponseDTO(revenda)).thenReturn(novoResponseDTO);

        RevendaResponseDTO result = revendaService.update(REVENDA_ID, novoDto);

        assertNotNull(result);
        assertEquals(novoResponseDTO, result);
        verify(revendaRepository).findById(REVENDA_ID);
        verify(revendaRepository).existsByCnpj(OUTRO_CNPJ);
        verify(revendaRepository).save(revenda);
        verify(revendaMapper).toResponseDTO(revenda);
        verifyNoMoreInteractions(revendaRepository, revendaMapper);
    }

    @Test
    void atualizar_NaoAdmin_LancaForbidden() {
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ASSISTENTE);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            revendaService.update(REVENDA_ID, revendaRequestDTO);
        });
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("Apenas administradores podem atualizar revendas", exception.getReason());
        verifyNoInteractions(revendaRepository, revendaMapper);
    }

    @Test
    void atualizar_RevendaNaoEncontrada_LancaNotFound() {
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(revendaRepository.findById(REVENDA_ID)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            revendaService.update(REVENDA_ID, revendaRequestDTO);
        });
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Revenda não encontrada", exception.getReason());
        verify(revendaRepository).findById(REVENDA_ID);
        verifyNoMoreInteractions(revendaRepository);
        verifyNoInteractions(revendaMapper);
    }

    @Test
    void atualizar_CnpjJaCadastrado_LancaBadRequest() {
        RevendaRequestDTO novoDto = new RevendaRequestDTO(OUTRO_CNPJ, "Novo Nome");
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(revendaRepository.findById(REVENDA_ID)).thenReturn(Optional.of(revenda));
        when(revendaRepository.existsByCnpj(OUTRO_CNPJ)).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            revendaService.update(REVENDA_ID, novoDto);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("CNPJ já cadastrado", exception.getReason());
        verify(revendaRepository).findById(REVENDA_ID);
        verify(revendaRepository).existsByCnpj(OUTRO_CNPJ);
        verifyNoMoreInteractions(revendaRepository);
        verifyNoInteractions(revendaMapper);
    }

    @Test
    void deletar_Admin_Sucesso() {
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(revendaRepository.findById(REVENDA_ID)).thenReturn(Optional.of(revenda));

        revendaService.delete(REVENDA_ID);

        verify(revendaRepository).findById(REVENDA_ID);
        verify(revendaRepository).deleteById(REVENDA_ID);
        verifyNoMoreInteractions(revendaRepository);
        verifyNoInteractions(revendaMapper);
    }

    @Test
    void deletar_NaoAdmin_LancaForbidden() {
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ASSISTENTE);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            revendaService.delete(REVENDA_ID);
        });
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("Apenas administradores podem excluir revendas", exception.getReason());
        verifyNoInteractions(revendaRepository, revendaMapper);
    }

    @Test
    void deletar_RevendaNaoEncontrada_LancaNotFound() {
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(revendaRepository.findById(REVENDA_ID)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            revendaService.delete(REVENDA_ID);
        });
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Revenda não encontrada", exception.getReason());
        verify(revendaRepository).findById(REVENDA_ID);
        verifyNoMoreInteractions(revendaRepository);
        verifyNoInteractions(revendaMapper);
    }
}