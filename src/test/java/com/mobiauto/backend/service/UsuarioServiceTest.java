package com.mobiauto.backend.service;

import com.mobiauto.backend.dto.UsuarioRequestDTO;
import com.mobiauto.backend.dto.UsuarioResponseDTO;
import com.mobiauto.backend.mapper.UsuarioMapper;
import com.mobiauto.backend.model.Cargo;
import com.mobiauto.backend.model.Revenda;
import com.mobiauto.backend.model.Usuario;
import com.mobiauto.backend.repository.RevendaRepository;
import com.mobiauto.backend.repository.UsuarioRepository;
import com.mobiauto.backend.utils.JwtAuthUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static com.mobiauto.backend.model.Cargo.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RevendaRepository revendaRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UsuarioMapper usuarioMapper;

    @Mock
    private Jwt jwt;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;
    private Revenda revenda;
    private UsuarioRequestDTO usuarioRequestDTO;
    private UsuarioResponseDTO usuarioResponseDTO;
    private static final Long USUARIO_ID = 1L;
    private static final Long REVENDA_ID = 1L;
    private static final Long OUTRA_REVENDA_ID = 2L;
    private static final String NOME = "Teste Usuario";
    private static final String EMAIL = "teste@exemplo.com";
    private static final String SENHA = "senha123";
    private static final String SENHA_CODIFICADA = "encodedSenha123";
    private static final Cargo CARGO = ASSISTENTE;
    private static final List<Cargo> CARGOS_ADMIN = List.of(ADMINISTRADOR);
    private static final List<Cargo> CARGOS_PROPRIETARIO = List.of(PROPRIETARIO);
    private static final List<Cargo> CARGOS_ASSISTENTE = List.of(ASSISTENTE);

    private MockedStatic<JwtAuthUtil> jwtAuthUtilMockedStatic;

    @BeforeEach
    void setUp() {
        revenda = new Revenda();
        revenda.setId(REVENDA_ID);

        usuario = new Usuario();
        usuario.setId(USUARIO_ID);
        usuario.setNome(NOME);
        usuario.setEmail(EMAIL);
        usuario.setSenha(SENHA_CODIFICADA);
        usuario.setCargo(CARGO);
        usuario.setRevenda(revenda);
        usuario.setDataUltimaAtribuicao(null);

        usuarioRequestDTO = new UsuarioRequestDTO(NOME, EMAIL, SENHA, CARGO, REVENDA_ID);

        usuarioResponseDTO = new UsuarioResponseDTO(USUARIO_ID, NOME, EMAIL, CARGO, REVENDA_ID, null);

        jwtAuthUtilMockedStatic = mockStatic(JwtAuthUtil.class);
    }

    @AfterEach
    void tearDown() {
        jwtAuthUtilMockedStatic.close();
    }

    @Test
    void buscarTodos_Admin_RetornaTodosUsuarios() {
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));
        when(usuarioMapper.toResponseDTO(usuario)).thenReturn(usuarioResponseDTO);

        List<UsuarioResponseDTO> result = usuarioService.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(usuarioResponseDTO, result.get(0));
        verify(usuarioRepository).findAll();
        verify(usuarioMapper).toResponseDTO(usuario);
        verifyNoMoreInteractions(usuarioRepository, usuarioMapper);
    }

    @Test
    void buscarTodos_NaoAdmin_RetornaUsuariosDaRevenda() {
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ASSISTENTE);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(usuarioRepository.findAllByRevenda_Id(REVENDA_ID)).thenReturn(List.of(usuario));
        when(usuarioMapper.toResponseDTO(usuario)).thenReturn(usuarioResponseDTO);

        List<UsuarioResponseDTO> result = usuarioService.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(usuarioResponseDTO, result.get(0));
        verify(usuarioRepository).findAllByRevenda_Id(REVENDA_ID);
        verify(usuarioMapper).toResponseDTO(usuario);
        verifyNoMoreInteractions(usuarioRepository, usuarioMapper);
    }

    @Test
    void buscarPorId_Admin_Sucesso() {
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(usuarioRepository.findById(USUARIO_ID)).thenReturn(Optional.of(usuario));
        when(usuarioMapper.toResponseDTO(usuario)).thenReturn(usuarioResponseDTO);

        UsuarioResponseDTO result = usuarioService.findById(USUARIO_ID);

        assertNotNull(result);
        assertEquals(usuarioResponseDTO, result);
        verify(usuarioRepository).findById(USUARIO_ID);
        verify(usuarioMapper).toResponseDTO(usuario);
        verifyNoMoreInteractions(usuarioRepository, usuarioMapper);
    }

    @Test
    void buscarPorId_NaoAdmin_MesmaRevenda_Sucesso() {
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ASSISTENTE);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(usuarioRepository.findById(USUARIO_ID)).thenReturn(Optional.of(usuario));
        when(usuarioMapper.toResponseDTO(usuario)).thenReturn(usuarioResponseDTO);

        UsuarioResponseDTO result = usuarioService.findById(USUARIO_ID);

        assertNotNull(result);
        assertEquals(usuarioResponseDTO, result);
        verify(usuarioRepository).findById(USUARIO_ID);
        verify(usuarioMapper).toResponseDTO(usuario);
        verifyNoMoreInteractions(usuarioRepository, usuarioMapper);
    }

    @Test
    void buscarPorId_NaoAdmin_RevendaDiferente_LancaForbidden() {
        Revenda outraRevenda = new Revenda();
        outraRevenda.setId(OUTRA_REVENDA_ID);
        usuario.setRevenda(outraRevenda);
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ASSISTENTE);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(usuarioRepository.findById(USUARIO_ID)).thenReturn(Optional.of(usuario));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            usuarioService.findById(USUARIO_ID);
        });
        assertEquals(FORBIDDEN, exception.getStatusCode());
        assertEquals("Você só pode acessar usuários da sua revenda", exception.getReason());
        verify(usuarioRepository).findById(USUARIO_ID);
        verifyNoInteractions(usuarioMapper);
    }

    @Test
    void buscarPorId_NaoEncontrado_LancaNotFound() {
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(usuarioRepository.findById(USUARIO_ID)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            usuarioService.findById(USUARIO_ID);
        });
        assertEquals(NOT_FOUND, exception.getStatusCode());
        assertEquals("Usuário não encontrado", exception.getReason());
        verify(usuarioRepository).findById(USUARIO_ID);
        verifyNoInteractions(usuarioMapper);
    }

    @Test
    void buscarPorEmail_Admin_Sucesso() {
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(usuarioMapper.toResponseDTO(usuario)).thenReturn(usuarioResponseDTO);

        UsuarioResponseDTO result = usuarioService.findByEmail(EMAIL);

        assertNotNull(result);
        assertEquals(usuarioResponseDTO, result);
        verify(usuarioRepository).findByEmail(EMAIL);
        verify(usuarioMapper).toResponseDTO(usuario);
        verifyNoMoreInteractions(usuarioRepository, usuarioMapper);
    }

    @Test
    void buscarPorEmail_NaoAdmin_MesmaRevenda_Sucesso() {
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ASSISTENTE);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(usuarioMapper.toResponseDTO(usuario)).thenReturn(usuarioResponseDTO);

        UsuarioResponseDTO result = usuarioService.findByEmail(EMAIL);

        assertNotNull(result);
        assertEquals(usuarioResponseDTO, result);
        verify(usuarioRepository).findByEmail(EMAIL);
        verify(usuarioMapper).toResponseDTO(usuario);
        verifyNoMoreInteractions(usuarioRepository, usuarioMapper);
    }

    @Test
    void buscarPorEmail_NaoAdmin_RevendaDiferente_LancaForbidden() {
        Revenda outraRevenda = new Revenda();
        outraRevenda.setId(OUTRA_REVENDA_ID);
        usuario.setRevenda(outraRevenda);
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ASSISTENTE);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            usuarioService.findByEmail(EMAIL);
        });
        assertEquals(FORBIDDEN, exception.getStatusCode());
        assertEquals("Você só pode acessar usuários da sua revenda", exception.getReason());
        verify(usuarioRepository).findByEmail(EMAIL);
        verifyNoInteractions(usuarioMapper);
    }

    @Test
    void buscarPorEmail_NaoEncontrado_LancaNotFound() {
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            usuarioService.findByEmail(EMAIL);
        });
        assertEquals(NOT_FOUND, exception.getStatusCode());
        assertEquals("Usuário não encontrado com o e-mail: " + EMAIL, exception.getReason());
        verify(usuarioRepository).findByEmail(EMAIL);
        verifyNoInteractions(usuarioMapper);
    }

    @Test
    void salvar_Admin_Sucesso() {
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(revendaRepository.findById(REVENDA_ID)).thenReturn(Optional.of(revenda));
        when(passwordEncoder.encode(SENHA)).thenReturn(SENHA_CODIFICADA);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(usuarioMapper.toResponseDTO(usuario)).thenReturn(usuarioResponseDTO);

        UsuarioResponseDTO result = usuarioService.save(usuarioRequestDTO);

        assertNotNull(result);
        assertEquals(usuarioResponseDTO, result);
        verify(usuarioRepository).findByEmail(EMAIL);
        verify(revendaRepository).findById(REVENDA_ID);
        verify(passwordEncoder).encode(SENHA);
        verify(usuarioRepository).save(any(Usuario.class));
        verify(usuarioMapper).toResponseDTO(usuario);
        verifyNoMoreInteractions(usuarioRepository, revendaRepository, passwordEncoder, usuarioMapper);
    }

    @Test
    void salvar_Proprietario_MesmaRevenda_Sucesso() {
        UsuarioRequestDTO dto = new UsuarioRequestDTO(NOME, EMAIL, SENHA, CARGO, REVENDA_ID);
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_PROPRIETARIO);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(revendaRepository.findById(REVENDA_ID)).thenReturn(Optional.of(revenda));
        when(passwordEncoder.encode(SENHA)).thenReturn(SENHA_CODIFICADA);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(usuarioMapper.toResponseDTO(usuario)).thenReturn(usuarioResponseDTO);

        UsuarioResponseDTO result = usuarioService.save(dto);

        assertNotNull(result);
        assertEquals(usuarioResponseDTO, result);
        verify(usuarioRepository).findByEmail(EMAIL);
        verify(revendaRepository).findById(REVENDA_ID);
        verify(passwordEncoder).encode(SENHA);
        verify(usuarioRepository).save(any(Usuario.class));
        verify(usuarioMapper).toResponseDTO(usuario);
        verifyNoMoreInteractions(usuarioRepository, revendaRepository, passwordEncoder, usuarioMapper);
    }

    @Test
    void salvar_Proprietario_RevendaDiferente_LancaForbidden() {
        UsuarioRequestDTO dtoOutraRevenda = new UsuarioRequestDTO(NOME, EMAIL, SENHA, CARGO, OUTRA_REVENDA_ID);
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_PROPRIETARIO);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            usuarioService.save(dtoOutraRevenda);
        });
        assertEquals(FORBIDDEN, exception.getStatusCode());
        assertEquals("Proprietários e gerentes só podem cadastrar usuários em sua própria revenda", exception.getReason());
        verifyNoInteractions(usuarioRepository, revendaRepository, passwordEncoder, usuarioMapper);
    }

    @Test
    void salvar_Assistente_LancaForbidden() {
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ASSISTENTE);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            usuarioService.save(usuarioRequestDTO);
        });
        assertEquals(FORBIDDEN, exception.getStatusCode());
        assertEquals("Apenas administradores, proprietários ou gerentes podem cadastrar usuários", exception.getReason());
        verifyNoInteractions(usuarioRepository, revendaRepository, passwordEncoder, usuarioMapper);
    }

    @Test
    void salvar_EmailJaCadastrado_LancaBadRequest() {
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            usuarioService.save(usuarioRequestDTO);
        });
        assertEquals(BAD_REQUEST, exception.getStatusCode());
        assertEquals("E-mail já cadastrado", exception.getReason());
        verify(usuarioRepository).findByEmail(EMAIL);
        verifyNoMoreInteractions(usuarioRepository);
        verifyNoInteractions(revendaRepository, passwordEncoder, usuarioMapper);
    }

    @Test
    void salvar_RevendaNaoEncontrada_LancaNotFound() {
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(revendaRepository.findById(REVENDA_ID)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            usuarioService.save(usuarioRequestDTO);
        });
        assertEquals(NOT_FOUND, exception.getStatusCode());
        assertEquals("Revenda não encontrada: " + REVENDA_ID, exception.getReason());
        verify(usuarioRepository).findByEmail(EMAIL);
        verify(revendaRepository).findById(REVENDA_ID);
        verifyNoMoreInteractions(usuarioRepository, revendaRepository);
        verifyNoInteractions(passwordEncoder, usuarioMapper);
    }

    @Test
    void atualizar_Admin_Sucesso() {
        UsuarioRequestDTO novoDto = new UsuarioRequestDTO("Novo Nome", "novo@exemplo.com", null, GERENTE, REVENDA_ID);
        System.out.println("novoDto.getRevendaId(): " + novoDto.getRevendaId()); 
        assertEquals(REVENDA_ID, novoDto.getRevendaId(), "O revendaId do DTO deve ser igual ao REVENDA_ID");
        UsuarioResponseDTO novoResponseDTO = new UsuarioResponseDTO(USUARIO_ID, "Novo Nome", "novo@exemplo.com", GERENTE, REVENDA_ID, null);
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(usuarioRepository.findById(USUARIO_ID)).thenReturn(Optional.of(usuario));
        when(revendaRepository.findById(REVENDA_ID)).thenReturn(Optional.of(revenda));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);
        when(usuarioMapper.toResponseDTO(usuario)).thenReturn(novoResponseDTO);

        UsuarioResponseDTO result = usuarioService.update(USUARIO_ID, novoDto);

        assertNotNull(result);
        assertEquals(novoResponseDTO, result);
        verify(usuarioRepository).findById(USUARIO_ID);
        verify(revendaRepository).findById(REVENDA_ID);
        verify(usuarioRepository).save(usuario);
        verify(usuarioMapper).toResponseDTO(usuario);
        verifyNoMoreInteractions(usuarioRepository, revendaRepository, usuarioMapper);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void atualizar_Proprietario_MesmaRevenda_Sucesso() {
        UsuarioRequestDTO novoDto = new UsuarioRequestDTO("Novo Nome", "novo@exemplo.com", SENHA, GERENTE, REVENDA_ID);
        UsuarioResponseDTO novoResponseDTO = new UsuarioResponseDTO(USUARIO_ID, "Novo Nome", "novo@exemplo.com", GERENTE, REVENDA_ID, null);
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_PROPRIETARIO);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(usuarioRepository.findById(USUARIO_ID)).thenReturn(Optional.of(usuario));
        when(revendaRepository.findById(REVENDA_ID)).thenReturn(Optional.of(revenda));
        when(passwordEncoder.encode(SENHA)).thenReturn(SENHA_CODIFICADA);
        when(usuarioRepository.save(usuario)).thenReturn(usuario);
        when(usuarioMapper.toResponseDTO(usuario)).thenReturn(novoResponseDTO);

        UsuarioResponseDTO result = usuarioService.update(USUARIO_ID, novoDto);

        assertNotNull(result);
        assertEquals(novoResponseDTO, result);
        verify(usuarioRepository).findById(USUARIO_ID);
        verify(revendaRepository).findById(REVENDA_ID);
        verify(passwordEncoder).encode(SENHA);
        verify(usuarioRepository).save(usuario);
        verify(usuarioMapper).toResponseDTO(usuario);
        verifyNoMoreInteractions(usuarioRepository, revendaRepository, passwordEncoder, usuarioMapper);
    }

    @Test
    void atualizar_Proprietario_RevendaDiferente_LancaForbidden() {
        Revenda outraRevenda = new Revenda();
        outraRevenda.setId(OUTRA_REVENDA_ID);
        usuario.setRevenda(outraRevenda);
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_PROPRIETARIO);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(usuarioRepository.findById(USUARIO_ID)).thenReturn(Optional.of(usuario));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            usuarioService.update(USUARIO_ID, usuarioRequestDTO);
        });
        assertEquals(FORBIDDEN, exception.getStatusCode());
        assertEquals("Proprietários só podem editar usuários da sua própria revenda", exception.getReason());
        verify(usuarioRepository).findById(USUARIO_ID);
        verifyNoMoreInteractions(usuarioRepository);
        verifyNoInteractions(revendaRepository, passwordEncoder, usuarioMapper);
    }

    @Test
    void atualizar_Assistente_LancaForbidden() {
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ASSISTENTE);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            usuarioService.update(USUARIO_ID, usuarioRequestDTO);
        });
        assertEquals(FORBIDDEN, exception.getStatusCode());
        assertEquals("Apenas administradores ou proprietários podem editar perfis", exception.getReason());
        verifyNoInteractions(usuarioRepository, revendaRepository, passwordEncoder, usuarioMapper);
    }

    @Test
    void atualizar_UsuarioNaoEncontrado_LancaNotFound() {
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(usuarioRepository.findById(USUARIO_ID)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            usuarioService.update(USUARIO_ID, usuarioRequestDTO);
        });
        assertEquals(NOT_FOUND, exception.getStatusCode());
        assertEquals("Usuário não encontrado", exception.getReason());
        verify(usuarioRepository).findById(USUARIO_ID);
        verifyNoMoreInteractions(usuarioRepository);
        verifyNoInteractions(revendaRepository, passwordEncoder, usuarioMapper);
    }

    @Test
    void atualizar_RevendaNaoEncontrada_LancaNotFound() {
        UsuarioRequestDTO dto = new UsuarioRequestDTO(NOME, EMAIL, SENHA, CARGO, REVENDA_ID);
        assertEquals(REVENDA_ID, dto.getRevendaId(), "O revendaId do DTO deve ser igual ao REVENDA_ID");
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(usuarioRepository.findById(USUARIO_ID)).thenReturn(Optional.of(usuario));
        when(revendaRepository.findById(REVENDA_ID)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            usuarioService.update(USUARIO_ID, dto);
        });
        assertEquals(NOT_FOUND, exception.getStatusCode());
        assertEquals("Revenda não encontrada: " + REVENDA_ID, exception.getReason());
        verify(usuarioRepository).findById(USUARIO_ID);
        verify(revendaRepository).findById(REVENDA_ID);
        verifyNoMoreInteractions(usuarioRepository, revendaRepository);
        verifyNoInteractions(passwordEncoder, usuarioMapper);
    }

    @Test
    void deletar_Admin_Sucesso() {
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(usuarioRepository.findById(USUARIO_ID)).thenReturn(Optional.of(usuario));

        usuarioService.delete(USUARIO_ID);

        verify(usuarioRepository).findById(USUARIO_ID);
        verify(usuarioRepository).deleteById(USUARIO_ID);
        verifyNoMoreInteractions(usuarioRepository);
        verifyNoInteractions(revendaRepository, passwordEncoder, usuarioMapper);
    }

    @Test
    void deletar_NaoAdmin_MesmaRevenda_Sucesso() {
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_PROPRIETARIO);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(usuarioRepository.findById(USUARIO_ID)).thenReturn(Optional.of(usuario));

        usuarioService.delete(USUARIO_ID);

        verify(usuarioRepository).findById(USUARIO_ID);
        verify(usuarioRepository).deleteById(USUARIO_ID);
        verifyNoMoreInteractions(usuarioRepository);
        verifyNoInteractions(revendaRepository, passwordEncoder, usuarioMapper);
    }

    @Test
    void deletar_NaoAdmin_RevendaDiferente_LancaForbidden() {
        Revenda outraRevenda = new Revenda();
        outraRevenda.setId(OUTRA_REVENDA_ID);
        usuario.setRevenda(outraRevenda);
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_PROPRIETARIO);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(usuarioRepository.findById(USUARIO_ID)).thenReturn(Optional.of(usuario));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            usuarioService.delete(USUARIO_ID);
        });
        assertEquals(FORBIDDEN, exception.getStatusCode());
        assertEquals("Você só pode excluir usuários da sua revenda", exception.getReason());
        verify(usuarioRepository).findById(USUARIO_ID);
        verifyNoMoreInteractions(usuarioRepository);
        verifyNoInteractions(revendaRepository, passwordEncoder, usuarioMapper);
    }

    @Test
    void deletar_UsuarioNaoEncontrado_LancaNotFound() {
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(usuarioRepository.findById(USUARIO_ID)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            usuarioService.delete(USUARIO_ID);
        });
        assertEquals(NOT_FOUND, exception.getStatusCode());
        assertEquals("Usuário não encontrado", exception.getReason());
        verify(usuarioRepository).findById(USUARIO_ID);
        verifyNoMoreInteractions(usuarioRepository);
        verifyNoInteractions(revendaRepository, passwordEncoder, usuarioMapper);
    }

    @Test
    void toUsuario_Sucesso() {
        when(usuarioRepository.findById(USUARIO_ID)).thenReturn(Optional.of(usuario));

        Usuario result = usuarioService.toUsuario(usuarioResponseDTO);

        assertNotNull(result);
        assertEquals(usuario, result);
        verify(usuarioRepository).findById(USUARIO_ID);
        verifyNoMoreInteractions(usuarioRepository);
        verifyNoInteractions(revendaRepository, passwordEncoder, usuarioMapper);
    }

    @Test
    void toUsuario_NaoEncontrado_LancaNotFound() {
        when(usuarioRepository.findById(USUARIO_ID)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            usuarioService.toUsuario(usuarioResponseDTO);
        });
        assertEquals(NOT_FOUND, exception.getStatusCode());
        assertEquals("Usuário não encontrado: " + USUARIO_ID, exception.getReason());
        verify(usuarioRepository).findById(USUARIO_ID);
        verifyNoMoreInteractions(usuarioRepository);
        verifyNoInteractions(revendaRepository, passwordEncoder, usuarioMapper);
    }
}