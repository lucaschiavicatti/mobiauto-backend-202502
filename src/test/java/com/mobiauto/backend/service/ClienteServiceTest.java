package com.mobiauto.backend.service;

import com.mobiauto.backend.dto.ClienteRequestDTO;
import com.mobiauto.backend.dto.ClienteResponseDTO;
import com.mobiauto.backend.mapper.ClienteMapper;
import com.mobiauto.backend.model.Cargo;
import com.mobiauto.backend.model.Cliente;
import com.mobiauto.backend.model.Revenda;
import com.mobiauto.backend.model.Usuario;
import com.mobiauto.backend.repository.ClienteRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private RevendaRepository revendaRepository;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private ClienteMapper clienteMapper;

    @InjectMocks
    private ClienteService clienteService;

    private Usuario usuarioAdmin;
    private Usuario usuarioNormal;
    private Revenda revenda;

    @BeforeEach
    void setUp() {
        // Usuário administrador
        usuarioAdmin = new Usuario(1L, "Admin", "admin@example.com", "senha", Cargo.ADMINISTRADOR, new Revenda(1L));
        // Usuário não administrador
        revenda = new Revenda(1L);
        usuarioNormal = new Usuario(2L, "João", "joao@example.com", "senha", Cargo.GERENTE, revenda);
    }

    private void mockSecurityContext(Usuario usuario) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(usuario);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(usuarioService.getUsuarioLogado()).thenReturn(usuario);
    }

    @Test
    void findAll_DeveRetornarTodosClientes_QuandoUsuarioAdmin() {
        mockSecurityContext(usuarioAdmin);
        Cliente cliente = new Cliente();
        List<Cliente> clientes = List.of(cliente);
        when(clienteRepository.findAll()).thenReturn(clientes);
        ClienteResponseDTO responseDTO = new ClienteResponseDTO(1L, "Cliente", "cliente@example.com", "123", 1L);
        when(clienteMapper.toResponseDTO(cliente)).thenReturn(responseDTO);

        List<ClienteResponseDTO> result = clienteService.findAll();

        assertEquals(1, result.size());
        assertEquals(responseDTO, result.get(0));
        verify(clienteRepository).findAll();
    }

    @Test
    void findAll_DeveRetornarClientesDaRevenda_QuandoUsuarioNaoAdmin() {
        mockSecurityContext(usuarioNormal);
        Cliente cliente = new Cliente();
        List<Cliente> clientes = List.of(cliente);
        when(clienteRepository.findAllByRevenda_Id(1L)).thenReturn(clientes);
        ClienteResponseDTO responseDTO = new ClienteResponseDTO(1L, "Cliente", "cliente@example.com", "123", 1L);
        when(clienteMapper.toResponseDTO(cliente)).thenReturn(responseDTO);

        List<ClienteResponseDTO> result = clienteService.findAll();

        assertEquals(1, result.size());
        assertEquals(responseDTO, result.get(0));
        verify(clienteRepository).findAllByRevenda_Id(1L);
    }

    @Test
    void findById_DeveRetornarCliente_QuandoUsuarioTemAcesso() {
        mockSecurityContext(usuarioNormal);
        Cliente cliente = new Cliente();
        cliente.setRevenda(revenda);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        ClienteResponseDTO responseDTO = new ClienteResponseDTO(1L, "Cliente", "cliente@example.com", "123", 1L);
        when(clienteMapper.toResponseDTO(cliente)).thenReturn(responseDTO);

        ClienteResponseDTO result = clienteService.findById(1L);

        assertEquals(responseDTO, result);
        verify(clienteRepository).findById(1L);
    }

    @Test
    void findById_DeveLancarNotFound_QuandoClienteNaoExiste() {
        mockSecurityContext(usuarioNormal);
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            clienteService.findById(1L);
        });
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Cliente não encontrado", exception.getReason());
    }

    @Test
    void findById_DeveLancarForbidden_QuandoUsuarioSemAcesso() {
        mockSecurityContext(usuarioNormal);
        Cliente cliente = new Cliente();
        cliente.setRevenda(new Revenda(2L));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            clienteService.findById(1L);
        });
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("Você só pode gerenciar clientes da sua revenda", exception.getReason());
    }

    @Test
    void save_DeveSalvarCliente_QuandoDadosValidos() {
        mockSecurityContext(usuarioNormal);
        ClienteRequestDTO dto = new ClienteRequestDTO("Cliente", "cliente@example.com", "123", 1L);
        when(clienteRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(revendaRepository.findById(1L)).thenReturn(Optional.of(revenda));
        Cliente cliente = new Cliente();
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);
        ClienteResponseDTO responseDTO = new ClienteResponseDTO(1L, "Cliente", "cliente@example.com", "123", 1L);
        when(clienteMapper.toResponseDTO(cliente)).thenReturn(responseDTO);

        ClienteResponseDTO result = clienteService.save(dto);

        assertEquals(responseDTO, result);
        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    void save_DeveLancarConflict_QuandoEmailJaExiste() {
        mockSecurityContext(usuarioNormal);
        ClienteRequestDTO dto = new ClienteRequestDTO("Cliente", "cliente@example.com", "123", 1L);
        when(clienteRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            clienteService.save(dto);
        });
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Email já cadastrado: " + dto.getEmail(), exception.getReason());
    }

    @Test
    void update_DeveAtualizarCliente_QuandoDadosValidos() {
        mockSecurityContext(usuarioNormal);
        ClienteRequestDTO dto = new ClienteRequestDTO("Cliente Atualizado", "cliente@example.com", "123", 1L);
        Cliente cliente = new Cliente();
        cliente.setEmail("oldemail@example.com");
        cliente.setRevenda(revenda);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(revendaRepository.findById(1L)).thenReturn(Optional.of(revenda));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);
        ClienteResponseDTO responseDTO = new ClienteResponseDTO(1L, "Cliente Atualizado", "cliente@example.com", "123", 1L);
        when(clienteMapper.toResponseDTO(cliente)).thenReturn(responseDTO);

        ClienteResponseDTO result = clienteService.update(1L, dto);

        assertEquals(responseDTO, result);
        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    void delete_DeveDeletarCliente_QuandoUsuarioTemAcesso() {
        mockSecurityContext(usuarioNormal);
        Cliente cliente = new Cliente();
        cliente.setRevenda(revenda);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        clienteService.delete(1L);

        verify(clienteRepository).deleteById(1L);
    }
}