package com.project.financeapi.service;

import com.project.financeapi.dto.OperationType.OperationTypeRequestCreateDTO;
import com.project.financeapi.dto.OperationType.OperationTypeRequestUpdateDTO;
import com.project.financeapi.dto.OperationType.OperationTypeResponseDTO;
import com.project.financeapi.dto.ResponseDefaultDTO;
import com.project.financeapi.dto.UpdateStatusRequestDTO;
import com.project.financeapi.dto.operationGroup.OperationGroupResponseDTO;
import com.project.financeapi.dto.util.JwtPayload;
import com.project.financeapi.entity.OperationGroup;
import com.project.financeapi.entity.OperationType;
import com.project.financeapi.entity.User;
import com.project.financeapi.enums.OperationStatus;
import com.project.financeapi.exception.AccessBlockedException;
import com.project.financeapi.exception.BusinessException;
import com.project.financeapi.repository.OperationGroupRepository;
import com.project.financeapi.repository.OperationTypeRepository;
import com.project.financeapi.repository.UserRepository;
import com.project.financeapi.util.JwtUtil;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OperationTypeService {

    private final OperationTypeRepository operationTypeRepository;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final OperationGroupRepository operationGroupRepository;


    public OperationTypeService(
            OperationTypeRepository operationTypeRepository,
            JwtUtil jwtUtil,
            UserRepository userRepository,
            OperationGroupRepository operationGroupRepository
    ) {
        this.operationTypeRepository = operationTypeRepository;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.operationGroupRepository = operationGroupRepository;
    }

    public OperationTypeResponseDTO create(String token, OperationTypeRequestCreateDTO dto){

        JwtPayload userToken = jwtUtil.extractPayload(token);

        User user = userRepository.findById(userToken.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Refaça o login"));


        OperationGroup operationGroup = operationGroupRepository.findById(dto.operationGroupId()).orElseThrow(
                () -> new BusinessException(HttpStatus.NOT_FOUND, "O grupo de operação informado não existe.")
        );

        if(!operationGroup.getIsGlobal() && !operationGroup.getCreatedBy().equals(user)){
            throw new BusinessException(
                    HttpStatus.UNAUTHORIZED, "Você não tem autorização para usar esse grupo de usuário."
            );
        }

        OperationType operationType = operationTypeRepository.save(new OperationType(
                dto.name(),
                dto.movementType(),
                user,
                operationGroup
        ));


        return new OperationTypeResponseDTO(
                operationType.getId(),
                operationType.getName(),
                operationType.getMovementType(),
                operationType.getOperationStatus(),
                operationType.getIsGlobal(),
                new OperationGroupResponseDTO(
                        operationType.getGroup().getId(),
                        operationType.getGroup().getName(),
                        operationType.getGroup().getIsGlobal(),
                        operationType.getGroup().getOperationStatus()
                )
        );
    }

    public List<OperationTypeResponseDTO> findAll(String token){

        JwtPayload userToken = jwtUtil.extractPayload(token);

        User user = userRepository.findById(userToken.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Refaça o login"));

        return operationTypeRepository.findAllByUserOrDefault(user).stream().map(operationType ->
                new OperationTypeResponseDTO(
                        operationType.getId(),
                        operationType.getName(),
                        operationType.getMovementType(),
                        operationType.getOperationStatus(),
                        operationType.getIsGlobal(),
                        new OperationGroupResponseDTO(
                                operationType.getGroup().getId(),
                                operationType.getGroup().getName(),
                                operationType.getIsGlobal(),
                                operationType.getGroup().getOperationStatus()
                        )
                )
        ).toList();

    }

    public ResponseDefaultDTO update(String token, UUID id, OperationTypeRequestUpdateDTO dto){

        JwtPayload userToken = jwtUtil.extractPayload(token);

        User user = userRepository.findById(userToken.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Refaça o login"));

        OperationType  operationType = operationTypeRepository.findByCreatedByAndId(user, id).orElseThrow(
                () -> new BusinessException(HttpStatus.NOT_FOUND, "O TIPO de operação informado, não existe.")
        );

        if(operationType.getIsGlobal()){
            throw new AccessBlockedException("Você não tem permissão para modificar");
        }

        Optional.ofNullable(dto.name()).ifPresent(operationType::setName);
        Optional.ofNullable(dto.movementType()).ifPresent(operationType::setMovementType);

        if(dto.operationGroupId() != null){
            OperationGroup operationGroup = operationGroupRepository.findById(dto.operationGroupId()).orElseThrow(
                    () -> new BusinessException(HttpStatus.NOT_FOUND, "O GRUPO de operação informado, não existe.")
            );

            operationType.setGroup(operationGroup);
        }

        operationTypeRepository.save(operationType);

        return new ResponseDefaultDTO(
                "O tipo de operação foi modificado com sucesso"
        );
    }

    @Transactional
    public ResponseDefaultDTO updateStatusOperationType(String token, UUID id, UpdateStatusRequestDTO dto){

        JwtPayload payload = jwtUtil.extractPayload(token);

        User user = userRepository.findById(payload.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "O usuário informado não existe"));

        OperationType  operationType = operationTypeRepository.findByCreatedByAndId(user, id).orElseThrow(
                () -> new BusinessException(HttpStatus.NOT_FOUND, "O TIPO de operação informado, não existe.")
        );

        operationType.setOperationStatus(dto.operationStatus());

        return new ResponseDefaultDTO(
                "O tipo de operação: " + operationType.getName() + " foi " +
                        (operationType.getOperationStatus() == OperationStatus.ACTIVE ? "Ativada " : "Desativada ") +
                        "com sucesso."
        );

    }
}
