package com.mobiauto.backend.service;

import com.mobiauto.backend.dto.VeiculoRequestDTO;
import com.mobiauto.backend.dto.VeiculoResponseDTO;
import com.mobiauto.backend.mapper.VeiculoMapper;
import com.mobiauto.backend.model.Cargo;
import com.mobiauto.backend.model.Revenda;
import com.mobiauto.backend.model.Veiculo;
import com.mobiauto.backend.repository.RevendaRepository;
import com.mobiauto.backend.repository.VeiculoRepository;
import com.mobiauto.backend.utils.JwtAuthUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static com.mobiauto.backend.model.Cargo.ADMINISTRADOR;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.*;

@ExtendWith(MockitoExtension.class)
class VeiculoServiceTest {

    @Mock
    private VeiculoRepository veiculoRepository;

    @Mock
    private RevendaRepository revendaRepository;

    @Mock
    private VeiculoMapper veiculoMapper;

    @Mock
    private Jwt jwt;

    @InjectMocks
    private VeiculoService veiculoService;

    private Veiculo veiculo;
    private Revenda revenda;
    private VeiculoRequestDTO veiculoRequestDTO;
    private VeiculoResponseDTO veiculoResponseDTO;
    private static final Long VEICULO_ID = 1L;
    private static final Long REVENDA_ID = 1L;
    private static final Long OUTRA_REVENDA_ID = 2L;
    private static final String MARCA = "Toyota";
    private static final String MODELO = "Corolla";
    private static final String VERSAO = "XLE";
    private static final int ANO_MODELO = 2023;
    private static final List<Cargo> CARGOS_ADMIN = List.of(ADMINISTRADOR);
    private static final List<Cargo> CARGOS_NAO_ADMIN = List.of(Cargo.ASSISTENTE);

    private MockedStatic<JwtAuthUtil> jwtAuthUtilMockedStatic;

    @BeforeEach
    void setUp() {
        revenda = new Revenda();
        revenda.setId(REVENDA_ID);

        veiculo = new Veiculo();
        veiculo.setId(VEICULO_ID);
        veiculo.setMarca(MARCA);
        veiculo.setModelo(MODELO);
        veiculo.setVersao(VERSAO);
        veiculo.setAnoModelo(ANO_MODELO);
        veiculo.setRevenda(revenda);

        veiculoRequestDTO = new VeiculoRequestDTO(MARCA, MODELO, VERSAO, ANO_MODELO, REVENDA_ID);

        veiculoResponseDTO = new VeiculoResponseDTO(VEICULO_ID, MARCA, MODELO, VERSAO, ANO_MODELO, REVENDA_ID);

        jwtAuthUtilMockedStatic = mockStatic(JwtAuthUtil.class);
    }

    @AfterEach
    void tearDown() {
        jwtAuthUtilMockedStatic.close();
    }

    @Test
    void buscarTodos_Admin_RetornaTodosVeiculos() {
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(veiculoRepository.findAll()).thenReturn(List.of(veiculo));
        when(veiculoMapper.toResponseDTO(veiculo)).thenReturn(veiculoResponseDTO);

        List<VeiculoResponseDTO> result = veiculoService.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(veiculoResponseDTO, result.get(0));
        verify(veiculoRepository).findAll();
        verify(veiculoMapper).toResponseDTO(veiculo);
        verifyNoMoreInteractions(veiculoRepository, veiculoMapper);
        verifyNoInteractions(revendaRepository);
    }

    @Test
    void buscarTodos_NaoAdmin_RetornaVeiculosDaRevenda() {
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_NAO_ADMIN);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(veiculoRepository.findAllByRevenda_Id(REVENDA_ID)).thenReturn(List.of(veiculo));
        when(veiculoMapper.toResponseDTO(veiculo)).thenReturn(veiculoResponseDTO);

        List<VeiculoResponseDTO> result = veiculoService.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(veiculoResponseDTO, result.get(0));
        verify(veiculoRepository).findAllByRevenda_Id(REVENDA_ID);
        verify(veiculoMapper).toResponseDTO(veiculo);
        verifyNoMoreInteractions(veiculoRepository, veiculoMapper);
        verifyNoInteractions(revendaRepository);
    }

    @Test
    void buscarPorId_Admin_Sucesso() {
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(veiculoRepository.findById(VEICULO_ID)).thenReturn(Optional.of(veiculo));
        when(veiculoMapper.toResponseDTO(veiculo)).thenReturn(veiculoResponseDTO);

        VeiculoResponseDTO result = veiculoService.findById(VEICULO_ID);

        assertNotNull(result);
        assertEquals(veiculoResponseDTO, result);
        verify(veiculoRepository).findById(VEICULO_ID);
        verify(veiculoMapper).toResponseDTO(veiculo);
        verifyNoMoreInteractions(veiculoRepository, veiculoMapper);
        verifyNoInteractions(revendaRepository);
    }
    @Test
    void buscarPorId_NaoAdmin_MesmaRevenda_Sucesso() {
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_NAO_ADMIN);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(veiculoRepository.findById(VEICULO_ID)).thenReturn(Optional.of(veiculo));
        when(veiculoMapper.toResponseDTO(veiculo)).thenReturn(veiculoResponseDTO);

        VeiculoResponseDTO result = veiculoService.findById(VEICULO_ID);

        assertNotNull(result);
        assertEquals(veiculoResponseDTO, result);
        verify(veiculoRepository).findById(VEICULO_ID);
        verify(veiculoMapper).toResponseDTO(veiculo);
        verifyNoMoreInteractions(veiculoRepository, veiculoMapper);
        verifyNoInteractions(revendaRepository);
    }

    @Test
    void buscarPorId_NaoAdmin_RevendaDiferente_LancaForbidden() {
        Revenda outraRevenda = new Revenda();
        outraRevenda.setId(OUTRA_REVENDA_ID);
        veiculo.setRevenda(outraRevenda);
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_NAO_ADMIN);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(veiculoRepository.findById(VEICULO_ID)).thenReturn(Optional.of(veiculo));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            veiculoService.findById(VEICULO_ID);
        });
        assertEquals(FORBIDDEN, exception.getStatusCode());
        assertEquals("Você só pode acessar veículos da sua revenda", exception.getReason());
        verify(veiculoRepository).findById(VEICULO_ID);
        verifyNoMoreInteractions(veiculoRepository);
        verifyNoInteractions(veiculoMapper, revendaRepository);
    }

    @Test
    void buscarPorId_VeiculoNaoEncontrado_LancaNotFound() {
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(veiculoRepository.findById(VEICULO_ID)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            veiculoService.findById(VEICULO_ID);
        });
        assertEquals(NOT_FOUND, exception.getStatusCode());
        assertEquals("Veículo não encontrado", exception.getReason());
        verify(veiculoRepository).findById(VEICULO_ID);
        verifyNoMoreInteractions(veiculoRepository);
        verifyNoInteractions(veiculoMapper, revendaRepository);
    }

    @Test
    void salvar_Admin_Sucesso() {
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(revendaRepository.findById(REVENDA_ID)).thenReturn(Optional.of(revenda));
        when(veiculoRepository.save(any(Veiculo.class))).thenReturn(veiculo);
        when(veiculoMapper.toResponseDTO(veiculo)).thenReturn(veiculoResponseDTO);

        VeiculoResponseDTO result = veiculoService.save(veiculoRequestDTO);

        assertNotNull(result);
        assertEquals(veiculoResponseDTO, result);
        verify(revendaRepository).findById(REVENDA_ID);
        verify(veiculoRepository).save(any(Veiculo.class));
        verify(veiculoMapper).toResponseDTO(veiculo);
        verifyNoMoreInteractions(veiculoRepository, revendaRepository, veiculoMapper);
    }

    @Test
    void salvar_NaoAdmin_MesmaRevenda_Sucesso() {
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_NAO_ADMIN);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(revendaRepository.findById(REVENDA_ID)).thenReturn(Optional.of(revenda));
        when(veiculoRepository.save(any(Veiculo.class))).thenReturn(veiculo);
        when(veiculoMapper.toResponseDTO(veiculo)).thenReturn(veiculoResponseDTO);

        VeiculoResponseDTO result = veiculoService.save(veiculoRequestDTO);

        assertNotNull(result);
        assertEquals(veiculoResponseDTO, result);
        verify(revendaRepository).findById(REVENDA_ID);
        verify(veiculoRepository).save(any(Veiculo.class));
        verify(veiculoMapper).toResponseDTO(veiculo);
        verifyNoMoreInteractions(veiculoRepository, revendaRepository, veiculoMapper);
    }

    @Test
    void salvar_NaoAdmin_RevendaDiferente_LancaForbidden() {
        VeiculoRequestDTO dtoOutraRevenda = new VeiculoRequestDTO(MARCA, MODELO, VERSAO, ANO_MODELO, OUTRA_REVENDA_ID);
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_NAO_ADMIN);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            veiculoService.save(dtoOutraRevenda);
        });
        assertEquals(FORBIDDEN, exception.getStatusCode());
        assertEquals("Você só pode criar veículos na sua revenda", exception.getReason());
        verifyNoInteractions(veiculoRepository, revendaRepository, veiculoMapper);
    }

    @Test
    void salvar_RevendaNaoEncontrada_LancaNotFound() {
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(revendaRepository.findById(REVENDA_ID)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            veiculoService.save(veiculoRequestDTO);
        });
        assertEquals(NOT_FOUND, exception.getStatusCode());
        assertEquals("Revenda não encontrada: " + REVENDA_ID, exception.getReason());
        verify(revendaRepository).findById(REVENDA_ID);
        verifyNoMoreInteractions(revendaRepository);
        verifyNoInteractions(veiculoRepository, veiculoMapper);
    }

    @Test
    void atualizar_Admin_Sucesso() {
        VeiculoRequestDTO novoDto = new VeiculoRequestDTO("Honda", "Civic", "Touring", 2024, REVENDA_ID);
        VeiculoResponseDTO novoResponseDTO = new VeiculoResponseDTO(VEICULO_ID, "Honda", "Civic", "Touring", 2024, REVENDA_ID);
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(veiculoRepository.findById(VEICULO_ID)).thenReturn(Optional.of(veiculo));
        when(revendaRepository.findById(REVENDA_ID)).thenReturn(Optional.of(revenda));
        when(veiculoRepository.save(veiculo)).thenReturn(veiculo);
        when(veiculoMapper.toResponseDTO(veiculo)).thenReturn(novoResponseDTO);

        VeiculoResponseDTO result = veiculoService.update(VEICULO_ID, novoDto);

        assertNotNull(result);
        assertEquals(novoResponseDTO, result);
        verify(veiculoRepository).findById(VEICULO_ID);
        verify(revendaRepository).findById(REVENDA_ID);
        verify(veiculoRepository).save(veiculo);
        verify(veiculoMapper).toResponseDTO(veiculo);
        verifyNoMoreInteractions(veiculoRepository, revendaRepository, veiculoMapper);
    }

    @Test
    void atualizar_NaoAdmin_MesmaRevenda_Sucesso() {
        VeiculoRequestDTO novoDto = new VeiculoRequestDTO("Honda", "Civic", "Touring", 2024, REVENDA_ID);
        VeiculoResponseDTO novoResponseDTO = new VeiculoResponseDTO(VEICULO_ID, "Honda", "Civic", "Touring", 2024, REVENDA_ID);
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_NAO_ADMIN);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(veiculoRepository.findById(VEICULO_ID)).thenReturn(Optional.of(veiculo));
        when(revendaRepository.findById(REVENDA_ID)).thenReturn(Optional.of(revenda));
        when(veiculoRepository.save(veiculo)).thenReturn(veiculo);
        when(veiculoMapper.toResponseDTO(veiculo)).thenReturn(novoResponseDTO);

        VeiculoResponseDTO result = veiculoService.update(VEICULO_ID, novoDto);

        assertNotNull(result);
        assertEquals(novoResponseDTO, result);
        verify(veiculoRepository).findById(VEICULO_ID);
        verify(revendaRepository).findById(REVENDA_ID);
        verify(veiculoRepository).save(veiculo);
        verify(veiculoMapper).toResponseDTO(veiculo);
        verifyNoMoreInteractions(veiculoRepository, revendaRepository, veiculoMapper);
    }

    @Test
    void atualizar_NaoAdmin_RevendaDiferente_LancaForbidden() {
        Revenda outraRevenda = new Revenda();
        outraRevenda.setId(OUTRA_REVENDA_ID);
        veiculo.setRevenda(outraRevenda);
        VeiculoRequestDTO novoDto = new VeiculoRequestDTO("Honda", "Civic", "Touring", 2024, REVENDA_ID);
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_NAO_ADMIN);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(veiculoRepository.findById(VEICULO_ID)).thenReturn(Optional.of(veiculo));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            veiculoService.update(VEICULO_ID, novoDto);
        });
        assertEquals(FORBIDDEN, exception.getStatusCode());
        assertEquals("Você só pode atualizar veículos da sua revenda", exception.getReason());
        verify(veiculoRepository).findById(VEICULO_ID);
        verifyNoMoreInteractions(veiculoRepository);
        verifyNoInteractions(revendaRepository, veiculoMapper);
    }

    @Test
    void atualizar_VeiculoNaoEncontrado_LancaNotFound() {
        VeiculoRequestDTO novoDto = new VeiculoRequestDTO("Honda", "Civic", "Touring", 2024, REVENDA_ID);
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(veiculoRepository.findById(VEICULO_ID)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            veiculoService.update(VEICULO_ID, novoDto);
        });
        assertEquals(NOT_FOUND, exception.getStatusCode());
        assertEquals("Veículo não encontrado", exception.getReason());
        verify(veiculoRepository).findById(VEICULO_ID);
        verifyNoMoreInteractions(veiculoRepository);
        verifyNoInteractions(revendaRepository, veiculoMapper);
    }

    @Test
    void atualizar_RevendaNaoEncontrada_LancaNotFound() {
        VeiculoRequestDTO novoDto = new VeiculoRequestDTO("Honda", "Civic", "Touring", 2024, REVENDA_ID);
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(veiculoRepository.findById(VEICULO_ID)).thenReturn(Optional.of(veiculo));
        when(revendaRepository.findById(REVENDA_ID)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            veiculoService.update(VEICULO_ID, novoDto);
        });
        assertEquals(NOT_FOUND, exception.getStatusCode());
        assertEquals("Revenda não encontrada: " + REVENDA_ID, exception.getReason());
        verify(veiculoRepository).findById(VEICULO_ID);
        verify(revendaRepository).findById(REVENDA_ID);
        verifyNoMoreInteractions(veiculoRepository, revendaRepository);
        verifyNoInteractions(veiculoMapper);
    }

    @Test
    void deletar_Admin_Sucesso() {
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(veiculoRepository.findById(VEICULO_ID)).thenReturn(Optional.of(veiculo));

        veiculoService.delete(VEICULO_ID);

        verify(veiculoRepository).findById(VEICULO_ID);
        verify(veiculoRepository).deleteById(VEICULO_ID);
        verifyNoMoreInteractions(veiculoRepository);
        verifyNoInteractions(revendaRepository, veiculoMapper);
    }

    @Test
    void deletar_NaoAdmin_MesmaRevenda_Sucesso() {
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_NAO_ADMIN);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(veiculoRepository.findById(VEICULO_ID)).thenReturn(Optional.of(veiculo));

        veiculoService.delete(VEICULO_ID);

        verify(veiculoRepository).findById(VEICULO_ID);
        verify(veiculoRepository).deleteById(VEICULO_ID);
        verifyNoMoreInteractions(veiculoRepository);
        verifyNoInteractions(revendaRepository, veiculoMapper);
    }

    @Test
    void deletar_NaoAdmin_RevendaDiferente_LancaForbidden() {
        Revenda outraRevenda = new Revenda();
        outraRevenda.setId(OUTRA_REVENDA_ID);
        veiculo.setRevenda(outraRevenda);
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_NAO_ADMIN);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(veiculoRepository.findById(VEICULO_ID)).thenReturn(Optional.of(veiculo));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            veiculoService.delete(VEICULO_ID);
        });
        assertEquals(FORBIDDEN, exception.getStatusCode());
        assertEquals("Você só pode excluir veículos da sua revenda", exception.getReason());
        verify(veiculoRepository).findById(VEICULO_ID);
        verifyNoMoreInteractions(veiculoRepository);
        verifyNoInteractions(revendaRepository, veiculoMapper);
    }

    @Test
    void deletar_VeiculoNaoEncontrado_LancaNotFound() {
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(veiculoRepository.findById(VEICULO_ID)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            veiculoService.delete(VEICULO_ID);
        });
        assertEquals(NOT_FOUND, exception.getStatusCode());
        assertEquals("Veículo não encontrado", exception.getReason());
        verify(veiculoRepository).findById(VEICULO_ID);
        verifyNoMoreInteractions(veiculoRepository);
        verifyNoInteractions(revendaRepository, veiculoMapper);
    }
}