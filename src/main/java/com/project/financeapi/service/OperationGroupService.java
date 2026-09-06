package com.project.financeapi.service;

import com.project.financeapi.dto.operationGroup.OperationGroupCreateRequestDTO;
import com.project.financeapi.dto.operationGroup.OperationGroupResponseDTO;
import com.project.financeapi.dto.operationGroup.UpdateRequestOperationGroup;
import com.project.financeapi.entity.DeactivatedOperationGroup;
import com.project.financeapi.entity.OperationGroup;
import com.project.financeapi.entity.User;
import com.project.financeapi.enumSystem.StatusEntity;
import com.project.financeapi.exception.BusinessException;
import com.project.financeapi.repository.DeactivatedOperationGroupRepository;
import com.project.financeapi.repository.OperationGroupRepository;
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
public class OperationGroupService {

    private final OperationGroupRepository operationGroupRepository;
    private final UserContextService userContextService;
    private final DeactivatedOperationGroupRepository deactivatedRepo;

    @Transactional
    public OperationGroupResponseDTO create(
            @NotNull OperationGroupCreateRequestDTO dto
    ) {
        User user = userContextService.getAuthenticatedUser();

        validateGroupName(user.getId(), dto.name(), false);

        OperationGroup group = new OperationGroup(
                dto.name(),
                user
        );

        operationGroupRepository.save(group);

        // Por padrão, todo novo grupo nasce ativo
        return group.toResponse(StatusEntity.ACTIVE);
    }

    @Transactional
    public OperationGroupResponseDTO update(UUID id, @NotNull UpdateRequestOperationGroup dto) {
        User user = userContextService.getAuthenticatedUser();
        OperationGroup operationGroup = validateGroupAccess(user.getId(), id);

        // TRAVA DE SEGURANÇA: Impede edição de grupos do sistema
        if (operationGroup.isSystem()) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Não é permitido editar um grupo de operação padrão do sistema.");
        }

        if(dto.name() != null){
            validateGroupName(user.getId(), dto.name(), true);
            operationGroup.setName(dto.name());
            operationGroup = operationGroupRepository.save(operationGroup);
        }

        // Verifica na blacklist para retornar o status correto no DTO
        boolean isDeactivated = deactivatedRepo.findByUserIdAndOperationGroupId(user.getId(), id).isPresent();
        StatusEntity status = isDeactivated ? StatusEntity.INACTIVATED : StatusEntity.ACTIVE;

        return operationGroup.toResponse(status);
    }

    @Transactional
    public void updateStatusOperationGroup(UUID id) {
        User user = userContextService.getAuthenticatedUser();
        OperationGroup group = validateGroupAccess(user.getId(), id);

        // LÓGICA DE BLACKLIST (OPT-OUT DINÂMICO)
        Optional<DeactivatedOperationGroup> deactivated = deactivatedRepo.findByUserIdAndOperationGroupId(user.getId(), id);

        if (deactivated.isPresent()) {
            // Se está na lista de desativados, o usuário quer ATIVAR. Apagamos o registro.
            deactivatedRepo.delete(deactivated.get());
        } else {
            // Se NÃO está na lista, ele está ativo. O usuário quer DESATIVAR. Salvamos o registro.
            deactivatedRepo.save(new DeactivatedOperationGroup(user, group));
        }
    }

    public List<OperationGroupResponseDTO> findAll() {
        User user = userContextService.getAuthenticatedUser();
        List<OperationGroup> allGroups = operationGroupRepository.findAllOperationGroupUserId(user.getId());
        Set<UUID> deactivatedIds = deactivatedRepo.findDeactivatedGroupIdsByUserId(user.getId());

        return allGroups.stream().map(group -> {
            StatusEntity status = deactivatedIds.contains(group.getId())
                    ? StatusEntity.INACTIVATED
                    : StatusEntity.ACTIVE;

            return group.toResponse(status);
        }).toList();
    }

    public List<OperationGroupResponseDTO> findAllOperationStatus(StatusEntity statusEntity) {
        User user = userContextService.getAuthenticatedUser();

        List<OperationGroup> allGroups = operationGroupRepository.findAllOperationGroupUserId(user.getId());
        Set<UUID> deactivatedIds = deactivatedRepo.findDeactivatedGroupIdsByUserId(user.getId());

        // Filtra em memória usando a blacklist
        return allGroups.stream()
                .filter(group -> {
                    StatusEntity currentStatus = deactivatedIds.contains(group.getId()) ? StatusEntity.INACTIVATED : StatusEntity.ACTIVE;
                    return currentStatus == statusEntity;
                })
                .map(group -> group.toResponse(statusEntity))
                .toList();
    }

    public OperationGroupResponseDTO findById(UUID id) {
        User user = userContextService.getAuthenticatedUser();
        OperationGroup operationGroup = validateGroupAccess(user.getId(), id);

        // Calcula o status dinâmico
        boolean isDeactivated = deactivatedRepo.findByUserIdAndOperationGroupId(user.getId(), id).isPresent();
        StatusEntity status = isDeactivated ? StatusEntity.INACTIVATED : StatusEntity.ACTIVE;

        return operationGroup.toResponse(status);
    }

    private OperationGroup validateGroupAccess(String userId, UUID groupId) {
        // Usa a query que já garante que o grupo pertence ao usuário OU é do sistema
        return operationGroupRepository.findByUserIdAndId(userId, groupId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Grupo de operação não encontrado."));
    }

    private void validateGroupName(String userId, String name, boolean edition) {
        Optional<OperationGroup> existsName = operationGroupRepository.findAccessibleByName(userId, name);

        if(existsName.isPresent()){
            if(existsName.get().isSystem() || !edition){
                throw new BusinessException(HttpStatus.CONFLICT, "Já existe um grupo com esse nome");
            }
        }
    }
}