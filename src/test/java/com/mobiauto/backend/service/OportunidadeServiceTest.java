package com.mobiauto.backend.service;

import com.mobiauto.backend.dto.OportunidadeRequestDTO;
import com.mobiauto.backend.dto.OportunidadeResponseDTO;
import com.mobiauto.backend.dto.OportunidadeResponseDTO.ClienteDTO;
import com.mobiauto.backend.dto.OportunidadeResponseDTO.VeiculoDTO;
import com.mobiauto.backend.mapper.OportunidadeMapper;
import com.mobiauto.backend.model.*;
import com.mobiauto.backend.repository.*;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.mobiauto.backend.model.Cargo.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.*;

@ExtendWith(MockitoExtension.class)
class OportunidadeServiceTest {

    private static final Long OPORTUNIDADE_ID = 1L;
    private static final Long CLIENTE_ID = 1L;
    private static final Long VEICULO_ID = 1L;
    private static final Long REVENDA_ID = 1L;
    private static final Long OUTRA_REVENDA_ID = 2L;
    private static final Long USUARIO_ID = 1L;
    private static final String STATUS = "EM_ATENDIMENTO";
    private static final String CLIENTE_NOME = "Cliente Teste";
    private static final String CLIENTE_EMAIL = "cliente@example.com";
    private static final String CLIENTE_TELEFONE = "123456789";
    private static final String VEICULO_MARCA = "Toyota";
    private static final String VEICULO_MODELO = "Corolla";
    private static final String VEICULO_VERSAO = "XLT";
    private static final Integer VEICULO_ANO_MODELO = 2023;
    private static final List<Cargo> CARGOS_ADMIN = List.of(ADMINISTRADOR);
    private static final List<Cargo> CARGOS_ASSISTENTE = List.of(ASSISTENTE);
    @Mock
    private OportunidadeRepository oportunidadeRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private VeiculoRepository veiculoRepository;
    @Mock
    private RevendaRepository revendaRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private OportunidadeMapper oportunidadeMapper;
    @Mock
    private Jwt jwt;
    @InjectMocks
    private OportunidadeService oportunidadeService;
    private Oportunidade oportunidade;
    private Cliente cliente;
    private Veiculo veiculo;
    private Revenda revenda;
    private Usuario usuario;
    private OportunidadeRequestDTO oportunidadeRequestDTO;
    private OportunidadeResponseDTO oportunidadeResponseDTO;
    private MockedStatic<JwtAuthUtil> jwtAuthUtilMockedStatic;

    @BeforeEach
    void setUp() {
        revenda = new Revenda();
        revenda.setId(REVENDA_ID);

        cliente = new Cliente();
        cliente.setId(CLIENTE_ID);
        cliente.setNome(CLIENTE_NOME);
        cliente.setEmail(CLIENTE_EMAIL);
        cliente.setTelefone(CLIENTE_TELEFONE);

        veiculo = new Veiculo();
        veiculo.setId(VEICULO_ID);
        veiculo.setMarca(VEICULO_MARCA);
        veiculo.setModelo(VEICULO_MODELO);
        veiculo.setVersao(VEICULO_VERSAO);
        veiculo.setAnoModelo(VEICULO_ANO_MODELO);

        usuario = new Usuario();
        usuario.setId(USUARIO_ID);
        usuario.setRevenda(revenda);

        oportunidade = new Oportunidade();
        oportunidade.setId(OPORTUNIDADE_ID);
        oportunidade.setCliente(cliente);
        oportunidade.setVeiculo(veiculo);
        oportunidade.setUsuario(usuario);
        oportunidade.setRevenda(revenda);
        oportunidade.setStatus(StatusOportunidade.EM_ATENDIMENTO);
        oportunidade.setDataAtribuicao(LocalDateTime.now());

        oportunidadeRequestDTO = new OportunidadeRequestDTO(
                CLIENTE_ID, VEICULO_ID, USUARIO_ID, REVENDA_ID, STATUS, null
        );

        oportunidadeResponseDTO = new OportunidadeResponseDTO(
                OPORTUNIDADE_ID,
                new ClienteDTO(CLIENTE_ID, CLIENTE_NOME, CLIENTE_EMAIL, CLIENTE_TELEFONE),
                new VeiculoDTO(VEICULO_ID, VEICULO_MARCA, VEICULO_MODELO, VEICULO_VERSAO, VEICULO_ANO_MODELO),
                USUARIO_ID,
                REVENDA_ID,
                STATUS,
                null,
                oportunidade.getDataAtribuicao(),
                null
        );

        jwtAuthUtilMockedStatic = mockStatic(JwtAuthUtil.class);
    }

    @AfterEach
    void tearDown() {
        jwtAuthUtilMockedStatic.close();
    }

    @Test
    void buscarTodos_Admin_RetornaTodasOportunidades() {

        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(oportunidadeRepository.findAll()).thenReturn(List.of(oportunidade));
        when(oportunidadeMapper.toResponseDTO(oportunidade)).thenReturn(oportunidadeResponseDTO);
        
        List<OportunidadeResponseDTO> result = oportunidadeService.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        OportunidadeResponseDTO dto = result.get(0);
        assertEquals(oportunidadeResponseDTO.id(), dto.id());
        assertEquals(oportunidadeResponseDTO.cliente().id(), dto.cliente().id());
        assertEquals(oportunidadeResponseDTO.cliente().nome(), dto.cliente().nome());
        assertEquals(oportunidadeResponseDTO.veiculo().id(), dto.veiculo().id());
        assertEquals(oportunidadeResponseDTO.veiculo().marca(), dto.veiculo().marca());
        verify(oportunidadeRepository).findAll();
        verify(oportunidadeMapper).toResponseDTO(oportunidade);
        verifyNoMoreInteractions(oportunidadeRepository);
    }

    @Test
    void buscarTodos_NaoAdmin_RetornaOportunidadesPorRevenda() {

        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ASSISTENTE);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(oportunidadeRepository.findAllByRevenda_Id(REVENDA_ID)).thenReturn(List.of(oportunidade));
        when(oportunidadeMapper.toResponseDTO(oportunidade)).thenReturn(oportunidadeResponseDTO);
        
        List<OportunidadeResponseDTO> result = oportunidadeService.findAll();
        
        assertNotNull(result);
        assertEquals(1, result.size());
        OportunidadeResponseDTO dto = result.get(0);
        assertEquals(oportunidadeResponseDTO.id(), dto.id());
        assertEquals(oportunidadeResponseDTO.cliente().id(), dto.cliente().id());
        assertEquals(oportunidadeResponseDTO.cliente().nome(), dto.cliente().nome());
        assertEquals(oportunidadeResponseDTO.veiculo().id(), dto.veiculo().id());
        assertEquals(oportunidadeResponseDTO.veiculo().marca(), dto.veiculo().marca());
        verify(oportunidadeRepository).findAllByRevenda_Id(REVENDA_ID);
        verify(oportunidadeMapper).toResponseDTO(oportunidade);
        verifyNoMoreInteractions(oportunidadeRepository);
    }

    @Test
    void buscarPorId_Admin_Sucesso() {

        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString()); 
        when(oportunidadeRepository.findById(OPORTUNIDADE_ID)).thenReturn(Optional.of(oportunidade));
        when(oportunidadeMapper.toResponseDTO(oportunidade)).thenReturn(oportunidadeResponseDTO);


        OportunidadeResponseDTO result = oportunidadeService.findById(OPORTUNIDADE_ID);


        assertNotNull(result);
        assertEquals(oportunidadeResponseDTO.id(), result.id());
        assertEquals(oportunidadeResponseDTO.cliente().id(), result.cliente().id());
        assertEquals(oportunidadeResponseDTO.cliente().nome(), result.cliente().nome());
        assertEquals(oportunidadeResponseDTO.veiculo().id(), result.veiculo().id());
        assertEquals(oportunidadeResponseDTO.veiculo().marca(), result.veiculo().marca());
        verify(oportunidadeRepository).findById(OPORTUNIDADE_ID);
        verify(oportunidadeMapper).toResponseDTO(oportunidade);
    }

    @Test
    void buscarPorId_NaoAdmin_MesmaRevenda_Sucesso() {

        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ASSISTENTE);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(oportunidadeRepository.findById(OPORTUNIDADE_ID)).thenReturn(Optional.of(oportunidade));
        when(oportunidadeMapper.toResponseDTO(oportunidade)).thenReturn(oportunidadeResponseDTO);


        OportunidadeResponseDTO result = oportunidadeService.findById(OPORTUNIDADE_ID);


        assertNotNull(result);
        assertEquals(oportunidadeResponseDTO.id(), result.id());
        assertEquals(oportunidadeResponseDTO.cliente().id(), result.cliente().id());
        assertEquals(oportunidadeResponseDTO.cliente().nome(), result.cliente().nome());
        assertEquals(oportunidadeResponseDTO.veiculo().id(), result.veiculo().id());
        assertEquals(oportunidadeResponseDTO.veiculo().marca(), result.veiculo().marca());
        verify(oportunidadeRepository).findById(OPORTUNIDADE_ID);
        verify(oportunidadeMapper).toResponseDTO(oportunidade);
    }

    @Test
    void buscarPorId_NaoAdmin_RevendaDiferente_LancaForbidden() {

        Revenda outraRevenda = new Revenda();
        outraRevenda.setId(OUTRA_REVENDA_ID);
        oportunidade.setRevenda(outraRevenda);
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ASSISTENTE);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(oportunidadeRepository.findById(OPORTUNIDADE_ID)).thenReturn(Optional.of(oportunidade));
         
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> oportunidadeService.findById(OPORTUNIDADE_ID));
        assertEquals(FORBIDDEN, exception.getStatusCode());
        assertEquals("Você só pode acessar oportunidades da sua revenda", exception.getReason());
        verify(oportunidadeRepository).findById(OPORTUNIDADE_ID);
        verifyNoInteractions(oportunidadeMapper);
    }

    @Test
    void buscarPorId_NaoEncontrado_LancaNotFound() {

        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(oportunidadeRepository.findById(OPORTUNIDADE_ID)).thenReturn(Optional.empty());
         
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> oportunidadeService.findById(OPORTUNIDADE_ID));
        assertEquals(NOT_FOUND, exception.getStatusCode());
        assertEquals("Oportunidade não encontrada", exception.getReason());
        verify(oportunidadeRepository).findById(OPORTUNIDADE_ID);
        verifyNoInteractions(oportunidadeMapper);
    }

    @Test
    void salvar_Admin_Sucesso() {

        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(clienteRepository.findById(CLIENTE_ID)).thenReturn(Optional.of(cliente));
        when(veiculoRepository.findById(VEICULO_ID)).thenReturn(Optional.of(veiculo));
        when(revendaRepository.findById(REVENDA_ID)).thenReturn(Optional.of(revenda));
        when(usuarioRepository.findById(USUARIO_ID)).thenReturn(Optional.of(usuario));
        when(oportunidadeRepository.save(any(Oportunidade.class))).thenReturn(oportunidade);
        when(oportunidadeMapper.toResponseDTO(oportunidade)).thenReturn(oportunidadeResponseDTO);

        OportunidadeResponseDTO result = oportunidadeService.save(oportunidadeRequestDTO);

        assertNotNull(result);
        assertEquals(oportunidadeResponseDTO.id(), result.id());
        assertEquals(oportunidadeResponseDTO.cliente().id(), result.cliente().id());
        assertEquals(oportunidadeResponseDTO.cliente().nome(), result.cliente().nome());
        assertEquals(oportunidadeResponseDTO.veiculo().id(), result.veiculo().id());
        assertEquals(oportunidadeResponseDTO.veiculo().marca(), result.veiculo().marca());
        verify(clienteRepository).findById(CLIENTE_ID);
        verify(veiculoRepository).findById(VEICULO_ID);
        verify(revendaRepository).findById(REVENDA_ID);
        verify(usuarioRepository).findById(USUARIO_ID);
        verify(usuarioRepository).save(usuario);
        verify(oportunidadeRepository).save(any(Oportunidade.class));
        verify(oportunidadeMapper).toResponseDTO(oportunidade);
    }

    @Test
    void salvar_SemUsuario_DistribuiAssistente_Sucesso() {

        OportunidadeRequestDTO dtoSemUsuario = new OportunidadeRequestDTO(
                CLIENTE_ID, VEICULO_ID, null, REVENDA_ID, STATUS, null
        );
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(clienteRepository.findById(CLIENTE_ID)).thenReturn(Optional.of(cliente));
        when(veiculoRepository.findById(VEICULO_ID)).thenReturn(Optional.of(veiculo));
        when(revendaRepository.findById(REVENDA_ID)).thenReturn(Optional.of(revenda));
        when(usuarioRepository.findByCargoAndRevendaId(ASSISTENTE, REVENDA_ID)).thenReturn(List.of(usuario));
        when(oportunidadeRepository.save(any(Oportunidade.class))).thenReturn(oportunidade);
        when(oportunidadeMapper.toResponseDTO(oportunidade)).thenReturn(oportunidadeResponseDTO);

        OportunidadeResponseDTO result = oportunidadeService.save(dtoSemUsuario);

        assertNotNull(result);
        assertEquals(oportunidadeResponseDTO.id(), result.id());
        assertEquals(oportunidadeResponseDTO.cliente().id(), result.cliente().id());
        assertEquals(oportunidadeResponseDTO.cliente().nome(), result.cliente().nome());
        assertEquals(oportunidadeResponseDTO.veiculo().id(), result.veiculo().id());
        assertEquals(oportunidadeResponseDTO.veiculo().marca(), result.veiculo().marca());
        verify(clienteRepository).findById(CLIENTE_ID);
        verify(veiculoRepository).findById(VEICULO_ID);
        verify(revendaRepository).findById(REVENDA_ID);
        verify(usuarioRepository).findByCargoAndRevendaId(ASSISTENTE, REVENDA_ID);
        verify(usuarioRepository).save(usuario);
        verify(oportunidadeRepository).save(any(Oportunidade.class));
        verify(oportunidadeMapper).toResponseDTO(oportunidade);
    }

    @Test
    void salvar_ClienteNaoEncontrado_LancaNotFound() {

        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(clienteRepository.findById(CLIENTE_ID)).thenReturn(Optional.empty());

         
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> oportunidadeService.save(oportunidadeRequestDTO));
        assertEquals(NOT_FOUND, exception.getStatusCode());
        assertEquals("Cliente não encontrado: " + CLIENTE_ID, exception.getReason());
        verify(clienteRepository).findById(CLIENTE_ID);
        verifyNoInteractions(veiculoRepository, revendaRepository, usuarioRepository, oportunidadeRepository, oportunidadeMapper);
    }

    @Test
    void salvar_NaoAdmin_RevendaDiferente_LancaForbidden() {

        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ASSISTENTE);
        when(jwt.getClaimAsString("revendaId")).thenReturn(OUTRA_REVENDA_ID.toString());

         
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> oportunidadeService.save(oportunidadeRequestDTO));
        assertEquals(FORBIDDEN, exception.getStatusCode());
        assertEquals("Você só pode criar oportunidades na sua revenda", exception.getReason());
        verifyNoInteractions(clienteRepository, veiculoRepository, revendaRepository, usuarioRepository, oportunidadeRepository, oportunidadeMapper);
    }

    @Test
    void salvar_StatusConcluido_SemMotivo_LancaBadRequest() {

        OportunidadeRequestDTO dtoConcluido = new OportunidadeRequestDTO(
                CLIENTE_ID, VEICULO_ID, USUARIO_ID, REVENDA_ID, "CONCLUIDO", null
        );
        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(clienteRepository.findById(CLIENTE_ID)).thenReturn(Optional.of(cliente));
        when(veiculoRepository.findById(VEICULO_ID)).thenReturn(Optional.of(veiculo));
        when(revendaRepository.findById(REVENDA_ID)).thenReturn(Optional.of(revenda));
        when(usuarioRepository.findById(USUARIO_ID)).thenReturn(Optional.of(usuario));

         
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> oportunidadeService.save(dtoConcluido));
        assertEquals(BAD_REQUEST, exception.getStatusCode());
        assertEquals("Motivo de conclusão é obrigatório para status CONCLUIDO", exception.getReason());
        verify(clienteRepository).findById(CLIENTE_ID);
        verify(veiculoRepository).findById(VEICULO_ID);
        verify(revendaRepository).findById(REVENDA_ID);
        verify(usuarioRepository).findById(USUARIO_ID);
        verifyNoInteractions(oportunidadeRepository, oportunidadeMapper);
    }

    @Test
    void atualizar_Admin_Sucesso() {

        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(jwt.getSubject()).thenReturn("2"); 
        when(oportunidadeRepository.findById(OPORTUNIDADE_ID)).thenReturn(Optional.of(oportunidade));
        when(clienteRepository.findById(CLIENTE_ID)).thenReturn(Optional.of(cliente));
        when(veiculoRepository.findById(VEICULO_ID)).thenReturn(Optional.of(veiculo));
        when(revendaRepository.findById(REVENDA_ID)).thenReturn(Optional.of(revenda));
        when(usuarioRepository.findById(USUARIO_ID)).thenReturn(Optional.of(usuario));
        when(oportunidadeRepository.save(oportunidade)).thenReturn(oportunidade);
        when(oportunidadeMapper.toResponseDTO(oportunidade)).thenReturn(oportunidadeResponseDTO);


        OportunidadeResponseDTO result = oportunidadeService.update(OPORTUNIDADE_ID, oportunidadeRequestDTO);


        assertNotNull(result);
        assertEquals(oportunidadeResponseDTO.id(), result.id());
        assertEquals(oportunidadeResponseDTO.cliente().id(), result.cliente().id());
        assertEquals(oportunidadeResponseDTO.cliente().nome(), result.cliente().nome());
        assertEquals(oportunidadeResponseDTO.veiculo().id(), result.veiculo().id());
        assertEquals(oportunidadeResponseDTO.veiculo().marca(), result.veiculo().marca());
        verify(oportunidadeRepository).findById(OPORTUNIDADE_ID);
        verify(clienteRepository).findById(CLIENTE_ID);
        verify(veiculoRepository).findById(VEICULO_ID);
        verify(revendaRepository).findById(REVENDA_ID);
        verify(usuarioRepository).findById(USUARIO_ID);
        verify(usuarioRepository).save(usuario);
        verify(oportunidadeRepository).save(oportunidade);
        verify(oportunidadeMapper).toResponseDTO(oportunidade);
    }

    @Test
    void atualizar_Assistente_NaoProprietario_LancaForbidden() {

        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ASSISTENTE);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(jwt.getSubject()).thenReturn("2"); 
        when(oportunidadeRepository.findById(OPORTUNIDADE_ID)).thenReturn(Optional.of(oportunidade));

         
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> oportunidadeService.update(OPORTUNIDADE_ID, oportunidadeRequestDTO));
        assertEquals(FORBIDDEN, exception.getStatusCode());
        assertEquals("Você só pode editar suas próprias oportunidades", exception.getReason());
        verify(oportunidadeRepository).findById(OPORTUNIDADE_ID);
        verifyNoInteractions(clienteRepository, veiculoRepository, revendaRepository, usuarioRepository, oportunidadeMapper);
    }

    @Test
    void atualizar_NaoEncontrado_LancaNotFound() {

        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(oportunidadeRepository.findById(OPORTUNIDADE_ID)).thenReturn(Optional.empty());

         
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> oportunidadeService.update(OPORTUNIDADE_ID, oportunidadeRequestDTO));
        assertEquals(NOT_FOUND, exception.getStatusCode());
        assertEquals("Oportunidade não encontrada", exception.getReason());
        verify(oportunidadeRepository).findById(OPORTUNIDADE_ID);
        verifyNoInteractions(clienteRepository, veiculoRepository, revendaRepository, usuarioRepository, oportunidadeMapper);
    }

    @Test
    void deletar_Admin_Sucesso() {

        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(oportunidadeRepository.findById(OPORTUNIDADE_ID)).thenReturn(Optional.of(oportunidade));


        oportunidadeService.delete(OPORTUNIDADE_ID);


        verify(oportunidadeRepository).findById(OPORTUNIDADE_ID);
        verify(oportunidadeRepository).deleteById(OPORTUNIDADE_ID);
        verifyNoInteractions(clienteRepository, veiculoRepository, revendaRepository, usuarioRepository, oportunidadeMapper);
    }

    @Test
    void deletar_NaoEncontrado_LancaNotFound() {

        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(oportunidadeRepository.findById(OPORTUNIDADE_ID)).thenReturn(Optional.empty());

         
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> oportunidadeService.delete(OPORTUNIDADE_ID));
        assertEquals(NOT_FOUND, exception.getStatusCode());
        assertEquals("Oportunidade não encontrada", exception.getReason());
        verify(oportunidadeRepository).findById(OPORTUNIDADE_ID);
        verifyNoInteractions(clienteRepository, veiculoRepository, revendaRepository, usuarioRepository, oportunidadeMapper);
    }

    @Test
    void distribuirParaAssistente_Sucesso() {

        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(clienteRepository.findById(CLIENTE_ID)).thenReturn(Optional.of(cliente));
        when(veiculoRepository.findById(VEICULO_ID)).thenReturn(Optional.of(veiculo));
        when(revendaRepository.findById(REVENDA_ID)).thenReturn(Optional.of(revenda));
        when(usuarioRepository.findByCargoAndRevendaId(ASSISTENTE, REVENDA_ID)).thenReturn(List.of(usuario));
        when(oportunidadeRepository.save(any(Oportunidade.class))).thenReturn(oportunidade);
        when(oportunidadeMapper.toResponseDTO(oportunidade)).thenReturn(oportunidadeResponseDTO);

        OportunidadeRequestDTO dtoSemUsuario = new OportunidadeRequestDTO(
                CLIENTE_ID, VEICULO_ID, null, REVENDA_ID, STATUS, null
        );

        OportunidadeResponseDTO result = oportunidadeService.save(dtoSemUsuario);

        assertNotNull(result);
        assertEquals(oportunidadeResponseDTO.id(), result.id());
        assertEquals(oportunidadeResponseDTO.cliente().id(), result.cliente().id());
        assertEquals(oportunidadeResponseDTO.cliente().nome(), result.cliente().nome());
        assertEquals(oportunidadeResponseDTO.veiculo().id(), result.veiculo().id());
        assertEquals(oportunidadeResponseDTO.veiculo().marca(), result.veiculo().marca());
        verify(usuarioRepository).findByCargoAndRevendaId(ASSISTENTE, REVENDA_ID);
        verify(oportunidadeRepository).save(any(Oportunidade.class));
        verify(oportunidadeMapper).toResponseDTO(oportunidade);
    }

    @Test
    void distribuirParaAssistente_SemAssistentes_LancaNotFound() {

        jwtAuthUtilMockedStatic.when(JwtAuthUtil::getJwt).thenReturn(jwt);
        jwtAuthUtilMockedStatic.when(() -> JwtAuthUtil.getCargosFromJwt(jwt)).thenReturn(CARGOS_ADMIN);
        when(jwt.getClaimAsString("revendaId")).thenReturn(REVENDA_ID.toString());
        when(clienteRepository.findById(CLIENTE_ID)).thenReturn(Optional.of(cliente));
        when(veiculoRepository.findById(VEICULO_ID)).thenReturn(Optional.of(veiculo));
        when(revendaRepository.findById(REVENDA_ID)).thenReturn(Optional.of(revenda));
        when(usuarioRepository.findByCargoAndRevendaId(ASSISTENTE, REVENDA_ID)).thenReturn(Collections.emptyList());

        OportunidadeRequestDTO dtoSemUsuario = new OportunidadeRequestDTO(
                CLIENTE_ID, VEICULO_ID, null, REVENDA_ID, STATUS, null
        );

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> oportunidadeService.save(dtoSemUsuario));
        assertEquals(NOT_FOUND, exception.getStatusCode());
        assertEquals("Nenhum assistente disponível na revenda", exception.getReason());
        verify(usuarioRepository).findByCargoAndRevendaId(ASSISTENTE, REVENDA_ID);
        verifyNoInteractions(oportunidadeRepository, oportunidadeMapper);
    }
}