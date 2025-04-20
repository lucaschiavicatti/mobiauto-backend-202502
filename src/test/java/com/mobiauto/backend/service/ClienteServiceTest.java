package com.mobiauto.backend.service;

import com.mobiauto.backend.dto.ClienteRequestDTO;
import com.mobiauto.backend.dto.ClienteResponseDTO;
import com.mobiauto.backend.mapper.ClienteMapper;
import com.mobiauto.backend.model.Cargo;
import com.mobiauto.backend.model.Cliente;
import com.mobiauto.backend.model.Revenda;
import com.mobiauto.backend.repository.ClienteRepository;
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
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private RevendaRepository revendaRepository;

    @Mock
    private ClienteMapper clienteMapper;

    @Mock
    private Jwt jwt;

    @InjectMocks
    private ClienteService clienteService;

    private Cliente cliente;
    private Revenda revenda;
    private ClienteRequestDTO clienteRequestDTO;
    private ClienteResponseDTO clienteResponseDTO;
    private static final Long CLIENTE_ID = 1L;
    private static final Long REVENDA_ID = 1L;
    private static final Long OUTRA_REVENDA_ID = 2L;
    private static final String EMAIL = "cliente@example.com";
    private static final String NOME = "Cliente Teste";
    private static final String TELEFONE = "123456789";
    private static final List<Cargo> CARGOS_ADMIN = List.of(Cargo.ADMINISTRADOR);
    private static final List<Cargo> CARGOS_GERENTE = List.of(Cargo.GERENTE);

    private MockedStatic<JwtAuthUtil> jwtAuthUtilMockedStatic;

    @BeforeEach
    void setUp() {
        revenda = new Revenda();
        revenda.setId(REVENDA_ID);

        cliente = new Cliente();
        cliente.setId(CLIENTE_ID);
        cliente.setNome(NOME);
        cliente.setEmail(EMAIL);
        cliente.setTelefone(TELEFONE);
        cliente.setRevenda(revenda);

        clienteRequestDTO = new ClienteRequestDTO(NOME, EMAIL, TELEFONE, REVENDA_ID);
        clienteResponseDTO = new ClienteResponseDTO(CLIENTE_ID, NOME, EMAIL, TELEFONE, REVENDA_ID);

        // Inicializa o mock estático
        jwtAuthUtilMockedStatic = mockStatic(JwtAuthUtil.class);
    }

    @AfterEach
    void tearDown() {
        // Fecha o mock estático para evitar vazamentos
        jwtAuthUtilMockedStatic.close();
    }

    @Test
    void buscarTodos_Admin_RetornaTodosClientes() {
        
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(clienteRepository.findAll()).thenReturn(List.of(cliente));
        when(clienteMapper.toResponseDTO(cliente)).thenReturn(clienteResponseDTO);
        
        List<ClienteResponseDTO> result = clienteService.findAll();
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(clienteResponseDTO, result.get(0));
        verify(clienteRepository).findAll();
        verify(clienteMapper).toResponseDTO(cliente);
        verifyNoMoreInteractions(clienteRepository);
    }

    @Test
    void buscarTodos_NaoAdmin_RetornaClientesPorRevenda() {
        
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_GERENTE);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(clienteRepository.findAllByRevenda_Id(REVENDA_ID)).thenReturn(List.of(cliente));
        when(clienteMapper.toResponseDTO(cliente)).thenReturn(clienteResponseDTO);
        
        List<ClienteResponseDTO> result = clienteService.findAll();
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(clienteResponseDTO, result.get(0));
        verify(clienteRepository).findAllByRevenda_Id(REVENDA_ID);
        verify(clienteMapper).toResponseDTO(cliente);
        verifyNoMoreInteractions(clienteRepository);
    }

    @Test
    void buscarPorId_Admin_Sucesso() {
        
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(clienteRepository.findById(CLIENTE_ID)).thenReturn(Optional.of(cliente));
        when(clienteMapper.toResponseDTO(cliente)).thenReturn(clienteResponseDTO);

        ClienteResponseDTO result = clienteService.findById(CLIENTE_ID);
        
        assertNotNull(result);
        assertEquals(clienteResponseDTO, result);
        verify(clienteRepository).findById(CLIENTE_ID);
        verify(clienteMapper).toResponseDTO(cliente);
    }

    @Test
    void buscarPorId_NaoAdmin_MesmaRevenda_Sucesso() {
        
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_GERENTE);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(clienteRepository.findById(CLIENTE_ID)).thenReturn(Optional.of(cliente));
        when(clienteMapper.toResponseDTO(cliente)).thenReturn(clienteResponseDTO);

        
        ClienteResponseDTO result = clienteService.findById(CLIENTE_ID);

        
        assertNotNull(result);
        assertEquals(clienteResponseDTO, result);
        verify(clienteRepository).findById(CLIENTE_ID);
        verify(clienteMapper).toResponseDTO(cliente);
    }

    @Test
    void buscarPorId_NaoAdmin_RevendaDiferente_LancaForbidden() {
        
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_GERENTE);
        when(jwt.getClaimAsString("revendaId")).thenReturn(OUTRA_REVENDA_ID.toString());
        when(clienteRepository.findById(CLIENTE_ID)).thenReturn(Optional.of(cliente));
         
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            clienteService.findById(CLIENTE_ID);
        });
        assertEquals(FORBIDDEN, exception.getStatusCode());
        assertEquals("Você só pode gerenciar clientes da sua revenda", exception.getReason());
        verify(clienteRepository).findById(CLIENTE_ID);
        verifyNoInteractions(clienteMapper);
    }

    @Test
    void buscarPorId_NaoEncontrado_LancaNotFound() {
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(clienteRepository.findById(CLIENTE_ID)).thenReturn(Optional.empty());
         
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            clienteService.findById(CLIENTE_ID);
        });
        assertEquals(NOT_FOUND, exception.getStatusCode());
        assertEquals("Cliente não encontrado", exception.getReason());
        verify(clienteRepository).findById(CLIENTE_ID);
        verifyNoInteractions(clienteMapper);
    }

    @Test
    void salvar_Admin_Sucesso() {
        
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(clienteRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(revendaRepository.findById(REVENDA_ID)).thenReturn(Optional.of(revenda));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);
        when(clienteMapper.toResponseDTO(cliente)).thenReturn(clienteResponseDTO);

        ClienteResponseDTO result = clienteService.save(clienteRequestDTO);
        
        assertNotNull(result);
        assertEquals(clienteResponseDTO, result);
        verify(clienteRepository).existsByEmail(EMAIL);
        verify(revendaRepository).findById(REVENDA_ID);
        verify(clienteRepository).save(any(Cliente.class));
        verify(clienteMapper).toResponseDTO(cliente);
    }

    @Test
    void salvar_NaoAdmin_MesmaRevenda_Sucesso() {
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_GERENTE);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(clienteRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(revendaRepository.findById(REVENDA_ID)).thenReturn(Optional.of(revenda));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);
        when(clienteMapper.toResponseDTO(cliente)).thenReturn(clienteResponseDTO);

        ClienteResponseDTO result = clienteService.save(clienteRequestDTO);
        
        assertNotNull(result);
        assertEquals(clienteResponseDTO, result);
        verify(clienteRepository).existsByEmail(EMAIL);
        verify(revendaRepository).findById(REVENDA_ID);
        verify(clienteRepository).save(any(Cliente.class));
        verify(clienteMapper).toResponseDTO(cliente);
    }

    @Test
    void salvar_NaoAdmin_RevendaDiferente_LancaForbidden() {
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_GERENTE);
        when(jwt.getClaimAsString("revendaId")).thenReturn(OUTRA_REVENDA_ID.toString());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            clienteService.save(clienteRequestDTO);
        });
        assertEquals(FORBIDDEN, exception.getStatusCode());
        assertEquals("Você só pode gerenciar clientes da sua revenda", exception.getReason());
        verifyNoInteractions(clienteRepository, revendaRepository, clienteMapper);
    }

    @Test
    void salvar_EmailExistente_LancaConflict() {
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(clienteRepository.existsByEmail(EMAIL)).thenReturn(true);
         
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            clienteService.save(clienteRequestDTO);
        });
        assertEquals(CONFLICT, exception.getStatusCode());
        assertEquals("Email já cadastrado: " + EMAIL, exception.getReason());
        verify(clienteRepository).existsByEmail(EMAIL);
        verifyNoInteractions(revendaRepository, clienteMapper);
    }

    @Test
    void salvar_RevendaNaoEncontrada_LancaNotFound() {
        
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(clienteRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(revendaRepository.findById(REVENDA_ID)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            clienteService.save(clienteRequestDTO);
        });
        assertEquals(NOT_FOUND, exception.getStatusCode());
        assertEquals("Revenda não encontrada: " + REVENDA_ID, exception.getReason());
        verify(clienteRepository).existsByEmail(EMAIL);
        verify(revendaRepository).findById(REVENDA_ID);
        verifyNoInteractions(clienteMapper);
    }

    @Test
    void atualizar_Admin_Sucesso() {
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(clienteRepository.findById(CLIENTE_ID)).thenReturn(Optional.of(cliente));
        when(revendaRepository.findById(REVENDA_ID)).thenReturn(Optional.of(revenda));
        when(clienteRepository.save(cliente)).thenReturn(cliente);
        when(clienteMapper.toResponseDTO(cliente)).thenReturn(clienteResponseDTO);
        
        ClienteResponseDTO result = clienteService.update(CLIENTE_ID, clienteRequestDTO);
        
        assertNotNull(result);
        assertEquals(clienteResponseDTO, result);
        verify(clienteRepository).findById(CLIENTE_ID);
        verify(revendaRepository).findById(REVENDA_ID);
        verify(clienteRepository).save(cliente);
        verify(clienteMapper).toResponseDTO(cliente);
    }

    @Test
    void atualizar_NaoAdmin_MesmaRevenda_Sucesso() {
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_GERENTE);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(clienteRepository.findById(CLIENTE_ID)).thenReturn(Optional.of(cliente));
        when(revendaRepository.findById(REVENDA_ID)).thenReturn(Optional.of(revenda));
        when(clienteRepository.save(cliente)).thenReturn(cliente);
        when(clienteMapper.toResponseDTO(cliente)).thenReturn(clienteResponseDTO);

        ClienteResponseDTO result = clienteService.update(CLIENTE_ID, clienteRequestDTO);

        assertNotNull(result);
        assertEquals(clienteResponseDTO, result);
        verify(clienteRepository).findById(CLIENTE_ID);
        verify(revendaRepository).findById(REVENDA_ID);
        verify(clienteRepository).save(cliente);
        verify(clienteMapper).toResponseDTO(cliente);
    }

    @Test
    void atualizar_NaoAdmin_RevendaDiferente_LancaForbidden() {
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_GERENTE);
        when(jwt.getClaimAsString("revendaId")).thenReturn(OUTRA_REVENDA_ID.toString());
        when(clienteRepository.findById(CLIENTE_ID)).thenReturn(Optional.of(cliente));
         
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            clienteService.update(CLIENTE_ID, clienteRequestDTO);
        });
        assertEquals(FORBIDDEN, exception.getStatusCode());
        assertEquals("Você só pode gerenciar clientes da sua revenda", exception.getReason());
        verify(clienteRepository).findById(CLIENTE_ID);
        verifyNoMoreInteractions(clienteRepository);
        verifyNoInteractions(revendaRepository, clienteMapper);
    }

    @Test
    void atualizar_EmailExistente_LancaConflict() {
        String novoEmail = "novo@example.com";
        ClienteRequestDTO clienteRequestDTOComNovoEmail = new ClienteRequestDTO(NOME, novoEmail, TELEFONE, REVENDA_ID);

        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(clienteRepository.findById(CLIENTE_ID)).thenReturn(Optional.of(cliente));
        when(clienteRepository.existsByEmail(novoEmail)).thenReturn(true);
         
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            clienteService.update(CLIENTE_ID, clienteRequestDTOComNovoEmail);
        });
        assertEquals(CONFLICT, exception.getStatusCode());
        assertEquals("Email já cadastrado: " + novoEmail, exception.getReason());
        verify(clienteRepository).findById(CLIENTE_ID);
        verify(clienteRepository).existsByEmail(novoEmail);
        verifyNoInteractions(clienteMapper);
        verifyNoInteractions(revendaRepository);
    }

    @Test
    void atualizar_NaoEncontrado_LancaNotFound() {
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        when(clienteRepository.findById(CLIENTE_ID)).thenReturn(Optional.empty());
         
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            clienteService.update(CLIENTE_ID, clienteRequestDTO);
        });
        assertEquals(NOT_FOUND, exception.getStatusCode());
        assertEquals("Cliente não encontrado", exception.getReason());
        verify(clienteRepository).findById(CLIENTE_ID);
        verifyNoInteractions(revendaRepository, clienteMapper);
    }

    @Test
    void deletar_Admin_Sucesso() {
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(clienteRepository.findById(CLIENTE_ID)).thenReturn(Optional.of(cliente));
        
        clienteService.delete(CLIENTE_ID);

        verify(clienteRepository).findById(CLIENTE_ID);
        verify(clienteRepository).deleteById(CLIENTE_ID);
        verifyNoInteractions(revendaRepository, clienteMapper);
    }

    @Test
    void deletar_NaoAdmin_MesmaRevenda_Sucesso() {
        
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_GERENTE);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(clienteRepository.findById(CLIENTE_ID)).thenReturn(Optional.of(cliente));
        
        clienteService.delete(CLIENTE_ID);

        verify(clienteRepository).findById(CLIENTE_ID);
        verify(clienteRepository).deleteById(CLIENTE_ID);
        verifyNoInteractions(revendaRepository, clienteMapper);
    }

    @Test
    void deletar_NaoAdmin_RevendaDiferente_LancaForbidden() {
        
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_GERENTE);
        when(jwt.getClaimAsString("revendaId")).thenReturn(OUTRA_REVENDA_ID.toString());
        when(clienteRepository.findById(CLIENTE_ID)).thenReturn(Optional.of(cliente));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            clienteService.delete(CLIENTE_ID);
        });
        assertEquals(FORBIDDEN, exception.getStatusCode());
        assertEquals("Você só pode gerenciar clientes da sua revenda", exception.getReason());
        verify(clienteRepository).findById(CLIENTE_ID);
        verifyNoMoreInteractions(clienteRepository);
        verifyNoInteractions(revendaRepository, clienteMapper);
    }

    @Test
    void deletar_NaoEncontrado_LancaNotFound() {
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(clienteRepository.findById(CLIENTE_ID)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            clienteService.delete(CLIENTE_ID);
        });
        assertEquals(NOT_FOUND, exception.getStatusCode());
        assertEquals("Cliente não encontrado", exception.getReason());
        verify(clienteRepository).findById(CLIENTE_ID);
        verifyNoMoreInteractions(clienteRepository);
        verifyNoInteractions(revendaRepository, clienteMapper);
    }
}