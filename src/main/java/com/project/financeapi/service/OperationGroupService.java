package com.project.financeapi.service;

import com.project.financeapi.dto.ResponseDefault;
import com.project.financeapi.dto.operationGroup.OperationGroupCreateRequestDTO;
import com.project.financeapi.dto.operationGroup.OperationGroupResponseDTO;
import com.project.financeapi.dto.operationGroup.UpdateRequestOperationGroup;
import com.project.financeapi.dto.operationGroup.UpdateStatusRequestOperationGroupDTO;
import com.project.financeapi.dto.util.JwtPayload;
import com.project.financeapi.entity.OperationGroup;
import com.project.financeapi.entity.User;
import com.project.financeapi.enums.OperationStatus;
import com.project.financeapi.exception.AccessBlockedException;
import com.project.financeapi.exception.BusinessException;
import com.project.financeapi.repository.OperationGroupRepository;
import com.project.financeapi.repository.UserRepository;
import com.project.financeapi.util.JwtUtil;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OperationGroupService {

    private final OperationGroupRepository operationGroupRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public OperationGroupService(OperationGroupRepository operationGroupRepository, UserRepository userRepository, JwtUtil jwtUtil) {
        this.operationGroupRepository = operationGroupRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public OperationGroupResponseDTO create(String token, OperationGroupCreateRequestDTO dto) {

        JwtPayload payload = jwtUtil.extractPayload(token);

        User user = userRepository.findById(payload.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "O usuário informado não existe"));

        OperationGroup operationGroup = operationGroupRepository.save(
                new OperationGroup(dto.name(), user)
        );

        return new OperationGroupResponseDTO(
                operationGroup.getId(),
                operationGroup.getName(),
                operationGroup.getIsGlobal(),
                operationGroup.getOperationStatus()
        );
    }

    public List<OperationGroupResponseDTO> findAll(String token) {

        JwtPayload payload = jwtUtil.extractPayload(token);

        User user = userRepository.findById(payload.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "O usuário informado não existe"));

        return operationGroupRepository.findAllByUserOrDefault(user).stream().map(operationGroup ->
            new OperationGroupResponseDTO(
                    operationGroup.getId(),
                    operationGroup.getName(),
                    operationGroup.getIsGlobal(),
                    operationGroup.getOperationStatus()
            )
        ).collect(Collectors.toList());
    }

    public List<OperationGroupResponseDTO> findActive(String token) {

        return this.findAll(token).stream().filter(
                operationGroup -> operationGroup.operationStatus() == OperationStatus.ACTIVE).toList();
    }

    public List<OperationGroupResponseDTO> findInactivated(String token) {

        return this.findAll(token).stream().filter(
                operationGroup -> operationGroup.operationStatus() == OperationStatus.INACTIVATED).toList();
    }

    @Transactional
    public ResponseDefault update(String token, String id, UpdateRequestOperationGroup dto){

        JwtPayload payload = jwtUtil.extractPayload(token);

        User user = userRepository.findById(payload.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "O usuário informado não existe"));


        OperationGroup operationGroup = operationGroupRepository.findById(id).orElseThrow(() -> new BusinessException(
                HttpStatus.NOT_FOUND, "O grupo de operação não foi localizada."
        ));

        if(operationGroup.getIsGlobal() || !operationGroup.getCreatedBy().equals(user)){
            throw new AccessBlockedException("Você não tem permissão para editar este grupo de operação.");
        }

        operationGroup.setName(dto.name());

        operationGroupRepository.save(operationGroup);

        return new ResponseDefault("Grupo de operação editado com sucesso");
    }

    @Transactional
    public ResponseDefault updateStatusOperationGroup(String token, String id, UpdateStatusRequestOperationGroupDTO dto){

        JwtPayload payload = jwtUtil.extractPayload(token);

        User user = userRepository.findById(payload.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "O usuário informado não existe"));


        OperationGroup operationGroup = operationGroupRepository.findById(id).orElseThrow(() -> new BusinessException(
                HttpStatus.NOT_FOUND, "O grupo de operação não foi localizada."
        ));

        if(operationGroup.getIsGlobal() || !operationGroup.getCreatedBy().equals(user)){
            throw new AccessBlockedException("Você não tem permissão para desativar este grupo de operação.");
        }


        operationGroup.setOperationStatus(dto.operationStatus());

        operationGroupRepository.save(operationGroup);

        return new ResponseDefault("O grupo de operação: " + "[" + operationGroup.getName() + "]" + " foi " +
                (operationGroup.getOperationStatus() == OperationStatus.ACTIVE ? "ATIVADO" : "DESATIVADO")
                + " com sucesso.");

    }

}
