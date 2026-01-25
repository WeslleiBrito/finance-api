package com.project.financeapi.service;

import com.project.financeapi.dto.OperationType.OperationTypeRequestCreateDTO;
import com.project.financeapi.dto.OperationType.OperationTypeRequestUpdateDTO;
import com.project.financeapi.dto.OperationType.OperationTypeResponseDTO;
import com.project.financeapi.dto.ResponseDefaultDTO;
import com.project.financeapi.dto.util.JwtPayload;
import com.project.financeapi.entity.OperationGroup;
import com.project.financeapi.entity.OperationType;
import com.project.financeapi.entity.User;
import com.project.financeapi.enumSystem.StatusEntity;
import com.project.financeapi.exception.BusinessException;
import com.project.financeapi.repository.OperationGroupRepository;
import com.project.financeapi.repository.OperationTypeRepository;
import com.project.financeapi.repository.UserOperationTypeRepository;
import com.project.financeapi.repository.UserRepository;
import com.project.financeapi.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OperationTypeService {

    private final OperationTypeRepository operationTypeRepository;
    private final UserOperationTypeRepository userOperationTypeRepository;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final OperationGroupRepository operationGroupRepository;

    @Transactional
    public OperationTypeResponseDTO create(String token, @NotNull OperationTypeRequestCreateDTO dto){

        User user = getUser(token);

        OperationGroup operationGroup = operationGroupRepository.findByUserIdAndId(user.getId(), dto.operationGroupId())
            .orElseThrow(
                () -> new BusinessException(HttpStatus.NOT_FOUND, "O grupo de operação informado não existe."));

        if(operationGroup.getStatus() == StatusEntity.INACTIVATED){
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "Não é permitido associar um grupo inativado."
            );
        }


        validateTypeOperationName(user.getId(), operationGroup.getId(), dto.name(), false);

        OperationType operationType = operationTypeRepository.save(new OperationType(
                dto.name(),
                dto.movementType(),
                user,
                operationGroup
        ));


        return operationType.toResponse();
    }

    @Transactional
    public ResponseDefaultDTO update(String token, UUID id, OperationTypeRequestUpdateDTO dto){

        User user = getUser(token);

        OperationType  operationType = operationTypeRepository.findByUserIdAndId(user.getId(), id).orElseThrow(
                () -> new BusinessException(HttpStatus.NOT_FOUND, "O TIPO de operação informado não existe.")
        );

        Optional.ofNullable(dto.name()).ifPresent(operationType::setName);
        Optional.ofNullable(dto.movementType()).ifPresent(operationType::setMovementType);

        if(dto.operationGroupId() != null){

            OperationGroup operationGroup = operationGroupRepository.findByUserIdAndId(user.getId(), dto.operationGroupId()).orElseThrow(
                    () -> new BusinessException(HttpStatus.NOT_FOUND, "O GRUPO de operação informado, não existe.")
            );

            if(operationGroup.getStatus() == StatusEntity.INACTIVATED){
                throw new BusinessException(HttpStatus.BAD_REQUEST,
                        "Não é permitido associar um grupo inativado."
                );
            }

            operationType.setGroup(operationGroup);
        }

        if(dto.name() != null){
            validateTypeOperationName(user.getId(), operationType.getGroup().getId(), dto.name(), true);
        }

        if(dto.movementType() != null){
            operationType.setMovementType(dto.movementType());
        }

        operationTypeRepository.save(operationType);

        return new ResponseDefaultDTO(
                "O tipo de operação foi modificado com sucesso"
        );
    }

    @Transactional
    public void updateStatusOperationType(String token, UUID id){

        User user = getUser(token);

        OperationType  operationType = operationTypeRepository.findByUserIdAndId(user.getId(), id).orElseThrow(
                () -> new BusinessException(HttpStatus.NOT_FOUND, "O TIPO de operação informado, não existe.")
        );

        operationType.setStatus(operationType.getStatus().toggle());

    }

    public List<OperationTypeResponseDTO> findAll(String token){

        User user = getUser(token);

        return operationTypeRepository.findAllOperationTypeUserId(user.getId())
                .stream().map(OperationType::toResponse).toList();

    }

    public OperationTypeResponseDTO findById(String token, UUID id){

        User user = getUser(token);

        OperationType  operationType = operationTypeRepository.findByUserIdAndId(user.getId(), id).orElseThrow(
                () -> new BusinessException(HttpStatus.NOT_FOUND, "O TIPO de operação informado não existe.")
        );

        return operationType.toResponse();
    }

    public List<OperationTypeResponseDTO> findAllOperationStatus(String token, boolean isEnabled){
        User user = getUser(token);

        return userOperationTypeRepository.findAllByUserIdEnabled(user.getId(), isEnabled).stream()
                .map(OperationType::toResponse).toList();
    }

    private User getUser(String token) {
        JwtPayload payload = jwtUtil.extractPayload(token);

        return userRepository.findById(payload.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "O usuário informado não existe"));
    }

    private void validateTypeOperationName(UUID userId, UUID groupId, String name, boolean edition) {

        Optional<OperationType> existsName = operationTypeRepository.findAccessibleByName(userId, groupId, name);

        if(existsName.isPresent()){

            if(existsName.get().isSystem() || !edition){
                throw new BusinessException(
                        HttpStatus.CONFLICT,
                        "Já existe um tipo de operação com esse nome ligado a este grupo de operação."
                );
            }
        }
    }
}
