package com.project.financeapi.service;

import com.project.financeapi.dto.operationGroup_.OperationGroupCreateRequestDTO;
import com.project.financeapi.dto.operationGroup_.OperationGroupResponseDTO;
import com.project.financeapi.dto.operationGroup_.UpdateRequestOperationGroup;
import com.project.financeapi.entity.OperationGroup;
import com.project.financeapi.entity.User;
import com.project.financeapi.entity.UserOperationGroup;
import com.project.financeapi.enumSystem.StatusEntity;
import com.project.financeapi.exception.BusinessException;
import com.project.financeapi.repository.OperationGroupRepository;
import com.project.financeapi.repository.UserOperationGroupRepository;
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
public class OperationGroupService {

    private final OperationGroupRepository operationGroupRepository;
    private final UserOperationGroupRepository userOperationGroupRepository;
    private final UserContextService userContextService;


    @Transactional
    public OperationGroupResponseDTO create(
            @NotNull OperationGroupCreateRequestDTO dto
    ) {
        User user = userContextService.getAuthenticatedUser();

        validateGroupName(user.getId(), dto.name(), false);
        // 1️⃣ Cria o grupo (definição)
        OperationGroup group = new OperationGroup(
                dto.name(),
                user
        );

        operationGroupRepository.save(group);

        // 2️⃣ Cria o vínculo usuário ↔ grupo
        UserOperationGroup link = new UserOperationGroup(
                user,
                group
        );

        userOperationGroupRepository.save(link);

        return group.toResponse();
    }


    @Transactional
    public OperationGroupResponseDTO update(UUID id, @NotNull UpdateRequestOperationGroup dto) {

        User user = userContextService.getAuthenticatedUser();

        OperationGroup operationGroup = validateGroupAccess(user.getId(), id);

        if(dto.name() != null){

            validateGroupName(user.getId(), dto.name(), true);

            operationGroup.setName(dto.name());

            operationGroup = operationGroupRepository.save(operationGroup);

        }

        return operationGroup.toResponse();
    }

    @Transactional
    public void updateStatusOperationGroup(UUID id) {

        User user = userContextService.getAuthenticatedUser();

        UserOperationGroup userOperationGroup = userOperationGroupRepository.findByUserIdAndOperationGroupId(user.getId(), id)
                .orElseThrow(() -> new BusinessException(
                HttpStatus.NOT_FOUND, "O grupo de operação não foi localizada."
        ));

        userOperationGroup.setEnabled(!userOperationGroup.isEnabled());

        userOperationGroupRepository.save(userOperationGroup);

    }

    public List<OperationGroupResponseDTO> findAll() {

        User user = userContextService.getAuthenticatedUser();

        return operationGroupRepository.findAllOperationGroupUserId(user.getId())
                .stream()
                .map(OperationGroup::toResponse)
                .toList();

    }

    public List<OperationGroupResponseDTO> findAllOperationStatus(StatusEntity statusEntity) {

        User user = userContextService.getAuthenticatedUser();

        return userOperationGroupRepository.findVisibleGroupsForUserStatus(user.getId(), statusEntity)
                .stream().map(OperationGroup::toResponse).toList();
    }

    public OperationGroupResponseDTO findById(UUID id) {

        User user = userContextService.getAuthenticatedUser();

        OperationGroup operationGroup = userOperationGroupRepository.findByUserById(user.getId(), id).orElseThrow(
                () -> new BusinessException(
                        HttpStatus.BAD_REQUEST, "O grupo de operação não foi encontrado"
                )
        );

        return operationGroup.toResponse();
    }


    private OperationGroup validateGroupAccess(String userId, UUID groupId)
    {
        OperationGroup group = operationGroupRepository
                .findByIdAndStatus(groupId, StatusEntity.ACTIVE)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Grupo de operação não encontrado."));

        boolean allowed = userOperationGroupRepository.existsByUserIdAndOperationGroupId(userId, groupId);

        if (!allowed) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Grupo não disponível para o usuário");
        }

        return group;
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
