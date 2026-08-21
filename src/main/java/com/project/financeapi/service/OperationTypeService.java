package com.project.financeapi.service;

import com.project.financeapi.dto.OperationType.OperationTypeRequestCreateDTO;
import com.project.financeapi.dto.OperationType.OperationTypeRequestUpdateDTO;
import com.project.financeapi.dto.OperationType.OperationTypeResponseDTO;
import com.project.financeapi.dto.ResponseDefaultDTO;
import com.project.financeapi.entity.DeactivatedOperationType;
import com.project.financeapi.entity.OperationGroup;
import com.project.financeapi.entity.OperationType;
import com.project.financeapi.entity.User;
import com.project.financeapi.enumSystem.StatusEntity;
import com.project.financeapi.exception.BusinessException;
import com.project.financeapi.repository.DeactivatedOperationGroupRepository;
import com.project.financeapi.repository.DeactivatedOperationTypeRepository;
import com.project.financeapi.repository.OperationGroupRepository;
import com.project.financeapi.repository.OperationTypeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OperationTypeService {

    private final OperationTypeRepository operationTypeRepository;
    private final OperationGroupRepository operationGroupRepository;
    private final UserContextService userContextService;

    // Repositórios de Blacklist
    private final DeactivatedOperationTypeRepository deactivatedTypeRepo;
    private final DeactivatedOperationGroupRepository deactivatedGroupRepo;

    @Transactional
    public OperationTypeResponseDTO create(@NotNull OperationTypeRequestCreateDTO dto){
        User user = userContextService.getAuthenticatedUser();

        OperationGroup operationGroup = operationGroupRepository.findByUserIdAndId(user.getId(), dto.operationGroupId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "O grupo de operação informado não existe."));

        // Valida se o grupo está na blacklist
        if(deactivatedGroupRepo.findByUserIdAndOperationGroupId(user.getId(), operationGroup.getId()).isPresent()){
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Não é permitido associar um grupo inativado.");
        }

        validateTypeOperationName(user.getId(), operationGroup.getId(), dto.name(), false);

        OperationType operationType = operationTypeRepository.save(new OperationType(
                dto.name(),
                dto.movementType(),
                user,
                operationGroup
        ));

        return operationType.toResponse(StatusEntity.ACTIVE, StatusEntity.ACTIVE);
    }

    @Transactional
    public ResponseDefaultDTO update(UUID id, OperationTypeRequestUpdateDTO dto){
        User user = userContextService.getAuthenticatedUser();

        OperationType operationType = operationTypeRepository.findByUserIdAndId(user.getId(), id).orElseThrow(
                () -> new BusinessException(HttpStatus.NOT_FOUND, "O TIPO de operação informado não existe.")
        );

        if (operationType.isSystem()) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Não é permitido editar um tipo de operação padrão do sistema.");
        }

        Optional.ofNullable(dto.name()).ifPresent(operationType::setName);
        Optional.ofNullable(dto.movementType()).ifPresent(operationType::setMovementType);

        if(dto.operationGroupId() != null){
            OperationGroup operationGroup = operationGroupRepository.findByUserIdAndId(user.getId(), dto.operationGroupId()).orElseThrow(
                    () -> new BusinessException(HttpStatus.NOT_FOUND, "O GRUPO de operação informado não existe.")
            );

            if(deactivatedGroupRepo.findByUserIdAndOperationGroupId(user.getId(), operationGroup.getId()).isPresent()){
                throw new BusinessException(HttpStatus.BAD_REQUEST, "Não é permitido associar um grupo inativado.");
            }

            operationType.setGroup(operationGroup);
        }

        if(dto.name() != null){
            validateTypeOperationName(user.getId(), operationType.getGroup().getId(), dto.name(), true);
        }

        operationTypeRepository.save(operationType);

        return new ResponseDefaultDTO("O tipo de operação foi modificado com sucesso");
    }

    @Transactional
    public void updateStatusOperationType(UUID id){
        User user = userContextService.getAuthenticatedUser();

        OperationType operationType = operationTypeRepository.findByUserIdAndId(user.getId(), id).orElseThrow(
                () -> new BusinessException(HttpStatus.NOT_FOUND, "O TIPO de operação informado não existe.")
        );

        Optional<DeactivatedOperationType> deactivated = deactivatedTypeRepo.findByUserIdAndOperationTypeId(user.getId(), id);

        if (deactivated.isPresent()) {
            deactivatedTypeRepo.delete(deactivated.get());
        } else {
            deactivatedTypeRepo.save(new DeactivatedOperationType(user, operationType));
        }
    }

    public List<OperationTypeResponseDTO> findAll(){
        User user = userContextService.getAuthenticatedUser();

        List<OperationType> allTypes = operationTypeRepository.findAllOperationTypeUserId(user.getId());
        Set<UUID> deactivatedTypes = deactivatedTypeRepo.findDeactivatedTypeIdsByUserId(user.getId());
        Set<UUID> deactivatedGroups = deactivatedGroupRepo.findDeactivatedGroupIdsByUserId(user.getId());

        return allTypes.stream().map(type -> {
            StatusEntity typeStatus = deactivatedTypes.contains(type.getId()) ? StatusEntity.INACTIVATED : StatusEntity.ACTIVE;
            StatusEntity groupStatus = deactivatedGroups.contains(type.getGroup().getId()) ? StatusEntity.INACTIVATED : StatusEntity.ACTIVE;
            return type.toResponse(typeStatus, groupStatus);
        }).toList();
    }

    public OperationTypeResponseDTO findById(UUID id){
        User user = userContextService.getAuthenticatedUser();

        OperationType operationType = operationTypeRepository.findByUserIdAndId(user.getId(), id).orElseThrow(
                () -> new BusinessException(HttpStatus.NOT_FOUND, "O TIPO de operação informado não existe.")
        );

        boolean isTypeDeactivated = deactivatedTypeRepo.findByUserIdAndOperationTypeId(user.getId(), id).isPresent();
        boolean isGroupDeactivated = deactivatedGroupRepo.findByUserIdAndOperationGroupId(user.getId(), operationType.getGroup().getId()).isPresent();

        return operationType.toResponse(
                isTypeDeactivated ? StatusEntity.INACTIVATED : StatusEntity.ACTIVE,
                isGroupDeactivated ? StatusEntity.INACTIVATED : StatusEntity.ACTIVE
        );
    }

    public List<OperationTypeResponseDTO> findAllOperationStatus(boolean isActive){
        User user = userContextService.getAuthenticatedUser();

        List<OperationType> allTypes = operationTypeRepository.findAllOperationTypeUserId(user.getId());
        Set<UUID> deactivatedTypes = deactivatedTypeRepo.findDeactivatedTypeIdsByUserId(user.getId());
        Set<UUID> deactivatedGroups = deactivatedGroupRepo.findDeactivatedGroupIdsByUserId(user.getId());

        StatusEntity targetStatus = isActive ? StatusEntity.ACTIVE : StatusEntity.INACTIVATED;

        return allTypes.stream()
                .map(type -> {
                    StatusEntity typeStatus = deactivatedTypes.contains(type.getId()) ? StatusEntity.INACTIVATED : StatusEntity.ACTIVE;
                    StatusEntity groupStatus = deactivatedGroups.contains(type.getGroup().getId()) ? StatusEntity.INACTIVATED : StatusEntity.ACTIVE;
                    return type.toResponse(typeStatus, groupStatus);
                })
                .filter(dto -> dto.statusEntity() == targetStatus)
                .toList();
    }

    private void validateTypeOperationName(String userId, UUID groupId, String name, boolean edition) {
        Optional<OperationType> existsName = operationTypeRepository.findAccessibleByName(userId, groupId, name);

        if(existsName.isPresent()){
            if(existsName.get().isSystem() || !edition){
                throw new BusinessException(HttpStatus.CONFLICT, "Já existe um tipo de operação com esse nome ligado a este grupo de operação.");
            }
        }
    }
}